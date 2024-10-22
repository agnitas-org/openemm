/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.service;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.dashboard.bean.DashboardWorkflow;
import com.agnitas.emm.core.dashboard.bean.ScheduledMailing;
import net.sf.json.JSONObject;
import org.agnitas.beans.impl.PaginatedListImpl;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface DashboardService {

    PaginatedListImpl<Map<String, Object>> getMailings(Admin admin, String sort, String direction, int rownums);

    List<Map<String, Object>> getLastSentWorldMailings(Admin admin, int rownums);

    JSONObject getStatisticsInfo(int mailingId, Locale locale, Admin admin) throws Exception;

    List<ScheduledMailing> getScheduledMailings(Admin admin, Date startDate, Date endDate);

    List<DashboardWorkflow> getWorkflows(Admin admin);

    List<DashboardRecipientReport> getRecipientReports(Admin admin);
}
