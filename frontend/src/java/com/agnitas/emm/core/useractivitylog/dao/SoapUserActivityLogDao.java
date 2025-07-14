/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.useractivitylog.bean.SoapUserActivityAction;
import com.agnitas.emm.core.useractivitylog.forms.SoapUserActivityLogFilter;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.util.SqlPreparedStatementManager;

public interface SoapUserActivityLogDao extends UserActivityLogDaoBase {

    void writeWebServiceUsage(ZonedDateTime timestamp, String endpoint, int companyID, String user, String clientIp);

    // TODO: EMMGUI-714: Remove when removing old design
    PaginatedListImpl<SoapUserActivityAction> getUserActivityEntries(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to, String sortColumn, String sortDirection, int pageNumber, int pageSize);
    PaginatedListImpl<SoapUserActivityAction> getUserActivityEntries(SoapUserActivityLogFilter filter, List<AdminEntry> visibleAdmins);

    // TODO: EMMGUI-714 remove when removing old design
    SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(List<AdminEntry> visibleAdmins, String selectedAdmin, Date from, Date to);
    SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(SoapUserActivityLogFilter filter, List<AdminEntry> visibleAdmins);

}
