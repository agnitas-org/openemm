/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.dao.impl;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dao.MailingTopDomainsStatsDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class MailingTopDomainsStatsDaoImpl extends BaseDaoImpl implements MailingTopDomainsStatsDao {

    public MailingTopDomainsStatsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public Map<Integer, Map<String, Integer>> getSentEmails(Filter filter, List<TargetFilter> targets) {
        Map<Integer, Map<String, Integer>> result = newResultMap(targets);
        if (targets.isEmpty()) {
            return result;
        }

        String domainExpr = getDomainFromEmailExpression(filter.isTopLevel());

        StringBuilder query = new StringBuilder("SELECT ").append(domainExpr).append(" AS domain_name");
        for (int i = 0; i < targets.size(); i++) {
            query.append(", COUNT(CASE WHEN ").append(targetCaseCondition(targets.get(i).targetSql())).append(" THEN 1 END) AS amount_").append(i);
        }
        query.append(" FROM mailtrack_").append(filter.companyId()).append("_tbl track")
                .append(" JOIN customer_").append(filter.companyId()).append("_tbl cust ON cust.customer_id = track.customer_id")
                .append(" WHERE track.mailing_id = ?")
                .append(" GROUP BY ").append(domainExpr);

        query(query.toString(), rs -> {
            String domain = rs.getString("domain_name");
            for (int i = 0; i < targets.size(); i++) {
                int amount = rs.getInt("amount_" + i);
                if (amount != 0) {
                    result.get(targets.get(i).targetId()).put(domain, amount);
                }
            }
        }, filter.mailingId());

        return result;
    }

    @Override
    public Map<Integer, Map<String, Integer>> getHardBounces(Filter filter, List<TargetFilter> targets) {
        return getBounces(filter, targets, "bounce.detail >= 510");
    }

    @Override
    public Map<Integer, Map<String, Integer>> getSoftBounces(Filter filter, List<TargetFilter> targets) {
        return getBounces(filter, targets, "bounce.detail < 510");
    }

    private Map<Integer, Map<String, Integer>> getBounces(Filter filter, List<TargetFilter> targets, String condition) {
        Map<Integer, Map<String, Integer>> result = newResultMap(targets);
        if (targets.isEmpty()) {
            return result;
        }

        String domainExpr = getDomainFromEmailExpression(filter.isTopLevel());

        StringBuilder query = new StringBuilder("SELECT ").append(domainExpr).append(" AS domain_name");
        for (int i = 0; i < targets.size(); i++) {
            query.append(", COUNT(CASE WHEN ").append(targetCaseCondition(targets.get(i).targetSql())).append(" THEN 1 END) AS amount_").append(i);
        }
        query.append(" FROM bounce_tbl bounce")
                .append(" JOIN customer_").append(filter.companyId()).append("_tbl cust ON cust.customer_id = bounce.customer_id")
                .append(" WHERE bounce.company_id = ? AND bounce.mailing_id = ? AND ").append(condition)
                .append(" GROUP BY ").append(domainExpr);

        query(query.toString(), rs -> {
            String domain = rs.getString("domain_name");
            for (int i = 0; i < targets.size(); i++) {
                int amount = rs.getInt("amount_" + i);
                if (amount != 0) {
                    result.get(targets.get(i).targetId()).put(domain, amount);
                }
            }
        }, filter.companyId(), filter.mailingId());

        return result;
    }

    @Override
    public Map<Integer, Map<String, Integer>> getOpeners(Filter filter, List<TargetFilter> targets) {
        return getDistinctCustomersByDomain(filter, targets, "onepixellog_device_" + filter.companyId() + "_tbl", "opl");
    }

    @Override
    public Map<Integer, Map<String, Integer>> getClickers(Filter filter, List<TargetFilter> targets) {
        return getDistinctCustomersByDomain(filter, targets, "rdirlog_" + filter.companyId() + "_tbl", "rlog");
    }

    private Map<Integer, Map<String, Integer>> getDistinctCustomersByDomain(
            Filter filter, List<TargetFilter> targets, String tableName, String tableAlias
    ) {
        Map<Integer, Map<String, Integer>> result = newResultMap(targets);
        if (targets.isEmpty()) {
            return result;
        }

        String domainExpr = getDomainFromEmailExpression(filter.isTopLevel());

        StringBuilder query = new StringBuilder("SELECT ").append(domainExpr).append(" AS domain_name");
        for (int i = 0; i < targets.size(); i++) {
            query.append(", COUNT(DISTINCT CASE WHEN ").append(targetCaseCondition(targets.get(i).targetSql())).append(" THEN cust.customer_id END) AS amount_").append(i);
        }
        query.append(" FROM ").append(tableName).append(" ").append(tableAlias)
                .append(" JOIN customer_").append(filter.companyId()).append("_tbl cust ON cust.customer_id = ").append(tableAlias).append(".customer_id")
                .append(" WHERE ").append(tableAlias).append(".mailing_id = ?")
                .append(" GROUP BY ").append(domainExpr);

        query(query.toString(), rs -> {
            String domain = rs.getString("domain_name");
            for (int i = 0; i < targets.size(); i++) {
                int amount = rs.getInt("amount_" + i);
                if (amount != 0) {
                    result.get(targets.get(i).targetId()).put(domain, amount);
                }
            }
        }, filter.mailingId());

        return result;
    }

    private Map<Integer, Map<String, Integer>> newResultMap(List<TargetFilter> targets) {
        Map<Integer, Map<String, Integer>> result = new LinkedHashMap<>();
        for (TargetFilter target : targets) {
            result.put(target.targetId(), new LinkedHashMap<>());
        }
        return result;
    }

    private String targetCaseCondition(String targetSql) {
        return isBlank(targetSql) ? "1=1" : "(" + targetSql + ")";
    }

    private String getDomainFromEmailExpression(boolean topLevelDomains) {
        return "SUBSTR(email, " + getPosExpression(topLevelDomains) + " + 1)";
    }

    private String getPosExpression(boolean topLevelDomains) {
        if (isOracleDB()) {
            return topLevelDomains ? "INSTR(email, '.', -1) " : "INSTR(email, '@')";
        }
        if (isPostgreSQL()) {
            return topLevelDomains ? "LENGTH(email) - STRPOS(REVERSE(email), '.') + 1" : "STRPOS(email, '@')";
        }
        return topLevelDomains ? "LENGTH(email) - INSTR(REVERSE(email), '.') + 1" : "INSTR(email, '@')";
    }
}
