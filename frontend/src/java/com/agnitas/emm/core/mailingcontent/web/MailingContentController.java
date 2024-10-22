/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.ProfileField;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.enums.ContentGenerationTonality;
import com.agnitas.emm.core.mailingcontent.form.FrameContentForm;
import com.agnitas.emm.core.mailingcontent.form.MailingContentForm;
import com.agnitas.emm.core.mailingcontent.validator.DynTagChainValidator;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingContentService;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.MailingComponent;
import org.agnitas.emm.core.mailing.service.MailingNotExistException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.UserMessageException;
import org.agnitas.util.AgnUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static org.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.agnitas.util.Const.Mvc.MESSAGES_VIEW;

public class MailingContentController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(MailingContentController.class);

    protected final MailingService mailingService;
    protected final MailingContentService mailingContentService;
    protected final MediaTypesService mediaTypesService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final MaildropService maildropService;
    private final ComTargetService targetService;
    private final UserActivityLogService userActivityLogService;
    private final ProfileFieldDao profileFieldDao;
    private final MailingPropertiesRules mailingPropertiesRules;
    private final ComMailingBaseService mailingBaseService;
    private final GridServiceWrapper gridServiceWrapper;
    private final AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory;
    private final AgnTagService agnTagService;
    private final PreviewImageService previewImageService;
    private final DynTagChainValidator dynTagChainValidator;
    private final ExtendedConversionService extendedConversionService;

    public MailingContentController(MailinglistApprovalService mailinglistApprovalService, MailingService mailingService, MaildropService maildropService,
                                    MailingContentService mailingContentService, ComTargetService targetService,
                                    UserActivityLogService userActivityLogService, ProfileFieldDao profileFieldDao, MailingPropertiesRules mailingPropertiesRules,
                                    ComMailingBaseService mailingBaseService, GridServiceWrapper gridServiceWrapper, AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory,
                                    AgnTagService agnTagService, PreviewImageService previewImageService, DynTagChainValidator dynTagChainValidator,
                                    ExtendedConversionService extendedConversionService, MediaTypesService mediaTypesService) {
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
        this.agnDynTagGroupResolverFactory = agnDynTagGroupResolverFactory;
        this.agnTagService = agnTagService;
        this.previewImageService = previewImageService;
        this.dynTagChainValidator = dynTagChainValidator;
        this.extendedConversionService = extendedConversionService;
        this.mediaTypesService = mediaTypesService;
    }

    @GetMapping("/{mailingId:\\d+}/view.action")
    public String viewContent(@PathVariable("mailingId") int mailingId, @ModelAttribute("form") MailingContentForm form, Admin admin, Model model) {
        form.setShowHTMLEditor(true);

        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), mailingId);
        prepareListPage(mailing, form, admin, model);

        writeUserActivityLog(admin, String.format("view %s", form.isIsTemplate() ? "template" : "mailing"),
                String.format("%s (%d)", form.getShortname(), form.getMailingID()));
        writeUserActivityLog(admin, "view content", "active tab - content");

        return "mailing_content_list";
    }

    @GetMapping("/name/{id:\\d+}/view.action")
    public ResponseEntity<DynTagDto> view(Admin admin, @PathVariable("id") int dynNameId) {
        DynTagDto dto = mailingContentService.getDynTag(admin.getCompanyID(), dynNameId);

        if (dto == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/save.action")
    public @ResponseBody
    DataResponseDto<DynTagDto> save(Admin admin, HttpSession session, @RequestBody DynTagDto dynTagDto, Popups popups) {
        if (mailingService.isSettingsReadonly(admin, dynTagDto.getMailingId())) {
            popups.alert(ERROR_MSG);
            return new DataResponseDto<>(popups, false);
        }

        if (!dynTagChainValidator.validate(dynTagDto, popups, admin)) {
            return new DataResponseDto<>(popups, false);
        }

        Mailing mailing = mailingService.getMailing(admin.getCompanyID(), dynTagDto.getMailingId());

        try {
            // editing or creating
            ServiceResult<List<UserAction>> serviceResult = mailingContentService.updateDynContent(mailing, dynTagDto, admin, popups);
            if (!serviceResult.isSuccess()) {
                popups.addPopups(serviceResult);
                return new DataResponseDto<>(popups, false);
            }

            List<UserAction> userActions = serviceResult.getResult();
            userActions.forEach(action -> userActivityLogService.writeUserActivityLog(admin, action));
            logger.info(String.format("Content of mailing was changed. mailing-name : %s, mailing-id: %d",
                    mailing.getDescription(), mailing.getId()));
        } catch (UserMessageException e) {
            popups.alert(e.getErrorMessageKey(), e.getAdditionalErrorData());
            return new DataResponseDto<>(popups, false);
        } catch (Exception e) {
            logger.error(String.format("Error during building dependencies. mailing-name : %s, mailing-id: %d",
                    mailing.getDescription(), mailing.getId()), e);
            popups.alert("error.mailing.save", mailing.getShortname());
            return new DataResponseDto<>(popups, false);
        }

        previewImageService.generateMailingPreview(admin, session.getId(), mailing.getId(), true);
        DynamicTag dynamicTag = mailing.getDynTags().get(dynTagDto.getName());
        DynTagDto dynTagDtoResponse = extendedConversionService.convert(dynamicTag, DynTagDto.class);

        popups.success("default.changes_saved");
        return new DataResponseDto<>(dynTagDtoResponse, popups, true);
    }

    @PostMapping("/{mailingId:\\d+}/save.action")
    public String saveRedesigned(@PathVariable int mailingId, Admin admin, HttpSession session,
                                 @RequestBody List<DynTagDto> dynTags, Popups popups) {
        if (mailingService.isSettingsReadonly(admin, mailingId)) {
            throw new UnsupportedOperationException();
        }

        mailingContentService.saveDynTags(mailingId, dynTags, admin, popups);
        if (popups.hasAlertPopups()) {
            return MESSAGES_VIEW;
        }
        previewImageService.generateMailingPreview(admin, session.getId(), mailingId, true);
        popups.success(CHANGES_SAVED_MSG);
        return String.format("redirect:/mailing/content/%d/view.action", mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/text-from-html/confirm.action")
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
    public String generateTextFromHtml(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) {
        if (!isMailingEditable(admin, mailingId)) {
            popups.alert("status_changed");
        } else {
            if (generateTextContent(admin, mailingId)) {
                popups.success(CHANGES_SAVED_MSG);
            } else {
                popups.alert(ERROR_MSG);
            }
        }

        return String.format("redirect:/mailing/content/%d/view.action", mailingId);
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

        if (isUiRedesign(admin)) {
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        } else {
            boolean isLimitedRecipientOverview = isWorldMailingSend &&
                    !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID());

            model.addAttribute("limitedRecipientOverview", isLimitedRecipientOverview);
        }

        model.addAttribute("isWorldMailingSend", isWorldMailingSend);
        model.addAttribute("isTextGenerationEnabled", isTextGenerationEnabled);
        model.addAttribute("isMailingEditable", isMailingEditable(admin, mailingId));
        model.addAttribute("isMailingExclusiveLockingAcquired", isMailingExclusiveLockingAcquired);
        model.addAttribute("contentGenerationTonalities", ContentGenerationTonality.values());
        model.addAttribute("isContentGenerationAllowed", false);
        if (isUiRedesign(admin)) {
            model.addAttribute("mailFormat", mailing.getEmailParam().getMailFormat());
            model.addAttribute("isSettingsReadonly", mailingService.isSettingsReadonly(admin, mailing.isIsTemplate()));
            model.addAttribute("MAILING_EDITABLE", isMailingEditable(mailingId, admin));
            model.addAttribute("showDynamicTemplateToggle", mailingService.isDynamicTemplateCheckboxVisible(mailing));
            model.addAttribute("isEmailMediaTypeActive", mediaTypesService.getActiveMediaTypes(mailing).contains(MediaTypes.EMAIL));
            model.addAttribute("frameContentForm", getFrameContentForm(mailing));
        }
    }

    private boolean isMailingEditable(int mailingId, Admin admin) {
        return !maildropService.isActiveMailing(mailingId, admin.getCompanyID());
    }

    protected boolean isUiRedesign(Admin admin) {
        return admin.isRedesignedUiUsed();
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
        loadDynTags(mailing, form);
        if (isUiRedesign(admin)) {
            loadTargets(admin, model);
        } else {
            loadTargetGroups(form, admin);
        }
        loadAvailableInterestGroups(form, admin);
    }

    private void loadTargets(Admin admin, Model model) {
        boolean showContentBlockTargetGroupsOnly = !admin.permissionAllowed(Permission.MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS);
        model.addAttribute("targets", targetService.getTargetLights(admin, true, false, showContentBlockTargetGroupsOnly));
    }

    private void loadDynTags(Mailing mailing, MailingContentForm form) {
        Map<String, DynamicTag> tags = mailing.getDynTags();

        // Grid mailing should not expose its internals (dynamic tags representing building blocks) for editing.
        if (form.getGridTemplateId() > 0) {
            clearBuildingBlocksFromTags(mailing, tags);
        }

        Map<String, DynTagDto> dynTags = new HashMap<>();

        for (Map.Entry<String, DynamicTag> tagEntry : tags.entrySet()) {
            DynamicTag dynTag = tagEntry.getValue();
            DynTagDto dynTagDto = extendedConversionService.convert(dynTag, DynTagDto.class);

            dynTags.put(tagEntry.getKey(), dynTagDto);
        }

        form.setTags(dynTags, true);
    }

    private void clearBuildingBlocksFromTags(Mailing mailing, Map<String, DynamicTag> dynTags) {
        MailingComponent htmlComponent = mailing.getComponents().get("agnHtml");
        if (htmlComponent != null) {
            for (String name : getAgnTags(htmlComponent.getEmmBlock(), mailing.getCompanyID())) {
                dynTags.remove(name);
                findTocItemDynNames(name, dynTags.keySet()).forEach(dynTags::remove);
            }
        }
    }

    private List<String> findTocItemDynNames(String dynName, Collection<String> dynNames) {
        return dynNames.stream()
                .filter(n -> n.startsWith(dynName + AgnUtils.TOC_ITEM_SUFFIX))
                .collect(Collectors.toList());
    }

    protected boolean isMailingEditable(Admin admin, int mailingId) {
        return !mailingService.isSettingsReadonly(admin, mailingId)
                && mailingPropertiesRules.isMailingContentEditable(mailingId, admin);
    }

    private void loadTargetGroups(MailingContentForm form, Admin admin) {
        boolean showContentBlockTargetGroupsOnly = !admin.permissionAllowed(Permission.MAILING_CONTENT_SHOW_EXCLUDED_TARGETGROUPS);
        List<TargetLight> list = targetService.getTargetLights(admin,  true, false, showContentBlockTargetGroupsOnly);

        form.setAvailableTargetGroups(list);
    }

    private void loadAvailableInterestGroups(MailingContentForm form, Admin admin) {
        List<ProfileField> availableInterestFields = Collections.emptyList();
        try {
            availableInterestFields = profileFieldDao.getProfileFieldsWithInterest(admin.getCompanyID(), admin.getAdminID());
        } catch (Exception e) {
            logger.error(String.format("Error occurred: %s", e.getMessage()), e);
        }
        form.setAvailableInterestGroups(availableInterestFields);
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

    private List<String> getAgnTags(String content, int companyId) {
        return agnTagService.getDynTagsNames(content, agnDynTagGroupResolverFactory.create(companyId, 0));
    }

    protected void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, action, description, logger);
    }
}
