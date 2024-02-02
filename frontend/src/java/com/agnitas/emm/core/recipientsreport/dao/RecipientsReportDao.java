/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.dao;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import org.agnitas.beans.impl.PaginatedListImpl;

import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;

public interface RecipientsReportDao {

    List<DashboardRecipientReport> getReportsForDashboard(int companyId);

    @Deprecated
    void createReport(int companyId, RecipientsReport report, String fileContent) throws Exception;

    void createNewReport(int companyId, RecipientsReport report, String fileContent) throws Exception;

    String getReportTextContent(int companyId, int reportId);

    PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types);

    RecipientsReport getReport(int companyId, int reportId);

    int deleteOldReports(int companyId, Date oldestReportDate);
    
    boolean deleteReportsByCompany(int companyId);

    @Deprecated
	void createSupplementalReportData(int companyId, RecipientsReport report, File temporaryDataFile, String textContent) throws Exception;

	void createNewSupplementalReport(int companyId, RecipientsReport report, File temporaryDataFile, String textContent) throws Exception;

	byte[] getReportFileData(int companyId, int reportId) throws Exception;
    
    RecipientsReport.RecipientReportType getReportType(int companyId, int reportId);
}
