/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.service;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import org.springframework.transaction.annotation.Transactional;

public interface RecipientsReportService {

    List<DashboardRecipientReport> getReportsForDashboard(int companyId);

    RecipientsReport saveNewReport(Admin admin, int companyId, RecipientsReport report, String content);

    String getImportReportContent(int companyId, int reportId);

    String getImportReportZipFileContent(Admin admin, int reportId);

    @Transactional
    PaginatedList<RecipientsReport> deleteOldReportsAndGetReports(RecipientsReportForm filter, Admin admin);

    RecipientsReport getReport(int companyId, int reportId);
    
    DownloadRecipientReport getRecipientReportForDownload(int reportId, Admin admin);

    void saveNewSupplementalReport(Admin admin, int companyId, RecipientsReport report, String content, File temporaryDataFile);

    File getZipToDownload(Set<Integer> ids, Admin admin);
}
