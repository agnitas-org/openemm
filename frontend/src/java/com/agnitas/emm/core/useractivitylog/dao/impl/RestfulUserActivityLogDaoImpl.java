/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.RestfulUserActivityAction;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class RestfulUserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements RestfulUserActivityLogDao {

    @Override
    public void writeUserActivityLog(String endpoint, String description, String httpMethod, String host, Admin admin) {
        if (admin == null || !admin.isRestful()) {
            return;
        }

        String supervisorName = admin.getSupervisor() != null ? admin.getSupervisor().getSupervisorName() : null;

        String sql = "INSERT INTO restful_usage_log_tbl (timestamp, endpoint, description, request_method, company_id, username, supervisor_name, host_name) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";
        update(sql, endpoint, description, httpMethod, admin.getCompanyID(), admin.getUsername(), supervisorName, host);
    }

    @Override
    public PaginatedList<RestfulUserActivityAction> getUserActivityEntries(RestfulUserActivityLogFilter filter) {
        StringBuilder query = new StringBuilder();
        List<Object> params = buildOverviewSelectQuery(query, filter);

        PaginatedList<RestfulUserActivityAction> list = selectPaginatedList(query.toString(), "restful_usage_log_tbl",
                filter, new RestfulUserActionRowMapper(), params.toArray());

        if (filter.isUiFiltersSet()) {
            list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCompanyId()));
        }

        return list;
    }

    private List<Object> buildOverviewSelectQuery(StringBuilder query, RestfulUserActivityLogFilter filter) {
        query.append("SELECT timestamp, username, endpoint, description, request_method, supervisor_name FROM restful_usage_log_tbl");
        return applyFilter(filter, query);
    }

    private List<Object> applyFilter(RestfulUserActivityLogFilter filter, StringBuilder query) {
        List<Object> params = applyRequiredOverviewFilter(query, filter.getCompanyId());

        query.append(getDateRangeFilterWithAnd("timestamp", filter.getTimestamp(), params));

        if (StringUtils.isNotBlank(filter.getUsername())) {
            query.append(" AND username = ?");
            params.add(filter.getUsername());
        }

        if (StringUtils.isNotBlank(filter.getDescription())) {
            query.append(getPartialSearchFilterWithAnd("description"));
            params.add(filter.getDescription());
        }

        if (StringUtils.isNotBlank(filter.getRequestUrl())) {
            query.append(getPartialSearchFilterWithAnd("endpoint"));
            params.add(filter.getRequestUrl());
        }

        if (StringUtils.isNotBlank(filter.getRequestMethod())) {
            query.append(" AND request_method = ?");
            params.add(filter.getRequestMethod());
        }

        return params;
    }

    private int getTotalUnfilteredCountForOverview(Integer companyId) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM restful_usage_log_tbl");
        List<Object> params = applyRequiredOverviewFilter(query, companyId);

        return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
    }

    private List<Object> applyRequiredOverviewFilter(StringBuilder query, Integer companyId) {
        if (companyId == null) {
            query.append(" WHERE 1 = 1");
            return new ArrayList<>();
        }

        query.append(" WHERE (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?))");
        return new ArrayList<>(List.of(companyId, companyId));
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(RestfulUserActivityLogFilter filter) {
        StringBuilder query = new StringBuilder();
        List<Object> params = buildOverviewSelectQuery(query, filter);

        return new SqlPreparedStatementManager(query.toString(), params.toArray());
    }

    private static final class RestfulUserActionRowMapper implements RowMapper<RestfulUserActivityAction> {

        @Override
        public RestfulUserActivityAction mapRow(ResultSet rs, int rowNum) throws SQLException {
            RestfulUserActivityAction userAction = new RestfulUserActivityAction();

            userAction.setTimestamp(rs.getTimestamp("timestamp"));
            userAction.setEndpoint(rs.getString("endpoint"));
            userAction.setDescription(rs.getString("description"));
            userAction.setRequestMethod(rs.getString("request_method"));
            userAction.setUsername(rs.getString("username"));
            userAction.setSupervisorName(rs.getString("supervisor_name"));

            return userAction;
        }
    }

    @Override
    protected String getTableName() {
        return "restful_usage_log_tbl";
    }

}
