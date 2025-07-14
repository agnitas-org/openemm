/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Target;
import com.agnitas.beans.factory.TargetFactory;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.core.beans.Dependent;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailingcontent.dto.ContentBlockAndMailingMetaData;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.TargetUtils;
import com.agnitas.emm.core.target.beans.TargetComplexityGrade;
import com.agnitas.emm.core.target.beans.TargetGroupDependentType;
import com.agnitas.emm.core.target.eql.EqlDetailedAnalysisResult;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.eql.parser.EqlSyntaxError;
import com.agnitas.emm.core.target.exception.TargetGroupNotCompatibleWithContentBlockException;
import com.agnitas.emm.core.target.exception.TargetGroupTooLargeException;
import com.agnitas.emm.core.target.exception.UnknownTargetGroupIdException;
import com.agnitas.emm.core.target.form.TargetDependentsListForm;
import com.agnitas.emm.core.target.form.TargetEditForm;
import com.agnitas.emm.core.target.form.validator.TargetEditFormValidator;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizationException;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizer;
import com.agnitas.emm.core.target.web.util.FormHelper;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.exception.DetailedRequestErrorException;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class QueryBuilderTargetController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(QueryBuilderTargetController.class);

    private static final String TARGET_SAVING_ERROR_KEY = "error.target.saving";
    private static final String CHANGES_SAVED_MSG_KEY = "default.changes_saved";
    private static final String MESSAGES_VIEW = "messages";

    private final ConfigService configService;
    private final MailinglistApprovalService mailinglistApprovalService;
    protected final TargetService targetService;
    private final RecipientService recipientService;
    private final EditorContentSynchronizer editorContentSynchronizer;
    private final EqlFacade eqlFacade;
    private final TargetCopyService targetCopyService;
    private final TargetFactory targetFactory;
    private final UserActivityLogService userActivityLogService;
    private final BirtStatisticsService birtStatisticsService;
    private final WebStorage webStorage;
    private final GridServiceWrapper gridService;
    private final TargetEditFormValidator editFormValidator;

    public QueryBuilderTargetController(MailinglistApprovalService mailinglistApprovalService, TargetService targetService,
                                        RecipientService recipientService, EditorContentSynchronizer editorContentSynchronizer,
                                        EqlFacade eqlFacade, TargetCopyService targetCopyService, TargetFactory targetFactory,
                                        UserActivityLogService userActivityLogService, BirtStatisticsService birtStatisticsService,
                                        WebStorage webStorage, GridServiceWrapper gridService, TargetEditFormValidator editFormValidator, ConfigService configService) {
        this.configService = configService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.targetService = targetService;
        this.recipientService = recipientService;
        this.editorContentSynchronizer = editorContentSynchronizer;
        this.eqlFacade = eqlFacade;
        this.targetCopyService = targetCopyService;
        this.targetFactory = targetFactory;
        this.userActivityLogService = userActivityLogService;
        this.birtStatisticsService = birtStatisticsService;
        this.webStorage = webStorage;
        this.gridService = gridService;
        this.editFormValidator = editFormValidator;
    }

    @RequestMapping("/{targetId:\\d+}/view.action")
    public String view(@PathVariable int targetId, Admin admin, Model model, @ModelAttribute("targetEditForm") TargetEditForm form,
                       Popups popups, HttpServletRequest request) {
        WorkflowUtils.updateForwardParameters(request);

        if (targetId > 0 && form.getPreviousViewFormat() == null) {
            boolean exists = fillFormIfTargetExists(admin, form);
            if (!exists) {
                logger.warn("Target group does not exists. Company ID: {} Target ID: {}", admin.getCompanyID(), form.getTargetId());
                return "redirect:/target/list.action?restoreSort=true";
            }
        }
        return form.getViewFormatOrDefault() == TargetgroupViewFormat.QUERY_BUILDER
            ? viewQB(admin, model, form, popups)
            : viewEQL(admin, model, form, popups);
    }

    @RequestMapping("/{targetId:\\d+}/save.action")
    public String save(Admin admin,
                       @ModelAttribute("targetEditForm") TargetEditForm form,
                       @RequestParam(required = false) boolean showStatistic,
                       Popups popups, RedirectAttributes redirectAttributes,
                       HttpSession session)
            throws UnknownTargetGroupIdException {

        int mailinglistId = form.getMailinglistId();
        int workflowId = WorkflowParametersHelper.getWorkflowIdFromSession(session);
        boolean redirectRequired = workflowId > 0;
        TargetgroupViewFormat previousFormat = form.getPreviousViewFormat();

        if (!isPreparedForSave(admin, form, popups, redirectRequired)) {
            return getErrorView(form.getTargetId(), redirectAttributes, session, redirectRequired);
        }

        int targetId = trySave(admin, form, popups);
        if (targetId <= 0 || popups.hasAlertPopups()) {
            return getErrorView(form.getTargetId(), redirectAttributes, session, redirectRequired);
        }

        analyzeComplexity(admin.getCompanyID(), targetId, popups);

        if (showStatistic) {
            if (!admin.isRedesignedUiUsed()) {
                redirectAttributes.addFlashAttribute("showStatistic", true);
                redirectAttributes.addFlashAttribute("statisticUrl", getReportUrl(admin, RequestContextHolder.getRequestAttributes().getSessionId(), targetId, mailinglistId));
            }

            redirectAttributes.addAttribute("mailinglistId", mailinglistId);
        }

        if (previousFormat != TargetEditForm.getDefaultViewFormat()) {
            redirectAttributes.addAttribute("viewFormat", previousFormat);
        }

        popups.success(CHANGES_SAVED_MSG_KEY);

        if (redirectRequired) {
            WorkflowParametersHelper.addEditedElemRedirectAttrs(redirectAttributes, session, targetId);
            return String.format("redirect:/workflow/%d/view.action", workflowId);
        }
        
        return redirectToView(targetId);
    }

    private String getErrorView(int targetId, RedirectAttributes ra, HttpSession session, boolean redirectRequired) {
        if (!redirectRequired) {
            return MESSAGES_VIEW;
        }
        WorkflowParametersHelper.addEditedElemRedirectAttrs(ra, session, targetId);
        return redirectToView(targetId);
    }

    private boolean isPreparedForSave(Admin admin, TargetEditForm form, Popups popups, boolean redirectRequired) {
        try {
            return doSavingPreparations(admin, form, popups);
        } catch (DetailedRequestErrorException ex) {
            if (!redirectRequired) {
                throw ex;
            }
            ex.getErrors().forEach(popups::alert);
            return false;
        }
    }

    @RequestMapping("/create.action")
    public String create(Admin admin, Model model, TargetEditForm form,
                         HttpServletRequest req, Popups popups) {
        WorkflowUtils.updateForwardParameters(req);

        form.setViewFormat(TargetgroupViewFormat.QUERY_BUILDER);

        model.addAttribute("isLocked", false);

        return viewQB(admin, model, form, popups);
    }

    @RequestMapping("/{targetId:\\d+}/lock.action")
    public String lock(@PathVariable int targetId, Admin admin, Popups popups) throws UnknownTargetGroupIdException {
        if (targetService.lockTargetGroup(admin.getCompanyID(), targetId)) {
            final Target target = targetService.getTargetGroup(targetId, admin.getCompanyID());
            String logAction = "do lock target group";
            String logDescription = target.getTargetName() + " (" + targetId + ")";
            userActivityLogService.writeUserActivityLog(admin, logAction, logDescription, logger);

            popups.success(CHANGES_SAVED_MSG_KEY);
        } else {
            popups.alert("error.target.not_saved");
        }

        return redirectToView(targetId);
    }

    @RequestMapping("/{targetId:\\d+}/unlock.action")
    public String unlock(@PathVariable int targetId, Admin admin, Popups popups) throws UnknownTargetGroupIdException {
        if (targetService.unlockTargetGroup(admin.getCompanyID(), targetId)) {
            final Target target = targetService.getTargetGroup(targetId, admin.getCompanyID());
            String logAction = "do unlock target group";
            String logDescription = target.getTargetName() + " (" + targetId + ")";
            userActivityLogService.writeUserActivityLog(admin, logAction, logDescription, logger);

            popups.success(CHANGES_SAVED_MSG_KEY);
        } else {
            popups.alert("error.target.not_saved");
        }

        return redirectToView(targetId);
    }

    @RequestMapping("/{targetId:\\d+}/copy.action")
    public final String copy(Admin admin, TargetEditForm form, Popups popups) throws Exception {
        int companyId = admin.getCompanyID();
        Target copiedTarget = form.getTargetId() != 0
                ? targetService.getTargetGroup(form.getTargetId(), companyId)
                : targetFactory.newTarget(companyId);
        copiedTarget.setId(0);
        copiedTarget.setCreationDate(null);
        copiedTarget.setChangeDate(null);

        final String newName = I18nString.getLocaleString("mailing.CopyOf", admin.getLocale()) +
                " " + copiedTarget.getTargetName();

        copiedTarget.setTargetName(newName);
        form.setShortname(newName);

        try {
            List<Message> messages = new LinkedList<>();

            int targetID = targetService.saveTarget(admin, copiedTarget, null, messages, userActivityLogService::writeUserActivityLog);

            messages.forEach(popups::alert);

            form.setTargetId(targetID);
        } catch (TargetGroupTooLargeException e) {
            popups.alert("error.target.too_large");

            form.setTargetId(e.getTargetId());
        }

        return redirectToView(form.getTargetId());
    }

    @GetMapping("/{targetId:\\d+}/stats.action")
    public String stats(@PathVariable int targetId, Admin admin, Model model, PaginationForm dependentsFilter) {
        loadEvaluateAttrs(model, targetId, 0, admin);
        loadDependentsAttrs(model, dependentsFilter, targetId, admin.getCompanyID());
        model.addAttribute("targetShortname", targetService.getTargetName(targetId, admin.getCompanyID()));
        model.addAttribute("isStatsPage", true);
        return "target_stats";
    }

    @GetMapping("/{targetId:\\d+}/dependentsRedesigned.action")
    public String dependents(@PathVariable int targetId, Admin admin, PaginationForm filter, Model model) {
        loadDependentsAttrs(model, filter, targetId, admin.getCompanyID());
        return "target_dependents";
    }

    @GetMapping("/{targetId:\\d+}/evaluate.action")
    public String evaluate(@PathVariable int targetId, @RequestParam int mailinglistId, Admin admin, Model model) {
        loadEvaluateAttrs(model, targetId, mailinglistId, admin);
        model.addAttribute("mailinglistId", mailinglistId);
        return "target_evaluate";
    }

    private void loadEvaluateAttrs(Model model, int targetId, int mailinglistId, Admin admin) {
        model.addAttribute("statistics", birtStatisticsService.getRecipientStatusStatistic(targetId, mailinglistId, admin))
             .addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
    }

    public void loadDependentsAttrs(Model model, PaginationForm filter, int targetId, int companyId) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.TARGET_DEPENDENTS_OVERVIEW, filter);
        PaginatedListImpl<Dependent<TargetGroupDependentType>> dependents = targetService.getDependents(targetId, companyId, filter);
        List<Integer> mailingIds = dependents.getList().stream()
            .filter(dependent -> TargetGroupDependentType.MAILING == dependent.getType() || TargetGroupDependentType.MAILING_CONTENT == dependent.getType())
            .map(Dependent::getId)
            .toList();
        model.addAttribute("dependents", dependents)
             .addAttribute("mailingGridTemplateMap", gridService.getGridTemplateIdsByMailingIds(companyId, mailingIds));
    }

    @RequestMapping("/{targetId:\\d+}/dependents.action")
    public String dependents(@PathVariable int targetId, Admin admin, @ModelAttribute("dependentsForm") TargetDependentsListForm form, Model model) {
        final int companyId = admin.getCompanyID();

        if (admin.isRedesignedUiUsed()) {
            FormUtils.syncNumberOfRows(webStorage, WebStorage.TARGET_DEPENDENTS_OVERVIEW, form);
        } else {
            webStorage.access(WebStorage.TARGET_DEPENDENTS_OVERVIEW, entry -> {
                if (form.getNumberOfRows() > 0) {
                    entry.setRowsCount(form.getNumberOfRows());
                    if (form.getFilterTypes() == null) {
                        entry.setFilterTypes(null);
                    } else {
                        entry.setFilterTypes(Arrays.asList(form.getFilterTypes()));
                    }
                } else {
                    form.setNumberOfRows(entry.getRowsCount());
                    form.setFilterTypes(entry.getFilterTypes().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
                }
            });

            model.addAttribute("targetShortname", targetService.getTargetName(form.getTargetId(), companyId));
            model.addAttribute("hidden", targetService.isHidden(targetId, admin.getCompanyID()));
        }

        PaginatedListImpl<Dependent<TargetGroupDependentType>> dependents = targetService.getDependents(companyId, form.getTargetId(), form.getFilterTypesSet(), form.getPage(), form.getNumberOfRows(), form.getSort(), form.getOrder());
        model.addAttribute("dependents", dependents);

        List<Integer> mailingIds = dependents.getList().stream()
                .filter(dependent -> TargetGroupDependentType.MAILING == dependent.getType() || TargetGroupDependentType.MAILING_CONTENT == dependent.getType())
                .map(Dependent::getId)
                .toList();

        model.addAttribute("mailingGridTemplateMap", gridService.getGridTemplateIdsByMailingIds(companyId, mailingIds));

        return "target_dependents_list";
    }

    private String viewQB(Admin admin, Model model, TargetEditForm form, Popups popups) {
        if (form.getPreviousViewFormat() == null) {
            form.setPreviousViewFormat(TargetgroupViewFormat.EQL);
        }

        boolean mailTrackingAvailable = AgnUtils.isMailTrackingAvailable(admin);

        EqlDetailedAnalysisResult analysisResult = eqlFacade.analyseEqlSafely(form.getEql());

        if (analysisResult.isAnaliseSuccess()) {
            if (analysisResult.isMailTrackingRequired() && !mailTrackingAvailable) {
                form.setViewFormat(TargetgroupViewFormat.EQL);
                popups.warning("warning.target.mailtrackingRequired");
            } else {
                form.setViewFormat(trySynchronizeForView(admin, form, popups, TargetgroupViewFormat.EQL));
            }
        } else {
            EqlSyntaxError eqlSyntaxError = analysisResult.getSyntaxErrors().get(0);

            form.setViewFormat(form.getPreviousViewFormat());
            model.addAttribute("errorPositionDetails", editFormValidator.getPositionDetails(eqlSyntaxError));
            popups.alert(editFormValidator.getEqlErrorMsg(form.getEql(), eqlSyntaxError));
        }

        setupCommonViewPageParams(admin, form.getTargetId(), form.getEql(), model);
        model.addAttribute("mailTrackingAvailable", mailTrackingAvailable);

        return admin.isRedesignedUiUsed() ? "target_settings" : "target_view";
    }

    private String viewEQL(Admin admin, Model model, TargetEditForm form, Popups popups) {
        if (form.getPreviousViewFormat() == null) {
            form.setPreviousViewFormat(TargetgroupViewFormat.EQL);
        }
        TargetgroupViewFormat viewFormat = trySynchronizeForView(admin, form, popups, TargetgroupViewFormat.QUERY_BUILDER);
        form.setViewFormat(viewFormat);

        if (popups.hasWarningPopups() || popups.hasAlertPopups()) {
            return MESSAGES_VIEW;
        }

        if (viewFormat != TargetgroupViewFormat.EQL) {
            return viewQB(admin, model, form, popups);
        }

        setupCommonViewPageParams(admin, form.getTargetId(), form.getEql(), model);
        return admin.isRedesignedUiUsed() ? "target_settings" : "target_view";
    }

    private TargetComplexityGrade getComplexityGrade(String eql, int companyId) {
        int complexityIndex = targetService.calculateComplexityIndex(eql, companyId);
        int recipientsCount = recipientService.getNumberOfRecipients(companyId);

        return TargetUtils.getComplexityGrade(complexityIndex, recipientsCount);
    }

    private String getReportUrl(Admin admin, String sessionId, int targetId, int mailinglistIs) {
        RecipientStatusStatisticDto statisticDto = new RecipientStatusStatisticDto();
        statisticDto.setTargetId(targetId);
        statisticDto.setMailinglistId(mailinglistIs);
        statisticDto.setFormat("html");

        return birtStatisticsService.getRecipientStatusStatisticUrl(admin, sessionId, statisticDto);
    }

    protected void setupCommonViewPageParams(Admin admin, int targetId, String eql, Model model) {
        boolean isLocked = targetService.isLocked(admin.getCompanyID(), targetId) || targetService.isEqlContainsInvisibleFields(eql, admin.getCompanyID(), admin.getAdminID());

        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("mailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        }
        model.addAttribute("complexityGrade", getComplexityGrade(eql, admin.getCompanyID()));
        model.addAttribute("isValid", targetService.isValid(admin.getCompanyID(), targetId));
        model.addAttribute("hidden", targetService.isHidden(targetId, admin.getCompanyID()));
        model.addAttribute("isLocked", isLocked);
        if (admin.isRedesignedUiUsed()) {
            AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        }
    }

    private boolean fillFormIfTargetExists(Admin admin, TargetEditForm form) {
        try {
            Target target = targetService.getTargetGroup(form.getTargetId(), admin.getCompanyID(), admin.getAdminID());
            form.setTargetId(target.getId());
            form.setShortname(target.getTargetName());
            form.setDescription(target.getTargetDescription());
            form.setEql(target.getEQL());
            form.setUseForAdminAndTestDelivery(target.isAdminTestDelivery());
            form.setAccessLimitation(target.isAccessLimitation());
            form.setFavorite(target.isFavorite());
            return true;
        } catch (UnknownTargetGroupIdException e) {
            logger.warn(String.format("Unknown target group ID %d", form.getTargetId()), e);
            return false;
        }
    }

    private TargetgroupViewFormat trySynchronizeForView(Admin admin, TargetEditForm form, Popups popups, TargetgroupViewFormat defaultFormat) {
        if (form.getPreviousViewFormat() == form.getViewFormatOrDefault()) {
            return form.getPreviousViewFormat();
        }
        try {
            editorContentSynchronizer.synchronizeEditors(admin, form.getPreviousViewFormat(), form);
            return form.getViewFormatOrDefault();
        } catch (final EditorContentSynchronizationException e) {
            popups.warning("targetgroup.tooComplex");

            logger.info("There was an error synchronizing editor content. Keeping current view format.", e);
            logger.warn("EQL: {}", form.getEql(), e);

            return defaultFormat;
        }
    }

    private boolean trySynchronizeToEqlForSave(Admin admin, TargetEditForm form, Popups popups) {
        if (form.getPreviousViewFormat() == TargetgroupViewFormat.EQL) {
            return true;
        }
        try {
            editorContentSynchronizer.synchronizeEditors(admin, form.getPreviousViewFormat(), form);
            return true;
        } catch (EditorContentSynchronizationException e) {
            logger.info("There was an error synchronizing editor content.", e);
            popups.alert(TARGET_SAVING_ERROR_KEY);
            return false;
        }
    }

    private int trySave(Admin admin, TargetEditForm form, Popups popups) throws UnknownTargetGroupIdException {
        int companyId = admin.getCompanyID();
        // Load target group or create new one
        final Target oldTarget;
        final Target newTarget;
        if (form.getTargetId() == 0) {
            oldTarget = null;
            newTarget = targetFactory.newTarget(companyId);
        } else {
            oldTarget = targetService.getTargetGroup(form.getTargetId(), companyId);
            newTarget = targetCopyService.copyTargetGroup(oldTarget, targetFactory.newTarget());
        }

        // Update editable properties
        FormHelper.formPropertiesToTargetGroup(newTarget, form);
        
        if (oldTarget != null) {
        	// Set AccessLimitation for existing targetgroup as it was set before, because the GUI form does not send the value again
        	newTarget.setAccessLimitation(oldTarget.isAccessLimitation());
        }
        
        if (form.isAccessLimitation()) {
	        int accessLimitingTargetgroupsAmount = targetService.getAccessLimitingTargetgroupsAmount(companyId);
	        int licenseMaximumOfAccessLimitingTargetgroupsPerCompany = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfAccessLimitingTargetgroupsPerCompany);
	        int gracefulExtension = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfAccessLimitingTargetgroupsPerCompany_Graceful);
			if (licenseMaximumOfAccessLimitingTargetgroupsPerCompany >= 0 && (licenseMaximumOfAccessLimitingTargetgroupsPerCompany + gracefulExtension) < accessLimitingTargetgroupsAmount + 1) {
				popups.alert("error.altg.exceeded", licenseMaximumOfAccessLimitingTargetgroupsPerCompany);
				return 0;
			} else if (licenseMaximumOfAccessLimitingTargetgroupsPerCompany >= 0 && licenseMaximumOfAccessLimitingTargetgroupsPerCompany < accessLimitingTargetgroupsAmount + 1) {
	        	popups.warning("error.numberOfAccessLimitingTargetgroupsExceeded.graceful", licenseMaximumOfAccessLimitingTargetgroupsPerCompany, accessLimitingTargetgroupsAmount, gracefulExtension);
			}
			
	        int configMaximumOfAccessLimitingTargetgroupsForThisCompany = configService.getIntegerValue(ConfigValue.MaximumAccessLimitingTargetgroups, companyId);
	        if (configMaximumOfAccessLimitingTargetgroupsForThisCompany >= 0 && configMaximumOfAccessLimitingTargetgroupsForThisCompany < accessLimitingTargetgroupsAmount + 1) {
	        	popups.alert("error.altg.exceeded", configMaximumOfAccessLimitingTargetgroupsForThisCompany);
				return 0;
			}
        }

        try {
            final List<Message> errors = new ArrayList<>();
            final int targetId = targetService.saveTarget(admin, newTarget, oldTarget, errors, userActivityLogService::writeUserActivityLog);

            errors.forEach(popups::alert);

            return targetId;
        } catch(TargetGroupNotCompatibleWithContentBlockException e) {
        	final int limit = 5;
        	
        	final List<String> allMailingNames = e.getUsage().stream()
                    .map(ContentBlockAndMailingMetaData::getMailingName)
                    .distinct()
                    .toList();

        	final String references = allMailingNames.stream()
                    .limit(limit)
                    .collect(Collectors.joining("</li><li>", "<li>", "</li>"));

        	if(allMailingNames.size() > limit) {
        		popups.alert("error.target.content.incompatible.limit.more", references, allMailingNames.size() - limit);
        	} else {
        		popups.alert("error.target.content.incompatible.limit", references);
        	}
        } catch (RequestErrorException e) {
            throw e;
        } catch (TargetGroupTooLargeException e) {
            popups.alert("error.target.too_large");
        } catch (Exception e) {
            logger.warn("There was an error Saving the target group. ", e);

            popups.alert(TARGET_SAVING_ERROR_KEY);
        }
        return 0;
    }

    private void analyzeComplexity(int companyId, int targetId, Popups popups) {
        TargetComplexityGrade complexityGrade = targetService.getTargetComplexityGrade(companyId, targetId);

        if (complexityGrade == TargetComplexityGrade.RED) {
            popups.warning("warning.target.group.performance.red");
        } else if (complexityGrade == TargetComplexityGrade.YELLOW) {
            popups.warning("warning.target.group.performance.yellow");
        }
    }

    private void analyzeEqlForSaving(Admin admin, String eql, Popups popups) {
        EqlDetailedAnalysisResult analysisResult = eqlFacade.analyseEqlSafely(eql);
        if (!analysisResult.isAnaliseSuccess()) {
            editFormValidator.throwEqlValidationException(eql, analysisResult.getSyntaxErrors().get(0));
        } else if (analysisResult.isMailTrackingRequired() && !AgnUtils.isMailTrackingAvailable(admin)) {
            popups.warning("warning.target.mailtrackingRequired");
        }
    }

    private boolean doSavingPreparations(Admin admin, TargetEditForm form, Popups popups) {
        if (form.getPreviousViewFormat() == null) {
            logger.warn("There is not previous view format. We do not know which tab use for saving.");
            popups.alert(TARGET_SAVING_ERROR_KEY);
            return false;
        }

        boolean isFormValid = editFormValidator.validate(admin.getCompanyID(), form, popups);
        if (!isFormValid) {
            return false;
        }

        boolean isSynchronized = trySynchronizeToEqlForSave(admin, form, popups);
        if (!isSynchronized) {
            return false;
        }

        analyzeEqlForSaving(admin, form.getEql(), popups);
        return !popups.hasAlertPopups();
    }

    private String redirectToView(int targetId) {
        return String.format("redirect:/target/%d/view.action", targetId);
    }

    @Override
    public boolean isParameterExcludedForUnsafeHtmlTagCheck(Admin admin, String param, String controllerMethodName) {
        return List.of("save", "view").contains(controllerMethodName)
                && configService.allowHtmlInReferenceAndProfileFields(admin.getCompanyID())
                && (List.of("eql", "queryBuilderRules").contains(param) || param.matches("targetgroup-querybuilder_rule_\\d+_value_\\d+"));
    }
}
