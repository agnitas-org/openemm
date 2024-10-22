/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UpdateMailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.exception.AttachmentDownloadException;
import com.agnitas.emm.core.components.form.UpdateMailingAttachmentsForm;
import com.agnitas.emm.core.components.form.UploadMailingAttachmentForm;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.FileResponseBody;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.MvcUtils;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.SimpleActionForm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.agnitas.emm.core.Permission.MAILING_CONTENT_CHANGE_ALWAYS;
import static org.agnitas.util.Const.Mvc.DELETE_VIEW;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static org.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;

@Controller
@RequestMapping("/mailing")
@PermissionMapping("mailing.attachment")
public class MailingAttachmentController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(MailingAttachmentController.class);

    private final ComTargetService targetService;
    private final MailingService mailingService;
    private final ComMailingBaseService mailingBaseService;
    private final ComMailingComponentsService mailingComponentsService;
    private final MaildropService maildropService;
    private final MailingPropertiesRules mailingPropertiesRules;
    private final GridServiceWrapper gridServiceWrapper;
    private final UserActivityLogService userActivityLogService;
    private final ExtendedConversionService conversionService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final WebStorage webStorage;

    public MailingAttachmentController(ComTargetService targetService,
                                       MailingService mailingService,
                                       ComMailingBaseService mailingBaseService,
                                       ComMailingComponentsService mailingComponentsService,
                                       MaildropService maildropService,
                                       MailingPropertiesRules mailingPropertiesRules,
                                       GridServiceWrapper gridServiceWrapper,
                                       UserActivityLogService userActivityLogService,
                                       ExtendedConversionService conversionService,
                                       MailinglistApprovalService mailinglistApprovalService,
                                       WebStorage webStorage) {
        this.targetService = targetService;
        this.mailingService = mailingService;
        this.mailingBaseService = mailingBaseService;
        this.mailingComponentsService = mailingComponentsService;
        this.maildropService = maildropService;
        this.mailingPropertiesRules = mailingPropertiesRules;
        this.gridServiceWrapper = gridServiceWrapper;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.webStorage = webStorage;
    }

    @ExceptionHandler(AttachmentDownloadException.class)
    public String onAttachmentDownloadException(AttachmentDownloadException ex, Popups popups) {
        ex.getErrors().forEach(popups::alert);
        return redirectToList(ex.getMailingId());
    }

    @RequestMapping("/{mailingId:\\d+}/attachment/list.action")
    public String list(Admin admin, @PathVariable int mailingId,
                       @ModelAttribute("form") UpdateMailingAttachmentsForm form,
                       @ModelAttribute UploadMailingAttachmentForm uploadMailingAttachmentForm,
                       @RequestParam(required = false) boolean restoreSort,
                       Model model) {
        int companyId = admin.getCompanyID();

        FormUtils.syncPaginationData(webStorage, WebStorage.MAILING_ATTACHMENTS_OVERVIEW, form, restoreSort);

        PaginatedListImpl<MailingAttachmentDto> attachments = mailingComponentsService.getAttachmentsOverview(form, mailingId, companyId);
        form.setAttachments(attachments.getList());

        Mailing mailing = mailingBaseService.getMailing(companyId, mailingId);
        boolean isActiveMailing = maildropService.isActiveMailing(mailingId, companyId);

        model.addAttribute("attachments", attachments);
        model.addAttribute("pdfUploads", mailingComponentsService.getUploadsByExtension(admin));
        model.addAttribute("mailing", mailing);
        model.addAttribute("isMailingEditable", !mailingService.isSettingsReadonly(admin, mailing.isIsTemplate()) && !isActiveMailing);
        model.addAttribute("gridTemplateId", gridServiceWrapper.getGridTemplateIdByMailingId(mailingId));
        model.addAttribute("workflowId", mailingBaseService.getWorkflowId(mailingId, companyId));
        model.addAttribute("isMailingUndoAvailable", mailingBaseService.checkUndoAvailable(mailingId));
        model.addAttribute("targetGroups", targetService.getTargetLights(admin));
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("isActiveMailing", isActiveMailing);
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        } else {
            model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailingId));
        }

        writeUserActivityLog(admin, new UserAction("attachments list", "active tab - attachments"));

        return "mailing_attachments";
    }

    @PostMapping(value = "/{mailingId:\\d+}/attachment/upload.action", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(Admin admin, @PathVariable int mailingId, @ModelAttribute UploadMailingAttachmentForm form, Popups popups) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new UnsupportedOperationException();
        }

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
            popups.alert(ERROR_MSG);
        }

        return MESSAGES_VIEW;
    }

    private String redirectToList(int mailingId) {
        return String.format("redirect:/mailing/%d/attachment/list.action?restoreSort=true", mailingId);
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
            if (isSettingsReadonly(admin, mailingId)) {
                throw new UnsupportedOperationException();
            }

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
            popups.alert(ERROR_MSG);
        }

        return MESSAGES_VIEW;
    }

    @GetMapping("/{mailingId:\\d+}/attachment/{id:\\d+}/confirmDelete.action")
    // TODO: EMMGUI-714: remove when old design will be removed
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

        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

    @RequestMapping(value = "/{mailingId:\\d+}/attachment/delete.action", method = { RequestMethod.POST, RequestMethod.DELETE})
    // TODO: EMMGUI-714: remove when old design will be removed
    public String delete(Admin admin, @PathVariable int mailingId, SimpleActionForm form, Popups popups) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new UnsupportedOperationException();
        }
        int id = form.getId();
        String name = form.getShortname();
        try {
            mailingComponentsService.deleteComponent(admin.getCompanyID(), mailingId, id);
            writeUserActivityLog(admin, "delete attachment",
                    String.format("%s (ID: %d) from mailing ID: %d", name, id, mailingId));
            popups.success("default.selection.deleted");
            return redirectToList(mailingId);
        } catch (Exception e) {
            logger.error("Mailing attachment ID: {} deletion failed", id, e);
        }
        popups.alert(ERROR_MSG);
        return MESSAGES_VIEW;
    }

    @GetMapping(value = "/{mailingId:\\d+}/attachment/deleteRedesigned.action")
    @PermissionMapping("confirmDelete")
    public String confirmDeleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, @PathVariable int mailingId, Admin admin, Model model) {
        validateSelectedIds(bulkIds);

        List<String> names = mailingComponentsService.getNames(bulkIds, mailingId, admin);
        if (names.isEmpty()) {
            throw new RequestErrorException(ERROR_MSG);
        }

        MvcUtils.addDeleteAttrs(model, names,
                "mailing.attachment.delete", "mailing.attachment.delete.question",
                "bulkAction.delete.mailing.attachment", "bulkAction.delete.mailing.attachment.question");
        return DELETE_VIEW;
    }

    @RequestMapping(value = "/{mailingId:\\d+}/attachment/deleteRedesigned.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    @PermissionMapping("delete")
    public String deleteRedesigned(@RequestParam(required = false) Set<Integer> bulkIds, @PathVariable int mailingId, Admin admin, Popups popups) {
        if (isSettingsReadonly(admin, mailingId)) {
            throw new UnsupportedOperationException();
        }
        validateSelectedIds(bulkIds);
        ServiceResult<UserAction> result = mailingComponentsService.delete(bulkIds, mailingId, admin);

        popups.addPopups(result);
        userActivityLogService.writeUserActivityLog(admin, result.getResult());

        return redirectToList(mailingId);
    }

    @GetMapping(value = "/{mailingId:\\d+}/attachment/bulk/download.action")
    public ResponseEntity<StreamingResponseBody> bulkDownload(@RequestParam(required = false) Set<Integer> bulkIds, @PathVariable int mailingId, Admin admin, Popups popups) {
        File zip = mailingComponentsService.getZipToDownload(bulkIds, mailingId, admin);
        writeUserActivityLog(admin, "download attachments", String.format("downloaded %d attachment(s) as ZIP archive", bulkIds.size()));

        String downloadFileName = String.format("mailing_attachments_%s.zip", admin.getDateFormat().format(new Date()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", downloadFileName))
                .contentLength(zip.length())
                .contentType(MediaType.parseMediaType(MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .body(new FileResponseBody(zip, true));
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    private boolean isSettingsReadonly(Admin admin, int mailingId) {
        return mailingService.isSettingsReadonly(admin, mailingId);
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
