/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

import com.agnitas.dao.TrackableLinkStatisticsDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.dto.DateRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TrackableLinkStatisticsDaoImpl extends BaseDaoImpl implements TrackableLinkStatisticsDao {

    protected TrackableLinkStatisticsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public Map<String, Map<Integer, LinkClickStatisticData>> getClickStatisticsPerLink(
            int mailingId,
            int companyId,
            DateRange timestamp,
            List<ClickFilter> filters
    ) {
        Map<String, Map<Integer, LinkClickStatisticData>> result = new LinkedHashMap<>();
        for (ClickFilter filter : filters) {
            result.put(filter.key(), new LinkedHashMap<>());
        }

        if (filters.isEmpty()) {
            return result;
        }

        List<Object> params = new ArrayList<>(List.of(mailingId));

        StringBuilder query = new StringBuilder("SELECT rlog.url_id");
        for (int i = 0; i < filters.size(); i++) {
            query.append(", ").append(buildClickPivotColumns(filters.get(i).sqlCondition(), i));
        }
        query.append(" FROM rdirlog_").append(companyId).append("_tbl rlog");

        boolean needsCustomerJoin = filters.stream().anyMatch(f -> containsCustomerCondition(f.sqlCondition()));
        if (needsCustomerJoin) {
            query.append(" LEFT JOIN customer_").append(companyId).append("_tbl cust ON rlog.customer_id = cust.customer_id");
        }

        query.append(" WHERE rlog.mailing_id = ?");
        query.append(getDateRangeFilterWithAnd("rlog.timestamp", timestamp, params));
        query.append(" GROUP BY rlog.url_id ORDER BY rlog.url_id");

        query(query.toString(), rs -> {
            int urlId = rs.getInt("url_id");
            for (int i = 0; i < filters.size(); i++) {
                int clicks = rs.getInt("clicks_" + i);
                if (clicks == 0) {
                    continue;
                }

                LinkClickStatisticData data = new LinkClickStatisticData(urlId, clicks, rs.getInt("clickers_" + i));
                result.get(filters.get(i).key()).put(urlId, data);
            }
        }, params.toArray());

        return result;
    }

    private String buildClickPivotColumns(String condition, int index) {
        if (StringUtils.isBlank(condition)) {
            return """
                    COUNT(*) AS clicks_%1$d,
                    COUNT(DISTINCT rlog.customer_id) AS clickers_%1$d""".formatted(index);
        }
        return """
                COUNT(CASE WHEN (%2$s) THEN 1 END) AS clicks_%1$d,
                COUNT(DISTINCT CASE WHEN (%2$s) THEN rlog.customer_id END) AS clickers_%1$d""".formatted(index, condition);
    }

    private static boolean containsCustomerCondition(String targetSql) {
        return Strings.CS.contains(targetSql, "cust.");
    }

}
