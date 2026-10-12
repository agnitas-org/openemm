/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.dashboard.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.dashboard.bean.DashboardWorkflow;
import com.agnitas.emm.core.dashboard.bean.ScheduledMailing;
import com.agnitas.emm.core.dashboard.dto.ClickersStatisticsResponse;
import com.agnitas.emm.core.dashboard.dto.DashboardStatisticsResponse;
import com.agnitas.emm.core.dashboard.dto.OpenersStatisticsResponse;

public interface DashboardService {

    PaginatedList<Map<String, Object>> getMailings(Admin admin, String sort, String direction, int rownums);

    List<Map<String, Object>> getLastSentWorldMailings(Admin admin, int rownums);

    List<ScheduledMailing> getScheduledMailings(Admin admin, Date startDate, Date endDate);

    List<DashboardWorkflow> getWorkflows(Admin admin);

    List<DashboardRecipientReport> getRecipientReports(Admin admin);

    OpenersStatisticsResponse getOpenersStatistics(int mailingId, int companyId);

    ClickersStatisticsResponse getClickersStatistics(int mailingId, int companyId);

    DashboardStatisticsResponse getStatistics(int mailingId, int companyId);

}
