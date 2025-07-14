/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import static com.agnitas.emm.core.mailing.dao.MailingParameterDao.ReservedMailingParam.isReservedParam;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MediaTypeStatus;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.TooManyTargetGroupsInMailingException;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.bean.impl.MailingValidator;
import com.agnitas.emm.core.mailing.forms.MailingSettingsForm;
import com.agnitas.emm.core.mailing.forms.mediatype.EmailMediatypeForm;
import com.agnitas.emm.core.mailing.forms.mediatype.MediatypeForm;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSettingsService;
import com.agnitas.emm.core.mailing.web.MailingSettingsOptions;
import com.agnitas.emm.core.mailingcontent.form.FrameContentForm;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.target.TargetExpressionUtils;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParameters;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.UserMessageException;
import com.agnitas.util.MissingEndTagException;
import com.agnitas.util.SafeString;
import com.agnitas.util.UnclosedTagException;
import com.agnitas.util.UserActivityUtil;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.mvc.Popups;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service("mailingSettingsService")
public class MailingSettingsServiceImpl implements MailingSettingsService {

    private static final Logger LOGGER = LogManager.getLogger(MailingSettingsServiceImpl.class);
    
    private final MailingService mailingService;
    private final MailingBaseService mailingBaseService;
    private final MaildropService maildropService;
    private final ApplicationContext applicationContext;
    private final DynamicTagDao dynamicTagDao;
    private final MailingParameterService mailingParameterService;
    private final UserActivityLogService userActivityLogService;
    private final MailingValidator mailingValidator;
    protected final GridServiceWrapper gridService;
    protected final WorkflowService workflowService;
    protected final ExtendedConversionService conversionService;
    protected final TargetService targetService;
    private final PreviewImageService previewImageService;

    public MailingSettingsServiceImpl(MailingService mailingService, MailingBaseService mailingBaseService, MaildropService maildropService, ApplicationContext applicationContext, DynamicTagDao dynamicTagDao, MailingParameterService mailingParameterService, UserActivityLogService userActivityLogService, MailingValidator mailingValidator, GridServiceWrapper gridService, WorkflowService workflowService, ExtendedConversionService conversionService, TargetService targetService, PreviewImageService previewImageService) {
        this.mailingService = mailingService;
        this.mailingBaseService = mailingBaseService;
        this.maildropService = maildropService;
        this.applicationContext = applicationContext;
        this.dynamicTagDao = dynamicTagDao;
        this.mailingParameterService = mailingParameterService;
        this.userActivityLogService = userActivityLogService;
        this.mailingValidator = mailingValidator;
        this.gridService = gridService;
        this.workflowService = workflowService;
        this.conversionService = conversionService;
        this.targetService = targetService;
        this.previewImageService = previewImageService;
    }

    @Override
    public boolean saveFrameContent(Mailing mailing, FrameContentForm form, Admin admin, String sessionId, Popups popups) {
        if (maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
            popups.alert(ERROR_MSG);
            return false;
        }
        return trySetFrameContent(mailing, form, admin, popups) && trySaveFrameContent(mailing, admin, sessionId, popups);
    }


    private boolean trySetFrameContent(Mailing mailing, FrameContentForm form, Admin admin, Popups popups) {
        return handleMailingPrepareExceptions(mailing.getId(), popups, () -> {
            setFrameContentFromForm(form, mailing);
            syncMediatypeTemplates(mailing);
            List<String> dynNamesForDeletion = new ArrayList<>();
            mailing.buildDependencies(popups, true, dynNamesForDeletion, applicationContext, admin);
            dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);
        });
    }

    protected void setFrameContentFromForm(FrameContentForm form, Mailing mailing) {
        mailing.getEmailParam().setTemplate(form.getTextTemplate());
        mailing.getEmailParam().setHtmlTemplate(form.getHtmlTemplate());
        mailing.setUseDynamicTemplate(form.isUseDynamicTemplate());
    }
    
    private void syncMediatypeTemplates(Mailing mailing) {
        for (Mediatype mediatype : mailing.getMediatypes().values()) {
            if (mediatype != null && mediatype.getStatus() == MediaTypeStatus.Active.getCode()) {
                mediatype.syncTemplate(mailing, applicationContext);
            }
        }
    }

    private boolean trySaveFrameContent(Mailing mailing, Admin admin, String sessionId, Popups popups) {
        int mailingId = mailing.getId();
        return handleSaveExceptions(mailingId, popups, () -> {
            mailingValidator.validateMailingBeforeSave(mailing, admin.getLocale(), popups);
            mailingBaseService.saveUndoData(mailing.getId(), admin.getAdminID());
            mailingService.saveMailingWithNewContent(mailing, admin);
            if (mailing.isIsTemplate()) {
                mailingService.updateMailingsWithDynamicTemplate(mailing, applicationContext);
            }
            previewImageService.generateMailingPreview(admin, sessionId, mailingId, true);
        });
    }

    @Override
    public boolean saveSettings(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions opts, Popups popups) {
        return tryPrepareSettingsForSave(mailing, form, admin, opts, popups)
                && trySaveSettings(mailing, form, admin, opts, popups);
    }

    private boolean tryPrepareSettingsForSave(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options, Popups popups) {
        return handleMailingPrepareExceptions(mailing.getId(), popups,
                () -> prepareMailingForSave(mailing, form, options, admin, popups));
    }

    private boolean trySaveSettings(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options, Popups popups) {
        return handleSaveExceptions(mailing.getId(), popups, () -> {
            if (options.isActiveOrSent()) {
                mailingService.saveMailingDescriptiveData(mailing);
            } else {
                saveMailing(mailing, form, admin, options, popups);
                previewImageService.generateMailingPreview(admin, options.getSessionId(), mailing.getId(), true);
            }
        });
    }

    private void prepareMailingForSave(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, Admin admin, Popups popups) throws Exception {
        if (!options.isActiveOrSent()) {
            setMailingPropertiesFromForm(mailing, form, admin, options);

            mailing.setParameters(collectMailingParams(form, admin, options));
            List<String> dynNamesForDeletion = new ArrayList<>();
            mailing.buildDependencies(popups, true, dynNamesForDeletion, applicationContext, admin);
            dynamicTagDao.markNamesAsDeleted(mailing.getId(), dynNamesForDeletion);
            if (form.getEmailMediatype().isActive()) {
                mailingBaseService.doTextTemplateFilling(mailing, admin, popups);
            }
        } else if (isFormContainsOnlyAlwaysAllowedChanges(mailing, form, options, popups)) {
            String shortname = form.getShortname();
            String description = form.getDescription();
            int archiveId = form.getArchiveId();
            boolean isArchived = form.isArchived();

            form = mailingToForm(mailing, admin);
            form.setShortname(shortname);
            form.setDescription(description);
            form.setArchiveId(archiveId);
            form.setArchived(isArchived);
            setMailingPropertiesFromForm(mailing, form, admin, options);
        }
    }

    @Override
    public MailingSettingsForm mailingToForm(Mailing mailing, Admin admin) {
        MailingSettingsForm form = getNewSettingsForm();
        form.setShortname(mailing.getShortname());
        form.setDescription(mailing.getDescription());
        form.setMailingContentType(mailing.getMailingContentType());
        form.setMailingType(mailing.getMailingType());
        form.setMailinglistId(mailing.getMailinglistID());
        form.setArchiveId(mailing.getCampaignID());
        form.setArchived(mailing.getArchived() != 0);
        setMailingTargetsToForm(form, mailing);
        form.setNeedsTarget(mailing.getNeedsTarget());
        form.setUseDynamicTemplate(mailing.getUseDynamicTemplate());
        setMediatypesToForm(mailing, form);
        setMailingParamsToForm(form, mailing.getId(), admin.getCompanyID());
        if (mailing.getPlanDate() != null) {
            form.setPlanDate(definePlanDateFormat(mailing.getPlanDate(), admin).format(mailing.getPlanDate()));
        } else {
            form.setPlanDate("");
        }
        return form;
    }

    private static SimpleDateFormat definePlanDateFormat(Date planDate, Admin admin) {
        return new SimpleDateFormat("HH:mm:ss").format(planDate).equals("00:00:00") // time was not set
                ? admin.getDateFormat()
                : admin.getDateTimeFormat();
    }

    protected MailingSettingsForm getNewSettingsForm() {
        return new MailingSettingsForm();
    }

    private void setMailingParamsToForm(MailingSettingsForm form, int mailingId, int companyId) {
        List<MailingParameter> params = mailingParameterService.getMailingParameters(companyId, mailingId);
        form.setParams(params.stream()
                .filter(param -> !isReservedParam(param.getName()))
                .collect(Collectors.toList()));
    }

    protected void setMailingPropertiesFromForm(Mailing mailing, MailingSettingsForm form, Admin admin,
                                                MailingSettingsOptions options) throws Exception {
        mailing.setSplitID(options.getNewSplitId());
        mailing.setIsTemplate(options.isTemplate());
        mailing.setCampaignID(form.getArchiveId());
        mailing.setDescription(form.getDescription());
        mailing.setShortname(form.getShortname());
        mailing.setMailinglistID(form.getMailinglistId());
        mailing.setMailingType(form.getMailingType());
        mailing.setPlanDate(getMailingPlanDateFromForm(form, admin));
        mailing.setArchived(form.isArchived() ? 1 : 0);
        if (!workflowDriven(options.getWorkflowId())) {
            mailing.setTargetExpression(generateMailingTargetExpression(mailing, form, admin, options));
        }
        mailing.setMailingContentType(form.getMailingContentType());
        mailing.setLocked(1);
        mailing.setNeedsTarget(form.isNeedsTarget());
        mailing.setUseDynamicTemplate(form.isUseDynamicTemplate());
        setMediatypesToMailing(mailing, options, form);
    }

    private Date getMailingPlanDateFromForm(MailingSettingsForm form, Admin admin) throws ParseException {
        if (StringUtils.isBlank(form.getPlanDate())) {
            return null;
        }
        String formPlanDate = StringUtils.trimToEmpty(form.getPlanDate());
        if (formPlanDate.split(" ").length == 2) { // date and time is set
            Date planDate = admin.getDateTimeFormat().parse(formPlanDate);
            if ("00:00".equals(formPlanDate.split(" ")[1])) {
                return DateUtils.addSeconds(planDate, 1); // add a second to indicate in db DATE column that the time was not left empty and user set 00:00 by intention
            }
            return planDate;
        }
        return admin.getDateFormat().parse(formPlanDate); // date is set, time is not
    }

    protected String generateMailingTargetExpression(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options) {
        // Only change target expressions, that are NOT complex and not managed by workflow manager
        if (isTargetExpressionComplex(mailing) || !form.isAssignTargetGroups()) {
            return mailing.getTargetExpression();
        }
        boolean useOperatorAnd = useOperatorAndForRegularTargetsPart(mailing, form, options);
        return isBlank(form.getTargetExpression())
                ? TargetExpressionUtils.makeTargetExpression(form.getTargetGroupIds(), useOperatorAnd)
                : form.getTargetExpression();
    } // overridden in extended class

    protected boolean useOperatorAndForRegularTargetsPart(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options) {
        if (isTargetModeCheckboxVisible(mailing, isTargetExpressionComplex(mailing), options)
                && !isTargetModeCheckboxDisabled(options)) {
            return isFormTargetsHaveConjunction(form);
        }
        // Use currently set target group operator
        return StringUtils.isBlank(mailing.getTargetExpression()) ||
                !TargetExpressionUtils.extractNotAltgTargetExpressionPart(mailing.getTargetExpression(), targetService)
                        .contains(TargetExpressionUtils.OPERATOR_OR);
    }

    private boolean isFormTargetsHaveConjunction(MailingSettingsForm form) {
        return form.getTargetMode() == Mailing.TARGET_MODE_AND;
    }

    @Override
    public boolean isTargetModeCheckboxDisabled(MailingSettingsOptions options) {
        return (workflowDriven(options.getWorkflowId()) || options.isWorldSend())
                && !(options.isForCopy() || options.isForFollowUp());
    }

    @Override
    public boolean isTargetModeCheckboxVisible(Mailing mailing, boolean isTargetExpressionComplex, MailingSettingsOptions options) {
        return !(isTargetExpressionComplex ||
                ((workflowDriven(options.getWorkflowId()) || options.isWorldSend())
                        && CollectionUtils.size(mailing.getTargetGroups()) < 2));
    }

    private boolean workflowDriven(int workflowId) {
        return workflowId > 0;
    }
    
    protected boolean isTargetExpressionComplex(Mailing mailing) {
        return mailing.hasComplexTargetExpression(); // overridden in extended class
    }

    protected void setMediatypesToMailing(Mailing mailing, MailingSettingsOptions options, MailingSettingsForm form) {
        boolean requestApproval = mailing.getEmailParam().isRequestApproval();
        String approvedBy = mailing.getEmailParam().getApprovedBy();
        mailing.setMediatypes(form.getMediatypes().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                mt -> conversionService.convert(mt.getValue(), Mediatype.class))));
        MediatypeEmail mailingEmailMediatype = mailing.getEmailParam();
        EmailMediatypeForm formEmailMediatype = form.getEmailMediatype();
        mailingEmailMediatype.setLinefeed(formEmailMediatype.getLinefeed());
        mailingEmailMediatype.setCharset(formEmailMediatype.getCharset());
        mailingEmailMediatype.setOnepixel(formEmailMediatype.getOnepixel());
        mailingEmailMediatype.setRequestApproval(requestApproval);
        mailingEmailMediatype.setApprovedBy(approvedBy);
        setMailingFollowupProperties(mailing, form);
        if (mailing.isGridMailing()) {
            mailingEmailMediatype.setStatus(MediaTypeStatus.Active.getCode());
        }
        if (mailing.getMailingType() != MailingType.DATE_BASED) {
            mailingEmailMediatype.deleteDateBasedParameters();
        }
        syncMediatypeTemplates(mailing);
    }

    protected void setMailingFollowupProperties(Mailing mailing, MailingSettingsForm form) {
        // overwritten in extended class
    }

    private List<MailingParameter> collectMailingParams(MailingSettingsForm form, Admin admin, MailingSettingsOptions options) {
        List<MailingParameter> params = options.getMailingParams();
        // Let's retrieve all the parameters currently stored.
        if (isMailingRequiresOriginParams(form, admin, options)) {
            return params;
        }
        // Overwrite all the parameters with the user-defined ones if user is permitted to change parameters.
        List<MailingParameter> intervalParams = retrieveReservedParams(params);
        params = form.getParams().stream()
                .filter(param -> isNotEmpty(param.getName()))
                .collect(Collectors.toList());
        params.addAll(intervalParams);
        return params;
    }

    private List<MailingParameter> retrieveReservedParams(List<MailingParameter> params) {
        return params.stream()
                .filter(p -> isReservedParam(p.getName()))
                .collect(Collectors.toList());
    }

    protected boolean isMailingRequiresOriginParams(MailingSettingsForm form, Admin admin, MailingSettingsOptions opts) {
        return !admin.permissionAllowed(Permission.MAILING_PARAMETER_CHANGE);
    }

    private boolean isFormContainsOnlyAlwaysAllowedChanges(Mailing mailing, MailingSettingsForm form, MailingSettingsOptions options, Popups popups) {
        int gridTemplateId = gridService.getGridTemplateIdByMailingId(options.getMailingId());
        String textTemplateText = form.getEmailMediatype().getTextTemplate();
        String htmlTemplateText = form.getEmailMediatype().getHtmlTemplate();

        if (options.getCompanyId() == mailing.getCompanyID()
                && options.getMailingId() == mailing.getId()
                && StringUtils.equals(textTemplateText, mailing.getTextTemplate().getEmmBlock())
                && ((gridTemplateId > 0 || mailing.getEmailParam().getMailFormat() == 0) || StringUtils.equals(htmlTemplateText, mailing.getHtmlTemplate().getEmmBlock()))) {
            return true;
        }

        popups.alert(mailingService.hasMailingStatus(mailing.getId(), MailingStatus.SENT, mailing.getCompanyID()) ? "error.sent.mailing.change.denied" : "status_changed");
        return false;
    }

    /**
     * Saves current mailing in DB (including mailing components, content blocks, dynamic tags, dynamic tags contents
     * and trackable links)
     *
     * @throws Exception if anything went wrong
     */
    protected void saveMailing(Mailing mailing, MailingSettingsForm form, Admin admin, MailingSettingsOptions options, Popups popups) throws Exception {
        mailingValidator.validateMailingBeforeSave(mailing, admin.getLocale(), popups);
        boolean approved = mailingService.isApproved(mailing.getId(), mailing.getCompanyID());

        mailingBaseService.saveUndoData(mailing.getId(), admin.getAdminID());
        if (options.isCopying()) {
            mailingService.saveMailing(mailing, admin.permissionAllowed(Permission.MAILING_TRACKABLELINKS_NOCLEANUP));
        } else {
            mailingService.saveMailingWithNewContent(mailing, admin);
        }
        if (options.getGridTemplateId() > 0) {
            saveMailingGridInfo(options.getGridTemplateId(), mailing.getId(), admin);
        }
        updateMailingParams(mailing, admin);
        if (mailing.isIsTemplate()) {
            mailingService.updateMailingsWithDynamicTemplate(mailing, applicationContext);
        }

        if (approved && !options.isNew()) {
            mailingService.writeRemoveApprovalLog(mailing.getId(), admin);
        }
    }

    private void updateMailingParams(Mailing mailing, Admin admin) {
        if (!admin.permissionAllowed(Permission.MAILING_CHANGE)) {
            return;
        }
        List<UserAction> userActions = new ArrayList<>();
        mailingParameterService.updateParameters(admin.getCompanyID(), mailing.getId(), mailing.getParameters(), admin.getAdminID(), userActions);
        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
    }
    
    private boolean handleSaveExceptions(int mailingId, Popups popups, ExceptionHandler saveFunc) {
        try {
            saveFunc.execute();
        } catch (TooManyTargetGroupsInMailingException e) {
            LOGGER.info(String.format("Too many target groups for mailing %d", mailingId), e);
            popups.alert("error.mailing.tooManyTargetGroups");
        } catch (Exception e) {
            LOGGER.info(String.format("Error occurred: %s", e.getMessage()), e);
            popups.alert(ERROR_MSG);
        }
        return !popups.hasAlertPopups();
    }

    private boolean handleMailingPrepareExceptions(int mailingId, Popups popups, ExceptionHandler prepareFunc) {
        try {
            prepareFunc.execute();
        } catch (UserMessageException e) {
            popups.alert(e.getErrorMessageKey(), e.getAdditionalErrorData());
        } catch (ParseException e) {
            popups.alert("error.mailing.wrong.plan.date.format");
        } catch (MissingEndTagException e) {
            LOGGER.info("Missing end tag", e);
            popups.alert("error.template.dyntags.missing_end_tag", e.getLineNumber(), e.getTag());
        } catch (UnclosedTagException e) {
            LOGGER.info("Unclosed tag", e);
            popups.alert("error.template.dyntags.unclosed_tag", e.getTag());
        } catch (Exception e) {
            LOGGER.error(String.format("Error in save mailing id: %d", mailingId), e);
            popups.alert("error.mailing.content");
        }
        return !popups.hasAlertPopups();
    }

    @Override
    public Map<String, Object> saveMailingGridInfo(int gridTemplateId, int mailingId, Admin admin) {
        Map<String, Object> data = new HashMap<>();
        data.put("TEMPLATE_ID", gridTemplateId);
        data.put("OWNER", admin.getAdminID());
        data.put("OWNER_NAME", admin.getUsername());

        gridService.saveMailingGridInfo(mailingId, admin.getCompanyID(), data);
        return data;
    }

    private void setMediatypesToForm(Mailing mailing, MailingSettingsForm form) {
        mailing.getMediatypes().values().forEach(mt -> form.getMediatypes()
                .put(mt.getMediaType().getMediaCode(), conversionService.convert(mt, MediatypeForm.class)));

        MailingComponent comp;

        for (MediaTypes type : MediaTypes.values()) {
            comp = mailing.getTemplate(type.getKey());
            if (comp != null) {
                setTemplateToMediatypeForm(form, comp, type);
            }
        }
        comp = mailing.getHtmlTemplate();
        if (comp != null) {
            form.getEmailMediatype().setHtmlTemplate(comp.getEmmBlock());
        }
    }
    
    @Override // overridden in extended class 
    public void setMailingTargetsToForm(MailingSettingsForm form, Mailing mailing) {
        form.setTargetGroupIds(mailing.getTargetGroups());
        form.setTargetMode(mailing.getTargetMode());
        form.setTargetExpression(mailing.getTargetExpression());
    }

    protected int getTargetModeForForm(Mailing mailing) {
        return mailing.getTargetMode(); // overridden in extended class
    }

    protected void setTemplateToMediatypeForm(MailingSettingsForm form, MailingComponent comp, MediaTypes mediatype) {
        if (mediatype == MediaTypes.EMAIL) {
            form.getEmailMediatype().setTextTemplate(comp.getEmmBlock());
        }
    }

    /**
     * Loads chosen mailing template data into form.
     *
     * @param template        Mailing bean object, contains mailing template data
     * @param form            MailingSettingsForm object
     * @param regularTemplate whether the regular template is used or a mailing (clone & edit)
     */
    @Override
    public void copyTemplateSettingsToMailingForm(Mailing template, MailingSettingsForm form, Integer workflowId, boolean regularTemplate, boolean withFollowUpSettings) {
        MailingComponent tmpComp;
        // If we already have a campaign we don't have to override settings inherited from it
        boolean overrideInherited = (workflowId == null || workflowId == 0 || !regularTemplate);

        if (overrideInherited) {
            form.setMailingType(template.getMailingType());
            form.setMailinglistId(template.getMailinglistID());
            form.setArchiveId(template.getCampaignID());
        }
        if (overrideInherited || template.getMailingType() == MailingType.DATE_BASED) {
            form.setTargetGroupIds(template.getTargetGroups());
        }
        form.setTargetMode(getTargetModeForForm(template));
        setMediatypesToForm(template, form);
        form.setArchived(template.getArchived() != 0);
        form.setNeedsTarget(template.getNeedsTarget());
        form.setUseDynamicTemplate(template.getUseDynamicTemplate());
        form.setMailingContentType(template.getMailingContentType());

        // load template for this mailing
        EmailMediatypeForm emailMediatypeForm = form.getEmailMediatype();
        if ((tmpComp = template.getHtmlTemplate()) != null) {
            emailMediatypeForm.setHtmlTemplate(tmpComp.getEmmBlock());
        }
        if ((tmpComp = template.getTextTemplate()) != null) {
            emailMediatypeForm.setTextTemplate(tmpComp.getEmmBlock());
        }

        MediatypeEmail emailMediatype = template.getEmailParam();
        if (emailMediatype != null) {
            emailMediatypeForm.setOnepixel(emailMediatype.getOnepixel());
            try {
                emailMediatypeForm.setReplyEmail(new InternetAddress(emailMediatype.getReplyAdr()).getAddress());
            } catch (Exception e) {
                // do nothing
            }
            try {
                emailMediatypeForm.setReplyFullname(new InternetAddress(emailMediatype.getReplyAdr()).getPersonal());
            } catch (Exception e) {
                // do nothing
            }
        }

        // Create a clone copy of all mailing parameters
        List<MailingParameter> templateMailingParameters = mailingParameterService.getMailingParameters(template.getCompanyID(), template.getId());
        List<MailingParameter> newParameters = new ArrayList<>();

        if (templateMailingParameters != null) {
            for (MailingParameter parameter : templateMailingParameters) {
                if (isReservedParam(parameter.getName())) {
                    continue;
                }
                MailingParameter newParameter = new MailingParameter();

                newParameter.setName(parameter.getName());
                newParameter.setValue(parameter.getValue());
                newParameter.setDescription(parameter.getDescription());
                newParameter.setCreationDate(parameter.getCreationDate());

                newParameters.add(newParameter);
            }
        }
        form.setParams(newParameters);
    }

    @Override
    public void populateDisabledSettings(Mailing mailing, MailingSettingsForm form, boolean isGrid, Admin admin, WorkflowParameters workflowParams) {
        if (!mailing.isIsTemplate() && admin.permissionAllowed(Permission.MAILING_SETTINGS_HIDE)) {
            populateDisabledGeneralSettings(form, mailing);
        }
        populateActiveMailingSettings(form, mailing, admin);

        // when creating a new mailing, the last state of the workflow was not saved
        if (mailing.getId() > 0 && !mailing.isIsTemplate()) {
            populateWorkflowDrivenSettings(form, mailing, admin, workflowParams);
        }
    }

    // When 'mailing.settings.hide' permission is set
    // then general settings should be passed from original mailing
    // But text template and html template should be editable
    protected void populateDisabledGeneralSettings(MailingSettingsForm form, Mailing mailing) {
        form.setMailinglistId(mailing.getMailinglistID());
        form.setMailingType(mailing.getMailingType());
        form.setArchived(mailing.getArchived() == 1);
        int emailMediatypeCode = MediaTypes.EMAIL.getMediaCode();
        EmailMediatypeForm emailMediatype = (EmailMediatypeForm)
                conversionService.convert(mailing.getMediatypes().get(emailMediatypeCode), MediatypeForm.class);
        if (emailMediatype != null) {
            emailMediatype.setTextTemplate(form.getEmailMediatype().getTextTemplate());
            emailMediatype.setHtmlTemplate(form.getEmailMediatype().getHtmlTemplate());
        }
        form.getMediatypes().put(emailMediatypeCode, emailMediatype);
    }

    private void populateActiveMailingSettings(MailingSettingsForm form, Mailing mailing, Admin admin) {
        if (maildropService.isActiveMailing(mailing.getId(), admin.getCompanyID())) {
            setMediatypesToForm(mailing, form);
            setDisabledSettingsToForm(form, mailing, admin);
        }
    }

    private void setDisabledSettingsToForm(MailingSettingsForm form, Mailing mailing, Admin admin) {
        form.setMailinglistId(mailing.getMailinglistID());
        setMailingTargetsToForm(form, mailing);
        form.setTargetExpression(mailing.getTargetExpression());
        if (mailing.getPlanDate() != null) {
            SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
            form.setPlanDate(dateTimeFormat.format(mailing.getPlanDate()));
        }
        form.setMailingType(mailing.getMailingType());
    }

    private void populateWorkflowDrivenSettings(MailingSettingsForm form, Mailing mailing, Admin admin, WorkflowParameters workflowParams) {
        if (workflowParams == null) {
            return;
        }
        workflowService.assignWorkflowDrivenSettings(admin, mailing, workflowParams.getWorkflowId(), workflowParams.getNodeId());
        if (StringUtils.isNotBlank(workflowParams.getParamsAsMap().get("mailingType"))) {
            getMailingTypeFromForwardParams(workflowParams)
                    .ifPresent(mailing::setMailingType);
        }

        setDisabledSettingsToForm(form, mailing, admin);
        form.setArchiveId(mailing.getCampaignID());
        form.setArchived(mailing.getArchived() == 1);
    }

    @Override
    public Optional<MailingType> getMailingTypeFromForwardParams(WorkflowParameters params) {
        if (params == null || StringUtils.isBlank(params.getParamsAsMap().get("mailingType"))) {
            return Optional.empty();
        }

        try {
            String mailingTypeStr = params.getParamsAsMap().get("mailingType");
            return Optional.of(MailingType.fromCode(NumberUtils.toInt(mailingTypeStr, 0)));
        } catch (Exception e) {
            return Optional.of(MailingType.NORMAL);
        }
    }

    @Override
    public MailingSettingsForm prepareFormForCopy(Mailing origin, Locale locale, Integer workflowId, boolean forFollowUp) {
        MailingSettingsForm form = getNewSettingsForm();

        copyTemplateSettingsToMailingForm(origin, form, workflowId, false, false);
        form.setShortname(forFollowUp
                ? SafeString.getLocaleString("mailing.Followup_Mailing", locale) + " " + origin.getShortname()
                : SafeString.getLocaleString("mailing.CopyOf", locale) + " " + origin.getShortname());
        form.setDescription(forFollowUp ? "" : origin.getDescription());
        return form;
    }

    @Override
    public void removeInvalidTargets(final Mailing mailing, final Popups popups) {
        if (mailing.getTargetGroups() == null) {
            return;
        }
        final List<TargetLight> targets = this.targetService.getTargetLights(mailing.getCompanyID(), mailing.getTargetGroups(), true);
        final Set<Integer> toRemove = new HashSet<>();

        for (final TargetLight target : targets) {
            if (!target.isValid()) {
                popups.warning(new Message("warning.mailing.import.targetgroupInvalid", new Object[]{target.getId(), target.getTargetName()}));
                toRemove.add(target.getId());
            }
        }
        mailing.getTargetGroups().removeAll(toRemove);
    }

    protected boolean isAllowedMailingIconTypeForFill(int type) {
        return type == WorkflowIconType.MAILING.getId();
    }

    // Functional interface for handling exceptions
    private interface ExceptionHandler {
        void execute() throws Exception;
    }

    private void writeUserActivityLog(Admin admin, UserAction ua) {
        UserActivityUtil.log(userActivityLogService, admin, ua, LOGGER);
    }
}
