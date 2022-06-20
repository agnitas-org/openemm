/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.ecs.web;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.ecs.EcsPreviewSize;
import org.agnitas.ecs.backend.service.EmbeddedClickStatService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HtmlUtils;
import org.agnitas.util.HttpUtils;
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
import org.w3c.dom.Document;

import com.agnitas.beans.ComAdmin;
import com.agnitas.ecs.service.EcsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.ecs.EcsModeType;
import com.agnitas.emm.ecs.form.EcsHeatmapForm;
import com.agnitas.messages.I18nString;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

import cz.vutbr.web.domassign.DeclarationMap;

@Controller
@PermissionMapping("heatmap")
public class EcsHeatmapController {

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
    private final ComMailingBaseService mailingBaseService;
    private final GridServiceWrapper gridService;
    private final EmbeddedClickStatService embeddedClickStatService;
    private ConfigService configService;
    private final UserActivityLogService userActivityLogService;

    public EcsHeatmapController(EcsService ecsService,
                                ComMailingBaseService mailingBaseService,
                                GridServiceWrapper gridService,
                                EmbeddedClickStatService embeddedClickStatService,
                                ConfigService configService,
                                UserActivityLogService userActivityLogService) {
        this.ecsService = ecsService;
        this.mailingBaseService = mailingBaseService;
        this.gridService = gridService;
        this.embeddedClickStatService = embeddedClickStatService;
        this.configService = configService;
        this.userActivityLogService = userActivityLogService;
    }

    @RequestMapping(value = "/mailing/{mailingId:\\d+}/heatmap/view.action", method = {RequestMethod.GET, RequestMethod.POST})
    public String view(ComAdmin admin, @PathVariable int mailingId, Model model, @ModelAttribute("form") EcsHeatmapForm form, Popups popups) {
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


        writeUserActivityLog(admin, new UserAction("view ecs", "active tab - heatmap"));
        return "ecs_view";
    }

    @RequestMapping("/mailing/{mailingId:\\d+}/heatmap/preview.action")
    public String preview(ComAdmin admin, Model model, @PathVariable int mailingId, @ModelAttribute("form") EcsHeatmapForm form, Popups popups) {
        try {
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
        } catch (Exception e) {
            popups.alert("Error");
        }

        return "messages";
    }

    @PostMapping("/mailing/{mailingId:\\d+}/heatmap/export.action")
    public Object export(ComAdmin admin, @PathVariable int mailingId, @ModelAttribute("form") EcsHeatmapForm form) {
        String mailingName = mailingBaseService.getMailingName(mailingId, admin.getCompanyID());
        String previewHeatmapUrl = getHeatmapPreviewUrl(mailingId, form);
        File file = ecsService.generatePDF(admin, previewHeatmapUrl, mailingName);

        String filename = String.format("%s_%s_%s.pdf",
                mailingName,
                I18nString.getLocaleString("ecs.Heatmap", admin.getLocale()),
                new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM).format(new Date()));
        HttpUtils.getContentDispositionAttachment(filename);

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

        // The following block is a workaround for media queries processing bug of wkhtmltopdf tool (see GWUA-1086)
        if (previewSize != null) {
            // For PDF rendering
            final String media = "print";

            try {
                Document document = HtmlUtils.parseDocument(mailingContent, StandardCharsets.UTF_8.name());

                URL base = null;
                try {
                    base = new URL(configService.getValue(ConfigValue.SystemUrl));
                } catch (MalformedURLException e) {
                    logger.error("Error occurred: " + e.getMessage(), e);
                }

                DeclarationMap declarationMap = HtmlUtils.getDeclarationMap(document, StandardCharsets.UTF_8.name(), base);
                HtmlUtils.StylesEmbeddingOptions options = HtmlUtils.stylesEmbeddingOptionsBuilder()
                        .setEncoding(StandardCharsets.UTF_8)
                        .setBaseUrl(base)
                        .setMediaType(media)
                        .setEscapeAgnTags(true)
                        .setPrettyPrint(false)
                        .build();

                mailingContent = HtmlUtils.embedStyles(document, declarationMap, options);
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }
        }

        return mailingContent;
    }

    private String getStyles(EcsPreviewSize previewSize) {
        if (previewSize == null) {
            return "";
        }

        return "<style>\n" + String.format(CSS_STYLES, previewSize.getWidth()) + "</style>\n";
    }

    protected void writeUserActivityLog(ComAdmin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }
}
