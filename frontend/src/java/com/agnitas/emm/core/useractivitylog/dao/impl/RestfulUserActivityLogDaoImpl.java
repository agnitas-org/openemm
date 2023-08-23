/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.bean.RestfulUserActivityAction;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RestfulUserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements RestfulUserActivityLogDao {

    private static final Logger LOGGER = LogManager.getLogger(RestfulUserActivityLogDaoImpl.class);

    @Override
    public void writeUserActivityLog(String endpoint, String description, String httpMethod, String host, Admin admin) {
        if (admin == null || !admin.isRestful()) {
            return;
        }

        String sql = "INSERT INTO restful_usage_log_tbl (timestamp, endpoint, description, request_method, company_id, username, host_name) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?)";
        update(LOGGER, sql, endpoint, description, httpMethod, admin.getCompanyID(), admin.getUsername(), host);
    }

    @Override
    public PaginatedListImpl<RestfulUserActivityAction> getUserActivityEntries(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to, String description, String sortColumn, String sortDirection, int pageNumber, int pageSize) throws Exception {
        if (StringUtils.isBlank(sortColumn)) {
            sortColumn = "timestamp";
        }

        boolean sortDirectionAscending = "asc".equalsIgnoreCase(sortDirection) || "ascending".equalsIgnoreCase(sortDirection);

        SqlPreparedStatementManager sqlPreparedStatementManager =
                prepareSqlStatementForEntriesRetrieving(visibleAdmins, selectedAdmin, from, to, description);

        return selectPaginatedList(LOGGER, sqlPreparedStatementManager.getPreparedSqlString(), "restful_usage_log_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new RestfulUserActionRowMapper(), sqlPreparedStatementManager.getPreparedSqlParameters());
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to, String description) throws Exception {
        SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager("SELECT timestamp, username, endpoint, description, request_method FROM restful_usage_log_tbl");
        sqlPreparedStatementManager.addWhereClause("timestamp >= ?", from);
        sqlPreparedStatementManager.addWhereClause("timestamp <= ?", to);

        //  If set, any of the visible admins must match
        if (visibleAdmins != null && !visibleAdmins.isEmpty()) {
            List<String> visibleAdminNameList = new ArrayList<>();
            for (AdminEntry visibleAdmin : visibleAdmins) {
                if (visibleAdmin != null) {
                    visibleAdminNameList.add(visibleAdmin.getUsername());
                }
            }
            if (!visibleAdminNameList.isEmpty()) {
                sqlPreparedStatementManager.addWhereClause(makeBulkInClauseForString("username", visibleAdminNameList));
            }
        }

        // If set, the selected admin must match
        if (StringUtils.isNotBlank(selectedAdmin) && !"0".equals(selectedAdmin)) {
            sqlPreparedStatementManager.addWhereClause("username = ?", selectedAdmin);
        }

        if (StringUtils.isNotBlank(description)) {
            if (isOracleDB()) {
                sqlPreparedStatementManager.addWhereClause("description LIKE ('%' || ? || '%')", description);
            } else {
                sqlPreparedStatementManager.addWhereClause("description LIKE CONCAT('%', ?, '%')", description);
            }
        }

        return sqlPreparedStatementManager;
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

            return userAction;
        }
    }
}
