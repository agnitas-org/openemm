/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.ecs.web;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.agnitas.beans.Admin;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.ecs.EcsModeType;
import com.agnitas.emm.ecs.form.EcsHeatmapForm;
import com.agnitas.messages.I18nString;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.ecs.EcsPreviewSize;
import com.agnitas.ecs.backend.service.EmbeddedClickStatService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.UserActivityUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;

@Controller
@PermissionMapping("heatmap")
public class EcsHeatmapController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(EcsHeatmapController.class);

    private static final String CHARSET_PATTERN = "<meta http-equiv *= *\"content-type\".*charset *= *[A-Za-z0-9-.:_]*";
    private static final String CSS_STYLES =
            "html {\n" +
            "    width: %dpx;\n" +
            "}\n" +
            "body {\n" +
            "    margin: 0;\n" +
            "}\n";

    private final EcsService ecsService;
    private final MailingBaseService mailingBaseService;
    private final GridServiceWrapper gridService;
    private final EmbeddedClickStatService embeddedClickStatService;
    private final ConfigService configService;
    private final UserActivityLogService userActivityLogService;
    private final MaildropService maildropService;
    private final MailinglistApprovalService mailinglistApprovalService;

    public EcsHeatmapController(EcsService ecsService, MailingBaseService mailingBaseService, GridServiceWrapper gridService, ConfigService configService,
                                EmbeddedClickStatService embeddedClickStatService, UserActivityLogService userActivityLogService, MaildropService maildropService,
                                MailinglistApprovalService mailinglistApprovalService) {
        this.ecsService = ecsService;
        this.mailingBaseService = mailingBaseService;
        this.gridService = gridService;
        this.embeddedClickStatService = embeddedClickStatService;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
        this.maildropService = maildropService;
        this.mailinglistApprovalService = mailinglistApprovalService;
    }

    @RequestMapping(value = "/mailing/{mailingId:\\d+}/heatmap/view.action", method = {RequestMethod.GET, RequestMethod.POST})
    public String view(Admin admin, @PathVariable int mailingId, Model model, @ModelAttribute("form") EcsHeatmapForm form, Popups popups) {
        int companyId = admin.getCompanyID();
        Map<Integer, String> testAndAdminRecipients = ecsService.getTestAndAdminRecipients(mailingId, companyId);

        // Default recipient for preview
        if (testAndAdminRecipients.isEmpty()) {
            popups.alert("error.preview.no_recipient");
        } else if (form.getRecipientId() <= 0 || !testAndAdminRecipients.containsKey(form.getRecipientId())) {
            form.setRecipientId(testAndAdminRecipients.keySet().iterator().next());
        }

        model.addAttribute("heatmapRecipients", testAndAdminRecipients);
        model.addAttribute("rangeColors", ecsService.getClickStatColors(companyId));
        model.addAttribute("templateId", gridService.getGridTemplateIdByMailingId(mailingId));
        model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, companyId));
        model.addAttribute("mailing", mailingBaseService.getMailing(companyId, mailingId));
        model.addAttribute("previewWidth", DeviceClass.getPreviewSizeByDeviceType(form.getDeviceType()).getWidth());
        model.addAttribute("isMailingUndoAvailable", mailingBaseService.checkUndoAvailable(mailingId));

        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("isActiveMailing", maildropService.isActiveMailing(mailingId, companyId));
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailingBaseService.getMailinglistId(mailingId, companyId)));
        }

        writeUserActivityLog(admin, new UserAction("view ecs", "active tab - heatmap"));
        return "ecs_view";
    }

    @RequestMapping("/mailing/{mailingId:\\d+}/heatmap/preview.action")
    public String preview(Admin admin, Model model, @PathVariable int mailingId, @ModelAttribute("form") EcsHeatmapForm form) {
        int recipientId = form.getRecipientId();
        int viewMode = form.getViewMode();

        if (mailingId > 0 && recipientId > 0) {
            model.addAttribute("previewAsString", previewAsString(mailingId, form));

            if (viewMode != EcsModeType.PURE_MAILING.getId()) {
                model.addAttribute("heatmapInfo", embeddedClickStatService.getStatsInfo(viewMode, mailingId, admin.getCompanyID(), form.getDeviceType()));
            }
        } else if (mailingId > 0) {
            model.addAttribute("isEmptyRecipientError", false);
        } else {
            logger.error("EmbeddedClickStatView: Parameters error (not enough parameters to show EmbeddedClickStat View)");
            model.addAttribute("isEmptyParamsError", true);
        }

        return "ecs_preview";
    }

    @PostMapping("/mailing/{mailingId:\\d+}/heatmap/export.action")
    public Object export(Admin admin, @PathVariable int mailingId, @ModelAttribute("form") EcsHeatmapForm form) throws IOException {
        String mailingName = mailingBaseService.getMailingName(mailingId, admin.getCompanyID());
        String previewHeatmapUrl = getHeatmapPreviewUrl(mailingId, form);
        File file = ecsService.generatePDF(admin, previewHeatmapUrl, mailingName);

        String filename = String.format("%s_%s_%s.pdf",
                mailingName,
                I18nString.getLocaleString("ecs.Heatmap", admin.getLocale()),
                new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM).format(new Date()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, HttpUtils.getContentDispositionAttachment(filename))
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new DeleteFileAfterSuccessReadResource(file));
    }

    private String getHeatmapPreviewUrl(int mailingId, EcsHeatmapForm form) {
        String sessionId = RequestContextHolder.getRequestAttributes().getSessionId();
        int previewSize = DeviceClass.getPreviewSizeByDeviceType(form.getDeviceType()).getId();
        return String.format("%s/mailing/%d/heatmap/preview.action;jsessionid=%s?" +
                        "recipientId=%d&viewMode=%d&deviceType=%d&previewSize=%d",
                StringUtils.removeEnd(configService.getValue(ConfigValue.SystemUrl), "/"),
                mailingId,
                sessionId,
                form.getRecipientId(),
                form.getViewMode(),
                form.getDeviceType(),
                previewSize);
    }

    private String previewAsString(int mailingId, EcsHeatmapForm form) {
        int recipientId = form.getRecipientId();
        int previewSizeId = form.getPreviewSize();

        String mailingContent = embeddedClickStatService.getMailingContent(mailingId, recipientId);

        Pattern pattern = Pattern.compile(CHARSET_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(mailingContent);
        mailingContent = matcher.replaceAll("<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8");


        EcsPreviewSize previewSize = EcsPreviewSize.getForIdOrNull(previewSizeId);

        String styles = getStyles(previewSize);

        if (StringUtils.containsIgnoreCase(mailingContent, "</head>")) {
            mailingContent = mailingContent.replaceAll("(?i)</head>", styles + "\n</head>");
        } else {
            mailingContent = styles + mailingContent;
        }
        return mailingContent;
    }

    private String getStyles(EcsPreviewSize previewSize) {
        if (previewSize == null) {
            return "";
        }

        return "<style>\n" + String.format(CSS_STYLES, previewSize.getWidth()) + "</style>\n";
    }

    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        UserActivityUtil.log(userActivityLogService, admin, userAction, logger);
    }
}
