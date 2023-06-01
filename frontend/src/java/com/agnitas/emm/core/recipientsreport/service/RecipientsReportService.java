/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;

public interface RecipientsReportService {
    RecipientsReport createAndSaveImportReport(int companyID, int adminID, String filename, int datasourceId, Date reportDate, String content, int autoImportID, boolean isError) throws Exception;

    RecipientsReport createAndSaveExportReport(int companyID, int adminID, String filename, Date reportDate, String content, boolean isError) throws Exception;

    String getImportReportContent(int companyId, int reportId);

    PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types);

    PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(Admin admin, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types);

    RecipientsReport getReport(@VelocityCheck int companyId, int reportId);
    
    RecipientsReport.RecipientReportType getReportType(@VelocityCheck int companyId, int reportId);
    
    DownloadRecipientReport getExportDownloadFileData(Admin admin, int reportId) throws UnsupportedEncodingException;
    
    DownloadRecipientReport getImportDownloadFileData(Admin admin, int reportId) throws Exception;
    
    void createSupplementalReportData(int companyID, int adminID, String filename, int datasourceId, Date reportDate, File temporaryDataFile, String textContent, int autoImportID, boolean isError) throws Exception;

	byte[] getImportReportFileData(int companyId, int reportId) throws Exception;
}
