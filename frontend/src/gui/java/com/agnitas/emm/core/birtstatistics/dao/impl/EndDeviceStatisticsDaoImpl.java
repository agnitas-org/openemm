/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.dao.impl;

import java.util.List;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.dao.EndDeviceStatisticsDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class EndDeviceStatisticsDaoImpl extends BaseDaoImpl implements EndDeviceStatisticsDao {

    private final RowMapper<DeviceStatisticsData> rowMapper = (rs, rowNum) ->
            new DeviceStatisticsData(
                    rs.getInt("device_class_id"),
                    rs.getString("description"),
                    rs.getInt("openings"),
                    rs.getInt("clicks")
            );

    public EndDeviceStatisticsDaoImpl(
            @Qualifier("dataSource") DataSource dataSource,
            @Autowired(required = false) JavaMailService javaMailService
    ) {
        super(dataSource, javaMailService);
    }

    @Override
    public List<DeviceStatisticsData> getData(int mailingId, int companyId, String targetSql, int maxDevicesCount) {
        StringBuilder sql = new StringBuilder("""
                SELECT o.device_class_id, d.description AS description, COUNT(*) AS openings, 0 AS clicks
                FROM onepixellog_device_%d_tbl o
                JOIN device_tbl d ON o.device_id = d.device_id
                """.formatted(companyId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(" JOIN customer_%d_tbl cust ON o.customer_id = cust.customer_id".formatted(companyId));
        }

        sql.append(" WHERE o.mailing_id = ?");

        if (StringUtils.isNotEmpty(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }

        sql.append(" GROUP BY o.device_class_id, d.description");

        sql.append(" UNION ALL ");

        sql.append("""
                SELECT r.device_class_id AS device_class_id, d.description AS description, 0 AS openings, COUNT(*) AS clicks
                FROM rdirlog_%d_tbl r
                JOIN device_tbl d ON r.device_id = d.device_id
                """.formatted(companyId));

        if (containsCustomerCondition(targetSql)) {
            sql.append(" JOIN customer_%d_tbl cust ON r.customer_id = cust.customer_id".formatted(companyId));
        }

        sql.append(" WHERE r.device_id = d.device_id AND r.mailing_id = ?");

        if (StringUtils.isNotEmpty(targetSql)) {
            sql.append(" AND (").append(targetSql).append(")");
        }

        sql.append(" GROUP BY r.device_class_id, d.description");

        String query = """
                SELECT device_class_id, description, openings, clicks
                FROM (SELECT device_class_id,
                             description,
                             SUM(openings)               AS openings,
                             SUM(clicks)                 AS clicks,
                             SUM(openings) + SUM(clicks) AS order_sum
                      FROM (%s) x
                      GROUP BY device_class_id, description) subsel
                ORDER BY order_sum DESC
                """.formatted(sql.toString());

        return select(addRowLimit(query, maxDevicesCount), rowMapper, mailingId, mailingId);
    }

    private boolean containsCustomerCondition(String targetSql) {
        return Strings.CS.contains(targetSql, "cust.");
    }

}
