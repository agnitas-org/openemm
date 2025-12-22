/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.exception.MailingNotExistException;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.enums.ContentGenerationTonality;
import com.agnitas.emm.core.mailingcontent.form.FrameContentForm;
import com.agnitas.emm.core.mailingcontent.form.MailingContentForm;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.target.beans.TargetGroupDeliveryOption;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingContentService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.NotAllowedActionException;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class MailingContentController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingContentController.class);

    protected final MailingService mailingService;
    protected final MailingContentService mailingContentService;
    protected final MediaTypesService mediaTypesService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final MaildropService maildropService;
    private final TargetService targetService;
    private final UserActivityLogService userActivityLogService;
    private final ProfileFieldDao profileFieldDao;
    private final MailingPropertiesRules mailingPropertiesRules;
    private final MailingBaseService mailingBaseService;
    private final GridServiceWrapper gridServiceWrapper;
    private final PreviewImageService previewImageService;

    public MailingContentController(MailinglistApprovalService mailinglistApprovalService, MailingService mailingService, MaildropService maildropService,
                                    MailingContentService mailingContentService, TargetService targetService,
                                    UserActivityLogService userActivityLogService, ProfileFieldDao profileFieldDao, MailingPropertiesRules mailingPropertiesRules,
                                    MailingBaseService mailingBaseService, GridServiceWrapper gridServiceWrapper,
                                    PreviewImageService previewImageService, MediaTypesService mediaTypesService) {
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.mailingService = mailingService;
        this.maildropService = maildropService;
        this.mailingContentService = mailingContentService;
        this.targetService = targetService;
        this.userActivityLogService = userActivityLogService;
        this.profileFieldDao = profileFieldDao;
        this.mailingPropertiesRules = mailingPropertiesRules;
        this.mailingBaseService = mailingBaseService;
        this.gridServiceWrapper = gridServiceWrapper;
        this.previewImageService = previewImageService;
        this.mediaTypesService = mediaTypesService;
    }

    @GetMapping("/{mailingId:\\d+}/view.action")
    @RequiredPermission("mailing.content.show")
    public String viewContent(@PathVariable("mailingId") int mailingId, @ModelAttribute("form") MailingContentForm form, Admin admin, Model model) {
        form.setShowHTMLEditor(true);

        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        prepareListPage(mailing, form, admin, model);

        writeUserActivityLog(admin, String.format("view %s", form.isIsTemplate() ? "template" : "mailing"),
                String.format("%s (%d)", form.getShortname(), form.getMailingID()));
        writeUserActivityLog(admin, "view content", "active tab - content");

        return "mailing_content";
    }

    @GetMapping("/name/{id:\\d+}/view.action")
    @RequiredPermission("mailing.content.show")
    public ResponseEntity<DynTagDto> view(Admin admin, @PathVariable("id") int dynNameId) {
        DynTagDto dto = mailingContentService.getDynTag(admin.getCompanyID(), dynNameId);

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{mailingId:\\d+}/save.action")
    @RequiredPermission("mailing.content.show")
    public ResponseEntity<Popups> save(@PathVariable int mailingId, Admin admin, HttpSession session,
                       @RequestBody List<DynTagDto> dynTags, Popups popups) {
        if (mailingService.isSettingsReadonly(admin, mailingId)) {
            throw new NotAllowedActionException();
        }
        mailingContentService.saveDynTags(mailingId, dynTags, admin, popups);
        if (!popups.hasAlertPopups()) {
            previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
            popups.changesSaved();
        }
        return ResponseEntity.ok(popups);
    }

    private String redirectToView(int mailingId) {
        return "redirect:/mailing/content/%d/view.action".formatted(mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/text-from-html/confirm.action")
    @RequiredPermission("mailing.content.show")
    public String confirmGenerateTextFromHtml(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups, Model model) {
        if (!isMailingEditable(admin, mailingId)) {
            popups.alert("status_changed");
            return MESSAGES_VIEW;
        }

        model.addAttribute("mailingId", mailingId);
        model.addAttribute("mailingShortname", mailingService.getMailingName(mailingId, admin.getCompanyID()));

        return "generate_text_question_ajax";
    }

    @PostMapping("/{mailingId:\\d+}/text-from-html/generate.action")
    @RequiredPermission("mailing.content.show")
    public String generateTextFromHtml(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) {
        if (!isMailingEditable(admin, mailingId)) {
            popups.alert("status_changed");
        } else {
            if (generateTextContent(admin, mailingId)) {
                popups.changesSaved();
            } else {
                popups.defaultError();
            }
        }

        return redirectToView(mailingId);
    }

    private boolean generateTextContent(Admin admin, int mailingId) {
        try {
            return mailingService.generateMailingTextContentFromHtml(admin, mailingId);
        } catch (Exception e) {
            logger.error(String.format("Error occurred: %s", e.getMessage()), e);
        }

        return false;
    }

    private void prepareListPage(Mailing mailing, MailingContentForm form, Admin admin, Model model) {
        prepareForm(mailing, form, admin);
        loadAdditionalData(mailing, form, admin, model);

        prepareModelAttributes(mailing, admin, model, form);
    }

    protected void prepareModelAttributes(Mailing mailing, Admin admin, Model model, MailingContentForm form) {
        int mailingId = mailing.getId();

        boolean isMailingExclusiveLockingAcquired = tryToLock(mailingId, admin, model);
        boolean isTextGenerationEnabled = !mailing.isIsTemplate() && mailingContentService.isGenerationAvailable(mailing);
        boolean isWorldMailingSend = maildropService.isActiveMailing(mailingId, admin.getCompanyID());

        model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        model.addAttribute("isWorldMailingSend", isWorldMailingSend);
        model.addAttribute("isTextGenerationEnabled", isTextGenerationEnabled);
        model.addAttribute("isMailingEditable", isMailingEditable(admin, mailingId));
        model.addAttribute("isMailingExclusiveLockingAcquired", isMailingExclusiveLockingAcquired);
        model.addAttribute("contentGenerationTonalities", ContentGenerationTonality.values());
        model.addAttribute("isContentGenerationAllowed", false);
        model.addAttribute("mailFormat", mailing.getEmailParam().getMailFormat());
        model.addAttribute("isSettingsReadonly", mailingService.isSettingsReadonly(admin, mailing.isIsTemplate()));
        model.addAttribute("MAILING_EDITABLE", isMailingEditable(mailingId, admin));
        model.addAttribute("showDynamicTemplateToggle", mailingService.isDynamicTemplateCheckboxVisible(mailing));
        model.addAttribute("isEmailMediaTypeActive", mediaTypesService.getActiveMediaTypes(mailing).contains(MediaTypes.EMAIL));
        model.addAttribute("frameContentForm", getFrameContentForm(mailing));
    }

    private boolean isMailingEditable(int mailingId, Admin admin) {
        return !maildropService.isActiveMailing(mailingId, admin.getCompanyID());
    }

    private void prepareForm(Mailing mailing, MailingContentForm form, Admin admin) {
        int mailingId = mailing.getId();

        form.setMailingID(mailingId);
        form.setShortname(mailing.getShortname());
        form.setIsTemplate(mailing.isIsTemplate());

        form.setDynTagNames(mailingBaseService.getDynamicTagNames(mailing));
        form.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailingId));
        form.setGridTemplateId(gridServiceWrapper.getGridTemplateIdByMailingId(mailingId));
        form.setWorkflowId(mailingBaseService.getWorkflowId(mailingId, admin.getCompanyID()));
    }

    protected FrameContentForm getFrameContentForm(Mailing mailing) {
        FrameContentForm form = new FrameContentForm();
        MailingComponent comp;
        
        comp = mailing.getTemplate(MediaTypes.EMAIL.getKey());
        form.setTextTemplate(comp != null ? comp.getEmmBlock() : "");
        comp = mailing.getHtmlTemplate();
        form.setHtmlTemplate(comp != null ? comp.getEmmBlock() : "");
        form.setUseDynamicTemplate(mailing.getUseDynamicTemplate());
        return form;
    }

    protected void loadAdditionalData(Mailing mailing, MailingContentForm form, Admin admin, Model model) {
        form.setTags(mailingContentService.loadDynTags(mailing), true);
        loadTargets(admin, model);
        loadAvailableInterestGroups(form, admin);
    }

    private void loadTargets(Admin admin, Model model) {
        boolean showContentBlockTargetGroupsOnly = !admin.permissionAllowed(Permission.MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS);
        model.addAttribute("targets", targetService.getTargetLights(admin, showContentBlockTargetGroupsOnly, TargetGroupDeliveryOption.FINAL));
    }

    protected boolean isMailingEditable(Admin admin, int mailingId) {
        return !mailingService.isSettingsReadonly(admin, mailingId)
                && mailingPropertiesRules.isMailingContentEditable(mailingId, admin);
    }

    private void loadAvailableInterestGroups(MailingContentForm form, Admin admin) {
        form.setAvailableInterestGroups(profileFieldDao.getProfileFieldsWithInterest(admin.getCompanyID(), admin.getAdminID()));
    }

    private boolean tryToLock(int mailingId, Admin admin, Model model) {
        try {
            boolean lockedByCurrentUser = mailingService.tryToLock(admin, mailingId);
            if (!lockedByCurrentUser) {
                Admin lockingUser = mailingService.getMailingLockingAdmin(mailingId, admin.getCompanyID());
                if (lockingUser != null) {
                    model.addAttribute("anotherLockingUserName", String.join(" ", lockingUser.getFirstName(), lockingUser.getFullname()));
                }
            }

            return lockedByCurrentUser;
        } catch (MailingNotExistException e) {
            // New mailing is always edited exclusively.
            return true;
        }
    }

    protected void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description, logger);
    }

}
