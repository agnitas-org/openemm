/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import com.agnitas.beans.Admin;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.useractivitylog.dao.impl.UserActivityLogDaoBaseImpl;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.util.SqlPreparedStatementManager;
import org.agnitas.util.UserActivityLogActions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Implementation of {@link UserActivityLogDao}.
 */
public class ComUserActivityLogDaoImpl extends UserActivityLogDaoBaseImpl implements UserActivityLogDao {

	private static final Logger logger = LogManager.getLogger(ComUserActivityLogDaoImpl.class);
	
	private static final int MAX_DESCRIPTION_LENGTH = 4000;

	@Override
	@DaoUpdateReturnValueCheck
	public void writeUserActivityLog(Admin admin, String action, String description) {
		String username = "";
		String supervisorName = null;

		if (admin != null) {
			username = admin.getUsername();

			if (admin.getSupervisor() != null) {
				supervisorName = admin.getSupervisor().getSupervisorName();
			}
		}

		if (StringUtils.length(description) > MAX_DESCRIPTION_LENGTH) {
			logger.warn("UAL description was abbreviated. Actual value was {}", description);
			description = StringUtils.abbreviate(description, MAX_DESCRIPTION_LENGTH);
		}

		String insertSql = "INSERT INTO userlog_tbl (logtime, username, supervisor_name, action, description) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)";
		update(logger, insertSql, username, supervisorName, action, description);
	}

	@Override
	public PaginatedListImpl<LoggedUserAction> getUserActivityEntries(List<AdminEntry> visibleAdmins, String selectedAdmin, int selectedAction, Date from, Date to, String description, String sortColumn, String sortDirection, int pageNumber, int pageSize) throws Exception {
		if (StringUtils.isBlank(sortColumn)) {
			sortColumn = "logtime";
		}
		
		boolean sortDirectionAscending = "asc".equalsIgnoreCase(sortDirection) || "ascending".equalsIgnoreCase(sortDirection);
		
		SqlPreparedStatementManager sqlPreparedStatementManager =
				prepareSqlStatementForEntriesRetrieving(visibleAdmins, selectedAdmin, selectedAction, from, to, description);

        return selectPaginatedList(logger, sqlPreparedStatementManager.getPreparedSqlString(), "userlog_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new LoggedUserActionRowMapper(), sqlPreparedStatementManager.getPreparedSqlParameters());
    }

    @Override
    public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(List<AdminEntry> visibleAdmins, String selectedAdmin, int selectedAction, Date from, Date to, String description) throws Exception {
		SqlPreparedStatementManager sqlPreparedStatementManager = new SqlPreparedStatementManager("SELECT logtime, username, supervisor_name, action, description FROM userlog_tbl");
		sqlPreparedStatementManager.addWhereClause("logtime >= ?", from);
		sqlPreparedStatementManager.addWhereClause("logtime <= ?", to);

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

		// If set, the selected action must match
		if (UserActivityLogActions.ANY.getIntValue() != selectedAction) {
			if (UserActivityLogActions.LOGIN_LOGOUT.getIntValue() == selectedAction) {
				sqlPreparedStatementManager.addWhereClause("action IN ('do login', 'do logout', 'login', 'logout', ?)", UserActivityLogActions.getLocalValue(selectedAction));
			} else if (UserActivityLogActions.ANY_WITHOUT_LOGIN.getIntValue() == selectedAction) {
				sqlPreparedStatementManager.addWhereClause("action NOT IN ('do login', 'do logout', 'login', 'logout', 'login_logout')");
			} else {
				sqlPreparedStatementManager.addAndClause();
				sqlPreparedStatementManager.appendOpeningParenthesis();
				String[] localValues = UserActivityLogActions.getLocalValues(selectedAction);
				for(int i = 0; i < localValues.length; i++){
					if(i != 0){
						sqlPreparedStatementManager.addOrClause();
					}
					sqlPreparedStatementManager.addWhereClauseSimple("(LOWER(action) LIKE ? OR LOWER(action) = ?)", localValues[i].toLowerCase() + " %", localValues[i].toLowerCase());
				}
				sqlPreparedStatementManager.appendClosingParenthesis();
				sqlPreparedStatementManager.addWhereClause("action NOT IN ('do login', 'do logout', 'login', 'logout', 'login_logout')");
			}
		}

		return sqlPreparedStatementManager;
	}
}
