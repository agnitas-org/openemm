/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponentType;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.SafeString;
import org.agnitas.web.DispatchBaseAction;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComCampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.service.ComSampleWorkflowFactory;
import com.agnitas.emm.core.workflow.service.ComWorkflowActivationService;
import com.agnitas.emm.core.workflow.service.ComWorkflowDataParser;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.emm.core.workflow.service.ComWorkflowStatisticsService;
import com.agnitas.emm.core.workflow.service.ComWorkflowValidationService;
import com.agnitas.emm.core.workflow.service.ComWorkflowValidationService.MailingTrackingUsageError;
import com.agnitas.emm.core.workflow.service.ComWorkflowValidationService.MailingTrackingUsageErrorType;
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.emm.core.workflow.web.forms.ComWorkflowForm;
import com.agnitas.service.ComWebStorage;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;

import net.sf.json.JSONObject;

public class ComWorkflowAction extends DispatchBaseAction {
    private static final transient Logger logger = Logger.getLogger(ComWorkflowAction.class);

    public static final String INCOMPLETE_WORKFLOW_NAME = "incompleteWorkflowName";
    
    public static final String FORWARD_USERFORM_CREATE = "userform_create";
    public static final String FORWARD_USERFORM_EDIT = "userform_edit";
    public static final String FORWARD_REPORT_CREATE = "report_create";
    public static final String FORWARD_REPORT_EDIT = "report_edit";
    public static final String FORWARD_TARGETGROUP_CREATE = "targetgroup_create";
    public static final String FORWARD_TARGETGROUP_CREATE_QB = "targetgroup_create_qb";
    public static final String FORWARD_TARGETGROUP_EDIT = "targetgroup_edit";
    public static final String FORWARD_TARGETGROUP_EDIT_QB = "targetgroup_edit_qb";
    public static final String FORWARD_ARCHIVE_CREATE = "archive_create";
    public static final String FORWARD_MAILING_CREATE = "mailing_create";
    public static final String FORWARD_MAILING_EDIT = "mailing_edit";
    public static final String FORWARD_MAILING_COPY = "mailing_copy";
    public static final String FORWARD_AUTOIMPORT_CREATE = "autoimport_create";
    public static final String FORWARD_AUTOIMPORT_EDIT = "autoimport_edit";
    public static final String FORWARD_AUTOEXPORT_CREATE = "autoexport_create";
    public static final String FORWARD_AUTOEXPORT_EDIT = "autoexport_edit";
    
    public static final String WORKFLOW_FORWARD_PARAMS = WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS;
    public static final String WORKFLOW_ID = WorkflowParametersHelper.WORKFLOW_ID;
    public static final String WORKFLOW_FORWARD_TARGET_ITEM_ID = WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID;
    public static final String WORKFLOW_KEEP_FORWARD = WorkflowParametersHelper.WORKFLOW_KEEP_FORWARD;
    public static final String WORKFLOW_NODE_ID = WorkflowParametersHelper.WORKFLOW_NODE_ID;
    public static final String WORKFLOW_CUSTOM_CSS_STYLE = ".body{background-color: #fff;} #viewPort { display: inline-block !important; width: 100% !important;}";
    
    private ComWorkflowService workflowService;
    private ComWorkflowValidationService validationService;
	private ComWorkflowActivationService workflowActivationService;
	private ComWorkflowStatisticsService workflowStatisticsService;
	private AutoImportService autoImportService;
	private AutoExportService autoExportService;
    private ComWorkflowDataParser workflowDataParser;
    protected ComCampaignDao campaignDao;
    private ComMailingDeliveryStatService deliveryStatService;
    private ComMailingComponentDao componentDao;
    private GenerationPDFService generationPDFService;
    private ComCompanyDao companyDao;
	protected ConfigService configService;
    protected ComMailinglistService mailinglistService;
    protected MailingService mailingService;
    private WebStorage webStorage;
    private MailinglistApprovalService mailinglistApprovalService;

    public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;

        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.WORKFLOW_OVERVIEW, workflowForm);

        if (workflowForm.getColumnwidthsList() == null) {
            workflowForm.setColumnwidthsList(getInitializedColumnWidthList(3));
        }
        request.setAttribute("workflows", workflowService.getWorkflowsOverview(AgnUtils.getCompanyID(request)));

        AgnUtils.setAdminDateTimeFormatPatterns(request);

        return mapping.findForward("list");
	}

    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        workflowForm.resetFormValues(mapping, request);
        workflowForm.getWorkflow().setStatus(WorkflowStatus.STATUS_OPEN);
        workflowForm.setNewStatus(WorkflowStatus.STATUS_ACTIVE.name());
		prepareViewPage(request, workflowForm);

        return mapping.findForward("view");
    }

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);
        if (admin == null) {
            return mapping.findForward("logon");
        }
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        String forwardName = request.getParameter("forwardName");

        Workflow newWorkflow = getWorkflow(workflowForm, admin);
        Workflow existingWorkflow = workflowService.getWorkflow(newWorkflow.getWorkflowId(), newWorkflow.getCompanyId());

        WorkflowStatus existingStatus = existingWorkflow != null ? existingWorkflow.getStatus() : WorkflowStatus.STATUS_NONE;
        WorkflowStatus newStatus = newWorkflow.getStatus() != WorkflowStatus.STATUS_NONE ? newWorkflow.getStatus() : WorkflowStatus.STATUS_OPEN;
        boolean isActiveOrTesting = newStatus == WorkflowStatus.STATUS_ACTIVE || newStatus == WorkflowStatus.STATUS_TESTING;

        ActionMessages messages = new ActionMessages();
        ActionErrors errors = new ActionErrors();

        if (StringUtils.isNotEmpty(forwardName) && StringUtils.length(newWorkflow.getShortname()) < ComWorkflowForm.SHORTNAME_MIN_LENGTH) {
            newWorkflow.setShortname(INCOMPLETE_WORKFLOW_NAME);
        }

        // Running or complete campaign should never be saved.
        if (existingStatus.isChangeable()) {
            // Set OPEN_STATUS until validation passed and workflow is activated.
            newWorkflow.setStatus(WorkflowStatus.STATUS_OPEN);
            workflowService.saveWorkflow(admin, newWorkflow, getIcons(workflowForm));
            newWorkflow.setStatus(newStatus);

            if (existingWorkflow == null) {
                writeWorkflowCreationLog(admin, newWorkflow);
            } else {
                writeWorkflowChangeLog(admin, existingWorkflow, newWorkflow);
            }

            List<WorkflowIcon> icons = newWorkflow.getWorkflowIcons();
            errors.add(validateWorkflow(request, icons, newWorkflow.getWorkflowId(), newStatus));

            if (StringUtils.isNotEmpty(forwardName)) {
                return getForward(mapping, request, forwardName, newWorkflow.getWorkflowId(), icons);
            }

            setStatus(admin, newWorkflow, existingWorkflow, messages, errors, errors.isEmpty() && request.getAttribute("affectedMailings") == null);
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));

            workflowForm.setWorkflowId(newWorkflow.getWorkflowId());
        } else {
            assert (existingWorkflow != null);

            if (StringUtils.isNotEmpty(forwardName)) {
                return getForward(mapping, request, forwardName, existingWorkflow.getWorkflowId(), existingWorkflow.getWorkflowIcons());
            }

            if (validateStatusTransition(existingStatus, newStatus, errors)) {
                workflowService.changeWorkflowStatus(existingWorkflow.getWorkflowId(), existingWorkflow.getCompanyId(), newStatus);
            }
            writeWorkflowStatusChangeLog(newWorkflow, existingWorkflow, admin);
        }

        if (isActiveOrTesting) {
            saveErrors(request, errors);
        } else {
            messages.add(errors);
        }

        saveMessages(request, messages);
        ComWorkflowAction.updateForwardParameters(request, false);

        return view(mapping, form, request, response);
    }

    private void checkAndSetDuplicateMailing(HttpServletRequest request, List<WorkflowIcon> icons, boolean isActiveOrTesting) {
        List<Mailing> duplicatedMailings = mailingService.getDuplicateMailing(icons, AgnUtils.getCompanyID(request));
        if (!duplicatedMailings.isEmpty()) {
            request.setAttribute("affectedMailingsMessageType", isActiveOrTesting ? GuiConstants.MESSAGE_TYPE_ALERT : GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
            request.setAttribute("affectedMailingsMessageKey", "error.workflow.mailingIsUsingInSeveralIcons");
            request.setAttribute("affectedMailings", duplicatedMailings);
        }
    }

    private void setStatus(ComAdmin admin, Workflow workflow, Workflow existingWorkflow, ActionMessages messages, ActionErrors errors, boolean isValid) throws Exception {
        WorkflowStatus currentStatus = existingWorkflow != null ? existingWorkflow.getStatus() : WorkflowStatus.STATUS_NONE;
        WorkflowStatus newStatus = workflow.getStatus();

        final int workflowId = workflow.getWorkflowId();

        if (isValid && validateStatusTransition(currentStatus, newStatus, errors)) {
            if (newStatus == WorkflowStatus.STATUS_ACTIVE || newStatus == WorkflowStatus.STATUS_TESTING) {
                boolean testing = newStatus == WorkflowStatus.STATUS_TESTING;

                List<UserAction> userActions = new ArrayList<>();
                workflowService.deleteWorkflowTargetConditions(admin.getCompanyID(), workflowId);
                if (workflowActivationService.activateWorkflow(workflowId, admin, testing, messages, errors, userActions)) {

                    for (UserAction action : userActions) {
                        writeUserActivityLog(admin, action);
                    }
                } else {
                    newStatus = testing ? WorkflowStatus.STATUS_TESTING_FAILED : WorkflowStatus.STATUS_FAILED;
                }
            }
        } else {
            newStatus = currentStatus == WorkflowStatus.STATUS_NONE ? WorkflowStatus.STATUS_OPEN : currentStatus;
        }

        workflow.setStatus(newStatus);
        workflowService.changeWorkflowStatus(workflowId, admin.getCompanyID(), newStatus);
    }

    private boolean validateStatusTransition(WorkflowStatus currentStatus, WorkflowStatus newStatus, ActionMessages errors) {
        switch (currentStatus) {
            case STATUS_COMPLETE:
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.workflow.campaignStatusHasNotBeUpdatedAfterCompleted"));
                return false;

            case STATUS_ACTIVE:
            case STATUS_TESTING:
                if (newStatus == WorkflowStatus.STATUS_OPEN || newStatus == WorkflowStatus.STATUS_INACTIVE) {
                    return true;
                }
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.workflow.SaveActivatedWorkflow"));
                return false;

            default:
                switch (newStatus) {
                    case STATUS_OPEN:
                    case STATUS_INACTIVE:
                    case STATUS_ACTIVE:
                    case STATUS_TESTING:
                        return true;

                    default:
                        return false;
                }
        }
    }

    private boolean writeWorkflowStatusChangeLog(Workflow workflow, Workflow existedWorkflow, ComAdmin admin) {
        if (workflow.getWorkflowId() == 0) {
            writeUserActivityLog(admin, "create campaign", getWorkflowDescription(workflow));
            return false;
        }

        WorkflowStatus currentStatus = existedWorkflow.getStatus(), newStatus = workflow.getStatus();
        String statusAction = null;

        if(currentStatus == WorkflowStatus.STATUS_ACTIVE && newStatus == WorkflowStatus.STATUS_INACTIVE) {
            statusAction = "do deactivate campaign";

        } else if ((currentStatus == WorkflowStatus.STATUS_INACTIVE || currentStatus == WorkflowStatus.STATUS_OPEN)
                && newStatus == WorkflowStatus.STATUS_ACTIVE) {
            statusAction ="do activate campaign";

        } else if (currentStatus == WorkflowStatus.STATUS_TESTING && newStatus == WorkflowStatus.STATUS_OPEN) {
            statusAction = "do stop test campaign";

        } else if (currentStatus == WorkflowStatus.STATUS_OPEN && newStatus == WorkflowStatus.STATUS_TESTING) {
            statusAction = "do start test campaign";
        }

        boolean isUpdated = false;
        if(statusAction != null){
            writeUserActivityLog(admin, statusAction, getWorkflowDescription(workflow));
            isUpdated = true;
        }

        return isUpdated;
    }

    private ActionMessages validateWorkflow(HttpServletRequest request, List<WorkflowIcon> icons, int workflowId, WorkflowStatus status) {
        final ComAdmin admin = AgnUtils.getAdmin(request);

        assert (admin != null);

        final int companyId = admin.getCompanyID();
        final boolean isMailtrackingActive = companyDao.isMailtrackingActive(companyId);
        final TimeZone timezone = TimeZone.getTimeZone(admin.getAdminTimezone());
        final boolean isActive = status == WorkflowStatus.STATUS_ACTIVE;
        final boolean isTesting = status == WorkflowStatus.STATUS_TESTING;

        ActionMessages errors = new ActionErrors();
        String errorsType = GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING;
        if (isActive || isTesting) {
            errorsType = ActionMessages.GLOBAL_MESSAGE;
        }

        if (!validationService.isAllIconsFilled(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.nodesShouldBeFilled"));
            if (icons.isEmpty()) {
                return errors;
            }
        }
        if (validationService.isStartIconMissing(icons) || validationService.isStopIconMissing(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.campaignShouldHaveAtLeastOneStartAndEnd"));
        }
        if (!validationService.validateReminderRecipients(icons)) {
            errors.add(errorsType, new ActionMessage("error.email.invalid"));
        }
        if (!validationService.hasRecipient(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.campaignShouldHaveRecipient"));
        }
        if (!validationService.isNotStartDateInPast(icons, timezone)) {
            errors.add(errorsType, new ActionMessage("error.workflow.campaignShouldNotHaveStartDateInPast"));
        }
        if (!validationService.isNotStopDateInPast(icons, timezone)) {
            errors.add(errorsType, new ActionMessage("error.workflow.campaignShouldNotHaveStopDateInPast"));
        }
        if (!validationService.noStartDateLaterEndDate(icons, timezone)) {
            errors.add(errorsType, new ActionMessage("error.validation.enddate"));
        }
        if (validationService.isReminderDateInThePast(icons, timezone)) {
            errors.add(errorsType, new ActionMessage("error.workflow.reminderDateInPast"));
        }
        if (!validationService.hasIconsConnections(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.nodesShouldHaveIncomingAndOutgoingArrows"));
        }
        if (validationService.isAutoImportMisused(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.autoImport.start.event"));
        }
        if (validationService.isAutoImportHavingShortDeadline(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.autoImport.delay.tooShort", WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT));
        }
        if (validationService.decisionsHaveTwoOutgoingConnections(icons)) {
            if (!isMailtrackingActive && !validationService.decisionNegativePathConnectionShouldLeadToStopIcon(icons)) {
                errors.add(errorsType, new ActionMessage("error.workflow.decisionNegativePathConnectionShouldLeadToStopIcon"));
            }
        } else {
            errors.add(errorsType, new ActionMessage("error.workflow.decisionsShouldHaveTwoOutgoingConnections"));
        }
        if (!validationService.noParallelBranches(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.ParallelBranches"));
        }
        if (!validationService.parametersSumNotHigher100(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.ParametersSumNotHigher100"));
        }
        if (!validationService.noParallelCampaigns(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.NoParallelCampaigns"));
        }
        if (!validationService.noMailingsBeforeRecipient(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.NoMailingsBeforeRecipient"));
        }
        if (!validationService.checkMailingTypesCompatible(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.mixedMailingTypes"));
        }
        if (!validationService.isFixedDeadlineUsageCorrect(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.fixedDeadlineIsNotPermitted"));
        }
		if (!validationService.isDeadlineDateCorrect(icons, timezone)) {
			errors.add(errorsType, new ActionMessage("error.workflow.deadlineDateTooEarly"));
		}
		if (!validationService.mailingHasOnlyOneRecipient(icons)) {
			errors.add(errorsType, new ActionMessage("error.workflow.mailingHasOneRecipient"));
		}
		if (!validationService.campaignHasOnlyOneMailingList(icons)) {
			errors.add(errorsType, new ActionMessage("error.workflow.oneMailingList"));
		}
        if (!validationService.campaignHasOnlyMailingsAssignedToThisWorkflow(icons, companyId, workflowId)) {
            errors.add(errorsType, new ActionMessage("error.workflow.mailingIsUsingInAnotherCampaign"));
        }

        if (!validationService.isImportIsNotActive(icons, companyId)) {
            errors.add(errorsType, new ActionMessage("error.workflow.importIsActive"));
        }

        if (!validationService.isExportIsNotActive(icons, companyId)) {
            errors.add(errorsType, new ActionMessage("error.workflow.exportIsActive"));
        }

        if (validationService.containsSentMailings(icons, companyId)) {
		    errors.add(errorsType, new ActionMessage("error.workflow.containsSentMailings"));
        }
        if (!isTesting) {
            if (validationService.isInvalidDelayForDateBase(icons)) {
                errors.add(errorsType, new ActionMessage("error.workflow.dateBased.invalid.delay"));
            }
        }

        if (workflowService.hasDeletedMailings(icons, companyId)) {
            errors.add(errorsType, new ActionMessage("error.workflow.containsDeletedContent"));
        }

        checkAndSetDuplicateMailing(request, icons, isActive || isTesting);

        int mailingTrackingDataExpirationPeriod = 0;
        if (isMailtrackingActive) {
            mailingTrackingDataExpirationPeriod = companyDao.getSuccessDataExpirePeriod(companyId);
        }

        if (!validationService.noLoops(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.NoLoops"));
            return errors;
        }

        if (!validationService.validateAutoOptimizationStructure(icons)) {
            errors.add(errorsType, new ActionMessage("error.workflow.connection.notAllowed"));
        }

        errors.add(validationService.validateStartTrigger(icons, companyId, errorsType));
        errors.add(validateMailingTrackingUsage(errorsType, icons, companyId, mailingTrackingDataExpirationPeriod));
        errors.add(validateReferencedProfileFields(errorsType, icons, companyId));
        errors.add(validateOperatorsInDecisions(errorsType, icons, companyId));

        return errors;
    }

    private ActionMessages validateOperatorsInDecisions(String errorsType, List<WorkflowIcon> icons, int companyId) {
        ActionMessages errors = new ActionMessages();
        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.DECISION.getId() && icon.isFilled()) {
                WorkflowDecision decision = (WorkflowDecision) icon;
                int criteriaId = decision.getDecisionCriteria().getId();
                if (criteriaId == WorkflowDecision.WorkflowDecisionCriteria.DECISION_PROFILE_FIELD.getId()) {
                    errors.add(validationService.validateDecisionRules(errorsType, decision, companyId));
                }
            }
        }

        return errors;
    }

    private ActionMessages validateMailingTrackingUsage(String errorsProp, List<WorkflowIcon> icons, @VelocityCheck int companyId, int trackingDays) {
        ActionMessages errors = new ActionMessages();

        // It's possible to show a separate error message for each case (e.g. listing names of affected mailings)
        // but for now just get rid of duplicated messages.
        Set<MailingTrackingUsageErrorType> reportedErrors = new HashSet<>();

        validationService.checkFollowupMailing(icons, companyId, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                errors.add(errorsProp, translateToActionMessage(e, trackingDays));
            }
        });

        validationService.checkMailingTrackingUsage(icons, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                errors.add(errorsProp, translateToActionMessage(e, trackingDays));
            }
        });

        validationService.checkMailingsReferencedInDecisions(icons, companyId, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                errors.add(errorsProp, translateToActionMessage(e, trackingDays));
            }
        });

        return errors;
    }

    private ActionMessages validateReferencedProfileFields(String errorsType, List<WorkflowIcon> workflowIcons, int companyId) {
        ActionMessages errors = new ActionMessages();
        List<String> columns;

        columns = validationService.checkTrackableProfileFields(workflowIcons, companyId);
        if (columns.size() > 0) {
            errors.add(errorsType, new ActionMessage("error.workflow.profiledb.missingTrackableColumns", "<br>" + StringUtils.join(columns, "<br>")));
        }

        columns = validationService.checkProfileFieldsUsedInConditions(workflowIcons, companyId);
        if (columns.size() > 0) {
            errors.add(errorsType, new ActionMessage("error.workflow.profiledb.missingColumnsForConditions", "<br>" + StringUtils.join(columns, "<br>")));
        }

        return errors;
    }

    private Workflow getWorkflow(ComWorkflowForm form, ComAdmin admin) {
        Workflow workflow = form.getWorkflow();
        workflow.setWorkflowId(form.getWorkflowId());
        workflow.setCompanyId(admin.getCompanyID());
        return workflow;
    }

    private ActionMessage translateToActionMessage(MailingTrackingUsageError error, int trackingDays) {
        int mailingType = error.getMailingType();
        switch (error.getErrorType()) {
            case BASE_MAILING_NOT_FOUND:
            case DECISION_MAILING_INVALID:
                if (mailingType == ComMailing.TYPE_ACTIONBASED || mailingType == ComMailing.TYPE_DATEBASED) {
                    return new ActionMessage("error.workflow.baseMailingNeedActivated", error.getMailingName());
                } else {
                    return new ActionMessage("error.workflow.baseMailingNeedsSent", error.getMailingName());
                }

            case BASE_MAILING_DISORDERED:
                return new ActionMessage("error.workflow.baseMailingAtFirst", error.getMailingName());

            case DECISION_MAILING_DISORDERED:
                return new ActionMessage("error.workflow.decision.requiresMailingBefore", error.getMailingName());

            case MAILING_TRACKING_DISABLED:
                return new ActionMessage("error.workflow.trackingRequired");

            case EXPIRATION_PERIOD_EXCEEDED:
                return new ActionMessage("error.workflow.trackingtime", trackingDays);
        }

        return new ActionMessage("Error");
    }

    public ActionForward view(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        ActionMessages messages = new ActionMessages();
        loadWorkflow(workflowForm, request, messages);

        if (StringUtils.isNotEmpty(workflowForm.getUsingActivatedWorkflow())) {
            final String msg = SafeString.getLocaleString("error.workflow." + workflowForm.getUsingActivatedWorkflow(),
                    AgnUtils.getLocale(request));
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING,
                    new ActionMessage(String.format(msg, workflowForm.getUsingActivatedWorkflowName()), false));
        }

        if (StringUtils.isNotEmpty(workflowForm.getPartOfActivatedWorkflow())) {
            final String msg = SafeString.getLocaleString("error.workflow." + workflowForm.getPartOfActivatedWorkflow(),
                    AgnUtils.getLocale(request));
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING,
                    new ActionMessage(String.format(msg, workflowForm.getPartOfActivatedWorkflowName()), false));
        }

        WorkflowStatus workflowStatus = workflowForm.getWorkflow().getStatus();
        if (workflowStatus == WorkflowStatus.STATUS_ACTIVE || workflowStatus == WorkflowStatus.STATUS_TESTING) {
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.workflow.SaveActivatedWorkflow"));
        }
        prepareViewPage(request, workflowForm);

        AgnUtils.getParamsMap(request.getParameter("forwardParams"))
            .forEach(request::setAttribute);

        if (!messages.isEmpty()) {
            saveMessages(request, messages);
        }

        writeUserActivityLog(AgnUtils.getAdmin(request), "view campaign", getWorkflowDescription(workflowForm.getWorkflow()));

        return mapping.findForward("view");
    }

    public ActionForward viewOnlyElements(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages messages = new ActionMessages();
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;

        loadWorkflow(workflowForm, request, messages);
        prepareViewPage(request, workflowForm);

        //extract forwardParams
        Map<String, String> forwardParamsMap = AgnUtils.getParamsMap(request.getParameter("forwardParams"));
        for (Entry<String, String> entry : forwardParamsMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
        if (!messages.isEmpty()) {
            saveMessages(request, messages);
        }
        request.setAttribute("showStatistics", request.getParameter("showStatistics"));

        writeUserActivityLog(AgnUtils.getAdmin(request), "view campaign", getWorkflowDescription(workflowForm.getWorkflow()));

        return mapping.findForward("view_only_elements");
    }

    public ActionForward generatePDF(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String jsessionid = request.getSession().getId();
        String workflowId = request.getParameter("workflowId");
        String showStatistics = request.getParameter("showStatistics");
        String hostUrl = configService.getValue(AgnUtils.getHostName(), ConfigValue.SystemUrl);
        String url = hostUrl + "/workflow.do;jsessionid=" + jsessionid + "?workflowId=" + workflowId + "&method=viewOnlyElements&showStatistics=" + showStatistics;

        String workflowName = workflowService.getWorkflow(Integer.parseInt(workflowId), AgnUtils.getCompanyID(request)).getShortname();
        boolean errorOccurred = false;
        File pdfFile = generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath), url, HttpUtils.escapeFileName(workflowName), AgnUtils.getAdmin(request), "wmLoadFinished", "Landscape", "workflow.single", WORKFLOW_CUSTOM_CSS_STYLE);
        if (pdfFile != null) {
            try (
                    ServletOutputStream responseOutput = response.getOutputStream();
                    FileInputStream instream = new FileInputStream(pdfFile)
            ) {
                response.setContentType("application/pdf");
                HttpUtils.setDownloadFilenameHeader(response, workflowName  + ".pdf" );
                response.setContentLength((int)pdfFile.length());

                byte bytes[] = new byte[16384];
                int len = 0;
                while((len = instream.read(bytes))!=-1) {
                    responseOutput.write(bytes, 0, len);
                }
            } catch (Exception e) {
            	errorOccurred = true;
                throw e;
            } finally {
                if (!errorOccurred && pdfFile != null && pdfFile.exists()) {
            		try {
            			pdfFile.delete();
    				} catch (Exception e) {
    					logger.error("Cannot delete temporary pdf file: " + pdfFile.getAbsolutePath(), e);
    				}
            	}
            	writeUserActivityLog(AgnUtils.getAdmin(request), "export workflow", "WorkflowID: " + workflowId);
            }
        }
        return null;
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("fromListPage", request.getParameter("fromListPage"));
        loadWorkflow((ComWorkflowForm) form, request, new ActionMessages());
        return mapping.findForward("delete");
    }

    public ActionForward deleteconfirm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        Workflow workflow = workflowService.getWorkflow(workflowForm.getWorkflowId(), AgnUtils.getCompanyID(request));
        if (workflow.getStatus() != WorkflowStatus.STATUS_ACTIVE && workflow.getStatus() != WorkflowStatus.STATUS_TESTING) {
            workflowService.deleteWorkflow(workflowForm.getWorkflowId(), AgnUtils.getCompanyID(request));
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
            saveMessages(request, messages);

            writeUserActivityLog(AgnUtils.getAdmin(request), "delete campaign", getWorkflowDescription(workflow));
        } else {
            ActionMessages errors = new ActionErrors();
            errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_MESSAGE, new ActionMessage("error.workflow.nodesShouldBeDisabledBeforeDeleting"));
            saveErrors(request, errors);
        }
        return list(mapping, form, request, response);
    }

    public ActionForward bulkDeleteConfirm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (validateSelectedItems((ComWorkflowForm) form, request)) {
            return mapping.findForward("bulkDeleteConfirm");
        } else {
            return mapping.findForward("messages");
        }
    }

    public ActionForward bulkDelete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final ComAdmin admin = AgnUtils.getAdmin(request);
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        Set<Integer> workflowIdsToDelete = new HashSet<>();
        List<Workflow> workflows = workflowService.getWorkflowsByIds(workflowForm.getBulkIds(), AgnUtils.getCompanyID(request));

        for (Workflow workflow : workflows) {
            if (workflow.getStatus() != WorkflowStatus.STATUS_ACTIVE && workflow.getStatus() != WorkflowStatus.STATUS_TESTING) {
                workflowIdsToDelete.add(workflow.getWorkflowId());
            }
        }

        if (workflowIdsToDelete.size() != workflowForm.getBulkIds().size()) {
            ActionMessages errors = new ActionErrors();
            errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_MESSAGE, new ActionMessage("error.workflow.nodesShouldBeDisabledBeforeDeleting"));
            saveErrors(request, errors);
        } else {
            workflowService.bulkDelete(workflowIdsToDelete, AgnUtils.getCompanyID(request));
            ActionMessages messages = new ActionMessages();
            messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.selection.deleted"));
            saveMessages(request, messages);

            for (Workflow workflow : workflows) {
                if (workflowIdsToDelete.contains(workflow.getWorkflowId())) {
                    writeUserActivityLog(admin, "delete campaign", getWorkflowDescription(workflow));
                }
            }
        }

        return list(mapping, form, request, response);
    }

	public ActionForward bulkDeactivateConfirm(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (validateSelectedItems((ComWorkflowForm) form, request)) {
            return mapping.findForward("bulkDeactivateConfirm");
        } else {
            return mapping.findForward("messages");
        }
    }

    public ActionForward bulkDeactivate(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        final ComAdmin admin = AgnUtils.getAdmin(request);
        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        Set<Integer> workflowIdsToDeactivate = new HashSet<>();
        List<Workflow> workflows = workflowService.getWorkflowsByIds(workflowForm.getBulkIds(), AgnUtils.getCompanyID(request));

        for (Workflow workflow : workflows) {
            switch (workflow.getStatus()) {
                case STATUS_ACTIVE:
                case STATUS_TESTING:
                    workflowIdsToDeactivate.add(workflow.getWorkflowId());
                    break;
                default:break;
            }
        }

        workflowService.bulkDeactivate(workflowIdsToDeactivate, AgnUtils.getCompanyID(request));

        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("default.changes_saved"));
        saveMessages(request, messages);

        for (Workflow workflow : workflows) {
            if (workflowIdsToDeactivate.contains(workflow.getWorkflowId())) {
                switch (workflow.getStatus()) {
                    case STATUS_ACTIVE:
                        writeUserActivityLog(admin, "do deactivate campaign", getWorkflowDescription(workflow));
                        break;

                    case STATUS_TESTING:
                        writeUserActivityLog(admin, "do stop test campaign", getWorkflowDescription(workflow));
                        break;
                    default:break;
                }
            }
        }

        return list(mapping, form, request, response);
    }

    private boolean validateSelectedItems(ComWorkflowForm form, HttpServletRequest request) {
        if (form.getBulkIds().size() == 0) {
            ActionMessages errors = new ActionMessages();
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("bulkAction.nothing.workflow"));
            saveErrors(request, errors);
            return false;
        }
        return true;
    }

	public ActionForward getMailingLinks(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		int mailingId = NumberUtils.toInt(request.getParameter("mailingId"), -1);
		Map<Integer, String> links = workflowService.getMailingLinks(mailingId, AgnUtils.getCompanyID(request));

        JSONObject orderedLinks = new JSONObject();
        int index = 0;

        for (Map.Entry<Integer, String> entry : links.entrySet()) {
            JSONObject data = new JSONObject();

            data.element("id", entry.getKey());
            data.element("url", entry.getValue());

            orderedLinks.element(Integer.toString(index++), data);
        }

        HttpUtils.responseJson(response, orderedLinks);
		return null;
	}

    public ActionForward getMailingsByWorkStatus(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int companyId = AgnUtils.getCompanyID(request);
        
	    String mailingTypes = request.getParameter("mailingTypes");
	    if(StringUtils.isEmpty(mailingTypes)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    } else {
		    List<Integer> mailingTypeList = com.agnitas.reporting.birt.external.utils.StringUtils.buildListFormCommaSeparatedValueString(mailingTypes);
		    String status = request.getParameter("status");
		    String mailingStatus = request.getParameter("mailingStatus");
		    boolean takeMailsForPeriod = Boolean.parseBoolean(request.getParameter("takeMailsForPeriod"));
		    int mailingId = NumberUtils.toInt(request.getParameter("mailingId"));
		
		    int parentMailingId = NumberUtils.toInt(request.getParameter("parentMailingId"));
		
		    String sort = request.getParameter("sort");
		    String order = request.getParameter("order");
		
		    List<Map<String, Object>> mailings = workflowService.getAllMailings(companyId, mailingTypeList,
				    status, mailingStatus, takeMailsForPeriod, sort, order);
		
		    String mailingsCampaign = request.getParameter("mailingsInCampaign");
		
		    if (StringUtils.isNotBlank(mailingsCampaign)) {
			    mailings.addAll(workflowService.getMailings(companyId, mailingsCampaign));
		    }
		
		    boolean parentNotInList = true;
		    for (Map<String, Object> mailing : mailings) {
			    if (((Number) mailing.get("MAILING_ID")).intValue() == parentMailingId) {
				    parentNotInList = false;
				    break;
			    }
		    }
		
		    if (parentNotInList && (parentMailingId != 0) && (parentMailingId != mailingId)) {
			    Map<String, Object> mailingData = workflowService.getMailingWithWorkStatus(parentMailingId, companyId);
			    mailings.add(mailingData);
		    }
		
		    ObjectMapper objectMapper = new ObjectMapper();
		    objectMapper.registerModule(new SimpleModule("", Version.unknownVersion()) {
				private static final long serialVersionUID = -4563093834218254808L;

				@Override
			    public void setupModule(SetupContext context) {
				    SimpleSerializers serializers = new SimpleSerializers();
				    serializers.addSerializer(String.class, new UpperCaseKeySerializer());
				    context.addKeySerializers(serializers);
			    }
		    });
		
		    HttpUtils.responseJson(response, objectMapper.writeValueAsString(mailings));
	    }
	    
        return null;
    }

    public ActionForward getAllMailingSorted(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String sortField = request.getParameter("sortField");
        String sortDirection = request.getParameter("sortDirection");
        List<LightweightMailing> mailings = workflowService.getAllMailingsSorted(AgnUtils.getCompanyID(request), sortField, sortDirection);

        ObjectMapper objectMapper = new ObjectMapper();
        HttpUtils.responseJson(response, objectMapper, mailings);
        return null;
    }

	public ActionForward getWorkflowContent(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);

        assert (admin != null);

		int workflowId = NumberUtils.toInt(request.getParameter("workflowId"));

        // We either reset all the icon content/settings or have to clone used mailings (if any).
        boolean isWithContent = Boolean.parseBoolean(request.getParameter("isWithContent"));

        List<WorkflowIcon> icons = workflowService.getIconsForClone(admin, workflowId, isWithContent);
        if (icons == null) {
            icons = Collections.emptyList();
        }

		HttpUtils.responseJson(response, workflowDataParser.serializeWorkflowIcons(icons));

		return null;
	}

    public ActionForward getSampleWorkflowContent(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String sampleWorkflowType = request.getParameter("type");
		List<WorkflowIcon> sample = ComSampleWorkflowFactory.createSampleWorkflow(sampleWorkflowType);
		HttpUtils.responseJson(response, sample == null ? "[]" : workflowDataParser.serializeWorkflowIcons(sample));
		return null;
	}

	public ActionForward loadStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		int workflowId = NumberUtils.toInt(request.getParameter("workflowId"));
		Map<Integer, List<String>> stats = workflowStatisticsService.getWorkflowStats(workflowId, AgnUtils.getCompanyID(request), AgnUtils.getLocale(request));
		ObjectMapper objectMapper = new ObjectMapper();
		HttpUtils.responseJson(response, objectMapper, stats);
		return null;
	}

	public ActionForward getCurrentAdminTime(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		GregorianCalendar calendar = new GregorianCalendar(AgnUtils.getTimeZone(request));
		int hours  = calendar.get(GregorianCalendar.HOUR_OF_DAY);
		int minutes = calendar.get(GregorianCalendar.MINUTE);
        HttpUtils.responseJson(response, "{\"hour\":" + hours + ", \"minute\":" + minutes + "}");
		return null;
	}

    public ActionForward getMailingContent(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int mailingId = NumberUtils.toInt(request.getParameter("mailingId"), -1);
        
        int companyId = AgnUtils.getCompanyID(request);
        ComMailing mailing = workflowService.getMailing(mailingId, companyId);
	
	    Map<String, Object> mailingData = new HashMap<>();
	    if(mailing.getId() > 0) {
	        mailingData.put("mailinglistId", mailing.getMailinglistID());
	        mailingData.put("targetGroupIds", mailing.getTargetGroups());
	        mailingData.put("planDate", mailing.getPlanDate());
	        mailingData.put("campaignId", mailing.getCampaignID());
	
	        int mailingType = mailing.getMailingType();
	        mailingData.put("mailingType", mailingType);
	
	        // Get send date.
	        DeliveryStat deliveryStat = deliveryStatService.getDeliveryStats(companyId, mailingId, mailingType);
	        if (deliveryStat.getScheduledSendTime() != null) {
		        Calendar sendDate = DateUtilities.calendar(deliveryStat.getScheduledSendTime(), AgnUtils.getTimeZone(request));
		        mailingData.put("sendDate", sendDate.getTime());
		        mailingData.put("sendHour", sendDate.get(Calendar.HOUR));
		        mailingData.put("sendMinute", sendDate.get(Calendar.MINUTE));
	        } else {
		        mailingData.put("sendDate", null);
		        mailingData.put("sendHour", null);
		        mailingData.put("sendMinute", null);
	        }
	
	        int splitID = mailing.getSplitID();
	        String name = splitID <= 0 ? "" : workflowService.getTargetSplitName(splitID);
		
	        String splitBase = "";
	        String splitPart = "";
	        
	        if (StringUtils.isNotEmpty(name)) {
		        for (String prefix : TargetLight.LIST_SPLIT_PREFIXES) {
			        if (name.startsWith(prefix)) {
				        name = name.substring(prefix.length());
				
				        int splitPartPos = name.lastIndexOf('_');
				        if (splitPartPos > 0) {
					        splitBase = name.substring(0, splitPartPos);
					        splitPart = name.substring(splitPartPos + 1);
				        }
				        break;
			        }
		        }
	        }
	
	        mailingData.put("splitBase", splitBase);
	        mailingData.put("splitPart", splitPart);
        }
        
        ObjectMapper objectMapper = new ObjectMapper();
        HttpUtils.responseJson(response, objectMapper.writeValueAsString(mailingData));
        return null;
    }

    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionMessages messages = new ActionMessages();
        final ComAdmin admin = AgnUtils.getAdmin(request);
        final boolean isWithContent = Boolean.parseBoolean(request.getParameter("isWithContent"));

        ComWorkflowForm workflowForm = (ComWorkflowForm) form;
        Workflow existedWorkflow = workflowService.getWorkflow(workflowForm.getWorkflowId(), admin.getCompanyID());
        Workflow workflow = workflowService.copyWorkflow(admin, workflowForm.getWorkflowId(), isWithContent);

        writeUserActivityLog(admin, "copy campaign " + (isWithContent ? "with" : "without") + " content",
                getWorkflowDescription(existedWorkflow) + " copied as " + getWorkflowDescription(workflow));

        workflowForm.setWorkflow(workflow);
        workflowForm.setWorkflowId(workflow.getWorkflowId());
        loadWorkflow(workflowForm, request, messages);
        prepareViewPage(request, workflowForm);

        if (!messages.isEmpty()) {
            saveMessages(request, messages);
        }

        return mapping.findForward("view");
    }

    public ActionForward validateDependency(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ComWorkflowForm form = (ComWorkflowForm) actionForm;

        WorkflowDependencyType type = WorkflowDependencyType.fromId(form.getType());

        // Don't use form.getWorkflowId because it doesn't reset on each request.
        int workflowId = NumberUtils.toInt(request.getParameter("workflowId"), -1);

        // A workflowId = 0 value is reserved for a new workflow.
        if (type == null || workflowId < 0) {
            // Type parameter is missing or invalid.
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } else {
            WorkflowDependency dependency = null;

            if (form.getEntityId() > 0) {
                dependency = type.forId(form.getEntityId());
            } else if (StringUtils.isNotEmpty(form.getEntityName())) {
                dependency = type.forName(form.getEntityName());
            }

            if (dependency == null) {
                // Either identifier or a name is required.
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                JSONObject data = new JSONObject();
                data.element("valid", workflowService.validateDependency(AgnUtils.getCompanyID(request), workflowId, dependency));
                HttpUtils.responseJson(response, data);
            }
        }

        return null;
    }

    private void prepareViewPage(HttpServletRequest request, ComWorkflowForm form) throws Exception {
		// @todo we need to think whether we need to set all that to request or is it better to get that by ajax requests when it is needed
        ComAdmin admin = AgnUtils.getAdmin(request);
        int companyId = admin.getCompanyID();

        request.setAttribute("profileFields", workflowService.getProfileFields(companyId));
        request.setAttribute("profileFieldsHistorized", workflowService.getHistorizedProfileFields(companyId));
        request.setAttribute("isMailtrackingActive", companyDao.isMailtrackingActive(companyId));
		request.setAttribute("admins", workflowService.getAdmins(companyId));
		request.setAttribute("allReports", workflowService.getAllReports(companyId));
		request.setAttribute("allTargets", workflowService.getAllTargets(companyId));
		request.setAttribute("allMailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        request.setAttribute("campaigns", campaignDao.getCampaignList(companyId, "lower(shortname)", 1));
        request.setAttribute("allUserForms", workflowService.getAllUserForms(companyId));
        request.setAttribute("localeDateNTimePattern", getLocaleDateNTimeFormat(request).toPattern());
        request.setAttribute("localeDatePattern", AgnUtils.getLocaleDateFormat(request).toPattern());
        request.setAttribute("adminTimezone", admin.getAdminTimezone());
		request.setAttribute("allWorkflows", workflowService.getWorkflowsOverview(companyId));
        request.setAttribute("hasDeepTrackingTables", workflowService.hasCompanyDeepTrackingTables(companyId));
        request.setAttribute("allAutoImports", autoImportService == null ? new ArrayList<AutoImportLight>() : autoImportService.listAutoImports(companyId));
        request.setAttribute("allAutoExports", autoExportService == null ? new ArrayList<AutoExport>() : autoExportService.getAutoExportsOverview(admin));
        request.setAttribute("allMailings", workflowService.getAllMailings(companyId));
        request.setAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, companyId));
    }

    private void loadWorkflow(ComWorkflowForm workflowForm, HttpServletRequest request, ActionMessages errors) {
        workflowForm.setUsingActivatedWorkflow("");
        workflowForm.setUsingActivatedWorkflowName("");
        workflowForm.setPartOfActivatedWorkflow("");
        workflowForm.setPartOfActivatedWorkflowName("");
        Workflow workflow = workflowService.getWorkflow(workflowForm.getWorkflowId(), AgnUtils.getCompanyID(request));
        if (INCOMPLETE_WORKFLOW_NAME.equals(workflow.getShortname())) {
            workflow.setShortname("");
        }
        workflowForm.setWorkflow(workflow);

        switch (workflowForm.getWorkflow().getStatus()) {
            case STATUS_ACTIVE:
                workflowForm.setNewStatus(WorkflowStatus.STATUS_INACTIVE.name());
                break;

            case STATUS_OPEN:
            case STATUS_INACTIVE:
            case STATUS_TESTED:
                workflowForm.setNewStatus(WorkflowStatus.STATUS_ACTIVE.name());
                break;

            case STATUS_NONE:
            case STATUS_TESTING:
            case STATUS_COMPLETE:
                workflowForm.setNewStatus(WorkflowStatus.STATUS_NONE.name());
                break;
        }

        String forwardParams = request.getParameter("forwardParams");
        if (StringUtils.isEmpty(forwardParams)) {
            workflowForm.setWorkflowUndoHistoryData("[]");
        }

        List<WorkflowIcon> icons = workflow.getWorkflowIcons();
        // check if there are deleted mailings used in current workflow
        if (workflowService.hasDeletedMailings(icons, workflow.getCompanyId()) && errors != null) {
            errors.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.workflow.containsDeletedContent"));
        }

        workflowForm.setSchema(workflowDataParser.serializeWorkflowIcons(icons));
    }

	public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setAutoImportService(AutoImportService autoImportService) {
        this.autoImportService = autoImportService;
    }

    public void setAutoExportService(AutoExportService autoExportService) {
        this.autoExportService = autoExportService;
    }

    public void setWorkflowDataParser(ComWorkflowDataParser workflowDataParser) {
        this.workflowDataParser = workflowDataParser;
    }

    public ComCampaignDao getCampaignDao() {
        return campaignDao;
    }

    public void setCampaignDao(ComCampaignDao campaignDao) {
        this.campaignDao = campaignDao;
    }

    @Required
    public void setDeliveryStatService(ComMailingDeliveryStatService deliveryStatService) {
        this.deliveryStatService = deliveryStatService;
    }

	public void setWorkflowActivationService(ComWorkflowActivationService workflowActivationService) {
		this.workflowActivationService = workflowActivationService;
	}

	private boolean isKnownForward(String forwardName) {
        if (forwardName == null) {
            return false;
        }

        switch (forwardName) {
            case FORWARD_USERFORM_CREATE:
            case FORWARD_USERFORM_EDIT:
            case FORWARD_REPORT_CREATE:
            case FORWARD_REPORT_EDIT:
            case FORWARD_TARGETGROUP_CREATE:
            case FORWARD_TARGETGROUP_CREATE_QB:
            case FORWARD_TARGETGROUP_EDIT:
            case FORWARD_TARGETGROUP_EDIT_QB:
            case FORWARD_ARCHIVE_CREATE:
            case FORWARD_MAILING_CREATE:
            case FORWARD_MAILING_EDIT:
            case FORWARD_MAILING_COPY:
            case FORWARD_AUTOIMPORT_CREATE:
            case FORWARD_AUTOIMPORT_EDIT:
            case FORWARD_AUTOEXPORT_CREATE:
            case FORWARD_AUTOEXPORT_EDIT:
                return true;

            default:
                return false;
        }
    }

	private ActionForward getForward(ActionMapping mapping, HttpServletRequest request, String forwardName, int workflowId, List<WorkflowIcon> icons) {
        if (isKnownForward(forwardName)) {
            ActionRedirect redirect = new ActionRedirect(mapping.findForward(forwardName));

            Map<String, String> paramsMap = AgnUtils.getParamsMap(request.getParameter("forwardParams"));

            // Validate and normalize nodeId parameter.
            int iconId = NumberUtils.toInt(paramsMap.get(WORKFLOW_NODE_ID));
            if (iconId > 0 && validateIconId(icons, iconId)) {
                paramsMap.put(WORKFLOW_NODE_ID, Integer.toString(iconId));
            } else {
                paramsMap.put(WORKFLOW_NODE_ID, "");
            }

            redirect.addParameter(ComWorkflowAction.WORKFLOW_FORWARD_PARAMS, AgnUtils.getParamsString(paramsMap));
            redirect.addParameter(ComWorkflowAction.WORKFLOW_ID, workflowId);

            String itemId = request.getParameter("forwardTargetItemId");
            if (StringUtils.isNotEmpty(itemId)) {
                redirect.addParameter(ComWorkflowAction.WORKFLOW_FORWARD_TARGET_ITEM_ID, itemId);
            }
            return redirect;
        } else {
            return mapping.findForward("view");
        }
    }

    private boolean validateIconId(List<WorkflowIcon> existingIcons, int iconId) {
        for (WorkflowIcon icon : existingIcons) {
            if (iconId == icon.getId()) {
                return true;
            }
        }

        return false;
    }

    private SimpleDateFormat getLocaleDateNTimeFormat(HttpServletRequest request) {
        Locale locale = AgnUtils.getLocale(request);
        return (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
    }

    /**
     * Get workflow description to be passed to {@link #writeUserActivityLog(com.agnitas.beans.ComAdmin, String, String)}.
     * @param workflow a workflow entity.
     * @return a description string.
     */
    private String getWorkflowDescription(Workflow workflow) {
        return workflow.getShortname() + " (" + workflow.getWorkflowId() + ")";
    }

    private void writeWorkflowCreationLog(ComAdmin admin, Workflow workflow) {
        writeUserActivityLog(admin, "create campaign", getWorkflowDescription(workflow));
    }

    /**
     * Write to a User Activity Log a changes made to the workflow.
     * @param admin a current user entity.
     * @param oldWorkflow a workflow entity before the changes.
     * @param newWorkflow a workflow entity after the changes.
     */
    private void writeWorkflowChangeLog(ComAdmin admin, Workflow oldWorkflow, Workflow newWorkflow) {
        final String description = getWorkflowDescription(oldWorkflow);

        boolean anyLogEntry = false;

        final String oldName = oldWorkflow.getShortname();
        final String newName = newWorkflow.getShortname();
        if (!StringUtils.equals(oldName, newName)) {
            writeUserActivityLog(admin, "edit campaign", description + " renamed to " + newName);
            anyLogEntry = true;
        }

        if (writeWorkflowFieldChangeLog(admin, description + ". Description ", oldWorkflow.getDescription(), newWorkflow.getDescription())) {
            anyLogEntry = true;
        }

        if(writeWorkflowIconsChangeLog(newWorkflow, oldWorkflow, admin)) {
            anyLogEntry = true;
        }

        if(writeWorkflowStartChangeLog(newWorkflow, oldWorkflow, admin)) {
            anyLogEntry = true;
        }

        if(writeWorkflowEndChangeLog(newWorkflow, oldWorkflow, admin)) {
            anyLogEntry = true;
        }

        if(writeWorkflowStatusChangeLog(newWorkflow, oldWorkflow, admin)) {
            anyLogEntry = true;
        }

        if (!anyLogEntry) {
            writeUserActivityLog(admin, "edit campaign", description);
        }
    }

    private boolean writeWorkflowIconsChangeLog(Workflow newWorkflow, Workflow oldWorkflow, ComAdmin admin) {
        List<WorkflowIcon> newIcons = newWorkflow.getWorkflowIcons();
        List<WorkflowIcon> oldIcons = oldWorkflow.getWorkflowIcons();

        boolean isChanged = false;

        for (WorkflowIcon newIcon : newIcons) {

            if(newIcon.getType() == WorkflowIconType.Constants.START_ID ||
                    newIcon.getType() == WorkflowIconType.Constants.STOP_ID) { // start and stop icon changes will be logged form workflow object fields in writeWorkflowChangeLog method.
                continue;
            }

            for (WorkflowIcon oldIcon : oldIcons) {
                if(oldIcon.getId() == newIcon.getId()) {
                    if(writeWorkflowIconChangeLog(newIcon, oldIcon, newWorkflow, admin)) {
                        isChanged = true;
                    }

                    if(!newIcon.getConnections().equals(oldIcon.getConnections())) {
                        String iconName = getIconNameByTypeId(newIcon);
                        writeUserActivityLog(admin, "edit campaign " + iconName + " path", getWorkflowDescription(newWorkflow));
                        isChanged = true;
                    }
                }
            }
        }

        return isChanged;
    }

    private boolean writeWorkflowIconChangeLog(WorkflowIcon newIcon, WorkflowIcon oldIcon, Workflow workflow, ComAdmin comAdmin) {
        boolean isChanged = false;

        if(newIcon.getType() == WorkflowIconType.Constants.RECIPIENT_ID) {
            WorkflowRecipientImpl newRecipientIcon = (WorkflowRecipientImpl) newIcon;
            WorkflowRecipientImpl oldRecipientIcon = (WorkflowRecipientImpl) oldIcon;

            if(writeWorkflowRecipientTargetChangeLog(newRecipientIcon, oldRecipientIcon, comAdmin, workflow)) {
                isChanged = true;
            }

            if(writeWorkflowRecipientMailingListLog(newRecipientIcon, oldRecipientIcon, comAdmin, workflow)) {
                isChanged = true;
            }

        }

        if(!isChanged && !newIcon.equals(oldIcon)) {
            String iconName = getIconNameByTypeId(newIcon);
            writeUserActivityLog(comAdmin, "edit campaign " + iconName, getWorkflowDescription(workflow));
            isChanged = true;
        }

        return isChanged;
    }

    private String getIconNameByTypeId(WorkflowIcon icon) {
        switch (icon.getType()) {
            case WorkflowIconType.Constants.START_ID: return WorkflowIconType.Constants.START_VALUE;
            case WorkflowIconType.Constants.STOP_ID: return WorkflowIconType.Constants.STOP_VALUE;
            case WorkflowIconType.Constants.DECISION_ID: return WorkflowIconType.Constants.DECISION_VALUE;
            case WorkflowIconType.Constants.DEADLINE_ID: return WorkflowIconType.Constants.DEADLINE_VALUE;
            case WorkflowIconType.Constants.PARAMETER_ID: return WorkflowIconType.Constants.PARAMETER_VALUE;
            case WorkflowIconType.Constants.REPORT_ID: return WorkflowIconType.Constants.REPORT_VALUE;
            case WorkflowIconType.Constants.RECIPIENT_ID: return WorkflowIconType.Constants.RECIPIENT_VALUE;
            case WorkflowIconType.Constants.ARCHIVE_ID: return WorkflowIconType.Constants.ARCHIVE_VALUE;
            case WorkflowIconType.Constants.FORM_ID: return WorkflowIconType.Constants.FORM_VALUE;
            case WorkflowIconType.Constants.MAILING_ID: return WorkflowIconType.Constants.MAILING_VALUE;
            case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID: return WorkflowIconType.Constants.ACTION_BASED_MAILING_VALUE;
            case WorkflowIconType.Constants.DATE_BASED_MAILING_ID: return WorkflowIconType.Constants.DATE_BASED_MAILING_VALUE;
            case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID: return WorkflowIconType.Constants.FOLLOWUP_MAILING_VALUE;
            case WorkflowIconType.Constants.IMPORT_ID: return WorkflowIconType.Constants.IMPORT_VALUE;
            case WorkflowIconType.Constants.EXPORT_ID: return WorkflowIconType.Constants.EXPORT_VALUE;
            default: throw new RuntimeException("Unknown icon type: " + icon.getType());
        }
    }

    private boolean writeWorkflowRecipientMailingListLog(WorkflowRecipientImpl newIcon, WorkflowRecipientImpl oldIcon, ComAdmin admin, Workflow workflow) {
        boolean isChanged = newIcon.getMailinglistId() != oldIcon.getMailinglistId();

        if(isChanged) {
            writeUserActivityLog(admin, "edit campaign recipient",
                    getWorkflowDescription(workflow) + "; recipient icon with id: " + newIcon.getId() + "; mailing list changed");
        }

        return isChanged;
    }

    private boolean writeWorkflowRecipientTargetChangeLog(WorkflowRecipientImpl newIcon, WorkflowRecipientImpl oldIcon, ComAdmin admin, Workflow workflow) {
        List<Integer> oldTargets = ListUtils.emptyIfNull(oldIcon.getTargets());
        List<Integer> newTargets = ListUtils.emptyIfNull(newIcon.getTargets());

        if (!oldTargets.equals(newTargets)) {
            writeUserActivityLog(admin, "edit campaign recipient",
                    getWorkflowDescription(workflow) + "; recipient icon with id: " + newIcon.getId() + "; target group changed");
            return true;
        }

        return false;
    }

    private boolean writeWorkflowStartChangeLog(Workflow newWorkflow, Workflow oldWorkflow, ComAdmin admin) {
        boolean isDateChanged = newWorkflow.getGeneralStartDate() != null && oldWorkflow.getGeneralStartDate() != null &&
                !newWorkflow.getGeneralStartDate().equals(oldWorkflow.getGeneralStartDate());

        boolean isEventChanged = newWorkflow.getGeneralStartEvent() != null && oldWorkflow.getGeneralStartEvent() != null &&
                !newWorkflow.getGeneralStartEvent().equals(oldWorkflow.getGeneralStartEvent());

        boolean isReactionChanged = newWorkflow.getGeneralStartReaction() != null && oldWorkflow.getGeneralStartReaction() != null &&
                !newWorkflow.getGeneralStartReaction().equals(oldWorkflow.getGeneralStartReaction());

        boolean isChanged = isDateChanged || isEventChanged || isReactionChanged;

        if(isChanged) {
            writeUserActivityLog(admin, "workflow start changed", getWorkflowDescription(newWorkflow));
        }

        return isChanged;
    }

    private boolean writeWorkflowEndChangeLog(Workflow newWorkflow, Workflow oldWorkflow, ComAdmin admin) {
        boolean isDateChanged = newWorkflow.getGeneralEndDate() != null && oldWorkflow.getGeneralEndDate() != null &&
                !newWorkflow.getGeneralEndDate().equals(oldWorkflow.getGeneralEndDate());

        boolean isTypeChanged = newWorkflow.getEndType() != null && oldWorkflow.getEndType() != null &&
                !newWorkflow.getEndType().equals(oldWorkflow.getEndType());

        boolean isChanged = isDateChanged || isTypeChanged;

        if(isChanged) {
            writeUserActivityLog(admin, "workflow end changed", getWorkflowDescription(newWorkflow));
        }

        return isChanged;
    }

    private boolean writeWorkflowFieldChangeLog(ComAdmin admin, String descriptionPrefix, String oldValue, String newValue) {
        oldValue = StringUtils.trimToNull(oldValue);
        newValue = StringUtils.trimToNull(newValue);

        if (StringUtils.equals(oldValue, newValue)) {
            return false;
        } else {
            String description;
            if (StringUtils.isNotEmpty(newValue)) {
                if (StringUtils.isNotEmpty(oldValue)) {
                    description = "changed from " + oldValue + " to " + newValue;
                } else {
                    description = "changed to " + newValue;
                }
            } else {
                description = "removed";
            }
            writeUserActivityLog(admin, "edit campaign", descriptionPrefix + description);
            return true;
        }
    }

    public ActionForward getMailingThumbnail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int companyId = AgnUtils.getCompanyID(request);
        int mailingId = NumberUtils.toInt(request.getParameter("mailingId"), -1);
        if(mailingId == -1) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        int componentId = componentDao.getImageComponent(companyId, mailingId, MailingComponentType.ThumbnailImage.getCode());

        HttpUtils.responseJson(response, Integer.toString(componentId));
        return null;
    }

    public static void updateForwardParameters(HttpServletRequest req) {
        updateForwardParameters(req, true);
    }

    public static void updateForwardParameters(HttpServletRequest req, boolean checkKeepForward) {
        WorkflowParameters workflowParameters = new WorkflowParameters();

        boolean override = true;
        if (checkKeepForward) {
            String keepForward = req.getParameter(WORKFLOW_KEEP_FORWARD);
            if (Boolean.valueOf(keepForward)) {
                override = false;
            }
        }

        String targetItemId = req.getParameter(WORKFLOW_FORWARD_TARGET_ITEM_ID);
        workflowParameters.setWorkflowForwardTargetItemId(NumberUtils.toInt(targetItemId));

        String workflowId = req.getParameter(WORKFLOW_ID);
        workflowParameters.setWorkflowId(NumberUtils.toInt(workflowId));

        String forwardParams = req.getParameter(WORKFLOW_FORWARD_PARAMS);
        workflowParameters.setWorkflowForwardParams(StringUtils.trimToEmpty(forwardParams));
        
        AgnUtils.saveWorkflowForwardParamsToSession(req, workflowParameters, override);
    }

    private List<WorkflowIcon> getIcons(ComWorkflowForm form) {
        String schema = form.getSchema();

        if (StringUtils.isNotBlank(schema)) {
            return workflowDataParser.deSerializeWorkflowIconsList(schema);
        }

        return Collections.emptyList();
    }

    public void setValidationService(ComWorkflowValidationService validationService) {
        this.validationService = validationService;
    }

    public void setWorkflowStatisticsService(ComWorkflowStatisticsService workflowStatisticsService) {
        this.workflowStatisticsService = workflowStatisticsService;
    }

    public void setComponentDao(ComMailingComponentDao componentDao) {
        this.componentDao = componentDao;
    }

    public void setGenerationPDFService(GenerationPDFService generationPDFService) {
        this.generationPDFService = generationPDFService;
    }

    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }
    
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
    public void setMailinglistService(ComMailinglistService mailinglistService) {
        this.mailinglistService = mailinglistService;
    }
	   
    @Required
    public final void setMailinglistApprovalService(final MailinglistApprovalService service) {
    	this.mailinglistApprovalService = Objects.requireNonNull(service, "Mailinglist approval service is null");
    }

    @Required
    public void setMailingService(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Required
    public void setWebStorage(WebStorage webStorage) {
        this.webStorage = webStorage;
    }

    private static class UpperCaseKeySerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String key, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeFieldName(key.toUpperCase());
        }
    }
}
