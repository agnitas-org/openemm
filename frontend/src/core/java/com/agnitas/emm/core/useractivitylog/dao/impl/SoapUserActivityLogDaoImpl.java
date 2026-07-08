/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.SoapUserActivityAction;
import com.agnitas.emm.core.useractivitylog.dao.SoapUserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.forms.SoapUserActivityLogFilter;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class SoapUserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements SoapUserActivityLogDao {

    @Override
    public void deleteByUsernames(Set<String> usernames) {
        if (usernames.isEmpty()) {
            return;
        }

        update("DELETE FROM " + getTableName() + " WHERE "
                + makeBulkInClauseForString("username", usernames));
    }

    @Override
    public void writeWebServiceUsage(ZonedDateTime timestamp, String endpoint, int companyID, String user, String clientIp) {
        String sql = "INSERT INTO webservice_usage_log_tbl (timestamp, endpoint, company_id, username, client_ip) VALUES (?, ?, ?, ?, ?)";
        update(sql, Timestamp.from(timestamp.toInstant()), endpoint, companyID, user, clientIp);
    }

    @Override
    public PaginatedList<SoapUserActivityAction> getUserActivityEntries(SoapUserActivityLogFilter filter) {
        StringBuilder query = new StringBuilder();
        List<Object> params = buildOverviewSelectQuery(query, filter);

        PaginatedList<SoapUserActivityAction> list = selectPaginatedList(query.toString(), "webservice_usage_log_tbl",
                filter, new SoapUserActionRowMapper(), params.toArray());

        if (filter.isUiFiltersSet()) {
            list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCompanyId()));
        }

        return list;
    }

    private List<Object> buildOverviewSelectQuery(StringBuilder query, SoapUserActivityLogFilter filter) {
        query.append("SELECT timestamp, username, endpoint, client_ip FROM webservice_usage_log_tbl");
        return applyFilter(filter, query);
    }

    private List<Object> applyFilter(SoapUserActivityLogFilter filter, StringBuilder query) {
        List<Object> params = applyRequiredOverviewFilter(query, filter.getCompanyId());

        query.append(getDateRangeFilterWithAnd("timestamp", filter.getTimestamp(), params));

        if (StringUtils.isNotBlank(filter.getUsername())) {
            query.append(" AND username = ?");
            params.add(filter.getUsername());
        }
        if (StringUtils.isNotBlank(filter.getEndpoint())) {
            query.append(getPartialSearchFilterWithAnd("endpoint", filter.getEndpoint(), params));
        }
        if (StringUtils.isNotBlank(filter.getIpAddress())) {
            query.append(getPartialSearchFilterWithAnd("client_ip", filter.getIpAddress(), params));
        }
        return params;
    }

    private int getTotalUnfilteredCountForOverview(Integer companyId) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM webservice_usage_log_tbl");
        List<Object> params = applyRequiredOverviewFilter(query, companyId);
        return selectIntWithDefaultValue(query.toString(), 0, params.toArray());
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(SoapUserActivityLogFilter filter) {
        StringBuilder query = new StringBuilder();
        List<Object> params = buildOverviewSelectQuery(query, filter);

        return new SqlPreparedStatementManager(query.toString(), params.toArray());
    }

    private List<Object> applyRequiredOverviewFilter(StringBuilder query, Integer companyId) {
        if (companyId == null) {
            query.append(" WHERE 1 = 1");
            return new ArrayList<>();
        }

        query.append(" WHERE (company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?))");
        return new ArrayList<>(List.of(companyId, companyId));
    }

    private static final class SoapUserActionRowMapper implements RowMapper<SoapUserActivityAction> {

        @Override
        public SoapUserActivityAction mapRow(ResultSet rs, int rowNum) throws SQLException {
            SoapUserActivityAction userAction = new SoapUserActivityAction();

            userAction.setTimestamp(rs.getTimestamp("timestamp"));
            userAction.setEndpoint(rs.getString("endpoint"));
            userAction.setUsername(rs.getString("username"));
            userAction.setClientIp(rs.getString("client_ip"));

            return userAction;
        }
    }

    @Override
    protected String getTableName() {
        return "webservice_usage_log_tbl";
    }

}
