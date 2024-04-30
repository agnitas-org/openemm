/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import static com.agnitas.emm.core.mailing.dao.ComMailingParameterDao.ReservedMailingParam.ERROR;
import static com.agnitas.emm.core.mailing.dao.ComMailingParameterDao.ReservedMailingParam.NEXT_START;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.DynamicTagContent;
import org.agnitas.beans.MailingBase;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.beans.MailingSendStatus;
import org.agnitas.beans.MediaTypeStatus;
import org.agnitas.beans.impl.DynamicTagContentImpl;
import org.agnitas.beans.impl.MailingBaseImpl;
import org.agnitas.beans.impl.MailingSendStatusImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.DateRowMapper;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.LightweightMailingRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.FulltextSearchInvalidQueryException;
import org.agnitas.util.FulltextSearchQueryException;
import org.agnitas.util.ParameterParser;
import org.agnitas.util.SafeString;
import org.agnitas.web.MailingAdditionalColumn;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.incrementer.MySQLMaxValueIncrementer;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComRdirMailingData;
import com.agnitas.beans.ComTrackableLink;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.ComRdirMailingDataImpl;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.ComUndoDynContentDao;
import com.agnitas.dao.ComUndoMailingComponentDao;
import com.agnitas.dao.ComUndoMailingDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.commons.database.DatabaseInformation;
import com.agnitas.emm.core.commons.database.DatabaseInformationException;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mailing.MailingDataException;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.MailingPropertyCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.MailingStatusCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SendDateCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SentAfterCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SentBeforeCondition;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;

/**
 * TODO: Check concatenated SQl statements for sql-injection possibilities,
 * replace where possible by real ?-parameters, also for better db performance
 */
public class ComMailingDaoImpl extends PaginatedBaseDaoImpl implements ComMailingDao {

    private static final Logger logger = LogManager.getLogger(ComMailingDaoImpl.class);
    
    private static Boolean FULLTEXTSEARCHSUPPORTED_CACHE = null;
    private static Boolean BASICFULLTEXTSEARCHSUPPORTED_CACHE = null;

    protected static final Set<String> SEPARATE_MAILING_FIELDS = Set.of(
        "company_id",
        "is_grid",
        "mailing_id",
        "shortname",
        "content_type",
        "deleted",
        "description",
        "mailtemplate_id",
        "mailinglist_id",
        "mailing_type",
        "campaign_id",
        "archived",
        "target_expression",
        "split_id",
        "is_template",
        "needs_target",
        "test_lock",
        "statmail_recp",
        "statmail_onerroronly",
        "clearance_threshold",
        "clearance_email",
        "creation_date",
        "dynamic_template",
        "openaction_id",
        "clickaction_id",
        "plan_date",
        "priority",
        "is_prioritization_allowed"
    );

    private static final ComMailingRowMapper MAILING_ROW_MAPPER = new ComMailingRowMapper();

    private static final String ADVERTISING_TYPE = "advertising";
    private static final String DATE_ORDER_KEY = "date";

    private static final PartialMailingBaseRowMapper PARTIAL_MAILING_BASE_ROW_MAPPER = new PartialMailingBaseRowMapper();

    private Boolean isGridTemplateSupported;

    public static String getSuccessTableName(final int companyId) {
        return "success_" + companyId + "_tbl";
    }

    public static String getBindingTableName(final int companyId) {
        return "customer_" + companyId + "_binding_tbl";
    }

    public static String getMailtrackTableName(final int companyId) {
        return "mailtrack_" + companyId + "_tbl";
    }

    protected ComUndoMailingDao undoMailingDao;
    protected ComMailingComponentDao mailingComponentDao;

    /** DAO accessing target groups. */
    protected ComTargetDao targetDao;
    protected ComUndoMailingComponentDao undoMailingComponentDao;
    protected ComUndoDynContentDao undoDynContentDao;
    protected TrackableLinkDao trackableLinkDao;
    protected DatabaseInformation databaseInformation;
    protected ConfigService configService;
    protected MaildropStatusDao maildropStatusDao;
    protected DynamicTagDao dynamicTagDao;
    protected MediatypesDao mediatypesDao;
    private FulltextSearchQueryGenerator fulltextSearchQueryGenerator;

    // ---------------------------------------------------------------------------------------------------------------------------------------
    // dependency injection code

    /**
     * Injection-setter for MediatypesDao.
     *
     * @param mediatypesDao - DAO
     */
    public void setMediatypesDao(final MediatypesDao mediatypesDao) {
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

    public void setTrackableLinkDao(final TrackableLinkDao trackableLinkDao) {
        this.trackableLinkDao = trackableLinkDao;
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
     * @param componentDao DAO
     */
    public void setMailingComponentDao(final ComMailingComponentDao componentDao) {
        this.mailingComponentDao = componentDao;
    }

    @Required
    public void setFulltextSearchQueryGenerator(final FulltextSearchQueryGenerator fulltextSearchQueryGenerator) {
        this.fulltextSearchQueryGenerator = fulltextSearchQueryGenerator;
    }

    @Required
    public void setDynamicTagDao(final DynamicTagDao dynamicTagDao) {
        this.dynamicTagDao = dynamicTagDao;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------
    // business logic

    /**
     * This method returns a sorted list with Mailings. The size of
     * the list (=amount of mailings) is given by the count parameter. Returned
     * are the last ones, which means the mailings with the highest mailing-id.
     * If a mailingStatus is given (can be 'W', 'T' or 'A'), a filtering is done
     * to only this mailings. So if you want a World-Mailing, use
     * mailingStatus="W". If you dont want a filter, use "" or null.
     * <p>
     * ATTENTION! NOT ALL FIELDS WILL BE FILLED!!!! JUST THE FIELDS NEEDED FOR
     * SOME SPECIAL CASES ARE USED. IF YOU NEED MORE YOU HAVE TO ADD THE NEEDED
     * FIELDS. TO DO THIS FIND THE MAPPING AND ADD YOUR STATEMENT.
     */
    @Override
    public List<Mailing> getMailings(int companyID, int adminId, int count, String mailingStatus, boolean takeMailsForPeriod) {
        final Vector<Object> parameters = new Vector<>();

        String sql;
        // create SQL-Statement for filtered query.
        if (mailingStatus == null ||
                mailingStatus.equals(MaildropStatus.WORLD.getCodeString()) ||
                mailingStatus.equals(MaildropStatus.TEST.getCodeString()) ||
                mailingStatus.equals(MaildropStatus.ADMIN.getCodeString())) {
            sql = "SELECT " + getMailingSqlSelectFields("a.") + " FROM mailing_tbl a, maildrop_status_tbl b WHERE a.company_id = ?";
            parameters.add(companyID);

            if (mailingStatus != null) {
                sql += " AND b.status_field = ?";
                parameters.add(mailingStatus);
            }

            if (takeMailsForPeriod) {
                final int numDays = configService.getIntegerValue(ConfigValue.ExpireSuccess, companyID);
                if (numDays > 0) {
                    if (isOracleDB()) {
                        sql += " AND b.senddate > (SYSDATE " + (-numDays) + " )";
                    } else {
                        sql += " AND b.senddate > DATE_ADD(now(), interval " + (-numDays) + " day) ";
                    }
                }
            }

            if (adminId > 0 && isDisabledMailingListsSupported()) {
                sql += " AND a.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
                parameters.add(adminId);
            }

            sql += " AND a.mailing_id = b.mailing_id ORDER BY a.mailing_id DESC";
        } else {
            sql = "SELECT " + getMailingSqlSelectFields("") + " FROM mailing_tbl WHERE company_id = ? ";
            parameters.add(companyID);

            if (adminId > 0 && isDisabledMailingListsSupported()) {
                sql += " AND mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
                parameters.add(adminId);
            }

            sql += " ORDER BY mailing_id DESC";
        }

        // Check, if we have to return all values.
        // If count <= 0 then all mailings are returned.
        if (count > 0) {
            if (isOracleDB()) {
            	// Using "SELECT * ...", because flexible sql
                sql = "SELECT * from (" + sql + ") WHERE rownum <= ?";
            } else {
                sql += " LIMIT ?";
            }
            parameters.add(count);
        }

        try {
            return select(logger, sql, getRowMapper(), parameters.toArray());
        } catch (final Exception e) {
            return new LinkedList<>();
        }
    }

    @Override
	public final List<Mailing> listMailings(final int companyId, final boolean template, final ListMailingFilter filter) {
    	if (filter == null) {
    		return getMailings(companyId, template);
    	}
    	
    	final List<Object> paramsList = new ArrayList<>();
    	
    	
    	final StringBuffer sql = new StringBuffer("SELECT * FROM mailing_tbl m WHERE m.company_id = ? AND m.is_template = ? AND m.deleted=0");
    	paramsList.add(companyId);
    	paramsList.add(template ? 1 : 0);
    	
    	for (final MailingPropertyCondition c : filter.listMailingPropertyConditions()) {
    		if (c instanceof MailingStatusCondition) {
    			final MailingStatusCondition cond = (MailingStatusCondition) c;
    			
    			if(!((MailingStatusCondition) c).getStatusList().isEmpty()) {
	    			sql.append(" AND m.work_status IN (");

	    			boolean first = true;
	    			for(final MailingStatus status : cond.getStatusList()) {
	    				sql.append(first ? "?" : ",?");
	    				first = false;
	    				
	    				paramsList.add(status.getDbKey());
	    			}
	    			
	    			sql.append(")");
    			}
    		} else {
				final String msg = String.format("Unhandled mailing property condition of type %s", c.getClass().getCanonicalName());
				logger.fatal(msg);
				throw new RuntimeException(msg);
    		}
    	}

    	if (filter.containsSendDateConditions()) {
    		sql.append(" AND EXISTS (SELECT 1 FROM maildrop_status_tbl mds WHERE mds.mailing_id = m.mailing_id AND mds.status_field NOT IN ('A', 'T')");
    		
    		for(final SendDateCondition c : filter.listSendDateConditions()) {
    			final Date timestamp = Date.from(c.getTimestamp().toInstant());
    			final boolean inclusive = c.isInclusive();
    			
    			if(c instanceof SentBeforeCondition) {
    				sql.append(" AND ").append(inclusive ? "mds.senddate <= ?" : "mds.senddate < ?");
    				paramsList.add(timestamp);
    			} else if(c instanceof SentAfterCondition) {
    				sql.append(" AND ").append(inclusive ? "mds.senddate >= ?" : "mds.senddate > ?");
    				paramsList.add(timestamp);
    			} else {
    				final String msg = String.format("Unhandled send date condition of type %s", c.getClass().getCanonicalName());
    				logger.fatal(msg);
    				throw new RuntimeException(msg);
    			}
    		}
    		
    		sql.append(")");
    	}

        return select(logger, sql.toString(), new ComMailingToMailingRowMapper(), paramsList.toArray());
	}

	/**
     * This method returns a sorted list with Lightweight Mailings. Only
     * CompanyID, MailingID, Description and Shortname are filled into the
     * object.
     * <p>
     * ATTENTION! NOT ALL FIELDS WILL BE FILLED!!!! JUST THE FIELDS NEEDED FOR
     * SOME SPECIAL CASES ARE USED. IF YOU NEED MORE YOU HAVE TO ADD THE NEEDED
     * FIELDS. TO DO THIS FIND THE MAPPING AND ADD YOUR STATEMENT.
     *
     * @param admin current user.
     * @return List<LightweightMailing>
     */
    @Override
    public List<LightweightMailing> getMailingNames(Admin admin) {
        ArrayList<Object> parameters = new ArrayList<>();
        String sql = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl WHERE company_id = ? AND deleted = 0";
        parameters.add(admin.getCompanyID());

        sql += getAdditionalSqlRestrictions(admin, parameters);
        sql += " ORDER BY shortname ASC";

        return select(logger, sql, LightweightMailingRowMapper.INSTANCE, parameters.toArray());
    }

    private String getDisabledMailinglistRestriction(int adminId, List<Object> queryParams, String tableAlias) {
        if (adminId > 0 && isDisabledMailingListsSupported()) {
            queryParams.add(adminId);
            return " AND " + (tableAlias == null ? "" : tableAlias + ".") + "mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
        } else {
        	return "";
        }
    }

    protected String getTargetRestrictions(Admin admin, List<Object> queryParams) {
        return getTargetRestrictions(admin, queryParams, null);
    }
    
    protected String getTargetRestrictions(Admin admin, List<Object> queryParams, String tableAlias) {
        return StringUtils.EMPTY;
    }
    
    protected String getAdditionalSqlRestrictions(Admin admin, List<Object> queryParams) {
        return getDisabledMailinglistRestriction(admin.getAdminID(), queryParams, null);
    }
    
    protected String getAdditionalSqlRestrictions(Admin admin, List<Object> queryParams, String tableAlias) {
        return getDisabledMailinglistRestriction(admin.getAdminID(), queryParams, tableAlias);
    }

    @Override
    public List<LightweightMailing> getAllMailingsSorted(Admin admin, String sortField, String sortDirection) {
        String unsentSort = StringUtils.defaultString(sortField);
        String unsentDirection = StringUtils.defaultString(sortDirection);
        if (unsentSort.equalsIgnoreCase("date")) {
            unsentSort = "creation_date";
        }

        if (unsentSort.isEmpty()) {
            unsentSort = "m.mailing_id";
            if (unsentDirection.isEmpty()) {
                unsentDirection = "DESC";
            }
        }

        List<LightweightMailing> allMailings = getUnsentMailingsSorted(admin, unsentSort, unsentDirection);

        String sentSort = StringUtils.defaultString(sortField);
        String sentDirection = StringUtils.defaultString(sortDirection);
        if (sentSort.equalsIgnoreCase("date")) {
            sentSort = "senddate";
        }
        if (sentSort.isEmpty()) {
            sentSort = "m.mailing_id";
            if (sentDirection.isEmpty()) {
                sentDirection = "DESC";
            }
        }

        allMailings.addAll(getSentMailingsSorted(admin, sentSort, sentDirection));
        return allMailings;
    }

    /**
     * Method for WorkflowManager. Returns list of all mailings: unsent mailings ordered by creation date; sent
     * mailings ordered by send date
     *
     * @param admin current user
     * @return list of all mailings
     */
    @Override
    public List<LightweightMailing> getMailingsDateSorted(Admin admin) {
        List<LightweightMailing> allMailings = getUnsentMailingsSorted(admin, "creation_date", "desc");
        allMailings.addAll(getSentMailingsSorted(admin, "senddate", "desc"));
        return allMailings;
    }

    private List<LightweightMailing> getUnsentMailingsSorted(Admin admin, String sortField, String sortDirection) {
        String unsentSql = "SELECT m.company_id, m.mailing_id, m.shortname, m.description, m.mailing_type, work_status, content_type " +
		        "FROM mailing_tbl m WHERE m.company_id = ? AND m.is_template = 0 AND m.deleted = 0 AND " +
		        "NOT EXISTS (SELECT 1 FROM maildrop_status_tbl " +
		        "WHERE status_field = ? AND mailing_id = m.mailing_id)";
        List<Object> unsentParams = new ArrayList<>();
        unsentParams.add(admin.getCompanyID());
        unsentParams.add(MaildropStatus.WORLD.getCodeString());

        unsentSql += getAdditionalSqlRestrictions(admin, unsentParams);

        return select(logger, addSortingToQuery(unsentSql, sortField, sortDirection),
        		LightweightMailingRowMapper.INSTANCE, unsentParams.toArray());
    }

    private List<LightweightMailing> getSentMailingsSorted(Admin admin, String sortField, String sortDirection) {
        String sentSql = "SELECT m.company_id, m.mailing_id, m.shortname, m.description, m.mailing_type, work_status, content_type " +
		        "FROM maildrop_status_tbl md LEFT JOIN mailing_tbl m " +
		        "ON (md.mailing_id = m.mailing_id) WHERE md.status_field = ? AND md.company_id = ? AND m.is_template = 0 AND m.deleted = 0 ";
        List<Object> sentParams = new ArrayList<>();
        sentParams.add(MaildropStatus.WORLD.getCodeString());
        sentParams.add(admin.getCompanyID());

        sentSql += getAdditionalSqlRestrictions(admin, sentParams);

        return select(logger, addSortingToQuery(sentSql, sortField, sortDirection),
        		LightweightMailingRowMapper.INSTANCE, sentParams.toArray());
    }

    @Override
    public List<Map<String, Object>> getMailings(int companyId, String commaSeparatedMailingIds) {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, statmail_onerroronly, clearance_threshold, clearance_email, dynamic_template, plan_date, work_status FROM mailing_tbl ")
                .append("WHERE company_id = ? ")
                .append("  AND mailing_id IN (").append(commaSeparatedMailingIds).append(") ")
                .append("ORDER BY mailing_id DESC");
        return select(logger, builder.toString(), companyId);
    }

    @Override
    public List<Map<String, Object>> getMailingsNamesByStatus(Admin admin, final List<MailingType> mailingTypes, String workStatus, String mailingStatus, boolean takeMailsForPeriod, String sort, String order) {
        int companyID = admin.getCompanyID();
        final Vector<Object> parameters = new Vector<>();
        parameters.add(companyID);

        final StringBuilder queryBuilder = new StringBuilder()
	        .append("SELECT a.company_id, a.mailing_id, a.shortname, a.description, a.work_status, b.senddate, a.change_date")
			.append(", workflow_id AS usedInWorkflows ")
	        .append(", (CASE WHEN (a.work_status = '" + MailingStatus.SENT.getDbKey() + "' OR a.work_status = '" + MailingStatus.SCHEDULED.getDbKey() + "' OR a.work_status = '" + MailingStatus.NORECIPIENTS.getDbKey() + "') THEN 2 ELSE 1 END) AS sent_sort_status ")
	        .append(", (CASE WHEN (a.work_status = '" + MailingStatus.SENT.getDbKey() + "' OR a.work_status = '" + MailingStatus.SCHEDULED.getDbKey() + "' OR a.work_status = '" + MailingStatus.NORECIPIENTS.getDbKey() + "') THEN b.senddate ELSE a.creation_date END) AS sent_sort_date ")
	        .append(", (CASE WHEN (a.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "') THEN 2 ELSE 1 END) AS active_sort_status ")
	        .append(", (CASE WHEN (a.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "') THEN b.senddate ELSE a.creation_date END) AS active_sort_date ")
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

        queryBuilder.append("WHERE a.company_id = ? AND a.is_template = 0 AND a.mailing_type IN (");
        queryBuilder.append(createMailingTypeCodeListString(mailingTypes));
        queryBuilder.append(") AND deleted = 0");

        if (mailingStatus != null) {
            queryBuilder.append(" AND md.status_field = ?");
            parameters.add(mailingStatus);
        }

        if (takeMailsForPeriod) {
            final int numDays = configService.getIntegerValue(ConfigValue.ExpireSuccess, companyID);
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
                queryBuilder.append(" AND (a.work_status != '" + MailingStatus.SENT.getDbKey() + "' AND a.work_status != '" + MailingStatus.SCHEDULED.getDbKey() + "' AND a.work_status != '" + MailingStatus.NORECIPIENTS.getDbKey() + "') ");
            } else if ("sent_scheduled".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status = '" + MailingStatus.SENT.getDbKey() + "' OR a.work_status = '" + MailingStatus.SCHEDULED.getDbKey() + "' OR a.work_status = '" + MailingStatus.NORECIPIENTS.getDbKey() + "') ");
            } else if ("active".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "') ");
            } else if ("inactive".equals(workStatus)) {
                queryBuilder.append(" AND (a.work_status != '" + MailingStatus.ACTIVE.getDbKey() + "') ");
            }
        }

        queryBuilder.append(getAdditionalSqlRestrictions(admin, parameters, "a"));

        if (StringUtils.isNotEmpty(sort)) {
            queryBuilder.append(" ORDER BY ").append(sort);
        }

        queryBuilder.append(" ").append(StringUtils.defaultIfEmpty(order, "ASC"));

        return select(logger, queryBuilder.toString(), parameters.toArray());
    }

	private String createMailingTypeCodeListString(final List<MailingType> mailingTypes) {
		StringBuilder listStringBuilder = new StringBuilder();
		for (int i = 0; i < mailingTypes.size(); i++) {
        	if (i > 0) {
        		listStringBuilder.append(", ");
        	}
        	listStringBuilder.append(Integer.toString(mailingTypes.get(i).getCode()));
        }
		return listStringBuilder.toString();
	}

    @Override
    public List<Integer> findTargetDependentMailings(int targetGroupId, int companyId) {
        String query = "SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND (" + DbUtilities.createTargetExpressionRestriction(isOracleDB()) + ")";
        return select(logger, query, IntegerRowMapper.INSTANCE, companyId, targetGroupId);
    }

    @Override
    public List<Integer> filterNotSentMailings(List<Integer> mailings) {
        String mailingIdsInClause = makeBulkInClauseForInteger("mailing_id", mailings);

        String query = "SELECT mailing_id FROM mailing_tbl WHERE " + mailingIdsInClause + " AND work_status != ?";
        return select(logger, query, IntegerRowMapper.INSTANCE, MailingStatus.SENT.getDbKey());
    }

    /**
     * This method returns all Mailings of the given CompanyID in a LinkedList
     * <p>
     * Attention: use only for Demo Account Service methods
     */
    @Override
    public List<Integer> getAllMailings(final int companyID) {
        return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = ?", IntegerRowMapper.INSTANCE, companyID);
    }

    @Override
    public Mailing getMailing(final int mailingID, final int companyID) {
        return getMailing(mailingID, companyID, false);
    }

    @Override
    public Mailing getMailingWithDeletedDynTags(final int mailingID, final int companyID) {
        return getMailing(mailingID, companyID, true);
    }

	private Mailing getMailing(final int mailingID, final int companyID, final boolean includeDeletedDynTags) {
		if (companyID <= 0 || mailingID <= 0) {
			throw new MailingNotExistException(companyID, mailingID);
		} else {
			try {
				String sql = "SELECT " + getMailingSqlSelectFields("a.") + ", a.send_date AS senddate"
					+ " FROM mailing_tbl a"
					+ " WHERE a.company_id = ? AND a.mailing_id = ? AND a.deleted <= 0 ORDER BY a.send_date";

				if (isOracleDB()) {
					sql = String.format("SELECT * FROM (%s) WHERE rownum = 1", sql);
				} else {
					sql += " LIMIT 1";
				}

				final Mailing mailing = selectObjectDefaultNull(logger, sql, getRowMapper(), companyID, mailingID);

				if (mailing == null) {
					throw new MailingNotExistException(companyID, mailingID);
				} else {
					mailing.setMediatypes(mediatypesDao.loadMediatypes(mailingID, companyID));

					final Map<String, DynamicTag> dynamicTagMap = new LinkedHashMap<>();
					for (final DynamicTag tag : dynamicTagDao.getDynamicTags(mailingID, companyID, includeDeletedDynTags)) {
						dynamicTagMap.put(tag.getDynName(), tag);
					}
					mailing.setDynTags(dynamicTagMap);

					final Map<String, MailingComponent> mailingComponentMap = new TreeMap<>();
					List<MailingComponent> mailingComponents = mailingComponentDao.getMailingComponents(mailingID, companyID);
					List<Integer> linkIds = mailingComponents.stream().map(MailingComponent::getUrlID).filter(linkID -> linkID != 0).collect(Collectors.toList());
					Map<Integer, String> trackableLinkUrls = trackableLinkDao.getTrackableLinkUrl(companyID, mailingID, linkIds);
					for (final MailingComponent mailingComponent : mailingComponents) {
						String url = trackableLinkUrls.get(mailingComponent.getUrlID());
						if (url != null) {
							mailingComponent.setLink(url);
						}
						mailingComponentMap.put(mailingComponent.getComponentName(), mailingComponent);
					}
					mailing.setComponents(mailingComponentMap);
					mailing.setTrackableLinks(getTrackableLinksMap(companyID, mailingID));

					final List<MaildropEntry> maildropEntryList = maildropStatusDao.getMaildropStatusEntriesForMailing(companyID, mailingID);
					mailing.setMaildropStatus(new HashSet<>(maildropEntryList));

					return mailing;
				}
			} catch (final MailingNotExistException e) {
				throw e;
			} catch (final Exception e) {
				logger.error("Error loading mailing ID " + mailingID, e);
				throw new MailingNotExistException(companyID, mailingID);
			}
		}
	}

    private void saveComponents(final int companyID, final int mailingID, final Map<String, MailingComponent> components, final boolean errorTolerant) throws Exception {
        if (MapUtils.isEmpty(components)) {
            return;
        }

        mailingComponentDao.setUnPresentComponentsForMailing(mailingID, new ArrayList<>(components.values()));

        for (final MailingComponent mailingComponent : components.values()) {
            try {
                mailingComponent.setCompanyID(companyID);
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
            return saveMailingWithoutTransaction(mailing, preserveTrackableLinks, true, false);
        } catch (final Exception e) {
            // Do nothing (formerly the transaction was rolled back)
            return 0;
        }
    }

    @Override
    public int saveMailing(final Mailing mailing, final boolean preserveTrackableLinks, final boolean errorTolerant) throws Exception {
        return saveMailing(mailing, preserveTrackableLinks, errorTolerant, false);
    }

    @Override
    public int saveMailing(final Mailing mailing, final boolean preserveTrackableLinks, final boolean errorTolerant, final boolean removeUnusedContent) throws Exception {
        return saveMailingWithoutTransaction(mailing, preserveTrackableLinks, errorTolerant, removeUnusedContent);
    }

    @DaoUpdateReturnValueCheck
    private int saveMailingWithoutTransaction(final Mailing mailing, final boolean preserveTrackableLinks, final boolean errorTolerant, final boolean removeUnusedContent) throws Exception {
        if (!validateTargetExpression(mailing.getTargetExpression())) {
            throw new TooManyTargetGroupsInMailingException(mailing.getId());
        }

        if (StringUtils.isBlank(mailing.getShortname())) {
            final MailingDataException mde = new MailingDataException("Invalid empty mailing shortname");
            logger.error("Invalid empty mailing shortname", mde);
            // Send an email to developers, because this is a critical problem (BAUR-545)
            javaMailService.sendExceptionMail(0, mde.getMessage(), mde);
            throw mde;
        }

        if (mailing.getMailinglistID() <= 0) {
            final MailingDataException mde = new MailingDataException("Invalid missing mailinglist");
            logger.error("Invalid missing mailinglist", mde);
            // Send an email to developers, because this is a critical problem (BAUR-545)
            javaMailService.sendExceptionMail(0, mde.getMessage(), mde);
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

            final String autoUrl = selectObjectDefaultNull(logger, "SELECT auto_url FROM mailinglist_tbl WHERE deleted = 0 AND mailinglist_id = ?", StringRowMapper.INSTANCE, mailing.getMailinglistID());

            performMailingSave(companyId, mailing, autoUrl);

            maildropStatusDao.saveMaildropEntries(companyId, mailing.getId(), mailing.getMaildropStatus());
            updateClearanceLastSendDate(mailing.getId());
            trackableLinkDao.batchSaveTrackableLinks(companyId, mailing.getId(), mailing.getTrackableLinks(), !preserveTrackableLinks);
            saveComponents(companyId, mailing.getId(), mailing.getComponents(), errorTolerant);
            mediatypesDao.saveMediatypes(companyId, mailing.getId(), mailing.getMediatypes());
            dynamicTagDao.saveDynamicTags(mailing, mailing.getDynTags(), removeUnusedContent);

            /*
             * if( mailing.isIsTemplate()){ updateMailingsWithDynamicTemplate(
             * (ComMailing)mailing); }
             */

        } catch (final Exception e) {
            logger.error("Error saving mailing " + mailing.getId(), e);

            throw e;
        }

        return mailing.getId();
    }

    private void updateClearanceLastSendDate(int mailingId) {
        String sendDate = isOracleDB() ? "TRUNC(mds.senddate)" : "DATE(mds.senddate)";
        String today = isOracleDB() ? "TRUNC(CURRENT_TIMESTAMP)" : "DATE(CURRENT_TIMESTAMP)";
        String query = "UPDATE rulebased_sent_tbl rbs SET rbs.lastsent = NULL WHERE mailing_id = ? AND EXISTS " +
                " (SELECT 1 FROM maildrop_status_tbl mds WHERE mds.mailing_id = ? AND mds.status_field = ?  AND " + sendDate + " = " + today + ")";

        update(logger, query, mailingId, mailingId, MaildropStatus.DATE_BASED.getCodeString());
    }

    @Override
    @DaoUpdateReturnValueCheck
    public boolean markAsDeleted(final int mailingID, final int companyID) {
        final int touchedLines = update(logger, "UPDATE mailing_tbl SET deleted = 1, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ? AND company_id = ? AND (deleted IS NULL OR deleted != 1)", mailingID, companyID);
        return touchedLines > 0;
    }

    @Override
    public List<Map<String, String>> loadAction(final int mailingID, final int companyID) {
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
            logger.error("Error loading actions for mailing " + mailingID, e);
        }

        return actions;
    }

    @Override
    public boolean updateStatus(Mailing mailing, MailingStatus mailingStatus) {
        if (mailing == null) {
            return false;
        }

        return updateStatus(mailing.getId(), mailingStatus, mailing.getSenddate());
    }

    /**
     * Keep the send date unchanged
     */
    @Override
    public boolean updateStatus(final int mailingID, final MailingStatus mailingStatus) {
        //status scheduled could only be overwritten by sending or canceled
        //status active could only be overwritten by disable
        if (isStatus(mailingID, "scheduled", "active")) {
            if (mailingStatus == MailingStatus.SENDING || mailingStatus == MailingStatus.CANCELED || mailingStatus == MailingStatus.DISABLE) {
                updateStatusInDB(mailingID, mailingStatus);
                return true;
            }

            return false;
        }

        //status sending could only be overwritten by "sent" ("norecipients" -> "sent" without recipients)
        if (isStatus(mailingID, "sending")) {
            if (mailingStatus == MailingStatus.SENT || mailingStatus == MailingStatus.NORECIPIENTS) {
                updateStatusInDB(mailingID, mailingStatus);
                return true;
            }

            return false;
        }

        //never overwrite status sent
        if (isStatus(mailingID, "sent", "norecipients")) {
            return false;
        }

        //never reset a mailing to status new
        if (mailingStatus == MailingStatus.NEW) {
            return false;
        }

        updateStatusInDB(mailingID, mailingStatus);
        return true;
    }

    @Override
    public boolean updateStatus(final int mailingID, final MailingStatus mailingStatus, Date sendDate) {
        //status scheduled could only be overwritten by sending or canceled
        //status active could only be overwritten by disable
        if (isStatus(mailingID, "scheduled", "active")) {
            if (mailingStatus == MailingStatus.SENDING || mailingStatus == MailingStatus.CANCELED || mailingStatus == MailingStatus.DISABLE) {
                updateStatusInDB(mailingID, mailingStatus, sendDate);
                return true;
            }

            return false;
        }

        //status sending could only be overwritten by "sent" ("norecipients" -> "sent" without recipients)
        if (isStatus(mailingID, "sending")) {
            if (mailingStatus == MailingStatus.SENT || mailingStatus == MailingStatus.NORECIPIENTS) {
                updateStatusInDB(mailingID, mailingStatus);
                return true;
            }

            return false;
        }

        //never overwrite status sent
        if (isStatus(mailingID, "sent", "norecipients")) {
            return false;
        }

        //never reset a mailing to status new
        if (mailingStatus == MailingStatus.NEW) {
            return false;
        }

        updateStatusInDB(mailingID, mailingStatus, sendDate);
        return true;
    }

    @Override
    public String getWorkStatus(final int companyID, final int mailingID) {
    	return selectWithDefaultValue(logger, "SELECT work_status FROM mailing_tbl WHERE company_id = ? and mailing_id = ?", String.class, MailingStatus.NEW.getDbKey(), companyID, mailingID);
    }

    /**
     * Keep the send date unchanged
     */
    @DaoUpdateReturnValueCheck
    private void updateStatusInDB(final int mailingID, final MailingStatus mailingStatus) {
        try {
            update(logger, "UPDATE mailing_tbl SET work_status = ? WHERE mailing_id = ?", mailingStatus.getDbKey(), mailingID);
        } catch (final Exception e) {
            logger.error("Error updating work status for mailing " + mailingID, e);
        }
    }

    @DaoUpdateReturnValueCheck
    private void updateStatusInDB(final int mailingID, final MailingStatus mailingStatus, Date sendDate) {
        try {
            update(logger, "UPDATE mailing_tbl SET work_status = ?, send_date = ? WHERE mailing_id = ?", mailingStatus.getDbKey(), sendDate, mailingID);
        } catch (final Exception e) {
            logger.error("Error updating work status for mailing " + mailingID, e);
        }
    }

    protected boolean isStatus(final int mailingID, final String... statuses) {
        if (statuses == null || statuses.length == 0) {
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
    public boolean hasEmail(int companyId, int mailingId) {
        return hasMediaType(companyId, mailingId, MediaTypes.EMAIL);
    }

    @Override
    public boolean hasMediaType(int companyId, int mailingId, MediaTypes type) {
        try {
            final String sql = "SELECT COUNT(*) FROM mailing_tbl m " +
                    "JOIN mailing_mt_tbl mt ON mt.mailing_id = m.mailing_id " +
                    "WHERE m.mailing_id = ? AND m.company_id = ? AND mt.mediatype = ? AND mt.status = ?";

            return selectInt(logger, sql, mailingId, companyId, type.getMediaCode(), MediaTypeStatus.Active.getCode()) > 0;
        } catch (final Exception e) {
            logger.error("Error checking email for mailing " + mailingId, e);
            return false;
        }
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getUnsentMailings(Admin admin, final int rownums) {
        return getMailingForCalendar(admin, rownums, "is null");
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getPlannedMailings(Admin admin, final int rownums) {
        return getMailingForCalendar(admin, rownums, "is not null");
    }

    private PaginatedListImpl<Map<String, Object>> getMailingForCalendar(Admin admin, final int rownums, final String plannedCondition) {
        int companyID = admin.getCompanyID();
        List<Object> params = new ArrayList<>();
        params.add(MailingType.NORMAL.getCode());
        params.add(companyID);
        params.add(MaildropStatus.WORLD.getCodeString());
        params.add(companyID);

        String additionalRestriction = getAdditionalSqlRestrictions(admin, params, "m");

        params.add(rownums);

        String sql = "SELECT * FROM (SELECT m.mailing_id AS mailing_id, m.shortname AS shortname, m.description AS description, "
                + " m.work_status AS work_status, ml.shortname AS mailinglist, m.plan_date, m.workflow_id "
                + " FROM mailing_tbl m "
                + " LEFT JOIN mailinglist_tbl ml ON (ml.mailinglist_id = m.mailinglist_id AND ml.company_id = m.company_id) "
                + " WHERE m.is_template = 0 AND m.mailing_type = ? AND m.company_id = ? AND ml.deleted = 0 AND m.deleted = 0 "
                + " AND NOT EXISTS (SELECT 1 FROM maildrop_status_tbl md WHERE md.mailing_id = m.mailing_id AND md.status_field = ? AND md.company_id = ?) "
                + additionalRestriction
                + " AND m.plan_date " + plannedCondition + " ";
        if (isOracleDB()) {
            sql += " ORDER BY m.mailing_id DESC) WHERE rownum <= ?";
        } else {
            sql += ") res ORDER BY mailing_id DESC LIMIT 0, ?";
        }

        List<Map<String, Object>> tmpList;
        tmpList = select(logger, sql, params.toArray());
        final List<Map<String, Object>> result = new ArrayList<>();
        for (final Map<String, Object> row : tmpList) {
            final Map<String, Object> newBean = new HashMap<>();
            newBean.put("workstatus", row.get("WORK_STATUS"));
            final int mailingID = ((Number) row.get("MAILING_ID")).intValue();
            newBean.put("mailingid", mailingID);
            newBean.put("shortname", row.get("SHORTNAME"));
            newBean.put("description", row.get("DESCRIPTION"));
            newBean.put("mailinglist", row.get("MAILINGLIST"));
            if ("is not null".equalsIgnoreCase(plannedCondition)) {
                newBean.put("plandate", row.get("PLAN_DATE"));
            }
            if (hasActions(mailingID, companyID)) {
                newBean.put("hasActions", Boolean.TRUE);
            }
            newBean.put("usedInCM", ((Number) row.get("workflow_id")).intValue() > 0);
            result.add(newBean);
        }
        return new PaginatedListImpl<>(result, rownums, rownums, 1, "mailingid", "desc");
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getMailingList(Admin admin, final MailingsListProperties props) throws FulltextSearchInvalidQueryException {
        List<Object> selectParameters = new ArrayList<>();
        String selectSql = generateMailingsSelectQuery(props, selectParameters, admin);

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
                    } else if ("senddate".equalsIgnoreCase(sort) || (dbColumnType != null && (dbColumnType.getSimpleDataType() == SimpleDataType.Date || dbColumnType.getSimpleDataType() == SimpleDataType.DateTime))) {
                        sortClause = generateSendDateSortClause(direction, sortColumn);
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
            sortClause = generateSendDateSortClause(direction, sortColumn);
        }

        PaginatedListImpl<Map<String, Object>> resultList = selectPaginatedListWithSortClause(logger,
                selectSql,
                sortClause, sortColumn, AgnUtils.sortingDirectionToBoolean(direction),
                props.getPage(), props.getRownums(),
                new MailingMapRowMapper(), selectParameters.toArray());
        addComponentIdsToList(resultList, "preview_component");
        return resultList;
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(Admin admin, MailingsListProperties props) throws FulltextSearchInvalidQueryException {
        List<Object> selectParameters = new ArrayList<>();
        String selectSql = generateMailingsSelectQuery(props, selectParameters, admin);

        return select(logger, selectSql, LightweightMailingRowMapper.INSTANCE, selectParameters.toArray());
    }

    private String generateMailingsSelectQuery(MailingsListProperties props, List<Object> selectParameters, Admin admin) throws FulltextSearchInvalidQueryException {
        int adminId = admin.getAdminID();
        int companyID = admin.getCompanyID();
        // Check supported search modes by available db indices
        String searchQuery = props.getSearchQuery();
        if (StringUtils.isNotEmpty(searchQuery)) {
            if ((props.isSearchName() || props.isSearchDescription()) && !isBasicFullTextSearchSupported()) {
                props.setSearchName(false);
                props.setSearchDescription(false);
            }

            // Full text index search only works for patterns of size 3+ characters
            if (!isContentFullTextSearchSupported() || searchQuery.length() < 3) {
                props.setSearchContent(false);
            }
        } else {
            props.setSearchName(false);
            props.setSearchDescription(false);
            props.setSearchContent(false);
        }

        String fullTextSearchClause = props.getSearchQuery();
        if (props.isSearchName() || props.isSearchDescription() || props.isSearchContent()) {
            try {
                fullTextSearchClause = fulltextSearchQueryGenerator.generateSpecificQuery(props.getSearchQuery());
            } catch (final FulltextSearchQueryException e) {
                logger.error("Cannot transform full text search query: " + props.getSearchQuery());
            }
        }

        final StringBuilder selectSql = new StringBuilder("SELECT")
                .append(" a.work_status AS work_status")
                .append(", a.company_id AS company_id")
                .append(", a.mailing_id AS mailing_id")
                .append(", a.shortname AS shortname")
                .append(", a.mailing_type AS mailing_type")
                .append(", a.description AS description")
                .append(", a.send_date AS senddate")
                .append(", m.shortname AS mailinglist")
                .append(", a.creation_date AS creationdate")
                .append(", a.change_date AS changedate")
                .append(", a.target_expression AS target_expression")
                .append(", a.is_grid")
                .append(", a.content_type")
                .append(", a.workflow_id")
                .append(joinPostTypeColumns(admin.getCompanyID(), "a"))
                .append(getAdditionalColumnsName(props.getAdditionalColumns()))
                .append(" FROM mailing_tbl a")
                .append(joinAdditionalColumns(props.getAdditionalColumns()))
                .append(" LEFT JOIN mailinglist_tbl m ON a.mailinglist_id = m.mailinglist_id AND a.company_id = m.company_id AND m.deleted <> 1");

        if (props.isSearchContent()) {
            // Subquery to search within static text and html blocks
            selectSql.append(" LEFT JOIN (SELECT mailing_id, MAX(")
                    .append(isOracleDB() ? "CONTAINS(emmblock, ?)" : "MATCH(emmblock) AGAINST(? IN BOOLEAN MODE)")
                    .append(") AS relevance");
            selectParameters.add(fullTextSearchClause);

            selectSql.append(" FROM component_tbl")
                    .append(" WHERE company_id = ? AND mtype IN ('text/plain', 'text/html')");
            selectParameters.add(companyID);

            selectSql.append(" GROUP BY mailing_id")
                    .append(") comp ON a.mailing_id = comp.mailing_id");

            // Subquery to search within dynamic (included into static with agn tags) text and html blocks
            selectSql.append(" LEFT JOIN (SELECT cont.mailing_id, MAX(")
                    .append(isOracleDB() ? "CONTAINS(cont.dyn_content, ?)" : "MATCH(cont.dyn_content) AGAINST(? IN BOOLEAN MODE)")
                    .append(") AS relevance");
            selectParameters.add(fullTextSearchClause);

            selectSql.append(" FROM dyn_name_tbl nm JOIN dyn_content_tbl cont ON nm.dyn_name_id = cont.dyn_name_id")
                    .append(" WHERE cont.company_id = ?");
            selectParameters.add(companyID);

            selectSql.append(" GROUP BY cont.mailing_id")
                    .append(") cont ON a.mailing_id = cont.mailing_id");
        }

        selectSql.append(" WHERE a.company_id = ?");
        selectParameters.add(companyID);

        selectSql.append(" AND a.is_template = ?");
        selectParameters.add(BooleanUtils.toInteger(props.isTemplate()));

        if (props.getGrid() != null) {
            selectSql.append(" AND a.is_grid = ?");
            selectParameters.add(BooleanUtils.toInteger(props.getGrid()));
        }

        selectSql.append(" AND a.deleted = 0");

        if (adminId > 0 && isDisabledMailingListsSupported()) {
            selectSql.append(" AND m.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY);
            selectParameters.add(adminId);
        }

        final List<Integer> targetGroupIds = props.getTargetGroups();
        if (isNotEmpty(targetGroupIds)) {
            final StringBuilder targetExpressionRestriction = new StringBuilder();
            for (final Integer targetGroupId : targetGroupIds) {
                if (Objects.nonNull(targetGroupId)) {
                    if (targetExpressionRestriction.length() > 0) {
                        targetExpressionRestriction.append(" OR ");
                    }
                    targetExpressionRestriction.append(DbUtilities.createTargetExpressionRestriction(isOracleDB(), "a"));
                    selectParameters.add(targetGroupId);
                }
            }
            if (targetExpressionRestriction.length() > 0) {
                selectSql.append(" AND (").append(targetExpressionRestriction).append(")");
            }
        }
        selectSql.append(getTargetRestrictions(admin, selectParameters, "a"));

        if (!props.isTemplate()) {
            selectSql.append(" AND a.mailing_type IN (").append(props.getTypes()).append(")");
        }

        if (StringUtils.isNotBlank(props.getMediaTypes())) {
            selectSql.append(" AND EXISTS(SELECT 1 FROM mailing_mt_tbl mt WHERE mt.mailing_id = a.mailing_id AND mt.status = ? AND mt.mediatype IN (")
                    .append(props.getMediaTypes())
                    .append("))");
            selectParameters.add(MediaTypeStatus.Active.getCode());
        }

        if (isNotEmpty(props.getStatuses())) {
            selectSql.append(" AND a.work_status IN ('").append(StringUtils.join(props.getStatuses(), "', '")).append("')");
        }

        if (isNotEmpty(props.getMailingLists())) {
            selectSql.append(" AND a.mailinglist_id IN (").append(StringUtils.join(props.getMailingLists(), ", ")).append(")");
        }
        
        if (isNotEmpty(props.getArchives())) {
            selectSql.append(" AND a.campaign_id IN (").append(StringUtils.join(props.getArchives(), ", ")).append(")");
        }

        if (props.getSendDateBegin() != null || props.getSendDateEnd() != null) {
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.send_date", props.getSendDateBegin(), props.getSendDateEnd(), isOracleDB()));
        }

        if (props.getCreationDateBegin() != null || props.getCreationDateEnd() != null) {
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.creation_date", props.getCreationDateBegin(), props.getCreationDateEnd(), isOracleDB()));
        }

        if (props.getChangeDateBegin() != null || props.getChangeDateEnd() != null) {
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.change_date", props.getChangeDateBegin(), props.getChangeDateEnd(), isOracleDB()));
        }

        if (isNotEmpty(props.getBadge())) {
            // this is used to filter for the badge signs "EMC-Mailing" and/or "UsedInCampaignManager"
            final boolean grid = props.getBadge().contains("isgrid");
            final boolean cm = props.getBadge().contains("isCampaignManager");
            if (grid && cm) {
                selectSql.append(" AND (a.is_grid = 1 OR a.workflow_id > 0)");
            } else if (grid) {
                selectSql.append(" AND a.is_grid = 1");
            } else if (cm) {
                selectSql.append(" AND a.workflow_id > 0");
            }
        }

        final List<String> searchClauses = new ArrayList<>();
        if (props.isSearchName()) {
            searchClauses.add(isOracleDB() ? "CONTAINS(a.shortname, ?) > 0" : "MATCH(a.shortname) AGAINST(? IN BOOLEAN MODE) > 0");
            selectParameters.add(fullTextSearchClause);
        }

        if (props.isSearchDescription()) {
            searchClauses.add(isOracleDB() ? "CONTAINS(a.description, ?) > 0" : "MATCH(a.description) AGAINST(? IN BOOLEAN MODE) > 0");
            selectParameters.add(fullTextSearchClause);
        }

        if (props.isSearchContent()) {
            searchClauses.add("comp.relevance > 0 OR cont.relevance > 0 ");
        }

        final String searchSqlPart = StringUtils.join(searchClauses, " OR ");

        if (StringUtils.isNotEmpty(searchSqlPart)) {
            selectSql.append(" AND (").append(searchSqlPart).append(")");
        }

        return selectSql.toString();
    }

    /**
     * Thumbnail component_ids are mixed in to the data for making the basic db select more performant, because it does not need to detect the component_id for invisible elements
     */
    private void addComponentIdsToList(PaginatedListImpl<Map<String, Object>> list, String componentIdPropertyName) {
        List<Map<String, Object>> items = list.getList();
        if (items.size() > 0) {
            List<Integer> mailingIds = new ArrayList<>();
            for (Map<String, Object> item : items) {
                mailingIds.add(((Number) item.get("mailingid")).intValue());
            }

            List<Map<String, Object>> result = select(logger, "SELECT mailing_id, MAX(component_id) AS component_id FROM component_tbl WHERE mailing_id IN (" + StringUtils.join(mailingIds, ", ") + ") AND binblock IS NOT NULL AND comptype = ? GROUP BY mailing_id", MailingComponentType.ThumbnailImage.getCode());
            Map<Integer, Integer> componentIdsByMailingId = new HashMap<>();
            for (Map<String, Object> row : result) {
                componentIdsByMailingId.put(((Number) row.get("mailing_id")).intValue(), ((Number) row.get("component_id")).intValue());
            }

            for (Map<String, Object> item : items) {
                Integer componentId = componentIdsByMailingId.get(item.get("mailingid"));
                if (componentId == null) {
                    componentId = 0;
                }
                item.put(componentIdPropertyName, componentId);
            }
        }
    }

    /**
     * Method generates SQL to obtain isOnlyPostType value
     *
     * @param tableName is used for extended version to get proper mailing_id field ref
     * @return sql string for select query
     * <p>
     * Method is overwritten in ComMailingDaoImplExtended
     */
    protected String joinPostTypeColumns(int companyID, String tableName) {
        return ", 0 AS isOnlyPostType";
    }

    private String generateSendDateSortClause(String direction, String sortColumn) {
    	if("senddate".equals(sortColumn)) {
    		sortColumn = "send_date";
    	}
    	
        final boolean isAscending = AgnUtils.sortingDirectionToBoolean(direction, true);
        direction = isAscending ? "ASC" : "DESC";
        if (isOracleDB()) {
            return String.format("ORDER BY a.%3$s %1$s NULLS %2$s, mailing_id %1$s", direction, isAscending ? "LAST" : "FIRST", sortColumn);
        } else {
            // MySQL DESC sorts null-values to the end by default, oracle DESC sorts null-values to the top
            // MySQL ASC sorts null-values to the top by default, oracle ASC sorts null-values to the end
            return String.format("ORDER BY ISNULL(a.%2$s) %1$s, a.%2$s %1$s, mailing_id %1$s", direction, sortColumn);
        }
    }

    private String joinAdditionalColumns(final Set<String> additionalColumns) {
        final StringBuilder queryPart = new StringBuilder();
        for (final String columnKey : additionalColumns) {
            final MailingAdditionalColumn column = MailingAdditionalColumn.getColumn(columnKey);

            if (column == MailingAdditionalColumn.TEMPLATE) {
                queryPart.append(" LEFT JOIN mailing_tbl t ON a.mailtemplate_id = t.mailing_id");
                if (isGridTemplateSupported()) {
                    queryPart.append(" LEFT JOIN (SELECT g.template_id, g.mailing_id, pt.name FROM grid_template_tbl g LEFT JOIN grid_template_tbl pt ON g.parent_template_id = pt.template_id AND pt.deleted <> 1) gt ON gt.mailing_id = a.mailing_id");
                }
            } else if (column == MailingAdditionalColumn.SUBJECT) {
                queryPart.append(" LEFT JOIN mailing_mt_tbl mt ON a.mailing_id = mt.mailing_id AND mt.mediatype = 0 AND mt.status = ").append(MediaTypeStatus.Active.getCode());
            } else if (column == MailingAdditionalColumn.ARCHIVE) {
                queryPart.append(" LEFT JOIN campaign_tbl arch ON a.campaign_id = arch.campaign_id AND a.company_id = arch.company_id");
            }
        }
        return queryPart.toString();
    }

    private String getAdditionalColumnsName(Set<String> additionalColumns) {
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
                queryPart.append(", a.delivered AS recipientscount ");
            } else if (column == MailingAdditionalColumn.ARCHIVE) {
                queryPart.append(", arch.shortname AS archive ");
            }
        }
        return queryPart.toString();
    }

    @Override
    public boolean usedInCampaignManager(final int mailingId) {
        final String sqlGetCount = "SELECT COUNT(*) FROM workflow_dependency_tbl " +
                "WHERE type = ? AND entity_id = ?";

        return selectInt(logger, sqlGetCount, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId) > 0;
    }

    @Override
    public boolean usedInRunningWorkflow(final int mailingId, final int companyId) {
        final String sqlGetCount = "SELECT COUNT(*) FROM workflow_tbl w " +
                "JOIN workflow_dependency_tbl dep ON dep.company_id = w.company_id AND dep.workflow_id = w.workflow_id " +
                "WHERE w.company_id = ? AND w.status IN (?, ?) AND dep.type IN (?, ?) AND dep.entity_id = ?";

        final Object[] sqlParameters = new Object[]{
                companyId,
                WorkflowStatus.STATUS_ACTIVE.getId(),
                WorkflowStatus.STATUS_TESTING.getId(),
                WorkflowDependencyType.MAILING_DELIVERY.getId(),
                WorkflowDependencyType.MAILING_REFERENCE.getId(),
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
    public int getWorkflowId(final int mailingId, final int companyId) {
        final String sqlGetMaxId = "SELECT MAX(workflow_id) FROM workflow_dependency_tbl " +
                "WHERE company_id = ? AND type = ? AND entity_id = ?";

        return selectInt(logger, sqlGetMaxId, companyId, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId);
    }

    @Override
    public List<MailingBase> getMailingsForComparation(Admin admin) {
        String sqlStatement = "SELECT a.mailing_id, a.shortname, a.description, MIN(c.mintime) senddate"
                + joinPostTypeColumns(admin.getCompanyID(), "a")
                + " FROM mailing_tbl a"
                + " LEFT JOIN mailing_account_sum_tbl c ON (a.mailing_id = c.mailing_id)"
                + " WHERE a.company_id = ? AND a.deleted = 0 AND c.status_field = ?"
                + " AND a.mailing_id in (SELECT mailing_id FROM maildrop_status_tbl WHERE status_field IN (?, ?, ?) AND company_id = ?)";

        final List<Object> sqlParameters = new ArrayList<>();
        sqlParameters.add(admin.getCompanyID());
        sqlParameters.add(MaildropStatus.WORLD.getCodeString());
        sqlParameters.add(MaildropStatus.WORLD.getCodeString());
        sqlParameters.add(MaildropStatus.ACTION_BASED.getCodeString());
        sqlParameters.add(MaildropStatus.DATE_BASED.getCodeString());
        sqlParameters.add(admin.getCompanyID());

        sqlStatement += getAdditionalSqlRestrictions(admin, sqlParameters, "a");
        sqlStatement += " GROUP BY a.mailing_id, a.shortname, a.description ORDER BY mailing_id DESC";

        final List<Map<String, Object>> tmpList = select(logger, sqlStatement, sqlParameters.toArray());

        final List<MailingBase> result = new ArrayList<>();
        for (final Map<String, Object> row : tmpList) {
            final MailingBase newBean = new MailingBaseImpl();
            newBean.setId(((Number) row.get("mailing_id")).intValue());
            newBean.setShortname((String) row.get("shortname"));
            newBean.setDescription((String) row.get("description"));
            newBean.setSenddate((Date) row.get("senddate"));
            newBean.setOnlyPostType(BooleanUtils.toBoolean(((Number) row.get("isOnlyPostType")).intValue()));
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
    @DaoUpdateReturnValueCheck
    public boolean saveStatusmailOnErrorOnly(int companyID, final int mailingID, final boolean statusmailOnErrorOnly) {
        try {
            final int result = update(logger, "UPDATE mailing_tbl SET statmail_onerroronly = ? WHERE company_id = ? AND mailing_id = ?", statusmailOnErrorOnly ? 1 : 0, companyID, mailingID);
            return result == 1;
        } catch (final Exception e) {
            logger.error("Error while saveStatusmailOnErrorOnly", e);
            return false;
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public boolean saveSecuritySettings(int companyId, final int mailingId, MailingSendSecurityOptions options) {
        try {
            final int result = update(logger, "UPDATE mailing_tbl SET statmail_onerroronly = ?, statmail_recp = ?, clearance_email = ?, clearance_threshold = ? WHERE company_id = ? AND mailing_id = ?",
                    BooleanUtils.toInteger(options.isEnableNoSendCheckNotifications()),
                    options.getClearanceEmail(), options.getClearanceEmail(),
                    options.getClearanceThreshold(),
                    companyId, mailingId);
            return result == 1;
        } catch (final Exception e) {
            logger.error("Error while saving security settings", e);
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
        if (!includeUnscheduled) {
            queryBuilder.append(" AND mailing.work_status = '" + MailingStatus.SCHEDULED.getDbKey() + "'");
        }
        queryBuilder.append(" AND mt.param LIKE '%followup_for=\"").append(mailingID).append("\"%'");
        return select(logger, queryBuilder.toString(), IntegerRowMapper.INSTANCE, companyID);
    }

    @Override
    public int getStatusidForWorldMailing(final int mailingID, final int companyID) {
        final String query = "SELECT status_id FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field = ? AND genstatus = 3 AND senddate < CURRENT_TIMESTAMP";
        try {
            return selectIntWithDefaultValue(logger, query, 0, companyID, mailingID, MaildropStatus.WORLD.getCodeString());
        } catch (final Exception e) {
            return 0;
        }
    }

    @Override
    public boolean isMailingMarkedDeleted(final int mailingID, final int companyID) {
        try {
            final int returnValue = selectIntWithDefaultValue(logger, "SELECT deleted FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", 1, companyID, mailingID);
            return returnValue == 1;
        } catch (final Exception e) {
            return true;
        }
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
     * in the mailing-class (eg. FollowUpType.TYPE_FOLLOWUP_CLICKER) if no followup
     * is found, null is the returnvalue!
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
     */
    private String getFollowUpTypeFromParameterString(final String params) {
        return AgnUtils.getAttributeFromParameterString(params, "followup_method");
    }

    /**
     * returns the base-mailing for the given followup.
     */
    @Override
    public String getFollowUpFor(final int mailingID) throws Exception {
    	if (mailingID == 0) {
            return null;
    	}
    	
        String params = null;
        String followUpFor = null;
        // first check, if we have a followup mailing.
        if (getMailingType(mailingID) != MailingType.FOLLOW_UP) {
            return null;
        }
        params = getEmailParameter(mailingID);

        // split the parameters
        if (params != null) {
            final String[] paramArray = params.split(",");

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
    public PaginatedListImpl<Map<String, Object>> getDashboardMailingList(Admin admin, final String sort, final String direction, final int rownums) {
        String orderBy = getDashboardMailingsOrder(sort, direction, Arrays.asList("shortname", "description", "mailinglist"));
        final boolean isOracle = isOracleDB();

        List<Object> params = new ArrayList<>();

        params.add(admin.getCompanyID());

        String additionalRestriction = getAdditionalSqlRestrictions(admin, params);

        params.add(rownums);
        params.add(MaildropStatus.WORLD.getCodeString());

        String sqlStatement;
        if (isOracle) {
            sqlStatement = "SELECT * FROM (" +
                    "SELECT m.work_status, m.mailing_id AS mid, m.shortname, m.description, acc.mintime AS senddate, ml.shortname AS mailinglist, m.change_date, m.workflow_id " +
                    joinPostTypeColumns(admin.getCompanyID(), "m") +
                    " FROM (" +
                    "  SELECT * FROM (" +
                    "    SELECT company_id, mailing_id, shortname, description, work_status, change_date, mailinglist_id, workflow_id FROM mailing_tbl" +
                    "    WHERE company_id = ? AND deleted = 0 AND is_template = 0" +
                    additionalRestriction +
                    "    ORDER BY COALESCE(change_date, TO_DATE('01.01.1900', 'dd.mm.yyyy')) DESC, mailing_id DESC" +
                    "  ) mailing WHERE ROWNUM <= ?" +
                    ") m" +
                    "  LEFT JOIN mailing_account_sum_tbl acc" +
                    "    ON m.mailing_id = acc.mailing_id AND acc.status_field = ?" +
                    "  LEFT JOIN mailinglist_tbl ml" +
                    "    ON m.mailinglist_id = ml.mailinglist_id AND ml.company_id = m.company_id AND ml.deleted = 0" +
                    ") subsel "
                    + orderBy;
        } else {
            sqlStatement = "SELECT * FROM (" +
                    "SELECT m.work_status, m.mailing_id AS mid, m.shortname, m.description, acc.mintime AS senddate, ml.shortname AS mailinglist, m.change_date, m.workflow_id" +
                    joinPostTypeColumns(admin.getCompanyID(), "m") +
                    " FROM (" +
                    "  SELECT company_id, mailing_id, shortname, description, work_status, change_date, mailinglist_id, workflow_id FROM mailing_tbl" +
                    "  WHERE company_id = ? AND deleted = 0 AND is_template = 0 " +
                    additionalRestriction +
                    "  ORDER BY COALESCE(change_date, DATE_FORMAT('01.01.1900','%d.%m.%Y')) DESC, mailing_id DESC" +
                    "  LIMIT ?" +
                    ") m" +
                    "  LEFT JOIN mailing_account_sum_tbl acc" +
                    "    ON m.mailing_id = acc.mailing_id AND acc.status_field = ?" +
                    "  LEFT JOIN mailinglist_tbl ml" +
                    "    ON m.mailinglist_id = ml.mailinglist_id AND ml.company_id = m.company_id AND ml.deleted = 0" +
                    ") subsel "
                    + orderBy.replaceAll(" e\\.", " ");
        }

        final List<Map<String, Object>> rows = select(logger, sqlStatement, params.toArray());

        final List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (final Map<String, Object> row : rows) {
            final Map<String, Object> newBean = new HashMap<>();
            newBean.put("workstatus", row.get("work_status"));
            newBean.put("mailingid", ((Number) row.get("mid")).intValue());
            newBean.put("shortname", row.get("shortname"));
            newBean.put("description", row.get("description"));
            newBean.put("mailinglist", row.get("mailinglist"));
            newBean.put("senddate", row.get("senddate"));
            newBean.put("usedInCM", ((Number) row.get("workflow_id")).intValue() > 0);
            newBean.put("changedate", row.get("change_date"));
            newBean.put("isOnlyPostType", ((Number) row.get("isOnlyPostType")).intValue() > 0);
            result.add(newBean);
        }

        PaginatedListImpl<Map<String, Object>> resultList = new PaginatedListImpl<>(result, result.size(), rownums, 1, sort, direction);
        addComponentIdsToList(resultList, "component");
        return resultList;
    }

    private String getDashboardMailingsOrder(final String sort, final String direction, final List<String> charColumns) {
        String orderby;
        if (StringUtils.isNotBlank(sort)) {
            /*
             * When the users want us to sort the list by senddate, then we have
             * to handle the null values to be the lowest values otherwise we
             * get a strange order: null values are always the greatest values.
             */
            if (sort.trim().equalsIgnoreCase("senddate")) {
                if (isOracleDB()) {
                    orderby = " ORDER BY COALESCE(senddate, TO_DATE('01.01.1900', 'dd.mm.yyyy'))";
                } else {
                    orderby = " ORDER BY COALESCE(senddate, DATE_FORMAT('01.01.1900', '%d.%m.%Y'))";
                }
            } else {
                if (charColumns.contains(sort)) {
                    orderby = " ORDER BY LOWER(TRIM(" + sort + "))";
                } else {
                    orderby = " ORDER BY " + sort;
                }
            }
            orderby += " " + direction;
        } else {
            // We need the workaround with COALESCE and TO_DATE here to get mailings
            // with change_date=NULL to be ordered lower than other change_dates
            if (isOracleDB()) {
                orderby = " ORDER BY COALESCE(change_date, TO_DATE('01.01.1900', 'dd.mm.yyyy')) DESC, mid DESC";
            } else {
                orderby = " ORDER BY COALESCE(change_date, DATE_FORMAT('01.01.1900', '%d.%m.%Y')) DESC, mid DESC";
            }
        }
        return orderby;
    }

    @Override
    public List<Integer> getBirtReportMailingsToSend(final int companyID, final int reportId, final Date startDate, final Date endDate, final int filterId, final int filterValue) {
        List<Object> params = new ArrayList<>();
        params.add(companyID);
        params.add(MaildropStatus.WORLD.getCodeString());
        params.add(reportId);

        String sql = "SELECT mailing_id FROM maildrop_status_tbl WHERE company_id = ? AND status_field = ?" +
                " AND mailing_id NOT IN (SELECT mailing_id FROM birtreport_sent_mailings_tbl WHERE report_id = ?)" +
                filterClause(filterId, companyID, filterValue, params) +
                " AND senddate >= ? AND senddate <= ? " +
                " ORDER BY senddate DESC";

        params.add(startDate);
        params.add(endDate);

        return select(logger, sql, IntegerRowMapper.INSTANCE, params.toArray());
    }

    private String filterClause(int filterCode, int companyId, int filterValue, List<Object> params) {
        switch (FilterType.getFilterTypeByKey(filterCode)) {
            case FILTER_MAILINGLIST:
                params.add(companyId);
                params.add(filterValue);
                return " AND mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?) ";

            case FILTER_ARCHIVE:
                params.add(companyId);
                params.add(filterValue);
                return " AND mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND campaign_id = ?) ";

            case FILTER_MAILING:
                params.add(filterValue);
                return " AND mailing_id = ?";

            default:
                return "";
        }
    }

    protected List<Integer> getLastWorldSendMailingIds(final int companyID, final int limit) {
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

        return select(logger, sql, IntegerRowMapper.INSTANCE, sqlParameters);
    }

    @Override
    public final Date getSendDate(final int companyId, final int mailingId) {
        final String sql = isOracleDB()
                ? "SELECT senddate FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY DECODE(status_field, 'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC"
                : "SELECT senddate FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY CASE status_field WHEN 'W' THEN 1 WHEN 'R' THEN 2 WHEN 'D' THEN 2 WHEN 'E' THEN 3 WHEN 'C' THEN 3 WHEN 'T' THEN 4 WHEN 'A' THEN 4 ELSE 5 END, status_id DESC";

        final List<Date> result = select(logger, sql, DateRowMapper.INSTANCE, companyId, mailingId);

        return !result.isEmpty()
                ? result.get(0)
                : null;
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
            final List<Map<String, Object>> sendDateResult = select(logger, sql, mailingID);
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
    public List<Map<String, Object>> getLastSentWorldMailings(Admin admin, int rownum) {
        String sql;
        final boolean isOracle = isOracleDB();
        List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());
        params.add(MaildropStatus.WORLD.getCodeString());

        String additionalRestriction = getAdditionalSqlRestrictions(admin, params, "m");

        if (isOracle) {
            sql = " SELECT mailing_id mailingid, shortname FROM (SELECT m.mailing_id, m.shortname, maxtime senddate"
                    + " FROM mailing_account_sum_tbl a JOIN mailing_tbl m ON a.mailing_id = m.mailing_id"
                    + "	WHERE m.company_id = ? AND a.status_field = ? AND m.deleted = 0 "
                    + additionalRestriction
                    + " ORDER BY senddate DESC) WHERE ROWNUM <= ? ";
            params.add(rownum);
        } else {
            sql = " SELECT m.mailing_id as mailingid, m.shortname, maxtime senddate"
                    + " FROM mailing_account_sum_tbl a JOIN mailing_tbl m ON a.mailing_id = m.mailing_id"
                    + "	WHERE m.company_id = ? AND a.status_field = ? AND m.deleted = 0 "
                    + additionalRestriction
                    + " ORDER BY senddate DESC LIMIT " + rownum;
        }

        final List<Map<String, Object>> worldmailingList = select(logger, sql, params.toArray());

        // update the type of mailingid from bigdecimal to integer
        for (final Map<String, Object> itemMap : worldmailingList) {
            itemMap.put("mailingid", ((Number) itemMap.get("mailingid")).intValue());
        }

        return worldmailingList;
    }

    @Override
    public boolean deleteAccountSumEntriesByCompany(final int companyID) {
        update(logger, "DELETE FROM mailing_account_sum_tbl WHERE company_id = ?", companyID);
        return selectInt(logger, "SELECT COUNT(*) FROM mailing_account_sum_tbl WHERE company_id = ?", companyID) == 0;
    }

    @Override
    public boolean isTransmissionRunning(final int mailingID) {
        String query = "SELECT count(*) FROM maildrop_status_tbl WHERE status_field IN (?) AND genstatus IN (1, 2) AND mailing_id = ?";
        final boolean isWorldMailingSentBeingAtTheMoment = selectInt(logger, query, MaildropStatus.WORLD.getCodeString(), mailingID) > 0;
        if (isWorldMailingSentBeingAtTheMoment) {
            query = " SELECT CASE WHEN COUNT(a.maildrop_id) = COUNT(d.status_id) THEN 0 ELSE 1 END transmission_running "
                    + " FROM maildrop_status_tbl d LEFT JOIN mailing_account_tbl a ON d.status_id = a.maildrop_id WHERE d.mailing_id = ?";

            return selectInt(logger, query, mailingID) == 1;
        } else {
            query = "SELECT count(*) FROM maildrop_status_tbl WHERE status_field IN (?, ?) AND genstatus IN (1, 2) AND mailing_id = ?";
            return selectInt(logger, query, MaildropStatus.ADMIN.getCodeString(), MaildropStatus.TEST.getCodeString(), mailingID) > 0;
        }
    }

    @Override
    public int getLastSentMailing(final int companyID, final int customerID) {
        return selectIntWithDefaultValue(logger, "SELECT MAX(mailing_id) FROM success_" + companyID + "_tbl WHERE customer_id = ?"
                + " AND timestamp = (SELECT MAX(timestamp) FROM success_" + companyID + "_tbl " + " WHERE customer_ID =  ?)", -1, customerID, customerID);
    }

    @Override
    public int getLastSentWorldMailingByCompanyAndMailinglist(final int companyID, final int mailingListID) {
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
    public List<MailingBase> getSentWorldMailingsForReports(final int companyID, final int number, final int targetId, Set<Integer> adminAltgIds) {
        return getPredefinedMailingsForReports(companyID, number, -1, 0, null, "", targetId, adminAltgIds);
    }

    @Override
    public List<MailingBase> getPredefinedNormalMailingsForReports(final int companyId, final Date from, final Date to, final int filterType, final int filterValue, final String orderKey, final int targetId, Set<Integer> adminAltgIds) {
        final char statusField = MaildropStatus.WORLD.getCode();
        String orderColumn = "shortname";
        if (!orderKey.isEmpty() && orderKey.equals("date")) {
            orderColumn = "change_date";
        }

        final StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ")
                .append("   (SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date ")
                .append(joinPostTypeColumns(companyId, "c"))
                .append("       FROM mailing_account_tbl c ")
                .append("       LEFT JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
                .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' AND a.mailing_type = 0 %s ")
                .append("       GROUP BY a.shortname, a.mailing_id, a.send_date, a.change_date")
                .append("       ORDER BY a.send_date DESC) res");
        final List<Object> parameters = new ArrayList<>();
        parameters.add(companyId);

        String filter = getSettingsTypeFilter(filterType, filterValue, parameters);

        if (StringUtils.isNotEmpty(filter)) {
            filter += " AND a.deleted = 0";
        }
        
        filter += getTargetRestrictionsForReports(companyId, targetId, adminAltgIds, parameters, "a");

        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            query.append("   WHERE res.senddate BETWEEN ? AND ? ");

            parameters.add(from);
            parameters.add(to);
        }

        query.append(" ORDER BY ").append(orderColumn).append(" DESC");

        final String filteredQuery = String.format(query.toString(), filter);


        return select(logger, filteredQuery, PARTIAL_MAILING_BASE_ROW_MAPPER, parameters.toArray());
    }

    protected String getTargetRestrictionsForReports(int companyId, int targetId, Set<Integer> adminAltgIds, List<Object> parameters, String tableAlias) {
        return StringUtils.EMPTY;
    }

    public static class PartialMailingBaseRowMapper implements RowMapper<MailingBase> {
        @Override
        public MailingBase mapRow(final ResultSet resultSet, final int i) throws SQLException {
            final MailingBaseImpl mailingBase = new MailingBaseImpl();
            mailingBase.setId(resultSet.getInt("mailing_id"));
            mailingBase.setShortname(resultSet.getString("shortname"));
            mailingBase.setSenddate(resultSet.getTimestamp("senddate"));
            mailingBase.setOnlyPostType(BooleanUtils.toBoolean(resultSet.getInt("isOnlyPostType")));
            return mailingBase;
        }
    }

    @Override
    public List<MailingBase> getPredefinedMailingsForReports(final int companyId, final int rownum, final int filterType, final int filterValue, final MailingType mailingType, String orderKey, final int targetId, Set<Integer> adminAltgIds) {
        if (DATE_ORDER_KEY.equalsIgnoreCase(orderKey)) {
            orderKey = "change_date";
        } else {
            orderKey = "shortname";
        }

        char statusField = getStatusField(mailingType);
        final StringBuilder query = new StringBuilder();
        if (isOracleDB()) {
            query.append("SELECT * FROM ")
                    .append("   (SELECT res.mailing_id, res.shortname, res.senddate, res.change_date, res.isOnlyPostType FROM ")
                    .append("   (SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date ")
                    .append(joinPostTypeColumns(companyId, "c"))
                    .append("       FROM mailing_account_tbl c")
                    .append("       INNER JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
                    .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' %s ) res")
                    .append("       GROUP BY res.mailing_id, res.shortname, res.senddate, res.change_date, res.isOnlyPostType")
                    .append("       ORDER BY senddate DESC, ").append(orderKey).append(" ASC)")
                    .append(" WHERE ROWNUM <= ?");
        } else {
            query.append("SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date ")
                    .append(joinPostTypeColumns(companyId, "c"))
                    .append("       FROM mailing_account_tbl c")
                    .append("       INNER JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
                    .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' %s ")
                    .append("       GROUP BY a.mailing_id, a.shortname, a.send_date, a.change_date, isOnlyPostType")
                    .append("       ORDER BY a.send_date DESC, ").append(orderKey).append(" ASC")
                    .append(" LIMIT ").append(rownum);
        }

        final List<Object> parameters = new ArrayList<>();
        parameters.add(companyId);

        String filter = getSettingsTypeFilter(filterType, filterValue, parameters);

        if (mailingType != null) {
            filter += " AND a.mailing_type = ?";
            parameters.add(mailingType.getCode());
        }
        if (StringUtils.isNotEmpty(filter)) {
            filter += " AND a.deleted = 0";
        }
        
        filter += getTargetRestrictionsForReports(companyId, targetId, adminAltgIds, parameters, "a");

        final String filteredQuery = String.format(query.toString(), filter);
        if (isOracleDB()) {
        	parameters.add(rownum);
        }

        return select(logger, filteredQuery, PARTIAL_MAILING_BASE_ROW_MAPPER, parameters.toArray());
    }

    private char getStatusField(MailingType mailingType) {
        if (mailingType != null) {
            switch (mailingType) {
                case ACTION_BASED:
                    return MaildropStatus.ACTION_BASED.getCode();
                case DATE_BASED:
                    return MaildropStatus.DATE_BASED.getCode();
                case INTERVAL:
                    return MaildropStatus.ON_DEMAND.getCode();
                default:
                    // nothing do
            }
        }
        return MaildropStatus.WORLD.getCode();
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
            case FILTER_TARGET:
                filterClause = " AND " + DbUtilities.createTargetExpressionRestriction(isOracleDB(), "a");
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
                    logger.info("Found no name target group ID " + targetId);
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
    public List<Map<String, Object>> getSentAndScheduled(Admin admin, final Date startDate, final Date endDate) {
        List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());
        params.add(startDate);
        params.add(endDate);

        String query = "SELECT mailing.mailing_id mailingid, mailing.work_status workstatus, MAX(cmp.component_id) preview_component,"
			+ " MIN(maildrop.senddate) senddate, mailing.shortname shortname, mediatype.param subject,"
			+ " COALESCE(SUM(account.no_of_mailings), 0) mailssent, MAX(maildrop.genstatus) genstatus, maildrop.status_field statusField"
			+ joinPostTypeColumns(admin.getCompanyID(), "mailing")
			+ " FROM mailing_tbl mailing LEFT JOIN maildrop_status_tbl maildrop ON mailing.mailing_id = maildrop.mailing_id"
			+ " LEFT JOIN mailing_mt_tbl mediatype ON maildrop.mailing_id = mediatype.mailing_id"
			+ " LEFT JOIN mailing_account_tbl account ON (maildrop.mailing_id = account.mailing_id AND account.status_field = maildrop.status_field)"
			+ " LEFT JOIN component_tbl cmp ON (maildrop.mailing_id = cmp.mailing_id AND cmp.comptype = " + MailingComponentType.ThumbnailImage.getCode() + ")"
			+ " WHERE mailing.company_id = ? AND mailing.deleted = 0 AND mailing.is_template = 0 AND maildrop.status_field IN ('" + MaildropStatus.WORLD.getCode() + "') AND mediatype.mediatype = " + MediaTypes.EMAIL.getMediaCode()
			+ " AND maildrop.senddate > ? AND maildrop.senddate < ?"
			+ getAdditionalSqlRestrictions(admin, params, "mailing")
			+ " GROUP BY mailing.mailing_id, mailing.shortname, mediatype.param, mailing.work_status, maildrop.status_field";
        query = addSortingToQuery(query, "MIN(maildrop.senddate)", "desc");
        final List<Map<String, Object>> mailingList = select(logger, query, params.toArray());
        
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
    public List<Map<String, Object>> getPlannedMailings(Admin admin, final Date startDate, final Date endDate) {
        List<Object> params = new ArrayList<>();

        params.add(MailingComponentType.ThumbnailImage.getCode());
        params.add(admin.getCompanyID());
        params.add(startDate);
        params.add(endDate);

        String additionalRestriction = getAdditionalSqlRestrictions(admin, params, "mailing");

        String query =
                "SELECT mailingid, workstatus, preview_component, senddate, shortname, subject, genstatus, isOnlyPostType, " +
                        "    (SELECT MAX(maildrop.status_field) FROM maildrop_status_tbl maildrop WHERE maildrop.mailing_id = mailingid AND maildrop.gendate = lastMailDropDate GROUP BY maildrop.mailing_id, maildrop.status_field) statusField " +
                        "FROM ( " +
                        "SELECT mailing.mailing_id mailingid," +
                        "    mailing.work_status workstatus," +
                        "    max(cmp.component_id) preview_component," +
                        "    mailing.plan_date senddate," +
                        "    mailing.shortname shortname," +
                        "    mediatype.param subject, " +
                        "    -1 genstatus, " +
                        "    MAX(maildrop.gendate) lastMailDropDate " +
                        joinPostTypeColumns(admin.getCompanyID(), "mailing") +
                        " FROM mailing_tbl mailing" +
                        "    LEFT JOIN mailing_mt_tbl mediatype ON mailing.mailing_id = mediatype.mailing_id" +
                        "    LEFT JOIN maildrop_status_tbl maildrop ON mailing.mailing_id = maildrop.mailing_id" +
                        "    LEFT JOIN component_tbl cmp ON (mailing.mailing_id = cmp.mailing_id AND cmp.comptype = ?) " +
                        "WHERE" +
                        "    mailing.company_id = ? AND" +
                        "    mailing.deleted = 0 AND" +
                        "    mailing.plan_date > ? AND mailing.plan_date < ? AND" +
                        "    mediatype.mediatype = 0 " +
                        additionalRestriction +
                        "    AND (maildrop.senddate IS NULL OR ( " +
                        "        maildrop.senddate IS NOT NULL AND maildrop.status_field IN ('A', 'T') AND " +
                        "        maildrop.genchange = (SELECT max(mds.genchange) FROM maildrop_status_tbl mds WHERE mds.mailing_id = mailing.mailing_id) AND " +
                        "        not exists (SELECT * FROM maildrop_status_tbl mds WHERE mds.mailing_id = mailing.mailing_id AND mds.status_field = 'W') " +
                        "        ) " +
                        "    ) " +
                        "GROUP BY mailing.mailing_id, mailing.shortname, mediatype.param, mailing.work_status, mailing.plan_date, mediatype.param )" + (isOracleDB() ? "" : "subsel ");
        query = addUnsentMailingSort(query, "mailingid", "desc");
        final List<Map<String, Object>> mailingList =
                select(logger, query, params.toArray());
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
    public Map<Integer, Integer> getOpeners(final int companyId, final List<Integer> mailingsId) {
        return getOpeners(companyId, mailingsId, false);
    }

    @Override
    public Map<Integer, Integer> getOpeners(final int companyId, final Collection<Integer> mailingsId, final boolean currentRecipientsOnly) {
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
    public Map<Integer, Integer> getClickers(final int companyId, final List<Integer> mailingsId) {
        return getClickers(companyId, mailingsId, false);
    }

    @Override
    public Map<Integer, Integer> getClickers(final int companyId, final Collection<Integer> mailingsId, final boolean currentRecipientsOnly) {
        String query = "SELECT COUNT(DISTINCT rlog.customer_id) AS clickers, rlog.mailing_id FROM rdirlog_" + companyId + "_tbl rlog ";
        if (currentRecipientsOnly) {
            query += " JOIN mailing_tbl m ON m.mailing_id = rlog.mailing_id ";
            query += " JOIN customer_" + companyId + "_binding_tbl bind ON bind.mailinglist_id = m.mailinglist_id AND rlog.customer_id = bind.customer_id ";
        }
        query += " WHERE " + makeBulkInClauseForInteger("rlog.mailing_id", mailingsId) + " GROUP BY rlog.mailing_id";
        final List<Map<String, Object>> resultList = select(logger, query);
        return fillAllMailings(resultList, "mailing_id", "clickers", mailingsId);
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

    public static class ComMailingRowMapper implements RowMapper<Mailing> {
        @Override
        public Mailing mapRow(final ResultSet resultSet, final int index) throws SQLException {
            final Mailing mailing = new MailingImpl();

            mailing.setCompanyID(resultSet.getInt("company_id"));
            mailing.setGridMailing(resultSet.getInt("is_grid") > 0);
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
            try {
				mailing.setMailingType(MailingType.fromCode(resultSet.getInt("mailing_type")));
			} catch (Exception e) {
				throw new SQLException("Invalid MailingType code: " + resultSet.getInt("mailing_type"));
			}
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
            mailing.setStatusmailOnErrorOnly(resultSet.getInt("statmail_onerroronly") > 0);
            mailing.setClearanceThreshold(resultSet.getInt("clearance_threshold"));
            mailing.setClearanceEmail(resultSet.getString("clearance_email"));
            mailing.setPlanDate(resultSet.getTimestamp("plan_date"));

            if (DbUtilities.resultsetHasColumn(resultSet, "senddate")) {
                mailing.setSenddate(resultSet.getTimestamp("senddate"));
            }

            return mailing;
        }
    }

    /**
     * TODO: Replace references to Mailing by ComMailing. (currently more than 1000) and than replace this special ComMailingToMailingRowMapper by ComMailingRowMapper
     */
    protected static class ComMailingToMailingRowMapper implements RowMapper<Mailing> {
        @Override
        public Mailing mapRow(final ResultSet resultSet, final int index) throws SQLException {
            final Mailing mailing = new MailingImpl();

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
            try {
				mailing.setMailingType(MailingType.fromCode(resultSet.getInt("mailing_type")));
			} catch (Exception e) {
				throw new SQLException("Invalid MailingType code: " + resultSet.getInt("mailing_type"));
			}
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
            mailing.setStatusmailOnErrorOnly(resultSet.getInt("statmail_onerroronly") > 0);
            mailing.setClearanceThreshold(resultSet.getInt("clearance_threshold"));
            mailing.setClearanceEmail(resultSet.getString("clearance_email"));
            mailing.setPlanDate(resultSet.getTimestamp("plan_date"));

            return mailing;
        }
    }

    protected static class MailingTemplatesWithPreviewRowMapper implements RowMapper<MailingBase> {
        @Override
        public MailingBase mapRow(final ResultSet resultSet, final int i) throws SQLException {
            final Mailing mailing = new MailingImpl();

            mailing.setId(resultSet.getInt("mailing_id"));
            mailing.setShortname(resultSet.getString("shortname"));
            mailing.setDescription(resultSet.getString("description"));
            mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
            mailing.setPreviewComponentId(resultSet.getInt("preview_component"));
            mailing.setOnlyPostType(BooleanUtils.toBoolean(resultSet.getInt("isOnlyPostType")));

            return mailing;
        }
    }

    /**
     * This Rowmapper reads all values from mailing_tbl also the class MailingBase cannot hold them all.
     * This is for keeping former code especially in JSPs working, which might use this values by reflection.
     * Hibernate returned all data of mailing_tbl in the old version of this Dao.
     * <p>
     * TODO: Check JSPs and return real MailingBase with reduced data
     */
    protected static class ComMailingToMailingBaseRowMapper implements RowMapper<MailingBase> {
        @Override
        public MailingBase mapRow(final ResultSet resultSet, final int index) throws SQLException {
            final Mailing mailing = new MailingImpl();

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
            try {
				mailing.setMailingType(MailingType.fromCode(resultSet.getInt("mailing_type")));
			} catch (Exception e) {
				throw new SQLException("Invalid MailingType code: " + resultSet.getInt("mailing_type"));
			}
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
            mailing.setStatusmailOnErrorOnly(resultSet.getInt("statmail_onerroronly") > 0);
            mailing.setClearanceThreshold(resultSet.getInt("clearance_threshold"));
            mailing.setClearanceEmail(resultSet.getString("clearance_email"));
            mailing.setPlanDate(resultSet.getTimestamp("plan_date"));
            mailing.setOnlyPostType(BooleanUtils.toBoolean(resultSet.getInt("isOnlyPostType")));

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
    public Map<String, Object> getMailingWithWorkStatus(final int mailingId, final int companyId) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT a.company_id, a.mailing_id, a.shortname, a.description, a.work_status, a.mailing_type, b.senddate, a.change_date ")
                .append(joinPostTypeColumns(companyId, "a"));
        if (isOracleDB()) {
            queryBuilder.append(", a.workflow_id AS usedInWorkflows ");

            queryBuilder
                    .append("FROM mailing_tbl a ")
                    .append("LEFT OUTER JOIN(")
                    .append("SELECT DISTINCT mailing_id, senddate, genchange FROM ( ")
                    .append("SELECT mailing_id, senddate, genchange, MAX(genchange) OVER (partition BY mailing_id) AS max_date ")
                    .append("FROM maildrop_status_tbl ) WHERE max_date = genchange")
                    .append(") b ON (a.mailing_id = b.mailing_id) ")
                    .append("WHERE a.company_id = ? AND a.mailing_id = ?");
        } else {
            queryBuilder.append(", a.workflow_id AS usedInWorkflows ");

            queryBuilder
                    .append("FROM mailing_tbl a ")
                    .append("LEFT OUTER JOIN(")
                    .append("SELECT DISTINCT mds.mailing_id, mds.senddate, mds.genchange FROM maildrop_status_tbl mds ")
                    .append("INNER JOIN (SELECT mailing_id, MAX(genchange) AS max_date FROM maildrop_status_tbl GROUP BY mailing_id) max_genchange ")
                    .append("ON mds.mailing_id = max_genchange.mailing_id AND mds.genchange = max_genchange.max_date")
                    .append(") b ON (a.mailing_id = b.mailing_id) ")
                    .append("WHERE a.company_id = ? AND a.mailing_id = ?");
        }
        final List<Map<String, Object>> list = select(logger, queryBuilder.toString(), companyId, mailingId);
        return (list.size() > 0 ? list.get(0) : new HashMap<>());
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void cleanTestDataInSuccessTbl(final int mailingId, final int companyId) {
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
    public void cleanTestDataInMailtrackTbl(final int mailingId, final int companyId) {
        final String query = "DELETE" + (isOracleDB() ? "" : " mtrack") + " FROM " + getMailtrackTableName(companyId) + " mtrack"
                + " WHERE EXISTS (SELECT 1 FROM maildrop_status_tbl mds"
                + " WHERE mds.status_id = mtrack.maildrop_status_id"
                + " AND mds.mailing_id = ?)"
                + " AND EXISTS (SELECT 1 FROM " + getBindingTableName(companyId) + " bind"
                + " WHERE bind.customer_id = mtrack.customer_id"
                + " AND bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')"
                + " AND bind.mailinglist_id IN (SELECT m.mailinglist_id FROM mailing_tbl m WHERE m.mailing_id = ? AND m.company_id = ?))";
        update(logger, query, mailingId, mailingId, companyId);
    }
    
    protected String addSortingToQuery(final String query, final String sortField, final String sortDirection) {
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

    @Override
    public List<Integer> getMailingIdsForIntervalSend() {
        String sql;
        if (isOracleDB()) {
            sql = "SELECT DISTINCT mail.mailing_id FROM mailing_info_tbl info, mailing_tbl mail WHERE mail.deleted = 0 AND mail.mailing_id = info.mailing_id AND mail.mailing_type = ? AND mail.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "' AND info.name = ? AND TO_DATE(info.value, 'YYYY.MM.DD HH24:MI') < CURRENT_TIMESTAMP AND NOT EXISTS (SELECT 1 FROM mailing_info_tbl b WHERE b.mailing_id = info.mailing_id AND b.name = ?)";
        } else {
            sql = "SELECT DISTINCT mail.mailing_id FROM mailing_info_tbl info, mailing_tbl mail WHERE mail.deleted = 0 AND mail.mailing_id = info.mailing_id AND mail.mailing_type = ? AND mail.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "' AND info.name = ? AND DATE_FORMAT(info.value,'%Y.%m.%d %H:%i') < CURRENT_TIMESTAMP AND NOT EXISTS (SELECT 1 FROM mailing_info_tbl b WHERE b.mailing_id = info.mailing_id AND b.name = ?)";
        }
        return select(logger, sql, IntegerRowMapper.INSTANCE, MailingType.INTERVAL.getCode(), NEXT_START.getName(), ERROR.getName());

    }

    @Override
    public final boolean isActiveIntervalMailing(final int mailingID) {
        final String sql = "SELECT count(mail.mailing_id) FROM mailing_info_tbl info, mailing_tbl mail WHERE mail.mailing_id = ? AND mail.mailing_id = info.mailing_id AND mail.mailing_type = ? AND mail.work_status = '" + MailingStatus.ACTIVE.getDbKey() + "' AND info.name = ?";

        final int result = selectInt(logger, sql, mailingID, MailingType.INTERVAL.getCode(), NEXT_START.getName());

        return result > 0;
    }

    @Override
    public MailingSendStatus getMailingSendStatus(final int mailingID, final int companyID) {
        final MailingSendStatus status = new MailingSendStatusImpl();

        final int daysToExpire = configService.getIntegerValue(ConfigValue.ExpireSuccess, companyID);
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
    public List<Mailing> getMailingsForMLID(final int companyID, final int mailinglistID) {
        final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, statmail_onerroronly, clearance_threshold, clearance_email, dynamic_template, plan_date"
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
     *
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
     *
     * @param customerID Id of the recipient for the newsletter.
     * @param companyID  the company to look in.
     * @return The mailingID of the last newsletter that would have been
     * sent to this recipient.
     */
    @Override
    public int findLastNewsletter(final int customerID, final int companyID, final int mailinglist) {
        final String sql = "SELECT m.mailing_id, m.target_expression, a.timestamp"
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
            logger.error("findLastNewsletter: " + e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public String getMailingRdirDomain(final int mailingID, final int companyID) {
        final String rdir_mailinglistquery = "SELECT ml.rdir_domain FROM mailinglist_tbl ml JOIN mailing_tbl m ON ml.mailinglist_id = m.mailinglist_id WHERE ml.deleted = 0 AND m.mailing_id = ?";
        final String rdirdomain = selectObjectDefaultNull(logger, rdir_mailinglistquery, StringRowMapper.INSTANCE, mailingID);
        if (rdirdomain != null) {
            return rdirdomain;
        } else {
            final String rdir_companyquery = "SELECT rdir_domain FROM company_tbl WHERE company_id = ?";
            return select(logger, rdir_companyquery, String.class, companyID);
        }
    }

    @Override
    public boolean isBasicFullTextSearchSupported() {
        if (BASICFULLTEXTSEARCHSUPPORTED_CACHE == null) {
        	BASICFULLTEXTSEARCHSUPPORTED_CACHE = checkIndicesAvailable(logger, "mailing$sname$idx", "mailing$descr$idx");
    	}
    	return BASICFULLTEXTSEARCHSUPPORTED_CACHE;
    }

    @Override
    public boolean isContentFullTextSearchSupported() {
    	if (FULLTEXTSEARCHSUPPORTED_CACHE == null) {
    		FULLTEXTSEARCHSUPPORTED_CACHE = checkIndicesAvailable(logger, "component$emmblock$idx", "dyncontent$content$idx");
    	}
    	return FULLTEXTSEARCHSUPPORTED_CACHE;
    }

    @Override
    public String getFormat(final int type) {
        try {
            String format = selectObjectDefaultNull(logger, "SELECT format FROM date_tbl WHERE type = ?", StringRowMapper.INSTANCE, type);
            if (format == null) {
                logger.error("Query failed for data_tbl: No such type: " + type);
                return "d.M.yyyy";
            } else {
                return format;
            }
        } catch (final Exception e) {
            logger.error("Query failed for data_tbl: " + e.getMessage(), e);
            return "d.M.yyyy";
        }
    }

    @Override
    public int getLastGenstatus(final int mailingID, final char... statuses) {
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
    public boolean hasPreviewRecipients(final int mailingID, final int companyID) {
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
    public boolean hasActions(final int mailingId, final int companyID) {
        final String stmt = "SELECT COUNT(action_id) FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ? AND action_id != 0";
        try {
            final int count = selectInt(logger, stmt, mailingId, companyID);
            return count > 0;
        } catch (final Exception e) {
            logger.error("hasActions: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<Mailing> getTemplates(Admin admin) {
        String sqlStatement = "SELECT mailing_id, shortname FROM mailing_tbl WHERE company_id = ? AND is_template = 1 AND deleted = 0";
        final List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());

        sqlStatement += getTargetRestrictions(admin, params);
        sqlStatement += " ORDER BY shortname";
        final List<Map<String, Object>> tmpList = select(logger, sqlStatement, params.toArray());
        final List<Mailing> result = new ArrayList<>();
        for (final Map<String, Object> row : tmpList) {
            final Mailing newBean = new MailingImpl();
            newBean.setId(((Number) row.get("mailing_id")).intValue());
            newBean.setShortname((String) row.get("shortname"));
            result.add(newBean);
        }
        return result;
    }

    @Override
    public List<MailingBase> getTemplateMailingsByCompanyID(final int companyID) {
        final String sql = "SELECT m.company_id, m.mailing_id, m.shortname, m.content_type, m.description, m.deleted, "
                + " m.mailtemplate_id, m.mailinglist_id, m.mailing_type, m.campaign_id, m.archived, m.target_expression, "
                + " m.split_id, m.creation_date, m.test_lock, m.is_template, m.needs_target, m.openaction_id, m.clickaction_id, "
                + " m.statmail_recp, m.statmail_onerroronly, m.clearance_threshold, m.clearance_email, m.dynamic_template, m.plan_date "
                + joinPostTypeColumns(companyID, "m")
                + " FROM mailing_tbl m"
                + " WHERE m.company_id = ? AND m.is_template = 1 AND m.deleted = 0"
                + " ORDER BY m.shortname";

        return select(logger, sql, new ComMailingToMailingBaseRowMapper(), companyID);
    }

    @Override
    public List<MailingBase> getMailingTemplatesWithPreview(Admin admin, String sort, String direction) {
		String selectPart = "SELECT m.mailing_id, m.shortname, COALESCE(cmp.component_id, 0) AS preview_component, m.description, m.creation_date " + joinPostTypeColumns(admin.getCompanyID(), "m");
		String fromPart = " FROM mailing_tbl m" + " LEFT JOIN component_tbl cmp ON m.mailing_id = cmp.mailing_id AND cmp.comptype = " + MailingComponentType.ThumbnailImage.getCode();
		String wherePart = " WHERE m.company_id = ? AND m.is_template = 1 AND m.deleted = 0";
		
        final List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());
        
		if (isDisabledMailingListsSupported()) {
			wherePart += " AND m.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
	        params.add(admin.getAdminID());
		}
		String orderPart = " ORDER BY ";
		

        wherePart += getTargetRestrictions(admin, params, "m");
        
        if (StringUtils.isBlank(sort)) {
            orderPart += getSortingExpression("m.mailing_id", direction);
        } else {
            orderPart += getSortingExpression(SafeString.getSafeDbColumnName(sort), direction) + ", " + getSortingExpression("m.mailing_id", direction);
        }

        final String sql = selectPart + fromPart + wherePart + orderPart;
        return select(logger, sql, new MailingTemplatesWithPreviewRowMapper(), params.toArray());
    }

    @Override
    public List<MailingBase> getMailingsByStatusE(final int companyID) {
        final List<Map<String, Object>> result = select(logger, "SELECT mail.mailing_id, mail.shortname "
                + joinPostTypeColumns(companyID, "mail")
                + " FROM mailing_tbl mail, maildrop_status_tbl mds WHERE mail.mailing_id = mds.mailing_id AND mail.company_id = ? AND mail.deleted = 0 AND mail.mailing_type = 1 AND mds.status_field = 'E'", companyID);
        final List<MailingBase> returnList = new ArrayList<>();
        for (final Map<String, Object> row : result) {
            final Mailing item = new MailingImpl();
            item.setId(((Number) row.get("mailing_id")).intValue());
            item.setShortname((String) row.get("shortname"));
            item.setOnlyPostType(BooleanUtils.toBoolean(((Number) row.get("isOnlyPostType")).intValue()));
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
            return select(logger, query, IntegerRowMapper.INSTANCE, mailTemplate.getId(), mailTemplate.getCompanyID());
        }
    }

    @Override
    public boolean checkMailingReferencesTemplate(final int templateID, final int companyID) throws Exception {
        if (companyID <= 0) {
            throw new Exception("Invalid companyID");
        } else if (templateID <= 0) {
            throw new Exception("Invalid templateID");
        } else {
            final int isTemplate = selectInt(logger, "SELECT is_template FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", templateID, companyID);
            return isTemplate == 1;
        }
    }

    @Override
    public boolean exist(final int mailingID, final int companyID) {
        return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted = 0", companyID, mailingID) > 0;
    }

    @Override
    public boolean exist(final int mailingID, final int companyID, final boolean isTemplate) {
        return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted = 0 AND is_template = ?", companyID, mailingID, isTemplate ? 1 : 0) > 0;
    }

    @Override
    public List<Mailing> getMailings(final int companyId, final boolean isTemplate) {
        final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type,"
                + " campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id,"
                + " clickaction_id, statmail_recp, statmail_onerroronly, clearance_threshold, clearance_email, dynamic_template, plan_date"
                + " FROM mailing_tbl"
                + " WHERE company_id = ? AND is_template = ? AND deleted = 0";
        return select(logger, sql, new ComMailingToMailingRowMapper(), companyId, isTemplate ? 1 : 0);
    }

    @Override
    public int getMailingOpenAction(final int mailingID, final int companyID) {
        try {
            return selectInt(logger, "SELECT openaction_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
        } catch (final Exception e) {
            logger.error("Error while getting mailing open action ID (mailingID: " + mailingID + ", companyID: " + companyID + ")", e);
            return 0;
        }
    }

    @Override
    public int getMailingClickAction(final int mailingID, final int companyID) {
        try {
            return selectInt(logger, "SELECT clickaction_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
        } catch (final Exception e) {
            logger.error("Error while getting mailing click action ID (mailingID: " + mailingID + ", companyID: " + companyID + ")", e);
            return 0;
        }
    }

    /**
     * returns the mailing-Parameter for the given mailing (only email, no sms or anything else).
     */
    @Override
    public String getEmailParameter(final int mailingID) {
        try {
            return selectWithDefaultValue(logger, "SELECT param FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = 0", String.class, null, mailingID);
        } catch (final Exception e) {
            logger.error("getEmaiLParameter() failed for mailing " + mailingID, e);
            return null;
        }
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(final int companyID) {
        return getLightweightMailings(companyID, 0);
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(final int companyID, final int targetId) {
        final List<java.lang.Object> params = new ArrayList<>();
        String sql = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0";
        params.add(companyID);

        if (targetId > 0) {
            sql += " AND " + DbUtilities.createTargetExpressionRestriction(isOracleDB());
            params.add(targetId);
        }

        return select(logger, sql, LightweightMailingRowMapper.INSTANCE, params.toArray());
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(int companyId, Collection<Integer> mailingIds) {
        String sqlGetMailings = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type " +
                "FROM mailing_tbl " +
                "WHERE company_id = ? AND deleted = 0 AND " + makeBulkInClauseForInteger("mailing_id", mailingIds);

        return select(logger, sqlGetMailings, LightweightMailingRowMapper.INSTANCE, companyId);
    }

    @Override
    public List<LightweightMailing> getLightweightIntervalMailings(Admin admin) {
        final List<java.lang.Object> params = new ArrayList<>();
        String sql = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0 AND mailing_type = ? ";
        params.add(admin.getCompanyID());
        params.add(MailingType.INTERVAL.getCode());

        sql += getTargetRestrictions(admin, params);
        sql += " ORDER BY shortname";
        return select(logger, sql, LightweightMailingRowMapper.INSTANCE, params.toArray());
    }

    /**
     * returns the mailing-type for the given mailing.
     */
    @Override
    public MailingType getMailingType(final int mailingID) throws Exception {
        try {
            return MailingType.fromCode(selectIntWithDefaultValue(logger, "SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", -1, mailingID));
        } catch (final Exception e) {
            throw new Exception("Error getting mailingType for mailing " + mailingID, e);
        }
    }

    @Override
    public Date getMailingPlanDate(final int mailingId, final int companyId) {
        final String sqlGetPlanDate = "SELECT plan_date FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?";
        return selectObjectDefaultNull(logger, sqlGetPlanDate, (rs, i) -> rs.getTimestamp("plan_date"), companyId, mailingId);
    }

    @Override
    public List<Integer> getSampleMailingIDs() {
        return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = 1 AND " +
                "(LOWER(shortname) LIKE '%sample%' " +
                "OR LOWER(shortname) LIKE '%example%' " +
                "OR LOWER(shortname) LIKE '%muster%' " +
                "OR LOWER(shortname) LIKE '%beispiel%') " +
                "AND deleted = 0", IntegerRowMapper.INSTANCE);
    }

    @Override
    public List<LightweightMailing> getMailingsByType(final int mailingType, final int companyID) {
        return getMailingsByType(mailingType, companyID, true);
    }

    @Override
    public List<LightweightMailing> getMailingsByType(final int mailingType, final int companyID, boolean includeInactive) {
        String sql = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl " +
                " WHERE company_id = ? AND deleted = 0 AND is_template = 0 AND mailing_type = ? ";

        if (!includeInactive) {
            sql += " AND work_status = '" + MailingStatus.ACTIVE.getDbKey() + "'";
        }

        return select(logger, sql, LightweightMailingRowMapper.INSTANCE, companyID, mailingType);
    }

    @Override
    public String getMailingName(final int mailingId, final int companyId) {
        final String sqlGetName = "SELECT shortname FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectObjectDefaultNull(logger, sqlGetName, (rs, index) -> rs.getString("shortname"), mailingId, companyId);
    }

    @Override
    public Map<Integer, String> getMailingNames(final Collection<Integer> mailingIds, final int companyId) {
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
        final String sqlQuery = "SELECT m.mailing_id, shortname FROM mailing_tbl m, maildrop_status_tbl mds WHERE status_field = 'W' AND deleted = 0 AND is_template = 0 AND m.company_id = ? AND campaign_id = ? AND archived = 1 AND m.mailing_id = mds.mailing_id ORDER BY senddate DESC, mailing_id DESC";
        return select(logger, sqlQuery, companyID, campaignID);
    }

    @Override
    public int getAnyMailingIdForCompany(final int companyID) {
        if (isOracleDB()) {
            return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND rownum=1", Integer.class, companyID);
        } else {
            return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE company_id = ? LIMIT 1", Integer.class, companyID);
        }
    }

    @Override
    public int getMailinglistId(final int mailingId, final int companyId) {
        return selectInt(logger, "SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", mailingId, companyId);
    }

    @Override
    public String getTargetExpression(final int mailingId, final int companyId) {
        return getTargetExpression(mailingId, companyId, false);
    }

    @Override
    public String getTargetExpression(final int mailingId, final int companyId, final boolean appendListSplit) {
        final String sqlGetTargets = "SELECT target_expression, split_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectObjectDefaultNull(logger, sqlGetTargets, new TargetExpressionMapper(appendListSplit), mailingId, companyId);
    }

    @Override
    public Map<Integer, String> getTargetExpressions(final int companyId, final Set<Integer> mailingIds) {
        if (CollectionUtils.isEmpty(mailingIds) || companyId <= 0) {
            return new HashMap<>();
        }

        final String sqlGetTargets = "SELECT mailing_id, target_expression, -1 FROM mailing_tbl WHERE mailing_id IN (:mailingIds) AND company_id = :companyId ";

        final Map<Integer, String> resultTargetsMap = new HashMap<>();

        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("mailingIds", mailingIds);
        parameters.addValue("companyId", companyId);

        final TargetExpressionMapper expressionMapper = new TargetExpressionMapper(false);
        final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
        jdbcTemplate.query(sqlGetTargets, parameters, rs -> {
            resultTargetsMap.put(rs.getInt("mailing_id"), expressionMapper.mapRow(rs, -1));
        });

        return resultTargetsMap;
    }

    @Override
    public Map<Integer, Set<Integer>> getTargetsUsedInContent(final int companyId, final Set<Integer> mailingIds) {
        if (CollectionUtils.isEmpty(mailingIds) || companyId <= 0) {
            return new HashMap<>();
        }

        final String query = "SELECT c.mailing_id, c.target_id FROM dyn_content_tbl c " +
                "JOIN dyn_name_tbl n ON n.deleted = 0 AND c.dyn_name_id = n.dyn_name_id " +
                "WHERE c.mailing_id IN (:mailingIds) AND c.company_id = :companyId AND c.target_id > 0";

        final MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("mailingIds", mailingIds);
        parameters.addValue("companyId", companyId);


        final NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());

        final List<Entry<Integer, Integer>> selectedTargets = jdbcTemplate.query(query, parameters, (rs, i) -> new DefaultMapEntry<>(rs.getInt("mailing_id"), rs.getInt("target_id")));
        final Map<Integer, Set<Integer>> resultTargets = new HashMap<>();

        for (Entry<Integer, Integer> entry : selectedTargets) {
            Set<Integer> currentTargets = resultTargets.get(entry.getKey());
            if (currentTargets == null) {
                currentTargets = new HashSet<>();
            }

            currentTargets.add(entry.getValue());
            resultTargets.put(entry.getKey(), currentTargets);
        }

        return resultTargets;
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
    public boolean setTargetExpression(final int mailingId, final int companyId, String targetExpression) throws TooManyTargetGroupsInMailingException {
        try {
            if (validateTargetExpression(targetExpression)) {
            	targetExpression = checkTargetExpressionForALTG(companyId, targetExpression);
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
                    return "(" + expression + ")&" + splitId;
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
    public boolean isAdvertisingContentType(final int companyId, final int mailingId) {
        if (companyId <= 0 || mailingId <= 0) {
            return false;
        }

        final String fieldClause = String.format("COALESCE(content_type, '%s') content_type_value", ADVERTISING_TYPE);
        final String query = "SELECT " + fieldClause + " FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?";
        final String contentType = selectWithDefaultValue(logger, query, String.class, "", companyId, mailingId);

        return StringUtils.isNotEmpty(contentType) && ADVERTISING_TYPE.equalsIgnoreCase(contentType);
    }

    @Override
    public boolean isTextVersionRequired(final int companyId, final int mailingId) {
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
            newBean.put("mailing_type", resultSet.getInt("mailing_type"));
            newBean.put("mailinglist", resultSet.getString("mailinglist"));
            newBean.put("senddate", resultSet.getTimestamp("senddate"));
            newBean.put("creationdate", resultSet.getTimestamp("creationdate"));
            newBean.put("changedate", resultSet.getTimestamp("changedate"));
            newBean.put("isgrid", resultSet.getInt("is_grid") > 0);
            newBean.put("usedInCM", resultSet.getInt("workflow_id") > 0);
            newBean.put("isOnlyPostType", resultSet.getInt("isOnlyPostType") > 0);

            if (DbUtilities.resultsetHasColumn(resultSet, "templatename")) {
                newBean.put("templateName", resultSet.getString("templatename"));
            }

            if (DbUtilities.resultsetHasColumn(resultSet, "subject")) {
                newBean.put("subject", resultSet.getString("subject"));
            }

            if (DbUtilities.resultsetHasColumn(resultSet, "archive")) {
                newBean.put("archive", resultSet.getString("archive"));
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

    @Override
    public Date getMailingSendDate(final int companyID, final int mailingID) {
        return select(logger, "SELECT MIN(mintime) FROM mailing_account_sum_tbl WHERE company_id = ? and mailing_id = ? AND status_field IN (?, ?, ?)", Date.class, companyID, mailingID, MaildropStatus.WORLD.getCodeString(), MaildropStatus.ACTION_BASED.getCodeString(), MaildropStatus.DATE_BASED.getCodeString());
    }

    @Override
    public List<LightweightMailing> listAllActionBasedMailingsForMailinglist(int companyID, int mailinglistID) {
        String query = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ? AND mailing_type = ? AND deleted=0";
        return select(logger, query, LightweightMailingRowMapper.INSTANCE, companyID, mailinglistID, MailingType.ACTION_BASED.getCode());
    }

    @Override
    public LightweightMailing getLightweightMailing(final int companyID, final int mailingID) throws MailingNotExistException {
        final String sql = "SELECT mailing_id, description, shortname, company_id, mailing_type, work_status, content_type FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";

        final List<LightweightMailing> list = select(logger, sql, LightweightMailingRowMapper.INSTANCE, mailingID, companyID);

        if (list.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Unable to load mailing (mailing ID %d, company ID %d)", mailingID, companyID));
            }

            throw new MailingNotExistException(companyID, mailingID);
        } else {
            return list.get(0);
        }
    }

    @Override
    public boolean tryToLock(int mailingId, int adminId, int companyId, long duration, TimeUnit durationUnit) {
        long durationSeconds = durationUnit.toSeconds(duration);

        String sqlCheckExists = "SELECT COUNT(*) FROM mailing_tbl WHERE mailing_id = ? AND company_id = ? AND deleted = 0";
        String sqlTryToLock = "UPDATE mailing_tbl SET locking_admin_id = ?, locking_expire_timestamp = " + getNewLockingTimestampExpression(durationSeconds) +
                " WHERE mailing_id = ? AND company_id = ? AND " +
                "(locking_admin_id IS NULL OR locking_expire_timestamp IS NULL OR locking_admin_id = ? OR locking_expire_timestamp < CURRENT_TIMESTAMP)";

        if (selectInt(logger, sqlCheckExists, mailingId, companyId) > 0) {
            return update(logger, sqlTryToLock, adminId, mailingId, companyId, adminId) > 0;
        } else {
            throw new MailingNotExistException(companyId, mailingId);
        }
    }

    @Override
    public int getMailingLockingAdminId(int mailingId, int companyId) {
        String query = "SELECT locking_admin_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectInt(logger, query, mailingId, companyId);
    }

    @Override
    public List<LightweightMailing> getUnsetMailingsForRootTemplate(int companyId, int templateId) {
        String query = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type " +
                "FROM mailing_tbl m " +
                "WHERE company_id = ? " +
                "AND mailing_id IN (" +
                "	SELECT mailing_id FROM grid_template_tbl WHERE company_id = ? AND parent_template_id = ? AND mailing_id > 0 AND deleted = 0" +
                ") AND NOT EXISTS (" +
                "   SELECT 1 FROM maildrop_status_tbl WHERE status_field = ? AND mailing_id = m.mailing_id" +
                ")";

        return select(logger, query, LightweightMailingRowMapper.INSTANCE, companyId, companyId, templateId, MaildropStatus.WORLD.getCodeString());
    }

    protected void insertMailing(final int companyId, final Mailing mailing, final String autoUrl) {
        if (mailing.getSplitID() < 0) {
            throw new IllegalArgumentException("SplitId should not be negative!");
        }
        
        mailing.setTargetExpression(checkTargetExpressionForALTG(companyId, mailing.getTargetExpression()));

        if (isOracleDB()) {
            mailing.setId(selectInt(logger, "SELECT mailing_tbl_seq.NEXTVAL FROM DUAL"));
            final Object[] params = new Object[]{
                    mailing.getId(),
                    companyId,
                    mailing.isGridMailing() ? 1 : 0,
                    mailing.getCampaignID(),
                    mailing.getShortname(),
                    mailing.getMailingContentType() == null ? null : mailing.getMailingContentType().name(),
                    mailing.getDescription(),
                    mailing.getMailingType().getCode(),
                    BooleanUtils.toInteger(mailing.isIsTemplate()),
                    BooleanUtils.toInteger(mailing.getNeedsTarget()),
                    mailing.getMailTemplateID(),
                    mailing.getMailinglistID(),
                    mailing.getDeleted(),
                    mailing.getArchived(),
                    mailing.getLocked(),
                    mailing.getTargetExpression(),
                    mailing.getSplitID(),
                    BooleanUtils.toInteger(mailing.getUseDynamicTemplate()),
                    mailing.getOpenActionID(),
                    mailing.getClickActionID(),
                    new Date(),
                    mailing.getStatusmailRecipients(),
                    BooleanUtils.toInteger(mailing.isStatusmailOnErrorOnly()),
                    mailing.getPlanDate(),
                    autoUrl,
            };
            update(logger, "INSERT INTO mailing_tbl (mailing_id, company_id, is_grid, campaign_id, shortname, content_type, description, mailing_type, is_template, needs_target, mailtemplate_id, mailinglist_id, deleted, archived, test_lock, target_expression, split_id, work_status, dynamic_template, openaction_id, clickaction_id, creation_date, statmail_recp, statmail_onerroronly, plan_date, auto_url)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '" + MailingStatus.NEW.getDbKey() + "', ?, ?, ?, ?, ?, ?, ?, ?)", params);
        } else {
            final int newID = insertIntoAutoincrementMysqlTable(logger, "mailing_id", "INSERT INTO mailing_tbl (company_id, is_grid, campaign_id, shortname, content_type, description, mailing_type, is_template, needs_target, mailtemplate_id, mailinglist_id, deleted, archived, test_lock, target_expression, split_id, work_status, dynamic_template, openaction_id, clickaction_id, creation_date, statmail_recp, statmail_onerroronly, plan_date, auto_url)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '" + MailingStatus.NEW.getDbKey() + "', ?, ?, ?, ?, ?, ?, ?, ?)",
                    companyId,
                    mailing.isGridMailing() ? 1 : 0,
                    mailing.getCampaignID(),
                    mailing.getShortname(),
                    mailing.getMailingContentType() == null ? null : mailing.getMailingContentType().name(),
                    mailing.getDescription(),
                    mailing.getMailingType().getCode(),
                    BooleanUtils.toInteger(mailing.isIsTemplate()),
                    BooleanUtils.toInteger(mailing.getNeedsTarget()),
                    mailing.getMailTemplateID(),
                    mailing.getMailinglistID(),
                    mailing.getDeleted(),
                    mailing.getArchived(),
                    mailing.getLocked(),
                    mailing.getTargetExpression(),
                    mailing.getSplitID(),
                    BooleanUtils.toInteger(mailing.getUseDynamicTemplate()),
                    mailing.getOpenActionID(),
                    mailing.getClickActionID(),
                    new Date(),
                    mailing.getStatusmailRecipients(),
                    BooleanUtils.toInteger(mailing.isStatusmailOnErrorOnly()),
                    mailing.getPlanDate(),
                    autoUrl);

            mailing.setId(newID);
        }
    }

    protected void performMailingUpdate(final int companyId, final Mailing mailing, final String autoUrl) {
        if (mailing.getSplitID() < 0) {
            throw new IllegalArgumentException("SplitId should not be negative!");
        }
        
        mailing.setTargetExpression(checkTargetExpressionForALTG(companyId, mailing.getTargetExpression()));
        
        final Object[] params = new Object[]{
                mailing.getCampaignID(),
                mailing.getShortname(),
                mailing.getMailingContentType() == null ? null : mailing.getMailingContentType().name(),
                mailing.getDescription(),
                mailing.getMailingType().getCode(),
                mailing.isIsTemplate() ? 1 : 0,
                mailing.getNeedsTarget() ? 1 : 0,
                mailing.getMailTemplateID(),
                mailing.getMailinglistID(),
                mailing.getDeleted(),
                mailing.getArchived(),
                mailing.getLocked(),
                mailing.getTargetExpression(),
                mailing.getSplitID(),
                mailing.getUseDynamicTemplate() ? 1 : 0,
                mailing.getOpenActionID(),
                mailing.getClickActionID(),
                mailing.getStatusmailRecipients(),
                mailing.isStatusmailOnErrorOnly() ? 1 : 0,
                mailing.getPlanDate(),
                autoUrl,
                mailing.getId(),
                companyId
        };
        update(logger, "UPDATE mailing_tbl SET campaign_id = ?, shortname = ?, content_type = ?, description = ?, mailing_type = ?, is_template = ?, needs_target = ?, mailtemplate_id = ?, mailinglist_id = ?, deleted = ?, archived = ?, test_lock = ?, target_expression = ?, split_id = ?, dynamic_template = ?, change_date = "
		        + "CURRENT_TIMESTAMP, openaction_id = ?, clickaction_id = ?, statmail_recp = ?, statmail_onerroronly = ?, plan_date = ?, auto_url = ? WHERE mailing_id = ? AND company_id = ?", params);
    }

     protected Set<String> getMailingTblSelectableFields() {
       return new HashSet<>(SEPARATE_MAILING_FIELDS);
    }

    private String getMailingSqlSelectFields(final String prefix) {
        Set<String> fields = getMailingTblSelectableFields();
        if (StringUtils.isNotBlank(prefix)) {
            return fields.stream()
                    .map(column -> prefix + column)
                    .collect(Collectors.joining(", "));
        }

        return StringUtils.join(fields, ", ");
    }

    protected ComMailingRowMapper getRowMapper() {
        return MAILING_ROW_MAPPER;
    }

    private String getNewLockingTimestampExpression(long durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Locking duration must be >= 1 second");
        }

        if (isOracleDB()) {
            return "(CURRENT_TIMESTAMP + INTERVAL '" + durationSeconds + "' SECOND)";
        } else {
            return "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL " + durationSeconds + " SECOND) ";
        }
    }

    private void performMailingSave(final int companyId, final Mailing mailing, final String autoUrl) {
        // !=0 means we have already a mailing id. We got that by a sequence from the DB
        if (mailing.getId() != 0) {
            performMailingUpdate(companyId, mailing, autoUrl);
        } else {
            // ==0 means, we have a new mailing, the sequence returns us a mailing-id from the db.
            insertMailing(companyId, mailing, autoUrl);
        }
    }

    @Override
    public String getEmailSubject(int companyID, int mailingID) throws Exception {
        return ((MediatypeEmail) mediatypesDao.loadMediatypes(mailingID, companyID).get(MediaTypes.EMAIL.getMediaCode())).getSubject();
    }

    @Override
    public MailingContentType getMailingContentType(int companyID, int mailingId) {
        // If this mailing was deleted oder does not exist content_type will be handled as null
        String contentTypeString = selectWithDefaultValue(logger, "SELECT content_type FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted <= 0", String.class, null, companyID, mailingId);
        if (contentTypeString != null) {
            try {
                return MailingContentType.getFromString(contentTypeString);
            } catch (Exception e) {
                logger.error("Invalid MailingContentType for mailing " + companyID + "/" + mailingId);
                return null;
            }
        } else {
            return null;
        }
    }

    private Map<String, ComTrackableLink> getTrackableLinksMap(final int companyId, final int mailingId) {
        final List<ComTrackableLink> trackableLinks = trackableLinkDao.getTrackableLinks(companyId, mailingId);
        final Map<String, ComTrackableLink> map = new HashMap<>(trackableLinks.size());

        for (final ComTrackableLink trackableLink : trackableLinks) {
            map.put(trackableLink.getFullUrl(), trackableLink);
        }

        return map;
    }

    @Override
    public boolean deleteMailingsByCompanyIDReally(int companyID) {
        // Do nothing in OpenEMM
        return true;
    }

    @Override
    public boolean isActiveIntervalMailing(int companyID, int mailingID) {
        return selectInt(logger, "SELECT COUNT(mailing_id) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND mailing_type = ? AND work_status = ?", companyID, mailingID, MailingType.INTERVAL.getCode(), MailingStatus.ACTIVE.getDbKey()) > 0;
    }

    @Override
    public boolean resumeDateBasedSending(int mailingId) {
        return update(logger, "UPDATE rulebased_sent_tbl SET clearance = 1, clearance_origin = ?, clearance_change = CURRENT_TIMESTAMP WHERE mailing_id = ?",
                AgnUtils.getHostName(), mailingId) > 0;
    }

    @Override
    public boolean isThresholdClearanceExceeded(int mailingId) {
        return selectInt(logger, "SELECT COUNT(clearance) FROM rulebased_sent_tbl WHERE mailing_id = ? AND clearance = 0", mailingId) > 0;
    }

	@Override
	public void removeApproval(int mailingID, int companyID) {
		final String sql = "UPDATE mailing_tbl SET test_lock=? WHERE mailing_id=? AND company_id=?";
		update(logger, sql, 1, mailingID, companyID);
	}

	@Override
	public boolean isApproved(int mailingId, int companyId) {
        String query = "SELECT test_lock FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectIntWithDefaultValue(logger, query, 1, mailingId, companyId) == 0;
    }
	
	/**
	 * Only used for testing purposes
	 */
	public void resetFulltextsearchSupportedCache() {
		FULLTEXTSEARCHSUPPORTED_CACHE = null;
		BASICFULLTEXTSEARCHSUPPORTED_CACHE = null;
	}

	/**
	 * EMM-9779: Check the TargetExpression for any ALTG targetgroups included in the group of standard targetgroups
	 * Allowed examples "(ALTG1|ALTG2)&(TG1|TG2|TG3)" or "(ALTG1|ALTG2)&(TG1&TG2&TG3)"
	 * Invalid examples "(ALTG1|ALTG2)&(TG1|ALTG2|TG3)" or "(ALTG1|ALTG2)&(TG1&ALTG2&TG3)" or "(ALTG1)&(TG1&ALTG2&TG3)"
	 */
    protected String checkTargetExpressionForALTG(int companyID, String targetExpression) {
		if (targetExpression != null) {
			TargetLightsOptions options = TargetLightsOptions.builder().setCompanyId(companyID).setAltgMode(AltgMode.ALTG_ONLY).build();
			List<Integer> altgTargetIDs = targetDao.getTargetLightsBySearchParameters(options).stream().map(TargetLight::getId).collect(Collectors.toList());
			
			targetExpression = targetExpression.trim();
			int bracketLevel = 0;
			boolean withinFirstTopLevelBrackets = true;
			StringBuilder leadingExpressionPart = new StringBuilder();
			StringBuilder trailingExpressionPart = new StringBuilder();
			for (int i = 0; i < targetExpression.length(); i++) {
				if (withinFirstTopLevelBrackets) {
					leadingExpressionPart.append(targetExpression.charAt(i));
				} else {
					trailingExpressionPart.append(targetExpression.charAt(i));
				}
					
				if ('(' == targetExpression.charAt(i)) {
					bracketLevel++;
				} else if (')' == targetExpression.charAt(i)) {
					bracketLevel--;
					if (bracketLevel == 0) {
						withinFirstTopLevelBrackets = false;
					}
				}
			}
			
			if (trailingExpressionPart.length() > 0) {
				String trailingExpressionPartString = trailingExpressionPart.toString();
				for (Integer altgTargetID : altgTargetIDs) {
					if (trailingExpressionPartString.contains(Integer.toString(altgTargetID))) {
						int previousLength = trailingExpressionPartString.length();
						trailingExpressionPartString = trailingExpressionPartString
								.replace("&(" + altgTargetID + ")", "")
								.replace("(" + altgTargetID + "|", "(")
								.replace("|" + altgTargetID + "|", "|")
								.replace("|" + altgTargetID + ")", ")")
								.replace("(" + altgTargetID + "&", "(")
								.replace("&" + altgTargetID + "&", "&")
								.replace("&" + altgTargetID + ")", ")")
								.replace("&" + altgTargetID + "|", "|")
								.replace("|" + altgTargetID + "&", "|");
						if (trailingExpressionPartString.length() < previousLength) {
							logger.error("Found and replaced ALTG targetgroup '" + altgTargetID + "' within targetExpression '" + targetExpression + "'");
						}
					}
				}
				return leadingExpressionPart.toString() + trailingExpressionPartString;
			} else {
				return targetExpression;
			}
		} else {
			return targetExpression;
		}
	}

	@Override
	public List<Map<String, Object>> getMailingsMarkedAsDeleted(int companyID, Date deletedMailingExpire) {
		return select(logger, "SELECT mailing_id FROM mailing_tbl WHERE deleted = 1 AND company_id = ? and change_date < ?", companyID, deletedMailingExpire);
	}

	@Override
	public void deleteOutdatedMailingData(int mailingID) {
		update(logger, "DELETE FROM mailing_mt_tbl WHERE mailing_id = ?", mailingID);
		update(logger, "DELETE FROM serverprio_tbl WHERE mailing_id = ?", mailingID);
		update(logger, "DELETE FROM mailing_info_tbl WHERE mailing_id= ?", mailingID);
		update(logger, "DELETE FROM mailing_grid_tbl WHERE mailing_id = ?", mailingID);
		update(logger, "DELETE FROM mailing_import_lock_tbl WHERE mailing_id = ?", mailingID);
	}

	@Override
	public void markMailingAsDataDeleted(int mailingID) {
		update(logger, "UPDATE mailing_tbl SET deleted = 2 WHERE mailing_id = ?", mailingID);
	}

	/**
	 * Only store changed shortname, description an archiveid.
	 * Those are the values that are allowed for change even after a mailing was delievered
	 */
	@Override
	public boolean saveMailingDescriptiveData(Mailing mailing) {
		return update(logger, "UPDATE mailing_tbl SET shortname = ?, description = ?, campaign_id = ? WHERE company_id = ? AND mailing_id = ?",
			mailing.getShortname(), mailing.getDescription(), mailing.getCampaignID(), mailing.getCompanyID(), mailing.getId()) > 0;
	}
}
