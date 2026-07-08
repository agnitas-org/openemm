/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao.impl;

import java.util.Map;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao;
import com.agnitas.util.Tuple;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class MailingTopDomainsStatsDaoImpl extends BaseDaoImpl implements MailingTopDomainsStatsDao {

    private static final RowMapper<Tuple<String, Integer>> DOMAIN_TO_COUNT_MAPPER = (rs, rowNum) -> new Tuple<>(
            rs.getString("domain_name"),
            rs.getInt("count_per_domain")
    );

    public MailingTopDomainsStatsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public Map<String, Integer> getSentEmails(TopDomainsFilter opts, String targetSql) {
        targetSql = andClause(targetSql);
        String domainFromEmailExpression = getDomainFromEmailExpression(opts.isTopLevel());

        return selectLinkedMap(
                addRowLimit(
                        """
                                SELECT COUNT(*) AS count_per_domain, domain_name
                                FROM (
                                  SELECT %s AS domain_name
                                  FROM mailtrack_%d_tbl track
                                    JOIN customer_%d_tbl cust ON cust.customer_id = track.customer_id
                                  WHERE track.mailing_id = ? %s
                                ) sub
                                GROUP BY domain_name HAVING COUNT(*) > 0
                                ORDER BY count_per_domain DESC
                                """.formatted(domainFromEmailExpression, opts.companyId(), opts.companyId(), targetSql),
                        opts.limit()
                ),
                DOMAIN_TO_COUNT_MAPPER,
                opts.mailingId()
        );
    }

    @Override
    public Map<String, Integer> getHardBounces(TopDomainsFilter opts, String targetSql) {
        return getBounces(opts, targetSql, "bounce.detail >= 510");
    }

    @Override
    public Map<String, Integer> getSoftBounces(TopDomainsFilter opts, String targetSql) {
        return getBounces(opts, targetSql, "bounce.detail < 510");
    }

    private Map<String, Integer> getBounces(TopDomainsFilter opts, String targetSql, String condition) {
        targetSql = andClause(targetSql);
        String domainFromEmailExpression = getDomainFromEmailExpression(opts.isTopLevel());
        return selectLinkedMap(
                addRowLimit("""
                                SELECT COUNT(*) AS count_per_domain, domain_name
                                FROM (
                                  SELECT %s AS domain_name
                                  FROM bounce_tbl bounce
                                    JOIN customer_%d_tbl cust ON cust.customer_id = bounce.customer_id
                                  WHERE bounce.company_id = ?
                                    AND bounce.mailing_id = ?
                                    AND %s
                                    %s
                                ) sub
                                GROUP BY domain_name HAVING COUNT(*) > 0
                                ORDER BY count_per_domain DESC
                                """.formatted(domainFromEmailExpression, opts.companyId(), condition, targetSql),
                        opts.limit()
                ),
                (rs, rowNum) -> new Tuple<>(
                        rs.getString("domain_name"),
                        rs.getInt("count_per_domain")),
                opts.companyId(), opts.mailingId()
        );
    }

    @Override
    public int getHardBouncesTotal(TopDomainsFilter opts, String targetSql) {
        return getBouncesTotal(opts.mailingId(), opts.companyId(), targetSql,  "bounce.detail >= 510");
    }

    @Override
    public int getSoftBouncesTotal(TopDomainsFilter opts, String targetSql) {
        return getBouncesTotal(opts.mailingId(), opts.companyId(), targetSql,  "bounce.detail < 510");
    }

    private int getBouncesTotal(int mailingId, int companyId, String targetSql, String condition) {
        return selectInt("""
                        SELECT COUNT(*)
                        FROM bounce_tbl bounce
                         JOIN customer_%d_tbl cust ON cust.customer_id = bounce.customer_id
                        WHERE bounce.company_id = ?
                          AND bounce.mailing_id = ?
                          AND %s
                          %s
                        """.formatted(companyId, condition, andClause(targetSql)),
                companyId,
                mailingId
        );
    }

    @Override
    public int getOpenersTotal(int mailingId, int companyId, String targetSql) {
        return selectInt("""
                        SELECT COUNT(DISTINCT cust.customer_id)
                        FROM onepixellog_device_%d_tbl opl
                          JOIN customer_%d_tbl cust ON opl.customer_id = cust.customer_id
                        WHERE opl.mailing_id = ? %s
                        """.formatted(companyId, companyId, andClause(targetSql)),
                mailingId
        );
    }

    @Override
    public int getClickersTotal(int mailingId, int companyId, String targetSql) {
        return selectInt("""
                        SELECT COUNT(DISTINCT cust.customer_id)
                        FROM rdirlog_%d_tbl rlog
                          JOIN customer_%d_tbl cust ON cust.customer_id = rlog.customer_id
                        WHERE rlog.mailing_id = ? %s
                        """.formatted(companyId, companyId, andClause(targetSql)),
                mailingId
        );
    }

    @Override
    public Map<String, Integer> getOpeners(TopDomainsFilter opts, String targetSql) {
        targetSql = andClause(targetSql);
        String domainFromEmailExpression = getDomainFromEmailExpression(opts.isTopLevel());
        return selectLinkedMap(addRowLimit("""
                                SELECT COUNT(DISTINCT customer_id) AS count_per_domain, domain_name
                                FROM (
                                  SELECT %s AS domain_name, cust.customer_id
                                  FROM onepixellog_device_%d_tbl opl
                                    JOIN customer_%d_tbl cust ON cust.customer_id = opl.customer_id
                                  WHERE opl.mailing_id = ? %s
                                ) sub
                                GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0
                                ORDER BY count_per_domain DESC
                                """.formatted(domainFromEmailExpression, opts.companyId(), opts.companyId(), targetSql),
                        opts.limit()
                ),
                DOMAIN_TO_COUNT_MAPPER,
                opts.mailingId()
        );
    }

    @Override
    public Map<String, Integer> getClickers(TopDomainsFilter opts, String targetSql) {
        targetSql = andClause(targetSql);
        String domainFromEmailExpression = getDomainFromEmailExpression(opts.isTopLevel());
        return selectLinkedMap(addRowLimit("""
                                SELECT COUNT(DISTINCT customer_id) AS count_per_domain, domain_name
                                FROM (
                                  SELECT %s AS domain_name, cust.customer_id
                                  FROM rdirlog_%s_tbl rlog
                                    JOIN customer_%s_tbl cust ON cust.customer_id = rlog.customer_id
                                  WHERE rlog.mailing_id = ? %s
                                ) sub
                                GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0
                                ORDER BY count_per_domain DESC
                                """.formatted(domainFromEmailExpression, opts.companyId(), opts.companyId(), targetSql),
                        opts.limit()
                ),
                DOMAIN_TO_COUNT_MAPPER,
                opts.mailingId()
        );
    }

    public String getDomainFromEmailExpression(boolean topLevelDomains) {
        return "SUBSTR(email, " + getPosExpression(topLevelDomains) + " + 1)";
    }

    private String getPosExpression(boolean topLevelDomains) {
        if (isOracleDB()) {
            return topLevelDomains ? "INSTR(email, '.', -1) " : "INSTR(email, '@')";
        }
        if (isPostgreSQL()) {
            return topLevelDomains ? "LENGTH(email) - STRPOS(REVERSE(email), '.')" : "STRPOS(email, '@')";
        }
        return topLevelDomains ? "LENGTH(email) - INSTR(REVERSE(email), '.')" : "INSTR(email, '@')";
    }
}
