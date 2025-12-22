/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
import com.agnitas.util.SqlPreparedStatementManager;

public interface UserActivityLogDao extends UserActivityLogDaoBase {

	List<String> getDistinctUsernames(Integer companyId);

	void writeUserActivityLog(Admin admin, String action, String description);

	PaginatedList<LoggedUserAction> getUserActivityEntries(UserActivityLogFilter filter);

	SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(UserActivityLogFilter filter);

	void deleteByUsernames(Set<String> username, int companyID);

}
