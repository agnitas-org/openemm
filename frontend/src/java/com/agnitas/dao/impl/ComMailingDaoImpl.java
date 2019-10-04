/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.beans.Mediatype;
import org.agnitas.beans.TrackableLink;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.beans.impl.MailingBaseImpl;
import org.agnitas.beans.impl.MailingSendStatusImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.DynamicTagContentDao;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.LightweightMailingRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.beans.LightweightMailingImpl;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.FulltextSearchQueryException;
import org.agnitas.util.ParameterParser;
import org.agnitas.util.importvalues.MailType;
import org.agnitas.web.MailingAdditionalColumn;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.ComMailing.MailingContentType;
import com.agnitas.beans.ComRdirMailingData;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.ComMailingImpl;
import com.agnitas.beans.impl.ComRdirMailingDataImpl;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.beans.impl.MediatypeEmailImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.dao.ComUndoDynContentDao;
import com.agnitas.dao.ComUndoMailingComponentDao;
import com.agnitas.dao.ComUndoMailingDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.LinkServiceImpl;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtreport.util.BirtReportSettingsUtils;
import com.agnitas.emm.core.commons.database.DatabaseInformation;
import com.agnitas.emm.core.commons.database.DatabaseInformationException;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.MailingDataException;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.ComFollowUpStats;
import com.agnitas.emm.core.mailing.dao.ComMailingParameterDao;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.grid.grid.dao.ComGridTemplateDao;
import com.agnitas.predelivery.dao.ComPredeliveryDao;
import com.agnitas.util.SpecialCharactersWorker;

/**
 * TODO: Check concatenated SQl statements for sql-injection possibilities,
 *       replace where possible by real ?-parameters, also for better db performance
 */
public class ComMailingDaoImpl extends PaginatedBaseDaoImpl implements ComMailingDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComMailingDaoImpl.class);

    public static final String UNSENT_MAILING_QUERY = "SELECT m.company_id, m.mailing_id, m.shortname, m.description " +
            "FROM mailing_tbl m WHERE m.company_id = ? AND m.is_template = 0 AND m.deleted = 0 AND " +
            "NOT EXISTS (select 1 from maildrop_status_tbl " +
            "WHERE status_field = ? AND mailing_id = m.mailing_id)";
    public static final String SENT_MAILING_QUERY = "SELECT m.company_id, m.mailing_id, m.shortname, m.description " +
            "FROM maildrop_status_tbl md LEFT JOIN mailing_tbl m " +
            "ON (md.mailing_id = m.mailing_id) WHERE md.status_field = ? AND md.company_id = ? AND m.is_template = 0 AND m.deleted = 0 ";

	// All characters (except digits) allowed in a target expression
	public static final String TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS = "!( )&|";

	private static final String ADVERTISING_TYPE = "advertising";

	private static final String DATE_ORDER_KEY = "date";

	private static final PartialMailingBaseRowMapper PARTIAL_MAILING_BASE_ROW_MAPPER = new PartialMailingBaseRowMapper();

	private FulltextSearchQueryGenerator fulltextSearchQueryGenerator;

	private Boolean isGridTemplateSupported;
	
	private ComGridTemplateDao gridTemplateDao;

	
	public void setGridTemplateDao(ComGridTemplateDao gridTemplateDao) {
		this.gridTemplateDao = gridTemplateDao;
	}
	
    @Required
    public void setFulltextSearchQueryGenerator(final FulltextSearchQueryGenerator fulltextSearchQueryGenerator) {
        this.fulltextSearchQueryGenerator = fulltextSearchQueryGenerator;
    }

	public static String getSuccessTableName(@VelocityCheck final int companyId) {
		return new StringBuilder("success_").append(companyId).append("_tbl").toString();
	}

	public static String getBindingTableName(@VelocityCheck final int companyId) {
		return new StringBuilder("customer_").append(companyId).append("_binding_tbl").toString();
	}

    private ComUndoMailingDao undoMailingDao;
    private ComCompanyDao companyDao;
	private ComMailingComponentDao mailingComponentDao;

	/** DAO accessing target groups. */
	private ComTargetDao targetDao;
	private ComUndoMailingComponentDao undoMailingComponentDao;
	private ComUndoDynContentDao undoDynContentDao;
	private ComTrackableLinkDao comTrackableLinkDao;
    private DatabaseInformation databaseInformation;
    private ConfigService configService;
    private MaildropStatusDao maildropStatusDao;
    private DynamicTagContentDao dynamicTagContentDao;
    private DynamicTagDao dynamicTagDao;

	/** DAO for accessing media types. */
	private MediatypesDao mediatypesDao;

	private ComPredeliveryDao predeliveryDao;

	/**
	 * Holds value of property applicationContext.
	 */
	protected ApplicationContext applicationContext;

	// ---------------------------------------------------------------------------------------------------------------------------------------
	// dependency injection code

	/**
	 * Injection-setter for MediatypesDao.
	 *
	 * @param mediatypesDao - DAO
	 */
	public void setMediatypesDao( final MediatypesDao mediatypesDao) {
		this.mediatypesDao = mediatypesDao;
	}

	/**
	 * Injection-setter for ComUndoMailingDao.
	 *
	 * @param undoMailingDao - DAO
	 */
	public void setUndoMailingDao(final ComUndoMailingDao undoMailingDao) {
		this.undoMailingDao = undoMailingDao;
	}

	@Required
	public void setCompanyDao(final ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	/**
	 * Injection-setter for ComUndoMailingComponentDao.
	 *
	 * @param undoMailingComponentDao - DAO
	 */
	public void setUndoMailingComponentDao(final ComUndoMailingComponentDao undoMailingComponentDao) {
		this.undoMailingComponentDao = undoMailingComponentDao;
	}

	/**
	 * Injection-setter for ComUndoDynContentDao.
	 *
	 * @param undoDynContentDao - DAO
	 */
	public void setUndoDynContentDao(final ComUndoDynContentDao undoDynContentDao) {
		this.undoDynContentDao = undoDynContentDao;
	}

	@Required
	public void setConfigService(final ConfigService service) {
		this.configService = service;
	}

	public void setComTrackableLinkDao(final ComTrackableLinkDao comTrackableLinkDao) {
		this.comTrackableLinkDao = comTrackableLinkDao;
	}

	/**
	 * Injection-setter for TargetDao.
	 *
	 * @param targetDao DAO
	 */
	public void setTargetDao(final ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

    @Required
    public void setDatabaseInformation(final DatabaseInformation info) {
    	this.databaseInformation = info;
    }

	/**
	 * Injection-setter for ComMailingComponentDao.
	 *
	 * @param componentDao
	 *            DAO
	 */
	public void setMailingComponentDao(final ComMailingComponentDao componentDao) {
		this.mailingComponentDao = componentDao;
	}

	// ---------------------------------------------------------------------------------------------------------------------------------------
	// business logic

	/**
	 * This method returns a sorted list with Lightweight Mailings. The size of
	 * the list (=amount of mailings) is given by the count parameter. Returned
	 * are the last ones, which means the mailings with the highest mailing-id.
	 * If a mailingStatus is given (can be 'W', 'T' or 'A'), a filtering is done
	 * to only this mailings. So if you want a World-Mailing, use
	 * mailingStatus="W". If you dont want a filter, use "" or null.
	 *
	 * ATTENTION! NOT ALL FIELDS WILL BE FILLED!!!! JUST THE FIELDS NEEDED FOR
	 * SOME SPECIAL CASES ARE USED. IF YOU NEED MORE YOU HAVE TO ADD THE NEEDED
	 * FIELDS. TO DO THIS FIND THE MAPPING AND ADD YOUR STATEMENT.
	 *
	 * @param companyID
	 * @param count
	 * @param mailingStatus
	 * @return
	 */
	@Override
	public List<ComMailing> getMailings(@VelocityCheck final int companyID, final int count, final String mailingStatus, final boolean takeMailsForPeriod) {
		final Vector<Object> parameters = new Vector<>();

		String sql;
		// create SQL-Statement for filtered query.
		if (mailingStatus == null || mailingStatus.equals("W") || mailingStatus.equals("T") || mailingStatus.equals("A")) {
			sql = "SELECT a.company_id, a.mailing_id, a.shortname, a.content_type, a.description, a.deleted, a.mailtemplate_id, a.mailinglist_id, a.mailing_type, a.campaign_id, a.archived, a.target_expression, a.split_id, a.creation_date, a.test_lock, a.is_template, a.needs_target, a.openaction_id, a.clickaction_id, a.statmail_recp, a.dynamic_template, a.plan_date, a.priority, a.is_prioritization_allowed"
					+ " FROM mailing_tbl a, maildrop_status_tbl b WHERE a.company_id = ?";
			parameters.add(companyID);

			if (mailingStatus != null) {
				sql += " AND b.status_field = ?";
				parameters.add(mailingStatus);
			}

			if (takeMailsForPeriod) {
				final int numDays = companyDao.getSuccessDataExpirePeriod(companyID);
				if (numDays > 0) {
					if (isOracleDB()) {
						sql += " AND b.senddate > (SYSDATE " + (-numDays) + " )";
					} else {
						sql += " AND b.senddate > DATE_ADD(now(), interval " + (-numDays) + " day) ";
					}
				}
			}
			sql += " AND a.mailing_id = b.mailing_id ORDER BY a.mailing_id DESC";
		} else {
			sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, dynamic_template, plan_date, priority, is_prioritization_allowed FROM mailing_tbl WHERE company_id = ? ORDER BY mailing_id DESC";
			parameters.add(companyID);
		}

		// Check, if we have to return all values.
		// If count <= 0 then all mailings are returned.
		if (count > 0) {
			if (isOracleDB()) {
				sql = "SELECT * from (" + sql + ") WHERE rownum <= ?";
			} else {
				sql += " LIMIT ?";
			}
			parameters.add(count);
		}

		try {
			return select(logger, sql, new ComMailingRowMapper(), parameters.toArray());
		} catch (final Exception e) {
			return new LinkedList<>();
		}
	}

	/**
	 * This method returns a sorted list with Lightweight Mailings. Only
	 * CompanyID, MailingID, Description and Shortname are filled into the
	 * object.
	 *
	 * ATTENTION! NOT ALL FIELDS WILL BE FILLED!!!! JUST THE FIELDS NEEDED FOR
	 * SOME SPECIAL CASES ARE USED. IF YOU NEED MORE YOU HAVE TO ADD THE NEEDED
	 * FIELDS. TO DO THIS FIND THE MAPPING AND ADD YOUR STATEMENT.
	 *
	 * @param companyID
	 * @return List<LightweightMailing>
	 */
	@Override
	public List<LightweightMailing> getMailingNames(@VelocityCheck final int companyID) {
		final String sql = "SELECT company_id, mailing_id, shortname, description from mailing_tbl WHERE company_id = ? AND deleted = 0 ORDER BY shortname ASC";
		return select(logger, sql, new LightweightMailingRowMapper(), companyID);
	}

    @Override
    public List<LightweightMailing> getAllMailingsSorted(@VelocityCheck final int companyId, final String sortField, final String sortDirection) {
        final List<LightweightMailing> allMailings = select(logger, addUnsentMailingSort(UNSENT_MAILING_QUERY, sortField, sortDirection),
                new LightweightMailingRowMapper(), companyId, MaildropStatus.WORLD.getCodeString());
        allMailings.addAll(select(logger, addSentMailingSort(SENT_MAILING_QUERY, sortField, sortDirection),
                new LightweightMailingRowMapper(), MaildropStatus.WORLD.getCodeString(), companyId));
        return allMailings;
    }

    /**
	 * Method for WorkflowManager. Returns list of all mailings: unsent mailings ordered by creation date; sent
	 * mailings ordered by send date
	 *
	 * @param companyID
	 * @return list of all mailings
	 */
	@Override
	public List<LightweightMailing> getMailingsDateSorted(@VelocityCheck final int companyID) {
		final List<LightweightMailing> allMailings = select(logger, addSortingToQuery(UNSENT_MAILING_QUERY, "creation_date", "desc"),
                new LightweightMailingRowMapper(), companyID, MaildropStatus.WORLD.getCodeString());
		allMailings.addAll(select(logger, addSortingToQuery(SENT_MAILING_QUERY, "senddate", "desc"),
                new LightweightMailingRowMapper(), MaildropStatus.WORLD.getCodeString(), companyID));
		return allMailings;
	}

    @Override
    public List<Map<String, Object>> getMailingsNamesByStatus(@VelocityCheck final int companyID, final List<Integer> mailingTypes, final String workStatus, final String sort, final String order) {
        return getMailingsNamesByStatus(companyID, mailingTypes, workStatus, null, false, sort, order);
    }

    @Override
    public List<Map<String, Object>> getMailings(@VelocityCheck final int companyId, final String commaSeparatedMailingIds) {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, dynamic_template, plan_date, work_status FROM mailing_tbl ")
                .append("WHERE company_id = ? ")
                .append("  AND mailing_id IN (").append(commaSeparatedMailingIds).append(") ")
                .append("ORDER BY mailing_id DESC");
        return select(logger, builder.toString(), companyId);
    }
    
    @Override
    public List<Integer> getMailingIds(@VelocityCheck int companyId) {
        return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = ?", new IntegerRowMapper(), companyId);
    }
    
    @Override
    public List<Map<String, Object>> getMailingsNamesByStatus(@VelocityCheck final int companyID, final List<Integer> mailingTypes, final String workStatus, final String mailingStatus, final boolean takeMailsForPeriod, final String sort, final String order) {
        final Vector<Object> parameters = new Vector<>();
        parameters.add(companyID);

        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
            .append("SELECT a.company_id, a.mailing_id, a.shortname, a.description, a.work_status, b.senddate, ")
            .append("a.change_date, ");

        if (isOracleDB()) {
            queryBuilder
				.append("(SELECT LISTAGG(dep.workflow_id, ',') WITHIN GROUP (ORDER BY dep.workflow_id) ")
				.append("FROM workflow_dependency_tbl dep WHERE dep.company_id = ")
				.append(companyID)
				.append(" AND dep.type = ")
				.append(WorkflowDependencyType.MAILING_DELIVERY.getId())
				.append(" AND dep.entity_id = a.mailing_id) AS usedInWorkflows ");
        } else {
            queryBuilder
				.append("(SELECT GROUP_CONCAT(dep.workflow_id ORDER BY dep.workflow_id SEPARATOR ',') ")
				.append("FROM workflow_dependency_tbl dep WHERE dep.company_id = ")
				.append(companyID)
				.append(" AND dep.type = ")
				.append(WorkflowDependencyType.MAILING_DELIVERY.getId())
				.append(" AND dep.entity_id = a.mailing_id) AS usedInWorkflows ");
        }

        queryBuilder
            .append(", (CASE WHEN (a.work_status = 'mailing.status.sent' OR a.work_status = 'mailing.status.scheduled' OR a.work_status = 'mailing.status.norecipients') THEN 2 ELSE 1 END) AS sent_sort_status ")
            .append(", (CASE WHEN (a.work_status = 'mailing.status.sent' OR a.work_status = 'mailing.status.scheduled' OR a.work_status = 'mailing.status.norecipients') THEN b.senddate ELSE a.creation_date END) AS sent_sort_date ")
            .append(", (CASE WHEN (a.work_status = 'mailing.status.active') THEN 2 ELSE 1 END) AS active_sort_status ")
            .append(", (CASE WHEN (a.work_status = 'mailing.status.active') THEN b.senddate ELSE a.creation_date END) AS active_sort_date ")
                .append("FROM mailing_tbl a ")
            .append("LEFT OUTER JOIN( ");

        if (isOracleDB()) {
            queryBuilder
                    .append("SELECT DISTINCT mailing_id, senddate, genchange FROM ( ")
                        .append("SELECT mailing_id, senddate, genchange, MAX(genchange) OVER (partition by mailing_id) max_date ")
                    .append("FROM maildrop_status_tbl ) WHERE max_date = genchange");
        } else {
            queryBuilder
                    .append("SELECT DISTINCT mds.mailing_id, mds.senddate, mds.genchange FROM maildrop_status_tbl mds ")
                    .append("INNER JOIN (SELECT mailing_id, MAX(genchange) AS max_date FROM maildrop_status_tbl GROUP BY mailing_id) max_genchange ")
                    .append("ON mds.mailing_id = max_genchange.mailing_id AND mds.genchange = max_genchange.max_date");
        }

        queryBuilder
            .append(") b ON (a.mailing_id = b.mailing_id) ");

        if (mailingStatus != null || takeMailsForPeriod) {
            queryBuilder.append("LEFT JOIN maildrop_status_tbl md ON md.mailing_id = a.mailing_id ");
        }

        queryBuilder
            .append("WHERE a.company_id = ? AND a.is_template = 0 AND a.mailing_type IN (")
            .append(StringUtils.join(mailingTypes, ", "))
            .append(") AND deleted = 0");

        if (mailingStatus != null) {
            queryBuilder.append(" AND md.status_field = ?");
            parameters.add(mailingStatus);
        }

        if (takeMailsForPeriod) {
            final int numDays = companyDao.getSuccessDataExpirePeriod(companyID);
            if (numDays > 0) {
	            if (isOracleDB()) {
	                queryBuilder.append(" AND b.senddate > (SYSDATE ").append(-numDays).append(" )");
	            } else {
	                queryBuilder.append(" AND b.senddate > DATE_ADD(now(), interval ").append(-numDays).append(" day) ");
	            }
            }
        }

        if (workStatus != null) {
            if ("unsent".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status != 'mailing.status.sent' AND a.work_status != 'mailing.status.scheduled' AND a.work_status != 'mailing.status.norecipients') ");
            } else if ("sent_scheduled".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status = 'mailing.status.sent' OR a.work_status = 'mailing.status.scheduled' OR a.work_status = 'mailing.status.norecipients') ");
            } else if ("active".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status = 'mailing.status.active') ");
            } else if ("inactive".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status != 'mailing.status.active') ");
            }
        }

        if (!"".equalsIgnoreCase(sort)) {
            queryBuilder.append(" ORDER BY ").append(sort);
        }

        if (order != null) {
            queryBuilder.append(" ").append(order);
        } else{
            queryBuilder.append(" ASC");
        }

        return select(logger, queryBuilder.toString(), parameters.toArray());
    }

	@Override
	public List<LightweightMailing> getMailingsDependentOnTargetGroup(@VelocityCheck final int companyID, final int targetGroupID) {
		final String sqlGetDependentMailings = "SELECT company_id, mailing_id, shortname, description " +
				"FROM mailing_tbl " +
				"WHERE company_id = ? AND deleted = 0 AND (" + createTargetExpressionRestriction() + ") " +
				"ORDER BY change_date DESC, mailing_id DESC";

		return select(logger, sqlGetDependentMailings, new LightweightMailingRowMapper(), companyID, targetGroupID);
	}

	@Override
	public boolean existMailingsDependsOnTargetGroup(final int companyID, final int targetGroupID){
		final String query = "SELECT 1 FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND (" + createTargetExpressionRestriction() + ")";
		return existsAtLeastOneRow(logger, query, companyID, targetGroupID);
	}

	@Override
	public boolean existsNotSentMailingsDependsOnTargetGroup(final int companyID, final int targetGroupID){
		final String query = "SELECT 1 FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND work_status != 'mailing.status.sent' AND (" + createTargetExpressionRestriction() + ")";
		return existsAtLeastOneRow(logger, query, companyID, targetGroupID);
	}

	@Override
	public boolean existMailingsWhichComponentDependsOnTargetGroup(final int companyID, final int targetGroupID){
		final String query =
				"SELECT 1 " +
				"FROM mailing_tbl m INNER JOIN component_tbl c " +
				"    ON c.mailing_id = m.mailing_id " +
				"WHERE m.company_id = ? AND m.deleted = 0 AND c.target_id = ?";
		return existsAtLeastOneRow(logger, query, companyID, targetGroupID);
	}

	@Override
	public boolean existsNotSentMailingsWhichComponentDependsOnTargetGroup(final int companyID, final int targetGroupID){
		final String query =
				"SELECT 1 " +
				"FROM mailing_tbl m INNER JOIN component_tbl c " +
				"    ON c.mailing_id = m.mailing_id " +
				"WHERE m.company_id = ? AND m.deleted = 0 AND c.target_id = ? AND m.work_status != 'mailing.status.sent'";
		return existsAtLeastOneRow(logger, query, companyID, targetGroupID);
	}

	@Override
	public boolean existMailingsWhichContentDependsOnTargetGroup(final int companyID, final int targetGroupID){
		final String query =
				"SELECT 1 " +
				"FROM mailing_tbl m INNER JOIN dyn_content_tbl c " +
				"    ON c.mailing_id = m.mailing_id " +
				"WHERE m.company_id = ? AND m.deleted = 0 AND c.target_id = ?";
		return existsAtLeastOneRow(logger, query, companyID, targetGroupID);
	}

	@Override
	public boolean existsNotSentMailingsWhichContentDependsOnTargetGroup(final int companyID, final int targetGroupID){
		final String query =
				"SELECT 1 " +
				"FROM mailing_tbl m INNER JOIN dyn_content_tbl c " +
				"    ON c.mailing_id = m.mailing_id " +
				"WHERE m.company_id = ? AND m.deleted = 0 AND c.target_id = ? AND m.work_status != 'mailing.status.sent'";
		return existsAtLeastOneRow(logger, query, companyID, targetGroupID);
	}

	private String createTargetExpressionRestriction(){
		return createTargetExpressionRestriction(null);
	}

	private String createTargetExpressionRestriction(final String mailingTableName){
		final StringBuilder targetExpressionRestriction = new StringBuilder();

		final String appendMailingTableName = mailingTableName == null ? "" : mailingTableName + ".";

		if (isOracleDB()) {
			// Works three times faster than an MySQL-incompatible REGEXP_INSTR() version
			targetExpressionRestriction.append("INSTR(':' || ");

			// Replace all non-numeric characters with a colon character
			targetExpressionRestriction.append("TRANSLATE(")
					.append(appendMailingTableName)
					.append("target_expression, '")
					.append(TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS)
					.append("', '")
					.append(StringUtils.repeat(":", TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.length()))
					.append("')");

			targetExpressionRestriction.append(" || ':', ':' || ? || ':') <> 0");
		} else {
			// Works three times faster than an MySQL-incompatible REGEXP_INSTR() version
			// MySQL: "||" is NOT concatenation, but an boolean expression. Concatenation is done by "concat()"
			targetExpressionRestriction.append("INSTR(CONCAT(':', ");

			// Replace all non-numeric characters with a colon character
			targetExpressionRestriction.append(StringUtils.repeat("REPLACE(", TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.length()))
					.append(appendMailingTableName).append("target_expression");
			for (int i = 0; i < TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.length(); i++) {
				targetExpressionRestriction.append(", '")
						.append(TARGET_EXPRESSION_NON_NUMERIC_CHARACTERS.charAt(i))
						.append("', ':')");
			}

			targetExpressionRestriction.append(", ':'), CONCAT(':', ?, ':')) <> 0");
		}
		return targetExpressionRestriction.toString();
	}

	@Override
	public List<LightweightMailing> getActionDbMailingNames(@VelocityCheck final int companyID, final String types, final String sort, final String direction) {
        final String mailingTypes = " AND mailing_type IN (" + types + ") ";
        final String sortClause = " ORDER BY " + sort + " " + direction;
		final String sql = "SELECT company_id, mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0 " + mailingTypes + sortClause;
		return select(logger, sql, new LightweightMailingRowMapper(), companyID);
	}

	/**
	 * This method returns a list with Lightweight Mailings. Only CompanyID,
	 * MailingID, Description and Shortname are filled into the object.
     *
	 * ATTENTION! NOT ALL FIELDS WILL BE FILLED!!!! JUST THE FIELDS NEEDED FOR
	 * SOME SPECIAL CASES ARE USED. IF YOU NEED MORE YOU HAVE TO ADD THE NEEDED
	 * FIELDS. TO DO THIS FIND THE MAPPING AND ADD YOUR STATEMENT.
	 *
	 * @param companyID
	 * @return List<LightweightMailing>
	 */
	@Override
	public List<LightweightMailing> getTemplateOverview(@VelocityCheck final int companyID) {
		final String sql = "SELECT company_id, mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND is_template = 1 AND deleted = 0";
		return select(logger, sql, new LightweightMailingRowMapper(), companyID);
	}

	/**
	 * This method returns all Mailings of the given CompanyID in a LinkedList
	 */
	@Override
	public List<ComMailing> getAllMailings(@VelocityCheck final int companyID) {
		return getMailings(companyID, -1, null, false);
	}

	@Override
	public Mailing getMailing(final int mailingID, @VelocityCheck final int companyID) {
		return getMailing(mailingID, companyID, false);
	}

	@Override
	public Mailing getMailingWithDeletedDynTags(final int mailingID, @VelocityCheck final int companyID) {
		return getMailing(mailingID, companyID, true);
	}

	private Mailing getMailing(final int mailingID, @VelocityCheck final int companyID, final boolean includeDeletedDynTags) {
		if (mailingID <= 0) {
			return new ComMailingImpl();
		} else {
			final String mailingSelect = "SELECT company_id, mailing_id, shortname, content_type, deleted, description, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, is_template, needs_target, test_lock, statmail_recp, creation_date, dynamic_template, openaction_id, clickaction_id, plan_date, priority, is_prioritization_allowed FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted <= 0";

			try {
				final List<ComMailing> mailingList = select(logger, mailingSelect, new ComMailingRowMapper(), companyID, mailingID);

				if (mailingList.size() <= 0) {
					final Mailing returnMailing = new ComMailingImpl();
					returnMailing.setCompanyID(companyID);
					return returnMailing;
				} else {
					final ComMailing mailing = mailingList.get(0);
					mailing.setMediatypes(mediatypesDao.loadMediatypes(mailingID, companyID));

					final Map<String, DynamicTag> dynamicTagMap = new LinkedHashMap<>();
					for (final DynamicTag tag : getDynamicTags(mailingID, companyID, includeDeletedDynTags)) {
						dynamicTagMap.put(tag.getDynName(), tag);
					}
					mailing.setDynTags(dynamicTagMap);

					final Map<String, MailingComponent> mailingComponentMap = new TreeMap<>();
					List<MailingComponent> mailingComponents = mailingComponentDao.getMailingComponents(mailingID, companyID);
					List<Integer> linkIds = mailingComponents.stream().map(MailingComponent::getUrlID).filter(linkID -> linkID != 0).collect(Collectors.toList());
					Map<Integer, String> trackableLinkUrls = comTrackableLinkDao.getTrackableLinkUrl(companyID, mailingID, linkIds);
					for (final MailingComponent mailingComponent : mailingComponents) {
						String url = trackableLinkUrls.get(mailingComponent.getUrlID());
						if (url != null) {
							mailingComponent.setLink(url);
						}
						mailingComponentMap.put(mailingComponent.getComponentName(), mailingComponent);
					}
					mailing.setComponents(mailingComponentMap);

					final List<ComTrackableLink> trackableLinks = comTrackableLinkDao.getTrackableLinks(companyID, mailingID);
					final Map<String, TrackableLink> trackableLinksMap = new HashMap<>();
					for (final ComTrackableLink trackableLink : trackableLinks) {
						trackableLinksMap.put(trackableLink.getFullUrl(), trackableLink);
					}
					mailing.setTrackableLinks(trackableLinksMap);

					final List<MaildropEntry> maildropEntryList = maildropStatusDao.getMaildropStatusEntriesForMailing(companyID, mailingID);
					mailing.setMaildropStatus(new HashSet<>(maildropEntryList));

					return mailing;
				}
			} catch (final Exception e) {
				logger.error( "Error loading mailing ID " + mailingID, e);
				return new ComMailingImpl();
			}
		}
	}

	private void saveComponents(final int companyID, final int mailingID, final Map<String, MailingComponent> components, final boolean errorTolerant) throws Exception {
		if (MapUtils.isEmpty(components)) {
			return;
		}

		for (final MailingComponent mailingComponent : components.values()) {
			try {
				mailingComponent.setCompanyID(companyID);
				mailingComponent.setMailingID(mailingID);

				final byte[] componentBinaryBlock = mailingComponent.getBinaryBlock();

				/*
				 *  At first step, save mailing component to database in any case. This will garantuee, that we won't loose data, if CDN is
				 *  write-protected of access to CDN fails due to other issues.
				 */

				if (mailingComponent.getType() != 0) {
					if (StringUtils.isNotEmpty(mailingComponent.getLink())) {
						final Mailing mail = getMailing(mailingID, mailingComponent.getCompanyID());
						final Map<String, TrackableLink> links = mail.getTrackableLinks();

						if (links.get(mailingComponent.getLink()) == null) {
							final Map<String, TrackableLink> trackableLinks = new HashMap<>();
							final TrackableLink trkLink = (TrackableLink) applicationContext.getBean("TrackableLink");
							trkLink.setCompanyID(mailingComponent.getCompanyID());
							trkLink.setFullUrl(mailingComponent.getLink());
							trkLink.setMailingID(mailingID);
							trkLink.setUsage(TrackableLink.TRACKABLE_TEXT_HTML);
							trkLink.setActionID(0);
							trackableLinks.put(mailingComponent.getLink(), trkLink);
							comTrackableLinkDao.batchSaveTrackableLinks(companyID, mailingID, trackableLinks, false);
						}
						final String sql = "SELECT distinct url_id FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ? AND full_url = ? AND deleted = 0";
						final int id = selectInt(logger, sql, mailingID, mailingComponent.getCompanyID(), mailingComponent.getLink());
						mailingComponent.setUrlID(id);
					}
				}

				mailingComponent.setMailingID(mailingID);
				mailingComponentDao.saveMailingComponent(mailingComponent);
			} catch (final Exception e) {
				logger.error(String.format("Error saving mailing component %d ('%s') or mailing %d", mailingComponent.getId(), mailingComponent.getComponentName(), mailingComponent.getMailingID()), e);

				if (!errorTolerant) {
					throw new Exception(String.format("Error saving mailing component %d ('%s') or mailing %d", mailingComponent.getId(), mailingComponent.getComponentName(), mailingComponent.getMailingID()), e);
				}
			}
		}
	}

	/*  // TODO Disbaled by EMM-6062
	@DaoUpdateReturnValueCheck
	private void removeDynTagContentsExceptIds(int mailingId, int dynNameId, Collection<Integer> ids) {
		String sqlDeleteExceptIds = "DELETE FROM dyn_content_tbl WHERE mailing_id = ? AND dyn_name_id = ?";
		if (ids.size() > 0) {
			sqlDeleteExceptIds += " AND dyn_content_id NOT IN (" + StringUtils.join(ids, ',') + ")";
		}
		update(logger, sqlDeleteExceptIds, mailingId, dynNameId);
	}
	*/
	
	@Override
	@DaoUpdateReturnValueCheck
    public boolean cleanupContentForDynName(int mailingId, int companyId, String dynName) {
        return cleanupContentForDynNames(mailingId, companyId, Collections.singletonList(dynName));
    }
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean cleanupContentForDynNames(int mailingId, int companyId, List<String> dynNames) {
	    if (dynNames.isEmpty()) {
	        return true;
        }
	    
		final String deleteContentSQL = "DELETE from dyn_content_tbl" +
				" WHERE mailing_id = ? AND company_id = ? AND dyn_name_id IN " +
				" (SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ? AND company_id = ? " +
				" AND " + makeBulkInClauseForString("dyn_name", dynNames) + ")";
	   
		return update(logger, deleteContentSQL, mailingId, companyId, mailingId, companyId) > 0;
	}
	
	@DaoUpdateReturnValueCheck
	private void saveDynamicTags(final Mailing mailing, final Map<String, DynamicTag> dynTags) throws Exception {
        int companyId = mailing.getCompanyID();
        int mailingId = mailing.getId();
        
        List<String> dynNames = dynTags.values().stream()
				.map(DynamicTag::getDynName)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.toList());
        
        Map<String, Integer> dynNamesToIdsMap = dynamicTagDao.getDynTagIdsByName(companyId, mailingId, dynNames);
        
        List<DynamicTag> dynTagsForUpdate = new ArrayList<>();
        List<DynamicTag> dynTagsForCreation = new ArrayList<>();
        
        for (final DynamicTag tag : dynTags.values()) {
			if (StringUtils.isBlank(tag.getDynName())) {
				if (tag.getId() > 0) {
					logger.warn("Could not update dynName ID " + tag.getId());
					dynNamesToIdsMap.entrySet().stream().filter(entry -> entry.getValue() == tag.getId())
							.map(Entry::getKey).findAny()
							.ifPresent(tag::setDynName);
				} else {
					logger.warn("Could not create a dynamic tag without an assigned name for mailingID: " + mailing.getId());
				}
			}
			
			if (StringUtils.isNotBlank(tag.getDynName())) {
				int tagId = tag.getId();
				if (tagId <= 0) {
					tagId = dynNamesToIdsMap.getOrDefault(tag.getDynName(), 0);
					tag.setId(tagId);
				}
				
				tag.setCompanyID(companyId);
				tag.setMailingID(mailingId);
				if (tagId > 0) {
					dynTagsForUpdate.add(tag);
				} else {
					dynTagsForCreation.add(tag);
				}
			}
		}
		
		createDynamicTags(mailing, dynTagsForCreation);
		updateDynamicTags(mailing, dynTagsForUpdate);
  
	}
	
	private void updateDynamicTags(Mailing mailing, List<DynamicTag> dynamicTags) throws Exception {
		if (CollectionUtils.isEmpty(dynamicTags)) {
			return;
		}
		
		List<Object[]> parameterList = dynamicTags.stream().map(tag -> new Object[]{
				tag.getDynName(),
				tag.getGroup(),
				tag.getDynInterestGroup(),
				tag.isDisableLinkExtension() ? 1 : 0,
				mailing.getId(),
				mailing.getCompanyID(),
				tag.getId()
			}).collect(Collectors.toList());
   
		final String updateSql = "UPDATE dyn_name_tbl SET change_date = current_timestamp, dyn_name = ?, dyn_group = ?, interest_group = ?, deleted = 0, no_link_extension = ? WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ?";
		batchupdate(logger, updateSql, parameterList);
		
		saveDynamicTagContent(mailing, dynamicTags);
	}
	
	private void createDynamicTags(Mailing mailing, List<DynamicTag> dynamicTags) throws Exception {
		if (CollectionUtils.isEmpty(dynamicTags)) {
			return;
		}
		
		int companyId = mailing.getCompanyID();
		int mailingId = mailing.getId();
		
		if (isOracleDB()) {
			dynamicTags.forEach(tag -> tag.setId(selectInt(logger, "SELECT dyn_name_tbl_seq.NEXTVAL FROM DUAL")));
			
			List<Object[]> parameterList = dynamicTags.stream().map(tag -> new Object[]{
					mailingId,
					companyId,
					tag.getId(),
					tag.getGroup(),
					tag.getDynName(),
					tag.getDynInterestGroup(),
					tag.isDisableLinkExtension() ? 1 : 0
			}).collect(Collectors.toList());
			batchupdate(logger,
					"INSERT INTO dyn_name_tbl (mailing_id, company_id, dyn_name_id, dyn_group, dyn_name, interest_group, no_link_extension) VALUES (?, ?, ?, ?, ?, ?, ?)",
					parameterList);
		} else {
			List<Object[]> parameterList = dynamicTags.stream().map(tag -> new Object[]{
					mailingId,
					companyId,
					tag.getGroup(),
					tag.getDynName(),
					tag.getDynInterestGroup(),
					tag.isDisableLinkExtension() ? 1 : 0
			}).collect(Collectors.toList());
			
			int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable(logger, "dyn_name_id",
					"INSERT INTO dyn_name_tbl (mailing_id, company_id, dyn_group, dyn_name, interest_group, no_link_extension, creation_date) VALUES (?, ?, ?, ?, ?, ?, current_timestamp)",
					parameterList);
			
			for (int i = 0; i < generatedKeys.length && i < dynamicTags.size(); i++) {
				dynamicTags.get(i).setId(generatedKeys[i]);
			}
		}
		
		saveDynamicTagContent(mailing, dynamicTags);
	}
	
	@DaoUpdateReturnValueCheck
	private void saveDynamicTagContent(Mailing mailing, List<DynamicTag> dynamicTags) throws Exception {
		int companyId = mailing.getCompanyID();
		int mailingId = mailing.getId();
		String encodingCharset = getCharset(mailing);
		
		Map<Integer, List<Integer>> existingDynContentForDynName =
				dynamicTagContentDao.getExistingDynContentForDynName(companyId, mailingId, dynamicTags.stream().map(DynamicTag::getId).collect(Collectors.toList()));
		
		for (DynamicTag dynamicTag : dynamicTags) {
			String dynamicName = dynamicTag.getDynName();
			if (AgnUtils.DEFAULT_MAILING_TEXT_DYNNAME.equals(dynamicName)) {
				dynamicTag.getDynContent().values().forEach(content -> {
					//update dyn content according to mailing charset
					content.setDynContent(SpecialCharactersWorker.processString(content.getDynContent(), encodingCharset));
				});
			}
			
			Map<Integer, DynamicTagContent> contentEntries = dynamicTag.getDynContent();
			
			if (CollectionUtils.isNotEmpty(contentEntries.values())) {
				List<Integer> existingIds = contentEntries.values().stream()
						.map(DynamicTagContent::getId)
						.filter(id -> id > 0)
						.collect(Collectors.toList());
				
				deleteDynContentEntriesExceptIds(mailingId, companyId, dynamicTag.getId(), existingIds);
			} else {
				deleteDynContentEntries(mailingId, companyId, dynamicTag.getId());
			}
			
			saveDynContent(dynamicTag, dynamicTag.getDynContent(), existingDynContentForDynName.getOrDefault(dynamicTag.getId(), Collections.emptyList()));
		}
	}
	
	private void deleteDynContentEntries(int mailingId, int companyId, int dynNameId) {
		deleteDynContentEntriesExceptIds(mailingId, companyId, dynNameId, Collections.emptyList());
	}

	private void deleteDynContentEntriesExceptIds(int mailingId, int companyId, int dynNameId, Collection<Integer> exceptIds) {
		String sqlDeleteEntries = "DELETE FROM dyn_content_tbl WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ?";

		if (CollectionUtils.isNotEmpty(exceptIds)) {
			sqlDeleteEntries += " AND dyn_content_id NOT IN (" + StringUtils.join(exceptIds, ',') + ")";
		}

		update(logger, sqlDeleteEntries, mailingId, companyId, dynNameId);
	}

	private String getCharset(Mailing mailing) {
		MediatypeEmail param = mailing.getEmailParam();

		if (param == null) {
			return "UTF-8";
		} else {
			return param.getCharset();
		}
	}

	private void saveDynContent(DynamicTag dynTag, Map<Integer, DynamicTagContent> contents, final List<Integer> existingContentIds) throws Exception {
		List<DynamicTagContent> tagContentForUpdate = new ArrayList<>();
		List<DynamicTagContent> tagContentForCreation = new ArrayList<>();
		
		contents.values().forEach(entry -> {
			entry.setCompanyID(dynTag.getCompanyID());
			entry.setMailingID(dynTag.getMailingID());
			entry.setDynNameID(dynTag.getId());
			if (existingContentIds.contains(entry.getId())) {
				tagContentForUpdate.add(entry);
			} else {
				tagContentForCreation.add(entry);
			}
		});
		
		batchUpdateDynContent(tagContentForUpdate);
		batchInsertDynContent(tagContentForCreation);
		
		// removeDynTagContentsExceptIds(mailing.getId(), dynNameId, ids);  // TODO Disbaled by EMM-6062
	}
	
	private void batchInsertDynContent(List<DynamicTagContent> dynamicTagContents) throws Exception {
		if (isOracleDB()) {
			dynamicTagContents.forEach(entry -> {
				entry.setId(selectInt(logger, "SELECT dyn_content_tbl_seq.NEXTVAL FROM DUAL"));
			});
			
			List<Object[]> paramList = dynamicTagContents.stream().map(entry -> new Object[]{
					entry.getMailingID(),
					entry.getCompanyID(),
					entry.getDynNameID(),
					entry.getId(),
					entry.getDynContent(),
					entry.getDynOrder(),
					entry.getTargetID()
			}).collect(Collectors.toList());
			
			
			final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
			batchupdate(logger, insertSql, paramList);
		} else {
			List<Object[]> paramList = dynamicTagContents.stream().map(entry -> new Object[]{
					entry.getMailingID(),
					entry.getCompanyID(),
					entry.getDynNameID(),
					entry.getDynContent(),
					entry.getDynOrder(),
					entry.getTargetID()
			}).collect(Collectors.toList());
			
			
			final String insertSql = "INSERT INTO dyn_content_tbl (mailing_id, company_id, dyn_name_id, dyn_content, dyn_order, target_id) VALUES (?, ?, ?, ?, ?, ?)";
			int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable(logger, "dyn_content_id", insertSql, paramList);
			
			for (int i = 0; i < generatedKeys.length && i < dynamicTagContents.size(); i++) {
				dynamicTagContents.get(i).setId(generatedKeys[i]);
			}
		}
	}
	
	private void batchUpdateDynContent(List<DynamicTagContent> dynamicTagContents) {
		List<Object[]> paramList = dynamicTagContents.stream().map(entry -> new Object[]{
				entry.getDynContent(),
				entry.getDynOrder(),
				entry.getTargetID(),
				entry.getMailingID(),
				entry.getCompanyID(),
				entry.getDynNameID(),
				entry.getId()
		}).collect(Collectors.toList());
		
		
		final String updateSql = "UPDATE dyn_content_tbl SET dyn_content = ?, dyn_order = ?, target_id = ? WHERE mailing_id = ? AND company_id = ? AND dyn_name_id = ? AND dyn_content_id = ?";
		batchupdate(logger, updateSql, paramList);
	}

	/*	// TODO Disabled by EMM-6089
	@DaoUpdateReturnValueCheck
	private void removeDynTagsExceptIds(int mailingId, Collection<Integer> ids) {
		String sqlDeleteNames = "DELETE FROM dyn_name_tbl WHERE mailing_id = ?";
		String sqlDeleteContent = "DELETE FROM dyn_content_tbl WHERE mailing_id = ?";
		String exceptIds = "";

		if (ids.size() > 0) {
			exceptIds = " AND dyn_name_id NOT IN (" + StringUtils.join(ids, ',') + ")";
		}

		update(logger, sqlDeleteContent + exceptIds, mailingId);
		update(logger, sqlDeleteNames + exceptIds, mailingId);
	}
	*/

    @Override
    public void deleteUndoDataOverLimit(final int mailingId) {
        final int undoId = undoMailingDao.getUndoIdOverLimit(mailingId, configService.getIntegerValue(ConfigValue.MailingUndoLimit));
        if (undoId != 0) {
            undoMailingComponentDao.deleteUndoDataOverLimit(mailingId, undoId);
            undoDynContentDao.deleteUndoDataOverLimit(mailingId, undoId);
            undoMailingDao.deleteUndoDataOverLimit(mailingId, undoId);
        }
    }

    @Override
    public int saveUndoMailing(final int mailingId, final int adminId) {
        int undoId = 0;
        if (mailingId > 0 && adminId > 0) {
            if (isOracleDB()) {
                undoId = selectInt(logger, "SELECT undo_id_seq.nextval FROM DUAL");
            } else {
                try {
                    undoId = new MySQLMaxValueIncrementer(getDataSource(), "undo_id_seq", "value").nextIntValue();
                } catch (final NullPointerException e) {
                    undoId = 1;
                }
            }
            final Date undoCreationDate = select(logger, "SELECT CURRENT_TIMESTAMP FROM DUAL", Date.class);
            undoMailingDao.saveUndoData(mailingId, undoId, undoCreationDate, adminId);
        }
        return undoId;
    }

	@Override
	public int saveMailing(final Mailing mailing, final boolean preserveTrackableLinks) {
		try {
			// Returns mailingId (existing or brand new one).
			return saveMailingWithoutTransaction(mailing, preserveTrackableLinks, true);
		} catch (final Exception e) {
			// Do nothing (formerly the transaction was rolled back)
			return 0;
		}
	}

	@Override
	public int saveMailing(final Mailing mailing, final boolean preserveTrackableLinks, final boolean errorTolerant) throws Exception {
    	return saveMailingWithoutTransaction(mailing, preserveTrackableLinks, errorTolerant);
	}

	@DaoUpdateReturnValueCheck
	private int saveMailingWithoutTransaction(final Mailing mailing, final boolean preserveTrackableLinks, final boolean errorTolerant) throws Exception {
		int result = 0;
		String sql;
		Object[] param;

		if (!validateTargetExpression(mailing.getTargetExpression())) {
			throw new TooManyTargetGroupsInMailingException(mailing.getId());
		}

		if (StringUtils.isBlank(mailing.getShortname()) || mailing.getMailinglistID() <= 0) {
			final MailingDataException mde = new MailingDataException("Invalid empty shortname or missing mailinglist");
			logger.error("Invalid empty shortname or missing mailinglist", mde);
			// Send an email to developers, because this is a critical problem (BAUR-545)
			javaMailService.sendExceptionMail(mde.getMessage(), mde);
			throw mde;
		}

		int companyId = mailing.getCompanyID();
		try {

			// At some point mailings are generated with companyID = 0. That is
			// a bad idea.
			// Here we will throw an exception if someone tries to do that.
			if (companyId == 0) {
				final String exceptionText = "ComMailingDaoImpl is trying to create a mailing with companyID = 0." + "This is a bad idea.";
				throw new Exception(exceptionText);
			}

			final String autoUrl = selectObjectDefaultNull(logger, "SELECT auto_url FROM mailinglist_tbl WHERE deleted = 0 AND mailinglist_id = ?", new StringRowMapper(), mailing.getMailinglistID());

			// !=0 means we have already a mailing id. We got that by a sequence
			// from the DB
			if (mailing.getId() != 0) {
				sql = "UPDATE mailing_tbl SET campaign_id = ?, shortname = ?, content_type = ?, description = ?, mailing_type = ?, is_template = ?, needs_target = ?, mailtemplate_id = ?, mailinglist_id = ?, deleted = ?, archived = ?, test_lock = ?, target_expression = ?, split_id = ?, dynamic_template = ?, change_date = "
						+ "CURRENT_TIMESTAMP, openaction_id = ?, clickaction_id = ?, statmail_recp = ?, plan_date = ?, auto_url = ? WHERE mailing_id = ? AND company_id = ?";
				param = new Object[] {
						mailing.getCampaignID(),
						mailing.getShortname(),
						((ComMailing) mailing).getMailingContentType() == null ? null : ((ComMailing) mailing).getMailingContentType().name(),
						mailing.getDescription(),
						mailing.getMailingType(),
						mailing.isIsTemplate() ? 1 : 0,
						mailing.getNeedsTarget() ? 1 : 0,
						mailing.getMailTemplateID(),
						mailing.getMailinglistID(),
						mailing.getDeleted(),
						mailing.getArchived(),
						mailing.getLocked(),
						mailing.getTargetExpression(),
						((ComMailing) mailing).getSplitID(),
						mailing.getUseDynamicTemplate() ? 1 : 0,
						mailing.getOpenActionID(),
						mailing.getClickActionID(),
						((ComMailing) mailing).getStatusmailRecipients(),
						((ComMailing) mailing).getPlanDate(),
						autoUrl,
                        mailing.getId(),
                        companyId
				};
				update(logger, sql, param);
			} else {
				// ==0 means, we have a new mailing, the sequence returns us a
				// mailing-id from the db.
				if (isOracleDB()) {
					mailing.setId(selectInt(logger, "SELECT mailing_tbl_seq.NEXTVAL FROM DUAL"));

					sql = "INSERT INTO mailing_tbl (mailing_id, company_id, campaign_id, shortname, content_type, description, mailing_type, is_template, needs_target, mailtemplate_id, mailinglist_id, deleted, archived, test_lock, target_expression, split_id, work_status, dynamic_template, openaction_id, clickaction_id, creation_date, statmail_recp, plan_date, auto_url)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'mailing.status.new', ?, ?, ?, ?, ?, ?, ?)";
					param = new Object[] {
							mailing.getId(),
							companyId,
							mailing.getCampaignID(),
							mailing.getShortname(),
							((ComMailing) mailing).getMailingContentType() == null ? null : ((ComMailing) mailing).getMailingContentType().name(),
							mailing.getDescription(),
							mailing.getMailingType(),
							mailing.isIsTemplate() ? 1 : 0,
							mailing.getNeedsTarget() ? 1 : 0,
							mailing.getMailTemplateID(),
							mailing.getMailinglistID(),
							mailing.getDeleted(),
							mailing.getArchived(),
							mailing.getLocked(),
							mailing.getTargetExpression(),
							((ComMailing) mailing).getSplitID(),
							mailing.getUseDynamicTemplate() ? 1 : 0,
							mailing.getOpenActionID(),
							mailing.getClickActionID(),
							new Date(),
							((ComMailing) mailing).getStatusmailRecipients(),
	                        ((ComMailing) mailing).getPlanDate(),
	                        autoUrl
					};
					update(logger, sql, param);
				} else {
					final String insertSql = "INSERT INTO mailing_tbl (company_id, campaign_id, shortname, content_type, description, mailing_type, is_template, needs_target, mailtemplate_id, mailinglist_id, deleted, archived, test_lock, target_expression, split_id, work_status, dynamic_template, openaction_id, clickaction_id, creation_date, statmail_recp, plan_date, auto_url)"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'mailing.status.new', ?, ?, ?, ?, ?, ?, ?)";
					final int newID = insertIntoAutoincrementMysqlTable(logger, "mailing_id", insertSql,
						companyId,
						mailing.getCampaignID(),
						mailing.getShortname(),
						((ComMailing) mailing).getMailingContentType() == null ? null : ((ComMailing) mailing).getMailingContentType().name(),
						mailing.getDescription(),
						mailing.getMailingType(),
						mailing.isIsTemplate() ? 1 : 0,
						mailing.getNeedsTarget() ? 1 : 0,
						mailing.getMailTemplateID(),
						mailing.getMailinglistID(),
						mailing.getDeleted(),
						mailing.getArchived(),
						mailing.getLocked(),
						mailing.getTargetExpression(),
						((ComMailing) mailing).getSplitID(),
						mailing.getUseDynamicTemplate() ? 1 : 0,
						mailing.getOpenActionID(),
						mailing.getClickActionID(),
						new Date(),
						((ComMailing) mailing).getStatusmailRecipients(),
                        ((ComMailing) mailing).getPlanDate(),
                        autoUrl);

					mailing.setId(newID);
				}
			}

			maildropStatusDao.saveMaildropEntries(companyId, mailing.getId(), mailing.getMaildropStatus());
			comTrackableLinkDao.batchSaveTrackableLinks(companyId, mailing.getId(), mailing.getTrackableLinks(), !preserveTrackableLinks);
			saveComponents(companyId, mailing.getId(), mailing.getComponents(), errorTolerant);
			mediatypesDao.saveMediatypes(mailing.getId(), mailing.getMediatypes());
			saveDynamicTags(mailing, mailing.getDynTags());

			/*
			 * if( mailing.isIsTemplate()){ updateMailingsWithDynamicTemplate(
			 * (ComMailing)mailing); }
			 */

		} catch (final Exception e) {
			logger.error("Error saving mailing " + mailing.getId(), e);

			throw e;
		}
		result = mailing.getId();
		return result;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailing(final int mailingID, @VelocityCheck final int companyID) {
		final int touchedLines = update(logger, "UPDATE mailing_tbl SET deleted = 1, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ? AND company_id = ? AND (deleted IS NULL OR deleted != 1)", mailingID, companyID);

		if (touchedLines > 0) {
			undoDynContentDao.deleteUndoDataForMailing(mailingID);
			undoMailingComponentDao.deleteUndoDataForMailing(mailingID);
			undoMailingDao.deleteUndoDataForMailing(mailingID);
   
			if (gridTemplateDao != null) {
				gridTemplateDao.deleteByMailingID(companyID, mailingID);
			}

            if (predeliveryDao != null) {
	            // Remove inbox tests
	            predeliveryDao.deletePredeliveryTest(mailingID, companyID);
            }

			return true;
		} else {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailingsByCompanyIDReally(@VelocityCheck final int companyID) {
		final List<ComMailing> mailings = getAllMailings(companyID);

		final int touchedLines = update(logger, "DELETE FROM mailing_tbl where company_id = ?", companyID);
		if (touchedLines < 0) {
			final Iterator<ComMailing> it = mailings.iterator();
			LightweightMailing snowflakeMailing = null;
			while (it.hasNext()) {
				snowflakeMailing = new LightweightMailingImpl(it.next());

				undoDynContentDao.deleteUndoDataForMailing(snowflakeMailing.getMailingID());
				undoMailingComponentDao.deleteUndoDataForMailing(snowflakeMailing.getMailingID());
				undoMailingDao.deleteUndoDataForMailing(snowflakeMailing.getMailingID());

				deleteAllDynTags(snowflakeMailing.getMailingID());
				deleteRdirUrls(companyID);
				mailingComponentDao.deleteMailingComponentsByCompanyID(companyID);

				return true;
			}
		} else {
			if (getAllMailings(companyID).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteContentFromMailing(final Mailing mailing, final int contentID) {
		final int affectedRows = update(logger, "DELETE FROM dyn_content_tbl WHERE dyn_content_id = ? AND mailing_id = ? AND company_id = ?", contentID, mailing.getId(), mailing.getCompanyID());
		return affectedRows > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteContentFromMailing(@VelocityCheck final int companyId, final int mailingId, final int contentId) {
		final int affectedRows = update(logger, "DELETE FROM dyn_content_tbl " +
				"WHERE dyn_content_id = ? AND mailing_id = ? AND company_id = ?", contentId, mailingId, companyId);
		return affectedRows > 0;
	}

	@DaoUpdateReturnValueCheck
	private boolean deleteRdirUrls(final int companyID) {
		final String deleteSQL = "DELETE from rdir_url_tbl WHERE company_id = ?";
		final int affectedRows = update(logger, deleteSQL, companyID);
		return affectedRows > 0;
	}

	@Override
	public List<Map<String, String>> loadAction(final int mailingID, @VelocityCheck final int companyID) {
        final List<Map<String, String>> actions = new LinkedList<>();
		final String stmt = "SELECT url.shortname AS url_shortname, alt_text, full_url, act.shortname AS act_shortname, url.url_id, url.action_id FROM rdir_url_tbl url INNER JOIN rdir_action_tbl act ON url.action_id = act.action_id AND url.company_id = act.company_id WHERE url.mailing_id = ? AND url.company_id = ? AND url.deleted = 0";
		try {
			final List<Map<String, Object>> list = select(logger, stmt, mailingID, companyID);
			for (final Map<String, Object> row : list) {
				String name = "";
				if (row.get("url_shortname") != null) {
					name = (String) row.get("url_shortname");
				} else if (row.get("alt_text") != null) {
					name = (String) row.get("alt_text");
				} else {
					name = (String) row.get("full_url");
				}

                final Map<String, String> itemAction = new HashMap<>();
                itemAction.put("action_name", (String) row.get("act_shortname"));
                itemAction.put("url", name);
                itemAction.put("url_id", row.get("url_id").toString());
                itemAction.put("action_id", row.get("action_id").toString());

                actions.add(itemAction);
			}
		} catch (final Exception e) {
			logger.error( "Error loading actions for mailing " + mailingID, e);
			javaMailService.sendExceptionMail("SQL: " + stmt + ", company: " + companyID, e);
		}

		return actions;
	}

	/**
	 * Finds the last newsletter that would have been sent to the given
	 * customer.
	 *
	 * @param customerID
	 *            Id of the recipient for the newsletter.
	 * @param companyID
	 *            the company to look in.
	 * @return The mailingID of the last newsletter that would have been sent to
	 *         this recipient.
	 */
	public int findLastNewsletter(final int customerID, @VelocityCheck final int companyID) {
		// TODO: This two statements can be joined in one for better performance
		// which also makes the two "for" loops obsolet
		String sql = "SELECT m.mailing_id, m.mailinglist_id, m.target_expression, a.timestamp FROM mailing_tbl m LEFT JOIN mailing_account_tbl a ON a.mailing_id = m.mailing_id"
				+ " WHERE m.company_id = ? AND m.deleted <= 0 AND m.is_template = 0 AND a.status_field = ? ORDER BY a.timestamp DESC, m.mailing_id DESC";
		final String customerMailinglistSql = "SELECT mailinglist_id FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ? AND user_status = ? ORDER BY timestamp desc";
		try {
			final List<Map<String, Object>> resultList = select(logger, sql, companyID, MaildropStatus.WORLD.getCodeString());
			final List<Map<String, Object>> customerMailinglist = select(logger, customerMailinglistSql, customerID, UserStatus.Active.getStatusCode());

			for (final Map<String, Object> listRow : resultList) {
				final int mailing_id = ((Number) listRow.get("mailing_id")).intValue();
				final int mailinglist_id = ((Number) listRow.get("mailinglist_id")).intValue();

				// test if the customer is in the mailinglist
				boolean found = false;
				for (final Map<String, Object> customerMapListRow : customerMailinglist) {
					final int customerMailinglistID = ((Number) customerMapListRow.get("mailinglist_id")).intValue();
					if (customerMailinglistID == mailinglist_id) {
						found = true;
						break;
					}
				}

				if (found) {
					final String targetExpression = (String) listRow.get("target_expression");
					if (StringUtils.isBlank(targetExpression)) {
						return mailing_id;
					}
					sql = "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust WHERE " + getSQLExpression(targetExpression) + " AND customer_id = ?";
					logger.error("SQL: " + sql);
					if (selectInt(logger, sql, customerID) > 0) {
						return mailing_id;
					}
				}
			}
			return 0;
		} catch (final Exception e) {
			logger.error( "Error finding last newsletter (companyID " + companyID + ", customerID " + customerID + ")", e);
			return 0;
		}
	}

	@Override
	public void updateStatus(final int mailingID, final String status) {
		//status scheduled could only be overwritten by sending or canceled
		//status active could only be overwritten by disable
		if (isStatus(mailingID, "scheduled", "active")) {
			if (status.equalsIgnoreCase("sending") || status.equalsIgnoreCase("canceled") || status.equalsIgnoreCase("disable")) {
				updateStatusInDB(mailingID, status);
				return;
			}
			return;
		}

		//status sending could only be overwritten by "sent" ("norecipients" -> "sent" without recipients)
		if (isStatus(mailingID, "sending")) {
			if (status.equalsIgnoreCase("sent") || status.equalsIgnoreCase("norecipients")) {
				updateStatusInDB(mailingID, status);
				return;
			}
			return;
		}
		//never overwrite status sent
		if (isStatus(mailingID, "sent", "norecipients")) {
			return;
		}

		//never reset a mailing to status new
		if (status.equalsIgnoreCase("new")) {
			return;
		}
		//else update
		updateStatusInDB(mailingID, status);
	}

	@Override
	public String getWorkStatus(@VelocityCheck final int companyID, final int mailingID) {
		return select(logger, "SELECT work_status FROM mailing_tbl WHERE company_id = ? and mailing_id = ?", String.class, companyID, mailingID);
	}

	@DaoUpdateReturnValueCheck
	protected void updateStatusInDB (final int mailingID, final String status) {
		final String sql = "UPDATE mailing_tbl SET work_status = ? WHERE mailing_id = ?";
		try {
			update(logger, sql, "mailing.status." + status, mailingID);
		} catch (final Exception e) {
			logger.error("Error updating work status for mailing " + mailingID, e);
		}
	}

	protected boolean isStatus(final int mailingID, final String... statuses) {
		if (statuses == null || statuses.length == 0){
			return false;
		}
		if (statuses.length == 1) {
			try {
				return selectInt(logger, "SELECT COUNT(mailing_id) FROM mailing_tbl WHERE mailing_id = ? AND work_status = ?", mailingID, "mailing.status." + statuses[0]) > 0;
			} catch (final Exception e) {
				logger.error("Error checking work status for mailing " + mailingID, e);
				return false;
			}
		} else {
			try {
				return selectInt(logger, "SELECT COUNT(mailing_id) FROM mailing_tbl WHERE mailing_id = ? AND work_status IN "
						+ DbUtilities.joinForIN(statuses, status -> "mailing.status." + status), mailingID) > 0;
			} catch (final Exception e) {
				logger.error("Error checking work status for mailing " + mailingID, e);
				return false;
			}
		}
	}

	@Override
	public boolean hasEmail(final int mailingID) {
		try {
			final String sql = "SELECT COUNT(mailing_id) FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = ? AND status = ?";
			return selectInt(logger, sql, mailingID, MediaTypes.EMAIL.getMediaCode(), Mediatype.STATUS_ACTIVE) > 0;
		} catch (final Exception e) {
			logger.error("Error checking email for mailing " + mailingID, e);
			return false;
		}
	}

	@Override
	public Map<Integer, Integer> getSendStats(final ComMailing mailing, @VelocityCheck final int companyId) throws Exception {
		final Map<Integer, Integer> map = new HashMap<>();
		int numText = 0;
		int numHtml = 0;
		int numOffline = 0;

		final MediatypeEmail mediatype = mailing.getEmailParam();

		if (StringUtils.isBlank(mediatype.getFollowupFor())) {
			final StringBuilder sqlSelection = new StringBuilder(" ");

			final Collection<Integer> targetGroupIds = mailing.getTargetGroups();

			if (CollectionUtils.isNotEmpty(targetGroupIds)) {
				final String operator = (mailing.getTargetMode() == Mailing.TARGET_MODE_OR) ? "OR " : "AND ";
				boolean isFirst = true;
				int numTargets = 0;

				for (final int targetId : targetGroupIds) {
					final String sql = targetDao.getTargetSQL(targetId, companyId);
					if (sql != null) {
						if (isFirst) {
							isFirst = false;
						} else {
							sqlSelection.append(operator);
						}
						sqlSelection.append("(").append(sql).append(") ");
						numTargets++;
					}
				}

				if (numTargets > 1) {
					sqlSelection.insert(0, " AND (");
				} else {
					sqlSelection.insert(0, " AND ");
				}

				if (!isFirst && numTargets > 1) {
					sqlSelection.append(") ");
				}
			}

			if (mailing.getSplitID() > 0) {
				String sql = targetDao.getTargetSQL(mailing.getSplitID(), companyId);

				// Could be system target group (doesn't belong to any company).
				if (sql == null) {
					sql = targetDao.getTargetSQL(mailing.getSplitID(), 0);
				}

				if (sql != null) {
					sqlSelection.append(" AND (").append(sql).append(") ");
				}
			}

			final String sqlStatement = "SELECT count(*) as count, bind.mediatype, cust.mailtype FROM customer_" + companyId + "_tbl cust, " + "customer_" + companyId
					+ "_binding_tbl bind WHERE bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id" + sqlSelection.toString()
					+ " AND bind.user_status = ? GROUP BY bind.mediatype, cust.mailtype";

			if (logger.isInfoEnabled()) {
				logger.info("sql: " + sqlStatement);
			}

			final boolean useUnsharpRecipientQuery = configService.useUnsharpRecipientQuery(companyId);

			try (Connection connection = getDataSource().getConnection()) {
				try {
					// TODO: IGNORE_BOUNCELOAD_COMPANY_ID is a bad hack for CONRAD-371!!!
					if (useUnsharpRecipientQuery) {
						try (Statement stmt = connection.createStatement()) {
							stmt.execute("ALTER SESSION SET OPTIMIZER_MODE=RULE");
						}
					}

					final SingleConnectionDataSource scds = new SingleConnectionDataSource(connection, true);
					final JdbcTemplate template = new JdbcTemplate(scds);

					final List<Map<String, Object>> result = template.queryForList(sqlStatement, mailing.getMailinglistID(), UserStatus.Active.getStatusCode());
					for (final Map<String, Object> resultRow : result) {
						if (((Number) resultRow.get("mediatype")).intValue() == 0) {
							final MailType mailType = MailType.getFromInt(((Number) resultRow.get("mailtype")).intValue());
							switch (mailType) {
								case TEXT:
									numText += ((Number) resultRow.get("count")).intValue();
									break;

								case HTML:
									if (mailing.getEmailParam().getMailFormat() == 0) {
										// nur Text-Mailing
										numText += ((Number) resultRow.get("count")).intValue();
									} else {
										numHtml += ((Number) resultRow.get("count")).intValue();
									}
									break;

								case HTML_OFFLINE:
									if (mailing.getEmailParam().getMailFormat() == 0) {
										// nur Text-Mailing
										numText += ((Number) resultRow.get("count")).intValue();
									}
									if (mailing.getEmailParam().getMailFormat() == 1) {
										// nur Text/Online-HTML-Mailing
										numHtml += ((Number) resultRow.get("count")).intValue();
									}
									if (mailing.getEmailParam().getMailFormat() == 2) {
										// alle Formate
										numOffline += ((Number) resultRow.get("count")).intValue();
									}
									break;
								default:
									throw new Exception("Invalid MailType");
							}
						} else {
							if (map.get(((Number) resultRow.get("mediatype")).intValue()) != null) {
								// add (do not overwrite) new value for same mediatype
								int number = map.get(((Number) resultRow.get("mediatype")).intValue());
								number += ((Number) resultRow.get("count")).intValue();
								map.put(((Number) resultRow.get("mediatype")).intValue(), number);
							} else {
								map.put(((Number) resultRow.get("mediatype")).intValue(), ((Number) resultRow.get("count")).intValue());
							}
						}
					}
				} finally {
					// TODO: IGNORE_BOUNCELOAD_COMPANY_ID is a bad hack for CONRAD-371!!!
					if (useUnsharpRecipientQuery) {
						try (Statement stmt = connection.createStatement()) {
							stmt.execute("ALTER SESSION SET OPTIMIZER_MODE=ALL_ROWS");
						}
					}

				}
			} catch (final Exception e) {
				logger.error("Error reading send status of mailing " + mailing.getId(), e);

				if( logger.isInfoEnabled()) {
					logger.error("SQL statement is: " + sqlStatement);
				}

				return null;
			}
		} else {
			numText = getFollowUpStat(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), companyId, true);
		}

		map.put(SEND_STATS_TEXT, numText);
		map.put(SEND_STATS_HTML, numHtml);
		map.put(SEND_STATS_OFFLINE, numOffline);

		return map;
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> getUnsentMailings(@VelocityCheck final int companyID, final int rownums) {
        return getMailingForCalendar(companyID, rownums, "is null");
	}

	@Override
    public PaginatedListImpl<Map<String, Object>> getPlannedMailings(@VelocityCheck final int companyID, final int rownums) {
        return getMailingForCalendar(companyID, rownums, "is not null");
	}

	public PaginatedListImpl<Map<String, Object>> getMailingForCalendar(@VelocityCheck final int companyID, final int rownums, final String plannedCondition) {
        String sql = "SELECT * FROM (SELECT m.mailing_id AS mailing_id, m.shortname AS shortname, m.description AS description, "
                + "m.work_status AS work_status, ml.shortname AS mailinglist, m.plan_date "
                + "FROM mailing_tbl m LEFT JOIN mailinglist_tbl ml ON (ml.mailinglist_id = m.mailinglist_id AND ml.company_id = m.company_id) "
                + "WHERE m.is_template = 0 AND m.mailing_type = ? AND m.company_id = ? AND ml.deleted = 0 AND m.deleted = 0 "
                + "AND NOT EXISTS (SELECT 1 FROM maildrop_status_tbl md WHERE md.mailing_id = m.mailing_id AND md.status_field = ? AND md.company_id = ?) "
                + "AND m.plan_date " + plannedCondition + " ";
        if (isOracleDB()) {
            sql += " ORDER BY m.mailing_id DESC) WHERE rownum <= ?";
        } else {
        	sql += ") res ORDER BY mailing_id DESC LIMIT 0, ?";
        }
        List<Map<String, Object>> tmpList = null;
        tmpList = select(logger, sql, Mailing.TYPE_NORMAL, companyID, MaildropStatus.WORLD.getCodeString(), companyID, rownums);
        final List<Map<String, Object>> result = new ArrayList<>();
        for (final Map<String, Object> row : tmpList) {
            final Map<String, Object> newBean = new HashMap<>();
            newBean.put("workstatus", row.get("WORK_STATUS"));
            final Number mailingID = (Number) row.get("MAILING_ID");
            newBean.put("mailingid", mailingID);
            newBean.put("shortname", row.get("SHORTNAME"));
            newBean.put("description", row.get("DESCRIPTION"));
            newBean.put("mailinglist", row.get("MAILINGLIST"));
            if ("is not null".equalsIgnoreCase(plannedCondition)) {
                newBean.put("plandate", row.get("PLAN_DATE"));
            }
            if (hasActions(mailingID.intValue(), companyID)) {
                newBean.put("hasActions", Boolean.TRUE);
            }
            newBean.put("usedInCM", usedInCampaignManager(mailingID.intValue()));
            result.add(newBean);
        }
        return new PaginatedListImpl<>(result, rownums, rownums, 1, "mailingid", "desc");
    }

	@Override
	public PaginatedListImpl<Map<String, Object>> getMailingList(@VelocityCheck final int companyID, final MailingsListProperties props) {
		// Check supported search modes by available db indices
		String searchText = props.getSearchQuery();
		if (StringUtils.isNotEmpty(searchText)) {
			searchText = DbUtilities.normalizeSqlLikeSearchPlaceholders(searchText);

			if (!DbUtilities.containsSqlLikeSearchPlaceholders(searchText)) {
				searchText = "%" + searchText + "%";
			}

			if ((props.isSearchName() || props.isSearchDescription()) && !isBasicFullTextSearchSupported()) {
				props.setSearchName(false);
				props.setSearchDescription(false);
			}

			// Full text index search only works for patterns of size 3+ characters
			if (props.isSearchContent() && !isContentFullTextSearchSupported() && searchText.length() >= 3) {
				props.setSearchContent(false);
			}
		} else {
			props.setSearchName(false);
			props.setSearchDescription(false);
			props.setSearchContent(false);
		}

		final List<Object> selectParameter = new ArrayList<>();
		final StringBuilder selectSql = new StringBuilder("SELECT")
			.append(" a.work_status AS work_status,")
			.append(" a.company_id AS company_id,")
			.append(" a.mailing_id AS mailing_id,")
			.append(" a.shortname AS shortname,")
			.append(" a.description AS description,")
			.append(" COALESCE(c.mintime, mds.senddate) AS senddate,")
			.append(" m.shortname AS mailinglist,")
			.append(" a.creation_date AS creationdate,")
			.append(" a.change_date AS changedate,")
			.append(" a.target_expression AS target_expression,");

		if (isGridTemplateSupported()) {
			selectSql.append(" COALESCE(g.grid_mailing, 0) AS isgrid,");
		} else {
			selectSql.append(" 0 AS isgrid,");
		}

		selectSql.append(" COALESCE(f.workflow_mailing, 0) AS usedincm,")
			.append(" COALESCE(cmp.component_id, 0) AS preview_component")
			.append(getAdditionalColumnsName(companyID, "a", props.getAdditionalColumns()))
			.append(" FROM mailing_tbl a")
			.append(joinAdditionalColumns(props.getAdditionalColumns()))
			.append(" LEFT JOIN mailing_account_sum_tbl c ON a.mailing_id = c.mailing_id AND c.status_field = '").append(MaildropStatus.WORLD.getCode()).append("'")
			.append(" LEFT JOIN mailinglist_tbl m ON a.mailinglist_id = m.mailinglist_id AND a.company_id = m.company_id AND m.deleted <> 1");

		if (isGridTemplateSupported()) {
			selectSql.append(" LEFT JOIN (SELECT DISTINCT mailing_id, 1 AS grid_mailing FROM grid_template_tbl) g ON a.mailing_id = g.mailing_id");
		}

		selectSql.append(" LEFT JOIN (SELECT DISTINCT entity_id, 1 AS workflow_mailing FROM workflow_dependency_tbl WHERE company_id = ? AND type = ").append(WorkflowDependencyType.MAILING_DELIVERY.getId()).append(") f ON a.mailing_id = f.entity_id");
		selectParameter.add(companyID);

		selectSql.append(" LEFT JOIN (SELECT MAX(component_id) AS component_id, mailing_id FROM component_tbl WHERE company_id = ? AND binblock IS NOT NULL AND comptype = ").append(MailingComponent.TYPE_THUMBNAIL_IMAGE).append(" GROUP BY mailing_id) cmp ON a.mailing_id = cmp.mailing_id");
		selectParameter.add(companyID);

		selectSql.append(" LEFT JOIN maildrop_status_tbl mds ON a.mailing_id = mds.mailing_id AND mds.status_field = '").append(MaildropStatus.WORLD.getCode()).append("'");

		if (props.isSearchContent()) {
			String contentSearchQuery = props.getSearchQuery();
			try {
				contentSearchQuery = fulltextSearchQueryGenerator.generateSpecificQuery(props.getSearchQuery());
			} catch (final FulltextSearchQueryException e) {
				logger.error("Cannot transform full text search query: " + props.getSearchQuery());
			}

			// Subquery to search within static text and html blocks
			selectSql.append(" LEFT JOIN (SELECT mailing_id, MAX(")
					.append(isOracleDB() ? "CONTAINS(emmblock, ?)" : "MATCH(emmblock) AGAINST(? IN BOOLEAN MODE)")
					.append(") AS relevance");
			selectParameter.add(contentSearchQuery);

			selectSql.append(" FROM component_tbl")
					.append(" WHERE company_id = ? AND mtype IN ('text/plain', 'text/html')");
			selectParameter.add(companyID);

			selectSql.append(" GROUP BY mailing_id")
				.append(") comp ON a.mailing_id = comp.mailing_id");

			// Subquery to search within dynamic (included into static with agn tags) text and html blocks
			selectSql.append(" LEFT JOIN (SELECT cont.mailing_id, MAX(")
					.append(isOracleDB() ? "CONTAINS(cont.dyn_content, ?)" : "MATCH(cont.dyn_content) AGAINST(? IN BOOLEAN MODE)")
					.append(") AS relevance");
			selectParameter.add(contentSearchQuery);

			selectSql.append(" FROM dyn_name_tbl nm JOIN dyn_content_tbl cont ON nm.dyn_name_id = cont.dyn_name_id")
					.append(" WHERE cont.company_id = ?");
			selectParameter.add(companyID);

			selectSql.append(" GROUP BY cont.mailing_id")
				.append(") cont ON a.mailing_id = cont.mailing_id");
		}

		selectSql.append(" WHERE a.company_id = ?");
		selectParameter.add(companyID);

		selectSql.append(" AND a.is_template = ?");
		selectParameter.add(BooleanUtils.toInteger(props.isTemplate()));

		selectSql.append(" AND a.deleted = 0");

		final List<Integer> targetGroupIds = props.getTargetGroups();
		if (CollectionUtils.isNotEmpty(targetGroupIds)) {
			final StringBuilder targetExpressionRestriction = new StringBuilder();
			for (final Integer targetGroupId : targetGroupIds) {
				if (Objects.nonNull(targetGroupId)) {
					if (targetExpressionRestriction.length() > 0) {
						targetExpressionRestriction.append(" OR ");
					}
					targetExpressionRestriction.append(createTargetExpressionRestriction("a"));
				}
			}
			if (targetExpressionRestriction.length() > 0) {
				selectSql.append(" AND (").append(targetExpressionRestriction).append(")");
			}
		}

		if (!props.isTemplate()) {
			selectSql.append(" AND a.mailing_type IN (").append(props.getTypes()).append(")");
		}

		if (CollectionUtils.isNotEmpty(props.getStatuses())) {
			selectSql.append(" AND a.work_status IN ('mailing.status.").append(StringUtils.join(props.getStatuses(), "', 'mailing.status.")).append("')");
		}

		if (CollectionUtils.isNotEmpty(props.getMailingLists())) {
			selectSql.append(" AND a.mailinglist_id IN (").append(StringUtils.join(props.getMailingLists(), ", ")).append(")");
		}

		if (props.getSendDateBegin() != null || props.getSendDateEnd() != null) {
			selectSql.append(" AND ").append(DbUtilities.getDateConstraint("c.mintime", props.getSendDateBegin(), props.getSendDateEnd(), isOracleDB()));
		}

		if (props.getCreationDateBegin() != null || props.getCreationDateEnd() != null) {
			selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.creation_date", props.getCreationDateBegin(), props.getCreationDateEnd(), isOracleDB()));
		}

		if (CollectionUtils.isNotEmpty(props.getBadge())) {
			// this is used to filter for the badge signs "EMC-Mailing" and/or "UsedInCampaignManager"
			final boolean grid = props.getBadge().contains("isgrid");
			final boolean cm = props.getBadge().contains("isCampaignManager");
			final String isGridIdentifier = "COALESCE(g.grid_mailing, 0)";
			final String usedInCMIdentifier = "COALESCE(f.workflow_mailing, 0)";
			if (grid && cm) {
				selectSql.append(String.format(" AND (%s = 1 OR %s = 1)", isGridIdentifier, usedInCMIdentifier));
			} else {
				if (grid) {
					selectSql.append(String.format(" AND %s = 1", isGridIdentifier));
				}
				if (cm) {
					selectSql.append(String.format(" AND %s = 1", usedInCMIdentifier));
				}
			}
		}

		final List<String> searchClauses = new ArrayList<>();
		if (props.isSearchName()) {
			String searchNameClause;
			if (isOracleDB()) {
				searchNameClause = "LOWER(a.shortname) LIKE ? ESCAPE '\\'";
			} else {
				searchNameClause = "LOWER(a.shortname) LIKE ?";
			}
			searchClauses.add(searchNameClause);
			selectParameter.add(StringUtils.lowerCase(searchText));
		}

		if (props.isSearchDescription()) {
			String searchDescriptionClause;
			if (isOracleDB()) {
				searchDescriptionClause = "LOWER(a.description) LIKE ? ESCAPE '\\'";
			} else {
				searchDescriptionClause = "LOWER(a.description) LIKE ?";
			}
			searchClauses.add(searchDescriptionClause);
			selectParameter.add(StringUtils.lowerCase(searchText));
		}

		if (props.isSearchContent()) {
			searchClauses.add("comp.relevance > 0 OR cont.relevance > 0 ");
		}

		final String searchSqlPart = StringUtils.join(searchClauses, " OR ");

		String sortColumn;
		String direction;
		String sortClause;
		if (StringUtils.isNotBlank(props.getSort())) {
			String sort = props.getSort();
			sortColumn = StringUtils.lowerCase(sort);
			direction = StringUtils.defaultIfEmpty(props.getDirection(), "ASC").toUpperCase();


			if (sort.startsWith("a.")) {
				props.setSort(sort.substring(2));
			}

			if ("creationdate".equals(sortColumn)) {
				sortColumn = "creation_date";
			}

			if (props.getAdditionalColumns().contains(sortColumn)) {
				final MailingAdditionalColumn column = MailingAdditionalColumn.getColumn(sortColumn);
				if (MailingAdditionalColumn.TEMPLATE == column) {
					sort = "templatename";
				}

				if (MailingAdditionalColumn.RECIPIENTS_COUNT == column) {
					sort = "recipientscount";
				}
			} else if (StringUtils.isBlank(sort)) {
				sort = "mailing_id";
				sortColumn = "mailing_id";
			}

			if ("mailing_id".equalsIgnoreCase(sort)) {
				sortClause = "ORDER BY " + sort + " " + props.getDirection();
			} else {
				try {
					DbColumnType dbColumnType = DbUtilities.getColumnDataType(getDataSource(), "mailing_tbl", sort);
					if (dbColumnType != null) {
						sort = "a." + sort;
					} else {
						dbColumnType = DbUtilities.getColumnDataType(getDataSource(), "mailing_account_sum_tbl", sort);
						if (dbColumnType != null) {
							sort = "c." + sort;
						} else {
							dbColumnType = DbUtilities.getColumnDataType(getDataSource(), "mailinglist_tbl", sort);
							if (dbColumnType != null) {
								sort = "m." + sort;
							}
						}
					}


					if ("mailinglist".equalsIgnoreCase(sort)
							|| "work_status".equalsIgnoreCase(sort)
							|| (dbColumnType != null
								&& dbColumnType.getSimpleDataType() == SimpleDataType.Characters)) {
						if (isOracleDB()) {
							sortClause = "ORDER BY LOWER(TRIM(" + sort + ")) " + direction + ", mailing_id " + direction;
						} else {
							// MySQL DESC sorts null-values to the end by default, oracle DESC sorts null-values to the top
							// MySQL ASC sorts null-values to the top by default, oracle ASC sorts null-values to the end
							if (StringUtils.isBlank(direction) || "ASC".equalsIgnoreCase(direction.trim())) {
								sortClause = "ORDER BY IF(" + sort + " = '' OR " + sort + " IS NULL, 1, 0), LOWER(TRIM(" + sort + ")) ASC, mailing_id ASC";
							} else {
								sortClause = "ORDER BY IF(" + sort + " = '' OR " + sort + " IS NULL, 0, 1), LOWER(TRIM(" + sort + ")) DESC, mailing_id DESC";
							}
						}
					} else if ("senddate".equalsIgnoreCase(sort) || (dbColumnType != null && dbColumnType.getSimpleDataType() == SimpleDataType.Date)) {
						sortClause = generateSendDateSortClause(direction);
					} else {
						sortClause = "ORDER BY " + sort + " " + direction + ", mailing_id " + direction;
					}
				} catch (final Exception e) {
					logger.error("Invalid sort field: " + props.getSort(), e);
					sortClause = "ORDER BY mailing_id DESC";
					sortColumn = "mailing_id";
					direction = "DESC";
				}
			}
		} else {
			sortColumn = "senddate";
			direction = StringUtils.isBlank(props.getDirection()) ? "DESC" : StringUtils.upperCase(props.getDirection());
			sortClause = generateSendDateSortClause(direction);
		}

		if(StringUtils.isNotEmpty(searchSqlPart)) {
			selectSql.append(" AND (").append(searchSqlPart).append(")");
		}

		return selectPaginatedListWithSortClause(logger,
				selectSql.toString(),
				sortClause, sortColumn, AgnUtils.sortingDirectionToBoolean(direction),
				props.getPage(), props.getRownums(),
				new MailingMapRowMapper(), selectParameter.toArray());
	}

	private String generateSendDateSortClause(String direction) {
		final boolean isAscending = AgnUtils.sortingDirectionToBoolean(direction, true);
		direction = isAscending ? "ASC" : "DESC";
		if (isOracleDB()) {
			return String.format("ORDER BY senddate %1$s NULLS %2$s, mailing_id %1$s", direction, isAscending ? "LAST" : "FIRST");
		} else {
			// MySQL DESC sorts null-values to the end by default, oracle DESC sorts null-values to the top
			// MySQL ASC sorts null-values to the top by default, oracle ASC sorts null-values to the end
			return String.format("ORDER BY %1$s, senddate %2$s, mailing_id %2$s",
					isAscending ? "IF(senddate IS NULL, 1, 0)" : "IF(senddate IS NULL, 0, 1)", direction);
		}
	}

	private String joinAdditionalColumns(final Set<String> additionalColumns) {
		final StringBuilder queryPart = new StringBuilder();
		for (final String columnKey : additionalColumns) {
			final MailingAdditionalColumn column = MailingAdditionalColumn.getColumn(columnKey);

			if (column == MailingAdditionalColumn.TEMPLATE) {
				queryPart.append(" LEFT JOIN mailing_tbl t ON a.mailtemplate_id = t.mailing_id");
				if (isGridTemplateSupported()) {
					queryPart.append(" LEFT JOIN (SELECT g.template_id, g.mailing_id, pt.name FROM grid_template_tbl g LEFT JOIN grid_template_tbl pt ON g.parent_template_id = pt.template_id) gt ON gt.mailing_id = a.mailing_id");
				}
			} else if (column == MailingAdditionalColumn.SUBJECT) {
				queryPart.append(" LEFT JOIN mailing_mt_tbl mt ON a.mailing_id = mt.mailing_id AND mt.mediatype = 0 AND mt.status = ").append(Mediatype.STATUS_ACTIVE);
			}
		}
		return queryPart.toString();
	}

	private String getRecipientsCountSubStatement(final int companyId, final String mailingTableAlias) {
		final String bindingTableName = getBindingTableName(companyId);
		return "SELECT COUNT (DISTINCT customer_id) " +
				String.format(" FROM %s ", bindingTableName) +
				String.format(" WHERE %s.mailinglist_id = %s.mailinglist_id", mailingTableAlias, bindingTableName);
	}

	private String getAdditionalColumnsName(final int companyId, final String mailingTableAlias, final Set<String> additionalColumns) {
		final StringBuilder queryPart = new StringBuilder();
		for (final String columnKey : additionalColumns) {
			final MailingAdditionalColumn column = MailingAdditionalColumn.getColumn(columnKey);

			if (column == MailingAdditionalColumn.TEMPLATE) {
				if (isGridTemplateSupported()) {
					queryPart.append(", COALESCE(gt.name, t.shortname) AS templatename");
				} else {
					queryPart.append(", t.shortname AS templatename");
				}
			} else if (column == MailingAdditionalColumn.SUBJECT) {
				if (isOracleDB()) {
					queryPart.append(", SUBSTR(param, INSTR(param, 'subject=\"') + 9, INSTR(param, '\", charset') - INSTR(param, 'subject=\"') - 9) AS subject");
				} else {
					queryPart.append(", SUBSTRING_INDEX(SUBSTRING_INDEX(mt.param, 'subject=\"', -1), '\", charset', 1) AS subject");
				}
			} else if (column == MailingAdditionalColumn.RECIPIENTS_COUNT) {
				queryPart.append(String.format(", (%s) AS recipientscount ", getRecipientsCountSubStatement(companyId, mailingTableAlias)));
			}
		}
		return queryPart.toString();
	}

    @Override
	public PaginatedListImpl<Map<String, Object>> getEmptyMailingList() {
        final List<Map<String, Object>> results = new ArrayList<>();
        return new PaginatedListImpl<>(results, 0, 1, 1, "senddate", "DESC");
    }

    @Override
	public boolean usedInCampaignManager(final int mailingId) {
		final String sqlGetCount = "SELECT COUNT(*) FROM workflow_dependency_tbl " +
			"WHERE type = ? AND entity_id = ?";

		return selectInt(logger, sqlGetCount, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId) > 0;
	}

    @Override
    public boolean usedInRunningWorkflow(final int mailingId, @VelocityCheck final int companyId) {
		final String sqlGetCount = "SELECT COUNT(*) FROM workflow_tbl w " +
			"JOIN workflow_dependency_tbl dep ON dep.company_id = w.company_id AND dep.workflow_id = w.workflow_id " +
			"WHERE w.company_id = ? AND w.status IN (?, ?) AND dep.type = ? AND dep.entity_id = ?";

        final Object[] sqlParameters = new Object[] {
        	companyId,
			WorkflowStatus.STATUS_ACTIVE.getId(),
			WorkflowStatus.STATUS_TESTING.getId(),
			WorkflowDependencyType.MAILING_DELIVERY.getId(),
			mailingId
		};

        return selectInt(logger, sqlGetCount, sqlParameters) > 0;
    }

    @Override
    public int getWorkflowId(final int mailingId) {
		final String sqlGetMaxId = "SELECT MAX(workflow_id) FROM workflow_dependency_tbl " +
			"WHERE type = ? AND entity_id = ?";

		return selectInt(logger, sqlGetMaxId, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId);
    }

	@Override
	public int getWorkflowId(final int mailingId, @VelocityCheck final int companyId) {
		final String sqlGetMaxId = "SELECT MAX(workflow_id) FROM workflow_dependency_tbl " +
			"WHERE company_id = ? AND type = ? AND entity_id = ?";

		return selectInt(logger, sqlGetMaxId, companyId, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId);
	}

    @Override
	public boolean showNumStat(final int mailingID, @VelocityCheck final int companyID) {
		final String sql = "SELECT COUNT(customer_id) FROM rdirlog_" + companyID + "_val_num_tbl WHERE mailing_id = ?"
				+ " AND page_tag IN (SELECT pagetag FROM trackpoint_def_tbl WHERE company_id = ? AND pagetag NOT IN ('revenue'))";
		return selectInt(logger, sql, mailingID, companyID) > 0;
	}

	@Override
	public boolean showAlphaStat(final int mailingID, @VelocityCheck final int companyID) {
		final String sql = "SELECT COUNT(customer_id) FROM rdirlog_" + companyID + "_val_alpha_tbl WHERE mailing_id = ?";
		return selectInt(logger, sql, mailingID) > 0;
	}

    @Override
    public boolean showSimpleStat(final int mailingID, @VelocityCheck final int companyID) {
        final String sql = "SELECT COUNT(customer_id) FROM rdirlog_" + companyID + "_ext_link_tbl WHERE mailing_id = " + mailingID;
        return selectInt(logger, sql) > 0;
    }

	@Override
	public List<MailingBase> getMailingsForComparation(@VelocityCheck final int companyID) {
		final String sqlStatement = "SELECT a.mailing_id, a.shortname, a.description, MIN(c.mintime) senddate FROM "
				+ " (mailing_tbl a LEFT JOIN mailing_account_sum_tbl c ON (a.mailing_id = c.mailing_id)) "
				+ " WHERE a.company_id = ? AND a.deleted = 0 AND c.status_field = ?"
				+ " AND a.mailing_id in (SELECT mailing_id FROM maildrop_status_tbl WHERE status_field IN (?, ?, ?) AND company_id = ?) "
				+ " GROUP BY a.mailing_id, a.shortname, a.description ORDER BY mailing_id DESC";

		final Object[] sqlParameters = new Object[]{
				companyID,
				MaildropStatus.WORLD.getCodeString(),
				MaildropStatus.WORLD.getCodeString(),
				MaildropStatus.ACTION_BASED.getCodeString(),
				MaildropStatus.DATE_BASED.getCodeString(),
				companyID
		};

		final List<Map<String, Object>> tmpList = select(logger, sqlStatement, sqlParameters);

		final List<MailingBase> result = new ArrayList<>();
		for (final Map<String, Object> row : tmpList) {
			final MailingBase newBean = new MailingBaseImpl();
			newBean.setId(((Number) row.get("mailing_id")).intValue());
			newBean.setShortname((String) row.get("shortname"));
			newBean.setDescription((String) row.get("description"));
			newBean.setSenddate((Date) row.get("senddate"));
			result.add(newBean);
		}

		return result;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean saveStatusmailRecipients(final int mailingID, final String statusmailRecipients) {
		try {
			final int result = update(logger, "UPDATE mailing_tbl SET statmail_recp = ? WHERE mailing_id = ?", statusmailRecipients, mailingID);
			return result == 1;
		} catch (final Exception e) {
			logger.error("Error while saveStatusmailRecipients", e);
			return false;
		}
	}

    @Override
    public List<Integer> getFollowupMailings(final int mailingID, final int companyID, final boolean includeUnscheduled) {
        final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder
				.append("SELECT mailing.mailing_id FROM mailing_tbl mailing")
				.append(" INNER JOIN mailing_mt_tbl mt ON mailing.mailing_id = mt.mailing_id")
				.append(" WHERE mailing.deleted = 0 AND mailing.company_id = ?")
				.append(" AND mailing.mailing_type = 3");
        if (!includeUnscheduled){
            queryBuilder.append(" AND mailing.work_status = 'mailing.status.scheduled'");
        }
        queryBuilder.append(" AND mt.param LIKE '%followup_for=\"").append(mailingID).append("\"%'");
        return select(logger, queryBuilder.toString(), new IntegerRowMapper(), companyID);
    }

	/**
	 * get the minimum senddate of mailing.
	 *
	 * @param mailingID
	 *            - trivial :)
	 * @return NULL if no date is available
	 */
	public Date getStartDate(final int mailingID) {
		final String sql = "SELECT MIN(timestamp) FROM mailing_account_tbl WHERE mailing_id = ?";
		return select(logger, sql, Date.class, mailingID);
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> getMailingShortList(@VelocityCheck final int companyID, final String searchQuery, final boolean searchName, final boolean searchDescription, String sortCriteria, final boolean sortAscending, int pageNumber, int pageSize) {
		final boolean databaseIsOracle = isOracleDB();

		final String sqlFromAndWhereClauses = "FROM mailing_tbl m" +
				" JOIN mailinglist_tbl l ON m.mailinglist_id = l.mailinglist_id" +
				" WHERE m.company_id = ? AND m.deleted = 0 AND l.deleted = 0 AND m.is_template = 0" +
				" AND m.mailing_id IN (SELECT DISTINCT mailing_id FROM mailing_account_sum_tbl WHERE company_id = ?)";

		final Map<String, String> sortableColumns = new CaseInsensitiveMap<>();
		sortableColumns.put("mailing_id", "m.mailing_id");
		sortableColumns.put("shortname", "m.shortname");
		sortableColumns.put("description", "m.description");
		sortableColumns.put("mailinglist", "l.shortname");

		String sqlRelevanceSumExpression = "";
		int searchColumnsCount = 0;

		if (StringUtils.isNotBlank(searchQuery) && (searchName || searchDescription)) {
			// Check if a database supports fulltext search (if a proper indices exist)
			if (isBasicFullTextSearchSupported()) {
				// Sorting by relevance is only available when search is supported and requested by user
				sortableColumns.put("relevance", "relevance");

				sqlRelevanceSumExpression += "(";

				if (searchName) {
					sqlRelevanceSumExpression += (databaseIsOracle ? "CONTAINS(m.shortname, ?)" : "MATCH(m.shortname) AGAINST(? IN BOOLEAN MODE)");
					searchColumnsCount++;
				}

				if (searchDescription) {
					if (searchName) {
						sqlRelevanceSumExpression += " + ";
					}
					sqlRelevanceSumExpression += (databaseIsOracle ? "CONTAINS(m.description, ?)" : "MATCH(m.description) AGAINST(? IN BOOLEAN MODE)");
					searchColumnsCount++;
				}

				sqlRelevanceSumExpression += ")";
			}
		}

		if (StringUtils.isNotBlank(sortCriteria) && sortableColumns.containsKey(sortCriteria)) {
			sortCriteria = sortCriteria.toLowerCase();
		} else {
			if (searchColumnsCount > 0) {
				sortCriteria = "relevance";
			} else {
				sortCriteria = "mailing_id";
			}
		}

		String sortClause = " ORDER BY " + sortableColumns.get(sortCriteria) + (sortAscending ? " ASC" : " DESC");
		if (!StringUtils.equals("mailing_id", sortCriteria)) {
			sortClause += ", m.mailing_id DESC";
		}

		final List<Object> sqlParameters = new ArrayList<>();
		sqlParameters.add(companyID);
		sqlParameters.add(companyID);

		String sqlGetTotalCount = "SELECT COUNT(*) " + sqlFromAndWhereClauses;

		// If search is required
		if (searchColumnsCount > 0) {
			for (int i = 0; i < searchColumnsCount; i++) {
				sqlParameters.add(searchQuery);
			}
			sqlGetTotalCount += " AND " + sqlRelevanceSumExpression + " > 0";
		}

		final int totalCount = selectInt(logger, sqlGetTotalCount, sqlParameters.toArray());

		if (pageSize < 1) {
			pageSize = 10;
		}

		if (pageNumber < 1) {
			pageNumber = 1;
		} else {
			pageNumber = AgnUtils.getValidPageNumber(totalCount, pageNumber, pageSize);
		}

		if (totalCount > 0) {
			sqlParameters.clear();

			String sqlGetMailings = "SELECT m.mailing_id mailingid, m.shortname, m.description, l.shortname mailinglist";
			if (searchColumnsCount > 0) {
				for (int i = 0; i < searchColumnsCount; i++) {
					sqlParameters.add(searchQuery);
				}
				final String sortClauseRewritten = sortClause
						.replace("m.mailing_id", "mailingid")
						.replace("m.shortname", "shortname")
						.replace("m.description", "description")
						.replace("l.shortname", "mailinglist");
				sqlGetMailings = "SELECT * FROM (" + sqlGetMailings + ", " + sqlRelevanceSumExpression + " AS relevance " + sqlFromAndWhereClauses + ") relevant_results WHERE relevance > 0" + sortClauseRewritten;
			} else {
				sqlGetMailings += " " + sqlFromAndWhereClauses + sortClause;
			}

			sqlParameters.add(companyID);
			sqlParameters.add(companyID);

			int paginationParam1, paginationParam2;
			if (databaseIsOracle) {
				sqlGetMailings = "SELECT * FROM (SELECT ROWNUM AS row_index, mv.* FROM (" +
						sqlGetMailings +
						") mv) WHERE row_index BETWEEN ? AND ?";

				paginationParam1 = (pageNumber - 1) * pageSize + 1;
				paginationParam2 = paginationParam1 + pageSize - 1;
			} else {
				sqlGetMailings += " LIMIT ?, ?";

				paginationParam1 = (pageNumber - 1) * pageSize;
				paginationParam2 = pageSize;
			}

			sqlParameters.add(paginationParam1);
			sqlParameters.add(paginationParam2);

			final List<Map<String, Object>> rows = select(logger, sqlGetMailings, sqlParameters.toArray());
			return new PaginatedListImpl<>(rows, totalCount, pageSize, pageNumber, sortCriteria, sortAscending);
		} else {
			return new PaginatedListImpl<>(Collections.emptyList(), totalCount, pageSize, pageNumber, sortCriteria, sortAscending);
		}
	}

	@Override
	public int getStatusidForWorldMailing(final int mailingID, @VelocityCheck final int companyID) {
		final String query = "SELECT status_id FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field = ? AND genstatus = 3 AND senddate < CURRENT_TIMESTAMP";
		try {
			return selectIntWithDefaultValue(logger, query, 0, companyID, mailingID, MaildropStatus.WORLD.getCodeString());
		} catch (final Exception e) {
			return 0;
		}
	}

    @Override
    public boolean isMailingMarkedDeleted(final int mailingID, @VelocityCheck final int companyID) {
        try {
            final int returnValue = selectIntWithDefaultValue(logger, "SELECT deleted FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", 1, companyID, mailingID);
            return returnValue == 1;
        } catch (final Exception e) {
        	return true;
        }
    }

	@Override
	public int getFollowUpStat(final ComFollowUpStats comFollowUpStats, final boolean useTargetGroups) throws Exception {
		final String followUpType = getFollowUpType(comFollowUpStats.getFollowupID());
		final int returnValue = getFollowUpStat(comFollowUpStats.getFollowupID(), comFollowUpStats.getBasemailID(), followUpType, comFollowUpStats.getCompanyID(), useTargetGroups);
		return returnValue;
	}

	/**
	 * This method returns the Follow-up statistics for the given mailing.
	 * @throws Exception
	 */
	@Override
	public int getFollowUpStat(final int mailingID, final int baseMailing, final String followUpType, @VelocityCheck final int companyID, final boolean useTargetGroups) throws Exception {
		int resultValue = 0;

		// What kind of followup do we have, choose the appropriate sql call...
		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_CLICKER)) {

			// TODO NEW click calculation with target groups.
			// the maildrop-id must be the one of the 'base' mailing, which is
			// already sent. The given
			// mailing-id must be the one of the current (that means the
			// follow-up) mailing.
			resultValue = getClicker(companyID, baseMailing, mailingID);
		}

		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_OPENER)) {
			resultValue = getOpener(companyID, baseMailing, mailingID);
		}

		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_NON_CLICKER)) {
			resultValue = getNonClicker(companyID, baseMailing, mailingID);
		}

		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_NON_OPENER)) {
			resultValue = getNonOpener(companyID, baseMailing, mailingID);
		}

		return resultValue;
	}

	@Override
	public int getFollowUpStat(final int mailingID, final int followUpFor, final String followUpType, final int companyID, final String sqlTargetExpression) throws Exception {

		int resultValue = 0;
		// What kind of followup do we have, choose the appropriate sql call...
		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_CLICKER)) {
			resultValue = getClicker(companyID, followUpFor, sqlTargetExpression);
		}

		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_OPENER)) {
			resultValue = getOpener(companyID, followUpFor, sqlTargetExpression);
		}

		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_NON_CLICKER)) {
			resultValue = getNonClicker(companyID, followUpFor, sqlTargetExpression);
		}

		if (followUpType.equals(Mailing.TYPE_FOLLOWUP_NON_OPENER)) {
			resultValue = getNonOpener(companyID, followUpFor, sqlTargetExpression);
		}

		return resultValue;
	}

	private int getClicker(final int companyID, final int followUpFor, final String sqlTargetExpression) {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

		if(StringUtils.isNotBlank(sqlTargetExpression)){
			sql.append(" AND (" + sqlTargetExpression + ")");
		}

		return selectInt(logger,sql.toString(),followUpFor);
	}

	private int getNonClicker(@VelocityCheck final int companyID, final int followUpFor, final String sqlTargetExpression) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
		sql.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

		if(StringUtils.isNotBlank(sqlTargetExpression)){
			sql.append(" AND (" + sqlTargetExpression + ")");
		}

		return selectInt(logger, sql.toString(), followUpFor, followUpFor);
	}

	private int getOpener(@VelocityCheck final int companyID, final int followUpFor, final String sqlTargetExpression) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

		if(StringUtils.isNotBlank(sqlTargetExpression)){
			sql.append(" AND (" + sqlTargetExpression + ")");
		}

		return selectInt(logger, sql.toString(), followUpFor);
	}

	private int getNonOpener(@VelocityCheck final int companyID, final int followUpFor, final String sqlTargetExpression) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
		sql.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

		if(StringUtils.isNotBlank(sqlTargetExpression)){
			sql.append(" AND (" + sqlTargetExpression + ")");
		}

		return selectInt(logger, sql.toString(), followUpFor, followUpFor);
	}

	private int getClicker(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

		// Add targetgroup sql for followup mailing
		final String targetString = getTargetIDString(followUpMailingID);
		final int[] targetIDs = getTargetExpressionIds(targetString);
		if (targetIDs.length != 0) {
			final String targetSql = createTargetStatementWithoutAnd(targetString);
			if (StringUtils.isNotBlank(targetSql)) {
				sql.append(" AND (" + targetSql + ")");
			}
		}

		// Add listsplit sql for followup mailing
		final int listSplitId = getListSplitId(followUpMailingID);
		if (listSplitId != 0) {
			final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
			sql.append(" AND (" + listSplitSql + ")");
		}

		return selectInt(logger, sql.toString(), baseMailingID);
	}

	/**
	 * returns the amount of non-clickers. maildrop-id is the id of the BASE!
	 * Mailing, not the actual followup!
	 *
	 * @param companyID
	 * @param baseMailingID
	 * @param followUpMailingID
	 * @return
	 * @throws Exception
	 */
	private int getNonClicker(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
		sql.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)");

		// Add targetgroup sql for followup mailing
		final String targetString = getTargetIDString(followUpMailingID);
		final int[] targetIDs = getTargetExpressionIds(targetString);
		if (targetIDs.length != 0) {
			final String targetSql = createTargetStatementWithoutAnd(targetString);
			if (StringUtils.isNotBlank(targetSql)) {
				sql.append(" AND (" + targetSql + ")");
			}
		}

		// Add listsplit sql for followup mailing
		final int listSplitId = getListSplitId(followUpMailingID);
		if (listSplitId != 0) {
			final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
			sql.append(" AND (" + listSplitSql + ")");
		}

		return selectInt(logger, sql.toString(), baseMailingID, baseMailingID);
	}

	/**
	 * returns the amount of openers for the given mailingID, companyID and
	 * targetID.
	 *
	 * @return
	 * @throws Exception
	 */
	private int getOpener(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

		// Add targetgroup sql for followup mailing
		final String targetString = getTargetIDString(followUpMailingID);
		final int[] targetIDs = getTargetExpressionIds(targetString);
		if (targetIDs.length != 0) {
			final String targetSql = createTargetStatementWithoutAnd(targetString);
			if (StringUtils.isNotBlank(targetSql)) {
				sql.append(" AND (" + targetSql + ")");
			}
		}

		// Add listsplit sql for followup mailing
		final int listSplitId = getListSplitId(followUpMailingID);
		if (listSplitId != 0) {
			final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
			sql.append(" AND (" + listSplitSql + ")");
		}

		return selectInt(logger, sql.toString(), baseMailingID);
	}

	/**
	 * returns the amount of openers for the given mailingID, companyID and
	 * targetID.
	 *
	 * @return
	 * @throws Exception
	 */
	private int getNonOpener(@VelocityCheck final int companyID, final int baseMailingID, final int followUpMailingID) throws Exception {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(DISTINCT bind.customer_id) FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust");
		sql.append(" WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1");
		sql.append(" AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)");
		sql.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)");

		// Add targetgroup sql for followup mailing
		final String targetString = getTargetIDString(followUpMailingID);
		final int[] targetIDs = getTargetExpressionIds(targetString);
		if (targetIDs.length != 0) {
			final String targetSql = createTargetStatementWithoutAnd(targetString);
			if (StringUtils.isNotBlank(targetSql)) {
				sql.append(" AND (" + targetSql + ")");
			}
		}

		// Add listsplit sql for followup mailing
		final int listSplitId = getListSplitId(followUpMailingID);
		if (listSplitId != 0) {
			final String listSplitSql = createTargetStatementWithoutAnd(Integer.toString(listSplitId));
			sql.append(" AND (" + listSplitSql + ")");
		}

		return selectInt(logger, sql.toString(), baseMailingID, baseMailingID);
	}

	private String createTargetStatementWithoutAnd(final String targetString) throws Exception {
		final int[] targetIDs = getTargetExpressionIds(targetString);
		final String operator = getTargetOperator(targetString);

		final StringBuilder targetSql = new StringBuilder();
		for (final int targetID : targetIDs) {
			if (targetSql.length() > 0) {
				targetSql.append(" ").append(operator).append(" ");
			}

			targetSql.append(getTargetExpressionForId(targetID));
		}

		return targetSql.toString();
	}

	/**
	 * This method fetchs a target-sql String for the target-groups from the
	 * database and returns it as String. you must then add this statement to
	 * your normal sql-query.
	 *
	 * @param expressionID
	 * @return
	 */
	@Override
	public String getTargetExpressionForId(final int expressionID) {
		try {
			return select(logger, "SELECT target_sql FROM dyn_target_tbl WHERE target_id = ?", String.class, expressionID);
		} catch (final Exception e) {
			logger.error("Error getting target-sql from DB.", e);
			return "";
		}
	}

	/**
	 * This method returns an Array with all Target_Expression IDs from the
	 * mailing_tbl.
	 */
	private int[] getTargetExpressionIds(final String targetExpression) {
		final Set<Integer> targetIds = TargetExpressionUtils.getTargetIds(targetExpression);
		final int[] targetsArray = ArrayUtils.toPrimitive(targetIds.toArray(new Integer[0]));
		return targetsArray;
	}

	@Override
	public int getCompanyIdForMailingId(final int mailingId) {
		return selectIntWithDefaultValue(logger, "SELECT company_id FROM mailing_tbl WHERE mailing_id = ?", -1, mailingId);
	}

	@Override
	public ComRdirMailingData getRdirMailingData(final int mailingId) {
		final List<Map<String, Object>> list = select(logger, "SELECT company_id, creation_date FROM mailing_tbl WHERE mailing_id = ?", mailingId);

		if (list.size() != 1) {
			return null;
		} else {
			final Map<String, Object> map = list.get(0);
			return new ComRdirMailingDataImpl(((Number) map.get("company_id")).intValue(), (Timestamp) map.get("creation_date"));
		}
	}

	/**
	 * returns the type of a Followup Mailing as String. The String can be fount
	 * in the mailing-class (eg. Mailing.TYPE_FOLLOWUP_CLICKER) if no followup
	 * is found, null is the returnvalue!
	 *
	 * @param mailingID
	 * @return
	 */
	@Override
	public String getFollowUpType(final int mailingID) {
		String returnValue = null;
		String params = null;
		params = getEmailParameter(mailingID);
		if (params != null) {
			returnValue = getFollowUpTypeFromParameterString(params);
		}
		return returnValue;
	}

	/**
	 * This method takes the param-field from the mailing_mt_tbl as parameter
	 * and extracts the follow-up Type from it.
	 *
	 * @param params
	 * @return
	 */
	private String getFollowUpTypeFromParameterString(final String params) {
		return AgnUtils.getAttributeFromParameterString(params, "followup_method");
	}

	/**
	 * returns the base-mailing for the given followup.
	 */
	@Override
	public String getFollowUpFor(final int mailingID) {
		String params = null;
		String followUpFor = null;
		// first check, if we have a followup mailing.
		if (getMailingType(mailingID) != 3) {
			return null;
		}
		params = getEmailParameter(mailingID);

		// split the parameters
		if (params != null) {
			final String paramArray[] = params.split(",");

			// loop over every entry
			for (final String item : paramArray) {
				if (item.trim().startsWith("followup_for")) {
					followUpFor = item.trim();
				}
			}

			// now extract the parameter.
			if (followUpFor != null) {
				followUpFor = followUpFor.replace("followup_for=", "");
				followUpFor = followUpFor.replace("\"", "").trim();
			}
		}
		return followUpFor;
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> getDashboardMailingList(@VelocityCheck final int companyID, final String sort, final String direction, final int rownums) {
		String orderBy = "";

		// If this method is not called, the order of the mailings on the dashboard is determined by database
		orderBy = getDashboardMailingsOrder(sort, direction, Arrays.asList("shortname", "description", "mailinglist"));


		String sqlStatement;
		if (isOracleDB()) {
			sqlStatement = "SELECT * FROM (" +
					"SELECT m.work_status, m.mailing_id AS mid, m.shortname, m.description, acc.mintime AS senddate, ml.shortname AS mailinglist, m.change_date, cmp.component_id AS component, " +
					"CASE WHEN (SELECT COUNT(*) FROM workflow_dependency_tbl dep WHERE dep.type = ? AND dep.entity_id = m.mailing_id) > 0 THEN 1 ELSE 0 END AS user_in_cm " +
					"FROM (" +
					"  SELECT * FROM (" +
					"    SELECT company_id, mailing_id, shortname, description, work_status, change_date, mailinglist_id FROM mailing_tbl" +
					"    WHERE company_id = ? AND deleted = 0 AND is_template = 0" +
					"    ORDER BY COALESCE(change_date, TO_DATE('01.01.1900', 'dd.mm.yyyy')) DESC, mailing_id DESC" +
					"  ) mailing WHERE ROWNUM <= ?" +
					") m" +
					"  LEFT JOIN mailing_account_sum_tbl acc" +
					"    ON m.mailing_id = acc.mailing_id AND acc.status_field = ?" +
					"  LEFT JOIN mailinglist_tbl ml" +
					"    ON m.mailinglist_id = ml.mailinglist_id AND ml.company_id = m.company_id AND ml.deleted = 0" +
					"  LEFT JOIN component_tbl cmp" +
					"    ON m.mailing_id = cmp.mailing_id AND cmp.comptype = ? AND cmp.binblock IS NOT NULL" +
					") subsel " + orderBy;
		} else {
			sqlStatement = "SELECT * FROM (" +
					"SELECT m.work_status, m.mailing_id AS mid, m.shortname, m.description, acc.mintime AS senddate, ml.shortname AS mailinglist, m.change_date, cmp.component_id AS component, " +
					"CASE WHEN (SELECT COUNT(*) FROM workflow_dependency_tbl dep WHERE dep.type = ? AND dep.entity_id = m.mailing_id) > 0 THEN 1 ELSE 0 END AS user_in_cm " +
					"FROM (" +
					"  SELECT company_id, mailing_id, shortname, description, work_status, change_date, mailinglist_id FROM mailing_tbl" +
					"  WHERE company_id = ? AND deleted = 0 AND is_template = 0" +
					"  ORDER BY COALESCE(change_date, DATE_FORMAT('01.01.1900','%d.%m.%Y')) DESC, mailing_id DESC" +
					"  LIMIT ?" +
					") m" +
					"  LEFT JOIN mailing_account_sum_tbl acc" +
					"    ON m.mailing_id = acc.mailing_id AND acc.status_field = ?" +
					"  LEFT JOIN mailinglist_tbl ml" +
					"    ON m.mailinglist_id = ml.mailinglist_id AND ml.company_id = m.company_id AND ml.deleted = 0" +
					"  LEFT JOIN component_tbl cmp" +
					"    ON m.mailing_id = cmp.mailing_id AND cmp.comptype = ? AND cmp.binblock IS NOT NULL" +
					") subsel " + orderBy.replaceAll(" e\\.", " ");
		}

		final List<Map<String, Object>> rows = select(logger, sqlStatement, WorkflowDependencyType.MAILING_DELIVERY.getId(), companyID, rownums, MaildropStatus.WORLD.getCodeString(), MailingComponent.TYPE_THUMBNAIL_IMAGE);

		final List<Map<String, Object>> result = new ArrayList<>(rows.size());
		for (final Map<String, Object> row : rows) {
			final Map<String, Object> newBean = new HashMap<>();
			newBean.put("workstatus", row.get("work_status"));
			newBean.put("mailingid", row.get("mid"));
			newBean.put("shortname", row.get("shortname"));
			newBean.put("description", row.get("description"));
			newBean.put("mailinglist", row.get("mailinglist"));
			newBean.put("senddate", row.get("senddate"));
			newBean.put("usedInCM", ((Number) row.get("user_in_cm")).intValue() == 1);
			newBean.put("changedate", row.get("change_date"));
			newBean.put("component", row.get("component"));
			result.add(newBean);
		}

		return new PaginatedListImpl<>(result, result.size(), rownums, 1, sort, direction);
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> getDashboardThumbnailsMailingList(@VelocityCheck final int companyID, final String sort, final String direction, final int rownums) {
		final List<String> charColumns = Arrays.asList(new String[] { "shortname", "description", "mailinglist" });
		final String orderby = getDashboardMailingsOrder(sort, direction, charColumns);

		String sqlStatement;
		if (isOracleDB()) {
			sqlStatement = "SELECT * from ("
					+ "SELECT work_status, mailing_id mid, shortname, change_date, senddate, company, component, recipientnum from ("
					+ "SELECT a.work_status, a.mailing_id, a.shortname, a.description, a.change_date, acsum.mintime senddate, comp.shortname company, cmp.component_id component, acsum.no_of_mailings recipientnum"
					+ " FROM mailing_tbl a"
					+ " LEFT JOIN company_tbl comp ON comp.company_id = ?"
					+ " LEFT JOIN component_tbl cmp ON a.mailing_id = cmp.mailing_id AND cmp.comptype = ?"
					+ " LEFT JOIN mailing_account_sum_tbl acsum ON a.mailing_id = acsum.mailing_id AND acsum.status_field = ?"
					+ " WHERE a.company_id = ? AND a.deleted = 0 AND a.is_template = ?"
					+ " ORDER BY NVL(change_date, TO_DATE('01.01.1900', 'dd.mm.yyyy')) DESC, mailing_id DESC) where 1 = 1 AND rownum <= ?)"
					+ orderby;
		} else {
			sqlStatement = "SELECT work_status, mailing_id mid, shortname, change_date, senddate, company, component, recipientnum from ("
					+ "SELECT a.work_status, a.mailing_id, a.shortname, a.description, a.change_date, comp.shortname company, cmp.component_id component, acsum.no_of_mailings recipientnum, acsum.mintime senddate"
					+ " FROM mailing_tbl a"
					+ " LEFT JOIN company_tbl comp ON comp.company_id = ?"
					+ " LEFT JOIN component_tbl cmp ON a.mailing_id = cmp.mailing_id AND cmp.comptype = ?"
					+ " LEFT JOIN mailing_account_sum_tbl acsum ON a.mailing_id = acsum.mailing_id AND acsum.status_field = ?"
					+ " WHERE a.company_id = ? AND a.deleted = 0 AND a.is_template = ?"
					+ " ORDER BY IFNULL(a.change_date, DATE_FORMAT('01.01.1900','%d.%m.%Y')) DESC, a.mailing_id DESC LIMIT ?) subsel"
					+ orderby;
		}

		final List<Map<String, Object>> tmpList = select(logger, sqlStatement, companyID, MailingComponent.TYPE_THUMBNAIL_IMAGE, MaildropStatus.WORLD.getCodeString(), companyID, 0, rownums);
		final int totalSize = tmpList.size();

		final List<Map<String, Object>> result = new ArrayList<>();
		for (final Map<String, Object> row : tmpList) {
			Map<String, Object> newBean;
			newBean = new HashMap<>();
			newBean.put("workstatus", row.get("work_status"));
			newBean.put("recipientnum", row.get("recipientnum"));
			newBean.put("mailingid", row.get("mid"));
			newBean.put("shortname", row.get("shortname"));
			newBean.put("changedate", row.get("change_date"));
			newBean.put("company", row.get("company"));
			newBean.put("component", row.get("component"));
			result.add(newBean);
		}

		return new PaginatedListImpl<>(result, totalSize, rownums, 1, sort, direction);
	}

	private String getDashboardMailingsOrder(final String sort, final String direction, final List<String> charColumns) {
		String orderby;
		if (StringUtils.isNotBlank(sort)) {
			/*
			 * When the users want us to sort the list by senddate, then we have
			 * to handle the null values to be the lowest values otherwise we
			 * get a strange order: null values are always the greatest values.
			 */
			if (sort.trim().toLowerCase().equals("senddate")) {
				if (isOracleDB()) {
					orderby = " ORDER BY NVL(senddate, TO_DATE('01.01.1900', 'dd.mm.yyyy'))";
				} else {
					orderby = " ORDER BY IFNULL(senddate, DATE_FORMAT('01.01.1900', '%d.%m.%Y'))";
				}
			} else {
				if (charColumns.contains(sort)) {
					orderby = " ORDER BY LOWER(TRIM(" + sort + "))" ;
				} else {
					orderby = " ORDER BY " + sort;
				}
			}
			orderby += " " + direction;
		} else {
			// We need the workaround with NVL and TO_DATE here to get mailings
			// with change_date=NULL to be ordered lower than other change_dates
			if (isOracleDB()) {
				orderby = " ORDER BY NVL(change_date, TO_DATE('01.01.1900', 'dd.mm.yyyy')) DESC, mid DESC";
			} else {
				orderby = " ORDER BY IFNULL(change_date, DATE_FORMAT('01.01.1900', '%d.%m.%Y')) DESC, mid DESC";
			}
		}
		return orderby;
	}

	@Override
	public int getLastSentMailingId(@VelocityCheck final int companyID) {
		final List<Integer> mailingIds = getLastWorldSendMailingIds(companyID, 1);
		if (mailingIds.isEmpty()) {
			return 0;
		} else {
			return mailingIds.get(0);
		}
	}

	@Override
	public List<Integer> getBirtReportMailingsToSend(final int companyID, final int reportId, final Date startDate, final Date endDate, final int filter, final int filterValue) {
		String sql = "SELECT mailing_id FROM maildrop_status_tbl WHERE company_id = ? AND status_field = ?" +
                " AND mailing_id NOT IN (SELECT mailing_id FROM birtreport_sent_mailings_tbl WHERE report_id = ?)" +
                " AND senddate >= ? AND senddate <= ? ";
		
		boolean isFilterSet = filter != BirtReportSettingsUtils.FILTER_NO_FILTER_VALUE;
        if (isFilterSet) {
            final String filterField = filterFieldName(filter);

            sql += " AND mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND " + filterField + " = ?) ";
        }
        sql += " ORDER BY senddate DESC";

        if (isFilterSet) {
        	return select(logger, sql, new IntegerRowMapper(), companyID, MaildropStatus.WORLD.getCodeString(), reportId, startDate, endDate, companyID, filterValue);
        } else {
        	return select(logger, sql, new IntegerRowMapper(), companyID, MaildropStatus.WORLD.getCodeString(), reportId, startDate, endDate);
        }
	}

	private static String filterFieldName(final int filterCode) {
		switch(FilterType.getFilterTypeByKey(filterCode)) {
			case FILTER_MAILINGLIST:
				return "mailinglist_id";
	
			case FILTER_MAILING:
				return "mailing_id";
	
			case FILTER_ARCHIVE:
				return "campaign_id";
	
			default:
				return null;
		}
	}

    protected List<Integer> getLastWorldSendMailingIds(@VelocityCheck final int companyID, final int limit) {
		String sql;
		if (isOracleDB()) {
			sql = "SELECT * FROM (SELECT mailing_id FROM maildrop_status_tbl WHERE company_id = ? AND status_field IN (?, ?, ?) ORDER BY senddate DESC) WHERE rownum <= ?";
		} else {
			sql = "SELECT mailing_id FROM maildrop_status_tbl WHERE company_id = ? AND status_field IN (?, ?, ?) ORDER BY senddate DESC LIMIT ?";
		}

		final Object[] sqlParameters = new Object[]{
				companyID,
				MaildropStatus.WORLD.getCodeString(),
				MaildropStatus.ACTION_BASED.getCodeString(),
				MaildropStatus.DATE_BASED.getCodeString(),
				limit
		};

		return select(logger, sql, new IntegerRowMapper(), sqlParameters);
	}

	@Override
	public LightweightMailing getLightweightMailing(final int mailingID) {
		final String sql = "SELECT description, shortname, company_id FROM mailing_tbl WHERE mailing_id = ?";
		try {
			final List<Map<String, Object>> result = select(logger, sql, mailingID);
			if (result.size() > 0) {
				final LightweightMailing mailing = new LightweightMailingImpl();
				mailing.setMailingID(mailingID);
				mailing.setCompanyID(((Number)result.get(0).get("company_id")).intValue());
				mailing.setMailingDescription((String) result.get(0).get("description"));
				mailing.setShortname((String) result.get(0).get("shortname"));

				return mailing;
			} else {
				logger.error("Something went wrong while fetching one mailing with mailing ID: " + mailingID);
				return null;
			}
		} catch (final Exception e) {
			logger.error("Something went wrong while fetching one mailing with mailing ID: " + mailingID, e);
			return null;
		}
	}

	@Override
	public String getSendDate(final String format, @VelocityCheck final int companyId, final int mailingId) {
		String sql;
		if (isOracleDB()) {
			sql = "SELECT TO_CHAR(senddate, ?) AS senddate FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY DECODE(status_field, 'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC";
		} else {
			sql = "SELECT DATE_FORMAT(senddate, ?) AS senddate FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ?"
					+ " ORDER BY CASE status_field WHEN 'W' THEN 1 WHEN 'R' THEN 2 WHEN 'D' THEN 2 WHEN 'E' THEN 3 WHEN 'C' THEN 3 WHEN 'T' THEN 4 WHEN 'A' THEN 4 ELSE 5 END, status_id DESC";
		}

		final List<Map<String, Object>> result = select(logger, sql, format, companyId, mailingId);

		if (result.size() == 0) {
			return null;
		} else {
			return (String) result.get(0).get("senddate");
		}
	}

	@Override
	public Timestamp getLastSendDate(final int mailingID) {
		String sql = "";
		if (isOracleDB()) {
			sql = "SELECT senddate FROM (SELECT senddate, row_number()"
					+ " OVER (ORDER BY DECODE(status_field, 'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC) r FROM maildrop_status_tbl"
					+ " WHERE mailing_id = ? ) WHERE r = 1";
		} else {
			sql = "SELECT senddate FROM maildrop_status_tbl WHERE mailing_id = ?"
					+ " ORDER BY CASE status_field WHEN 'W' THEN 1 WHEN 'R' THEN 2 WHEN 'D' THEN 2 WHEN 'E' THEN 3 WHEN 'C' THEN 3 WHEN 'T' THEN 4 WHEN 'A' THEN 4 ELSE 5 END, status_id DESC LIMIT 1";
		}

		try {
			final List<Map<String,Object>> sendDateResult = select(logger, sql, mailingID);
			if (sendDateResult != null && sendDateResult.size() == 1 && sendDateResult.get(0).get("senddate") != null) {
				return (Timestamp) sendDateResult.get(0).get("senddate");
			} else {
				return null;
			}
		} catch (final IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> getLastSentWorldMailings(@VelocityCheck final int companyID, int number) {
		String sql;
		if (isOracleDB()) {
			sql = " SELECT mailing_id mailingid, shortname FROM (SELECT m.mailing_id, m.shortname, maxtime senddate"
					+ " FROM mailing_account_sum_tbl a JOIN mailing_tbl m ON a.mailing_id = m.mailing_id"
					+ "	WHERE m.company_id = ? AND a.status_field = ? AND m.deleted = 0 ORDER BY senddate DESC) WHERE rownum < ? ";
		} else {
			sql = " SELECT m.mailing_id as mailingid, m.shortname, maxtime senddate"
					+ " FROM mailing_account_sum_tbl a JOIN mailing_tbl m ON a.mailing_id = m.mailing_id"
					+ "	WHERE m.company_id = ? AND a.status_field = ? AND m.deleted = 0 ORDER BY senddate DESC LIMIT ?";
			number = number - 1;
		}

		final List<Map<String, Object>> worldmailingList = select(logger, sql, companyID, MaildropStatus.WORLD.getCodeString(), number + 1);

		// update the type of mailingid from bigdecimal to integer
		for (final Map<String, Object> itemMap : worldmailingList) {
			itemMap.put("mailingid", ((Number) itemMap.get("mailingid")).intValue());
		}

		return worldmailingList;
	}

	@Override
	public boolean deleteAccountSumEntriesByCompany(@VelocityCheck final int companyID) {
		update(logger, "DELETE FROM mailing_account_sum_tbl WHERE company_id = ?", companyID);
		return selectInt(logger, "SELECT COUNT(*) FROM mailing_account_sum_tbl WHERE company_id = ?", companyID) == 0;
	}

	/**
	 * This method returns the string with all target IDs.
	 *
	 * @param followUpMailingID
	 * @return
	 */
	private String getTargetIDString(final int followUpMailingID) {
		try {
			return select(logger, "SELECT target_expression FROM mailing_tbl WHERE mailing_id = ?", String.class, followUpMailingID);
		} catch (final Exception e) {
			logger.error("Database Error getting target-id Strings in ComMailingDaoImpl", e);
			return null;
		}
	}

	/**
	 * This method returns the Operator for the target String. If the
	 * targetString contains "|" it returns "OR" and if it contains "&" it
	 * returns "AND". WARNING! Mixing up targets will result in an exception!
	 *
	 * @param targetString
	 * @return
	 * @throws Exception
	 */
	private String getTargetOperator(final String targetString) throws Exception {
		if (targetString.contains("|") && targetString.contains("&")) {
			// Mix-Check. We don't support mixed target-group at this time (2011.02.01)
			throw new Exception("Unsupported Mixed Target Groups: " + targetString);
		} else if (targetString.contains("|")) {
			return "OR";
		} else {
			return "AND"; // this is the less dangerous version (=less recipients).
		}
	}

	@Override
	public boolean isTransmissionRunning(final int mailingID) {
		String query = "SELECT count(*) FROM maildrop_status_tbl WHERE status_field IN (?) AND genstatus IN (1, 2) AND mailing_id = ?";
		final boolean isWorldMailingSentBeingAtTheMoment = selectInt(logger, query, MaildropStatus.WORLD.getCodeString(), mailingID) > 0;
		if (isWorldMailingSentBeingAtTheMoment) {
			query = " SELECT CASE WHEN COUNT(a.maildrop_id) = COUNT(d.status_id) THEN 0 ELSE 1 END transmission_running "
					+ " FROM maildrop_status_tbl d LEFT JOIN mailing_account_tbl a ON d.status_id = a.maildrop_id WHERE d.mailing_id = ?";

			final boolean worldMailingTransmissionRunning = selectInt(logger, query, mailingID) == 1;
			return worldMailingTransmissionRunning;
		} else {
			query = "SELECT count(*) FROM maildrop_status_tbl WHERE status_field IN (?, ?) AND genstatus IN (1, 2) AND mailing_id = ?";
			final boolean testOrAdminMailingTransmissionRunning = selectInt(logger, query, MaildropStatus.ADMIN.getCodeString(), MaildropStatus.TEST.getCodeString(), mailingID) > 0;
			return testOrAdminMailingTransmissionRunning;
		}
	}

	@Override
	public int getLastSentMailing(@VelocityCheck final int companyID, final int customerID) {
		return selectIntWithDefaultValue(logger, "SELECT MAX(mailing_id) FROM success_" + companyID + "_tbl WHERE customer_id = ?"
				+ " AND timestamp = (SELECT MAX(timestamp) FROM success_" + companyID + "_tbl " + " WHERE customer_ID =  ?)", -1, customerID, customerID);
	}

	@Override
	public int getLastSentWorldMailingByCompanyAndMailinglist(@VelocityCheck final int companyID, final int mailingListID) {
		Object[] sqlParameters;

		final StringBuilder sb = new StringBuilder();

		sb.append("SELECT mailing_id FROM maildrop_status_tbl WHERE status_id IN (");
		sb.append("SELECT MAX(status_id) FROM maildrop_status_tbl WHERE company_id = ? AND status_field = ?");
		if (mailingListID == 0) {
			sqlParameters = new Object[]{companyID, MaildropStatus.WORLD.getCodeString()};
		} else {
			sqlParameters = new Object[]{companyID, MaildropStatus.WORLD.getCodeString(), mailingListID};
			sb.append("AND mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE mailinglist_id = ? AND deleted = 0)");
		}
		sb.append(")");

		try {
			return selectInt(logger, sb.toString(), sqlParameters);
		} catch (final Exception e) {
			logger.error("Error getting lastSent mailingID. CompanyID: " + companyID + " mailingListID: " + mailingListID, e);
		}

		return -1;
	}

	@Override
	public List<MailingBase> getSentWorldMailingsForReports(@VelocityCheck final int companyID, final int number) {
        return getPredefinedMailingsForReports(companyID, number, -1, 0, -1, "");
	}

	@Override
	public List<MailingBase> getPredefinedNormalMailingsForReports(@VelocityCheck final int companyId, final Date from, final Date to, final int filterType, final int filterValue, final String orderKey){
		final char statusField = MaildropStatus.WORLD.getCode();
		String orderColumn = "shortname";
		if(!orderKey.isEmpty() && orderKey.equals("date")) {
			orderColumn = "change_date";
		}

		final StringBuilder query = new StringBuilder();
		if (isOracleDB()) {
			query.append("SELECT * FROM ")
					.append("   (SELECT a.shortname, c.mailing_id, MIN(c.timestamp) AS senddate, a.change_date ")
					.append("       FROM mailing_account_tbl c ")
					.append("       LEFT JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
					.append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' AND a.mailing_type = 0 %s ")
					.append("       GROUP BY a.shortname, c.mailing_id, a.change_date ORDER BY senddate DESC) res");
		} else {
			query.append("SELECT * FROM ")
					.append("   (SELECT a.shortname, c.mailing_id, MIN(c.timestamp) AS senddate, a.change_date ")
					.append("       FROM mailing_account_tbl c ")
					.append("       LEFT JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
					.append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' AND a.mailing_type = 0 %s ")
					.append("       GROUP BY a.shortname, c.mailing_id, a.change_date ORDER BY senddate DESC) res");
		}
		final List<Object> parameters = new ArrayList<>();
		parameters.add(companyId);

		String filter = getSettingsTypeFilter(filterType, filterValue, parameters);

		if (StringUtils.isNotEmpty(filter)) {
			filter += " AND a.deleted = 0";
		}

		if(Objects.nonNull(from) && Objects.nonNull(to)){
			query.append("   WHERE res.senddate BETWEEN ? AND ? ");
			
		    parameters.add(from);
		    parameters.add(to);
        }
        
        query.append(" ORDER BY ").append(orderColumn).append(" DESC");

        final String filteredQuery = String.format(query.toString(), filter);


		return select(logger, filteredQuery, PARTIAL_MAILING_BASE_ROW_MAPPER, parameters.toArray());
	}

	public static class PartialMailingBaseRowMapper implements RowMapper<MailingBase> {
		@Override
		public MailingBase mapRow(final ResultSet resultSet, final int i) throws SQLException {
			final MailingBaseImpl mailingBase = new MailingBaseImpl();
			mailingBase.setId(resultSet.getInt("mailing_id"));
			mailingBase.setShortname(resultSet.getString("shortname"));
			mailingBase.setSenddate(resultSet.getDate("senddate"));
			return mailingBase;
		}
	}

	@Override
	public List<MailingBase> getPredefinedMailingsForReports(@VelocityCheck final int companyId, final int number, final int filterType, final int filterValue, final int mailingType, String orderKey) {
        if (DATE_ORDER_KEY.equalsIgnoreCase(orderKey)) {
			orderKey = "change_date";
        } else {
			orderKey = "shortname";
		}

        char statusField;
        switch (mailingType) {
            case Mailing.TYPE_ACTIONBASED:
                statusField = MaildropStatus.ACTION_BASED.getCode();
                break;
            case Mailing.TYPE_DATEBASED:
                statusField = MaildropStatus.DATE_BASED.getCode();
                break;
            default:
                statusField = MaildropStatus.WORLD.getCode();
        }
        final StringBuilder query = new StringBuilder();
        if (isOracleDB()) {
        	query.append("SELECT * FROM ")
		            .append("   (SELECT a.shortname, c.mailing_id, MIN(c.timestamp) AS senddate, a.change_date ")
			        .append("       FROM mailing_account_tbl c")
			        .append("       INNER JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
			        .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' %s ")
			        .append("       GROUP BY a.shortname, c.mailing_id, a.change_date ORDER BY senddate DESC) res")
			        .append("   WHERE rownum < ? ORDER BY ").append(orderKey).append(" ASC");
        } else {
        	query.append("SELECT a.shortname, c.mailing_id, MIN(c.timestamp) AS senddate, a.change_date ")
			        .append("       FROM mailing_account_tbl c")
			        .append("       INNER JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
			        .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' %s ")
			        .append("       GROUP BY a.shortname, c.mailing_id, a.change_date ORDER BY senddate DESC, ").append(orderKey).append(" ASC LIMIT ?");
        }

        final List<Object> parameters = new ArrayList<>();
        parameters.add(companyId);

        String filter = getSettingsTypeFilter(filterType, filterValue, parameters);

        if (mailingType >= 0) {
            filter += " AND a.mailing_type = ?";
            parameters.add(mailingType);
        }
        if (StringUtils.isNotEmpty(filter)) {
           filter += " AND a.deleted = 0";
        }

        final String filteredQuery = String.format(query.toString(), filter);
        parameters.add(number+1);

        return select(logger, filteredQuery, PARTIAL_MAILING_BASE_ROW_MAPPER, parameters.toArray());
    }

	private String getSettingsTypeFilter(final int filterType, final int filterValue, final List<Object> parameters) {
		String filterClause = "";
		switch (FilterType.getFilterTypeByKey(filterType)) {
			case FILTER_ARCHIVE:
				filterClause = " AND a.campaign_id = ?";
				parameters.add(filterValue);
				break;
			case FILTER_MAILINGLIST:
				filterClause = " AND a.mailinglist_id = ?";
				parameters.add(filterValue);
				break;
			default:
				//nothing do
		}

		return filterClause;
	}

	private List<Map<String, String>> getTargetGroupsForTargetExpression(final String targetExpression, final Map<Integer, String> targetNameCache) {
		final List<Map<String, String>> result = new Vector<>();

		if (targetExpression != null) {
			final Set<Integer> targetIds = TargetExpressionUtils.getTargetIds(targetExpression);
			for (final Integer targetId : targetIds) {
				final String targetGroupName = getTargetGroupName(targetId, targetNameCache);

				if (targetGroupName != null) {
					final Map<String, String> target = new HashMap<>();
					target.put("target_id", String.valueOf(targetId));
					target.put("target_name", targetGroupName);

					result.add(target);
				} else {
					Logger.getLogger(this.getClass()).info("Found no name target group ID " + targetId);
				}
			}
		}

		return result;
	}

	private String getTargetGroupName(final int targetId, final Map<Integer, String> targetNameCache) {
		if (targetNameCache.containsKey(targetId)) {
			return targetNameCache.get(targetId);
		}

		try {
			final String targetName = selectWithDefaultValue(logger, "SELECT target_shortname FROM dyn_target_tbl WHERE target_id = ?", String.class, null, targetId);

			if (targetName != null) {
				targetNameCache.put(targetId, targetName);
			}

			return targetName;
		} catch (final Exception e) {
			// In some cases, mailings reference to non-existing target groups.
			logger.error("Exception reading name for target group ID " + targetId, e);
			return null;
		}
	}

	@Override
	public Date getChangeDate(final int mailingID) {
		try {
			return select(logger, "SELECT change_date FROM mailing_tbl WHERE mailing_id = ?", Date.class, mailingID);
		} catch (final Exception e) {
			logger.error("error getting change_date for mailing: " + mailingID, e);
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> getMailingsForMLIDs(final Set<Integer> mailinglistIds, @VelocityCheck final int companyId) {
		final String stmt = "SELECT mailing_id FROM mailing_tbl WHERE mailinglist_id IN (" + StringUtils.join(mailinglistIds, ", ") + ") AND company_id = ? AND deleted = 0";
		try {
			return select(logger, stmt, companyId);
		} catch (final Exception e) {
			logger.error("Error reading mails for mailinglists", e);
			return null;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteAllDynTags(final int mailingId) {
		update(logger, "DELETE FROM dyn_content_tbl WHERE dyn_name_id IN (SELECT dyn_name_id FROM dyn_name_tbl WHERE mailing_id = ?)", mailingId);
		update(logger, "DELETE FROM dyn_name_tbl WHERE mailing_id = ?", mailingId);
	}

	@Override
	public List<Map<String, Object>> getSentAndScheduled(@VelocityCheck final int companyId, final Date startDate, final Date endDate) {
        String query = "SELECT mailing.mailing_id mailingid, mailing.work_status workstatus, MAX(cmp.component_id) preview_component, "
				+ " MIN(maildrop.senddate) senddate, mailing.shortname shortname, mediatype.param subject, "
                + " COALESCE(SUM(account.no_of_mailings), 0) mailssent, MAX(maildrop.genstatus) genstatus, 'W' statusField "
                + " FROM mailing_tbl mailing LEFT JOIN maildrop_status_tbl maildrop ON mailing.mailing_id = maildrop.mailing_id"
                + " LEFT JOIN mailing_mt_tbl mediatype ON maildrop.mailing_id = mediatype.mailing_id "
                + " LEFT JOIN mailing_account_tbl account ON (maildrop.mailing_id = account.mailing_id AND account.status_field = 'W')"
                + " LEFT JOIN component_tbl cmp ON (maildrop.mailing_id = cmp.mailing_id AND cmp.comptype = ?)"
                + " WHERE mailing.company_id = ? AND mailing.deleted = 0 AND maildrop.status_field = 'W' AND mediatype.mediatype = 0 "
				+ (isOracleDB() ? "AND maildrop.senddate > CAST(? AS DATE) AND maildrop.senddate < CAST(? AS DATE)" : "AND maildrop.senddate > ? AND maildrop.senddate < ?")
                + " GROUP BY mailing.mailing_id, mailing.shortname, mediatype.param, mailing.work_status "; //ORDER BY min(maildrop.senddate)";
        query = addSortingToQuery(query, "MIN(maildrop.senddate)", "desc");
        final List<Map<String, Object>> mailingList = select(logger, query, MailingComponent.TYPE_THUMBNAIL_IMAGE, companyId, startDate, endDate);
		for (final Map<String, Object> mailing : mailingList) {
			final String param = mailing.get("subject").toString();
			if (param != null) {
				final String subject = new ParameterParser(param).parse("subject");
				mailing.put("subject", subject);
			}
		}
		return mailingList;
	}

    @Override
	public List<Map<String, Object>> getPlannedMailings(@VelocityCheck final int companyId, final Date startDate, final Date endDate) {
		String query =
                "SELECT mailingid, workstatus, preview_component, senddate, shortname, subject, genstatus, " +
                "    (SELECT MAX(maildrop.status_field) FROM maildrop_status_tbl maildrop WHERE maildrop.mailing_id = mailingid AND maildrop.gendate = lastMailDropDate GROUP BY maildrop.mailing_id, maildrop.status_field) statusField " +
                "FROM ( "+
                "SELECT mailing.mailing_id mailingid," +
                "    mailing.work_status workstatus," +
                "    max(cmp.component_id) preview_component," +
                "    mailing.plan_date senddate," +
                "    mailing.shortname shortname," +
                "    mediatype.param subject, " +
                "    -1 genstatus, " +
                "    MAX(maildrop.gendate) lastMailDropDate " +
                "FROM mailing_tbl mailing" +
                "    LEFT JOIN mailing_mt_tbl mediatype ON mailing.mailing_id = mediatype.mailing_id" +
                "    LEFT JOIN maildrop_status_tbl maildrop ON mailing.mailing_id = maildrop.mailing_id" +
                "    LEFT JOIN component_tbl cmp ON (mailing.mailing_id = cmp.mailing_id AND cmp.comptype = ?) " +
                "WHERE" +
                "    mailing.company_id = ? AND" +
                "    mailing.deleted = 0 AND" +
                "    mailing.plan_date > ? AND mailing.plan_date < ? AND" +
                "    mediatype.mediatype = 0 AND" +
                "    (maildrop.senddate IS NULL OR ( " +
                "        maildrop.senddate IS NOT NULL AND maildrop.status_field IN ('A', 'T') AND " +
                "        maildrop.genchange = (SELECT max(mds.genchange) FROM maildrop_status_tbl mds WHERE mds.mailing_id = mailing.mailing_id) AND " +
                "        not exists (SELECT * FROM maildrop_status_tbl mds WHERE mds.mailing_id = mailing.mailing_id AND mds.status_field = 'W') " +
                "        ) " +
                "    ) " +
                "GROUP BY mailing.mailing_id, mailing.shortname, mediatype.param, mailing.work_status, mailing.plan_date, mediatype.param )" + (isOracleDB() ? "" : "subsel ");
        query = addUnsentMailingSort(query, "mailingid", "desc");
		final List<Map<String, Object>> mailingList = select(logger, query, MailingComponent.TYPE_THUMBNAIL_IMAGE, companyId, startDate, endDate);
		for (final Map<String, Object> mailing : mailingList) {
			final String param = mailing.get("subject").toString();
			if (param != null) {
				final String subject = new ParameterParser(param).parse("subject");
				mailing.put("subject", subject);
			}
		}
		return mailingList;
	}

	@Override
	public Map<Integer, Integer> getOpeners(@VelocityCheck final int companyId, final List<Integer> mailingsId) {
		return getOpeners(companyId, mailingsId, false);
	}

	@Override
	public Map<Integer, Integer> getOpeners(@VelocityCheck final int companyId, final Collection<Integer> mailingsId, final boolean currentRecipientsOnly) {
		String query = "SELECT COUNT(DISTINCT opl.customer_id) AS openers, opl.mailing_id FROM onepixellog_" + companyId + "_tbl opl ";
		if (currentRecipientsOnly) {
			query += " JOIN mailing_tbl m ON m.mailing_id = opl.mailing_id ";
			query += " JOIN customer_" + companyId + "_binding_tbl bind ON bind.mailinglist_id = m.mailinglist_id AND opl.customer_id = bind.customer_id ";
		}
		query += " WHERE " + makeBulkInClauseForInteger("opl.mailing_id", mailingsId) + " GROUP BY opl.mailing_id";
		final List<Map<String, Object>> resultList = select(logger, query);
		return fillAllMailings(resultList, "mailing_id", "openers", mailingsId);
	}

	@Override
	public Map<Integer, Integer> getClickers(@VelocityCheck final int companyId, final List<Integer> mailingsId) {
		return getClickers(companyId, mailingsId, false);
	}

	@Override
	public Map<Integer, Integer> getClickers(@VelocityCheck final int companyId, final Collection<Integer> mailingsId, final boolean currentRecipientsOnly) {
		String query = "SELECT COUNT(DISTINCT rlog.customer_id) AS clickers, rlog.mailing_id FROM rdirlog_" + companyId + "_tbl rlog ";
		if (currentRecipientsOnly) {
			query += " JOIN mailing_tbl m ON m.mailing_id = rlog.mailing_id ";
			query += " JOIN customer_" + companyId + "_binding_tbl bind ON bind.mailinglist_id = m.mailinglist_id AND rlog.customer_id = bind.customer_id ";
		}
		query += " WHERE " + makeBulkInClauseForInteger("rlog.mailing_id", mailingsId) + " GROUP BY rlog.mailing_id";
		final List<Map<String, Object>> resultList = select(logger, query);
		return fillAllMailings(resultList, "mailing_id", "clickers", mailingsId);
	}

	@Override
	public Map<Integer, Integer> getSentNumber(@VelocityCheck final int companyId, final Collection<Integer> mailingsId) {
		final String query = "SELECT mailing_id, " + "COALESCE(SUM(no_of_mailings), 0) sentmails FROM mailing_account_tbl WHERE " +
				makeBulkInClauseForInteger("mailing_id", mailingsId) +
                " AND (status_field = 'W' OR status_field = 'E' OR status_field = 'D') GROUP BY mailing_id ";
		final List<Map<String, Object>> resultList = select(logger, query);
		return fillAllMailings(resultList, "mailing_id", "sentmails", mailingsId);
	}

	private Map<Integer, Integer> fillAllMailings(final List<Map<String, Object>> dbList, final String keyColumn, final String valueColumn, final Collection<Integer> mailingsId) {
		final Map<Integer, Integer> result = new HashMap<>();
		for (final Integer mailingId : mailingsId) {
			result.put(mailingId, 0);
		}
		for (final Map<String, Object> row : dbList) {
			final int key = ((Number) row.get(keyColumn)).intValue();
			final int value = ((Number) row.get(valueColumn)).intValue();
			result.put(key, value);
		}
		return result;
	}

	protected static class ComMailingRowMapper implements RowMapper<ComMailing> {
		@Override
		public ComMailing mapRow(final ResultSet resultSet, final int index) throws SQLException {
			final ComMailing mailing = new ComMailingImpl();

			mailing.setCompanyID(resultSet.getInt("company_id"));
			mailing.setId(resultSet.getInt("mailing_id"));
			mailing.setShortname(resultSet.getString("shortname"));
			final String contentTypeString = resultSet.getString("content_type");
			if (contentTypeString == null) {
				mailing.setMailingContentType(null);
			} else {
				try {
					mailing.setMailingContentType(MailingContentType.getFromString(contentTypeString));
				} catch (final Exception e) {
					throw new SQLException(e.getMessage(), e);
				}
			}
			mailing.setDescription(resultSet.getString("description"));
			mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
			mailing.setMailTemplateID(resultSet.getInt("mailtemplate_id"));
			mailing.setMailinglistID(resultSet.getInt("mailinglist_id"));
            mailing.setDeleted(resultSet.getInt("deleted"));
			mailing.setMailingType(resultSet.getInt("mailing_type"));
			mailing.setCampaignID(resultSet.getInt("campaign_id"));
			mailing.setArchived(resultSet.getInt("archived"));
			mailing.setTargetExpression(resultSet.getString("target_expression"));
			mailing.setSplitID(resultSet.getInt("split_id"));
			mailing.setUseDynamicTemplate(resultSet.getInt("dynamic_template") > 0);
			mailing.setLocked(resultSet.getInt("test_lock"));
			mailing.setPriority(resultSet.getInt("priority"));
			mailing.setPrioritizationAllowed(resultSet.getBoolean("is_prioritization_allowed"));
			mailing.setIsTemplate(resultSet.getInt("is_template") > 0);
			mailing.setNeedsTarget(resultSet.getInt("needs_target") > 0);
			mailing.setOpenActionID(resultSet.getInt("openaction_id"));
			mailing.setClickActionID(resultSet.getInt("clickaction_id"));
			mailing.setStatusmailRecipients(resultSet.getString("statmail_recp"));
            mailing.setPlanDate(resultSet.getTimestamp("plan_date"));

			return mailing;
		}
	}

	/**
	 * TODO: Replace references to Mailing by ComMailing. (currently more than 1000) and than replace this special ComMailingToMailingRowMapper by ComMailingRowMapper
	 */
	protected static class ComMailingToMailingRowMapper implements RowMapper<Mailing> {
		@Override
		public Mailing mapRow(final ResultSet resultSet, final int index) throws SQLException {
			final ComMailing mailing = new ComMailingImpl();

			mailing.setCompanyID(resultSet.getInt("company_id"));
			mailing.setId(resultSet.getInt("mailing_id"));
			mailing.setShortname(resultSet.getString("shortname"));
			final String contentTypeString = resultSet.getString("content_type");
			if (contentTypeString == null) {
				mailing.setMailingContentType(null);
			} else {
				try {
					mailing.setMailingContentType(MailingContentType.getFromString(contentTypeString));
				} catch (final Exception e) {
					throw new SQLException(e.getMessage(), e);
				}
			}
			mailing.setDescription(resultSet.getString("description"));
			mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
			mailing.setMailTemplateID(resultSet.getInt("mailtemplate_id"));
			mailing.setMailinglistID(resultSet.getInt("mailinglist_id"));
            mailing.setDeleted(resultSet.getInt("deleted"));
			mailing.setMailingType(resultSet.getInt("mailing_type"));
			mailing.setCampaignID(resultSet.getInt("campaign_id"));
			mailing.setArchived(resultSet.getInt("archived"));
			mailing.setTargetExpression(resultSet.getString("target_expression"));
			mailing.setSplitID(resultSet.getInt("split_id"));
			mailing.setUseDynamicTemplate(resultSet.getInt("dynamic_template") > 0);
			mailing.setLocked(resultSet.getInt("test_lock"));
			mailing.setIsTemplate(resultSet.getInt("is_template") > 0);
			mailing.setNeedsTarget(resultSet.getInt("needs_target") > 0);
			mailing.setOpenActionID(resultSet.getInt("openaction_id"));
			mailing.setClickActionID(resultSet.getInt("clickaction_id"));
			mailing.setStatusmailRecipients(resultSet.getString("statmail_recp"));
            mailing.setPlanDate(resultSet.getTimestamp("plan_date"));

			return mailing;
		}
	}


	protected static class MailingTemplatesWithPreviewRowMapper implements RowMapper<MailingBase> {

		@Override
		public MailingBase mapRow(final ResultSet resultSet, final int i) throws SQLException {
			final ComMailing mailing = new ComMailingImpl();

			mailing.setId(resultSet.getInt("mailing_id"));
			mailing.setShortname(resultSet.getString("shortname"));
			mailing.setDescription(resultSet.getString("description"));
			mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
			mailing.setPreviewComponentId(resultSet.getInt("preview_component"));

			return mailing;
		}
	}

	/**
	 * This Rowmapper reads all values from mailing_tbl also the class MailingBase cannot hold them all.
	 * This is for keeping former code especially in JSPs working, which might use this values by reflection.
	 * Hibernate returned all data of mailing_tbl in the old version of this Dao.
	 *
	 * TODO: Check JSPs and return real MailingBase with reduced data
	 */
	protected static class ComMailingToMailingBaseRowMapper implements RowMapper<MailingBase> {
		@Override
		public MailingBase mapRow(final ResultSet resultSet, final int index) throws SQLException {
			final ComMailing mailing = new ComMailingImpl();

			mailing.setCompanyID(resultSet.getInt("company_id"));
			mailing.setId(resultSet.getInt("mailing_id"));
			mailing.setShortname(resultSet.getString("shortname"));
			final String contentTypeString = resultSet.getString("content_type");
			if (contentTypeString == null) {
				mailing.setMailingContentType(null);
			} else {
				try {
					mailing.setMailingContentType(MailingContentType.getFromString(contentTypeString));
				} catch (final Exception e) {
					throw new SQLException(e.getMessage(), e);
				}
			}
			mailing.setDescription(resultSet.getString("description"));
			mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
			mailing.setMailTemplateID(resultSet.getInt("mailtemplate_id"));
			mailing.setMailinglistID(resultSet.getInt("mailinglist_id"));
            mailing.setDeleted(resultSet.getInt("deleted"));
			mailing.setMailingType(resultSet.getInt("mailing_type"));
			mailing.setCampaignID(resultSet.getInt("campaign_id"));
			mailing.setArchived(resultSet.getInt("archived"));
			mailing.setTargetExpression(resultSet.getString("target_expression"));
			mailing.setSplitID(resultSet.getInt("split_id"));
			mailing.setUseDynamicTemplate(resultSet.getInt("dynamic_template") > 0);
			mailing.setLocked(resultSet.getInt("test_lock"));
			mailing.setIsTemplate(resultSet.getInt("is_template") > 0);
			mailing.setNeedsTarget(resultSet.getInt("needs_target") > 0);
			mailing.setOpenActionID(resultSet.getInt("openaction_id"));
			mailing.setClickActionID(resultSet.getInt("clickaction_id"));
			mailing.setStatusmailRecipients(resultSet.getString("statmail_recp"));
            mailing.setPlanDate(resultSet.getTimestamp("plan_date"));

			return mailing;
		}
	}

	protected static class DynamicTagContentRowMapper implements RowMapper<DynamicTagContent> {
		@Override
		public DynamicTagContent mapRow(final ResultSet resultSet, final int index) throws SQLException {
			final DynamicTagContent tag = new DynamicTagContentImpl();

			tag.setCompanyID(resultSet.getInt("company_id"));
			tag.setMailingID(resultSet.getInt("mailing_id"));
			tag.setId(resultSet.getInt("dyn_content_id"));
			tag.setDynNameID(resultSet.getInt("dyn_name_id"));
			tag.setTargetID(resultSet.getInt("target_id"));
			tag.setDynOrder(resultSet.getInt("dyn_order"));
			tag.setDynContent(resultSet.getString("dyn_content"));

			return tag;
		}
	}

	protected static class DynamicTagRowMapper implements RowMapper<DynamicTag> {
		@Override
		public DynamicTag mapRow(final ResultSet resultSet, final int index) throws SQLException {
			final DynamicTag tag = new DynamicTagImpl();

			tag.setCompanyID(resultSet.getInt("company_id"));
			tag.setMailingID(resultSet.getInt("mailing_id"));
			tag.setId(resultSet.getInt("dyn_name_id"));
			tag.setDynName(resultSet.getString("dyn_name"));
			tag.setGroup(resultSet.getInt("dyn_group"));
			tag.setDynInterestGroup(resultSet.getString("interest_group"));
			tag.setDisableLinkExtension(resultSet.getInt("no_link_extension") == 1);

			return tag;
		}
	}

    @Override
	public int copyMailing(final int mailingId, final int companyId, final String newMailingNamePrefix) throws Exception {
        final ComMailing mailing = (ComMailing) getMailing(mailingId, companyId);
        final ComMailing newMailing = copyMailing(companyId, mailing.getMailinglistID(), mailing, mailing.isIsTemplate());
        newMailing.setShortname(newMailingNamePrefix + newMailing.getShortname());
        newMailing.setMailingContentType(mailing.getMailingContentType());
        saveMailing(newMailing, false);
        return newMailing.getId();
    }

    @Override
	public Mailing copyMailing(final int newCompanyId, final int mailinglistID, final Mailing aTemplate, final boolean isTemplate) throws Exception {
    	return copyMailing(newCompanyId, mailinglistID, (ComMailing) aTemplate, isTemplate);
    }

	@Override
	public ComMailing copyMailing(final int newCompanyId, final int mailinglistID, final ComMailing aTemplate, final boolean isTemplate) throws Exception {
		aTemplate.setCompanyID(newCompanyId);
		final Map<String, MailingComponent> components = new HashMap<>();
		//loop components and set new companyID (otherwise components would not be saved)
		for (final Entry<String, MailingComponent> componentEntry : aTemplate.getComponents().entrySet()) {
			final MailingComponent entry = componentEntry.getValue();
			entry.setCompanyID(newCompanyId);
			components.put(componentEntry.getKey(), entry);
		}
		aTemplate.setComponents(components);

		final Map<String, DynamicTag> dynTags = new HashMap<>();
		// loop dyntags and set new companyID (otherwise dyntags would not be saved)
		for (final Entry<String, DynamicTag> dynTagEntry : aTemplate.getDynTags().entrySet()) {
			final DynamicTag entry = dynTagEntry.getValue();
			entry.setCompanyID(newCompanyId);
			entry.setId(0);

			final Map<Integer, DynamicTagContent> content = new HashMap<>();
			// loop content and set new companyID (otherwise content would not be saved)
			for (final Entry<Integer, DynamicTagContent> dynContentEntry : entry.getDynContent().entrySet()) {
				final DynamicTagContent dyn = dynContentEntry.getValue();
				dyn.setCompanyID(newCompanyId);
				content.put(dynContentEntry.getKey(), dyn);
			}
			entry.setDynContent(content);
			dynTags.put(dynTagEntry.getKey(), entry);
		}
		aTemplate.setDynTags(dynTags);

		final ComMailing aMailing = (ComMailing) aTemplate.clone(applicationContext);
		aMailing.setId(0);
		aMailing.setMailTemplateID(0);
		aMailing.setMailinglistID(mailinglistID);
		aMailing.setIsTemplate(isTemplate);
		aMailing.setMailingType(aTemplate.getMailingType());
		aMailing.setCompanyID(newCompanyId);
		aMailing.setShortname(aTemplate.getShortname());
		aMailing.setMailingContentType(aTemplate.getMailingContentType());
		final Mediatype mt = new MediatypeEmailImpl();
		mt.setStatus(Mediatype.STATUS_ACTIVE);
		final Map<Integer, Mediatype> mediatypes = new HashMap<>();
		mediatypes.put(new Integer(0), mt);
		aMailing.setMediatypes(mediatypes);
		MediatypeEmail paramEmail = null;
		paramEmail = aMailing.getEmailParam();
		paramEmail.setSubject(aTemplate.getEmailParam().getSubject());
		paramEmail.setMailFormat(aTemplate.getEmailParam().getMailFormat());
		paramEmail.setLinefeed(aTemplate.getEmailParam().getLinefeed());
		paramEmail.setCharset(aTemplate.getEmailParam().getCharset());
		paramEmail.setOnepixel(aTemplate.getEmailParam().getOnepixel());
		paramEmail.setFromEmail(aTemplate.getEmailParam().getFromEmail());
		paramEmail.setFromFullname(aTemplate.getEmailParam().getFromFullname());
		paramEmail.setReplyEmail(aTemplate.getEmailParam().getReplyEmail());
		paramEmail.setReplyFullname(aTemplate.getEmailParam().getReplyFullname());

		aMailing.buildDependencies(true, applicationContext);
		saveMailing(aMailing, false);
        return aMailing;
	}

    @Override
    public Map<String, Object> getMailingWithWorkStatus(final int mailingId, final int companyId) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT a.company_id, a.mailing_id, a.shortname, a.description, a.work_status, a.mailing_type, b.senddate, a.change_date, ");
        if (isOracleDB()) {
            queryBuilder.append("(SELECT LISTAGG(dep.workflow_id, ',') WITHIN GROUP (ORDER BY a.mailing_id) ")
				.append("FROM workflow_dependency_tbl dep WHERE dep.company_id = ? AND dep.type = ? AND dep.entity_id = a.mailing_id) AS usedInWorkflows ");

            queryBuilder
    			.append("FROM mailing_tbl a ")
    			.append("LEFT OUTER JOIN( ")
    			.append("SELECT DISTINCT mailing_id, senddate, genchange FROM ( ")
    			.append("SELECT mailing_id, senddate, genchange, MAX(genchange) OVER (partition BY mailing_id) AS max_date ")
    			.append("FROM maildrop_status_tbl ) WHERE max_date = genchange")
    			.append(") b ON (a.mailing_id = b.mailing_id) ")
    			.append("WHERE a.company_id = ? AND a.mailing_id = ?");
        } else {
            queryBuilder.append("(SELECT GROUP_CONCAT(dep.workflow_id SEPARATOR ',') ")
				.append("FROM workflow_dependency_tbl dep WHERE dep.company_id = ? AND dep.type = ? AND dep.entity_id = a.mailing_id) AS usedInWorkflows ");

            queryBuilder
    			.append("FROM mailing_tbl a ")
    			.append("LEFT OUTER JOIN(")
    			.append("SELECT DISTINCT mds.mailing_id, mds.senddate, mds.genchange FROM maildrop_status_tbl mds ")
                .append("INNER JOIN (SELECT mailing_id, MAX(genchange) AS max_date FROM maildrop_status_tbl GROUP BY mailing_id) max_genchange ")
                .append("ON mds.mailing_id = max_genchange.mailing_id AND mds.genchange = max_genchange.max_date")
    			.append(") b ON (a.mailing_id = b.mailing_id) ")
    			.append("WHERE a.company_id = ? AND a.mailing_id = ?");
        }
        final List<Map<String, Object>> list = select(logger, queryBuilder.toString(), companyId, WorkflowDependencyType.MAILING_DELIVERY.getId(), companyId, mailingId);
        return (list.size() > 0 ? list.get(0) : new HashMap<>());
    }

    @Override
	@DaoUpdateReturnValueCheck
    public void cleanTestDataInSuccessTbl(final int mailingId, @VelocityCheck final int companyId) {
        // delete from success_<companyId>_tbl
        final boolean individualSuccessTableExists = DbUtilities.checkIfTableExists(getDataSource(), getSuccessTableName(companyId));
        if (individualSuccessTableExists) {
			String deleteAlias = "";
			if (!isOracleDB()) {
			    deleteAlias = "sc";
			}
			final String query = "DELETE " + deleteAlias + " FROM " + getSuccessTableName(companyId) + " sc"
				+ " WHERE sc.mailing_id = ?"
				+ " AND EXISTS (SELECT 1 FROM " + getBindingTableName(companyId) + " bind"
					+ " WHERE bind.customer_id = sc.customer_id"
					+ " AND bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')"
					+ " AND bind.mailinglist_id IN (SELECT m.mailinglist_id FROM mailing_tbl m WHERE m.mailing_id = ? AND m.company_id = ?))";
            update(logger, query, mailingId, mailingId, companyId);
        }
    }

    @Override
	@DaoUpdateReturnValueCheck
    public void cleanTestDataInMailtrackTbl(final int mailingId, @VelocityCheck final int companyId) {
		final String query = "DELETE" + (isOracleDB() ? "" : " mtrack") + " FROM mailtrack_" + companyId + "_tbl mtrack"
			+ " WHERE EXISTS (SELECT 1 FROM maildrop_status_tbl mds"
				+ " WHERE mds.status_id = mtrack.maildrop_status_id"
				+ " AND mds.mailing_id = ?)"
			+ " AND EXISTS (SELECT 1 FROM " + getBindingTableName(companyId) + " bind"
				+ " WHERE bind.customer_id = mtrack.customer_id"
				+ " AND bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')"
				+ " AND bind.mailinglist_id IN (SELECT m.mailinglist_id FROM mailing_tbl m WHERE m.mailing_id = ? AND m.company_id = ?))";
        update(logger, query, mailingId, mailingId, companyId);
    }

    /**
	 * Retrieve list split ID for mailing ID.
	 *
	 * @param mailingId mailing ID
	 *
	 * @return list split ID or 0
	 */
	private int getListSplitId( final int mailingId) {
		try {
			return selectInt(logger, "SELECT split_id FROM mailing_tbl WHERE mailing_id = ?", mailingId);
		} catch( final Exception e) {
			logger.warn("Error reading list split ID for mailing " + mailingId, e);
			return 0;
		}
	}

    private String addSortingToQuery(final String query, final String sortField, final String sortDirection) {
		String sorting = getSortingClause(sortField, sortDirection);

		if (sorting.isEmpty()) {
			return query;
		} else {
			return query + " " + sorting;
		}
    }

    private String getSortingClause(final String column, final String direction) {
		String expression = getSortingExpression(column, direction);

		if (expression.isEmpty()) {
			return "";
		} else {
			return "ORDER BY " + expression;
		}
	}

    private String getSortingExpression(final String column, final String direction) {
		if (StringUtils.isBlank(column)) {
			return "";
		}

		String directionKeyword = AgnUtils.sortingDirectionToBoolean(direction, true) ? "ASC" : "DESC";

		if (isOracleDB()) {
			return String.format("%s %s NULLS LAST", column, directionKeyword);
		} else {
			return String.format("ISNULL(%s), %s %s", column, column, directionKeyword);
		}
	}

    private String addUnsentMailingSort(final String query, final String sort, final String direction) {
        String unsentSort = (sort == null) ? "" : sort;
        String unsentDirection = (direction == null) ? "" : direction;
        if (unsentSort.equalsIgnoreCase("date")) {
            unsentSort = "creation_date";
        }
        if (unsentSort.isEmpty()) {
            unsentSort = "m.mailing_id";
            if (unsentDirection.isEmpty()) {
                unsentDirection = "DESC";
            }
        }
        return addSortingToQuery(query, unsentSort, unsentDirection);
    }

    public String addSentMailingSort(final String query, final String sort, final String direction) {
        String sentSort = (sort == null) ? "" : sort;
        String sentDirection = (direction == null) ? "" : direction;
        if (sentSort.equalsIgnoreCase("date")) {
            sentSort = "senddate";
        }
        if (sentSort.isEmpty()) {
            sentSort = "m.mailing_id";
            if (sentDirection.isEmpty()) {
                sentDirection = "DESC";
            }
        }
        return addSortingToQuery(query, sentSort, sentDirection);
    }

	@Override
	public List<Integer> getMailingIdsForIntervalSend() {
		String sql;
		if (isOracleDB()) {
			sql = "SELECT DISTINCT mail.mailing_id FROM mailing_info_tbl info, mailing_tbl mail WHERE mail.deleted = 0 AND mail.mailing_id = info.mailing_id AND mail.mailing_type = ? AND mail.work_status = 'mailing.status.active' AND info.name = ? AND TO_DATE(info.value, 'YYYY.MM.DD HH24:MI') < CURRENT_TIMESTAMP AND NOT EXISTS (SELECT 1 FROM mailing_info_tbl b WHERE b.mailing_id = info.mailing_id AND b.name = ?)";
		} else {
			sql = "SELECT DISTINCT mail.mailing_id FROM mailing_info_tbl info, mailing_tbl mail WHERE mail.deleted = 0 AND mail.mailing_id = info.mailing_id AND mail.mailing_type = ? AND mail.work_status = 'mailing.status.active' AND info.name = ? AND DATE_FORMAT(info.value,'%Y.%m.%d %H:%i') < CURRENT_TIMESTAMP AND NOT EXISTS (SELECT 1 FROM mailing_info_tbl b WHERE b.mailing_id = info.mailing_id AND b.name = ?)";
		}
		return select(logger, sql, new IntegerRowMapper(), Mailing.TYPE_INTERVAL, ComMailingParameterDao.PARAMETERNAME_NEXT_START, ComMailingParameterDao.PARAMETERNAME_ERROR);

	}

	@Override
	public final boolean isActiveIntervalMailing(final int mailingID) {
		final String sql = "SELECT count(mail.mailing_id) FROM mailing_info_tbl info, mailing_tbl mail WHERE mail.mailing_id = ? AND mail.mailing_id = info.mailing_id AND mail.mailing_type = ? AND mail.work_status = 'mailing.status.active' AND info.name = ?";

		final int result = selectInt(logger, sql, mailingID, Mailing.TYPE_INTERVAL, ComMailingParameterDao.PARAMETERNAME_NEXT_START);

		return result > 0;
	}

	@Override
	public MailingSendStatus getMailingSendStatus(final int mailingID, @VelocityCheck final int companyID) {
		final MailingSendStatus status = new MailingSendStatusImpl();

		final int daysToExpire = companyDao.getSuccessDataExpirePeriod(companyID);
		status.setExpirationDays(daysToExpire);

		final String sqlGetSendStatus = "SELECT COUNT(*) FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field NOT IN ('A', 'T')";

		final int sentEntries = selectInt(logger, sqlGetSendStatus, companyID, mailingID);
		status.setHasMailtracks(sentEntries > 0);

		if (status.getHasMailtracks()) {
			final int dataEntries = selectInt(logger, "SELECT COUNT(*) FROM success_" + companyID + "_tbl WHERE mailing_id = ?", mailingID);
			status.setHasMailtrackData(dataEntries > 0);
		}

		return status;
	}

	@Override
	public List<Mailing> getMailingsForMLID(@VelocityCheck final int companyID, final int mailinglistID) {
		final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, dynamic_template, plan_date"
				+ " FROM mailing_tbl"
				+ " WHERE company_id = ? AND mailinglist_id = ? AND deleted = 0";

		return select(logger, sql, new ComMailingToMailingRowMapper(), companyID, mailinglistID);
	}

	/**
	 * Build an SQL-expression from th egiven target_expression.
	 * The expression is a list of targetIDs connected with the operators:
	 * <ul>
	 * <li>( - block start
	 * <li>) - block end
	 * <li>&amp; - AND
	 * <li>| - OR
	 * <li>! - NOT
	 * </ul>
	 * @param targetExpression The expression as string.
	 * @return the resulting where clause.
	 */
	@Override
	public String getSQLExpression(final String targetExpression) {
		if (targetExpression == null) {
			return null;
		}

		final StringBuffer buf = new StringBuffer();
		final int tlen = targetExpression.length();

		for (int n = 0; n < tlen; ++n) {
			final char ch = targetExpression.charAt(n);

			if ((ch == '(') || (ch == ')')) {
				buf.append(ch);
			} else if ((ch == '&') || (ch == '|')) {
				if (ch == '&') {
					buf.append(" AND");
				} else {
					buf.append(" OR");
				}
				while (((n + 1) < tlen) && (targetExpression.charAt(n + 1) == ch)) {
					++n;
				}
			} else if (ch == '!') {
				buf.append(" NOT");
			} else if (Character.isDigit(ch)) {
				String temp = "";
				final int first = n;
				int tid = (-1);

				while (n < tlen && Character.isDigit(targetExpression.charAt(n))) {
					n++;
				}
				tid = Integer.parseInt(targetExpression.substring(first, n));
				n--;
				temp = select(logger, "SELECT target_sql FROM dyn_target_tbl WHERE target_id = ?", String.class, tid);
				if (temp != null && temp.trim().length() > 2) {
					buf.append(" (");
					buf.append(temp);
					buf.append(")");
				}
			}
		}

		if (buf.length() >= 3) {
			return buf.toString();
		} else {
			return null;
		}
	}

	/**
	 * Finds the last newsletter that would have been sent to the given
	 * customer.
	 * @param customerID Id of the recipient for the newsletter.
	 * @param companyID the company to look in.
	 * @return The mailingID of the last newsletter that would have been
	 *              sent to this recipient.
	 */
	@Override
	public int findLastNewsletter(final int customerID, @VelocityCheck final int companyID, final int mailinglist) {
		final String	sql = "SELECT m.mailing_id, m.target_expression, a.timestamp"
				+ " FROM mailing_tbl m"
				+ " LEFT JOIN mailing_account_tbl a ON a.mailing_id = m.mailing_id"
				+ " WHERE m.company_id = ? AND m.deleted = 0 AND m.is_template = 0 AND a.status_field = 'W' and m.mailinglist_id = ? ORDER BY a.timestamp desc, m.mailing_id DESC";

		try {
			final List<Map<String, Object>> list = select(logger, sql, companyID, mailinglist);

			for (final Map<String, Object> map : list) {
				final int mailing_id = ((Number) map.get("mailing_id")).intValue();
				final String targetExpression = (String) map.get("target_expression");

				if (targetExpression == null || targetExpression.trim().length() == 0) {
					return mailing_id;
				} else {
					if (selectInt(logger, "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust WHERE " + getSQLExpression(targetExpression) + " AND customer_id = ?", customerID) > 0) {
						return mailing_id;
					}
				}
			}
			return 0;
		} catch (final Exception e) {
			logger.error( "findLastNewsletter: " + e.getMessage(), e);
			return 0;
		}
	}

	@Override
	public String getAutoURL(final int mailingID) {
		try	{
			return select(logger, "SELECT auto_url FROM mailing_tbl WHERE mailing_id = ?", String.class, mailingID);
		} catch(final Exception e) {
			logger.error( "getAutoURL: " + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String getAutoURL(final int mailingID, @VelocityCheck final int companyID) {
		final String rdir_mailinglistquery = "SELECT ml.rdir_domain FROM mailinglist_tbl ml JOIN mailing_tbl m ON ml.mailinglist_id = m.mailinglist_id WHERE ml.deleted = 0 AND m.mailing_id = ?";
		final String rdirdomain = selectObjectDefaultNull(logger, rdir_mailinglistquery, new StringRowMapper(), mailingID);
		if (rdirdomain != null ) {
			return rdirdomain;
		} else {
			final String rdir_companyquery = "SELECT rdir_domain FROM company_tbl WHERE company_id = ?";
			return select(logger, rdir_companyquery, String.class, companyID);
		}
	}

	/**
	 * Setter for property applicationContext.
	 * @param applicationContext New value of property applicationContext.
	 */
	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
	    this.applicationContext = applicationContext;
	}

	@Override
	public boolean isBasicFullTextSearchSupported() {
		return checkIndicesAvailable(logger, "mailing$sname$idx", "mailing$descr$idx");
	}

	@Override
	public boolean isContentFullTextSearchSupported() {
		return checkIndicesAvailable(logger, "component$emmblock$idx", "dyncontent$content$idx");
	}

	@Override
	public String getFormat(final int type) {
		try {
	        return select(logger, "SELECT format FROM date_tbl WHERE type = ?", String.class, type);
	    } catch (final Exception e) {
	    	logger.error("Query failed for data_tbl: " + e.getMessage(), e);
	    	return "d.M.yyyy";
	    }
	}

	@Override
	public int getGenstatusForWorldMailing(final int mailingID) {
		return selectIntWithDefaultValue(logger, "SELECT genstatus FROM maildrop_status_tbl WHERE mailing_id = ? and status_field = 'W'", -1, mailingID);
	}

	@Override
	public int getLastGenstatus(final int mailingID, final char ...statuses) {
		final StringBuilder sqlQueryBuilder = new StringBuilder();
		final List<Object> sqlParameters = new ArrayList<>();

		if (isOracleDB()) {
			sqlQueryBuilder.append("SELECT * FROM (");
		}

		sqlQueryBuilder.append("SELECT genstatus FROM maildrop_status_tbl WHERE mailing_id = ?");
		sqlParameters.add(mailingID);

		if (statuses.length > 0) {
			sqlQueryBuilder.append(" AND status_field IN (");
			for (int i = 0; i < statuses.length; i++) {
				sqlParameters.add(Character.toString(statuses[i]));
				if (i + 1 < statuses.length) {
					sqlQueryBuilder.append("?, ");
				} else {
					sqlQueryBuilder.append("?");
				}
			}
			sqlQueryBuilder.append(")");
		}

		sqlQueryBuilder.append(" ORDER BY senddate DESC");

		if (isOracleDB()) {
			sqlQueryBuilder.append(") WHERE ROWNUM = 1");
		} else {
			sqlQueryBuilder.append(" LIMIT 1");
		}
		return selectIntWithDefaultValue(logger, sqlQueryBuilder.toString(), -1, sqlParameters.toArray());
	}

	@Override
	public boolean hasPreviewRecipients(final int mailingID, @VelocityCheck final int companyID) {
		if (companyID <= 0 || mailingID <= 0) {
			return false;
		} else {
			final String query = "SELECT DISTINCT 1"
					+ " FROM mailing_tbl m, mailinglist_tbl ml, customer_" + companyID + "_binding_tbl c"
					+ " WHERE m.company_id = ml.company_id AND m.mailinglist_id = ml.mailinglist_id AND c.mailinglist_id = ml.mailinglist_id"
					+ " AND c.user_status = 1 AND c.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND ml.deleted = 0 AND m.company_id = ? AND m.mailing_id = ?";

			try {
				selectInt(logger, query, companyID, mailingID);
				return true;
			} catch (final Exception e) {
				logger.error("hasPreviewRecipients: mailingID = " + mailingID + ", companyID = " + companyID, e);
				return false;
			}
		}
	}

	@Override
	public boolean hasActions(final int mailingId, @VelocityCheck final int companyID) {
		final String stmt = "SELECT COUNT(action_id) FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ? AND action_id != 0";
		try {
			final int count = selectInt(logger, stmt, mailingId, companyID);
	        return count > 0;
		} catch (final Exception e) {
			logger.error( "hasActions: " + e.getMessage(), e);
		    return false;
		}
	}

	/**
	 * Names and descriptions
	 */
	@Override
	public String compareMailingsNameAndDesc(final String mailingIDList, final Map<Integer, String> allNames, final Map<Integer, String> allDesc, @VelocityCheck final int companyID) {
	    final StringBuilder returnValue = new StringBuilder();
	    final String sql = "SELECT shortname, description, mailing_id FROM mailing_tbl A WHERE company_id = ? AND mailing_id IN (" + mailingIDList + ")";

	    try {
	    	final List<Map<String, Object>> result = select(logger, sql, companyID);

	        for (final Map<String, Object>row: result) {
	        	final String shortname = (String) row.get("shortname");
	        	final String description = (String) row.get("description");
	        	final int mailingID = ((Number) row.get("mailing_id")).intValue();

	            allNames.put(mailingID, shortname);
	            allDesc.put(mailingID, description);
	            returnValue.append("\r\n" + shortname + " (" + description + ")");
	        }
	    } catch (final Exception e) {
	    	logger.error( "error loading mailing info (sql: " + sql + ")", e);
	    }

	    return returnValue.toString();
	}

	/**
	 * Opened Mails
	 */
	@Override
	public int compareMailingsOpened(final String mailingIDList, @VelocityCheck final int companyID, final Map<Integer, Integer> allOpen, int biggestOpened, final ComTarget aTarget) {
	    final StringBuffer sqlBuf = new StringBuffer();
        sqlBuf.append("SELECT COUNT(onepixel.customer_id) AS custsum, onepixel.mailing_id FROM onepixellog_").append(companyID).append("_tbl onepixel");
	    if (aTarget.getId() != 0) {
	        sqlBuf.append(", customer_");
	        sqlBuf.append(companyID);
	        sqlBuf.append("_tbl cust");
	    }
	    sqlBuf.append(" WHERE mailing_id IN (");
	    sqlBuf.append(mailingIDList);
	    sqlBuf.append(")");
	    if (aTarget.getId() != 0) {
	        sqlBuf.append(" AND ((");
	        sqlBuf.append(aTarget.getTargetSQL());
	        sqlBuf.append(") AND onepixel.customer_id = cust.customer_id)");
	    }
	    sqlBuf.append(" GROUP BY mailing_id");

	    try {
	    	final List<Map<String, Object>> result = select(logger, sqlBuf.toString());

	        for (final Map<String, Object> row : result) {
	        	final int custSum = ((Number) row.get("custsum")).intValue();
	        	final int mailingID = ((Number) row.get("mailing_id")).intValue();

	            allOpen.put(mailingID, new Integer(custSum));
	            if (custSum > biggestOpened) {
	                biggestOpened = custSum;
	            }
	        }
	    } catch (final Exception e) {
	    	logger.error( "Error getting opened mails (sql: " + sqlBuf.toString() + ")", e);
	    }

	    return biggestOpened;
	}

	/**
	 * Total CLicks
	 */
	@Override
	public int compareMailingsTotalClicks(final String mailingIDList, final Map<Integer, Integer> allClicks, int biggestClicks, @VelocityCheck final int companyID, final ComTarget aTarget) {
	    final StringBuffer sqlBuf = new StringBuffer("SELECT COUNT(rdir.customer_id) AS custsum, rdir.url_id, rdir.mailing_id FROM rdirlog_" + companyID + "_tbl rdir");
	    if (aTarget.getId() != 0) {
	        sqlBuf.append(", customer_");
	        sqlBuf.append(companyID);
	        sqlBuf.append("_tbl cust");
	    }
	    sqlBuf.append(" WHERE rdir.mailing_id IN (");
	    sqlBuf.append(mailingIDList);
	    sqlBuf.append(")");

	    if (aTarget.getId() != 0) {
	        sqlBuf.append(" AND ((");
	        sqlBuf.append(aTarget.getTargetSQL());
	        sqlBuf.append(") AND cust.customer_id = rdir.customer_id)");
	    }
	    sqlBuf.append(" GROUP BY rdir.url_id, rdir.mailing_id");

	    try {
	    	final List<Map<String, Object>> result = select(logger, sqlBuf.toString());

	        for (final Map<String, Object> row : result) {
	        	final int custSum = ((Number) row.get("custsum")).intValue();
	        	final int mailingID = ((Number) row.get("mailing_id")).intValue();
	            int aVal = 0;

	            if (allClicks.containsKey(mailingID)) {
	                aVal = (allClicks.get(mailingID)).intValue();
	                aVal += custSum;
	            } else {
	                aVal = custSum;
	            }
	            allClicks.put(mailingID, aVal);
	            if (aVal > biggestClicks) {
	                biggestClicks = aVal;
	            }
	        }
	    } catch (final Exception e) {
	    	logger.error( "Error getting total clicks (sql: " + sqlBuf.toString() + ")", e);
	    }

	    return biggestClicks;
	}

	/**
	 * Optout and Bounce
	 */
	@Override
	public Map<String, Integer> compareMailingsOptoutAndBounce(final String mailingIDList, final Map<Integer, Integer> allOptout, final Map<Integer, Integer> allBounce, int biggestOptout, int biggestBounce, @VelocityCheck final int companyID, final ComTarget aTarget) {
	    final StringBuffer sqlBuf = new StringBuffer("SELECT COUNT(bind.customer_id) AS custsum, bind.user_status, bind.exit_mailing_id FROM customer_" + companyID + "_binding_tbl bind");
	    if (aTarget.getId() != 0) {
	        sqlBuf.append(", customer_");
	        sqlBuf.append(companyID);
	        sqlBuf.append("_tbl cust");
	    }
	    sqlBuf.append(" WHERE exit_mailing_id IN (");
	    sqlBuf.append(mailingIDList);
	    sqlBuf.append(")");
	    if (aTarget.getId() != 0) {
	        sqlBuf.append(" AND ((");
	        sqlBuf.append(aTarget.getTargetSQL());
	        sqlBuf.append(") AND cust.customer_id = bind.customer_id)");
	    }
	    sqlBuf.append(" GROUP BY bind.user_status, bind.exit_mailing_id, bind.mailinglist_id");

	    try {
	    	final List<Map<String, Object>> result = select(logger, sqlBuf.toString());

	        for (final Map<String, Object> row : result) {
	        	final int custSum = ((Number) row.get("custsum")).intValue();
	        	final int exitMailingID = ((Number) row.get("exit_mailing_id")).intValue();
	        	final int userStatus = ((Number) row.get("user_status")).intValue();

				switch (UserStatus.getUserStatusByID(userStatus)) {
					case AdminOut:
					case UserOut:
						allOptout.put(exitMailingID, new Integer(custSum));
						if (custSum > biggestOptout) {
							biggestOptout = custSum;
						}
						break;

					case Bounce:
						int tmpVal = 0;
						if (allBounce.containsKey(exitMailingID)) {
							tmpVal = (allBounce.get(exitMailingID)).intValue();
						}
						if (custSum > tmpVal) {
							tmpVal = custSum;
						}
						allBounce.put(exitMailingID, new Integer(tmpVal));
						if (custSum > biggestBounce) {
							biggestBounce = tmpVal;
						}
						break;

					default:
						// do nothing
				}
	        }
	    } catch (final Exception e) {
	    	logger.error( "Error getting optout (sql: " + sqlBuf.toString() + ")", e);
	    }

	    final Map<String, Integer> optoutBounce = new HashMap<>();
	    optoutBounce.put("biggestBounce", biggestBounce);
	    optoutBounce.put("biggestOptout", biggestOptout);
	    return optoutBounce;
	}

	@Override
	public List<Mailing> getTemplates(@VelocityCheck final int companyID) {
	    final String sqlStatement = "SELECT mailing_id, shortname FROM mailing_tbl WHERE company_id = ? AND is_template = 1 AND deleted = 0 ORDER BY shortname";
		final List<Map<String, Object>> tmpList = select(logger, sqlStatement, companyID);

	    final List<Mailing> result = new ArrayList<>();
	    for (final Map<String, Object> row : tmpList) {
	    	final ComMailing newBean = new ComMailingImpl();
	        newBean.setId(((Number) row.get("mailing_id")).intValue());
	        newBean.setShortname((String) row.get("shortname"));
	        result.add(newBean);
	    }
	    return result;
	}

	@Override
	public List<MailingBase> getTemplateMailingsByCompanyID(@VelocityCheck final int companyID) {
		final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, dynamic_template, plan_date"
				+ " FROM mailing_tbl"
				+ " WHERE company_id = ? AND is_template = 1 AND deleted = 0"
				+ " ORDER BY shortname";

		return select(logger, sql, new ComMailingToMailingBaseRowMapper(), companyID);
	}

	@Override
	public List<MailingBase> getMailingTemplatesWithPreview(@VelocityCheck final int companyId) {
		final StringBuilder querySb = new StringBuilder();
		querySb.append("SELECT m.mailing_id, m.shortname, COALESCE(cmp.component_id, 0) AS preview_component, m.description, m.creation_date")
				.append(" FROM mailing_tbl m")
				.append(" LEFT JOIN component_tbl cmp ON m.mailing_id = cmp.mailing_id AND cmp.comptype = ").append(MailingComponent.TYPE_THUMBNAIL_IMAGE)
				.append(" WHERE m.company_id = ? AND m.is_template = 1 AND m.deleted = 0 ORDER BY m.shortname");
		return select(logger, querySb.toString(), new MailingTemplatesWithPreviewRowMapper(), companyId);
	}

	@Override
	public MailingBase getMailingForTemplateID(final int templateID, @VelocityCheck final int companyID) {
		final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, dynamic_template, plan_date"
				+ " FROM mailing_tbl"
				+ " WHERE mailing_id = ? AND company_id = ?"
				+ " ORDER BY shortname";

		final List<MailingBase> result = select(logger, sql, new ComMailingToMailingBaseRowMapper(), templateID, companyID);
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<MailingBase> getMailingsByStatusE(@VelocityCheck final int companyID) {
		final List<Map<String, Object>> result = select(logger, "SELECT mail.mailing_id, mail.shortname FROM mailing_tbl mail, maildrop_status_tbl mds WHERE mail.mailing_id = mds.mailing_id AND mail.company_id = ? AND mail.deleted = 0 AND mail.mailing_type = 1 AND mds.status_field = 'E'", companyID);
		final List<MailingBase> returnList = new ArrayList<>();
		for (final Map<String, Object> row : result) {
			final Mailing item = new ComMailingImpl();
			item.setId(((Number) row.get("mailing_id")).intValue());
			item.setShortname((String) row.get("shortname"));
			returnList.add(item);
		}
	    return returnList;
	}

	@Override
	public List<Integer> getTemplateReferencingMailingIds(final Mailing mailTemplate) {
		if (!mailTemplate.isIsTemplate()) {
			// No template? Do nothing!
			return null;
		} else {
			final String query = "SELECT m.mailing_id FROM mailing_tbl m"
					+ " WHERE m.dynamic_template = 1 AND m.is_template = 0 AND m.mailtemplate_id = ? AND m.company_id = ? AND deleted = 0"
					+ " AND NOT EXISTS (SELECT 1 FROM maildrop_status_tbl mds WHERE mds.mailing_id = m.mailing_id AND mds.status_field IN ('w', 'W'))";
			return select(logger, query, new IntegerRowMapper(), mailTemplate.getId(), mailTemplate.getCompanyID());
		}
	}

	@Override
	public boolean checkMailingReferencesTemplate(final int templateID, @VelocityCheck final int companyID) {
		final int isTemplate = selectInt(logger, "SELECT is_template FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", templateID, companyID);
		return isTemplate == 1;
	}

	@Override
	public boolean exist(final int mailingID, @VelocityCheck final int companyID) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted = 0", companyID, mailingID) > 0;
	}

	@Override
	public boolean exist(final int mailingID, final int companyID, @VelocityCheck final boolean isTemplate) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted = 0 AND is_template = ?", companyID, mailingID, isTemplate ? 1 : 0) > 0;
	}

	@Override
	public List<Mailing> getMailings(final int companyId, @VelocityCheck final boolean isTemplate) {
		final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type,"
				+ " campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id,"
				+ " clickaction_id, statmail_recp, dynamic_template, plan_date"
				+ " FROM mailing_tbl"
				+ " WHERE company_id = ? AND is_template = ? AND deleted = 0";
		return select(logger, sql, new ComMailingToMailingRowMapper(), companyId, isTemplate ? 1: 0);
	}

	@Override
	public int getMailingOpenAction(final int mailingID, @VelocityCheck final int companyID) {
	    try {
		    return selectInt(logger, "SELECT openaction_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
	    } catch (final Exception e) {
	    	logger.error( "Error while getting mailing open action ID (mailingID: " + mailingID + ", companyID: " + companyID + ")", e);
			return 0;
		}
	}

	@Override
	public int getMailingClickAction(final int mailingID, @VelocityCheck final int companyID) {
	    try {
	        return selectInt(logger, "SELECT clickaction_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
	    } catch (final Exception e) {
	    	logger.error("Error while getting mailing click action ID (mailingID: " + mailingID + ", companyID: " + companyID + ")", e);
			return 0;
		}
	}

	/**
	 * returns the mailing-Parameter for the given mailing (only email, no sms or anything else).
	 * @param mailingID
	 * @return
	 */
	@Override
	public String getEmailParameter(final int mailingID) {
		try {
			return selectWithDefaultValue(logger, "SELECT param FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = 0", String.class, null, mailingID);
		} catch (final Exception e) {
			logger.error( "getEmaiLParameter() failed for mailing " + mailingID, e);
			return null;
		}
	}

	@Override
	public List<LightweightMailing> getLightweightMailings(final int companyID) {
		return select(logger, "SELECT company_id, mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0", new LightweightMailingRowMapper(), companyID);
	}

	@Override
	public List<LightweightMailing> getLightweightIntervalMailings(final int companyID) {
		return select(logger, "SELECT company_id, mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0 AND mailing_type = ? ORDER BY shortname", new LightweightMailingRowMapper(), companyID, Mailing.TYPE_INTERVAL);
	}

	/**
	 * returns the mailing-type for the given mailing.
	 */
	@Override
	public int getMailingType(final int mailingID) {
	    try {
	    	return selectIntWithDefaultValue(logger, "SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", -1, mailingID);
	    } catch (final Exception e) {
	        logger.error("Error getting mailingType for mailing " + mailingID, e);
	        return -1;
	    }
	}

	@Override
	public Date getMailingPlanDate(final int mailingId, @VelocityCheck final int companyId) {
		final String sqlGetPlanDate = "SELECT plan_date FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?";
		return selectObjectDefaultNull(logger, sqlGetPlanDate, (rs, i) -> rs.getTimestamp("plan_date"), companyId, mailingId);
	}

	@Override
	public List<Integer> getSampleMailingIDs() {
		return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = 1 AND (LOWER(shortname) LIKE '%sample%' OR LOWER(shortname) LIKE '%example%' OR LOWER(shortname) LIKE '%muster%' OR LOWER(shortname) LIKE '%beispiel%') AND deleted = 0", new IntegerRowMapper());
	}

	@Override
	public List<LightweightMailing> listMailingsByType(final int mailingType, final @VelocityCheck int companyID) {
		return select(logger, "SELECT company_id, mailing_id, shortname, description FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0 AND mailing_type = ? ", new LightweightMailingRowMapper(), companyID, mailingType);
	}

	@Override
	public String getMailingName(final int mailingId, @VelocityCheck final int companyId) {
		final String sqlGetName = "SELECT shortname FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
		return selectObjectDefaultNull(logger, sqlGetName, (rs, index) -> rs.getString("shortname"), mailingId, companyId);
	}

	@Override
	public Map<Integer, String> getMailingNames(final Collection<Integer> mailingIds, @VelocityCheck final int companyId) {
		if (CollectionUtils.isEmpty(mailingIds) || companyId <= 0) {
			return Collections.emptyMap();
		}

		final String sqlGetNames = "SELECT mailing_id, shortname FROM mailing_tbl " +
				"WHERE company_id = ? AND mailing_id IN (" + StringUtils.join(mailingIds, ',') + ")";

		final Map<Integer, String> namesMap = new HashMap<>();
		query(logger, sqlGetNames, rs -> namesMap.put(rs.getInt("mailing_id"), rs.getString("shortname")), companyId);
		return namesMap;
	}

	@Override
	public List<Map<String, Object>> getMailingsForActionOperationGetArchiveList(final int companyID, final int campaignID) {
		final String sqlQuery = "SELECT m.mailing_id, shortname FROM mailing_tbl m, maildrop_status_tbl mds WHERE status_field = 'W' AND deleted = 0 AND is_template = 0 AND m.company_id = ? AND campaign_id = ? AND archived = 1 AND m.mailing_id = mds.mailing_id ORDER BY senddate DESC, mailing_id DESC" ;
		return select(logger, sqlQuery, companyID, campaignID);
	}

	@Override
	public int getAnyMailingIdForCompany(final int companyID) {
		if (isOracleDB()){
		    return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND rownum=1", Integer.class, companyID);
	    } else {
	    	return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = ? LIMIT 1", Integer.class, companyID);
	    }
	}

	@Override
	public int getMailinglistId(final int mailingId, @VelocityCheck final int companyId) {
		return selectInt(logger, "SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", mailingId, companyId);
	}

	@Override
	public String getTargetExpression(final int mailingId, @VelocityCheck final int companyId) {
		return getTargetExpression(mailingId, companyId, false);
	}

	@Override
	public String getTargetExpression(final int mailingId, @VelocityCheck final int companyId, final boolean appendListSplit) {
		final String sqlGetTargets = "SELECT target_expression, split_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
		return selectObjectDefaultNull(logger, sqlGetTargets, new TargetExpressionMapper(appendListSplit), mailingId, companyId);
	}

	private boolean validateTargetExpression(final String expression) throws DatabaseInformationException {
		if (StringUtils.isNotEmpty(expression)) {
			final int maxLength = databaseInformation.getColumnStringLength("mailing_tbl", "TARGET_EXPRESSION");
			if (expression.length() > maxLength) {
				logger.error(String.format("Target expression exceeds maximum length of %d", maxLength));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean setTargetExpression(final int mailingId, @VelocityCheck final int companyId, final String targetExpression) throws TooManyTargetGroupsInMailingException {
		try {
			if (validateTargetExpression(targetExpression)) {
				final String sqlSetExpression = "UPDATE mailing_tbl SET target_expression = ? WHERE company_id = ? AND mailing_id = ? AND deleted = 0";
				return update(logger, sqlSetExpression, targetExpression, companyId, mailingId) > 0;
			} else {
				throw new TooManyTargetGroupsInMailingException(mailingId);
			}
		} catch (final DatabaseInformationException e) {
			logger.error("Error occurred: " + e.getMessage());
		}
		return false;
	}

	@Override
	public List<DynamicTag> getDynamicTags(final int mailingId, @VelocityCheck final int companyId) {
		return getDynamicTags(mailingId, companyId, false);
	}

	private List<DynamicTag> getDynamicTags(final int mailingId, @VelocityCheck final int companyId, final boolean includeDeletedDynTags) {
		final String sqlGetTags = "SELECT company_id, mailing_id, dyn_name_id, dyn_name, dyn_group, interest_group, no_link_extension " +
				"FROM dyn_name_tbl " +
				"WHERE company_id = ? AND mailing_id = ? AND (deleted = 0 OR 1 = ?) " +
				"ORDER BY dyn_group, dyn_name ASC";

		final String sqlGetContents = "SELECT c.company_id, c.mailing_id, c.dyn_content_id, c.dyn_name_id, c.target_id, c.dyn_order, c.dyn_content " +
				"FROM dyn_content_tbl c " +
				"JOIN dyn_name_tbl n ON n.dyn_name_id = c.dyn_name_id AND n.company_id = ? AND n.mailing_id = ? " +
				"WHERE c.company_id = ? AND c.mailing_id = ? AND (n.deleted = 0 OR 1 = ?) " +
				"ORDER BY c.dyn_order, c.dyn_content_id ASC";

		final List<DynamicTag> tags = select(logger, sqlGetTags, new DynamicTagRowMapper(), companyId, mailingId, includeDeletedDynTags ? 1 : 0);

		if (tags.size() > 0) {
			final Map<Integer, Map<Integer, DynamicTagContent>> contentMapsMap = new HashMap<>();

			for (final DynamicTag tag : tags) {
				final Map<Integer, DynamicTagContent> contentMap = new LinkedHashMap<>();
				tag.setDynContent(contentMap);
				contentMapsMap.put(tag.getId(), contentMap);
			}

			// Retrieve contents for all the tags.
			final List<DynamicTagContent> contents = select(logger, sqlGetContents, new DynamicTagContentRowMapper(), companyId, mailingId, companyId, mailingId, includeDeletedDynTags ? 1 : 0);

			for (final DynamicTagContent dynamicTagContent : contents) {
				contentMapsMap.get(dynamicTagContent.getDynNameID()).put(dynamicTagContent.getDynOrder(), dynamicTagContent);
			}
		}

		return tags;
	}

	@Required
	public final void setMaildropStatusDao(final MaildropStatusDao dao) {
		this.maildropStatusDao = dao;
	}

	private static class TargetExpressionMapper implements RowMapper<String> {
		private final boolean appendListSplit;

		public TargetExpressionMapper(final boolean appendListSplit) {
			this.appendListSplit = appendListSplit;
		}

		@Override
		public String mapRow(final ResultSet rs, final int i) throws SQLException {
			final String expression = rs.getString("target_expression");
			final int splitId = appendListSplit ? rs.getInt("split_id") : 0;

			if (StringUtils.isNotBlank(expression)) {
				if (splitId > 0) {
					return "(" + expression + ") AND " + splitId;
				} else {
					return expression;
				}
			} else {
				if (splitId > 0) {
					return Integer.toString(splitId);
				} else {
					return null;
				}
			}
		}
	}

	@Override
	public void migrateOldLinkExtension(final int companyID) {
		final List<ComMailing> mailingsWithOldExtension = select(logger, "SELECT * FROM mailing_tbl WHERE trackable_link_extension IS NOT NULL AND company_id = ? ORDER BY company_id, mailing_id", new ComMailingRowMapper(), companyID);
		for (final ComMailing mailingWithOldExtension : mailingsWithOldExtension) {
			try {
				final String oldLinkExtension = select(logger, "SELECT trackable_link_extension FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", String.class, companyID, mailingWithOldExtension.getId());

				final List<LinkProperty> newLinkExtensions = LinkServiceImpl.getLinkExtensions(oldLinkExtension);
				for (final LinkProperty newLinkExtension : newLinkExtensions) {
					update(logger, "INSERT INTO rdir_url_param_tbl (url_id, param_type, param_key, param_value) (SELECT url_id, ?, ?, ? FROM rdir_url_tbl WHERE company_id = ? and mailing_id = ?)",
						newLinkExtension.getPropertyType().name(), newLinkExtension.getPropertyName(), newLinkExtension.getPropertyValue(), companyID, mailingWithOldExtension.getId());
				}
				update(logger, "UPDATE mailing_tbl SET trackable_link_extension = NULL WHERE company_id = ? AND mailing_id = ?", companyID, mailingWithOldExtension.getId());
			} catch (final Exception e) {
				logger.error("Cannot migrate old link extension for mailing id: " + companyID + " / " + mailingWithOldExtension.getId());
			}
		}
	}

	@Override
	public boolean isAdvertisingContentType(@VelocityCheck final int companyId, final int mailingId) {
		if(companyId <= 0 || mailingId <= 0) {
			return false;
		}

		final String fieldClause = String.format("COALESCE(content_type, '%s') content_type_value", ADVERTISING_TYPE);
		final String query = "SELECT " + fieldClause + " FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?";
		final String contentType = selectWithDefaultValue(logger, query, String.class, "", companyId, mailingId);

		return StringUtils.isNotEmpty(contentType) && ADVERTISING_TYPE.equalsIgnoreCase(contentType);
	}

	@Override
	public boolean isTextVersionRequired(@VelocityCheck final int companyId, final int mailingId) {
		final String sqlGetIsTextVersionRequired = "SELECT is_text_version_required FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
		return BooleanUtils.toBoolean(selectInt(logger, sqlGetIsTextVersionRequired, mailingId, companyId));
	}

	private boolean isGridTemplateSupported() {
		if (isGridTemplateSupported == null) {
			isGridTemplateSupported = DbUtilities.checkIfTableExists(getDataSource(), "grid_template_tbl");
		}

		return isGridTemplateSupported;
	}

    protected class MailingMapRowMapper implements RowMapper<Map<String, Object>> {
		private final Map<Integer, String> targetNameCache = new TreeMap<>();

		@Override
		public Map<String, Object> mapRow(final ResultSet resultSet, final int row) throws SQLException {
			final Map<String, Object> newBean = new HashMap<>();

			final int companyID = resultSet.getInt("company_id");
			final int mailingID = resultSet.getInt("mailing_id");

			newBean.put("mailingid", mailingID);
			newBean.put("workstatus", resultSet.getString("work_status"));
			newBean.put("shortname", resultSet.getString("shortname"));
			newBean.put("description", resultSet.getString("description"));
			newBean.put("mailinglist", resultSet.getString("mailinglist"));
			newBean.put("senddate", resultSet.getTimestamp("senddate"));
			newBean.put("creationdate", resultSet.getTimestamp("creationdate"));
			newBean.put("changedate", resultSet.getTimestamp("changedate"));
			newBean.put("isgrid", resultSet.getInt("isgrid") > 0);
			newBean.put("usedInCM", resultSet.getInt("usedincm") > 0);
			newBean.put("preview_component", resultSet.getInt("preview_component"));

			if (DbUtilities.resultsetHasColumn(resultSet, "templatename")) {
				newBean.put("templateName", resultSet.getString("templatename"));
			}

			if (DbUtilities.resultsetHasColumn(resultSet, "subject")) {
				newBean.put("subject", resultSet.getString("subject"));
			}

			if (DbUtilities.resultsetHasColumn(resultSet, "recipientscount")) {
				newBean.put("recipientsCount", resultSet.getInt("recipientscount"));
			}

			if (hasActions(mailingID, companyID)) {
				newBean.put("hasActions", true);
			}

			if (DbUtilities.resultsetHasColumn(resultSet, "target_expression")) {
				newBean.put("targetgroups", getTargetGroupsForTargetExpression(resultSet.getString("target_expression"), targetNameCache));
			}

			return newBean;
		}
	}

	@Required
	public void setDynamicTagContentDao(final DynamicTagContentDao dynamicTagContentDao) {
		this.dynamicTagContentDao = dynamicTagContentDao;
	}

	@Required
	public void setDynamicTagDao(final DynamicTagDao dynamicTagDao) {
		this.dynamicTagDao = dynamicTagDao;
	}

	public final void setPredeliveryDao(final ComPredeliveryDao dao) {
		this.predeliveryDao = dao;
	}
}
