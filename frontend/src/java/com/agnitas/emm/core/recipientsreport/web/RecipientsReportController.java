/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recipientsreport")
@PermissionMapping("recipientsreport")
public class RecipientsReportController {
    private static final transient Logger logger = Logger.getLogger(RecipientsReportController.class);

    private static final String DATE_FORMAT = DateUtilities.YYYY_MM_DD;

    private RecipientsReportService recipientsReportService;
    private WebStorage webStorage;
    private UserActivityLogService userActivityLogService;

    public RecipientsReportController(RecipientsReportService recipientsReportService, WebStorage webStorage,
                                      UserActivityLogService userActivityLogService) {
        this.recipientsReportService = recipientsReportService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
    }

    @RequestMapping("/list.action")
    public String list(ComAdmin admin, RecipientsReportForm reportForm, Model model) {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.IMPORT_EXPORT_LOG_OVERVIEW, reportForm);

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Date startDate = reportForm.getFilterDateStart().get(format);
        Date finishDate = reportForm.getFilterDateFinish().get(format);


        PaginatedListImpl<RecipientsReport> reports =
                recipientsReportService.deleteOldReportsAndGetReports(admin,
                        reportForm.getPage(), reportForm.getNumberOfRows(),
                        reportForm.getSort(), reportForm.getDir(),
                        startDate, finishDate,
                        reportForm.getFilterTypes());

        model.addAttribute("reportsList", reports);
        model.addAttribute("dateFormatPattern", DATE_FORMAT);
        model.addAttribute("adminDateTimeFormatPattern", admin.getDateTimeFormat().toPattern());
        
        writeUserActivityLog(admin, "Import/Export logs", "active tab - overview");

        return "recipient_reports";
    }

    @GetMapping("/{reportId:\\d+}/view.action")
    public String view(ComAdmin admin, @PathVariable int reportId, Model model) {
        String reportContent = recipientsReportService.getImportReportContent(admin.getCompanyID(), reportId);
    
        if (StringUtils.isBlank(reportContent)) {
            reportContent = I18nString.getLocaleString("recipient.reports.notAvailable", admin.getLocale());
        }
        
        if (RecipientReportUtils.resolveMediaType(reportContent) != MediaType.TEXT_HTML) {
            reportContent = reportContent.replace("\r\n", "\n").replace("\n", "<br />\n");
        }
    
        model.addAttribute("reportId", reportId);
        // Escape all text, even html, so it can be viewed in an iframe as docsource
        model.addAttribute("reportContentEscaped", StringEscapeUtils.escapeHtml4(reportContent));
        
        writeUserActivityLog(admin, "Import/Export logs view", "Report ID: " + reportId);
    
        return "recipient_report_view";
    }

    @GetMapping("/{reportId:\\d+}/download.action")
    public ResponseEntity<Resource> download(ComAdmin admin, @PathVariable int reportId) throws Exception {
        RecipientsReport.RecipientReportType reportType = recipientsReportService.getReportType(admin.getCompanyID(), reportId);
        
        if (reportType != null) {
            switch (reportType) {
                case EXPORT_REPORT:
                    if (admin.permissionAllowed(Permission.WIZARD_EXPORT)) {
                        return handleExportReportDownload(reportId, admin);
                    }
                    break;
                case IMPORT_REPORT:
                    if (admin.permissionAllowed(Permission.WIZARD_IMPORT)) {
                        return handleImportReportDownload(reportId, admin);
                    }
                    break;
                default:
                    //nothing do
            }
        }
        
        return null;
    }

    private ResponseEntity<Resource> handleExportReportDownload(int reportId, ComAdmin admin) throws Exception {
        DownloadRecipientReport fileData = recipientsReportService.getExportDownloadFileData(admin, reportId);
        
        if (fileData == null) {
            return null;
        }
        
        writeDownloadUserActivityLog(admin, fileData.getFilename(), reportId, "Export report");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + HttpUtils.escapeFileName(fileData.getFilename()) + "\";")
                .contentType(fileData.getMediaType())
                .body(new ByteArrayResource(fileData.getContent()));
    }
    
    private ResponseEntity<Resource> handleImportReportDownload(int reportId, ComAdmin admin) throws Exception {
        DownloadRecipientReport fileData = recipientsReportService.getImportDownloadFileData(admin, reportId);
        
        if (fileData == null) {
            return null;
        }
        
        if (fileData.isSuplemental()) {
            writeDownloadUserActivityLog(admin, fileData.getFilename(), reportId, "Import report's supplemental data");
        } else {
            writeDownloadUserActivityLog(admin, fileData.getFilename(), reportId, "Import report");
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + HttpUtils.escapeFileName(fileData.getFilename()) + "\";")
                .contentType(fileData.getMediaType())
                .body(new ByteArrayResource(fileData.getContent()));
    }
    
    private void writeUserActivityLog(ComAdmin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), logger);
    }

    private void writeDownloadUserActivityLog(ComAdmin admin, String fileName, int reportId, String downloadType) {
        writeUserActivityLog(admin, "Import/Export logs download",
                String.format("%s - report ID: %d, file name: %s", downloadType, reportId, fileName));
    }
}
