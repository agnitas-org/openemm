/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.useractivitylog.dao.impl;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.McpUserActivityAction;
import com.agnitas.emm.core.useractivitylog.dao.McpUserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.forms.McpUserActivityLogFilter;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class McpUserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements McpUserActivityLogDao {

    private final RowMapper<McpUserActivityAction> userActionRowMapper = (rs, rowNum) ->
            new McpUserActivityAction(
                    rs.getString("username"),
                    rs.getString("source"),
                    rs.getString("tool_name"),
                    rs.getTimestamp("timestamp")
            );

    @Override
    public PaginatedList<McpUserActivityAction> getUserActivityEntries(McpUserActivityLogFilter filter) {
        StringBuilder query = new StringBuilder();
        List<Object> params = buildOverviewSelectQuery(query, filter);

        PaginatedList<McpUserActivityAction> list = selectPaginatedList(
                query.toString(),
                getTableName(),
                filter,
                userActionRowMapper,
                params.toArray()
        );

        if (filter.isUiFiltersSet()) {
            list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCompanyId()));
        }

        return list;
    }

    private List<Object> buildOverviewSelectQuery(StringBuilder query, McpUserActivityLogFilter filter) {
        query.append("SELECT timestamp, username, tool_name, source FROM mcp_usage_log_tbl");
        return applyFilter(filter, query);
    }

    private List<Object> applyFilter(McpUserActivityLogFilter filter, StringBuilder query) {
        List<Object> params = applyRequiredOverviewFilter(query, filter.getCompanyId());

        query.append(getDateRangeFilterWithAnd("timestamp", filter.getTimestamp(), params));

        if (StringUtils.isNotBlank(filter.getUsername())) {
            query.append(" AND username = ?");
            params.add(filter.getUsername());
        }

        if (StringUtils.isNotBlank(filter.getSource())) {
            query.append(getPartialSearchFilterWithAnd("source"));
            params.add(filter.getSource());
        }

        if (StringUtils.isNotBlank(filter.getToolName())) {
            query.append(getPartialSearchFilterWithAnd("tool_name"));
            params.add(filter.getToolName());
        }

        return params;
    }

    private int getTotalUnfilteredCountForOverview(Integer companyId) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM mcp_usage_log_tbl");
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
    public List<String> getDistinctSources(Integer companyId) {
        StringBuilder query = new StringBuilder("SELECT DISTINCT source FROM mcp_usage_log_tbl");
        List<Object> params = applyRequiredOverviewFilter(query, companyId);
        query.append(" AND source IS NOT NULL");

        return select(query.toString(), StringRowMapper.INSTANCE, params.toArray());
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(McpUserActivityLogFilter filter) {
        StringBuilder query = new StringBuilder();
        List<Object> params = buildOverviewSelectQuery(query, filter);

        return new SqlPreparedStatementManager(query.toString(), params.toArray());
    }

    @Override
    public void writeUserActivityLog(String toolName, String source, Admin admin) {
        if (admin == null) {
            return;
        }

        String sql = "INSERT INTO mcp_usage_log_tbl (timestamp, tool_name, source, company_id, username) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)";
        update(sql, toolName, source, admin.getCompanyID(), admin.getUsername());
    }

    @Override
    protected String getTableName() {
        return "mcp_usage_log_tbl";
    }

}
