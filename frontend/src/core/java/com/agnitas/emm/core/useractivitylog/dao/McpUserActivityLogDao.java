/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.McpUserActivityAction;
import com.agnitas.emm.core.useractivitylog.forms.McpUserActivityLogFilter;
import com.agnitas.util.SqlPreparedStatementManager;

public interface McpUserActivityLogDao {

    PaginatedList<McpUserActivityAction> getUserActivityEntries(McpUserActivityLogFilter filter);

    SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(McpUserActivityLogFilter filter);

    void writeUserActivityLog(String toolName, String source, Admin admin);

    void deleteByUsernames(Set<String> usernames, int companyID);

    List<String> getDistinctUsernames(Integer companyId);

    List<String> getDistinctSources(Integer companyId);

}
