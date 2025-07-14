/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.service.impl;

import static com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils.IMPORT_RESULT_FILE_PREFIX;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.common.exceptions.ZipDownloadException;
import com.agnitas.emm.common.service.BulkFilesDownloadService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.recipientsreport.RecipientReportDownloadException;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport.EntityType;
import com.agnitas.emm.core.recipientsreport.dao.RecipientsReportDao;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.messages.I18nString;
import com.agnitas.service.MimeTypeService;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.Tuple;
import com.agnitas.util.ZipUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

public class RecipientsReportServiceImpl implements RecipientsReportService {

    private static final Logger logger = LogManager.getLogger(RecipientsReportServiceImpl.class);

    private static final Map<EntityType, Permission> REPORT_TYPE_PERMISSIONS = Map.of(
            EntityType.IMPORT, Permission.WIZARD_IMPORT,
            EntityType.EXPORT, Permission.WIZARD_EXPORT
    );

    private RecipientsReportDao recipientsReportDao;
    private MimeTypeService mimeTypeService;
    private BulkFilesDownloadService bulkFilesDownloadService;

    @Override
    public List<DashboardRecipientReport> getReportsForDashboard(int companyId) {
        return recipientsReportDao.getReportsForDashboard(companyId);
    }

    @Override
    public RecipientsReport saveNewReport(Admin admin, int companyId, RecipientsReport report, String content) {
        report.setAdminId(admin != null ? admin.getAdminID() : 0);
        recipientsReportDao.createNewReport(companyId, report, content);
        return report;
    }

    @Override
    public String getImportReportContent(int companyId, int reportId) {
        return recipientsReportDao.getReportTextContent(companyId, reportId);
    }

    @Override
    public byte[] getImportReportFileData(int companyId, int reportId) {
        return recipientsReportDao.getReportFileData(companyId, reportId);
    }

    @Override
    public String getImportReportZipFileContent(Admin admin, int reportId) {
        try {
            byte[] fileBytes = getImportReportFileData(admin.getCompanyID(), reportId);
            return IOUtils.toString(ZipUtilities.unzip(fileBytes), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            logger.error("Cant read invalidRecipientsCsv file: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional
    public PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(Admin admin, int pageNumber, int pageSize, String sortProperty, String dir, Date startDate, Date finishDate, RecipientsReport.RecipientReportType...types){
        int companyId = admin.getCompanyID();

        RecipientsReport.RecipientReportType[] allowedTypes;
        if (types == null) {
            allowedTypes = RecipientsReport.RecipientReportType.values();
        } else {
            Map<RecipientsReport.RecipientReportType, Permission> TYPE_PERMISSIONS = Map.of(
                    RecipientsReport.RecipientReportType.IMPORT_REPORT, Permission.WIZARD_IMPORT,
                    RecipientsReport.RecipientReportType.EXPORT_REPORT, Permission.WIZARD_EXPORT
            );
            allowedTypes = Arrays.stream(types)
                    .filter(Objects::nonNull)
                    .filter(type -> admin.permissionAllowed(TYPE_PERMISSIONS.get(type)))
                    .toList()
                    .toArray(new RecipientsReport.RecipientReportType[]{});
        }

        PaginatedListImpl<RecipientsReport> returnList = recipientsReportDao.getReports(companyId, pageNumber, pageSize, sortProperty, dir, startDate, finishDate, allowedTypes);
        DateTimeFormatter formatter = admin.getDateTimeFormatter();
        ZoneId dbTimezone = ZoneId.systemDefault();
        for (RecipientsReport item : returnList.getList()) {
    		ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(item.getReportDate().toInstant(), dbTimezone);
        	item.setReportDateFormatted(formatter.format(dbZonedDateTime));
        }
        return returnList;
    }

    @Override
    @Transactional
    public PaginatedListImpl<RecipientsReport> deleteOldReportsAndGetReports(RecipientsReportForm filter, Admin admin){
        int companyId = admin.getCompanyID();
        filter.setTypes(getAllowedReportTypes(filter.getTypes(), admin));
        PaginatedListImpl<RecipientsReport> returnList = recipientsReportDao.getReports(filter, companyId);
        DateTimeFormatter formatter = admin.getDateTimeFormatter();
        ZoneId dbTimezone = ZoneId.systemDefault();
        for (RecipientsReport item : returnList.getList()) {
    		ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(item.getReportDate().toInstant(), dbTimezone);
        	item.setReportDateFormatted(formatter.format(dbZonedDateTime));
        }
        return returnList;
    }

    @Override
    public RecipientsReport getReport(int companyId, int reportId){
        if (reportId > 0 && companyId > 0) {
            return recipientsReportDao.getReport(companyId, reportId);
        }
        return null;
    }

    @Override
    public DownloadRecipientReport getRecipientReportForDownload(int reportId, Admin admin) {
        EntityType type = recipientsReportDao.getReportType(admin.getCompanyID(), reportId);
       
        if (type == EntityType.EXPORT && admin.permissionAllowed(Permission.WIZARD_EXPORT)) {
            return getExportDownloadFileData(admin, reportId);
        }
        if (type == EntityType.IMPORT && admin.permissionAllowed(Permission.WIZARD_IMPORT)) {
            return getImportDownloadFileData(admin, reportId);
        }
        return null;
    }

    private DownloadRecipientReport getExportDownloadFileData(Admin admin, int reportId) {
        RecipientsReport report = getReport(admin.getCompanyID(), reportId);
        if (report == null) {
            return null;
        }
        String reportContent = getImportReportContent(admin.getCompanyID(), reportId);
        DownloadRecipientReport download = getDownloadReportData(admin, report.getFilename(), reportContent);
        download.setType(EntityType.EXPORT);
        return download;    
    }
    
    private DownloadRecipientReport getDownloadReportData(Admin admin, String filename, String reportContent) {
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
        
        recipientReport.setContent(reportContent.getBytes(StandardCharsets.UTF_8));
        return recipientReport;
    }
    
    private DownloadRecipientReport getImportDownloadFileData(Admin admin, int reportId) {
        int companyId = admin.getCompanyID();
        RecipientsReport report = getReport(companyId, reportId);
        
        if (report == null) {
            return null;
        }

        byte[] fileData = getImportReportFileData(companyId, reportId);
    
        if (fileData == null) {
            String reportContent = getImportReportContent(companyId, reportId);
            String fileName = generateDownloadImportFileName(report);
            DownloadRecipientReport recipientReport = getDownloadReportData(admin, fileName, reportContent);
            recipientReport.setType(EntityType.IMPORT);
            return recipientReport;
        }
        DownloadRecipientReport recipientReport = new DownloadRecipientReport();
        recipientReport.setType(EntityType.IMPORT);
        recipientReport.setContent(fileData);
        recipientReport.setFilename(report.getFilename());
        String mimeType = mimeTypeService.getMimetypeForFile(StringUtils.lowerCase(report.getFilename()));
        recipientReport.setMediaType(MediaType.parseMediaType(mimeType));
        recipientReport.setSuplemental(true);
        return recipientReport;
    }

    private String generateDownloadImportFileName(RecipientsReport report) {
        return String.format("%s_%s_%d", IMPORT_RESULT_FILE_PREFIX,
                FilenameUtils.removeExtension(report.getFilename()),
                report.getDatasourceId());
    }

    @Override
    public File getZipToDownload(Set<Integer> ids, Admin admin) {
        try {
            return bulkFilesDownloadService.getZipToDownload(ids, "recipient-logs", id -> {
                DownloadRecipientReport file = getRecipientReportForDownload(id, admin);
                return file == null ? null : new Tuple<>(HttpUtils.escapeFileName(file.getFilename()), file.getContent());
            });
        } catch (ZipDownloadException e) {
            throw new RecipientReportDownloadException(e.getErrors());
        }
    }

    @Override
    public void saveNewSupplementalReport(Admin admin, int companyId, RecipientsReport report, String content, File temporaryDataFile) throws Exception {
        report.setAdminId(admin != null ? admin.getAdminID() : 0);
        recipientsReportDao.createNewSupplementalReport(companyId, report, temporaryDataFile, content);
    }

    private EntityType[] getAllowedReportTypes(EntityType[] types, final Admin admin) {
        if (types == null) {
            return EntityType.values();
        }
        return Arrays.stream(types)
                .filter(Objects::nonNull)
                .filter(type -> admin.permissionAllowed(REPORT_TYPE_PERMISSIONS.get(type)))
                .toList()
                .toArray(new EntityType[]{});
    }

    public void setRecipientsReportDao(RecipientsReportDao recipientsReportDao) {
        this.recipientsReportDao = recipientsReportDao;
    }

    public void setMimeTypeService(MimeTypeService mimeTypeService) {
        this.mimeTypeService = mimeTypeService;
    }

    public void setBulkFilesDownloadService(BulkFilesDownloadService bulkFilesDownloadService) {
        this.bulkFilesDownloadService = bulkFilesDownloadService;
    }
}
