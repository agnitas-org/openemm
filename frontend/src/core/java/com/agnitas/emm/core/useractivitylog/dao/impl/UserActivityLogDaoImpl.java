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
import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.useractivitylog.dao.LoggedUserAction;
import com.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.SqlPreparedStatementManager;
import com.agnitas.util.UserActivityLogActions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class UserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements UserActivityLogDao {

	private static final int MAX_DESCRIPTION_LENGTH = 4000;

	@Override
	@DaoUpdateReturnValueCheck
	public void writeUserActivityLog(Admin admin, String action, String description) {
		String username = "";
		String supervisorName = null;
        int companyId = 0;

		if (admin != null) {
			username = admin.getUsername();
            companyId = admin.getCompanyID();

			if (admin.getSupervisor() != null) {
				supervisorName = admin.getSupervisor().getSupervisorName();
			}
		}

		if (StringUtils.length(description) > MAX_DESCRIPTION_LENGTH) {
			logger.warn("UAL description was abbreviated. Actual value was {}", description);
			description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LENGTH);
		}

		String insertSql = "INSERT INTO userlog_tbl (logtime, company_id, username, supervisor_name, action, description) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?)";
		update(insertSql, companyId, username, supervisorName, action, description);
	}

	@Override
	public PaginatedList<LoggedUserAction> getUserActivityEntries(UserActivityLogFilter filter) {
        SqlPreparedStatementManager sqlPreparedStatementManager = prepareSqlStatementForEntriesRetrieving(filter);

		PaginatedList<LoggedUserAction> list = selectPaginatedList(sqlPreparedStatementManager.getPreparedSqlString(),
				"userlog_tbl", filter, new LoggedUserActionRowMapper(), sqlPreparedStatementManager.getPreparedSqlParameters());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCompanyId()));
		}

		return list;
    }

	private int getTotalUnfilteredCountForOverview(Integer companyId) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM userlog_tbl");

		if (companyId != null) {
			query.append(" WHERE (company_id = ").append(companyId)
					.append(" OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ")
					.append(companyId)
					.append("))");
		}

		return selectIntWithDefaultValue(query.toString(), 0);
	}

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(UserActivityLogFilter filter) {
		SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager("SELECT logtime, username, supervisor_name, action, description FROM userlog_tbl");

		if (filter.getTimestamp().getFrom() != null) {
			sqlPreparedStatementManager.addWhereClause("logtime >= ?", filter.getTimestamp().getFrom());
		}

		if (filter.getTimestamp().getTo() != null) {
			sqlPreparedStatementManager.addWhereClause(
					"logtime < ?",
					DateUtilities.addDaysToDate(filter.getTimestamp().getTo(), 1)
			);
		}

		if (filter.getCompanyId() != null) {
			sqlPreparedStatementManager.addWhereClause(
					"(company_id = ? OR company_id IN (SELECT company_id FROM company_tbl WHERE creator_company_id = ?))",
					filter.getCompanyId(), filter.getCompanyId());
		}

		// If set, the selected admin must match
		if (StringUtils.isNotBlank(filter.getUsername())) {
			sqlPreparedStatementManager.addWhereClause("username = ?", filter.getUsername());
		}

		if (StringUtils.isNotBlank(filter.getDescription())) {
			if (isOracleDB()) {
				sqlPreparedStatementManager.addWhereClause("description LIKE ('%' || ? || '%')", filter.getDescription());
			} else {
				sqlPreparedStatementManager.addWhereClause("description LIKE CONCAT('%', ?, '%')", filter.getDescription());
			}
		}

		if (UserActivityLogActions.LOGIN_LOGOUT == filter.getAction()) {
			sqlPreparedStatementManager.addWhereClause("action IN ('do login', 'do logout', 'login', 'logout', ?)", filter.getAction().getLocalValue());
		} else if (UserActivityLogActions.ANY_WITHOUT_LOGIN == filter.getAction()) {
			sqlPreparedStatementManager.addWhereClause("action NOT IN ('do login', 'do logout', 'login', 'logout', 'login_logout')");
		} else if (filter.getAction() != null) {
			sqlPreparedStatementManager.addWhereClause("1=1");
			sqlPreparedStatementManager.addAndClause();
			sqlPreparedStatementManager.appendOpeningParenthesis();
			String[] localValues = filter.getAction().getLocalValues();
			for(int i = 0; i < localValues.length; i++){
				if(i != 0){
					sqlPreparedStatementManager.addOrClause();
				}
				sqlPreparedStatementManager.addWhereClauseSimple("(LOWER(action) LIKE ? OR LOWER(action) = ?)", localValues[i].toLowerCase() + " %", localValues[i].toLowerCase());
			}
			sqlPreparedStatementManager.appendClosingParenthesis();
			sqlPreparedStatementManager.addWhereClause("action NOT IN ('do login', 'do logout', 'login', 'logout', 'login_logout')");
		}

		return sqlPreparedStatementManager;
	}

	private static class LoggedUserActionRowMapper implements RowMapper<LoggedUserAction> {
		@Override
		public LoggedUserAction mapRow(ResultSet rs, int row) throws SQLException {
			Timestamp date = rs.getTimestamp( "logtime");
			String username = rs.getString( "username");
			String supervisorName = rs.getString( "supervisor_name");
			String action = rs.getString( "action");
			String description = rs.getString( "description");

			return new LoggedUserAction(action, description, username, supervisorName, new Date(date.getTime()));
		}
	}

	@Override
	protected String getTableName() {
		return "userlog_tbl";
	}

}
