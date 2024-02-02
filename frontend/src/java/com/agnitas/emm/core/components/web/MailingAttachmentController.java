/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UpdateMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.form.UpdateMailingAttachmentsForm;
import com.agnitas.emm.core.components.form.UploadMailingAttachmentForm;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.upload.bean.UploadData;
import com.agnitas.emm.core.upload.bean.UploadFileExtension;
import com.agnitas.emm.core.upload.service.UploadService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.Permission.MAILING_CONTENT_CHANGE_ALWAYS;

@Controller
@RequestMapping("/mailing")
@PermissionMapping("mailing.attachment")
public class MailingAttachmentController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(MailingAttachmentController.class);

    private static final String MESSAGES_VIEW = "messages";
    private static final String ERROR_MSG_KEY = "Error";

    private final ComTargetService targetService;
    private final ComMailingBaseService mailingBaseService;
    private final ComMailingComponentsService mailingComponentsService;
    private final MaildropService maildropService;
    private final MailingPropertiesRules mailingPropertiesRules;
    private final UploadService uploadService;
    private final GridServiceWrapper gridServiceWrapper;
    private final UserActivityLogService userActivityLogService;
    private final ExtendedConversionService conversionService;

    public MailingAttachmentController(ComTargetService targetService,
                                       ComMailingBaseService mailingBaseService,
                                       ComMailingComponentsService mailingComponentsService,
                                       MaildropService maildropService,
                                       MailingPropertiesRules mailingPropertiesRules, UploadService uploadService,
                                       GridServiceWrapper gridServiceWrapper,
                                       UserActivityLogService userActivityLogService,
                                       ExtendedConversionService conversionService) {
        this.targetService = targetService;
        this.mailingBaseService = mailingBaseService;
        this.mailingComponentsService = mailingComponentsService;
        this.maildropService = maildropService;
        this.mailingPropertiesRules = mailingPropertiesRules;
        this.uploadService = uploadService;
        this.gridServiceWrapper = gridServiceWrapper;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
    }

    @GetMapping("/{mailingId:\\d+}/attachment/list.action")
    public String list(Admin admin, @PathVariable int mailingId,
                       @ModelAttribute("form") UpdateMailingAttachmentsForm form,
                       @ModelAttribute UploadMailingAttachmentForm uploadMailingAttachmentForm,
                       Model model) {
        int companyId = admin.getCompanyID();

        List<MailingComponent> attachments = mailingComponentsService.getPreviewHeaderComponents(companyId, mailingId);
        form.setAttachments(conversionService.convert(attachments, MailingComponent.class, MailingAttachmentDto.class));

        List<UploadData> pdfUploads = uploadService.getUploadsByExtension(admin, UploadFileExtension.PDF);
        model.addAttribute("pdfUploads", pdfUploads);

        model.addAttribute("mailing", mailingBaseService.getMailing(companyId, mailingId));
        model.addAttribute("isMailingEditable", !maildropService.isActiveMailing(mailingId, companyId));
        model.addAttribute("gridTemplateId", gridServiceWrapper.getGridTemplateIdByMailingId(mailingId));
        model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, companyId));
        model.addAttribute("isMailingUndoAvailable", mailingBaseService.checkUndoAvailable(mailingId));
        model.addAttribute("targetGroups", targetService.getTargetLights(admin));
        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingId));

        writeUserActivityLog(admin, new UserAction("attachments list", "active tab - attachments"));

        return "mailing_attachments";
    }

    @PostMapping(value = "/{mailingId:\\d+}/attachment/upload.action", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(Admin admin, @PathVariable int mailingId, @ModelAttribute UploadMailingAttachmentForm form, Popups popups) {
        try {
            if (mailingEditable(admin, mailingId)) {
                UploadMailingAttachmentDto attachment = conversionService.convert(form, UploadMailingAttachmentDto.class);
                SimpleServiceResult result = mailingComponentsService.uploadMailingAttachment(admin, mailingId, attachment);

                popups.addPopups(result);

                if (result.isSuccess()) {
                    return redirectToList(mailingId);
                }
            } else {
                popups.alert("status_changed");
            }
        } catch (Exception e) {
            logger.error("Uploading attachment failed: ", e);
            popups.alert(ERROR_MSG_KEY);
        }

        return MESSAGES_VIEW;
    }

    private String redirectToList(@PathVariable int mailingId) {
        return String.format("redirect:/mailing/%d/attachment/list.action", mailingId);
    }

    private boolean mailingEditable(Admin admin, int mailingId) {
        if (admin.permissionAllowed(MAILING_CONTENT_CHANGE_ALWAYS)) {
            return true;
        }

        return !mailingPropertiesRules.mailingIsWorldSentOrActive(mailingId, admin.getCompanyID());
    }

    @PostMapping("/{mailingId:\\d+}/attachment/save.action")
    public String save(Admin admin, @PathVariable int mailingId, @ModelAttribute("form") UpdateMailingAttachmentsForm form, Popups popups) {
        try {
            if (mailingEditable(admin, mailingId)) {
                SimpleServiceResult result = mailingComponentsService.updateMailingAttachments(admin, mailingId, convertUpdateMailingsData(form.getAttachments()));

                popups.addPopups(result);

                if (result.isSuccess()) {
                    return redirectToList(mailingId);
                }
            } else {
                popups.alert("status_changed");
            }
        } catch (Exception e) {
            logger.error("Uploading attachment failed: ", e);
            popups.alert(ERROR_MSG_KEY);
        }

        return MESSAGES_VIEW;
    }

    @GetMapping("/{mailingId:\\d+}/attachment/{id:\\d+}/confirmDelete.action")
    public String confirmDelete(Admin admin, @PathVariable int mailingId, @PathVariable int id,
                                @ModelAttribute("simpleActionForm") SimpleActionForm form,
                                Model model, Popups popups) {
        MailingComponent attachment = mailingComponentsService.getComponent(admin.getCompanyID(), mailingId, id);
        if (attachment != null) {
            form.setId(attachment.getId());
            form.setShortname(attachment.getComponentName());

            model.addAttribute("mailingId", mailingId);

            return "mailing_attachments_delete_ajax";
        }

        popups.alert(ERROR_MSG_KEY);
        return MESSAGES_VIEW;
    }

    @RequestMapping(value = "/{mailingId:\\d+}/attachment/delete.action", method = { RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, @PathVariable int mailingId, SimpleActionForm form, Popups popups) {
        try {
            mailingComponentsService.deleteComponent(admin.getCompanyID(), mailingId, form.getId());
            writeUserActivityLog(admin, "delete attachment",
                    String.format("%s (ID: %d) from mailing ID: %d", form.getShortname(), form.getId(), mailingId));
            popups.success("default.selection.deleted");
            return redirectToList(mailingId);
        } catch (Exception e) {
            logger.error("Mailing attachment ID: {} deletion failed", form.getId(), e);
        }
        popups.alert(ERROR_MSG_KEY);
        return MESSAGES_VIEW;
    }

    private Map<Integer, UpdateMailingAttachmentDto> convertUpdateMailingsData(List<MailingAttachmentDto> attachments) {
        return attachments.stream()
                .map(attachment -> {
                    UpdateMailingAttachmentDto component = new UpdateMailingAttachmentDto();
                    component.setId(attachment.getId());
                    component.setTargetId(attachment.getTargetId());
                    return component;
                })
                .collect(Collectors.toMap(UpdateMailingAttachmentDto::getId, Function.identity()));
    }

    private void writeUserActivityLog(Admin admin, String action, String description) {
        writeUserActivityLog(admin, new UserAction(action, description));
    }
    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in {}", this.getClass().getSimpleName());
            logger.info("Userlog: {} {} {}", admin.getUsername(), userAction.getAction(), userAction.getDescription());
        }
    }
}
