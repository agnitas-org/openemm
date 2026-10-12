/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

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
                    appendSingleDeviceFilter(query, params, filter.getEntityId(), filter.getDeviceClass(), filter.getDateRange(), tableName, timestampColumnName);
            case MULTI ->
                    appendMultiDeviceFilter(query, params, filter.getEntityId(), filter.getDateRange(), tableName, timestampColumnName);
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
            int entityId,
            DateRange dateRange,
            String tableName,
            String timestampColumnName
    ) {
        sql.append("""
         AND t.customer_id IN (
             SELECT t2.customer_id
             FROM %s t2
             WHERE t2.%s = ?
        """.formatted(tableName, getKeyColumnName()));
        params.add(entityId);

        sql.append(getRangeFilterWithAnd("t2." + timestampColumnName, dateRange, params));
        sql.append(" GROUP BY t2.customer_id HAVING COUNT(DISTINCT t2.device_class_id) > 1)");
    }

    private void appendSingleDeviceFilter(
            StringBuilder sql,
            List<Object> params,
            int entityId,
            DeviceClass deviceClass,
            DateRange dateRange,
            String tableName,
            String timestampColumnName
    ) {
        sql.append("""
         AND t.customer_id IN (
             SELECT t2.customer_id
             FROM %s t2
             WHERE t2.%s = ?
        """.formatted(tableName, getKeyColumnName()));
        params.add(entityId);

        sql.append(getRangeFilterWithAnd("t2." + timestampColumnName, dateRange, params));
        sql.append(" GROUP BY t2.customer_id HAVING COUNT(DISTINCT t2.device_class_id) = 1 AND MAX(t2.device_class_id) = ?)");
        params.add(deviceClass.getId());
    }

    private static boolean containsCustomerCondition(String targetSql) {
        return Strings.CS.contains(targetSql, "cust.");
    }

}
