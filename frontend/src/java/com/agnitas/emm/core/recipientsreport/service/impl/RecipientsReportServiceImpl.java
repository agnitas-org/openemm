/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.service.impl;

import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.download.dao.DownloadDao;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dao.RecipientsReportDao;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.messages.I18nString;
import com.agnitas.service.MimeTypeService;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

public class RecipientsReportServiceImpl implements RecipientsReportService {

    private static final Map<RecipientsReport.RecipientReportType, Permission> TYPE_PERMISSIONS;

    static {
        TYPE_PERMISSIONS = new HashMap<>();
        TYPE_PERMISSIONS.put(RecipientsReport.RecipientReportType.IMPORT_REPORT, Permission.WIZARD_IMPORT);
        TYPE_PERMISSIONS.put(RecipientsReport.RecipientReportType.EXPORT_REPORT, Permission.WIZARD_EXPORT);
    }

    private RecipientsReportDao recipientsReportDao;
    private DownloadDao downloadDao;
    private ConfigService configService;
    private MimeTypeService mimeTypeService;

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
    
    @Required
    public void setMimeTypeService(MimeTypeService mimeTypeService) {
        this.mimeTypeService = mimeTypeService;
    }
    
    @Override
    public RecipientsReport createAndSaveImportReport(ComAdmin admin, String filename, int datasourceId, Date reportDate, String content, int autoImportID, boolean isError) {
        RecipientsReport report = new RecipientsReport();
        report.setAdminId(admin.getAdminID());
        report.setDatasourceId(datasourceId);
        report.setFilename(filename);
        report.setReportDate(reportDate);
        report.setType(RecipientsReport.RecipientReportType.IMPORT_REPORT);
        if (autoImportID > 0) {
        	report.setAutoImportID(autoImportID);
        }
        report.setIsError(isError);
        recipientsReportDao.createReport(admin.getCompanyID(), report, content);
        return report;
    }

    @Override
    public RecipientsReport createAndSaveExportReport(ComAdmin admin, String filename, Date reportDate, String content, boolean isError) {
        RecipientsReport report = new RecipientsReport();
        report.setAdminId(admin.getAdminID());
        report.setFilename(filename);
        report.setReportDate(reportDate);
        report.setType(RecipientsReport.RecipientReportType.EXPORT_REPORT);
        report.setIsError(isError);
        recipientsReportDao.createReport(admin.getCompanyID(), report, content);
        return report;
    }

    @Override
    public String getImportReportContent(int companyId, int reportId) {
        return recipientsReportDao.getReportTextContent(companyId, reportId);
    }

    @Override
    public byte[] getImportReportFileData(int companyId, int reportId) throws Exception {
        return recipientsReportDao.getReportFileData(companyId, reportId);
    }

    @Override
    @Deprecated
    public void writeContentOfExportReportToStream(int companyId, int reportId, OutputStream outputStream) throws Exception{
        downloadDao.writeContentOfExportReportToStream(companyId, reportId, outputStream);
    }

    @Override
    public PaginatedListImpl<RecipientsReport> getReports(int companyId, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types){
        return recipientsReportDao.getReports(companyId, pageNumber, pageSize, sortProperty, dir, startDate, finishDate, types);
    }

    @Override
    public int deleteOldReports(int companyId) {
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
    @Transactional
    public PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(ComAdmin admin, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types){
        int companyId = admin.getCompanyID();
        deleteOldReports(companyId);
        return getReports(companyId, pageNumber, pageSize, sortProperty, dir, startDate, finishDate, getAllowedReportTypes(types, admin));
    }

    @Override
    public RecipientsReport getReport(@VelocityCheck int companyId, int reportId){
        if (reportId > 0 && companyId > 0) {
            return recipientsReportDao.getReport(companyId, reportId);
        }
        return null;
    }

    @Override
    public RecipientsReport.RecipientReportType getReportType(@VelocityCheck int companyId, int reportId) {
        if (reportId > 0 && companyId > 0) {
            return recipientsReportDao.getReportType(companyId, reportId);
        }
        return null;
    }
    
    @Override
    public DownloadRecipientReport getExportDownloadFileData(ComAdmin admin, int reportId) throws UnsupportedEncodingException {
        RecipientsReport report = getReport(admin.getCompanyID(), reportId);
        if (report != null) {
            String reportContent = getImportReportContent(admin.getCompanyID(), reportId);
            return getDownloadReportData(admin, report.getFilename(), reportContent);
        }
    
        return null;
    }
    
    private DownloadRecipientReport getDownloadReportData(ComAdmin admin, String filename, String reportContent) throws UnsupportedEncodingException {
        DownloadRecipientReport recipientReport = new DownloadRecipientReport();
        if (StringUtils.isBlank(reportContent)) {
            reportContent = I18nString.getLocaleString("recipient.reports.notAvailable", admin.getLocale());
            recipientReport.setMediaType(MediaType.TEXT_PLAIN);
            recipientReport.setFilename(FilenameUtils.removeExtension(filename) + RecipientReportUtils.TXT_EXTENSION);
        } else {
            recipientReport.setFilename(RecipientReportUtils.resolveFileName(filename, reportContent));
            MediaType mediaType = MediaType.parseMediaType(mimeTypeService.getMimetypeForFile(filename.toLowerCase()));
            recipientReport.setMediaType(mediaType);
            if (MediaType.TEXT_HTML == mediaType) {
                reportContent = reportContent.replace("\r\n", "\n").replace("\n", "<br />\n");
            }
        }
        
        recipientReport.setContent(reportContent.getBytes("UTF-8"));
        return recipientReport;
    }
    
    @Override
    public DownloadRecipientReport getImportDownloadFileData(ComAdmin admin, int reportId) throws Exception {
        int companyId = admin.getCompanyID();
        RecipientsReport report = getReport(companyId, reportId);
        
        if (report == null) {
            return null;
        }

        byte[] fileData = getImportReportFileData(companyId, reportId);
    
        if (fileData != null) {
            DownloadRecipientReport recipientReport = new DownloadRecipientReport();
            recipientReport.setContent(fileData);
            recipientReport.setFilename(report.getFilename());
            String mimeType = mimeTypeService.getMimetypeForFile(StringUtils.lowerCase(report.getFilename()));
            recipientReport.setMediaType(MediaType.parseMediaType(mimeType));
            recipientReport.setSuplemental(true);
            return recipientReport;
        } else {
            String reportContent = getImportReportContent(companyId, reportId);
            return getDownloadReportData(admin, report.getFilename(), reportContent);
        }
    }
    
    @Override
	public void createSupplementalReportData(ComAdmin admin, String filename, int datasourceId, Date reportDate, File temporaryDataFile, String textContent, int autoImportID, boolean isError) throws Exception {
        RecipientsReport report = new RecipientsReport();
        report.setAdminId(admin.getAdminID());
        report.setDatasourceId(datasourceId);
        report.setFilename(filename);
        report.setReportDate(reportDate);
        report.setType(RecipientsReport.RecipientReportType.IMPORT_REPORT);
        if (autoImportID > 0) {
        	report.setAutoImportID(autoImportID);
        }
        report.setIsError(isError);
		recipientsReportDao.createSupplementalReportData(admin.getCompanyID(), report, temporaryDataFile, textContent);
	}

    private RecipientsReport.RecipientReportType[] getAllowedReportTypes(final RecipientsReport.RecipientReportType[] reportTypes, final ComAdmin admin) {
        RecipientsReport.RecipientReportType[] result;
        if (reportTypes == null) {
            result = RecipientsReport.RecipientReportType.values();
        } else {
            result = Arrays.stream(reportTypes)
                    .filter(Objects::nonNull)
                    .filter(type -> admin.permissionAllowed(TYPE_PERMISSIONS.get(type)))
                    .collect(Collectors.toList())
                    .toArray(new RecipientsReport.RecipientReportType[]{});
        }
        return result;
    }
}

