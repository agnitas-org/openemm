/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.service;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import org.agnitas.beans.impl.PaginatedListImpl;

import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface RecipientsReportService {
    RecipientsReport createAndSaveImportReport(ComAdmin admin, String filename, int datasourceId, Date reportDate, String content, int autoImportID, boolean isError);

    RecipientsReport createAndSaveExportReport(ComAdmin admin, String filename, Date reportDate, String content, boolean isError);

    String getImportReportContent(int companyId, int reportId);

    PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types);

    int deleteOldReports(int companyId);

    PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types);

    PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(ComAdmin admin, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types);

    RecipientsReport getReport(@VelocityCheck int companyId, int reportId);
    
    RecipientsReport.RecipientReportType getReportType(@VelocityCheck int companyId, int reportId);
    
    @Deprecated
	void writeContentOfExportReportToStream(int companyId, int reportId, OutputStream outputStream) throws Exception;
    
    DownloadRecipientReport getExportDownloadFileData(ComAdmin admin, int reportId) throws UnsupportedEncodingException;
    
    DownloadRecipientReport getImportDownloadFileData(ComAdmin admin, int reportId) throws Exception;
    
    void createSupplementalReportData(ComAdmin admin, String filename, int datasourceId, Date reportDate, File temporaryDataFile, String textContent, int autoImportID, boolean isError) throws Exception;

	byte[] getImportReportFileData(int companyId, int reportId) throws Exception;
}
