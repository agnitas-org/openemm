/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.stats.recipient;

import static com.agnitas.util.DbUtilities.getCustomerBindingTableName;
import static com.agnitas.util.DbUtilities.getHstCustomerBindingTableName;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.Tuple;
import com.agnitas.util.importvalues.MailType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
class RecipientStatsDaoImpl extends BaseDaoImpl implements RecipientStatsDao {

    private static final RowMapper<BindStatusDetails> STATUS_AMOUNT_MAPPER = (rs, rowNum) -> new BindStatusDetails(
            rs.getTimestamp("time").toInstant(),
            UserStatus.getUserStatusOrNull(rs.getInt("userstatus")),
            rs.getLong("amount")
    );

    private final ConfigService configService;

    public RecipientStatsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            JavaMailService javaMailService,
            ConfigService configService
    ) {
        super(dataSource, javaMailService);
        this.configService = configService;
    }

    @Override
    public List<BindStatusDetails> getRecipientStatusCounts(RecipientStatsFilter filter) {
        List<Object> params = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
                SELECT bind.timestamp AS time,
                       bind.user_status AS userstatus,
                       COUNT(*) AS amount
                FROM customer_%d_binding_tbl bind
                """.formatted(filter.companyId()));

        if (!isBlank(filter.targetSql())) {
            sql.append(" JOIN customer_%d_tbl cust ON bind.customer_id = cust.customer_id".formatted(filter.companyId()));
        }

        sql.append(" WHERE ").append(getDateRangeFilter("bind.timestamp", filter.period(), params).orElse(""));
        sql.append(andClause(filter.targetSql()));

        // bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries

        sql.append(getMailinglistFilter(filter.mailinglistId(), "bind", params));

        if (filter.mediaType() != null && filter.mediaType() != MediaTypes.EMAIL) {
            sql.append(" AND bind.mediatype = ?");
            params.add(filter.mediaType().getMediaCode());
        } else if (filter.mediaType() == MediaTypes.EMAIL) {
            sql.append(" AND (bind.mediatype = 0 OR bind.mediatype IS NULL)");
        }
        if (DbUtilities.checkIfTableExists(getDataSource(), "ahv_" + filter.companyId() + "_tbl")) {
            // Exclude bounced recipients that are included in AHV process
            sql.append(" AND (bind.user_status != %s OR bind.customer_id NOT IN (SELECT customer_id FROM ahv_%d_tbl WHERE bouncecount > 1))"
                    .formatted(UserStatus.Bounce.getStatusCode(), filter.companyId()));
        }
        sql.append(" GROUP BY bind.timestamp, bind.user_status");
        sql.append(" ORDER BY bind.timestamp, bind.user_status");

        Object[] paramsArray = params.toArray(new Object[0]);
        List<BindStatusDetails> result = select(sql.toString(), STATUS_AMOUNT_MAPPER, paramsArray);

        if (configService.getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, filter.companyId())) {
            result.addAll(select(adaptProgressSqlForHst(filter.companyId(), sql), STATUS_AMOUNT_MAPPER, paramsArray));
        }
        return result;
    }

    private static String adaptProgressSqlForHst(int companyId, StringBuilder sql) {
        return sql.toString().replace(
                getCustomerBindingTableName(companyId),
                getHstCustomerBindingTableName(companyId)
        );
    }

    @Override
    public Map<MailType, Long> getMailtypeCounts(RecipientStatsFilter filter) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT cust.mailtype,
                       COUNT(DISTINCT cust.customer_id) AS amount
                FROM customer_%d_tbl cust
                   JOIN customer_%d_binding_tbl bind ON bind.customer_id = cust.customer_id
                WHERE bind.user_status = 1
                  AND cust.mailtype in (0, 1, 2)""".formatted(filter.companyId(), filter.companyId()));

        // bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries
        sql.append(andClause(filter.targetSql()));

        if (filter.mailinglistId() > 0) {
            sql.append(" AND bind.mailinglist_id = ?");
            params.add(filter.mailinglistId());
        }
        sql.append(" GROUP BY cust.mailtype");
        sql.append(" ORDER BY cust.mailtype");
        return selectLinkedMap(sql.toString(), (rs, i) -> new Tuple<>(
                MailType.getFromInt(rs.getInt("mailtype")),
                rs.getLong("amount")
        ), params.toArray(new Object[0]));
    }

    @Override
    public Map<UserStatus, Long> getUserStatusCounts(RecipientStatsFilter filter) {
        List<Object> params = new ArrayList<>();
        @SuppressWarnings("java:S1640") // allow null as map key
        Map<UserStatus, Long> result = new HashMap<>();
        query("""
                        SELECT bind.user_status                 AS userstatus,
                               COUNT(DISTINCT cust.customer_id) AS amount
                        FROM customer_%d_tbl cust
                          LEFT JOIN customer_%d_binding_tbl bind ON (cust.customer_id = bind.customer_id)
                        WHERE (bind.user_status IN (0, 1, 2, 3, 4, 5, 6, 7) OR bind.user_status IS NULL)
                          AND cust.%s = 0
                          AND NOT EXISTS (
                            SELECT 1 FROM customer_%d_binding_tbl bind2
                            WHERE cust.customer_id = bind2.customer_id
                              AND bind2.user_status < bind.user_status
                              %s
                          )
                          %s
                          %s
                        GROUP BY bind.user_status""".formatted(
                        filter.companyId(),
                        filter.companyId(),
                        RecipientStandardField.Bounceload.getColumnName(),
                        filter.companyId(),
                        getMailinglistFilter(filter.mailinglistId(), "bind2", params),
                        andClause(filter.targetSql()),
                        getMailinglistFilter(filter.mailinglistId(), "bind", params)
                ),
                rs -> result.merge(
                        UserStatus.findByCode(rs.getInt("userstatus")).orElse(null),
                        rs.getLong("amount"),
                        Long::sum
                ), params.toArray(new Object[0])
        );
        return result;
    }

    @Override
    public long getTotalDoiCount(RecipientStatsFilter filter) {
        long count1 = selectLong("""
                        SELECT COUNT(*)
                        FROM customer_%d_binding_tbl
                        WHERE mailinglist_id = ?
                          AND user_status = ?
                          AND timestamp > ?
                          AND timestamp < ?""".formatted(filter.companyId()),
                filter.mailinglistId(),
                UserStatus.WaitForConfirm.getStatusCode(),
                filter.period().getFrom(),
                filter.period().getTo()
        );
        long count2 = selectLong("""
                        SELECT COUNT(*)
                        FROM hst_customer_%d_binding_tbl
                        WHERE mailinglist_id = ?
                          AND user_status = ?
                          AND timestamp > ? AND timestamp < ?""".formatted(filter.companyId()),
                filter.mailinglistId(),
                UserStatus.WaitForConfirm.getStatusCode(),
                filter.period().getFrom(),
                filter.period().getTo());
        return count1 + count2;
    }

    @Override
    public long getNotConfirmedDoiCount(RecipientStatsFilter filter) {
        return selectLong("""
                        SELECT COUNT(*)
                        FROM customer_%d_binding_tbl
                        WHERE mailinglist_id = ?
                          AND user_status = ?
                          AND timestamp > ?
                          AND timestamp < ?""".formatted(filter.companyId()),
                filter.mailinglistId(),
                UserStatus.WaitForConfirm.getStatusCode(),
                filter.period().getFrom(),
                filter.period().getTo()
        );
    }

    @Override
    public long getNotConfirmedAndDeletedDoiCount(RecipientStatsFilter filter) {
        return selectLong("""
                        SELECT COUNT(*)
                        FROM hst_customer_%d_binding_tbl
                        WHERE mailinglist_id = ?
                          AND user_status = ?
                          AND timestamp > ?
                          AND timestamp < ?
                          AND change_type = 0""".formatted(filter.companyId()),
                filter.mailinglistId(),
                UserStatus.WaitForConfirm.getStatusCode(),
                filter.period().getFrom(),
                filter.period().getTo()
        );
    }

    @Override
    public long getConfirmedDoiCount(RecipientStatsFilter filter) {
        return selectInt("""
                        SELECT COUNT(*)
                        FROM customer_%d_binding_tbl bind
                        WHERE mailinglist_id = ?
                          AND user_status = ?
                          AND EXISTS (
                            SELECT 1
                            FROM hst_customer_%d_binding_tbl hst
                            WHERE hst.mailinglist_id = bind.mailinglist_id
                              AND user_status = ?
                              AND timestamp > ?
                              AND timestamp < ?
                              AND bind.customer_id = hst.customer_id
                        )""".formatted(filter.companyId(), filter.companyId()),
                filter.mailinglistId(),
                UserStatus.Active.getStatusCode(),
                UserStatus.WaitForConfirm.getStatusCode(),
                filter.period().getFrom(),
                filter.period().getTo()
        );
    }

    @Override
    public long getConfirmedAndNotActiveDoiCount(RecipientStatsFilter filter) {
        return selectInt("""
                        SELECT COUNT(*)
                        FROM customer_%d_binding_tbl bind
                        WHERE mailinglist_id = ?
                          AND user_status IN (2,3,4,6,7)
                          AND EXISTS (
                            SELECT 1
                            FROM hst_customer_%d_binding_tbl hst
                            WHERE hst.mailinglist_id = bind.mailinglist_id
                              AND user_status = ?
                              AND timestamp > ?
                              AND timestamp < ?
                              AND bind.customer_id = hst.customer_id
                        )""".formatted(filter.companyId(), filter.companyId()),
                filter.mailinglistId(),
                UserStatus.WaitForConfirm.getStatusCode(),
                filter.period().getFrom(),
                filter.period().getTo()
        );
    }

    private static String getMailinglistFilter(int mailinglist, String alias, List<Object> params) {
        if (mailinglist <= 0) {
            return "";
        }
        params.add(mailinglist);
        return " AND %s.mailinglist_id = ?".formatted(alias);
    }
}
