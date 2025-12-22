/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.recipientsreport.RecipientReportDownloadException;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportSearchParams;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.messages.I18nString;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.HttpUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recipientsreport")
@RequiredPermission("wizard.import|wizard.export")
@SessionAttributes(types = RecipientsReportSearchParams.class)
public class RecipientsReportController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(RecipientsReportController.class);
    private static final String REDIRECT_TO_OVERVIEW = "redirect:/recipientsreport/list.action?restoreSort=true";
    
    private final RecipientsReportService recipientsReportService;
    private final WebStorage webStorage;
    private final AdminService adminService;
    private final UserActivityLogService userActivityLogService;

    public RecipientsReportController(RecipientsReportService recipientsReportService, WebStorage webStorage,
                                      AdminService adminService, UserActivityLogService userActivityLogService) {
        this.recipientsReportService = recipientsReportService;
        this.webStorage = webStorage;
        this.adminService = adminService;
        this.userActivityLogService = userActivityLogService;
    }

    @InitBinder
    public void initBinder(Admin admin, WebDataBinder binder) {
        SimpleDateFormat dateFormat = admin.getDateFormat();
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @ExceptionHandler(RecipientReportDownloadException.class)
    public String onRecipientReportDownloadException(RecipientReportDownloadException ex, Popups popups) {
        ex.getErrors().forEach(popups::alert);
        return REDIRECT_TO_OVERVIEW;
    }

    @RequestMapping("/list.action")
    public String list(@RequestParam(required = false) Boolean restoreSort, RecipientsReportForm reportForm, RecipientsReportSearchParams searchParams,
                       Admin admin, Model model) {
        FormUtils.syncPaginationData(webStorage, WebStorage.IMPORT_EXPORT_LOG_OVERVIEW, reportForm, restoreSort);
        searchParams.restoreParams(reportForm);

        model.addAttribute("reportsList", recipientsReportService.deleteOldReportsAndGetReports(reportForm, admin));
        model.addAttribute("users", adminService.listAdminsByCompanyID(admin.getCompanyID()));
        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        
        writeUserActivityLog(admin, "Import/Export logs", "active tab - overview");

        return "recipient_reports";
    }

    @GetMapping("/search.action")
    public String search(RecipientsReportForm filter, RecipientsReportSearchParams searchParams, RedirectAttributes ra) {
        searchParams.storeParams(filter);
        ra.addFlashAttribute("recipientsReportForm", filter);
        return REDIRECT_TO_OVERVIEW;
    }

    @GetMapping("/{reportId:\\d+}/view.action")
    public String view(Admin admin, @PathVariable int reportId, Model model) {
        String reportContent = recipientsReportService.getImportReportContent(admin.getCompanyID(), reportId);
    
        if (StringUtils.equals(reportContent, "Downloadable file with invalid recipients data")) {
            reportContent = recipientsReportService.getImportReportZipFileContent(admin, reportId);
        }

        if (StringUtils.isBlank(reportContent)) {
            reportContent = I18nString.getLocaleString("recipient.reports.notAvailable", admin.getLocale());
        }
        
        if (RecipientReportUtils.resolveMediaType(reportContent) != MediaType.TEXT_HTML) {
            reportContent = reportContent.replace("\r\n", "\n").replace("\n", "<br />\n");
        }
    
        model.addAttribute("reportId", reportId);
        // Escape all text, even html, so it can be viewed in an iframe as docsource
        model.addAttribute("reportContentEscaped", StringEscapeUtils.escapeHtml4(reportContent));
        model.addAttribute("datasourceId", recipientsReportService.getReport(admin.getCompanyID(), reportId).getDatasourceId());
        
        writeUserActivityLog(admin, "Import/Export logs view", "Report ID: " + reportId);
    
        return "recipient_report_view";
    }

    @GetMapping("/{reportId:\\d+}/download.action")
    public Object download(@PathVariable int reportId, Admin admin, Popups popups) {
        DownloadRecipientReport file = recipientsReportService.getRecipientReportForDownload(reportId, admin);
        if (file == null) {
            popups.defaultError();
            return REDIRECT_TO_OVERVIEW;
        }
        writeDownloadUserActivityLog(admin, reportId, file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(file.getFilename()))
                .contentType(file.getMediaType())
                .body(new ByteArrayResource(file.getContent()));
    }

    @GetMapping(value = "/bulk/download.action")
    public ResponseEntity<FileSystemResource> bulkDownload(@RequestParam(required = false) Set<Integer> bulkIds, Admin admin) {
        File zip = recipientsReportService.getZipToDownload(bulkIds, admin);
        writeUserActivityLog(admin, "Import/Export logs download", String.format("downloaded %d log(s) as ZIP archive", bulkIds.size()));

        String downloadFileName = String.format("logs_%s.zip", admin.getDateFormat().format(new Date()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", downloadFileName))
                .contentLength(zip.length())
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(new DeleteFileAfterSuccessReadResource(zip));
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), logger);
    }

    private void writeDownloadUserActivityLog(Admin admin, int reportId, DownloadRecipientReport download) {
        String downloadType = getDownloadType(download);
        writeUserActivityLog(admin, "Import/Export logs download",
                String.format("%s - report ID: %d, file name: %s", downloadType, reportId, download.getFilename()));
    }

    private static String getDownloadType(DownloadRecipientReport report) {
        if (report.getType() == null) {
            return "Unknown report type";
        }

        switch (report.getType()) {
            case EXPORT:
                return "Export report";
            case IMPORT:
                if (report.isSuplemental()) {
                    return "Import report's supplemental data";
                }
                return "Import report";
            default:
                return report.getType().name();
        }
    }

}
