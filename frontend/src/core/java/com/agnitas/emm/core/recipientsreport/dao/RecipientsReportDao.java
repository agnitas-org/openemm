/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.dao;

import java.util.List;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;

public interface RecipientsReportDao {

    List<DashboardRecipientReport> getReportsForDashboard(int companyId);

    void createNewReport(int companyId, RecipientsReport report, String fileContent);

    String getReportTextContent(int companyId, int reportId);

    PaginatedList<RecipientsReport> getReports(RecipientsReportForm filter, int companyId);

    RecipientsReport getReport(int companyId, int reportId);

    boolean deleteReportsByCompany(int companyId);

	void createNewSupplementalReport(int companyId, RecipientsReport report, byte[] content, String textContent);

	byte[] getReportFileData(int companyId, int reportId);
    
    RecipientsReport.EntityType getReportType(int companyId, int reportId);
}
