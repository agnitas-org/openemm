/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.QueryAndParams;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.dao.impl.mapper.DoubleRowMapper;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.Tuple;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

public class MailingStatisticsDaoImpl extends BaseDaoImpl implements MailingStatisticsDao {

    private static final int HARD_BOUNCE_DETAIL_CODE = 510;

    private final TargetService targetService;

    public MailingStatisticsDaoImpl(TargetService targetService) {
    	this.targetService = Objects.requireNonNull(targetService);
    }

    @Override
    public int getNonOpeningClickers(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        return selectInt(buildOpenersClickersQuery(mailingId, companyId, targetSql, userTypes, timestamp, false));
    }

    @Override
    public int getOpeningClickers(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        return selectInt(buildOpenersClickersQuery(mailingId, companyId, targetSql, userTypes, timestamp, true));
    }

    private QueryAndParams buildOpenersClickersQuery(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp, boolean opening) {
        StringBuilder query = new StringBuilder(
                "SELECT COUNT(DISTINCT(r.customer_id)) FROM rdirlog_%d_tbl r".formatted(companyId)
        );
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON r.customer_id = cust.customer_id");
        }

        query.append(" WHERE r.mailing_id = ? AND r.customer_id <> 0");

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        query.append(getRangeFilterWithAnd("r.timestamp", timestamp, params));
        query.append(" AND ").append(opening ? "" : "NOT").append(" EXISTS (SELECT 1 FROM onepixellog_")
                .append(companyId).append("_tbl o WHERE o.mailing_id = r.mailing_id AND o.customer_id = r.customer_id)");

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyId).append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append("AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        return new QueryAndParams(query.toString(), params);
    }

    @Override
    public boolean isRevenueTableExists(int companyId) {
        return DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_%d_val_num_tbl".formatted(companyId));
    }

    @Override
    public boolean isMailtrackTableExists(int companyId) {
        return DbUtilities.checkIfTableExists(getDataSource(), "mailtrack_" + companyId + "_tbl");
    }

    @Override
    public double getRevenue(int mailingId, int companyId, String targetSql, DateRange timestamp) {
        StringBuilder query = new StringBuilder(
                "SELECT COALESCE(SUM(r.num_parameter), 0) FROM rdirlog_%d_val_num_tbl r".formatted(companyId)
        );
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON r.customer_id = cust.customer_id");
        }

        query.append(" WHERE r.mailing_id = ? AND r.page_tag = 'revenue' AND r.num_parameter IS NOT NULL");
        query.append(getRangeFilterWithAnd("r.timestamp", timestamp, params));

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        return selectObject(query.toString(), DoubleRowMapper.INSTANCE, params.toArray());
    }

    @Override
    public int countOfOnceSending(int mailingId, Date thresholdDate) {
        return selectInt("""
                SELECT COUNT(mailing_id)
                FROM maildrop_status_tbl
                WHERE mailing_id = ?
                  AND status_field IN ('W')
                  AND senddate < CURRENT_DATE
                  AND senddate >= ?
                """, mailingId, thresholdDate);
    }

    @Override
    public int getPeriodicallySendCount(int mailingId) {
        return selectInt("""
                SELECT COUNT(mst.mailing_id)
                FROM maildrop_status_tbl mst
                         JOIN mailing_tbl mt ON mst.mailing_id = mt.mailing_id
                WHERE mst.mailing_id = ?
                  AND mst.status_field IN ('C', 'E', 'R', 'D')
                  AND mt.work_status = ?
                  AND mst.senddate < CURRENT_DATE
                """, mailingId, MailingStatus.ACTIVE.getDbKey());
    }

    @Override
    public boolean hasSuccessTableData(int mailingId, int companyId) {
        return selectInt("SELECT COUNT(*) FROM success_%d_tbl WHERE mailing_id = ?".formatted(companyId), mailingId) > 0;
    }

    @Override
    public int getDeliveredCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        // Do not count by "distinct customer_id", because event based mailings (birthday mailings etc.) might be delivered multiple times
        StringBuilder query = new StringBuilder("SELECT COUNT(s.customer_id) AS counter FROM success_%d_tbl s".formatted(companyId));
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON s.customer_id = cust.customer_id");
        }

        query.append(" WHERE s.mailing_id = ?")
                .append(getRangeFilterWithAnd("s.timestamp", timestamp, params));

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyId).append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = s.mailing_id) AND bind.customer_id = s.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getSentCountFromMailtrackTbl(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM");
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(" customer_").append(companyId).append("_tbl cust,");
        }
        sql.append(" mailtrack_").append(companyId).append("_tbl track");
        sql.append(" WHERE track.mailing_id = ? AND track.maildrop_status_id <> 0");
        sql.append(getRangeFilterWithAnd("track.timestamp", dateRange, params));

        if (containsCustomerCondition(targetSql)) {
            sql.append(" AND cust.customer_id = track.customer_id");
        }
        sql.append(" AND (").append(targetSql).append(")");

        return selectIntWithDefaultValue(sql.toString(), 0, params.toArray());
    }

    @Override
    public int getSentCountForIntervalMailing(int mailingId, int companyId, String targetSql, DateRange dateRange) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM");
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (targetSql != null && targetSql.contains("cust.")) {
            sql.append(" customer_").append(companyId).append("_tbl cust,");
        }
        sql.append(" interval_track_").append(companyId).append("_tbl track");
        sql.append(" WHERE track.mailing_id = ?");
        sql.append(getRangeFilterWithAnd("track.send_date", dateRange, params));

        if (containsCustomerCondition(targetSql)) {
            sql.append(" AND cust.customer_id = track.customer_id");
        }
        if (MailingStatisticsDao.isTargetFilterRequired(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }
        return selectIntWithDefaultValue(sql.toString(), 0, params.toArray());
    }

    @Override
    public int getClickersCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT r.customer_id) FROM rdirlog_%d_tbl r".formatted(companyId));
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON r.customer_id = cust.customer_id");
        }

        query.append(" WHERE r.mailing_id = ? AND r.customer_id <> 0")
                .append(getRangeFilterWithAnd("r.timestamp", timestamp, params));

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyId).append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getOpenersCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp, Set<Integer> deviceIds) {
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT o.customer_id) FROM onepixellog_device_").append(companyId).append("_tbl o");
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON o.customer_id = cust.customer_id");
        }

        query.append(" WHERE o.mailing_id = ? AND o.customer_id <> 0");

        if (CollectionUtils.isNotEmpty(deviceIds)) {
            query.append(" AND ").append(makeBulkInClauseForInteger("device_id", deviceIds));
        }

        query.append(getRangeFilterWithAnd("o.creation", timestamp, params));

        if (MailingStatisticsDao.isTargetFilterRequired(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND EXISTS (SELECT 1 FROM customer_")
                    .append(companyId)
                    .append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getOpeningsCount(int mailingId, int companyId, Set<UserType> userTypes, String targetSql, DateRange dateRange) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM onepixellog_device_").append(companyId).append("_tbl o");
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(", customer_").append(companyId).append("_tbl cust");
            sql.append(" WHERE o.mailing_id = ? AND o.customer_id = cust.customer_id");
        } else {
            sql.append(" WHERE o.mailing_id = ?");
        }

        sql.append(getRangeFilterWithAnd("o.creation", dateRange, params));

        if (MailingStatisticsDao.isTargetFilterRequired(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            sql.append(" AND EXISTS (SELECT 1 FROM customer_")
                    .append(companyId)
                    .append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }
        return selectInt(sql.toString(), params.toArray());
    }

    @Override
    public int getClicksCount(int mailingId, int companyId, Set<UserType> userTypes, String targetSql, DateRange dateRange) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM rdirlog_" + companyId + "_tbl r");
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(", customer_").append(companyId).append("_tbl cust");
            sql.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            sql.append(" WHERE r.mailing_id = ?");
        }

        sql.append(getRangeFilterWithAnd("r.timestamp", dateRange, params));

        if (MailingStatisticsDao.isTargetFilterRequired(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            sql.append(" AND EXISTS (SELECT 1 FROM customer_")
                    .append(companyId)
                    .append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }
        return selectInt(sql.toString(), params.toArray());
    }

    @Override
    public int getAnonymousOpenings(int mailingId, int companyId, DateRange timestamp) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM onepixellog_device_")
                .append(companyId)
                .append("_tbl o WHERE o.mailing_id = ? AND o.customer_id = 0");

        List<Object> params = new ArrayList<>(List.of(mailingId));
        query.append(getRangeFilterWithAnd("o.creation", timestamp, params));

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getAnonymousClicks(int mailingId, int companyId, DateRange timestamp) {
        StringBuilder query = new StringBuilder(
                "SELECT COUNT(*) FROM rdirlog_%d_tbl r WHERE r.mailing_id = ? AND r.customer_id = 0".formatted(companyId)
        );

        List<Object> params = new ArrayList<>(List.of(mailingId));
        query.append(getRangeFilterWithAnd("r.timestamp", timestamp, params));

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getSentCountFromMailingAccount(int mailingId, DateRange timestamp, Set<MaildropStatus> maildropStatuses) {
        StringBuilder query = new StringBuilder("SELECT SUM(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = ?");
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (!maildropStatuses.isEmpty()) {
            query.append(" AND ").append(makeBulkInClause("status_field", maildropStatuses.size()));
            params.addAll(maildropStatuses.stream().map(MaildropStatus::getCodeString).toList());
        }

        query.append(getRangeFilterWithAnd("timestamp", timestamp, params));

        return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
    }

    @Override
    public int getHardBouncesCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) FROM bounce_tbl b");
        List<Object> params = new ArrayList<>(List.of(companyId, mailingId, HARD_BOUNCE_DETAIL_CODE));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON b.customer_id = cust.customer_id");
        }

        query.append(" WHERE b.company_id = ? AND b.mailing_id = ? AND b.detail >= ?");

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyId).append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = b.mailing_id) AND bind.customer_id = b.customer_id)");
            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        query.append(getRangeFilterWithAnd("b.timestamp", timestamp, params));

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getHardBouncesCountFromBindings(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) FROM customer_%d_binding_tbl b".formatted(companyId));
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON b.customer_id = cust.customer_id");
        }

        query.append(" WHERE b.exit_mailing_id = ? AND (b.user_remark = 'bounce' OR b.user_remark LIKE 'bounce:%') AND b.user_status = 2");

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND ").append(makeBulkInClause("b.user_type", userTypes.size()));
            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        query.append(" AND b.mailinglist_id = (SELECT mail.mailinglist_id FROM mailing_tbl mail WHERE mail.mailing_id = b.exit_mailing_id)");
        query.append(getRangeFilterWithAnd("b.timestamp", timestamp, params));

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public Optional<Integer> getMailAgeInDays(int mailingId, int companyId) {
        String query;
        if (isOracleDB()) {
            query = """
                    SELECT (sysdate - senddate)
                    FROM maildrop_status_tbl
                    WHERE company_id = ?
                      AND mailing_id = ?
                    ORDER BY DECODE(status_field, 'W', 1, 'R', 2, 'D', 2, 'E', 3, 'C', 3, 'T', 4, 'A', 4, 5), status_id DESC
                    """;
        } else {
            query = """
                    SELECT %s
                    FROM maildrop_status_tbl
                    WHERE company_id = ?
                      AND mailing_id = ?
                    ORDER BY CASE status_field
                                 WHEN 'W' THEN 1
                                 WHEN 'R' THEN 2
                                 WHEN 'D' THEN 2
                                 WHEN 'E' THEN 3
                                 WHEN 'C' THEN 3
                                 WHEN 'T' THEN 4
                                 WHEN 'A' THEN 4
                                 ELSE 5 END, status_id DESC
                    """.formatted(isPostgreSQL() ? "EXTRACT(DAY FROM CURRENT_TIMESTAMP - senddate)" : "TIMESTAMPDIFF(DAY, senddate, CURRENT_TIMESTAMP)");
        }

        return Optional.ofNullable(selectIntDefaultNull(addRowLimit(query, 1), companyId, mailingId));
    }

    @Override
    public int getOptOutsCount(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange timestamp) {
        StringBuilder query = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) FROM customer_%d_binding_tbl b".formatted(companyId));
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            query.append(" JOIN customer_").append(companyId).append("_tbl cust ON b.customer_id = cust.customer_id");
        }

        query.append(" WHERE b.exit_mailing_id = ? AND b.user_status IN (3, 4)");

        if (CollectionUtils.isNotEmpty(userTypes)) {
            query.append(" AND ").append(makeBulkInClause("b.user_type", userTypes.size()));
            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        query.append(" AND b.mailinglist_id = (SELECT mail.mailinglist_id FROM mailing_tbl mail WHERE mail.mailing_id = b.exit_mailing_id)");
        query.append(getRangeFilterWithAnd("b.timestamp", timestamp, params));

        if (StringUtils.isNotBlank(targetSql)) {
            query.append(" AND (").append(targetSql).append(")");
        }

        return selectInt(query.toString(), params.toArray());
    }

    @Override
    public int getFollowUpRecipientsCount(int mailingID, int baseMailing, String followUpType, int companyID) {
        return getFollowUpRecipients(mailingID, baseMailing, followUpType, companyID).size();
    }

    private List<Integer> getFollowUpRecipients(int mailingID, int baseMailing, String followUpType, int companyID) {
        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {
            // TODO NEW click calculation with target groups.
            // the maildrop-id must be the one of the 'base' mailing, which is
            // already sent. The given
            // mailing-id must be the one of the current (that means the
            // follow-up) mailing.
            return getClickers(baseMailing, mailingID, companyID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
            return getOpeners(baseMailing, mailingID, companyID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
            return getNonClickers(baseMailing, mailingID, companyID);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
            return getNonOpeners(baseMailing, mailingID, companyID);
        }

        return Collections.emptyList();
    }

	@Override
    public int getFollowUpRecipientsCount(int followUpFor, String followUpType, int companyID, String sqlTargetExpression) {
        int resultValue = 0;
        // What kind of followup do we have, choose the appropriate sql call...
        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_CLICKER.getKey())) {
            resultValue = getClickersCount(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_OPENER.getKey())) {
            resultValue = getOpenersCount(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER.getKey())) {
            resultValue = getNonClickersCount(companyID, followUpFor, sqlTargetExpression);
        }

        if (followUpType.equals(FollowUpType.TYPE_FOLLOWUP_NON_OPENER.getKey())) {
            resultValue = getNonOpenersCount(companyID, followUpFor, sqlTargetExpression);
        }

        return resultValue;
    }

    private int getClickersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getClickersQuery(companyID);
        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor);
    }

    private int getNonClickersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getNonClickersQuery(companyID);

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor, followUpFor);
    }

    private int getOpenersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getOpenersQuery(companyID);

        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor);
    }

    private int getNonOpenersCount(int companyID, int followUpFor, String sqlTargetExpression) {
        String query = getNonOpenersQuery(companyID);
        if (StringUtils.isNotBlank(sqlTargetExpression)) {
            query += " AND (%s)".formatted(sqlTargetExpression);
        }

        return selectInt(wrapIntoCountQuery(query), followUpFor, followUpFor);
    }

    private String wrapIntoCountQuery(String query) {
        return "SELECT COUNT(*) FROM (%s) c".formatted(query);
    }

    private List<Integer> getClickers(int baseMailingId, int followUpMailingId, int companyId) {
        String query = getClickersQuery(companyId) + getTargetAndSplitSqlCondition(followUpMailingId);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingId);
    }

    private String getClickersQuery(int companyId) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyId + "_binding_tbl bind, customer_" + companyId + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM rdirlog_" + companyId + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)";
    }

    private String getTargetAndSplitSqlCondition(int followUpMailingId) {
        String sqlCondition = "";

        String targetExpression = getTargetExpression(followUpMailingId);
        if (getTargetExpressionIds(targetExpression).length != 0) {
            String targetSql = createTargetStatementWithoutAnd(targetExpression);
            if (StringUtils.isNotBlank(targetSql)) {
                sqlCondition += " AND (%s)".formatted(targetSql);
            }
        }

        int listSplitId = getListSplitId(followUpMailingId);
        if (listSplitId != 0) {
            sqlCondition += " AND (%s)".formatted(createTargetStatementWithoutAnd(Integer.toString(listSplitId)));
        }

        return sqlCondition;
    }

    /**
     * returns the non-clickers. maildrop-id is the id of the BASE!
     * Mailing, not the actual followup!
     */
    private List<Integer> getNonClickers(int baseMailingID, int followUpMailingID, int companyID) {
        String query = getNonClickersQuery(companyID) + getTargetAndSplitSqlCondition(followUpMailingID);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingID, baseMailingID);
    }

    private String getNonClickersQuery(int companyID) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)" +
                " AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.mailing_id = ? AND cust.customer_id = rdir.customer_ID)";
    }

    private List<Integer> getOpeners(int baseMailingID, int followUpMailingID, int companyID) {
        String query = getOpenersQuery(companyID) + getTargetAndSplitSqlCondition(followUpMailingID);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingID);
    }

    private String getOpenersQuery(int companyID) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)";
    }

    private List<Integer> getNonOpeners(int baseMailingID, int followUpMailingID, int companyID) {
        String query = getNonOpenersQuery(companyID) + getTargetAndSplitSqlCondition(followUpMailingID);
        return select(query, IntegerRowMapper.INSTANCE, baseMailingID, baseMailingID);
    }

    private String getNonOpenersQuery(int companyID) {
        return "SELECT DISTINCT bind.customer_id FROM customer_" + companyID + "_binding_tbl bind, customer_" + companyID + "_tbl cust" +
                " WHERE bind.customer_id = cust.customer_id AND bind.user_status = 1" +
                " AND EXISTS (SELECT 1 FROM success_" + companyID + "_tbl succ WHERE succ.mailing_id = ? AND cust.customer_id = succ.customer_id)" +
                " AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl one WHERE one.mailing_id = ? AND cust.customer_id = one.customer_ID)";
    }

    private String createTargetStatementWithoutAnd(String targetString) {
        final int[] targetIDs = getTargetExpressionIds(targetString);
        final String operator = getTargetOperator(targetString);

        final StringBuilder targetSql = new StringBuilder();
        for (int targetID : targetIDs) {
            if (!targetSql.isEmpty()) {
                targetSql.append(" ").append(operator).append(" ");
            }

            targetSql.append(getTargetSql(targetID));
        }

        return targetSql.toString();
    }

    /**
     * This method returns an Array with all Target_Expression IDs from the
     * mailing_tbl.
     */
    private int[] getTargetExpressionIds(String targetExpression) {
        final Set<Integer> targetIds = TargetExpressionUtils.getTargetIds(targetExpression);
        return ArrayUtils.toPrimitive(targetIds.toArray(new Integer[0]));
    }

    /**
     * This method returns the Operator for the target String. If the
     * targetString contains "|" it returns "OR" and if it contains "&" it
     * returns "AND". WARNING! Mixing up targets will result in an exception!
     */
    private String getTargetOperator(String targetString) {
        if (targetString.contains("|") && targetString.contains("&")) {
            // Mix-Check. We don't support mixed target-group at this time (2011.02.01)
            throw new IllegalArgumentException("Unsupported Mixed Target Groups: " + targetString);
        } else if (targetString.contains("|")) {
            return "OR";
        } else {
            return "AND"; // this is the less dangerous version (=less recipients).
        }
    }

    private String getTargetExpression(int mailingId) {
        return selectStringDefaultNull("SELECT target_expression FROM mailing_tbl WHERE mailing_id = ?", mailingId);
    }

    /**
     * Retrieve list split ID for mailing ID.
     *
     * @param mailingId mailing ID
     * @return list split ID or 0
     */
    private int getListSplitId(int mailingId) {
        return selectIntWithDefaultValue("SELECT split_id FROM mailing_tbl WHERE mailing_id = ?", 0, mailingId);
    }

    private String getTargetSql(int targetId) {
        return selectWithDefaultValue("SELECT target_sql FROM dyn_target_tbl WHERE target_id = ?", String.class, "", targetId);
    }

    @Override
    public int getRecipientsCount(Mailing mailing) {
        return getRecipientsIds(mailing).size();
    }

    @Override
    public Set<Integer> getRecipientsIds(Mailing mailing) {
        MediatypeEmail mediatype = mailing.getEmailParam();
        if (mediatype != null && StringUtils.isNotBlank(mediatype.getFollowupFor())) {
            return Set.copyOf(getFollowUpRecipients(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), mailing.getCompanyID()));
        }

        Set<Integer> recipientsIds = new HashSet<>();

        Set<MediaTypes> alreadyCountedMediaTypes = new HashSet<>();
        for (MediaTypes mediaType : getActiveMediaTypesSortedByPrio(mailing)) {
            recipientsIds.addAll(getRecipientsByMediaType(mediaType, mailing, alreadyCountedMediaTypes));
            alreadyCountedMediaTypes.add(mediaType);
        }

        return recipientsIds;
    }

    @Override
	public Map<Integer, Integer> getSendStats(Mailing mailing, int companyId) {
		final Map<Integer, Integer> returnMap = new HashMap<>();
		returnMap.put(SEND_STATS_TEXT, 0);
		returnMap.put(SEND_STATS_HTML, 0);
		returnMap.put(SEND_STATS_OFFLINE, 0);

		final MediatypeEmail mediatype = mailing.getEmailParam();
		if (mediatype != null && StringUtils.isNotBlank(mediatype.getFollowupFor())) {
			returnMap.put(SEND_STATS_TEXT, getFollowUpRecipientsCount(mailing.getId(), Integer.parseInt(mediatype.getFollowupFor()), mediatype.getFollowUpMethod(), companyId));
			return returnMap;
		}

        Set<MediaTypes> alreadyCountedMediatypes = new HashSet<>();
        for (MediaTypes mediaType : getActiveMediaTypesSortedByPrio(mailing)) {
            if (mediaType == MediaTypes.EMAIL) {
                Map<MailType, Integer> emailRecipientNumbers = getMailingRecipientAmountsForEmail(mailing, alreadyCountedMediatypes);
                returnMap.put(SEND_STATS_TEXT, returnMap.get(SEND_STATS_TEXT) + emailRecipientNumbers.get(MailType.TEXT));
                returnMap.put(SEND_STATS_HTML, returnMap.get(SEND_STATS_HTML) + emailRecipientNumbers.get(MailType.HTML));
                returnMap.put(SEND_STATS_OFFLINE, returnMap.get(SEND_STATS_OFFLINE) + emailRecipientNumbers.get(MailType.HTML_OFFLINE));
            } else {
                Set<Integer> recipients = getRecipientsByMediaType(mediaType, mailing, alreadyCountedMediatypes);

                returnMap.putIfAbsent(mediaType.getMediaCode(), 0);
                returnMap.put(mediaType.getMediaCode(), returnMap.get(mediaType.getMediaCode()) + recipients.size());
            }
            alreadyCountedMediatypes.add(mediaType);
        }
        return returnMap;
	}

    private List<MediaTypes> getActiveMediaTypesSortedByPrio(Mailing mailing) {
        return mailing.getMediatypes().values().stream()
                .filter(x -> x.getStatus() == MediaTypeStatus.Active.getCode())
                .distinct()
                .sorted(Comparator.comparingInt(Mediatype::getPriority))
                .map(Mediatype::getMediaType)
                .toList();
    }

	private Map<MailType, Integer> getMailingRecipientAmountsForEmail(Mailing mailing, Set<MediaTypes> excludingMediaTypes) {
        Map<MailType, Integer> mailTypesCountsMap = getRecipientsByMailTypeMap(mailing, MediaTypes.EMAIL, excludingMediaTypes)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()));

        List.of(MailType.TEXT, MailType.HTML, MailType.HTML_OFFLINE)
                .forEach(mt -> mailTypesCountsMap.putIfAbsent(mt, 0));

        MailType mailType = MailType.getFromInt(mailing.getEmailParam().getMailFormat());
        if (mailType == MailType.TEXT) {
            mailTypesCountsMap.put(MailType.TEXT, mailTypesCountsMap.get(MailType.TEXT) + mailTypesCountsMap.get(MailType.HTML) + mailTypesCountsMap.get(MailType.HTML_OFFLINE));
            mailTypesCountsMap.put(MailType.HTML, 0);
            mailTypesCountsMap.put(MailType.HTML_OFFLINE, 0);
        } else if (mailType == MailType.HTML) {
            mailTypesCountsMap.put(MailType.HTML, mailTypesCountsMap.get(MailType.HTML) + mailTypesCountsMap.get(MailType.HTML_OFFLINE));
            mailTypesCountsMap.put(MailType.HTML_OFFLINE, 0);
        }

        return mailTypesCountsMap;
	}

    private Map<MailType, Set<Integer>> getRecipientsByMailTypeMap(Mailing mailing, MediaTypes mediaType, Set<MediaTypes> excludingMediaTypes) {
        int companyID = mailing.getCompanyID();

        String query = "SELECT DISTINCT cust.customer_id, cust.mailtype"
                + " FROM customer_" + companyID + "_tbl cust, " + "customer_" + companyID + "_binding_tbl bind"
                + " WHERE cust.customer_id = bind.customer_id"
                + " AND bind.mailinglist_id = ? AND bind.user_status = ? AND bind.mediatype = ?";

        String targetAndSplitSql = targetService.getSQLFromTargetExpression(
                mailing.getTargetExpression(),
                mailing.getSplitID(),
                mailing.getCompanyID()
        );

        if (StringUtils.isNotBlank(targetAndSplitSql)) {
            query += " AND (" + targetAndSplitSql + ")";
        }

        if (mailing.isEncryptedSend()) {
            query += " AND cust.sys_encrypted_sending = 1";
        }

        if (CollectionUtils.isNotEmpty(excludingMediaTypes)) {
            String joinedMailTypes = excludingMediaTypes.stream()
                    .map(x -> Integer.toString(x.getMediaCode()))
                    .collect(Collectors.joining(", "));

            query += " AND NOT EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind2"
                    + " WHERE cust.customer_id = bind2.customer_id"
                    + " AND bind2.mediatype IN (" + joinedMailTypes + "))";
        }

        query += " ORDER BY cust.mailtype";

        List<Tuple<MailType, Integer>> rows = select(
                query,
                (rs, rowNum) -> Tuple.of(MailType.getFromInt(rs.getInt("mailtype")), rs.getInt("customer_id")),
                mailing.getMailinglistID(),
                UserStatus.Active.getStatusCode(),
                mediaType.getMediaCode()
        );

        return rows.stream().
                collect(Collectors.groupingBy(
                        Tuple::getFirst,
                        Collectors.mapping(Tuple::getSecond, Collectors.toSet())
                ));
    }

    private Set<Integer> getRecipientsByMediaType(MediaTypes mediaType, Mailing mailing, Set<MediaTypes> excludingMediaTypes) {
        return getRecipientsByMailTypeMap(mailing, mediaType, excludingMediaTypes).values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private static boolean containsCustomerCondition(String targetSql) {
        return Strings.CS.contains(targetSql, "cust.");
    }

    @Override
    public boolean isIntervalTrackDataExists(int mailingId, int companyId) {
        return select("""
                        SELECT COUNT(*)
                        FROM interval_track_%d_tbl
                        WHERE mailing_id = ?
                        """.formatted(companyId),
                Integer.class,
                mailingId
        ) > 0;
    }

    @Override
    public boolean isTrackingAvailableForMailing(int mailingId, int companyId) {
        return selectInt(
                "SELECT COUNT(mailing_id) FROM mailtrack_%d_tbl WHERE mailing_id = ?".formatted(companyId),
                mailingId
        ) > 0;
    }

    /**
     * Selects a map with numbers of openers per device class for a single mailing id.
     * Watch out:
     * If a customer is within one of these device classes, then he hasn't opened the mail with any other deviceclass.
     * Mixed combination openers are explicitly excluded by intent.
     * This means: totalOpeners - deviceclass1_openers - deviceclass2_openers ... - deviceclassX_openers
     * = openers that opened this mailing with any combination of more than one deviceclass
     */
    @Override
    public Map<DeviceClass, Integer> getOpenersByDevice(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange dateRange) {
        StringBuilder sql = new StringBuilder("SELECT o.device_class_id AS deviceClassId, COUNT(DISTINCT o.customer_id) AS counter FROM onepixellog_device_%d_tbl o".formatted(companyId));
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(", customer_").append(companyId).append("_tbl cust");
        }
        sql.append(" WHERE o.mailing_id = ?");
        if (containsCustomerCondition(targetSql)) {
            sql.append(" AND o.customer_id = cust.customer_id");
        }
        // Exclude anonymous entries
        sql.append(" AND o.customer_id <> 0");

        // Exclude clickers with combinations of deviceclasses
        sql.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_device_%d_tbl opl WHERE opl.device_class_id != o.device_class_id AND opl.mailing_id = o.mailing_id AND opl.customer_id = o.customer_id)".formatted(companyId));

        sql.append(getRangeFilterWithAnd("o.creation", dateRange, params));

        if (MailingStatisticsDao.isTargetFilterRequired(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            sql.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyId).append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        sql.append(" GROUP BY o.device_class_id");

        return getDeviceClassMap(sql, params);
    }

    private Map<DeviceClass, Integer> getDeviceClassMap(StringBuilder sql, List<Object> params) {
        Map<DeviceClass, Integer> returnMap = new EnumMap<>(DeviceClass.class);
        for (DeviceClass deviceClass : AVAILABLE_DEVICE_CLASSES) {
            returnMap.put(deviceClass, 0);
        }

        select(sql.toString(), (rs, rowNum) -> {
            // Some old entries dont't have a deviceclassid, those are desktop values
            DeviceClass deviceClass = DeviceClass.fromIdWithDefault(rs.getInt("deviceClassId"), DeviceClass.DESKTOP);
            returnMap.put(deviceClass, rs.getInt("counter"));
            return null;
        }, params.toArray());

        return returnMap;
    }

    /**
     * Selects a map with numbers of clickers per deviceclass for a single mailingid.
     * Watch out:
     * If a customer is within one of these deviceclasses, then he hasn't clicked in the mail with any other deviceclass.
     * Mixed combination clickers are explicitly excluded by intent.
     * This means: totalClickers - deviceclass1_clickers - deviceclass2_clickers ... - deviceclassX_clickers
     * = clickers that clicked a link of this mailing with any combination of more than one deviceclass
     */
    @Override
    public Map<DeviceClass, Integer> getClickersByDevice(int mailingId, int companyId, String targetSql, Set<UserType> userTypes, DateRange dateRange) {
        StringBuilder sql = new StringBuilder("SELECT r.device_class_id AS deviceClassId, COUNT(DISTINCT r.customer_id) AS counter FROM rdirlog_%d_tbl r".formatted(companyId));
        List<Object> params = new ArrayList<>(List.of(mailingId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(", customer_").append(companyId).append("_tbl cust");
        }
        sql.append(" WHERE r.mailing_id = ?");
        if (containsCustomerCondition(targetSql)) {
            sql.append(" AND r.customer_id = cust.customer_id");
        }
        // Exclude anonymous entries.
        sql.append(" AND r.customer_id <> 0");

        // Exclude clickers with combinations of deviceclasses
        sql.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyId
                   + "_tbl rdir WHERE rdir.device_class_id != r.device_class_id AND rdir.mailing_id = r.mailing_id AND rdir.customer_id = r.customer_id)");

        sql.append(getRangeFilterWithAnd("r.timestamp", dateRange, params));

        if (MailingStatisticsDao.isTargetFilterRequired(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }

        if (CollectionUtils.isNotEmpty(userTypes)) {
            sql.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyId).append("_binding_tbl bind WHERE ")
                    .append(makeBulkInClause("bind.user_type", userTypes.size()))
                    .append(" AND bind.mailinglist_id IN (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");

            params.addAll(userTypes.stream().map(UserType::getTypeCode).toList());
        }

        sql.append(" GROUP BY r.device_class_id");

        return getDeviceClassMap(sql, params);
    }
}
