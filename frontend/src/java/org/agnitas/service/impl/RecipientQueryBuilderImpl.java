/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.agnitas.dao.UserStatus;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.RecipientDuplicateSqlOptions;
import org.agnitas.service.RecipientOptions;
import org.agnitas.service.RecipientQueryBuilder;
import org.agnitas.service.RecipientSqlOptions;
import org.agnitas.target.ChainOperator;
import org.agnitas.target.ConditionalOperator;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.web.RecipientForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ProfileField;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.codegen.sql.SqlCode;
import com.agnitas.emm.core.target.eql.parser.EqlParserException;

/**
 * Helper-class for building the sql-query in /recipient/list.jsp
 */
public class RecipientQueryBuilderImpl implements RecipientQueryBuilder {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RecipientQueryBuilderImpl.class);

	// TODO This is part of a workaround a should be removed as soon as possible after recipient search uses the QueryBuilder UI
	private static final transient Pattern TODAY_WITH_OFFSET_PATTERN = Pattern.compile("^\\s*(?:TODAY|SYSDATE|CURRENT_TIMESTAMP|NOW)\\s*(\\+|\\-)\\s*(\\d+)\\s*$");
	
	/** DAO for target groups. */
	protected ComTargetDao targetDao;

	/** Service for accessing DB column metadata. */
	protected ColumnInfoService columnInfoService;
    protected MailinglistApprovalService mailinglistApprovalService;
    
	protected DataSource dataSource;
	
	/** Facade providing full EQL functionality. */
    protected EqlFacade eqlFacade;
	
	/**
	 * Cache variable for the dataSource vendor, so it must not be recalculated everytime.
	 * This variable may be uninitialized before the first execution of the isOracleDB method
	 */
	protected Boolean isOracleDB = null;

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
	public final String createEqlFromForm(final RecipientForm form, final int companyId) {
		final StringBuffer eqlBuffer = new StringBuffer();
		
		final int lastIndex = form.getNumTargetNodes();
		for(int index = 0; index < lastIndex; index++) {
			createEqlFromForm(form, companyId, index, eqlBuffer);
		}
		
		return eqlBuffer.toString();
	}
	
	public final void createEqlFromForm(final RecipientForm form, final int companyId, final int index, final StringBuffer eqlBuffer) {
		final String column = extractColumnFromForm(form, index);
		final String type = determineColumnType(column, companyId);
		
		final String eqlOfNode = convertRuleToEql(form, column, type, index, companyId);

		// In recipient search, conditions are ANDed only
		if(index > 0) {
			if(form.getChainOperator(index) == ChainOperator.OR.getOperatorCode()) {
				eqlBuffer.append(" OR ");
			} else {
				eqlBuffer.append(" AND ");
			}
		}
		
		// Add left parenthesis
		if(form.getParenthesisOpened(index) == 1) {
			eqlBuffer.append('(');
		}
		
		eqlBuffer.append(eqlOfNode);
		
		// Add right parenthesis
		if(form.getParenthesisClosed(index) == 1) {
			eqlBuffer.append(')');
		}
	}

	/**
	 * Converts rule at given index. Parenthesis will be handled by caller!
	 * 
	 * Overwrite this method in sub-classes.
	 */
	protected String convertRuleToEql(final RecipientForm form, final String columnName, final String columnType, final int nodeIndex, final int companyId) {
		switch (columnType) {
			case DbColumnType.GENERIC_TYPE_VARCHAR: // fall-through
			case "VARCHAR2": // fall-through
			case "CHAR":
				return convertStringRuleToEql(form, columnNameToProfileFieldName(columnName, companyId), columnType, nodeIndex);
				
			case DbColumnType.GENERIC_TYPE_INTEGER: // fall-through
			case "DOUBLE": // fall-through
			case DbColumnType.GENERIC_TYPE_FLOAT: // fall-through
			case "NUMBER":
				return convertNumericRuleToEql(form, columnNameToProfileFieldName(columnName, companyId), columnType, nodeIndex);
				
			case DbColumnType.GENERIC_TYPE_DATE: // fall-through
			case DbColumnType.GENERIC_TYPE_DATETIME:
				return convertDateRuleToEql(form, columnNameToProfileFieldName(columnName, companyId), columnType, nodeIndex);
				
			default:
				throw new RuntimeException(String.format("Encountered unhandled column type '%s'", columnType));
		}
	}
	
	private final String columnNameToProfileFieldName(final String columnName, final int companyId) {
		try {
			final ProfileField field = this.columnInfoService.getColumnInfo(companyId, columnName);
			
			return field != null ? field.getShortname() : columnName;
		} catch(final Exception e) {
			logger.error(String.format("Cannot determine shortname for profile field column '%s'", columnName), e);
			
			return columnName;
		}
	}
	
	private final String convertStringRuleToEql(final RecipientForm form, final String profileFieldName, final String columnType, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);
		final String primaryValue = form.getPrimaryValue(nodeIndex);
	
		if(primaryOperator == ConditionalOperator.IS) {
			return "null".equalsIgnoreCase(primaryValue)
					? String.format("`%s` IS EMPTY", profileFieldName)
							: String.format("`%s` IS NOT EMPTY", profileFieldName);
		} else {
			return String.format("`%s` %s \"%s\"", profileFieldName, primaryOperator.getEqlSymbol(), primaryValue);
		}
	}
	
	private final String convertNumericRuleToEql(final RecipientForm form, final String profileFieldName, final String columnType, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);
		final ConditionalOperator secondaryOperator = ConditionalOperator.fromOperatorCode(form.getSecondaryOperator(nodeIndex)).orElse(null);

		if(primaryOperator == ConditionalOperator.MOD) {
			return String.format(
					"`%s` MOD %s %s %s",
					profileFieldName,
					primaryOperator.getEqlSymbol(),
					form.getPrimaryValue(nodeIndex),
					secondaryOperator.getEqlSymbol(),
					form.getSecondaryValue(nodeIndex));
		} else {
			return String.format(
					"`%s` %s %s",
					profileFieldName,
					primaryOperator.getEqlSymbol(),
					form.getPrimaryValue(nodeIndex));
		}
	}
	
	private final String convertDateRuleToEql(final RecipientForm form, final String profileFieldName, final String columnType, final int nodeIndex) {
		final ConditionalOperator primaryOperator = ConditionalOperator.fromOperatorCode(form.getPrimaryOperator(nodeIndex)).orElse(ConditionalOperator.EQ);
		final String primaryValue = form.getPrimaryValue(nodeIndex);
		final String dateFormat = form.getDateFormat(nodeIndex) != null
				? form.getDateFormat(nodeIndex).toUpperCase()
				: "YYYYMMDD";
				
		if(primaryOperator == ConditionalOperator.IS) {
			return "null".equalsIgnoreCase(primaryValue)
					? String.format("`%s` IS EMPTY", profileFieldName)
							: String.format("`%s` IS NOT EMPTY", profileFieldName);
		} else {
			// TODO This is part of a workaround a should be removed as soon as possible after recipient search uses the QueryBuilder UI
			final Matcher todayWithOffsetMatcher = TODAY_WITH_OFFSET_PATTERN.matcher(primaryValue.toUpperCase());
			
			if(todayWithOffsetMatcher.matches()) {
				final String operator = todayWithOffsetMatcher.group(1);
				final String offset = todayWithOffsetMatcher.group(2);
				
				return String.format(
						"%s %s TODAY%s%s DATEFORMAT \"%s\"",
						isNow(profileFieldName) ? "TODAY" : String.format("`%s`", profileFieldName),
						primaryOperator.getEqlSymbol(),
						operator,
						offset,
						dateFormat.toUpperCase());
				
			} else {
				return String.format(
						"%s %s %s DATEFORMAT \"%s\"",
						isNow(profileFieldName) ? "TODAY" : String.format("`%s`", profileFieldName),
						primaryOperator.getEqlSymbol(),
						isNow(primaryValue) ? "TODAY" : String.format("\"%s\"", primaryValue),
						dateFormat.toUpperCase());
			}
		}
	}

	private static final boolean isNow(final String name) {
		return "current_timestamp".equalsIgnoreCase(name)
				|| "sysdate".equalsIgnoreCase(name)
				|| "now".equals(name)
				|| "today".equals(name);
	}
	
	private final String extractColumnFromForm(final RecipientForm form, final int index) {
  		final String column = form.getColumnAndType(index);
  	  	
  		final int indexOfHash = column.indexOf('#');
  		
  		if(indexOfHash != -1) {
  			return column.substring(0, indexOfHash);
  		} else {
  			return column;
  		}
	}
	
	protected String determineColumnType(final String columnName, final int companyId) {
		if ("CURRENT_TIMESTAMP".equalsIgnoreCase(columnName)) {
			return DbColumnType.GENERIC_TYPE_DATE;
		} else {
			try {
				return columnInfoService.getColumnInfo(companyId, columnName).getDataType();
			} catch (Exception e) {
				logger.error(String.format("Cannot find fieldtype for companyId %d and column '%s'", companyId, columnName), e);
				
				return "unknownType";
			}
		}
	}

	@Override
	public SqlPreparedStatementManager getRecipientListSQLStatement(ComAdmin admin, RecipientSqlOptions options) throws Exception {
		return getSqlStatement(admin, options);
	}
	
	private SqlPreparedStatementManager getSqlStatement(final ComAdmin admin, final RecipientSqlOptions options) throws Exception {
		final int companyId = admin.getCompanyID();
		final int adminId = admin.getAdminID();

		if (logger.isInfoEnabled()) {
			logger.info("Creating SQL statement for recipients");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Oracle DB: " + isOracleDB());
		}

		try {
			final SqlPreparedStatementManager statement = new SqlPreparedStatementManager("SELECT * FROM customer_" + companyId + "_tbl cust");

			addTargetWhereClause(options, companyId, statement);

			final String eql = options.getTargetEQL();
			if (StringUtils.isNotEmpty(eql)) {
				final SqlCode sqlCode = eqlFacade.convertEqlToSql(eql, companyId);

				if(sqlCode != null) {
					statement.addWhereClause(sqlCode.getSql());
				}
			}
			
			final boolean checkDisabledMailingLists = mailinglistApprovalService.hasAnyDisabledMailingListsForAdmin(companyId, adminId);
			if (isBindingCheckRequired(options, checkDisabledMailingLists)) {
				final SqlPreparedStatementManager sqlCheckBinding = createBindingCheckQuery(companyId, adminId, options, checkDisabledMailingLists);

				// The mailingListId == -1 means "No binding", but ignored ("All" option used instead) in restricted mode (when checkDisabledMailingLists == true).
				if (options.getListId()>= 0 || checkDisabledMailingLists) {
					// Binding must be present for customer to pass filters.
					statement.addWhereClause(asExistsClause(sqlCheckBinding, true), sqlCheckBinding.getPreparedSqlParameters());
				} else {
					// Binding must be absent for customer to pass.
					statement.addWhereClause(asExistsClause(sqlCheckBinding, false), sqlCheckBinding.getPreparedSqlParameters());
				}
			}

			return statement;
		} catch(final EqlParserException e) {
			logger.warn("Unable to create SQL statement for recipient search", e);
			
			// In case of an error, return a statement that won't show recipients
			final SqlPreparedStatementManager statement = new SqlPreparedStatementManager("SELECT * FROM customer_" + companyId + "_tbl cust ");
			statement.addWhereClause("1 = 0");
			
			return statement;
			
		}
	}

    @Override
    public SqlPreparedStatementManager getDuplicateAnalysisSQLStatement(com.agnitas.beans.ComAdmin admin, RecipientDuplicateSqlOptions options, boolean includeBounceLoad) throws Exception {
	    logger.warn("getDuplicateAnalysisSQLStatement is unsupported!");
        return null;
    }
    
    @Override
    public SqlPreparedStatementManager getDuplicateAnalysisSQLStatement(com.agnitas.beans.ComAdmin admin, RecipientDuplicateSqlOptions options, java.util.List<String> selectedColumns, boolean includeBounceLoad) throws Exception {
        logger.warn("getDuplicateAnalysisSQLStatement is unsupported!");
        return null;
    }

    protected void addBindingCheck(final int companyId, final int adminId, final RecipientOptions options, final boolean checkDisabledMailingLists,
								   final SqlPreparedStatementManager mainStatement) throws Exception {
        SqlPreparedStatementManager sqlCheckBinding = createBindingCheckQuery(companyId, adminId, options, checkDisabledMailingLists);
        // The mailingListId == -1 means "No binding", but ignored ("All" option used instead) in restricted mode (when checkDisabledMailingLists == true).
        if (options.getListId() >= 0 || checkDisabledMailingLists) {
            // Binding must be present for customer to pass filters.
            String whereClause = asExistsClause(sqlCheckBinding, true);

            if (options.isUserTypeEmpty()) {
                SqlPreparedStatementManager checkIfUserStatusNotExists = createBindingCheckQuery(companyId, adminId, 0, 0, "", false);
                whereClause += " OR " + asExistsClause(checkIfUserStatusNotExists, false);
            }
            mainStatement.addWhereClause(whereClause, sqlCheckBinding.getPreparedSqlParameters());
        } else {
            // Binding must be absent for customer to pass.
            mainStatement.addWhereClause(asExistsClause(sqlCheckBinding, false), sqlCheckBinding.getPreparedSqlParameters());
        }
    }

	private String asExistsClause(SqlPreparedStatementManager statement, boolean isPositive) {
		return String.format((isPositive ? "EXISTS (%s)" : "NOT EXISTS (%s)"), statement.getPreparedSqlString());
	}

	protected SqlPreparedStatementManager createBindingCheckQuery(int companyId, int adminId, RecipientOptions options, boolean checkDisabledMailingLists) throws Exception {
		return createBindingCheckQuery(companyId, adminId, options.getListId(), options.getUserStatus(), options.getUserType(), checkDisabledMailingLists);
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
				// Check for valid UserStatus code
				UserStatus.getUserStatusByID(userStatus);

				sqlCheckBinding.addWhereClause("bind.user_status = ?", userStatus);
			}

			if (StringUtils.isNotBlank(userType)) {
				sqlCheckBinding.addWhereClause("bind.user_type = ?", userType);
			}
		}

		return sqlCheckBinding;
	}

	// Checks if a customer binding check (its presence or its absence) to pass a filter (otherwise all unbound customers will be excluded).
    protected boolean isBindingCheckRequired(RecipientOptions options, boolean checkDisabledMailingLists) {
		// Binding check is required in restricted mode to filter by enabled/disabled mailing lists.
		if (checkDisabledMailingLists) {
			return true;
		}

		// Binding check is required to show unbound customers or customers having binding to given mailing list.
		if (options.getListId() != 0) {
			return true;
		}

		// Binding check is required if there's at least one filter by binding's properties.
		return options.getUserStatus() != 0 || StringUtils.isNotBlank(options.getUserType());
	}
	
	protected boolean isOracleDB() {
		if (isOracleDB == null) {
			isOracleDB = DbUtilities.checkDbVendorIsOracle(dataSource);
		}
		return isOracleDB;
	}

	protected void addTargetWhereClause(RecipientOptions options, int companyId, SqlPreparedStatementManager mainStatement) throws Exception {
    	if (options.getTargetId() > 0) {
			mainStatement.addWhereClause(targetDao.getTarget(options.getTargetId(), companyId).getTargetSQL());
		}

    	if (options.getAltgId() > 0 && options.getTargetId() != options.getAltgId()) {
			mainStatement.addWhereClause(targetDao.getTarget(options.getAltgId(), companyId).getTargetSQL());
		}
	}
}
