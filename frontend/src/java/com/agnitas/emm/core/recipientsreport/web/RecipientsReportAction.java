/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.recipientsreport.service.impl.RecipientReportUtils;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.MimeTypeService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.DispatchBaseAction;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;

public class RecipientsReportAction extends DispatchBaseAction {
    @SuppressWarnings("unused")
    private static final transient Logger logger = Logger.getLogger(RecipientsReportAction.class);

    private static final Map<RecipientsReport.RecipientReportType, Permission> TYPE_PERMISSIONS;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    static {
        TYPE_PERMISSIONS = new HashMap<>();
        TYPE_PERMISSIONS.put(RecipientsReport.RecipientReportType.IMPORT_REPORT, Permission.WIZARD_IMPORT);
        TYPE_PERMISSIONS.put(RecipientsReport.RecipientReportType.EXPORT_REPORT, Permission.WIZARD_EXPORT);
    }

    protected RecipientsReportService recipientsReportService;
    protected WebStorage webStorage;
	private MimeTypeService mimeTypeService;

    @Required
    public void setRecipientsReportService(RecipientsReportService recipientsReportService) {
        this.recipientsReportService = recipientsReportService;
    }
    
    @Required
    public void setMimeTypeService(MimeTypeService mimeTypeService) {
        this.mimeTypeService = mimeTypeService;
    }

    public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return mapping.findForward("logon");
        }
        RecipientsReportForm reportForm = (RecipientsReportForm) form;
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.IMPORT_EXPORT_LOG_OVERVIEW, reportForm);
        reportForm.setDateFormatPattern(AgnUtils.getLocaleDateTimeFormat(request).toPattern());
        RecipientsReport.RecipientReportType[] reportTypes = getReportTypes(reportForm, admin);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Date startDate = getDate(reportForm.getFilterDateStart(), format);
        Date finishDate = getDate(reportForm.getFilterDateFinish(), format);
        request.setAttribute("reportsList",
                recipientsReportService.deleteOldReportsAndGetReports(admin.getCompanyID(),
                        reportForm.getPageNumber(), reportForm.getNumberOfRows(),
                        reportForm.getSort(), reportForm.getDir(),
                        startDate, finishDate,
                        reportTypes));
        return mapping.findForward("list");
    }

    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return mapping.findForward("logon");
        }
        int companyId = AgnUtils.getCompanyID(request);
        String reportContent = recipientsReportService.getImportReportContent(companyId, ((RecipientsReportForm) form).getReportId());
        if (reportContent != null && !reportContent.startsWith("<html>") && !reportContent.startsWith("<!DOCTYPE html>")) {
        	// Replace plain text linefeeds for html view
        	reportContent = reportContent.replace("\r\n", "\n").replace("\n", "<br />\n");
        }
        
        if (StringUtils.isBlank(reportContent)) {
        	reportContent = I18nString.getLocaleString("recipient.reports.notAvailable", admin.getLocale());
        }
        
        // Escape all text, even html, so it can be viewed in an iframe as docsource
        request.setAttribute("reportContentEscaped", StringEscapeUtils.escapeHtml(reportContent));
        return mapping.findForward("view");
    }

    public ActionForward download(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return mapping.findForward("logon");
        }
        int companyId = AgnUtils.getCompanyID(request);

        RecipientsReportForm reportForm = (RecipientsReportForm) form;

        if (reportForm.getReportId() < 1) {
            return null;
        }

        RecipientsReport report = recipientsReportService.getReport(companyId, reportForm.getReportId());
        if (report.getType() == RecipientsReport.RecipientReportType.EXPORT_REPORT
        		|| report.getType() == RecipientsReport.RecipientReportType.EXPORT_REPORT) {
            if (admin.permissionAllowed(Permission.WIZARD_EXPORT)) {
                String fileType = "export report";
                String reportContent = recipientsReportService.getImportReportContent(companyId, ((RecipientsReportForm) form).getReportId());
                
                if (StringUtils.isBlank(reportContent)) {
                    reportContent = I18nString.getLocaleString("recipient.reports.notAvailable", admin.getLocale());
                }
    
                String fileName = RecipientReportUtils.resolveFileName(report.getFilename(), reportContent);
    
                MediaType mediaType = MediaType.parseMediaType(mimeTypeService.getMimetypeForFile(fileName.toLowerCase()));
                if (MediaType.TEXT_HTML == mediaType) {
                    reportContent = reportContent.replace("\r\n", "\n").replace("\n", "<br />\n");
                }
                

                response.setContentType(mediaType.toString());
    	        HttpUtils.setDownloadFilenameHeader(response, fileName);
    	        IOUtils.write(reportContent.getBytes("UTF-8"), response.getOutputStream());
    	        
                writeUserActivityLog(AgnUtils.getAdmin(request), "download " + fileType, "File name: " + report.getFilename() + ", RecipientsReport ID: " + report.getId());
            }
        } else {
            if (admin.permissionAllowed(Permission.WIZARD_IMPORT)) {
            	byte[] fileData = recipientsReportService.getImportReportFileData(companyId, ((RecipientsReportForm) form).getReportId());
            	if (fileData != null) {
	                String fileType = "import report supplemental data";
	                String fileName = report.getFilename();
	                
	    	        response.setContentType(mimeTypeService.getMimetypeForFile(fileName.toLowerCase()));
	    	        HttpUtils.setDownloadFilenameHeader(response, fileName);
	    	        IOUtils.write(fileData, response.getOutputStream());
	    	        
	                writeUserActivityLog(AgnUtils.getAdmin(request), "download " + fileType, "File name: " + report.getFilename() + ", RecipientsReport ID: " + report.getId());
            	} else {
	                String fileType = "import report";
                    String reportContent = recipientsReportService.getImportReportContent(companyId, ((RecipientsReportForm) form).getReportId());
                    if (StringUtils.isBlank(reportContent)) {
                        reportContent = I18nString.getLocaleString("recipient.reports.notAvailable", admin.getLocale());
                    }
        
                    String fileName = RecipientReportUtils.resolveFileName(report.getFilename(), reportContent);
        
                    MediaType mediaType = MediaType.parseMediaType(mimeTypeService.getMimetypeForFile(fileName.toLowerCase()));
                    if (MediaType.TEXT_HTML == mediaType) {
                        reportContent = reportContent.replace("\r\n", "\n").replace("\n", "<br />\n");
                    }
        
                    response.setContentType(mediaType.toString());
	    	        HttpUtils.setDownloadFilenameHeader(response, fileName);
	    	        IOUtils.write(reportContent.getBytes("UTF-8"), response.getOutputStream());
	    	        
	                writeUserActivityLog(AgnUtils.getAdmin(request), "download " + fileType, "File name: " + report.getFilename() + ", RecipientsReport ID: " + report.getId());
            	}
            }
        }

        return null;
    }

    private RecipientsReport.RecipientReportType[] getReportTypes(RecipientsReportForm form, ComAdmin admin) {
        RecipientsReport.RecipientReportType[] reportTypes;
        if (form.getFilterTypes() == null) {
            reportTypes = RecipientsReport.RecipientReportType.values();
        } else {
            reportTypes = Arrays.stream(form.getFilterTypes())
                    .filter(Objects::nonNull)
                    .map(type -> {
                        try {
                            return RecipientsReport.RecipientReportType.valueOf(type);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(type -> admin.permissionAllowed(TYPE_PERMISSIONS.get(type)))
                    .collect(Collectors.toList())
                    .toArray(new RecipientsReport.RecipientReportType[]{});
        }
        return reportTypes;
    }

    private Date getDate(String stringDate, DateFormat format) {
        try {
            return format.parse(StringUtils.defaultString(stringDate));
        } catch (ParseException e) {
            return null;
        }
    }

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }
}
