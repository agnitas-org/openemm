/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao.impl;

import com.agnitas.emm.core.useractivitylog.bean.SoapUserActivityAction;
import com.agnitas.emm.core.useractivitylog.dao.SoapUserActivityLogDao;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SoapUserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements SoapUserActivityLogDao {

    private static final Logger LOGGER = LogManager.getLogger(SoapUserActivityLogDaoImpl.class);

    @Override
    public void writeWebServiceUsage(ZonedDateTime timestamp, String endpoint, int companyID, String user, String clientIp) {
        String sql = "INSERT INTO webservice_usage_log_tbl (timestamp, endpoint, company_id, username, client_ip) VALUES (?, ?, ?, ?, ?)";
        update(LOGGER, sql, Timestamp.from(timestamp.toInstant()), endpoint, companyID, user, clientIp);
    }

    @Override
    public PaginatedListImpl<SoapUserActivityAction> getUserActivityEntries(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to, String sortColumn, String sortDirection, int pageNumber, int pageSize) throws Exception {
        if (StringUtils.isBlank(sortColumn)) {
            sortColumn = "timestamp";
        }

        boolean sortDirectionAscending = "asc".equalsIgnoreCase(sortDirection) || "ascending".equalsIgnoreCase(sortDirection);

        SqlPreparedStatementManager sqlPreparedStatementManager
                = prepareSqlStatementForEntriesRetrieving(visibleAdmins, selectedAdmin, from, to);

        return selectPaginatedList(LOGGER, sqlPreparedStatementManager.getPreparedSqlString(), "webservice_usage_log_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new SoapUserActionRowMapper(), sqlPreparedStatementManager.getPreparedSqlParameters());
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to) throws Exception {
        SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager("SELECT timestamp, username, endpoint, client_ip FROM webservice_usage_log_tbl");
        sqlPreparedStatementManager.addWhereClause("timestamp >= ?", from);
        sqlPreparedStatementManager.addWhereClause("timestamp <= ?", DateUtilities.addDaysToDate(to, 1));

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

        return sqlPreparedStatementManager;
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
}
