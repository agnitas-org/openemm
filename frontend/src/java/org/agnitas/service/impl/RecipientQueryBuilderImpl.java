/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.target.TargetNode;
import org.agnitas.target.TargetNodeFactory;
import org.agnitas.target.TargetRepresentation;
import org.agnitas.target.TargetRepresentationFactory;
import org.agnitas.target.impl.TargetNodeDate;
import org.agnitas.target.impl.TargetNodeNumeric;
import org.agnitas.target.impl.TargetNodeString;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.web.RecipientForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.eql.EqlFacade;

/**
 * Helper-class for building the sql-query in /recipient/list.jsp
 */
public class RecipientQueryBuilderImpl implements RecipientQueryBuilder {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RecipientQueryBuilderImpl.class);

	/** DAO for target groups. */
	private ComTargetDao targetDao;
	private ComMailinglistService mailingListService;

	/** Service for accessing DB column metadata. */
	private ColumnInfoService columnInfoService;
    private MailinglistApprovalService mailinglistApprovalService;
    
	private DataSource dataSource;
	
	/** Facade providing full EQL functionality. */
    private EqlFacade eqlFacade;
	
	/**
	 * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
	 * This variable may be uninitialized before the first execution of the isOracleDB method
	 */
	private Boolean isOracleDB = null;

	/**
	 * Set DAO for target groups.
	 * 
	 * @param targetDao DAO for target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}
	  
    @Required
    public void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

	/**
	 * Set Service for mailing lists.
	 *
	 * @param mailingListService service for work with mailing lists;
	 */
	@Required
	public void setMailinglistService(ComMailinglistService mailingListService) {
		this.mailingListService = mailingListService;
	}

	/**
	 * Set service for DB column meta data.
	 * 
	 * @param service ColumnInfoService
	 */
	@Required
	public void setColumnInfoService( ColumnInfoService service) {
		this.columnInfoService = service;
	}
	
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	@Required
    public void setEqlFacade(EqlFacade eqlFacade) {
    	this.eqlFacade = eqlFacade;
    }
	
	public final ColumnInfoService getColumnInfoService() {
		return this.columnInfoService;
	}

    @Override
	public TargetRepresentation createTargetRepresentationFromForm(RecipientForm form, TargetRepresentationFactory targetRepresentationFactory, TargetNodeFactory targetNodeFactory, int companyID) {
		TargetRepresentation target = targetRepresentationFactory.newTargetRepresentation();

		int lastIndex = form.getNumTargetNodes();

		for (int index = 0; index < lastIndex; index++) {
    		String column = form.getColumnAndType(index);
    		if (column.contains("#")) {
    			column = column.substring(0, column.indexOf('#'));
    		}
    		String type = "unknownType";
    		if ("CURRENT_TIMESTAMP".equalsIgnoreCase(column)) {
    			type = "DATE";
    		} else {
				try {
					type = columnInfoService.getColumnInfo(companyID, column).getDataType();
				} catch (Exception e) {
					logger.error("Cannot find fieldtype for companyId " + companyID + " and column '" + column + "'", e);
				}
    		}

			form.setColumnName(index, column);

			TargetNode node;

			if (type.equalsIgnoreCase("VARCHAR") || type.equalsIgnoreCase("VARCHAR2") || type.equalsIgnoreCase("CHAR")) {
				node = createStringNode(form, column, type, index, targetNodeFactory);
			} else if (type.equalsIgnoreCase("INTEGER") || type.equalsIgnoreCase("DOUBLE") || type.equalsIgnoreCase("NUMBER")) {
				node = createNumericNode(form, column, type, index, targetNodeFactory);
			} else if (type.equalsIgnoreCase("DATE")) {
				node = createDateNode(form, column, type, index, targetNodeFactory);
			} else {
				throw new RuntimeException("Unknown type found");
			}

			if(node.getChainOperator() == TargetNode.CHAIN_OPERATOR_NONE) {
				node.setChainOperator(TargetNode.CHAIN_OPERATOR_AND);
			}
			
			target.addNode(node);
		}

		return target;
	}

	/**
	 * Construct a sql query from all the provided parameters.
	 * 
	 * "optimized" means, that there are too many customers to show (> 10.000), so the view is shortened to a more ore less randomized set of customers for an simple view.
	 * This action is being taken to speed up the db interaction
	 */
	@Override
	public SqlPreparedStatementManager getSQLStatement(HttpServletRequest request, RecipientForm aForm, TargetRepresentationFactory targetRepresentationFactory, TargetNodeFactory targetNodeFactory) throws Exception {
		final int companyId = AgnUtils.getCompanyID(request);
		final int adminId = AgnUtils.getAdminId(request);

		if (logger.isInfoEnabled()) {
			logger.info("Creating SQL statement for recipients");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Oracle DB: " + isOracleDB());
		}

		if (!aForm.checkParenthesisBalance()) {
			if(logger.isInfoEnabled()) {
				logger.info("Parenthesis is unbalanced for recipient search");
			}

			SqlPreparedStatementManager mainStatement = new SqlPreparedStatementManager("SELECT * FROM customer_" + companyId + "_tbl cust ");
			mainStatement.addWhereClause("1 = 0");
			
			return mainStatement;
		}
		
		String sort = request.getParameter("sort");
		if (sort == null) {
			sort = aForm.getSort();
			
			if (logger.isDebugEnabled()) {
				logger.debug("request parameter sort = null");
				logger.debug("using form parameter sort = " + sort);
			}
		}

		String direction = request.getParameter("dir");
		if (direction == null) {
			direction = aForm.getOrder();
			
			if (logger.isDebugEnabled()) {
				logger.debug("request parameter dir = null");
				logger.debug("using form parameter order = " + direction);
			}
		}

		if (request.getParameter("listID") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter listID = " + request.getParameter("listID"));
			}

			aForm.setListID(Integer.parseInt(request.getParameter("listID")));
		}

		if (request.getParameter("targetID") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter targetID = " + request.getParameter("targetID"));
			}

			aForm.setTargetID(Integer.parseInt(request.getParameter("targetID")));
		}

		if (request.getParameter("user_type") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter user_type = " + request.getParameter("user_type"));
			}

			aForm.setUser_type(request.getParameter("user_type"));
		}

		if (request.getParameter("searchFirstName") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter searchFirstName = " + request.getParameter("searchFirstName"));
			}

			aForm.setSearchFirstName(request.getParameter("searchFirstName"));
		}

		if (request.getParameter("searchLastName") != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("parameter searchLastName = " + request.getParameter("searchLastName"));
			}

			aForm.setSearchLastName(request.getParameter("searchLastName"));
		}

		if (request.getParameter("searchEmail") != null) {
			aForm.setSearchEmail(request.getParameter("searchEmail"));
		}

		if (request.getParameter("user_status") != null) {
			aForm.setUser_status(Integer.parseInt(request.getParameter("user_status")));
		}

		SqlPreparedStatementManager mainStatement = new SqlPreparedStatementManager("SELECT * FROM customer_" + companyId + "_tbl cust");

		final int mailingListId = aForm.getListID();
		final String userType = aForm.getUser_type();
		final int userStatus = aForm.getUser_status();

		if (aForm.getTargetID() > 0) {
			mainStatement.addWhereClause(targetDao.getTarget(aForm.getTargetID(), companyId).getTargetSQL());
		}

		TargetRepresentation targetRep = createTargetRepresentationFromForm(aForm, targetRepresentationFactory, targetNodeFactory, companyId);
		if (targetRep.checkBracketBalance() && CollectionUtils.isNotEmpty(targetRep.getAllNodes())) {
			String targetSql = eqlFacade.convertEqlToSql(eqlFacade.convertTargetRepresentationToEql(targetRep, companyId), companyId).getSql();
			if (StringUtils.isNotBlank(targetSql)) {
				mainStatement.addWhereClause(targetSql);
			}
		}

		boolean checkDisabledMailingLists = mailinglistApprovalService.hasAnyDisabledMailingListsForAdmin(companyId, adminId);

		SqlPreparedStatementManager sqlCheckBinding = createBindingCheckQuery(companyId, adminId, mailingListId, userStatus, userType, checkDisabledMailingLists);

		if (isBindingCheckRequired(mailingListId, userStatus, userType, checkDisabledMailingLists)) {
			// The mailingListId == -1 means "No binding", but ignored ("All" option used instead) in restricted mode (when checkDisabledMailingLists == true).
			if (mailingListId >= 0 || checkDisabledMailingLists) {
				// Binding must be present for customer to pass filters.
				mainStatement.addWhereClause(asExistsClause(sqlCheckBinding, true), sqlCheckBinding.getPreparedSqlParameters());
			} else {
				// Binding must be absent for customer to pass.
				mainStatement.addWhereClause(asExistsClause(sqlCheckBinding, false), sqlCheckBinding.getPreparedSqlParameters());
			}
		}

		return mainStatement;
	}

	private String asExistsClause(SqlPreparedStatementManager statement, boolean isPositive) {
		return String.format((isPositive ? "EXISTS (%s)" : "NOT EXISTS (%s)"), statement.getPreparedSqlString());
	}

	protected SqlPreparedStatementManager createBindingCheckQuery(int companyId, int adminId, int mailingListId, int userStatus, String userType, boolean checkDisabledMailingLists) throws Exception {
		SqlPreparedStatementManager sqlCheckBinding;

		sqlCheckBinding = new SqlPreparedStatementManager("SELECT 1 FROM customer_" + companyId + "_binding_tbl bind");

		sqlCheckBinding.addWhereClause("bind.customer_id = cust.customer_id");

		if (checkDisabledMailingLists) {
			sqlCheckBinding.addWhereClause("dis.mailinglist_id IS NULL");
		}

		if (mailingListId > 0) {
			sqlCheckBinding.addWhereClause("bind.mailinglist_id = ?", mailingListId);
		}

		// The mailingListId < 0 means "No binding", but this option is ignored in restricted mode (when disabled mailing lists to be checked).
		if (mailingListId >= 0 || checkDisabledMailingLists) {
			if (userStatus != 0) {
				sqlCheckBinding.addWhereClause("bind.user_status = ?", userStatus);
			}

			if (StringUtils.isNotBlank(userType)) {
				sqlCheckBinding.addWhereClause("bind.user_type = ?", userType);
			}
		}

		return sqlCheckBinding;
	}

	// Checks if a customer binding check (its presence or its absence) to pass a filter (otherwise all unbound customers will be excluded).
	private boolean isBindingCheckRequired(int mailingListId, int userStatus, String userType, boolean checkDisabledMailingLists) {
		// Binding check is required in restricted mode to filter by enabled/disabled mailing lists.
		if (checkDisabledMailingLists) {
			return true;
		}

		// Binding check is required to show unbound customers or customers having binding to given mailing list.
		if (mailingListId < 0 || mailingListId > 0) {
			return true;
		}

		// Binding check is required if there's at least one filter by binding's properties.
		return userStatus != 0 || StringUtils.isNotBlank(userType);
	}

	protected TargetNodeString createStringNode(RecipientForm form, String column, String type, int index, TargetNodeFactory factory) {
		String primaryValue = form.getPrimaryValue(index)
				.replaceAll("\\*", "%")
				.replaceAll("\\?", "_");

		return factory.newStringNode(form.getChainOperator(index), form.getParenthesisOpened(index), column, type,
				form.getPrimaryOperator(index), primaryValue, form.getParenthesisClosed(index));
	}

	protected TargetNodeNumeric createNumericNode(RecipientForm form, String column, String type, int index, TargetNodeFactory factory) {
		int primaryOperator = form.getPrimaryOperator(index);
		int secondaryOperator = form.getSecondaryOperator(index);
		int secondaryValue = 0;

		if (primaryOperator == TargetNode.OPERATOR_MOD.getOperatorCode()) {
			try {
				secondaryOperator = Integer.parseInt(form.getSecondaryValue(index));
			} catch (Exception e) {
				secondaryOperator = TargetNode.OPERATOR_EQ.getOperatorCode();
			}
			try {
				secondaryValue = Integer.parseInt(form.getSecondaryValue(index));
			} catch (Exception e) {
				secondaryValue = 0;
			}
		}

		return factory.newNumericNode(form.getChainOperator(index), form.getParenthesisOpened(index), column, type, primaryOperator, form.getPrimaryValue(index),
				secondaryOperator, secondaryValue, form.getParenthesisClosed(index));
	}

	protected TargetNodeDate createDateNode(RecipientForm form, String column, String type, int index, TargetNodeFactory factory) {
		return factory.newDateNode(form.getChainOperator(index), form.getParenthesisOpened(index), column, type, form.getPrimaryOperator(index), form.getDateFormat(index),
				form.getPrimaryValue(index), form.getParenthesisClosed(index));
	}
	
	protected boolean isOracleDB() {
		if (isOracleDB == null) {
			isOracleDB = DbUtilities.checkDbVendorIsOracle(dataSource);
		}
		return isOracleDB;
	}
}
