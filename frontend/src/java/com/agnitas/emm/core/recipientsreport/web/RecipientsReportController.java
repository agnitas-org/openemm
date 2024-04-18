/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportSearchParams;
import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.dto.DownloadRecipientReport;
import com.agnitas.emm.core.recipientsreport.forms.RecipientsReportForm;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recipientsreport")
@PermissionMapping("recipientsreport")
@SessionAttributes(types = RecipientsReportSearchParams.class)
public class RecipientsReportController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(RecipientsReportController.class);
    
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

    @RequestMapping("/list.action")
    public String list(Admin admin, RecipientsReportForm reportForm, RecipientsReportSearchParams searchParams, Model model) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.IMPORT_EXPORT_LOG_OVERVIEW, reportForm);
        FormUtils.syncSearchParams(searchParams, reportForm, true);

        Date startDate = reportForm.getFilterDateStart().get(admin.getDateFormat());
        Date finishDate = reportForm.getFilterDateFinish().get(admin.getDateFormat());

        PaginatedListImpl<RecipientsReport> reports = isUiRedesign(admin)
                ? recipientsReportService.deleteOldReportsAndGetReports(reportForm, admin)
                : recipientsReportService.deleteOldReportsAndGetReports(admin,
                        reportForm.getPage(), reportForm.getNumberOfRows(),
                        reportForm.getSort(), reportForm.getDir(),
                        startDate, finishDate,
                        reportForm.getFilterTypes());

        model.addAttribute("reportsList", reports);

        if (isUiRedesign(admin)) {
            model.addAttribute("users", adminService.listAdminsByCompanyID(admin.getCompanyID()));
        }

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        
        writeUserActivityLog(admin, "Import/Export logs", "active tab - overview");

        return "recipient_reports";
    }

    private boolean isUiRedesign(Admin admin) {
        return admin.isRedesignedUiUsed(Permission.RECIPIENTS_REPORT_UI_MIGRATION);
    }

    @GetMapping("/search.action")
    public String search(RecipientsReportForm filter, RecipientsReportSearchParams searchParams, RedirectAttributes ra) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        ra.addFlashAttribute("recipientsReportForm", filter);
        return "redirect:/recipientsreport/list.action";
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
    public ResponseEntity<Resource> download(Admin admin, @PathVariable int reportId) throws Exception {
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

    private ResponseEntity<Resource> handleExportReportDownload(int reportId, Admin admin) throws Exception {
        DownloadRecipientReport fileData = recipientsReportService.getExportDownloadFileData(admin, reportId);
        
        if (fileData == null) {
            return null;
        }
        
        writeDownloadUserActivityLog(admin, fileData.getFilename(), reportId, "Export report");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(fileData.getFilename()))
                .contentType(fileData.getMediaType())
                .body(new ByteArrayResource(fileData.getContent()));
    }
    
    private ResponseEntity<Resource> handleImportReportDownload(int reportId, Admin admin) throws Exception {
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
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(fileData.getFilename()))
                .contentType(fileData.getMediaType())
                .body(new ByteArrayResource(fileData.getContent()));
    }
    
    private void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), logger);
    }

    private void writeDownloadUserActivityLog(Admin admin, String fileName, int reportId, String downloadType) {
        writeUserActivityLog(admin, "Import/Export logs download",
                String.format("%s - report ID: %d, file name: %s", downloadType, reportId, fileName));
    }
}
