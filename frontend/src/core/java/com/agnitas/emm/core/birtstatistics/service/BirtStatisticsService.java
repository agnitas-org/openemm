/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportStatisticDto;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.reporting.birt.external.beans.RecipientStatusRow;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.logging.log4j.Logger;

public interface BirtStatisticsService {

	// TODO: GWUA-7067: Remove after migration
	Map<String, String> getReportStatisticsUrlMap(BirtReport report, List<BirtReportSettings> reportSettings);

	Map<String, StatisticsReportRequest> getReportRequestsMap(BirtReport report, List<BirtReportSettings> reportSettings);

	File generateReportFile(int reportId, int companyId, StatisticsReportRequest reportRequest);

	StatisticsReportRequest getReportRequest(BirtReport report, BirtReportSettings settings, Admin admin);

	// TODO: GWUA-7000: Remove when removing old code (if possible)
    String getMailingStatisticUrl(Admin admin, String sessionId, MailingStatisticDto mailingStatistic);

	DateTimeRange getDateTimeRestrictions(DateTimeRange selectedDate, DateMode dateMode, LocalDateTime mailingStart, int year, Month month);

	// TODO: GWUA-7000: Remove when removing old code (if possible)
    String changeFormat(String inputUrl, String newFormat);

	// TODO: GWUA-7067: Remove after migration
	File getReportTmpFile(int birtReportId, String birtUrl, CloseableHttpClient httpClient, Logger logger);

	// TODO: GWUA-7000: Remove when removing old code
	String getWorkflowStatisticUrl(Admin admin, WorkflowStatisticDto workflowStatisticDto);

    boolean isWorldMailing(Mailing mailing);

	// TODO: GWUA-7067: Remove after migration
	String getReportStatisticsUrl(Admin admin, BirtReportStatisticDto settings);

	File getReportTmpFile(BirtReportDownload birtDownload, int companyId);

	List<RecipientStatusRow> getRecipientStatusStatistic(int targetId, int mailinglistId, Admin admin);

}
