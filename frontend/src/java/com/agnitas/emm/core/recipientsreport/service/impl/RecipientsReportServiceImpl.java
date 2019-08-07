/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.service.impl;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.DateUtilities;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.emm.core.download.dao.DownloadDao;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dao.RecipientsReportDao;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;

public class RecipientsReportServiceImpl implements RecipientsReportService {

    protected RecipientsReportDao recipientsReportDao;
    protected DownloadDao downloadDao;
    protected ConfigService configService;

    @Required
    public void setRecipientsReportDao(RecipientsReportDao recipientsReportDao) {
        this.recipientsReportDao = recipientsReportDao;
    }

    @Required
    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    @Required
    public void setDownloadDao(DownloadDao downloadDao) {
        this.downloadDao = downloadDao;
    }

    @Override
    public RecipientsReport createAndSaveImportReport(int adminId, int companyId, String filename, int datasourceId, Date reportDate, String content, int autoImportID, boolean isError) {
        RecipientsReport report = new RecipientsReport();
        report.setAdminId(adminId);
        report.setDatasourceId(datasourceId);
        report.setFilename(filename);
        report.setReportDate(reportDate);
        report.setType(RecipientsReport.RecipientReportType.IMPORT_REPORT);
        if (autoImportID > 0) {
        	report.setAutoImportID(autoImportID);
        }
        report.setIsError(isError);
        recipientsReportDao.createReport(companyId, report, content);
        return report;
    }

    @Override
    public RecipientsReport createAndSaveExportReport(int adminId, int companyId, String filename, Date reportDate, String content, boolean isError) {
        RecipientsReport report = new RecipientsReport();
        report.setAdminId(adminId);
        report.setFilename(filename);
        report.setReportDate(reportDate);
        report.setType(RecipientsReport.RecipientReportType.EXPORT_REPORT);
        report.setIsError(isError);
        recipientsReportDao.createReport(companyId, report, content);
        return report;
    }

    @Override
    public String getImportReportContent(int companyId, int reportId){
        return recipientsReportDao.getReportTextContent(companyId, reportId);
    }

    @Override
    public byte[] getImportReportFileData(int companyId, int reportId) throws Exception{
        return recipientsReportDao.getReportFileData(companyId, reportId);
    }

    @Override
    public void writeContentOfExportReportToStream(int companyId, int reportId, OutputStream outputStream) throws Exception{
        downloadDao.writeContentOfExportReportToStream(companyId, reportId, outputStream);
    }

    @Override
    public PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types){
        return recipientsReportDao.getReports(companyId, pageNumber, pageSize, sortProperty, dir, startDate, finishDate, types);
    }

    @Override
    public int deleteOldReports(int companyId){
        int expireDays = configService.getIntegerValue(ConfigValue.ExpireRecipientsReport, companyId);
        Date oldestReportDate = DateUtilities.getDateOfDaysAgo(new Date(),  expireDays);
        downloadDao.deleteAllContentOfOldExportReports(companyId, oldestReportDate);
        return recipientsReportDao.deleteOldReports(companyId, oldestReportDate);
    }

    @Override
    @Transactional
    public PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types){
        deleteOldReports(companyId);
        return getReports(companyId, pageNumber, pageSize, sortProperty, dir, startDate, finishDate, types);
    }

    @Override
    public RecipientsReport getReport(int companyId, int reportId){
        return recipientsReportDao.getReport(companyId, reportId);
    }

	@Override
	public void createSupplementalReportData(int adminId, int companyId, String filename, int datasourceId, Date reportDate, File temporaryDataFile, String textContent, int autoImportID, boolean isError) throws Exception {
        RecipientsReport report = new RecipientsReport();
        report.setAdminId(adminId);
        report.setDatasourceId(datasourceId);
        report.setFilename(filename);
        report.setReportDate(reportDate);
        report.setType(RecipientsReport.RecipientReportType.IMPORT_REPORT);
        if (autoImportID > 0) {
        	report.setAutoImportID(autoImportID);
        }
        report.setIsError(isError);
		recipientsReportDao.createSupplementalReportData(companyId, report, temporaryDataFile, textContent);
	}
}

