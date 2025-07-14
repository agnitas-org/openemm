/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.util.SqlPreparedStatementManager;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.bean.RestfulUserActivityAction;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;

public interface RestfulUserActivityLogDao extends UserActivityLogDaoBase {

    void writeUserActivityLog(String endpoint, String description, String httpMethod, String host, Admin admin);

    // TODO: EMMGUI-714: Remove when removing old design
    PaginatedListImpl<RestfulUserActivityAction> getUserActivityEntries(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to, String description, String sortColumn, String sortDirection, int pageNumber, int pageSize);
    PaginatedListImpl<RestfulUserActivityAction> getUserActivityEntriesRedesigned(RestfulUserActivityLogFilter filter, List<AdminEntry> visibleAdmins);

    // TODO: EMMGUI-714: Remove when removing old design
    SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to, String description);

    SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(RestfulUserActivityLogFilter filter, List<AdminEntry> visibleAdmins);

}
