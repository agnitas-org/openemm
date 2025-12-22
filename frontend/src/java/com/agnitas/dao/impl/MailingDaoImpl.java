/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import static com.agnitas.util.DbUtilities.resultsetHasColumn;
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

import com.agnitas.beans.Admin;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingBase;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.MailingContentType;
import com.agnitas.beans.MailingSendStatus;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.RdirMailingData;
import com.agnitas.beans.TargetLight;
import com.agnitas.beans.impl.DynamicTagContentImpl;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.beans.impl.MailingBaseImpl;
import com.agnitas.beans.impl.MailingImpl;
import com.agnitas.beans.impl.MailingSendStatusImpl;
import com.agnitas.beans.impl.RdirMailingDataImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.dao.UndoDynContentDao;
import com.agnitas.dao.UndoMailingComponentDao;
import com.agnitas.dao.UndoMailingDao;
import com.agnitas.dao.exception.TooBroadSearchException;
import com.agnitas.dao.impl.mapper.DateRowMapper;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.LightweightMailingRowMapper;
import com.agnitas.dao.impl.mapper.LightweightMailingWithMailinglistRowMapper;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingComparisonFilter;
import com.agnitas.emm.core.calendar.beans.MailingPopoverInfo;
import com.agnitas.emm.core.commons.database.DatabaseInformation;
import com.agnitas.emm.core.commons.database.fulltext.FulltextSearchQueryGenerator;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.dashboard.bean.ScheduledMailing;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.MailingDataException;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailing.bean.LightweightMailingWithMailingList;
import com.agnitas.emm.core.mailing.bean.MailingArchiveEntry;
import com.agnitas.emm.core.mailing.bean.MailingDto;
import com.agnitas.emm.core.mailing.dao.MailingDaoOptions;
import com.agnitas.emm.core.mailing.dao.MailingParameterDao;
import com.agnitas.emm.core.mailing.enums.MailingAdditionalColumn;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.mailing.exception.MailingSaveException;
import com.agnitas.emm.core.mailing.forms.MailingTemplateSelectionFilter;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.MailingPropertyCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.MailingStatusCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SendDateCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SentAfterCondition;
import com.agnitas.emm.core.mailing.service.ListMailingCondition.SentBeforeCondition;
import com.agnitas.emm.core.mailing.service.ListMailingFilter;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.dao.MediatypesDao;
import com.agnitas.emm.core.target.AltgMode;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetLightsOptions;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.FulltextSearchInvalidQueryException;
import com.agnitas.util.FulltextSearchQueryException;
import com.agnitas.util.ParameterParser;
import com.agnitas.util.SqlPreparedStatementManager;
import com.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * TODO: Check concatenated SQl statements for sql-injection possibilities,
 * replace where possible by real ?-parameters, also for better db performance
 */
public class MailingDaoImpl extends PaginatedBaseDaoImpl implements MailingDao {

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

    private static final MailingRowMapper MAILING_ROW_MAPPER = new MailingRowMapper();

    private static final String ADVERTISING_TYPE = "advertising";
    private static final String DATE_ORDER_KEY = "date";
    private static final String SUBJECT_COL_NAME = "subject";
    protected static final String MAILING_ALIAS = "mailing";

    private static final PartialMailingBaseRowMapper PARTIAL_MAILING_BASE_ROW_MAPPER = new PartialMailingBaseRowMapper();

    private Boolean isGridTemplateSupported;

    protected UndoMailingDao undoMailingDao;
    protected MailingComponentDao mailingComponentDao;

    /** DAO accessing target groups. */
    protected TargetDao targetDao;
    protected UndoMailingComponentDao undoMailingComponentDao;
    protected UndoDynContentDao undoDynContentDao;
    protected TrackableLinkDao trackableLinkDao;
    protected DatabaseInformation databaseInformation;
    protected ConfigService configService;
    private MaildropService maildropService;
    protected DynamicTagDao dynamicTagDao;
    protected MediatypesDao mediatypesDao;
    private FulltextSearchQueryGenerator fulltextSearchQueryGenerator;
    private MailingParameterDao mailingParameterDao;

    // ---------------------------------------------------------------------------------------------------------------------------------------
    // dependency injection code

    /**
     * Injection-setter for MediatypesDao.
     *
     * @param mediatypesDao - DAO
     */
    public void setMediatypesDao(MediatypesDao mediatypesDao) {
        this.mediatypesDao = mediatypesDao;
    }

    /**
     * Injection-setter for UndoMailingDao.
     *
     * @param undoMailingDao - DAO
     */
    public void setUndoMailingDao(UndoMailingDao undoMailingDao) {
        this.undoMailingDao = undoMailingDao;
    }

    /**
     * Injection-setter for UndoMailingComponentDao.
     *
     * @param undoMailingComponentDao - DAO
     */
    public void setUndoMailingComponentDao(UndoMailingComponentDao undoMailingComponentDao) {
        this.undoMailingComponentDao = undoMailingComponentDao;
    }

    /**
     * Injection-setter for UndoDynContentDao.
     *
     * @param undoDynContentDao - DAO
     */
    public void setUndoDynContentDao(UndoDynContentDao undoDynContentDao) {
        this.undoDynContentDao = undoDynContentDao;
    }

    public void setConfigService(ConfigService service) {
        this.configService = service;
    }

    public void setMailingParameterDao(MailingParameterDao dao) {
    	this.mailingParameterDao = Objects.requireNonNull(dao, "mailing parameter dao");
    }

    public void setTrackableLinkDao(TrackableLinkDao trackableLinkDao) {
        this.trackableLinkDao = trackableLinkDao;
    }

    /**
     * Injection-setter for TargetDao.
     *
     * @param targetDao DAO
     */
    public void setTargetDao(TargetDao targetDao) {
        this.targetDao = targetDao;
    }

    public void setDatabaseInformation(DatabaseInformation info) {
        this.databaseInformation = info;
    }

    /**
     * Injection-setter for MailingComponentDao.
     *
     * @param componentDao DAO
     */
    public void setMailingComponentDao(MailingComponentDao componentDao) {
        this.mailingComponentDao = componentDao;
    }

    public void setFulltextSearchQueryGenerator(FulltextSearchQueryGenerator fulltextSearchQueryGenerator) {
        this.fulltextSearchQueryGenerator = fulltextSearchQueryGenerator;
    }

    public void setDynamicTagDao(DynamicTagDao dynamicTagDao) {
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
                    } else if (isPostgreSQL()) {
                        sql += " AND b.senddate > CURRENT_TIMESTAMP - INTERVAL '" + numDays + " DAYS'";
                    } else {
                        sql += " AND b.senddate > DATE_ADD(now(), interval " + (-numDays) + " day) ";
                    }
                }
            }

            if (adminId > 0 && configService.isDisabledMailingListsSupported()) {
                sql += " AND a.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
                parameters.add(adminId);
            }

            sql += " AND a.mailing_id = b.mailing_id ORDER BY a.mailing_id DESC";
        } else {
            sql = "SELECT " + getMailingSqlSelectFields("") + " FROM mailing_tbl WHERE company_id = ? ";
            parameters.add(companyID);

            if (adminId > 0 && configService.isDisabledMailingListsSupported()) {
                sql += " AND mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
                parameters.add(adminId);
            }

            sql += " ORDER BY mailing_id DESC";
        }

        // Check, if we have to return all values.
        // If count <= 0 then all mailings are returned.
        if (count > 0) {
            sql = addRowLimit(sql, count);
        }

        try {
            return select(sql, getRowMapper(), parameters.toArray());
        } catch (Exception e) {
            return new LinkedList<>();
        }
    }

    @Override
	public List<Mailing> listMailings(int companyId, boolean template, ListMailingFilter filter) {
    	if (filter == null) {
    		return getMailings(companyId, template);
    	}

    	final List<Object> paramsList = new ArrayList<>();

    	final StringBuffer sql = new StringBuffer("SELECT * FROM mailing_tbl m WHERE m.company_id = ? AND m.is_template = ? AND m.deleted=0");
    	paramsList.add(companyId);
    	paramsList.add(template ? 1 : 0);

    	for (final MailingPropertyCondition c : filter.listMailingPropertyConditions()) {
    		if (c instanceof MailingStatusCondition cond) {
    			if(!cond.getStatusList().isEmpty()) {
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
				throw new IllegalArgumentException(msg);
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
    				throw new IllegalArgumentException(msg);
    			}
    		}

    		sql.append(")");
    	}

        return select(sql.toString(), new MailingLighterRowMapper(), paramsList.toArray());
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

        return select(sql, LightweightMailingRowMapper.INSTANCE, parameters.toArray());
    }

    @Override
    public List<Integer> getClassicTemplatesByName(String name, int companyId) {
        return select("SELECT mailing_id FROM mailing_tbl " +
                        "WHERE shortname = ? AND company_id = ? AND is_template = 1 AND deleted = 0",
                IntegerRowMapper.INSTANCE, name, companyId);
    }

    @Override
    public LightweightMailing getMailingByName(String name, int companyId) {
        return selectObject(addRowLimit("""
            SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type
            FROM mailing_tbl WHERE shortname = ? AND company_id = ? AND is_template = 0 AND deleted = 0
            ORDER BY mailing_id""", 1), LightweightMailingRowMapper.INSTANCE, name, companyId);
    }

    private String getDisabledMailinglistRestriction(int adminId, List<Object> queryParams, String tableAlias) {
        if (adminId > 0 && configService.isDisabledMailingListsSupported()) {
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

        return select(addSortingToQuery(unsentSql, sortField, sortDirection),
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

        return select(addSortingToQuery(sentSql, sortField, sortDirection),
        		LightweightMailingRowMapper.INSTANCE, sentParams.toArray());
    }

    @Override
    public List<Map<String, Object>> getMailings(int companyId, String commaSeparatedMailingIds) {
        final StringBuilder builder = new StringBuilder();
        builder.append("SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, statmail_onerroronly, clearance_threshold, clearance_email, dynamic_template, plan_date, work_status FROM mailing_tbl ")
                .append("WHERE company_id = ? ")
                .append("  AND mailing_id IN (").append(commaSeparatedMailingIds).append(") ")
                .append("ORDER BY mailing_id DESC");
        return select(builder.toString(), companyId);
    }

    @Override
    public List<Map<String, Object>> getMailingsNamesByStatus(Admin admin, List<MailingType> mailingTypes, String workStatus, String mailingStatus, boolean takeMailsForPeriod, String sort, String order) {
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
                } else if (isPostgreSQL()) {
                    queryBuilder.append(" AND b.senddate > CURRENT_TIMESTAMP - INTERVAL '").append(numDays).append(" DAYS'");
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

        return select(queryBuilder.toString(), parameters.toArray());
    }

	private String createMailingTypeCodeListString(List<MailingType> mailingTypes) {
		StringBuilder listStringBuilder = new StringBuilder();
		for (int i = 0; i < mailingTypes.size(); i++) {
        	if (i > 0) {
        		listStringBuilder.append(", ");
        	}
        	listStringBuilder.append(mailingTypes.get(i).getCode());
        }
		return listStringBuilder.toString();
	}

    @Override
    public List<Integer> findTargetDependentMailings(int targetGroupId, int companyId) {
        String query = "SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND (" + DbUtilities.createTargetExpressionRestriction(dataSource) + ")";
        return select(query, IntegerRowMapper.INSTANCE, companyId, targetGroupId);
    }

    @Override
    public List<Integer> filterNotSentMailings(List<Integer> mailings) {
        String mailingIdsInClause = makeBulkInClauseForInteger("mailing_id", mailings);

        String query = "SELECT mailing_id FROM mailing_tbl WHERE " + mailingIdsInClause + " AND work_status != ?";
        return select(query, IntegerRowMapper.INSTANCE, MailingStatus.SENT.getDbKey());
    }

    /**
     * This method returns all Mailings of the given CompanyID in a LinkedList
     * <p>
     * Attention: use only for Demo Account Service methods
     */
    @Override
    public List<Integer> getAllMailings(int companyID) {
        return select("SELECT mailing_id FROM mailing_tbl WHERE company_id = ?", IntegerRowMapper.INSTANCE, companyID);
    }

    @Override
    public Mailing getMailing(int mailingID, int companyID) {
        return getMailing(mailingID, companyID, true, false);
    }

    @Override
    public Mailing getMailing(int mailingId, int companyId, boolean includeDependencies) {
        return getMailing(mailingId, companyId, includeDependencies, false);
    }

    @Override
    public Mailing getMailingWithDeletedDynTags(int mailingID, int companyID) {
        return getMailing(mailingID, companyID, true, true);
    }

	private Mailing getMailing(int mailingID, int companyID, boolean includeDependencies, boolean includeDeletedDynTags) {
		if (companyID <= 0 || mailingID <= 0) {
			throw new MailingNotExistException(companyID, mailingID);
		} else {
			try {
				String sql = "SELECT " + getMailingSqlSelectFields("a.") + ", a.send_date AS senddate"
					+ " FROM mailing_tbl a"
					+ " WHERE a.company_id = ? AND a.mailing_id = ? AND a.deleted <= 0 ORDER BY a.send_date";

				Mailing mailing = selectObjectDefaultNull(addRowLimit(sql, 1), getRowMapper(), companyID, mailingID);

				if (mailing == null) {
					throw new MailingNotExistException(companyID, mailingID);
				} else if (includeDependencies) {
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
					mailing.setTrackableLinks(trackableLinkDao.getTrackableLinksMap(mailingID, companyID, false));

					mailing.setParameters(this.mailingParameterDao.getMailingParameters(companyID, mailingID));

					final List<MaildropEntry> maildropEntryList = maildropService.getMaildropStatusEntriesForMailing(companyID, mailingID);
					mailing.setMaildropStatus(new HashSet<>(maildropEntryList));
				}
                return mailing;
            } catch (MailingNotExistException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Error loading mailing ID {}", mailingID, e);
				throw new MailingNotExistException(companyID, mailingID);
			}
		}
	}

    private void saveComponents(int companyID, int mailingID, Map<String, MailingComponent> components, boolean errorTolerant) {
        if (MapUtils.isEmpty(components)) {
            return;
        }

        mailingComponentDao.setUnPresentComponentsForMailing(mailingID, new ArrayList<>(components.values()));

        for (MailingComponent mailingComponent : components.values()) {
            try {
                mailingComponent.setCompanyID(companyID);
                mailingComponent.setMailingID(mailingID);

                mailingComponentDao.saveMailingComponent(mailingComponent);
            } catch(Exception e) {
                logger.error(String.format("Error saving mailing component %d ('%s') or mailing %d", mailingComponent.getId(), mailingComponent.getComponentName(), mailingComponent.getMailingID()), e);

                if (!errorTolerant) {
                    throw new RuntimeException(String.format("Error saving mailing component %d ('%s') or mailing %d", mailingComponent.getId(), mailingComponent.getComponentName(), mailingComponent.getMailingID()), e);
                }
            }
        }
    }

    @Override
    public int saveUndoMailing(int mailingId, int adminId) {
        return undoMailingDao.saveUndoData(mailingId, new Date(), adminId);
    }

    @Override
    public int saveMailing(Mailing mailing, boolean preserveTrackableLinks) {
        try {
            // Returns mailingId (existing or brand new one).
            return saveMailingWithoutTransaction(mailing, preserveTrackableLinks, true, false);
        } catch (Exception e) {
            // Do nothing (formerly the transaction was rolled back)
            return 0;
        }
    }

    @Override
    public int saveMailing(Mailing mailing, boolean preserveTrackableLinks, boolean errorTolerant, boolean removeUnusedContent) {
        return saveMailingWithoutTransaction(mailing, preserveTrackableLinks, errorTolerant, removeUnusedContent);
    }

    @DaoUpdateReturnValueCheck
    private int saveMailingWithoutTransaction(Mailing mailing, boolean preserveTrackableLinks, boolean errorTolerant, boolean removeUnusedContent) {
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
        if (companyId == 0) {
            throw new IllegalArgumentException("MailingDaoImpl is trying to create a mailing with companyID = 0!");
        }

        try {
            final String autoUrl = selectStringDefaultNull("SELECT auto_url FROM mailinglist_tbl WHERE deleted = 0 AND mailinglist_id = ?", mailing.getMailinglistID());

            performMailingSave(companyId, mailing, autoUrl);

            maildropService.saveMaildropEntries(companyId, mailing.getId(), mailing.getMaildropStatus());
            updateClearanceLastSendDate(mailing.getId());
            trackableLinkDao.batchSaveTrackableLinks(companyId, mailing.getId(), mailing.getTrackableLinks(), !preserveTrackableLinks);
            saveComponents(companyId, mailing.getId(), mailing.getComponents(), errorTolerant);
            mediatypesDao.saveMediatypes(companyId, mailing.getId(), mailing.getMediatypes());
            dynamicTagDao.saveDynamicTags(mailing, mailing.getDynTags(), removeUnusedContent);
            this.mailingParameterDao.updateParameters(companyId, mailing.getId(), mailing.getParameters(), 0);
        } catch (Exception e) {
            String errorMessage = "Error saving mailing (%d)".formatted(mailing.getId());
            logger.error(errorMessage, e);
            throw new MailingSaveException(errorMessage, e);
        }

        return mailing.getId();
    }

    private void updateClearanceLastSendDate(int mailingId) {
        String sendDate = "DATE(mds.senddate)";
        String today = "DATE(CURRENT_TIMESTAMP)";
        if (isOracleDB()) {
            sendDate = "TRUNC(mds.senddate)";
            today = "TRUNC(CURRENT_TIMESTAMP)";
        } else if (isPostgreSQL()) {
            sendDate = "CAST(mds.senddate AS DATE)";
            today = "CAST(CURRENT_TIMESTAMP AS DATE)";
        }
        String query = "UPDATE rulebased_sent_tbl rbs SET lastsent = NULL WHERE mailing_id = ? AND EXISTS " +
                " (SELECT 1 FROM maildrop_status_tbl mds WHERE mds.mailing_id = ? AND mds.status_field = ?  AND " + sendDate + " = " + today + ")";

        update(query, mailingId, mailingId, MaildropStatus.DATE_BASED.getCodeString());
    }

    @Override
    @DaoUpdateReturnValueCheck
    public boolean markAsDeleted(int mailingID, int companyID) {
        final int touchedLines = update("UPDATE mailing_tbl SET deleted = 1, change_date = CURRENT_TIMESTAMP WHERE mailing_id = ? AND company_id = ? AND (deleted IS NULL OR deleted != 1)", mailingID, companyID);
        return touchedLines > 0;
    }

    @Override
    public List<Map<String, String>> loadAction(int mailingID, int companyID) {
        final List<Map<String, String>> actions = new LinkedList<>();
        final String stmt = "SELECT url.shortname AS url_shortname, alt_text, full_url, act.shortname AS act_shortname, url.url_id, url.action_id FROM rdir_url_tbl url INNER JOIN rdir_action_tbl act ON url.action_id = act.action_id AND url.company_id = act.company_id WHERE url.mailing_id = ? AND url.company_id = ? AND url.deleted = 0";
        try {
            final List<Map<String, Object>> list = select(stmt, mailingID, companyID);
            for (final Map<String, Object> row : list) {
                String name;
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
        } catch (Exception e) {
            logger.error("Error loading actions for mailing {}", mailingID, e);
        }

        return actions;
    }

    @Override
    public boolean updateStatus(int companyID, int mailingID, MailingStatus mailingStatus, Date sendDate) {
    	if (mailingStatus == MailingStatus.NEW) {
    		// never reset a mailing to status new
            return false;
        }

        MailingStatus currentMailingWorkStatus = getStatus(companyID, mailingID);
        MailingStatus newMailingStatus;
        Date newSendDate;
        switch (currentMailingWorkStatus) {
            case ACTIVE, SCHEDULED:
                if (mailingStatus == MailingStatus.SENDING || mailingStatus == MailingStatus.CANCELED
                        || mailingStatus == MailingStatus.INSUFFICIENT_VOUCHERS || mailingStatus == MailingStatus.DISABLE) {
                    newMailingStatus = mailingStatus;
                    newSendDate = sendDate;
                    break;
                }
                return false;
            case SENDING:
                if (mailingStatus == MailingStatus.SENT || mailingStatus == MailingStatus.NORECIPIENTS
                        || mailingStatus == MailingStatus.CANCELED || mailingStatus== MailingStatus.INSUFFICIENT_VOUCHERS
                        || mailingStatus == MailingStatus.CANCELED_AND_COPIED) {
                    newMailingStatus = mailingStatus;
                    newSendDate = sendDate;
                    break;
                }
                return false;
            case CANCELED, CANCELED_AND_COPIED, INSUFFICIENT_VOUCHERS:
                if (mailingStatus == MailingStatus.SENDING
                    || mailingStatus == MailingStatus.SENT
                    || mailingStatus == MailingStatus.SCHEDULED) {
                    newMailingStatus = mailingStatus;
                    newSendDate = sendDate;
                    break;
                }
                return false;
            // never overwrite status sent or norecipients
            case SENT, NORECIPIENTS:
                return false;
            case ADMIN, DISABLE, EDIT, GENERATION_FINISHED, IN_GENERATION, NEW, READY, TEST:
                newMailingStatus = mailingStatus;
                newSendDate = sendDate;
                break;
            default:
                throw new IllegalArgumentException("Unknown mailing status");
        }

        try {
            if (newSendDate == null) {
                int touchedMailings = update("UPDATE mailing_tbl SET work_status = ? WHERE mailing_id = ?", newMailingStatus.getDbKey(), mailingID);
                return touchedMailings >= 1;
            } else {
                int touchedMailings = update("UPDATE mailing_tbl SET work_status = ?, send_date = ? WHERE mailing_id = ?", newMailingStatus.getDbKey(), newSendDate, mailingID);
                return touchedMailings >= 1;
            }
        } catch (Exception e) {
            logger.error("Error updating work status for mailing {}", mailingID, e);
            return false;
        }
    }

    @Override
    public MailingStatus getStatus(int companyID, int mailingID) {
        String statusString = selectStringDefaultNull("SELECT work_status FROM mailing_tbl WHERE company_id = ? and mailing_id = ?", companyID, mailingID);
        if (StringUtils.isBlank(statusString)) {
        	return MailingStatus.NEW;
        }
        return MailingStatus.fromDbKey(statusString);
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

            return selectInt(sql, mailingId, companyId, type.getMediaCode(), MediaTypeStatus.Active.getCode()) > 0;
        } catch (Exception e) {
            logger.error("Error checking email for mailing {}", mailingId, e);
            return false;
        }
    }

    @Override
    public List<MailingDto> getUnsentMailings(Admin admin, boolean planned) {
        List<Object> params = new ArrayList<>(List.of(
            MailingType.NORMAL.getCode(),
            admin.getCompanyID(),
            MaildropStatus.WORLD.getCodeString(),
            admin.getCompanyID(),
            MediaTypeStatus.Active.getCode(),
            MediaTypeStatus.Active.getCode()));
        return select("""
                SELECT * FROM (
                    SELECT m.mailing_id mailingId, m.shortname, m.work_status, mt.mediatype
                    FROM mailing_tbl m
                        LEFT JOIN mailing_mt_tbl mt ON m.mailing_id = mt.mailing_id
                        LEFT JOIN mailinglist_tbl ml ON (ml.mailinglist_id = m.mailinglist_id AND ml.company_id = m.company_id)
                    WHERE m.is_template = 0
                        AND m.mailing_type = ?
                        AND m.company_id = ?
                        AND ml.deleted = 0
                        AND m.deleted = 0
                        AND NOT EXISTS (SELECT 1 FROM maildrop_status_tbl md WHERE md.mailing_id = m.mailing_id AND md.status_field = ? AND md.company_id = ?)
                        AND mt.status = ? AND mt.priority = (SELECT MIN(priority) FROM mailing_mt_tbl mt2 WHERE mt2.mailing_id = m.mailing_id AND mt2.status = ?)
                        AND m.plan_date %s %s
                    %s
                     """.formatted(
                planned ? "IS NOT NULL" : "IS NULL",
                getAdditionalSqlRestrictions(admin, params, "m"),
                isOracleDB() ? " ORDER BY mailingId DESC) WHERE rownum <= 10" : ") res ORDER BY mailingId DESC LIMIT 10"),
            getMailingDtoRowMapper(),
            params.toArray());
    }

    @Override
    public PaginatedList<Map<String, Object>> getMailingList(Admin admin, MailingsListProperties props) throws FulltextSearchInvalidQueryException {
        List<Object> selectParameters = new ArrayList<>();
        String selectSql = generateMailingsSelectQuery(props, selectParameters, admin);

        String sortColumn;
        String direction;
        String sortClause;
        if (StringUtils.isNotBlank(props.getSort())) {
            String sort = props.getSort();
            sortColumn = StringUtils.lowerCase(sort);
            direction = StringUtils.defaultIfEmpty(props.getDirection(), "ASC").toUpperCase();

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

                if (MailingAdditionalColumn.ATTACHMENTS == column) {
                    sort = "attachments_count";
                }
            }

            if ("mailing_id".equalsIgnoreCase(sort)) {
                sortClause = "ORDER BY " + sort + " " + props.getDirection();
            } else {
                DbColumnType dbColumnType = DbUtilities.getColumnDataType(getDataSource(), "mailing_tbl", sort);
                if (dbColumnType != null) {
                    sort = "a." + sort;
                } else {
                    dbColumnType = DbUtilities.getColumnDataType(getDataSource(), "mailinglist_tbl", sort);
                    if (dbColumnType != null) {
                        sort = "m." + sort;
                    }
                }

                if ("mailinglist".equalsIgnoreCase(sort) || "work_status".equalsIgnoreCase(sort)
                        || (dbColumnType != null && dbColumnType.getSimpleDataType() == SimpleDataType.Characters)) {
                    if (isOracleDB() || isPostgreSQL()) {
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
            }
        } else {
            sortColumn = "senddate";
            direction = StringUtils.isBlank(props.getDirection()) ? "DESC" : StringUtils.upperCase(props.getDirection());
            sortClause = generateSendDateSortClause(direction, sortColumn);
        }

        try {
            PaginatedList<Map<String, Object>> resultList = selectPaginatedListWithSortClause(
                    selectSql, sortClause, sortColumn, AgnUtils.sortingDirectionToBoolean(direction),
                    props.getPage(), props.getRownums(), new MailingMapRowMapper(), selectParameters.toArray());

            if (props.isUiFiltersSet()) {
                resultList.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(props, admin));
            }

            addComponentIdsToList(resultList, "preview_component");

            if (props.getAdditionalColumns().contains(MailingAdditionalColumn.ATTACHMENTS.getSortColumn())) {
                addAttachmentsNamesToList(resultList);
            }

            return resultList;
        } catch (UncategorizedSQLException ex) {
            if (isOracleTextTooManyTermsError(ex)) {
                throw new TooBroadSearchException("Too broad search query", ex);
            }
            throw ex;
        }
    }

    private boolean isOracleTextTooManyTermsError(UncategorizedSQLException ex) {
        Throwable root = ex.getRootCause();
        if (root == null) {
            return false;
        }

        return StringUtils.contains(root.getMessage(), "DRG-51030:");
    }

    private int getTotalUnfilteredCountForOverview(MailingsListProperties props, Admin admin) {
        final boolean disabledMailingListsSupported = configService.isDisabledMailingListsSupported();
        final List<Object> params = new ArrayList<>(List.of(
                admin.getCompanyID(),
                BooleanUtils.toInteger(props.isTemplate()),
                BooleanUtils.toInteger(props.isUseRecycleBin())
        ));

        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM mailing_tbl m");

        if (disabledMailingListsSupported) {
            query.append(" LEFT JOIN mailinglist_tbl ml ON m.mailinglist_id = ml.mailinglist_id AND m.company_id = ml.company_id AND ml.deleted <> 1");
        }
        query.append(" WHERE m.company_id = ? AND m.is_template = ? AND m.deleted = ?");

        if (disabledMailingListsSupported) {
            query.append(" AND ml.mailinglist_id NOT IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?)");
            params.add(admin.getAdminID());
        }

        if (props.isMailingStatisticsOverview()) {
            query.append(" AND m.work_status IN ('").append(StringUtils.join(props.getStatuses(), "', '")).append("')");
            query.append(" AND m.mailing_type IN (").append(props.getTypes()).append(")");
        }

        return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(Admin admin, MailingsListProperties props) throws FulltextSearchInvalidQueryException {
        List<Object> selectParameters = new ArrayList<>();
        String selectSql = generateMailingsSelectQuery(props, selectParameters, admin);

        return select(selectSql, LightweightMailingRowMapper.INSTANCE, selectParameters.toArray());
    }

    private String generateMailingsSelectQuery(MailingsListProperties props, List<Object> selectParameters, Admin admin) throws FulltextSearchInvalidQueryException {
        int adminId = admin.getAdminID();
        int companyID = admin.getCompanyID();

        if ((StringUtils.isNotBlank(props.getSearchNameStr()) || StringUtils.isNotBlank(props.getSearchDescriptionStr())) && !isBasicFullTextSearchSupported()) {
            props.setSearchNameStr("");
            props.setSearchDescriptionStr("");
        }

        // Full text index search only works for patterns of size 3+ characters
        if (!isContentFullTextSearchSupported() || StringUtils.length(props.getSearchContentStr()) < 3) {
            props.setSearchContentStr("");
        }

        String nameFullTextSearchClause = generateFullTextSearchQuery(props.getSearchNameStr());
        String descriptionFullTextSearchClause = generateFullTextSearchQuery(props.getSearchDescriptionStr());
        String contentFullTextSearchClause = generateFullTextSearchQuery(props.getSearchContentStr());

        final StringBuilder selectSql = new StringBuilder("SELECT ")
                .append("a.work_status AS work_status, a.company_id AS company_id, a.mailing_id AS mailing_id,")
                .append("a.shortname AS shortname, a.mailing_type AS mailing_type, a.description AS description,")
                .append("a.send_date AS senddate, m.shortname AS mailinglist, a.creation_date AS creationdate,")
                .append("a.change_date AS changedate, a.target_expression AS target_expression, a.delivered AS recipientscount,")
                .append("a.plan_date AS plan_date, a.is_grid, a.content_type, a.workflow_id, a.is_post AS isOnlyPostType")
                .append(getAdditionalColumnsName(props.getAdditionalColumns()))
                .append(" FROM mailing_tbl a")
                .append(joinAdditionalColumns(props.getAdditionalColumns()))
                .append(" LEFT JOIN mailinglist_tbl m ON a.mailinglist_id = m.mailinglist_id AND a.company_id = m.company_id AND m.deleted <> 1");

        if (StringUtils.isNotBlank(contentFullTextSearchClause)) {
            // Subquery to search within static text and html blocks
            selectSql.append(" LEFT JOIN (SELECT mailing_id, MAX(")
                    .append(isPostgreSQL() ? "(" : "")
                    .append(DbUtilities.getFullTextSearchPart("emmblock", dataSource))
                    .append(isPostgreSQL() ? ")::int" : "")
                    .append(") AS relevance");
            selectParameters.add(contentFullTextSearchClause);

            selectSql.append(" FROM component_tbl WHERE company_id = ? AND mtype IN ('text/plain', 'text/html')");
            selectParameters.add(companyID);

            selectSql.append(" GROUP BY mailing_id) comp ON a.mailing_id = comp.mailing_id");

            // Subquery to search within dynamic (included into static with agn tags) text and html blocks
            selectSql.append(" LEFT JOIN (SELECT cont.mailing_id, MAX(")
                    .append(isPostgreSQL() ? "(" : "")
                    .append(DbUtilities.getFullTextSearchPart("cont.dyn_content", dataSource))
                    .append(isPostgreSQL() ? ")::int" : "")
                    .append(") AS relevance");
            selectParameters.add(contentFullTextSearchClause);

            selectSql.append(" FROM dyn_name_tbl nm JOIN dyn_content_tbl cont ON nm.dyn_name_id = cont.dyn_name_id")
                    .append(" WHERE cont.company_id = ?");
            selectParameters.add(companyID);

            selectSql.append(" GROUP BY cont.mailing_id) cont ON a.mailing_id = cont.mailing_id");
        }

        selectSql.append(" WHERE a.company_id = ?");
        selectParameters.add(companyID);

        selectSql.append(" AND a.is_template = ?");
        selectParameters.add(BooleanUtils.toInteger(props.isTemplate()));

        if (props.getGrid() != null) {
            selectSql.append(" AND a.is_grid = ?");
            selectParameters.add(BooleanUtils.toInteger(props.getGrid()));
        }

        selectSql.append(" AND a.deleted = ?");
        selectParameters.add(BooleanUtils.toInteger(props.isUseRecycleBin()));

        if (adminId > 0 && configService.isDisabledMailingListsSupported()) {
            selectSql.append(" AND m.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY);
            selectParameters.add(adminId);
        }

        final List<Integer> targetGroupIds = props.getTargetGroups();
        if (isNotEmpty(targetGroupIds)) {
            String targetExpressionRestriction = createTargetExpressionRestriction(targetGroupIds, selectParameters);
            if (!targetExpressionRestriction.isEmpty()) {
                selectSql.append(" AND (").append(targetExpressionRestriction).append(")");
            }
        }

        selectSql.append(getTargetRestrictions(admin, selectParameters, "a"));

        if (StringUtils.isNotBlank(props.getTypes())) {
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
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.send_date", props.getSendDateBegin(), props.getSendDateEnd(), isOracleDB() || isPostgreSQL()));
        }

        if (props.getCreationDateBegin() != null || props.getCreationDateEnd() != null) {
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.creation_date", props.getCreationDateBegin(), props.getCreationDateEnd(), isOracleDB() || isPostgreSQL()));
        }

        if (props.getPlanDateBegin() != null || props.getPlanDateEnd() != null) {
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.plan_date", props.getPlanDateBegin(), props.getPlanDateEnd(), isOracleDB() || isPostgreSQL()));
        }

        if (props.getChangeDateBegin() != null || props.getChangeDateEnd() != null) {
            selectSql.append(" AND ").append(DbUtilities.getDateConstraint("a.change_date", props.getChangeDateBegin(), props.getChangeDateEnd(), isOracleDB() || isPostgreSQL()));
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
        if (StringUtils.isNotBlank(nameFullTextSearchClause)) {
            searchClauses.add(getFullTextSearchMatchFilter("a.shortname"));
            selectParameters.add(nameFullTextSearchClause);
        }

        if (StringUtils.isNotBlank(descriptionFullTextSearchClause)) {
            searchClauses.add(getFullTextSearchMatchFilter("a.description"));
            selectParameters.add(descriptionFullTextSearchClause);
        }

        if (StringUtils.isNotBlank(contentFullTextSearchClause)) {
            searchClauses.add("(comp.relevance > 0 OR cont.relevance > 0) ");
        }

        final String searchSqlPart = StringUtils.join(searchClauses, " AND ");

        if (StringUtils.isNotEmpty(searchSqlPart)) {
            selectSql.append(" AND (").append(searchSqlPart).append(")");
        }

        return selectSql.toString();
    }

    private String createTargetExpressionRestriction(List<Integer> targetGroupIds, List<Object> params) {
        StringBuilder result = new StringBuilder();

        for (Integer targetGroupId : targetGroupIds) {
            if (Objects.nonNull(targetGroupId)) {
                if (!result.isEmpty()) {
                    result.append(" OR ");
                }
                result.append(DbUtilities.createTargetExpressionRestriction(dataSource, "a"));
                params.add(targetGroupId);
            }
        }

        return result.toString();
    }

    private String generateFullTextSearchQuery(String searchQuery) throws FulltextSearchInvalidQueryException {
        try {
            return fulltextSearchQueryGenerator.generateSpecificQuery(searchQuery);
        } catch (FulltextSearchQueryException e) {
            logger.error("Cannot transform full text search query: {}", searchQuery);
        }

        return searchQuery;
    }

    /**
     * Thumbnail component_ids are mixed in to the data for making the basic db select more performant, because it does not need to detect the component_id for invisible elements
     */
    private void addComponentIdsToList(PaginatedList<Map<String, Object>> list, String componentIdPropertyName) {
        List<Integer> mailingIds = getMailingIds(list);
        if (mailingIds.isEmpty()) {
            return;
        }

        List<Map<String, Object>> result = select("SELECT mailing_id, MAX(component_id) AS component_id FROM component_tbl WHERE mailing_id IN (" + StringUtils.join(mailingIds, ", ") + ") AND binblock IS NOT NULL AND comptype = ? GROUP BY mailing_id", MailingComponentType.ThumbnailImage.getCode());
        Map<Integer, Integer> componentIdsByMailingId = new HashMap<>();
        for (Map<String, Object> row : result) {
            componentIdsByMailingId.put(((Number) row.get("mailing_id")).intValue(), ((Number) row.get("component_id")).intValue());
        }

        for (Map<String, Object> item : list.getList()) {
            Integer componentId = componentIdsByMailingId.get(item.get("mailingid"));
            if (componentId == null) {
                componentId = 0;
            }
            item.put(componentIdPropertyName, componentId);
        }
    }

    private void addAttachmentsNamesToList(PaginatedList<Map<String, Object>> list) {
        List<Integer> mailingIds = getMailingIds(list);
        if (mailingIds.isEmpty()) {
            return;
        }

        String query = "SELECT mailing_id, compname FROM component_tbl WHERE " + makeBulkInClauseForInteger("mailing_id", mailingIds)
                + " AND "
                + makeBulkInClauseForInteger(
                "comptype",
                MailingComponentType.Attachment.getCode(),
                MailingComponentType.PersonalizedAttachment.getCode(),
                MailingComponentType.PrecAAttachement.getCode()
        );

        Map<Integer, List<String>> attachmentsNamesMap = select(query, (rs, i) -> Tuple.of(rs.getInt("mailing_id"), rs.getString("compname")))
                .stream()
                .collect(Collectors.groupingBy(Tuple::getFirst, Collectors.mapping(Tuple::getSecond, Collectors.toList())));

        for (Map<String, Object> item : list.getList()) {
            item.put("attachments_names", attachmentsNamesMap.getOrDefault(
                    ((Number) item.get("mailingid")).intValue(),
                    Collections.emptyList()
            ));
        }
    }

    private List<Integer> getMailingIds(PaginatedList<Map<String, Object>> list) {
        List<Map<String, Object>> items = list.getList();
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> mailingIds = new ArrayList<>();
        for (Map<String, Object> item : items) {
            mailingIds.add(((Number) item.get("mailingid")).intValue());
        }

        return mailingIds;
    }

    private String generateSendDateSortClause(String direction, String sortColumn) {
    	if("senddate".equals(sortColumn)) {
    		sortColumn = "send_date";
    	}

        final boolean isAscending = AgnUtils.sortingDirectionToBoolean(direction, true);
        direction = isAscending ? "ASC" : "DESC";
        if (isOracleDB() || isPostgreSQL()) {
            return String.format("ORDER BY a.%3$s %1$s NULLS %2$s, mailing_id %1$s", direction, isAscending ? "LAST" : "FIRST", sortColumn);
        } else {
            // MySQL DESC sorts null-values to the end by default, oracle and postgres DESC sorts null-values to the top
            // MySQL ASC sorts null-values to the top by default, oracle and postgres ASC sorts null-values to the end
            return String.format("ORDER BY ISNULL(a.%2$s) %1$s, a.%2$s %1$s, mailing_id %1$s", direction, sortColumn);
        }
    }

    private String joinAdditionalColumns(Set<String> additionalColumns) {
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
                } else if (isPostgreSQL()) {
                    queryPart.append(", SUBSTRING(param FROM 'subject=\"([^\"]+)\", charset') AS subject");
                } else {
                    queryPart.append(", SUBSTRING_INDEX(SUBSTRING_INDEX(mt.param, 'subject=\"', -1), '\", charset', 1) AS subject");
                }
            } else if (column == MailingAdditionalColumn.ARCHIVE) {
                queryPart.append(", arch.shortname AS archive ");
            } else if (column == MailingAdditionalColumn.ATTACHMENTS) {
                queryPart.append(", (SELECT COUNT(*) FROM component_tbl att WHERE att.mailing_id = a.mailing_id AND ")
                        .append(makeBulkInClauseForInteger(
                                "comptype",
                                MailingComponentType.Attachment.getCode(),
                                MailingComponentType.PersonalizedAttachment.getCode(),
                                MailingComponentType.PrecAAttachement.getCode()
                        ))
                        .append(") AS attachments_count");
            }
        }
        return queryPart.toString();
    }

    @Override
    public boolean usedInRunningWorkflow(int mailingId, int companyId) {
        final String sqlGetCount = "SELECT COUNT(*) FROM workflow_tbl w " +
                "JOIN workflow_dependency_tbl dep ON dep.company_id = w.company_id AND dep.workflow_id = w.workflow_id " +
                "WHERE w.company_id = ? AND w.status IN (?, ?, ?) AND dep.type IN (?, ?) AND dep.entity_id = ?";

        final Object[] sqlParameters = {
                companyId,
                WorkflowStatus.STATUS_ACTIVE.getId(),
                WorkflowStatus.STATUS_TESTING.getId(),
                WorkflowStatus.STATUS_PAUSED.getId(),
                WorkflowDependencyType.MAILING_DELIVERY.getId(),
                WorkflowDependencyType.MAILING_REFERENCE.getId(),
                mailingId
        };

        return selectInt(sqlGetCount, sqlParameters) > 0;
    }

    @Override
    public int getWorkflowId(int mailingId) {
        final String sqlGetMaxId = "SELECT MAX(workflow_id) FROM workflow_dependency_tbl " +
                "WHERE type = ? AND entity_id = ?";

        return selectInt(sqlGetMaxId, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId);
    }

    @Override
    public int getWorkflowId(int mailingId, int companyId) {
        final String sqlGetMaxId = "SELECT MAX(workflow_id) FROM workflow_dependency_tbl " +
                "WHERE company_id = ? AND type = ? AND entity_id = ?";

        return selectInt(sqlGetMaxId, companyId, WorkflowDependencyType.MAILING_DELIVERY.getId(), mailingId);
    }

    @Override
    public PaginatedList<MailingBase> getMailingsForComparison(MailingComparisonFilter filter, Admin admin) {
        SqlPreparedStatementManager statementManager = prepareMailingComparisonOverviewQuery(filter, admin, false);

        String sortTable = "senddate".equals(filter.getSort()) ? "" : "mailing_tbl";

        PaginatedList<MailingBase> list = selectPaginatedList(statementManager.getPreparedSqlString(), sortTable, filter.getSortOrDefault("mailing_id"),
                filter.ascending(), filter.getPage(), filter.getNumberOfRows(), new ComparisonMailingRowMapper(), statementManager.getPreparedSqlParameters());

        if (filter.isUiFiltersSet()) {
            SqlPreparedStatementManager countStatementManager = prepareMailingComparisonOverviewQuery(null, admin, true);
            list.setNotFilteredFullListSize(selectInt(
                    countStatementManager.getPreparedSqlString(),
                    countStatementManager.getPreparedSqlParameters()
            ));
        }

        return list;
    }

    private SqlPreparedStatementManager prepareMailingComparisonOverviewQuery(MailingComparisonFilter filter, Admin admin, boolean useCountQuery) {
        String query = "SELECT " +
                (useCountQuery ? "1" : "a.mailing_id, a.shortname, a.description, MIN(c.mintime) senddate, a.is_post AS isOnlyPostType")
                + " FROM mailing_tbl a"
                + " LEFT JOIN mailing_account_sum_tbl c ON (a.mailing_id = c.mailing_id)"
                + " WHERE a.company_id = ? AND a.deleted = 0 AND c.status_field = ?"
                + " AND a.mailing_id in (SELECT mailing_id FROM maildrop_status_tbl WHERE status_field IN (?, ?, ?) AND company_id = ?)";

        final List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());
        params.add(MaildropStatus.WORLD.getCodeString());
        params.add(MaildropStatus.WORLD.getCodeString());
        params.add(MaildropStatus.ACTION_BASED.getCodeString());
        params.add(MaildropStatus.DATE_BASED.getCodeString());
        params.add(admin.getCompanyID());

        query += getAdditionalSqlRestrictions(admin, params, "a");

        if (filter != null) {
            query += applyOverviewFilter(filter, params);
        }

        query += " GROUP BY a.mailing_id, a.shortname, a.description, a.is_post";

        if (filter != null && filter.getSendDate().isPresent()) {
            DateRange sendDate = filter.getSendDate();
            query += " HAVING " + getDateRangeFilter("MIN(c.mintime)", sendDate, params).orElse("");
        }

        if (useCountQuery) {
            query = String.format("SELECT COUNT(*) FROM (%s) records_count", query);
        }

        return new SqlPreparedStatementManager(query, params.toArray());
    }

    private String applyOverviewFilter(MailingComparisonFilter filter, List<Object> params) {
        StringBuilder filterSql = new StringBuilder();
        if (StringUtils.isNotBlank(filter.getMailing())) {
            filterSql.append(getPartialSearchFilterWithAnd("a.shortname", filter.getMailing(), params));
        }
        if (StringUtils.isNotBlank(filter.getDescription())) {
            filterSql.append(getPartialSearchFilterWithAnd("a.description", filter.getDescription(), params));
        }
        return filterSql.toString();
    }

    @Override
    @DaoUpdateReturnValueCheck
    public boolean saveStatusmailRecipients(int mailingID, final String statusmailRecipients) {
        try {
            final int result = update("UPDATE mailing_tbl SET statmail_recp = ? WHERE mailing_id = ?", statusmailRecipients, mailingID);
            return result == 1;
        } catch (final Exception e) {
            logger.error("Error while saveStatusmailRecipients", e);
            return false;
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public boolean saveStatusmailOnErrorOnly(int companyID, int mailingID, boolean statusmailOnErrorOnly) {
        try {
            final int result = update("UPDATE mailing_tbl SET statmail_onerroronly = ? WHERE company_id = ? AND mailing_id = ?", statusmailOnErrorOnly ? 1 : 0, companyID, mailingID);
            return result == 1;
        } catch (Exception e) {
            logger.error("Error while saveStatusmailOnErrorOnly", e);
            return false;
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public boolean saveSecuritySettings(int companyId, int mailingId, MailingSendSecurityOptions options) {
        try {
            final int result = update("UPDATE mailing_tbl SET statmail_onerroronly = ?, statmail_recp = ?, clearance_email = ?, clearance_threshold = ? WHERE company_id = ? AND mailing_id = ?",
                    BooleanUtils.toInteger(options.isEnableNoSendCheckNotifications()),
                    options.getClearanceEmail(), options.getClearanceEmail(),
                    options.getClearanceThreshold(),
                    companyId, mailingId);
            return result == 1;
        } catch (Exception e) {
            logger.error("Error while saving security settings", e);
            return false;
        }
    }

    @Override
    public List<Integer> getFollowupMailings(int mailingID, int companyID, boolean includeUnscheduled) {
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
        return select(queryBuilder.toString(), IntegerRowMapper.INSTANCE, companyID);
    }

    @Override
    public int getStatusidForWorldMailing(int mailingID, int companyID) {
        final String query = "SELECT status_id FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field = ? AND genstatus = 3 AND senddate < CURRENT_TIMESTAMP";
        try {
            return selectIntWithDefaultValue(query, 0, companyID, mailingID, MaildropStatus.WORLD.getCodeString());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int getCompanyIdForMailingId(int mailingId) {
        return selectIntWithDefaultValue("SELECT company_id FROM mailing_tbl WHERE mailing_id = ?", -1, mailingId);
    }

    @Override
    public RdirMailingData getRdirMailingData(int mailingId) {
        final List<Map<String, Object>> list = select("SELECT company_id, creation_date FROM mailing_tbl WHERE mailing_id = ?", mailingId);

        if (list.size() != 1) {
            return null;
        } else {
            final Map<String, Object> map = list.get(0);
            return new RdirMailingDataImpl(((Number) map.get("company_id")).intValue(), (Timestamp) map.get("creation_date"));
        }
    }

    /**
     * returns the base-mailing for the given followup.
     */
    @Override
    public String getFollowUpFor(int mailingID) {
    	if (mailingID == 0) {
            return null;
    	}

        String params;
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
    public PaginatedList<Map<String, Object>> getDashboardMailingList(Admin admin, String sort, String direction, int rownums) {
        String orderBy = getDashboardMailingsOrder(sort, direction, Arrays.asList("shortname", "description", "mailinglist"));
        if (!isOracleDB()) {
            orderBy = orderBy.replaceAll(" e\\.", " ");
        }

        List<Object> params = new ArrayList<>();

        params.add(admin.getCompanyID());
        String additionalRestriction = getAdditionalSqlRestrictions(admin, params);
        params.add(MaildropStatus.WORLD.getCodeString());

        String mailingsQuery = addRowLimit("""
                SELECT company_id, mailing_id, shortname, description, work_status, change_date, mailinglist_id, workflow_id, is_post
                FROM mailing_tbl
                WHERE company_id = ? AND deleted = 0 AND is_template = 0 %s
                ORDER BY COALESCE(change_date, %s) DESC, mailing_id DESC
                """
                .formatted(
                        additionalRestriction,
                        isOracleDB() || isPostgreSQL() ? "TO_DATE('01.01.1900', 'dd.mm.yyyy')" : "DATE_FORMAT('01.01.1900','%d.%m.%Y')"
                ), rownums);

        String query = """
                SELECT * FROM (
                    SELECT m.work_status, m.mailing_id AS mid, m.shortname, m.description, acc.mintime AS senddate,
                           ml.shortname AS mailinglist, m.change_date, m.workflow_id, m.is_post AS isOnlyPostType
                    FROM (%s) m
                    LEFT JOIN mailing_account_sum_tbl acc ON m.mailing_id = acc.mailing_id AND acc.status_field = ?
                    LEFT JOIN mailinglist_tbl ml ON m.mailinglist_id = ml.mailinglist_id AND ml.company_id = m.company_id AND ml.deleted = 0
                ) subsel %s
                """.formatted(mailingsQuery, orderBy);

        final List<Map<String, Object>> rows = select(query, params.toArray());

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

        PaginatedList<Map<String, Object>> resultList = new PaginatedList<>(result, result.size(), rownums, 1, sort, direction);
        addComponentIdsToList(resultList, "component");
        return resultList;
    }

    private String getDashboardMailingsOrder(String sort, String direction, List<String> charColumns) {
        String orderby;
        if (StringUtils.isNotBlank(sort)) {
            /*
             * When the users want us to sort the list by senddate, then we have
             * to handle the null values to be the lowest values otherwise we
             * get a strange order: null values are always the greatest values.
             */
            if (sort.trim().equalsIgnoreCase("senddate")) {
                if (isOracleDB() || isPostgreSQL()) {
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
            if (isOracleDB() || isPostgreSQL()) {
                orderby = " ORDER BY COALESCE(change_date, TO_DATE('01.01.1900', 'dd.mm.yyyy')) DESC, mid DESC";
            } else {
                orderby = " ORDER BY COALESCE(change_date, DATE_FORMAT('01.01.1900', '%d.%m.%Y')) DESC, mid DESC";
            }
        }
        return orderby;
    }

    @Override
    public List<Integer> getBirtReportMailingsToSend(int companyID, int reportId, Date startDate, Date endDate, int filterId, int filterValue) {
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

        return select(sql, IntegerRowMapper.INSTANCE, params.toArray());
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

    @Override
    public Date getSendDate(int companyId, int mailingId) {
        final String sql = isOracleDB()
                ? "SELECT senddate FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY DECODE(status_field, 'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC"
                : "SELECT senddate FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? ORDER BY CASE status_field WHEN 'W' THEN 1 WHEN 'R' THEN 2 WHEN 'D' THEN 2 WHEN 'E' THEN 3 WHEN 'C' THEN 3 WHEN 'T' THEN 4 WHEN 'A' THEN 4 ELSE 5 END, status_id DESC";

        final List<Date> result = select(sql, DateRowMapper.INSTANCE, companyId, mailingId);

        return !result.isEmpty()
                ? result.get(0)
                : null;
    }

    @Override
    public Timestamp getLastSendDate(int mailingID) {
        String sql;
        if (isOracleDB()) {
            sql = "SELECT senddate FROM (SELECT senddate, row_number()"
                    + " OVER (ORDER BY DECODE(status_field, 'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC) r FROM maildrop_status_tbl"
                    + " WHERE mailing_id = ? ) WHERE r = 1";
        } else {
            sql = "SELECT senddate FROM maildrop_status_tbl WHERE mailing_id = ?"
                    + " ORDER BY CASE status_field WHEN 'W' THEN 1 WHEN 'R' THEN 2 WHEN 'D' THEN 2 WHEN 'E' THEN 3 WHEN 'C' THEN 3 WHEN 'T' THEN 4 WHEN 'A' THEN 4 ELSE 5 END, status_id DESC LIMIT 1";
        }

        try {
            final List<Map<String, Object>> sendDateResult = select(sql, mailingID);
            if (sendDateResult != null && sendDateResult.size() == 1 && sendDateResult.get(0).get("senddate") != null) {
                return (Timestamp) sendDateResult.get(0).get("senddate");
            } else {
                return null;
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> getLastSentWorldMailings(Admin admin, int rownum) {
        List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());
        params.add(MaildropStatus.WORLD.getCodeString());

        String query = addRowLimit("""
                SELECT m.mailing_id AS mailingid, m.shortname, maxtime senddate
                FROM mailing_account_sum_tbl a
                         JOIN mailing_tbl m ON a.mailing_id = m.mailing_id
                WHERE m.company_id = ? AND a.status_field = ? AND m.deleted = 0 %s
                ORDER BY senddate DESC
                """.formatted(getAdditionalSqlRestrictions(admin, params, "m")), rownum);

        final List<Map<String, Object>> worldmailingList = select(query, params.toArray());

        // update the type of mailingid from bigdecimal to integer
        for (final Map<String, Object> itemMap : worldmailingList) {
            itemMap.put("mailingid", ((Number) itemMap.get("mailingid")).intValue());
        }

        return worldmailingList;
    }

    @Override
    public boolean deleteAccountSumEntriesByCompany(int companyID) {
        update("DELETE FROM mailing_account_sum_tbl WHERE company_id = ?", companyID);
        return selectInt("SELECT COUNT(*) FROM mailing_account_sum_tbl WHERE company_id = ?", companyID) == 0;
    }

    @Override
    public boolean isTransmissionRunning(int mailingID) {
        String query = "SELECT count(*) FROM maildrop_status_tbl WHERE status_field IN (?) AND genstatus IN (1, 2) AND mailing_id = ?";
        final boolean isWorldMailingSentBeingAtTheMoment = selectInt(query, MaildropStatus.WORLD.getCodeString(), mailingID) > 0;
        if (isWorldMailingSentBeingAtTheMoment) {
            query = " SELECT CASE WHEN COUNT(a.maildrop_id) = COUNT(d.status_id) THEN 0 ELSE 1 END transmission_running "
                    + " FROM maildrop_status_tbl d LEFT JOIN mailing_account_tbl a ON d.status_id = a.maildrop_id WHERE d.mailing_id = ?";

            return selectInt(query, mailingID) == 1;
        } else {
            query = "SELECT count(*) FROM maildrop_status_tbl WHERE status_field IN (?, ?) AND genstatus IN (1, 2) AND mailing_id = ?";
            return selectInt(query, MaildropStatus.ADMIN.getCodeString(), MaildropStatus.TEST.getCodeString(), mailingID) > 0;
        }
    }

    @Override
    public int getLastSentMailing(int companyID, int customerID) {
        return selectIntWithDefaultValue("SELECT MAX(mailing_id) FROM success_" + companyID + "_tbl WHERE customer_id = ?"
                + " AND timestamp = (SELECT MAX(timestamp) FROM success_" + companyID + "_tbl " + " WHERE customer_ID =  ?)", -1, customerID, customerID);
    }

    @Override
    public int getLastSentWorldMailingByCompanyAndMailinglist(int companyID, int mailingListID) {
        Object[] sqlParameters;

        final StringBuilder sb = new StringBuilder();

        sb.append("SELECT mailing_id FROM maildrop_status_tbl WHERE status_id IN (");
        sb.append("SELECT MAX(status_id) FROM maildrop_status_tbl WHERE company_id = ? AND status_field = ?");
        if (mailingListID == 0) {
            sqlParameters = new Object[]{companyID, MaildropStatus.WORLD.getCodeString()};
        } else {
            sqlParameters = new Object[]{companyID, MaildropStatus.WORLD.getCodeString(), mailingListID};
            sb.append(" AND mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE mailinglist_id = ? AND deleted = 0)");
        }
        sb.append(")");

        try {
            return selectInt(sb.toString(), sqlParameters);
        } catch (final Exception e) {
            logger.error("Error getting lastSent mailingID. CompanyID: {} mailingListID: {}", companyID, mailingListID, e);
        }

        return -1;
    }

    @Override
    public List<MailingBase> getSentWorldMailingsForReports(int companyID, int number, int targetId, Set<Integer> adminAltgIds) {
        return getPredefinedMailingsForReports(companyID, number, -1, 0, null, "", targetId, adminAltgIds);
    }

    @Override
    public List<MailingBase> getPredefinedNormalMailingsForReports(int companyId, Date from, Date to, int filterType, int filterValue, String orderKey, int targetId, Set<Integer> adminAltgIds) {
        final char statusField = MaildropStatus.WORLD.getCode();
        String orderColumn = "shortname";
        if ("date".equals(orderKey)) {
            orderColumn = "change_date";
        }

        final StringBuilder query = new StringBuilder();

        if (isOracleDB()) {
            query.append("SELECT * FROM (")
                    .append("  SELECT res.mailing_id, res.shortname, res.senddate, res.change_date, res.isOnlyPostType FROM (")
                    .append("    SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date, a.is_post AS isOnlyPostType")
                    .append("       FROM mailing_account_tbl c ")
                    .append("       LEFT JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
                    .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' AND a.mailing_type = 0 %s) res ")
                    .append("    GROUP BY res.mailing_id, res.shortname, res.senddate, res.change_date, res.isOnlyPostType")
                    .append("    ORDER BY senddate DESC)");
        } else {
            query.append("SELECT * FROM ")
                    .append("   (SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date, a.is_post AS isOnlyPostType")
                    .append("       FROM mailing_account_tbl c ")
                    .append("       LEFT JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
                    .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' AND a.mailing_type = 0 %s ")
                    .append("       GROUP BY a.shortname, a.mailing_id, a.send_date, a.change_date")
                    .append("       ORDER BY a.send_date DESC) res");
        }

        final List<Object> parameters = new ArrayList<>();
        parameters.add(companyId);

        String filter = getSettingsTypeFilter(filterType, filterValue, parameters);

        if (StringUtils.isNotEmpty(filter)) {
            filter += " AND a.deleted = 0";
        }

        filter += getTargetRestrictionsForReports(companyId, targetId, adminAltgIds, parameters, "a");

        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            if (isOracleDB()) {
                query.append("   WHERE senddate BETWEEN ? AND ? ");
            } else {
                query.append("   WHERE res.senddate BETWEEN ? AND ? ");
            }

            parameters.add(from);
            parameters.add(to);
        }

        query.append(" ORDER BY ").append(orderColumn).append(" DESC");

        final String filteredQuery = String.format(query.toString(), filter);


        return select(filteredQuery, PARTIAL_MAILING_BASE_ROW_MAPPER, parameters.toArray());
    }

    protected String getTargetRestrictionsForReports(int companyId, int targetId, Set<Integer> adminAltgIds, List<Object> parameters, String tableAlias) {
        return StringUtils.EMPTY;
    }

    public static class PartialMailingBaseRowMapper implements RowMapper<MailingBase> {
        @Override
        public MailingBase mapRow(ResultSet resultSet, int i) throws SQLException {
            final MailingBaseImpl mailingBase = new MailingBaseImpl();
            mailingBase.setId(resultSet.getInt("mailing_id"));
            mailingBase.setShortname(resultSet.getString("shortname"));
            mailingBase.setSenddate(resultSet.getTimestamp("senddate"));
            mailingBase.setOnlyPostType(BooleanUtils.toBoolean(resultSet.getInt("isOnlyPostType")));
            return mailingBase;
        }
    }

    @Override
    public List<MailingBase> getPredefinedMailingsForReports(int companyId, int rownum, int filterType, int filterValue, MailingType mailingType, String orderKey, int targetId, Set<Integer> adminAltgIds) {
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
                    .append("   (SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date, a.is_post AS isOnlyPostType")
                    .append("       FROM mailing_account_tbl c")
                    .append("       INNER JOIN mailing_tbl a ON a.mailing_id = c.mailing_id")
                    .append("       WHERE c.company_id = ? AND c.status_field = '").append(statusField).append("' %s ) res")
                    .append("       GROUP BY res.mailing_id, res.shortname, res.senddate, res.change_date, res.isOnlyPostType")
                    .append("       ORDER BY senddate DESC, ").append(orderKey).append(" ASC)")
                    .append(" WHERE ROWNUM <= ?");
        } else {
            query.append("SELECT a.shortname, a.mailing_id, a.send_date AS senddate, a.change_date, a.is_post AS isOnlyPostType")
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

        return select(filteredQuery, PARTIAL_MAILING_BASE_ROW_MAPPER, parameters.toArray());
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

    private String getSettingsTypeFilter(int filterType, int filterValue, List<Object> parameters) {
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
                filterClause = " AND " + DbUtilities.createTargetExpressionRestriction(dataSource, "a");
                parameters.add(filterValue);
                break;

            default:
                //nothing do
        }

        return filterClause;
    }

    private List<Map<String, String>> getTargetGroupsForTargetExpression(String targetExpression, Map<Integer, String> targetNameCache) {
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
                    logger.info("Found no name target group ID {}", targetId);
                }
            }
        }

        return result;
    }

    private String getTargetGroupName(int targetId, Map<Integer, String> targetNameCache) {
        if (targetNameCache.containsKey(targetId)) {
            return targetNameCache.get(targetId);
        }

        try {
            final String targetName = selectWithDefaultValue("SELECT target_shortname FROM dyn_target_tbl WHERE target_id = ?", String.class, null, targetId);

            if (targetName != null) {
                targetNameCache.put(targetId, targetName);
            }

            return targetName;
        } catch (Exception e) {
            // In some cases, mailings reference to non-existing target groups.
            logger.error("Exception reading name for target group ID {}", targetId, e);
            return null;
        }
    }

    @Override
    public List<MailingPopoverInfo> getMailingsCalendarInfo(Set<Integer> mailingIds, Admin admin) {
        String mailingIdsCsv = StringUtils.join(mailingIds, ", ");
        List<Object> params = new ArrayList<>(Arrays.asList(
                MailingComponentType.ThumbnailImage.getCode(),
                MediaTypes.EMAIL.getMediaCode(),
                String.valueOf(MaildropStatus.WORLD.getCode())));

        return select("""
                SELECT mailing.mailing_id, mailing.shortname, mailing.description, mailing.is_post,
                       mt.param                        mt_param,
                       MAX(cmp.component_id)           preview_component,
                       COALESCE(account.sent_count, 0) sent_count,
                       COALESCE(opl.openers, 0)        openers,
                       COALESCE(rdir.clickers, 0)      clickers,
                       MIN(maildrop.senddate)          send_date,
                       mailinglist.shortname           mailinglist_name
                FROM mailing_tbl mailing
                LEFT JOIN component_tbl cmp ON (mailing.mailing_id = cmp.mailing_id AND cmp.comptype = ?)
                LEFT JOIN mailing_mt_tbl mt ON mailing.mailing_id = mt.mailing_id AND mt.mediatype = ?
                LEFT JOIN maildrop_status_tbl maildrop ON mailing.mailing_id = maildrop.mailing_id AND maildrop.status_field = ?
                LEFT JOIN mailinglist_tbl mailinglist ON mailing.mailinglist_id = mailinglist.mailinglist_id
                LEFT JOIN (SELECT SUM(no_of_mailings) AS sent_count, mailing_id, status_field
                           FROM mailing_account_tbl
                           WHERE mailing_id IN (%s)
                           GROUP BY mailing_id, status_field) account ON mailing.mailing_id = account.mailing_id AND account.status_field = maildrop.status_field
                LEFT JOIN (SELECT COUNT(DISTINCT customer_id) AS openers, mailing_id
                           FROM onepixellog_%d_tbl
                           WHERE mailing_id IN (%s)
                           GROUP BY mailing_id) opl ON mailing.mailing_id = opl.mailing_id
                           LEFT JOIN (SELECT COUNT(DISTINCT customer_id) AS clickers, mailing_id
                                      FROM rdirlog_%d_tbl
                                      WHERE mailing_id IN (%s)
                                      GROUP BY mailing_id) rdir ON mailing.mailing_id = rdir.mailing_id
                                      WHERE mailing.mailing_id IN (%s)
                %s
                GROUP BY mailing.mailing_id, mailing.shortname, mailing.description, mt.param, mailing.is_post,
                    COALESCE(account.sent_count, 0), COALESCE(opl.openers, 0), COALESCE(rdir.clickers, 0), mailinglist.shortname
                """.formatted(mailingIdsCsv, admin.getCompanyID(), mailingIdsCsv, admin.getCompanyID(),
                mailingIdsCsv, mailingIdsCsv, getAdditionalSqlRestrictions(admin, params, MAILING_ALIAS)),
            getMailingPopoverInfoRowMapper(admin), params.toArray());
    }

    private static RowMapper<MailingPopoverInfo> getMailingPopoverInfoRowMapper(Admin admin) {
        return (rs, i) -> {
            String mtParam = rs.getString("mt_param");
            Date sendDate = rs.getTimestamp("send_date");
            return new MailingPopoverInfo(
                rs.getInt("mailing_id"),
                rs.getInt("sent_count"),
                rs.getInt("openers"),
                rs.getInt("clickers"),
                rs.getBoolean("is_post"),
                rs.getString("shortname"),
                mtParam == null ? "" : new ParameterParser(mtParam).parse(SUBJECT_COL_NAME),
                rs.getString("mailinglist_name"),
                rs.getString("description"),
                sendDate != null ? admin.getDateTimeFormat().format(sendDate) : "",
                rs.getInt("preview_component")
            );
        };
    }

    @Override
    public List<MailingDto> getSentAndScheduled(MailingDaoOptions opts, Admin admin) {
        List<Object> params = new ArrayList<>(List.of(
            admin.getCompanyID(),
            MaildropStatus.WORLD.getCodeString(),
            MediaTypeStatus.Active.getCode(),
            MediaTypeStatus.Active.getCode(),
            opts.getStartIncl(), opts.getEndExcl()));

        String sql = """
            SELECT * FROM (
                SELECT m.mailing_id mailingId, m.work_status, m.shortname,
                    MIN(md.senddate) senddate, MAX(md.genstatus) genstatus, mt.mediatype %s
                    %s
                FROM mailing_tbl m
                    LEFT JOIN maildrop_status_tbl md ON m.mailing_id = md.mailing_id
                    LEFT JOIN mailing_mt_tbl mt ON md.mailing_id = mt.mailing_id
                    %s
                WHERE m.company_id = ? AND m.deleted = 0 AND m.is_template = 0 AND md.status_field = ?
                    AND mt.status = ? AND mt.priority = (SELECT MIN(priority) FROM mailing_mt_tbl mt2 WHERE mt2.mailing_id = m.mailing_id AND mt2.status = ?)
                    AND md.senddate >= ? AND md.senddate < ?
                    %s
                GROUP BY m.mailing_id, m.shortname, mt.mediatype, m.work_status, md.senddate %s) subquery
            %s
            """.formatted(
            opts.getAdditionalCols(),
            getRowNumCalendarRestriction(opts.getLimit()),
            opts.isIncludeMailinglistName() ? "LEFT JOIN mailinglist_tbl ml ON m.mailinglist_id = ml.mailinglist_id" : "",
            getAdditionalSqlRestrictions(admin, params, "m"),
            opts.getGroupByCols(),
            opts.getLimit() > 0 ? " WHERE row_num <= " + opts.getLimit() : "");

        return select(addSortingToQuery(sql, "senddate", "desc"), getMailingDtoRowMapper(), params.toArray());
    }

    protected String getRowNumCalendarRestriction(int limit) {
        if (limit <= 0) {
            return "";
        }
        return ", ROW_NUMBER() OVER (PARTITION BY "
                + (isOracleDB() || isPostgreSQL() ? "TO_CHAR(senddate, 'YYYY-MM-DD')" : "DATE_FORMAT(senddate, '%Y-%m-%d')")
                + " ORDER BY senddate) AS row_num ";
    }

    @Override
    public List<ScheduledMailing> getScheduledMailings(Admin admin, Date startDate, Date endDate) {
        List<Object> params = new ArrayList<>(List.of(
                admin.getCompanyID(),
                String.valueOf(MaildropStatus.WORLD.getCode()),
                String.valueOf(MaildropStatus.ON_DEMAND.getCode()),
                startDate,
                endDate
        ));

        String query = "SELECT mailing.mailing_id mailing_id, mailing.work_status workstatus,"
                + " MIN(maildrop.senddate) send_date, mailing.shortname shortname, mailinglist.shortname mailinglist_name"
                + " FROM mailing_tbl mailing LEFT JOIN maildrop_status_tbl maildrop ON mailing.mailing_id = maildrop.mailing_id"
                + " LEFT JOIN mailing_mt_tbl mediatype ON maildrop.mailing_id = mediatype.mailing_id"
                + " LEFT JOIN mailinglist_tbl mailinglist ON mailing.mailinglist_id = mailinglist.mailinglist_id"
                + " WHERE mailing.company_id = ? AND mailing.deleted = 0 AND mailing.is_template = 0 AND maildrop.status_field IN (?, ?) AND mediatype.mediatype = 0"
                + (isOracleDB() ? " AND maildrop.senddate > CAST(? AS DATE) AND maildrop.senddate < CAST(? AS DATE)" : " AND maildrop.senddate > ? AND maildrop.senddate < ?")
                + (isOracleDB() ? " AND maildrop.senddate > SYSDATE" : " AND maildrop.senddate > NOW()")
                + getAdditionalSqlRestrictions(admin, params, MAILING_ALIAS)
                + " GROUP BY mailing.mailing_id, mailing.shortname, mailing.work_status, maildrop.senddate, mailinglist.shortname";
        query = addSortingToQuery(query, "MIN(maildrop.senddate)", "desc");

        return select(query, new ScheduledMailingRowMapper(), params.toArray());
    }

    @Override
    public List<MailingDto> getPlannedMailings(MailingDaoOptions opts, Admin admin) {
        List<Object> params = new ArrayList<>(List.of(
            admin.getCompanyID(),
            opts.getStartIncl(), opts.getEndExcl(),
            MediaTypeStatus.Active.getCode(),
            MediaTypeStatus.Active.getCode()));

        String sql = """
                %s
                SELECT m.mailing_id mailingId, m.work_status, m.plan_date senddate, m.shortname, mt.mediatype, -1 genstatus %s
                FROM mailing_tbl m
                    LEFT JOIN mailing_mt_tbl mt ON m.mailing_id = mt.mailing_id
                    LEFT JOIN maildrop_status_tbl md ON m.mailing_id = md.mailing_id
                    %s
                WHERE m.company_id = ?
                  AND m.deleted = 0
                  AND m.plan_date >= ?
                  AND m.plan_date < ?
                  AND mt.status = ? AND mt.priority = (SELECT MIN(priority) FROM mailing_mt_tbl mt2 WHERE mt2.mailing_id = m.mailing_id AND mt2.status = ?)
                  %s
                  AND (
                      md.senddate IS NULL OR (
                          md.senddate IS NOT NULL AND md.status_field IN ('A', 'T') AND
                          md.genchange = (SELECT max(mds.genchange)
                                                FROM maildrop_status_tbl mds
                                                WHERE mds.mailing_id = m.mailing_id) AND
                          NOT EXISTS (SELECT * FROM maildrop_status_tbl mds WHERE mds.mailing_id = m.mailing_id AND mds.status_field = 'W')
                      )
                  )
                GROUP BY m.mailing_id, m.shortname, mt.mediatype, m.work_status, m.plan_date, mt.mediatype %s
                %s
                """.formatted(
            opts.getLimit() > 0 ? "SELECT * FROM (SELECT mailingId, work_status, senddate, shortname, mediatype, genstatus " + getRowNumCalendarRestriction(opts.getLimit()) + " FROM (" : "",
            opts.getAdditionalCols(),
            opts.isIncludeMailinglistName() ? "LEFT JOIN mailinglist_tbl ml ON m.mailinglist_id = ml.mailinglist_id" : "",
            getAdditionalSqlRestrictions(admin, params, "m"),
            opts.getGroupByCols(),
            opts.getLimit() > 0 ? ") " + (isOracleDB() ? "" : "subsel ") + ") subsel2 WHERE row_num <= " + opts.getLimit() : "");
        return select(addSortingToQuery(sql, "mailingId", "desc"), getMailingDtoRowMapper(), params.toArray());
    }

    protected static RowMapper<MailingDto> getMailingDtoRowMapper() {
        return (rs, i) -> new MailingDto(
            rs.getInt("mailingId"),
            rs.getString("shortname"),
            MailingStatus.fromDbKey(rs.getString("work_status")),
            MediaTypes.getMediaTypeForCode(rs.getInt("mediatype")),
            resultsetHasColumn(rs, "mailinglist_name") ? rs.getString("mailinglist_name") : null,
            resultsetHasColumn(rs, "genstatus") ? MaildropGenerationStatus.fromCodeOrNull(rs.getInt("genstatus")) : null,
            resultsetHasColumn(rs, "senddate") ? rs.getTimestamp("senddate") : null
        );
    }

    public static class MailingRowMapper implements RowMapper<Mailing> {
        @Override
        public Mailing mapRow(ResultSet resultSet, int index) throws SQLException {
            final Mailing mailing = new MailingImpl();

            mailing.setCompanyID(resultSet.getInt("company_id"));
            mailing.setGridMailing(resultSet.getInt("is_grid") > 0);
            mailing.setId(resultSet.getInt("mailing_id"));
            mailing.setShortname(resultSet.getString("shortname"));
            final String contentTypeString = resultSet.getString("content_type");
            if (contentTypeString == null) {
                mailing.setMailingContentType(null);
            } else {
                mailing.setMailingContentType(MailingContentType.getFromString(contentTypeString));
            }
            mailing.setDescription(resultSet.getString("description"));
            mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
            mailing.setMailTemplateID(resultSet.getInt("mailtemplate_id"));
            mailing.setMailinglistID(resultSet.getInt("mailinglist_id"));
            mailing.setDeleted(resultSet.getInt("deleted"));
            mailing.setMailingType(MailingType.getByCode(resultSet.getInt("mailing_type")));
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

            if (resultsetHasColumn(resultSet, "senddate")) {
                mailing.setSenddate(resultSet.getTimestamp("senddate"));
            }

            return mailing;
        }
    }

    // some fields are missing. see com.agnitas.dao.impl.MailingDaoImpl.MailingRowMapper for more
    protected static class MailingLighterRowMapper implements RowMapper<Mailing> {
        @Override
        public Mailing mapRow(ResultSet resultSet, int index) throws SQLException {
            final Mailing mailing = new MailingImpl();

            mailing.setCompanyID(resultSet.getInt("company_id"));
            mailing.setId(resultSet.getInt("mailing_id"));
            mailing.setShortname(resultSet.getString("shortname"));
            final String contentTypeString = resultSet.getString("content_type");
            if (contentTypeString == null) {
                mailing.setMailingContentType(null);
            } else {
                mailing.setMailingContentType(MailingContentType.getFromString(contentTypeString));
            }
            mailing.setDescription(resultSet.getString("description"));
            mailing.setCreationDate(resultSet.getTimestamp("creation_date"));
            mailing.setMailTemplateID(resultSet.getInt("mailtemplate_id"));
            mailing.setMailinglistID(resultSet.getInt("mailinglist_id"));
            mailing.setDeleted(resultSet.getInt("deleted"));
            mailing.setMailingType(MailingType.getByCode(resultSet.getInt("mailing_type")));
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
        public MailingBase mapRow(ResultSet resultSet, int i) throws SQLException {
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

    protected static class ScheduledMailingRowMapper implements RowMapper<ScheduledMailing> {
        @Override
        public ScheduledMailing mapRow(ResultSet resultSet, int i) throws SQLException {
            ScheduledMailing mailing = new ScheduledMailing();

            mailing.setId(resultSet.getInt("mailing_id"));
            mailing.setShortname(resultSet.getString("shortname"));
            mailing.setWorkstatus(resultSet.getString("workstatus"));
            mailing.setMailinglistName(resultSet.getString("mailinglist_name"));
            mailing.setMaildropSendDate(resultSet.getTimestamp("send_date"));

            return mailing;
        }
    }

    private static class ComparisonMailingRowMapper implements RowMapper<MailingBase> {
        @Override
        public MailingBase mapRow(ResultSet resultSet, int i) throws SQLException {
            final MailingBase newBean = new MailingBaseImpl();
            newBean.setId(resultSet.getInt("mailing_id"));
            newBean.setShortname(resultSet.getString("shortname"));
            newBean.setDescription(resultSet.getString("description"));
            newBean.setSenddate(resultSet.getTimestamp("senddate"));
            newBean.setOnlyPostType(BooleanUtils.toBoolean(resultSet.getInt("isOnlyPostType")));

            return newBean;
        }
    }

    protected static class DynamicTagContentRowMapper implements RowMapper<DynamicTagContent> {
        @Override
        public DynamicTagContent mapRow(ResultSet resultSet, int index) throws SQLException {
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
        public DynamicTag mapRow(ResultSet resultSet, int index) throws SQLException {
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
    public Map<String, Object> getMailingWithWorkStatus(int mailingId, int companyId) {
        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT a.company_id, a.mailing_id, a.shortname, a.description, a.work_status, a.mailing_type, b.senddate, a.change_date, a.is_post AS isOnlyPostType");
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
        final List<Map<String, Object>> list = select(queryBuilder.toString(), companyId, mailingId);
        return !list.isEmpty() ? list.get(0) : new HashMap<>();
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void cleanTestDataInSuccessTbl(int mailingID, int companyID) {
        // delete from success_<companyId>_tbl
        final boolean individualSuccessTableExists = DbUtilities.checkIfTableExists(getDataSource(), "success_" + companyID + "_tbl");
        if (individualSuccessTableExists) {
        	String sqlBounce = "DELETE FROM success_" + companyID + "_tbl"
				+ " WHERE mailing_id = ?"
					+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
						+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
			update(sqlBounce, mailingID, mailingID);
        }
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void cleanTestDataInMailtrackTbl(int mailingID, int companyID) {
    	String sql = "DELETE FROM mailtrack_" + companyID + "_tbl"
			+ " WHERE mailing_id = ?"
				+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
					+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
		update(sql, mailingID, mailingID);
    }

    @Override
    @DaoUpdateReturnValueCheck
    public void deleteMailtrackDataForMailing(int companyID, int mailingID) {
		update("DELETE FROM mailtrack_" + companyID + "_tbl WHERE mailing_id = ?", mailingID);
    }

    protected String addSortingToQuery(String query, String sortField, String sortDirection) {
        String sorting = getSortingClause(sortField, sortDirection);

        if (sorting.isEmpty()) {
            return query;
        } else {
            return query + " " + sorting;
        }
    }

    private String getSortingClause(String column, String direction) {
        String expression = getSortingExpression(column, direction);

        if (expression.isEmpty()) {
            return "";
        } else {
            return "ORDER BY " + expression;
        }
    }

    private String getSortingExpression(String column, String direction) {
        if (StringUtils.isBlank(column)) {
            return "";
        }

        String directionKeyword = AgnUtils.sortingDirectionToBoolean(direction, true) ? "ASC" : "DESC";

        if (isOracleDB() || isPostgreSQL()) {
            return String.format("%s %s NULLS LAST", column, directionKeyword);
        } else {
            return String.format("ISNULL(%s), %s %s", column, column, directionKeyword);
        }
    }

    @Override
    public boolean isActiveIntervalMailing(int mailingId) {
        return selectInt("""
            SELECT COUNT(m.mailing_id)
            FROM interval_mailing_tbl i, mailing_tbl m
            WHERE m.mailing_id = ?
              AND m.mailing_id = i.mailing_id
              AND m.mailing_type = ?
              AND m.work_status = ?
              AND i.next_start IS NOT NULL
        """, mailingId, MailingType.INTERVAL.getCode(), MailingStatus.ACTIVE.getDbKey()) > 0;
    }

    @Override
    public MailingSendStatus getMailingSendStatus(int mailingID, int companyID) {
        final MailingSendStatus status = new MailingSendStatusImpl();

        final int daysToExpire = configService.getIntegerValue(ConfigValue.ExpireSuccess, companyID);
        status.setExpirationDays(daysToExpire);

        final String sqlGetSendStatus = "SELECT COUNT(*) FROM maildrop_status_tbl WHERE company_id = ? AND mailing_id = ? AND status_field NOT IN ('A', 'T')";

        final int sentEntries = selectInt(sqlGetSendStatus, companyID, mailingID);
        status.setHasMailtracks(sentEntries > 0);

        if (status.getHasMailtracks()) {
            final int dataEntries = selectInt("SELECT COUNT(*) FROM success_" + companyID + "_tbl WHERE mailing_id = ?", mailingID);
            status.setHasMailtrackData(dataEntries > 0);
        }

        return status;
    }

    @Override
    public List<Mailing> getMailingsForMLID(int companyID, int mailinglistID) {
        final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type, campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id, clickaction_id, statmail_recp, statmail_onerroronly, clearance_threshold, clearance_email, dynamic_template, plan_date"
                + " FROM mailing_tbl"
                + " WHERE company_id = ? AND mailinglist_id = ? AND deleted = 0";

        return select(sql, new MailingLighterRowMapper(), companyID, mailinglistID);
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
    protected String getSQLExpression(String targetExpression) {
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
                temp = select("SELECT target_sql FROM dyn_target_tbl WHERE target_id = ?", String.class, tid);
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
    public int findLastNewsletter(int customerID, int companyID, int mailinglist) {
        final String sql = "SELECT m.mailing_id, m.target_expression, a.timestamp"
                + " FROM mailing_tbl m"
                + " LEFT JOIN mailing_account_tbl a ON a.mailing_id = m.mailing_id"
                + " WHERE m.company_id = ? AND m.deleted = 0 AND m.is_template = 0 AND a.status_field = 'W' and m.mailinglist_id = ? ORDER BY a.timestamp desc, m.mailing_id DESC";

        try {
            final List<Map<String, Object>> list = select(sql, companyID, mailinglist);

            for (final Map<String, Object> map : list) {
                final int mailing_id = ((Number) map.get("mailing_id")).intValue();
                final String targetExpression = (String) map.get("target_expression");

                if (targetExpression == null || targetExpression.trim().isEmpty()) {
                    return mailing_id;
                } else {
                    if (selectInt("SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust WHERE " + getSQLExpression(targetExpression) + " AND customer_id = ?", customerID) > 0) {
                        return mailing_id;
                    }
                }
            }
            return 0;
        } catch (Exception e) {
            logger.error("findLastNewsletter: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public String getMailingRdirDomain(int mailingID, int companyID) {
        final String rdir_mailinglistquery = "SELECT ml.rdir_domain FROM mailinglist_tbl ml JOIN mailing_tbl m ON ml.mailinglist_id = m.mailinglist_id WHERE ml.deleted = 0 AND m.mailing_id = ?";
        final String rdirdomain = selectStringDefaultNull(rdir_mailinglistquery, mailingID);
        if (rdirdomain != null) {
            return rdirdomain;
        } else {
            final String rdir_companyquery = "SELECT rdir_domain FROM company_tbl WHERE company_id = ?";
            return select(rdir_companyquery, String.class, companyID);
        }
    }

    @Override
    public boolean isBasicFullTextSearchSupported() {
        if (BASICFULLTEXTSEARCHSUPPORTED_CACHE == null) {
        	BASICFULLTEXTSEARCHSUPPORTED_CACHE = checkIndicesAvailable("mailing$sname$idx", "mailing$descr$idx");
    	}
    	return BASICFULLTEXTSEARCHSUPPORTED_CACHE;
    }

    @Override
    public boolean isContentFullTextSearchSupported() {
    	if (FULLTEXTSEARCHSUPPORTED_CACHE == null) {
    		FULLTEXTSEARCHSUPPORTED_CACHE = checkIndicesAvailable("component$emmblock$idx", "dyncontent$content$idx");
    	}
    	return FULLTEXTSEARCHSUPPORTED_CACHE;
    }

    @Override
    public String getFormat(int type) {
        try {
            String format = selectStringDefaultNull("SELECT format FROM date_tbl WHERE type = ?", type);
            if (format == null) {
                logger.error("Query failed for data_tbl: No such type: {}", type);
                return "d.M.yyyy";
            } else {
                return format;
            }
        } catch (Exception e) {
            logger.error("Query failed for data_tbl: {}", e.getMessage(), e);
            return "d.M.yyyy";
        }
    }

    @Override
    public int getLastGenstatus(int mailingID, char... statuses) {
        final StringBuilder sqlQueryBuilder = new StringBuilder();
        final List<Object> sqlParameters = new ArrayList<>();

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

        return selectIntWithDefaultValue(
                addRowLimit(sqlQueryBuilder.toString(), 1),
                -1,
                sqlParameters.toArray()
        );
    }

    @Override
    public boolean hasPreviewRecipients(int mailingID, int companyID) {
        if (companyID <= 0 || mailingID <= 0) {
            return false;
        } else {
            final String query = "SELECT DISTINCT 1"
                    + " FROM mailing_tbl m, mailinglist_tbl ml, customer_" + companyID + "_binding_tbl c"
                    + " WHERE m.company_id = ml.company_id AND m.mailinglist_id = ml.mailinglist_id AND c.mailinglist_id = ml.mailinglist_id"
                    + " AND c.user_status = 1 AND c.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND ml.deleted = 0 AND m.company_id = ? AND m.mailing_id = ?";

            try {
                selectInt(query, companyID, mailingID);
                return true;
            } catch (Exception e) {
                logger.error("hasPreviewRecipients: mailingID = {}, companyID = {}", mailingID, companyID, e);
                return false;
            }
        }
    }

    private boolean hasActions(int mailingId, int companyID) {
        try {
            return selectInt("SELECT COUNT(action_id) FROM rdir_url_tbl WHERE mailing_id = ? AND company_id = ? AND action_id != 0", mailingId, companyID) > 0;
        } catch (Exception e) {
            logger.error("hasActions: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<MailingBase> getMailingTemplatesWithPreview(MailingTemplateSelectionFilter filter, Admin admin) {
        StringBuilder query = new StringBuilder("SELECT m.mailing_id, m.shortname, COALESCE(cmp.component_id, 0) AS preview_component, m.description, m.creation_date, m.is_post AS isOnlyPostType")
                .append(" FROM mailing_tbl m LEFT JOIN component_tbl cmp ON m.mailing_id = cmp.mailing_id AND cmp.comptype = ").append(MailingComponentType.ThumbnailImage.getCode())
                .append(" WHERE m.company_id = ? AND m.is_template = 1 AND m.deleted = 0");

        final List<Object> params = new ArrayList<>();
        params.add(admin.getCompanyID());

		if (configService.isDisabledMailingListsSupported()) {
            query.append(" AND m.mailinglist_id NOT IN ").append(DISABLED_MAILINGLIST_QUERY);
	        params.add(admin.getAdminID());
		}

        if (filter.getMediaType() != null) {
            query.append(" AND EXISTS(SELECT 1 FROM mailing_mt_tbl mt WHERE mt.mailing_id = m.mailing_id AND mt.mediatype = ? AND mt.status = ?)");
            params.addAll(List.of(filter.getMediaType(), MediaTypeStatus.Active.getCode()));
        }

        if (filter.getName() != null) {
            query.append(getPartialSearchFilterWithAnd("m.shortname", filter.getName(), params));
        }

        query.append(getTargetRestrictions(admin, params, "m"))
                .append(" ORDER BY ");

        query.append(getSortingExpression("m." + filter.getSort(), filter.getDir()));
        return select(query.toString(), new MailingTemplatesWithPreviewRowMapper(), params.toArray());
    }

    @Override
    public boolean clearPlanDate(int mailingId, int companyId) {
        return update("UPDATE mailing_tbl SET plan_date = NULL WHERE mailing_id = ? AND company_id = ?",
                mailingId, companyId) == 1;
    }

    @Override
    public List<MailingBase> getMailingsByStatusE(int companyID) {
        final List<Map<String, Object>> result = select("SELECT mail.mailing_id, mail.shortname, mail.is_post AS isOnlyPostType"
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
    public List<Integer> getTemplateReferencingMailingIds(Mailing mailTemplate) {
        if (!mailTemplate.isIsTemplate()) {
            // No template? Do nothing!
            return null;
        } else {
            final String query = "SELECT m.mailing_id FROM mailing_tbl m"
                    + " WHERE m.dynamic_template = 1 AND m.is_template = 0 AND m.mailtemplate_id = ? AND m.company_id = ? AND deleted = 0"
                    + " AND NOT EXISTS (SELECT 1 FROM maildrop_status_tbl mds WHERE mds.mailing_id = m.mailing_id AND mds.status_field IN ('w', 'W'))";
            return select(query, IntegerRowMapper.INSTANCE, mailTemplate.getId(), mailTemplate.getCompanyID());
        }
    }

    @Override
    public boolean checkMailingReferencesTemplate(int templateID, int companyID) {
        if (companyID <= 0) {
            throw new IllegalArgumentException("Invalid companyID");
        } else if (templateID <= 0) {
            throw new IllegalArgumentException("Invalid templateID");
        } else {
            return isTemplate(templateID, companyID);
        }
    }

    @Override
    public boolean isTemplate(int mailingId, int companyId) {
        return selectIntWithDefaultValue("SELECT is_template FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", 0, mailingId, companyId) == 1;
    }

    @Override
    public boolean isMarkedAsDeleted(int mailingId, int companyID) {
        return selectIntWithDefaultValue("SELECT deleted FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", 0, mailingId, companyID) == 1;
    }

    @Override
    public boolean exist(int mailingID, int companyID) {
        return selectInt("SELECT COUNT(*) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted = 0", companyID, mailingID) > 0;
    }

    @Override
    public boolean exist(int mailingID, int companyID, boolean isTemplate) {
        return selectInt("SELECT COUNT(*) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted = 0 AND is_template = ?", companyID, mailingID, isTemplate ? 1 : 0) > 0;
    }

    @Override
    public List<Mailing> getMailings(int companyId, boolean isTemplate) {
        final String sql = "SELECT company_id, mailing_id, shortname, content_type, description, deleted, mailtemplate_id, mailinglist_id, mailing_type,"
                + " campaign_id, archived, target_expression, split_id, creation_date, test_lock, is_template, needs_target, openaction_id,"
                + " clickaction_id, statmail_recp, statmail_onerroronly, clearance_threshold, clearance_email, dynamic_template, plan_date"
                + " FROM mailing_tbl"
                + " WHERE company_id = ? AND is_template = ? AND deleted = 0";
        return select(sql, new MailingLighterRowMapper(), companyId, isTemplate ? 1 : 0);
    }

    @Override
    public int getMailingOpenAction(int mailingID, int companyID) {
        try {
            return selectInt("SELECT openaction_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
        } catch (Exception e) {
            logger.error("Error while getting mailing open action ID (mailingID: {}, companyID: {})", mailingID, companyID, e);
            return 0;
        }
    }

    @Override
    public int getMailingClickAction(int mailingID, int companyID) {
        try {
            return selectInt("SELECT clickaction_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyID, mailingID);
        } catch (Exception e) {
            logger.error("Error while getting mailing click action ID (mailingID: {}, companyID: {})", mailingID, companyID, e);
            return 0;
        }
    }

    /**
     * returns the mailing-Parameter for the given mailing (only email, no sms or anything else).
     */
    @Override
    public String getEmailParameter(int mailingID) {
        try {
            return selectWithDefaultValue("SELECT param FROM mailing_mt_tbl WHERE mailing_id = ? AND mediatype = 0", String.class, null, mailingID);
        } catch (Exception e) {
            logger.error("getEmaiLParameter() failed for mailing {}", mailingID, e);
            return null;
        }
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(int companyID) {
        String sql = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl WHERE company_id = ? AND deleted = 0 AND is_template = 0";
        return select(sql, LightweightMailingRowMapper.INSTANCE, companyID);
    }

    @Override
    public List<LightweightMailing> getLightweightMailings(int companyId, Collection<Integer> mailingIds) {
        String sqlGetMailings = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type " +
                "FROM mailing_tbl " +
                "WHERE company_id = ? AND deleted = 0 AND " + makeBulkInClauseForInteger("mailing_id", mailingIds);

        return select(sqlGetMailings, LightweightMailingRowMapper.INSTANCE, companyId);
    }

    @Override
    public MailingType getMailingType(int mailingID) {
        return MailingType.findByCode(selectIntWithDefaultValue("SELECT mailing_type FROM mailing_tbl WHERE mailing_id = ?", -1, mailingID))
                .orElse(null);
    }

    @Override
    public List<Integer> getSampleMailingIDs() {
        return select("SELECT mailing_id FROM mailing_tbl WHERE company_id = 1 AND " +
                "(LOWER(shortname) LIKE '%sample%' " +
                "OR LOWER(shortname) LIKE '%example%' " +
                "OR LOWER(shortname) LIKE '%muster%' " +
                "OR LOWER(shortname) LIKE '%beispiel%') " +
                "AND deleted = 0", IntegerRowMapper.INSTANCE);
    }

    @Override
    public List<LightweightMailing> getMailingsByType(int mailingType, int companyID) {
        return getMailingsByType(mailingType, companyID, true);
    }

    @Override
    public List<LightweightMailing> getMailingsByType(int mailingType, int companyID, boolean includeInactive) {
        String sql = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl " +
                " WHERE company_id = ? AND deleted = 0 AND is_template = 0 AND mailing_type = ? ";

        if (!includeInactive) {
            sql += " AND work_status = '" + MailingStatus.ACTIVE.getDbKey() + "'";
        }

        return select(sql, LightweightMailingRowMapper.INSTANCE, companyID, mailingType);
    }

    @Override
    public String getMailingName(int mailingId, int companyId) {
        return selectStringDefaultNull("SELECT shortname FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", mailingId, companyId);
    }

    @Override
    public Map<Integer, String> getMailingNames(Collection<Integer> mailingIds, int companyId) {
        if (CollectionUtils.isEmpty(mailingIds) || companyId <= 0) {
            return Collections.emptyMap();
        }

        final String sqlGetNames = "SELECT mailing_id, shortname FROM mailing_tbl " +
                "WHERE company_id = ? AND mailing_id IN (" + StringUtils.join(mailingIds, ',') + ")";

        final Map<Integer, String> namesMap = new HashMap<>();
        query(sqlGetNames, rs -> namesMap.put(rs.getInt("mailing_id"), rs.getString("shortname")), companyId);
        return namesMap;
    }

    @Override
    public List<MailingArchiveEntry> listMailingArchive(int campaignId, DateRange sendDate, Integer countLimit, int companyId) {
        List<Object> params = new ArrayList<>(List.of(companyId, campaignId, MediaTypes.EMAIL.getMediaCode()));

        String query = """
                SELECT m.company_id, m.mailing_id, m.shortname, m.description, m.mailing_type, m.work_status, m.content_type,
                       mt.mediatype, mt.priority, mt.status, mt.param
                FROM mailing_tbl m, maildrop_status_tbl mds, mailing_mt_tbl mt
                WHERE status_field = 'W' AND deleted = 0 AND is_template = 0 AND m.company_id = ? AND campaign_id = ?
                  AND archived = 1 AND mt.mediatype = ? AND m.mailing_id = mds.mailing_id AND mt.mailing_id = m.mailing_id
                """ +
                getDateRangeFilterWithAnd("m.send_date", sendDate, params) +
                " ORDER BY senddate DESC, mailing_id DESC";

        return select(
                addRowLimit(query, countLimit),
                MailingArchiveEntryRowMapper.INSTANCE,
                params.toArray()
        );
    }

    @Override
    public int getAnyMailingIdForCompany(int companyID) {
        String query = addRowLimit("SELECT mailing_id FROM mailing_tbl WHERE company_id = ?", 1);
        return select(query, Integer.class, companyID);
    }

    @Override
    public int getMailinglistId(int mailingId, int companyId) {
        return selectInt("SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?", mailingId, companyId);
    }

    @Override
    public String getTargetExpression(int mailingId, int companyId) {
        return getTargetExpression(mailingId, companyId, false);
    }

    @Override
    public String getTargetExpression(int mailingId, int companyId, boolean appendListSplit) {
        String sqlGetTargets = "SELECT target_expression, split_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectObjectDefaultNull(sqlGetTargets, new TargetExpressionMapper(appendListSplit), mailingId, companyId);
    }

    @Override
    public Map<Integer, String> getTargetExpressions(int companyId, Set<Integer> mailingIds) {
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
    public Map<Integer, Set<Integer>> getTargetsUsedInContent(int companyId, Set<Integer> mailingIds) {
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

    private boolean validateTargetExpression(String expression) {
        if (StringUtils.isNotEmpty(expression)) {
            final int maxLength = databaseInformation.getColumnStringLength("mailing_tbl", "TARGET_EXPRESSION");
            if (expression.length() > maxLength) {
                logger.error("Target expression exceeds maximum length of {}", maxLength);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean setTargetExpression(int mailingId, int companyId, String targetExpression) {
        if (!validateTargetExpression(targetExpression)) {
            throw new TooManyTargetGroupsInMailingException(mailingId);
        }

        targetExpression = checkTargetExpressionForALTG(companyId, targetExpression);
        final String sqlSetExpression = "UPDATE mailing_tbl SET target_expression = ? WHERE company_id = ? AND mailing_id = ? AND deleted = 0";
        return update(sqlSetExpression, targetExpression, companyId, mailingId) > 0;
    }

    public void setMaildropService(MaildropService maildropService) {
        this.maildropService = maildropService;
    }

    private static class TargetExpressionMapper implements RowMapper<String> {

        private final boolean appendListSplit;

        public TargetExpressionMapper(boolean appendListSplit) {
            this.appendListSplit = appendListSplit;
        }

        @Override
        public String mapRow(ResultSet rs, int i) throws SQLException {
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
    public boolean isAdvertisingContentType(int companyId, int mailingId) {
        if (companyId <= 0 || mailingId <= 0) {
            return false;
        }

        final String fieldClause = String.format("COALESCE(content_type, '%s') content_type_value", ADVERTISING_TYPE);
        final String query = "SELECT " + fieldClause + " FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?";
        final String contentType = selectWithDefaultValue(query, String.class, "", companyId, mailingId);

        return StringUtils.isNotEmpty(contentType) && ADVERTISING_TYPE.equalsIgnoreCase(contentType);
    }

    @Override
    public boolean isTextVersionRequired(int companyId, int mailingId) {
        final String sqlGetIsTextVersionRequired = "SELECT is_text_version_required FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return BooleanUtils.toBoolean(selectInt(sqlGetIsTextVersionRequired, mailingId, companyId));
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
        public Map<String, Object> mapRow(ResultSet resultSet, int row) throws SQLException {
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

            if (resultsetHasColumn(resultSet, "templatename")) {
                newBean.put("templateName", resultSet.getString("templatename"));
            }

            if (resultsetHasColumn(resultSet, SUBJECT_COL_NAME)) {
                newBean.put(SUBJECT_COL_NAME, resultSet.getString(SUBJECT_COL_NAME));
            }

            if (resultsetHasColumn(resultSet, "archive")) {
                newBean.put("archive", resultSet.getString("archive"));
            }

            if (resultsetHasColumn(resultSet, "recipientscount")) {
                newBean.put("recipientsCount", resultSet.getInt("recipientscount"));
            }

            if (resultsetHasColumn(resultSet, "attachments_count")) {
                newBean.put("attachments_count", resultSet.getInt("attachments_count"));
            }

            if (resultsetHasColumn(resultSet, "plan_date")) {
                newBean.put("planDate", resultSet.getTimestamp("plan_date"));
            }

            if (hasActions(mailingID, companyID)) {
                newBean.put("hasActions", true);
            }

            if (resultsetHasColumn(resultSet, "target_expression")) {
                newBean.put("targetgroups", getTargetGroupsForTargetExpression(resultSet.getString("target_expression"), targetNameCache));
            }

            return newBean;
        }
    }

    @Override
    public Date getMailingSendDate(int companyID, int mailingID) {
        return select("SELECT MIN(mintime) FROM mailing_account_sum_tbl WHERE company_id = ? and mailing_id = ? AND status_field IN (?, ?, ?)", Date.class, companyID, mailingID, MaildropStatus.WORLD.getCodeString(), MaildropStatus.ACTION_BASED.getCodeString(), MaildropStatus.DATE_BASED.getCodeString());
    }

    @Override
    public List<LightweightMailingWithMailingList> listAllActionBasedMailingsForMailinglists(Set<Integer> mailinglistsIds, int companyID) {
        if (CollectionUtils.isEmpty(mailinglistsIds)) {
            return Collections.emptyList();
        }

        String query = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type, mailinglist_id FROM mailing_tbl WHERE company_id = ? AND mailing_type = ? AND deleted = 0 AND "
                + makeBulkInClauseForInteger("mailinglist_id", mailinglistsIds);
        return select(query, LightweightMailingWithMailinglistRowMapper.INSTANCE, companyID, MailingType.ACTION_BASED.getCode());
    }

    @Override
    public LightweightMailing getLightweightMailing(int companyID, int mailingID) {
        final String sql = "SELECT mailing_id, description, shortname, company_id, mailing_type, work_status, content_type FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";

        final List<LightweightMailing> list = select(sql, LightweightMailingRowMapper.INSTANCE, mailingID, companyID);

        if (list.isEmpty()) {
            logger.info("Unable to load mailing (mailing ID {}, company ID {})", mailingID, companyID);
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

        if (selectInt(sqlCheckExists, mailingId, companyId) > 0) {
            return update(sqlTryToLock, adminId, mailingId, companyId, adminId) > 0;
        } else {
            throw new MailingNotExistException(companyId, mailingId);
        }
    }

    @Override
    public int getMailingLockingAdminId(int mailingId, int companyId) {
        String query = "SELECT locking_admin_id FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectInt(query, mailingId, companyId);
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

        return select(query, LightweightMailingRowMapper.INSTANCE, companyId, companyId, templateId, MaildropStatus.WORLD.getCodeString());
    }

    protected void insertMailing(int companyId, Mailing mailing, String autoUrl) {
        if (mailing.getSplitID() < 0) {
            throw new IllegalArgumentException("SplitId should not be negative!");
        }

        mailing.setTargetExpression(checkTargetExpressionForALTG(companyId, mailing.getTargetExpression()));

        if (isOracleDB()) {
            mailing.setId(selectInt("SELECT mailing_tbl_seq.NEXTVAL FROM DUAL"));
            final Object[] params = {
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
            update("INSERT INTO mailing_tbl (mailing_id, company_id, is_grid, campaign_id, shortname, content_type, description, mailing_type, is_template, needs_target, mailtemplate_id, mailinglist_id, deleted, archived, test_lock, target_expression, split_id, work_status, dynamic_template, openaction_id, clickaction_id, creation_date, statmail_recp, statmail_onerroronly, plan_date, auto_url)"
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '" + MailingStatus.NEW.getDbKey() + "', ?, ?, ?, ?, ?, ?, ?, ?)", params);
        } else {
            final int newID = insert("mailing_id", "INSERT INTO mailing_tbl (company_id, is_grid, campaign_id, shortname, content_type, description, mailing_type, is_template, needs_target, mailtemplate_id, mailinglist_id, deleted, archived, test_lock, target_expression, split_id, work_status, dynamic_template, openaction_id, clickaction_id, creation_date, statmail_recp, statmail_onerroronly, plan_date, auto_url)"
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

    protected void performMailingUpdate(int companyId, Mailing mailing, String autoUrl) {
        if (mailing.getSplitID() < 0) {
            throw new IllegalArgumentException("SplitId should not be negative!");
        }

        mailing.setTargetExpression(checkTargetExpressionForALTG(companyId, mailing.getTargetExpression()));

        final Object[] params = {
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
        update("UPDATE mailing_tbl SET campaign_id = ?, shortname = ?, content_type = ?, description = ?, mailing_type = ?, is_template = ?, needs_target = ?, mailtemplate_id = ?, mailinglist_id = ?, deleted = ?, archived = ?, test_lock = ?, target_expression = ?, split_id = ?, dynamic_template = ?, change_date = "
		        + "CURRENT_TIMESTAMP, openaction_id = ?, clickaction_id = ?, statmail_recp = ?, statmail_onerroronly = ?, plan_date = ?, auto_url = ? WHERE mailing_id = ? AND company_id = ?", params);
    }

     protected Set<String> getMailingTblSelectableFields() {
       return new HashSet<>(SEPARATE_MAILING_FIELDS);
    }

    private String getMailingSqlSelectFields(String prefix) {
        Set<String> fields = getMailingTblSelectableFields();
        if (StringUtils.isNotBlank(prefix)) {
            return fields.stream()
                    .map(column -> prefix + column)
                    .collect(Collectors.joining(", "));
        }

        return StringUtils.join(fields, ", ");
    }

    protected MailingRowMapper getRowMapper() {
        return MAILING_ROW_MAPPER;
    }

    private String getNewLockingTimestampExpression(long durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Locking duration must be >= 1 second");
        }

        if (isOracleDB()) {
            return "(CURRENT_TIMESTAMP + INTERVAL '" + durationSeconds + "' SECOND)";
        } else if (isPostgreSQL()) {
            return "(CURRENT_TIMESTAMP + INTERVAL '" + durationSeconds + " SECONDS')";
        } else {
            return "DATE_ADD(CURRENT_TIMESTAMP, INTERVAL " + durationSeconds + " SECOND) ";
        }
    }

    private void performMailingSave(int companyId, Mailing mailing, String autoUrl) {
        // !=0 means we have already a mailing id. We got that by a sequence from the DB
        if (mailing.getId() != 0) {
            performMailingUpdate(companyId, mailing, autoUrl);
        } else {
            // ==0 means, we have a new mailing, the sequence returns us a mailing-id from the db.
            insertMailing(companyId, mailing, autoUrl);
        }
    }

    @Override
    public String getEmailSubject(int companyID, int mailingID) {
        return ((MediatypeEmail) mediatypesDao.loadMediatypes(mailingID, companyID).get(MediaTypes.EMAIL.getMediaCode())).getSubject();
    }

    @Override
    public MailingContentType getMailingContentType(int companyID, int mailingId) {
        // If this mailing was deleted oder does not exist content_type will be handled as null
        String contentTypeString = selectStringDefaultNull("SELECT content_type FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND deleted <= 0", companyID, mailingId);
        if (contentTypeString != null) {
            return MailingContentType.getFromString(contentTypeString);
        }
        return null;
    }

    @Override
    public boolean deleteMailingsByCompanyIDReally(int companyID) {
        // Do nothing in OpenEMM
        return true;
    }

    @Override
    public boolean isActiveIntervalMailing(int companyID, int mailingID) {
        return selectInt("SELECT COUNT(mailing_id) FROM mailing_tbl WHERE company_id = ? AND mailing_id = ? AND mailing_type = ? AND work_status = ?", companyID, mailingID, MailingType.INTERVAL.getCode(), MailingStatus.ACTIVE.getDbKey()) > 0;
    }

    @Override
    public boolean resumeDateBasedSending(int mailingId) {
        return update("UPDATE rulebased_sent_tbl SET clearance = 1, clearance_origin = ?, clearance_change = CURRENT_TIMESTAMP WHERE mailing_id = ?",
                AgnUtils.getHostName(), mailingId) > 0;
    }

    @Override
    public boolean isThresholdClearanceExceeded(int mailingId) {
        return selectInt("SELECT COUNT(clearance) FROM rulebased_sent_tbl WHERE mailing_id = ? AND clearance = 0", mailingId) > 0;
    }

    @Override
    public boolean isDateBasedMailingWasSentToday(int mailingId) {
        String query;
        if (isOracleDB()) {
            query = "SELECT COUNT(*) FROM rulebased_sent_tbl WHERE mailing_id = ? AND TRUNC(lastsent) = TRUNC(SYSDATE)";
        } else if (isPostgreSQL()) {
            query = "SELECT COUNT(*) FROM rulebased_sent_tbl WHERE mailing_id = ? AND CAST(lastsent AS DATE) = CURRENT_DATE";
        } else {
            query = "SELECT COUNT(*) FROM rulebased_sent_tbl WHERE mailing_id = ? AND DATE(lastsent) = CURDATE()";
        }

        return selectInt(query, mailingId) > 0;
    }

    @Override
    public void allowDateBasedMailingResending(int mailingId) {
        String query;
        if (isOracleDB()) {
            query = "DELETE FROM rulebased_sent_tbl WHERE mailing_id = ? AND TRUNC(lastsent) = TRUNC(SYSDATE)";
        } else if (isPostgreSQL()) {
            query = "DELETE FROM rulebased_sent_tbl WHERE mailing_id = ? AND CAST(lastsent AS DATE) = CURRENT_DATE";
        } else {
            query = "DELETE FROM rulebased_sent_tbl WHERE mailing_id = ? AND DATE(lastsent) = CURDATE()";
        }

        update(query, mailingId);
    }

    @Override
	public void removeApproval(int mailingID, int companyID) {
		final String sql = "UPDATE mailing_tbl SET test_lock=? WHERE mailing_id=? AND company_id=?";
		update(sql, 1, mailingID, companyID);
	}

	@Override
	public boolean isApproved(int mailingId, int companyId) {
        String query = "SELECT test_lock FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        return selectIntWithDefaultValue(query, 1, mailingId, companyId) == 0;
    }

    @Override
    public void restoreMailings(Collection<Integer> mailingIds, int companyID) {
        String query = "UPDATE mailing_tbl SET deleted = 0, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND deleted = 1 AND "
                + makeBulkInClauseForInteger("mailing_id", mailingIds);
        update(query, companyID);
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

			if (!trailingExpressionPart.isEmpty()) {
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
							logger.error("Found and replaced ALTG targetgroup '{}' within targetExpression '{}'", altgTargetID, targetExpression);
						}
					}
				}
				return leadingExpressionPart + trailingExpressionPartString;
			} else {
				return targetExpression;
			}
		} else {
			return targetExpression;
		}
	}

	@Override
	public List<Map<String, Object>> getMailingsMarkedAsDeleted(int companyID, Date deletedMailingExpire) {
		return select("SELECT mailing_id FROM mailing_tbl WHERE deleted = 1 AND company_id = ? AND change_date < ?", companyID, deletedMailingExpire);
	}

	@Override
	public void deleteOutdatedMailingData(int companyID, int mailingID) {
		update("DELETE FROM mailing_mt_tbl WHERE mailing_id = ?", mailingID);
		update("DELETE FROM serverprio_tbl WHERE mailing_id = ?", mailingID);
		update("DELETE FROM mailing_info_tbl WHERE mailing_id= ?", mailingID);
		update("DELETE FROM interval_mailing_tbl WHERE mailing_id= ?", mailingID);
		update("DELETE FROM mailing_grid_tbl WHERE mailing_id = ?", mailingID);
		update("DELETE FROM mailing_import_lock_tbl WHERE mailing_id = ?", mailingID);
		//update("DELETE FROM mailing_account_tbl WHERE mailing_id = ?", mailingID);
		update("DELETE FROM mailing_backend_log_tbl WHERE mailing_id = ? ", mailingID);
		update("DELETE FROM mailtrack_" + companyID + "_tbl WHERE mailing_id = ?", mailingID);
		update("DELETE FROM mailing_import_lock_tbl WHERE mailing_id = ?", mailingID);
	}

	@Override
	public void markMailingAsDataDeleted(int mailingID) {
		update("UPDATE mailing_tbl SET deleted = 2 WHERE mailing_id = ?", mailingID);
	}

	/**
	 * Only store changed shortname, description, archive_id and archived.
	 * Those are the values that are allowed for change even after a mailing was delivered
	 */
	@Override
	public boolean saveMailingDescriptiveData(Mailing mailing) {
		return update("UPDATE mailing_tbl SET shortname = ?, description = ?, campaign_id = ?, archived = ?, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND mailing_id = ?",
			mailing.getShortname(), mailing.getDescription(), mailing.getCampaignID(), mailing.getArchived(), mailing.getCompanyID(), mailing.getId()) > 0;
	}

    @Override
    public List<LightweightMailing> getMailingsUsingEmmAction(int actionId, int companyID) {
        String query = "SELECT DISTINCT m.company_id, m.mailing_id, m.shortname, m.description, m.mailing_type, m.work_status, m.content_type " +
                "FROM mailing_tbl m LEFT JOIN rdir_url_tbl ru ON ru.mailing_id = m.mailing_id AND ru.deleted = 0 " +
                "WHERE m.deleted = 0 AND m.company_id = ? AND (m.openaction_id = ? OR m.clickaction_id = ? OR ru.action_id = ?)";

        return select(query, LightweightMailingRowMapper.INSTANCE, companyID, actionId, actionId, actionId);
    }

	@Override
	public List<LightweightMailing> getMailingTemplates(int companyID) {
        String selectMailingTemplates = "SELECT company_id, mailing_id, shortname, description, mailing_type, work_status, content_type FROM mailing_tbl"
        	+ " WHERE company_id = ? AND deleted = 0 AND is_template = 1";
        return select(selectMailingTemplates, LightweightMailingRowMapper.INSTANCE, companyID);
    }

}
