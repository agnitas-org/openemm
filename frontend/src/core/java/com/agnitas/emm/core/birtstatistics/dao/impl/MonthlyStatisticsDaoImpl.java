/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.agnitas.beans.QueryAndParams;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dao.MonthlyStatisticsDao;
import com.agnitas.emm.core.birtstatistics.dto.MonthlyMailingDetailStatistics;
import com.agnitas.emm.core.birtstatistics.monthly.MonthlyStatType;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.importvalues.MailType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MonthlyStatisticsDaoImpl extends BaseDaoImpl implements MonthlyStatisticsDao {

    public MonthlyStatisticsDaoImpl(@Qualifier("dataSource") DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }

    @Override
    public Map<MaildropStatus, MaildropStatusSummary> getMaildropStatusStatistics(int adminId, int companyId, DateRange timestamp) {
        Map<MaildropStatus, MaildropStatusSummary> resultMap = new EnumMap<>(MaildropStatus.class);

        QueryAndParams baseQuery = getStatusStatisticsBaseQuery(adminId, companyId, timestamp);

        String query = baseQuery.sql() +
                " AND a.status_field NOT IN ('A', 'T') GROUP BY a.status_field" +
                " UNION " +
                baseQuery.sql() +
                " AND a.status_field IN ('A', 'T') AND EXISTS (" +
                """
                        SELECT 1
                        FROM mailing_account_tbl b
                        WHERE b.mailing_id = a.mailing_id
                          AND b.status_field NOT IN ('A', 'T')
                          AND b.timestamp >= ?
                          AND b.timestamp < ?
                        """ +
                ") GROUP BY a.status_field";

        List<Object> params = new ArrayList<>();
        params.addAll(baseQuery.params());
        params.addAll(baseQuery.params());
        params.addAll(List.of(timestamp.getFrom(), timestamp.getTo()));

        query(query, rs ->
                resultMap.put(MaildropStatus.fromCode(
                        rs.getString("status_field")),
                        new MaildropStatusSummary(
                                rs.getInt("mailings_count"),
                                rs.getInt("emails_count"),
                                rs.getDouble("kbPerMail")
                        )
                ), params.toArray());

        return resultMap;
    }

    private QueryAndParams getStatusStatisticsBaseQuery(int adminId, int companyId, DateRange timestamp) {
        List<Object> params = new ArrayList<>();

        String baseQuery = """
                SELECT COUNT(DISTINCT a.mailing_id)                            mailings_count,
                       SUM(a.no_of_mailings)                                   emails_count,
                       SUM(a.no_of_bytes) / SUM(a.no_of_mailings) * 1.0 / 1024 kbPerMail,
                       a.status_field
                FROM mailing_account_tbl a
                """;

        if (isDisabledMailinglistsTableExists()) {
            baseQuery += " JOIN mailing_tbl m ON a.mailing_id = m.mailing_id";
            baseQuery += " AND m.mailinglist_id NOT IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?)";
            params.add(adminId);
        }

        baseQuery += " WHERE a.company_id = ? AND a.timestamp >= ? AND a.timestamp < ?";

        params.addAll(List.of(companyId, timestamp.getFrom(), timestamp.getTo()));

        return new QueryAndParams(baseQuery, params);
    }

    @Override
    public List<MonthlyMailingDetailStatistics> getDetailedMailingsStatistics(int adminId, int companyID, MonthlyStatType statisticType, DateRange timestamp) {
        QueryAndParams subQuery = buildStatisticsSubquery(statisticType, companyID, timestamp);
        StringBuilder query = new StringBuilder("""
                SELECT m.mailing_id,
                       m.shortname,
                       m.description,
                       a.mailtype,
                       SUM(a.no_of_mailings)                                   emails_count,
                       SUM(a.no_of_bytes) / SUM(a.no_of_mailings) * 1.0 / 1024 kbPerMail,
                       MIN(a.timestamp)                                        send_date,
                       %s                                                      AS total
                FROM mailing_tbl m, mailing_account_tbl a
                WHERE m.company_id = ?
                  AND m.mailing_id = a.mailing_id
                  AND a.no_of_mailings <> 0
                """.formatted(subQuery.sql()));

        List<Object> params = new ArrayList<>(subQuery.params());
        params.add(companyID);

        query.append(getDateRangeFilterWithAnd("a.timestamp", timestamp, params));

        if (isDisabledMailinglistsTableExists()) {
            query.append(" AND m.mailinglist_id NOT IN (SELECT mailinglist_id FROM disabled_mailinglist_tbl WHERE admin_id = ?)");
            params.add(adminId);
        }

        query.append(" GROUP BY m.mailing_id, m.shortname, m.description, a.mailtype")
                .append(", ")
                .append(
                        isOracleDB() || isPostgreSQL()
                                ? "TO_CHAR(a.timestamp, 'DD.MM.YYYY')"
                                : "DATE_FORMAT(a.timestamp, '%d.%m.%Y')"
                );

        query.append(" ORDER BY total desc, send_date, m.shortname");

        return select(query.toString(), (resultSet, rowNum) ->
                new MonthlyMailingDetailStatistics(
                        resultSet.getInt("mailing_id"),
                        resultSet.getString("shortname"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("send_date"),
                        resultSet.getDouble("kbPerMail"),
                        resultSet.getInt("emails_count"),
                        MailType.getFromInt(resultSet.getInt("mailtype")),
                        resultSet.getInt("total")
                ), params.toArray());
    }

    private QueryAndParams buildStatisticsSubquery(MonthlyStatType type, int companyId, DateRange timestamp) {
        List<Object> timestampParams = List.of(timestamp.getFrom(), timestamp.getTo());

        return switch (type) {
            case RECIPIENT_NUM -> new QueryAndParams("SUM(a.no_of_mailings)", Collections.emptyList());
            case OPENERS -> new QueryAndParams("""
                    (SELECT COUNT(distinct o.customer_id)
                    FROM %s o
                    WHERE m.mailing_id = o.mailing_id
                      AND o.creation >= ?
                      AND o.creation < ?)
                    """.formatted(getOnePixelLogDeviceTable(companyId)), timestampParams);
            case ANONYMOUS_OPENINGS -> new QueryAndParams("""
                    (SELECT COUNT(o.customer_id)
                    FROM %s o
                    WHERE m.mailing_id = o.mailing_id
                      AND o.customer_id = 0
                      AND o.creation >= ?
                      AND o.creation < ?)
                    """.formatted(getOnePixelLogDeviceTable(companyId)), timestampParams);
            case CLICKERS -> new QueryAndParams("""
                    (SELECT COUNT(distinct r.customer_id)
                    FROM %s r
                    WHERE r.mailing_id = m.mailing_id
                      AND r.timestamp >= ?
                      AND r.timestamp < ?)
                    """.formatted(getRdirLogTable(companyId)), timestampParams);
            case ANONYMOUS_CLICKS -> new QueryAndParams("""
                    (SELECT COUNT(r.customer_id)
                    FROM %s r
                    WHERE r.mailing_id = m.mailing_id
                      AND r.customer_id = 0
                      AND r.timestamp >= ?
                      AND r.timestamp < ?)
                    """.formatted(getRdirLogTable(companyId)), timestampParams);
        };
    }

    private boolean isDisabledMailinglistsTableExists() {
        return DbUtilities.checkIfTableExists(dataSource, "disabled_mailinglist_tbl");
    }

    private String getOnePixelLogDeviceTable(int companyID) {
        return "onepixellog_device_%d_tbl".formatted(companyID);
    }

    private String getRdirLogTable(int companyID) {
        return "rdirlog_%d_tbl".formatted(companyID);
    }

}
