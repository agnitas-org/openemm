/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service;

import java.io.IOException;
import java.util.Map;

import com.agnitas.emm.core.birtreport.dto.MailingReportRequest;
import com.agnitas.emm.core.birtreport.dto.MailingStatisticReportResultDTO;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;

public interface ReportGenerationService {

    String WINDOW_WAIT_STATUS = "reportReady";

    Map<Integer, MailingStatisticReportResultDTO> getMailingsReportData(MailingReportRequest request, int companyId);

    byte[] generateReport(StatisticsReportRequest request, int reportId, int companyId) throws IOException;

    StatisticsReportRequest parseRequest(String reportRequestJson);

}
