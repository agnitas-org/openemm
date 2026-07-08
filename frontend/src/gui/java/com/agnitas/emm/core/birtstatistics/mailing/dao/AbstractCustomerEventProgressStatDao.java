/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.mailing.dao;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.birtstatistics.model.CustomerEventStats;
import com.agnitas.emm.core.birtstatistics.model.MailingProgressStatisticFilter;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractCustomerEventProgressStatDao extends BaseDaoImpl {

    private final RowMapper<CustomerEventStats> rowMapper = (rs, rowNum) ->
            new CustomerEventStats(
                    rs.getInt("net"),
                    rs.getInt("gross"),
                    rs.getInt("anonymous")
            );

    protected enum DeviceFilterType {
        SINGLE,
        MULTI
    }

    protected AbstractCustomerEventProgressStatDao(DataSource dataSource, JavaMailService javaMailService) {
        super(dataSource, javaMailService);
    }

    protected abstract String getKeyColumnName();

    protected CustomerEventStats getEventStats(
            MailingProgressStatisticFilter filter,
            DeviceFilterType deviceFilterType,
            String tableName,
            String timestampColumnName
    ) {
        List<Object> params = new ArrayList<>(List.of(filter.getEntityId()));

        StringBuilder query = new StringBuilder("""
                SELECT COUNT(DISTINCT CASE WHEN t.customer_id != 0 THEN t.customer_id END) AS net,
                       COUNT(CASE WHEN t.customer_id != 0 THEN 1 END)                      AS gross,
                       COUNT(CASE WHEN t.customer_id = 0 THEN 1 END)                       AS anonymous
                FROM %s t
                """.formatted(tableName));

        if (containsCustomerCondition(filter.getTargetSql())) {
            query.append("LEFT JOIN customer_").append(filter.getCompanyId()).append("_tbl cust ON t.customer_id = cust.customer_id");
        }

        query.append(" WHERE t.%s = ?".formatted(getKeyColumnName()));

        switch (deviceFilterType) {
            case SINGLE ->
                    appendSingleDeviceFilter(query, params, filter.getDeviceClass(), filter.getDateRange(), tableName, timestampColumnName);
            case MULTI ->
                    appendMultiDeviceFilter(query, params, filter.getDateRange(), tableName, timestampColumnName);
        }

        query.append(getRangeFilterWithAnd("t." + timestampColumnName, filter.getDateRange(), params));

        if (StringUtils.isNotBlank(filter.getTargetSql())) {
            query.append(" AND ").append(filter.getTargetSql());
        }

        return selectObject(query.toString(), rowMapper, params.toArray());
    }

    private void appendMultiDeviceFilter(
            StringBuilder sql,
            List<Object> params,
            DateRange dateRange,
            String tableName,
            String timestampColumnName
    ) {
        sql.append("""
         AND (
             SELECT COUNT(DISTINCT device_class_id)
             FROM %s t2
             WHERE t.%s = t2.%s
               AND t.customer_id = t2.customer_id
        """.formatted(tableName, getKeyColumnName(), getKeyColumnName()));

        sql.append(getRangeFilterWithAnd("t2." + timestampColumnName, dateRange, params));
        sql.append(") > 1");
    }

    private void appendSingleDeviceFilter(
            StringBuilder sql,
            List<Object> params,
            DeviceClass deviceClass,
            DateRange dateRange,
            String tableName,
            String timestampColumnName
    ) {
        sql.append("""
         AND EXISTS (
            SELECT 1 FROM %s t2
            WHERE t.%s = t2.%s
              AND t.customer_id = t2.customer_id
              AND t2.device_class_id = ?
        """.formatted(tableName, getKeyColumnName(), getKeyColumnName()));

        params.add(deviceClass.getId());
        sql.append(getRangeFilterWithAnd("t2." + timestampColumnName, dateRange, params));
        sql.append(")");

        sql.append("""
         AND NOT EXISTS (
             SELECT 1 FROM %s t3
             WHERE t.%s = t3.%s
               AND t.customer_id = t3.customer_id
               AND t3.device_class_id != ?
        """.formatted(tableName, getKeyColumnName(), getKeyColumnName()));

        params.add(deviceClass.getId());
        sql.append(getRangeFilterWithAnd("t3." + timestampColumnName, dateRange, params));
        sql.append(")");
    }

    private static boolean containsCustomerCondition(String targetSql) {
        return Strings.CS.contains(targetSql, "cust.");
    }

}
