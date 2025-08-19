/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.web;

import static com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus.STATUS_PAUSED;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_ACTIVE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_INACTIVE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_NONE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_OPEN;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_TESTED;
import static com.agnitas.util.Const.Mvc.CHANGES_SAVED_MSG;
import static com.agnitas.util.Const.Mvc.DELETE_VIEW;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;
import static com.agnitas.util.Const.Mvc.NOTHING_SELECTED_MSG;
import static com.agnitas.util.Const.Mvc.SELECTION_DELETED_MSG;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponentType;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.CampaignDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.MailingComponentDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.mailing.forms.MailingStatisticForm;
import com.agnitas.emm.core.mailing.service.MailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.core.workflow.beans.WorkflowDependency;
import com.agnitas.emm.core.workflow.beans.WorkflowDependencyType;
import com.agnitas.emm.core.workflow.beans.WorkflowForward;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowRecipientImpl;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParameters;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import com.agnitas.emm.core.workflow.service.ChangingWorkflowStatusResult;
import com.agnitas.emm.core.workflow.service.SampleWorkflowFactory;
import com.agnitas.emm.core.workflow.service.SampleWorkflowFactory.SampleWorkflowType;
import com.agnitas.emm.core.workflow.service.WorkflowActivationService;
import com.agnitas.emm.core.workflow.service.WorkflowDataParser;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.core.workflow.service.WorkflowStatisticsService;
import com.agnitas.emm.core.workflow.service.WorkflowValidationService;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.emm.core.workflow.web.forms.WorkflowDependencyValidationForm;
import com.agnitas.emm.core.workflow.web.forms.WorkflowForm;
import com.agnitas.exception.RequestErrorException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.PdfService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.GuiConstants;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.util.StringUtil;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.forms.BulkActionForm;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.mvc.editors.IntEnumEditor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import jakarta.servlet.http.HttpSession;
import org.agnitas.emm.core.autoexport.bean.AutoExport;
import org.agnitas.emm.core.autoexport.service.AutoExportService;
import org.agnitas.emm.core.autoimport.bean.AutoImportLight;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class WorkflowController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(WorkflowController.class);

    public static final String INCOMPLETE_WORKFLOW_NAME = "incompleteWorkflowName";

    // TODO: EMMGUI-714: check usage and remove forward contant when old design will be removed
    public static final String FORWARD_USERFORM_CREATE = "userform_create";
    public static final String FORWARD_USERFORM_EDIT = "userform_edit";

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

    public static final String WORKFLOW_CUSTOM_CSS_STYLE = ".body{background-color: #fff;} #viewPort { display: inline-block !important; width: 100% !important;}";

    private static final String REDIRECT_TO_LIST = "redirect:/workflow/list.action";
    private static final Set<String> SPLIT_TYPES = Set.of(
        "050505050575",
        "0505050580",
        "05050585",
        "050590",
        "101010101050",
        "1010101060",
        "10101070",
        "101080",
        "1090",
        "151570",
        "2080",
        "25252525",
        "252550",
        "3070",
        "333333",
        "4060",
        "5050");

    protected final AdminService adminService;
    protected final ConfigService configService;
    protected final TargetService targetService;
    private final WorkflowService workflowService;
    private final RecipientFieldService recipientFieldService;
    private final WorkflowValidationService validationService;
    private final WorkflowActivationService workflowActivationService;
    private final WorkflowStatisticsService workflowStatisticsService;
    private final AutoImportService autoImportService;
    private final AutoExportService autoExportService;
    private final WorkflowDataParser workflowDataParser;
    private final CampaignDao campaignDao;
    private final MailingDeliveryStatService deliveryStatService;
    private final MailingComponentDao componentDao;
    private final PdfService pdfService;
    private final CompanyDao companyDao;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final UserActivityLogService userActivityLogService;
    private final ConversionService conversionService;
    private final MailingService mailingService;

    public WorkflowController(WorkflowService workflowService, WorkflowValidationService validationService,
                              WorkflowActivationService workflowActivationService, WorkflowStatisticsService workflowStatisticsService,
                              @Autowired(required = false) AutoImportService autoImportService, @Autowired(required = false) AutoExportService autoExportService,
                              WorkflowDataParser workflowDataParser, CampaignDao campaignDao, MailingDeliveryStatService deliveryStatService, MailingComponentDao componentDao,
                              PdfService pdfService, CompanyDao companyDao, ConfigService configService, MailinglistApprovalService mailinglistApprovalService,
                              UserActivityLogService userActivityLogService, ConversionService conversionService, MailingService mailingService, AdminService adminService,
                              TargetService targetService, RecipientFieldService recipientFieldService) {
        this.workflowService = workflowService;
        this.validationService = validationService;
        this.workflowActivationService = workflowActivationService;
        this.workflowStatisticsService = workflowStatisticsService;
        this.autoImportService = autoImportService;
        this.autoExportService = autoExportService;
        this.workflowDataParser = workflowDataParser;
        this.campaignDao = campaignDao;
        this.deliveryStatService = deliveryStatService;
        this.componentDao = componentDao;
        this.pdfService = pdfService;
        this.companyDao = companyDao;
        this.configService = configService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
        this.mailingService = mailingService;
        this.adminService = adminService;
        this.targetService = targetService;
        this.recipientFieldService = recipientFieldService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(WorkflowDependencyType.class, new IntEnumEditor<>(WorkflowDependencyType.class));
    }

    @GetMapping("/create.action")
    public String create(Admin admin, Model model) {
        WorkflowForm workflowForm = new WorkflowForm();
        workflowForm.setStatus(STATUS_OPEN);
        workflowForm.setStatusMaybeChangedTo(STATUS_ACTIVE);
        workflowForm.setWorkflowSchema(workflowService.getInitialWorkflowSchema());
        prepareViewPage(admin, model, 0);

        model.addAttribute("workflowForm", workflowForm);

        return "workflow_view";
    }

    @RequestMapping("/{id:\\d+}/view.action")
    public String view(Admin admin, @PathVariable int id, Model model,
                       @RequestParam(name = "forwardParams", required = false) String forwardParams, Popups popups) {
        if (id == 0) {
            return "redirect:/workflow/create.action";
        }

        if (!model.containsAttribute("workflowForm")) {
            Workflow workflow = workflowService.getWorkflow(id, admin.getCompanyID());
            WorkflowForm form = new WorkflowForm();

            if (workflow == null) {
                // Given identifier is invalid.
                popups.alert(ERROR_MSG);
            } else {
                form = conversionService.convert(workflow, WorkflowForm.class);
                writeUserActivityLog(admin, "workflow view", getWorkflowDescription(form));
            }

            model.addAttribute("workflowForm", form);
            collectWarnings(model, form, admin, popups);
            if (form.getStatus() == WorkflowForm.WorkflowStatus.STATUS_PAUSED) {
                model.addAttribute("pauseTime", workflowService.getPauseDate(id, admin.getCompanyID()).getTime());
                model.addAttribute("pauseExpirationHours", configService.getIntegerValue(ConfigValue.WorkflowPauseExpirationHours, admin.getCompanyID()));
            }
        }

        prepareViewPage(admin, model, id);
        model.addAllAttributes(AgnUtils.getParamsMap(forwardParams));
        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("statisticUrls", workflowStatisticsService.getStatUrlsMap(id, admin));
        }
        return "workflow_view";
    }

    private void collectWarnings(Model model, WorkflowForm form, Admin admin, Popups popups) {
        collectSentMailings(model, form, admin, popups);
        validationService.validateDeadlineBeforeDecision(form.getWorkflowIcons(), popups);
    }

    private void collectSentMailings(Model model, WorkflowForm form, Admin admin, Popups popups) {
        if (!List.of(STATUS_OPEN, STATUS_INACTIVE, STATUS_TESTED).contains(form.getStatus())) {
            return;
        }
        List<Integer> sentMailings = validationService.collectSentMailings(form.getWorkflowIcons(), admin);
        if (CollectionUtils.isNotEmpty(sentMailings)) {
            popups.exactWarning(getSentMailingsWarnText(sentMailings, admin.getCompanyID(), admin.getLocale()));
        }
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("sentMailings", sentMailings);
        }
    }

    private String getSentMailingsWarnText(List<Integer> sentMailings, int companyId, Locale locale) {
        String sentMailingsStr = sentMailings.stream()
                .map(sentMailingId -> mailingService.getMailingName(sentMailingId, companyId))
                .collect(Collectors.joining("<br>"));
        return I18nString.getLocaleString("error.workflow.containsSentMailings", locale) + "<br>" + sentMailingsStr;
    }

    @GetMapping("/list.action")
    public String list(Admin admin, Model model) {
        model.addAttribute("workflowsJson", workflowService.getWorkflowListJson(admin));

        model.addAttribute("adminTimeZone", admin.getAdminTimezone());
        SimpleDateFormat dateTimeFormat = admin.getDateTimeFormat();
        model.addAttribute("adminDateTimeFormat", dateTimeFormat.toPattern());
        SimpleDateFormat dateFormat = admin.getDateFormat();
        model.addAttribute("adminDateFormat", dateFormat.toPattern());
        SimpleDateFormat timeFormat = admin.getTimeFormat();
        model.addAttribute("adminTimeFormat", timeFormat.toPattern());
        SimpleDateFormat dateTimeFormatWithSeconds = admin.getDateTimeFormatWithSeconds();
        model.addAttribute("adminDateTimeFormatWithSeconds", dateTimeFormatWithSeconds.toPattern());

        return "workflow_list";
    }

    @GetMapping("/{id:\\d+}/confirmDelete.action")
    // TODO: check usage and remove after EMMGUI-714 will be finished and old design will be removed
    public String confirmDelete(Admin admin, @PathVariable("id") int workflowId, WorkflowForm workflowForm, Popups popups) {
        Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());

        if (workflow == null) {
            popups.alert(ERROR_MSG);
            return MESSAGES_VIEW;
        }

        workflowForm.setWorkflowId(workflow.getWorkflowId());
        workflowForm.setShortname(workflow.getShortname());
        return "workflow_delete_ajax";
    }

    @RequestMapping("/{id:\\d+}/delete.action")
    // TODO: check usage and remove after EMMGUI-714 will be finished and old design will be removed
    public String delete(Admin admin, @PathVariable("id") int workflowId, Popups popups) {
        Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
        try {
            if (workflow.getStatus() != Workflow.WorkflowStatus.STATUS_ACTIVE
                    && workflow.getStatus() != Workflow.WorkflowStatus.STATUS_TESTING
                    && workflow.getStatus() != STATUS_PAUSED) {
                workflowService.deleteWorkflow(workflowId, admin.getCompanyID());

                popups.success("default.selection.deleted");
                writeUserActivityLog(admin, "delete campaign", getWorkflowDescription(workflow));
                return REDIRECT_TO_LIST;
            } else {
                popups.alert("error.workflow.nodesShouldBeDisabledBeforeDeleting");
            }
        } catch (Exception e) {
            logger.error("Workflow deletion error", e);
            popups.alert(ERROR_MSG);
        }

        return MESSAGES_VIEW;
    }

    // TODO: check usage and remove after EMMGUI-714 will be finished and old design will be removed
    @PostMapping("/confirmBulkDelete.action")
    public String confirmBulkDelete(@ModelAttribute("bulkForm") BulkActionForm form, Popups popups) {
        if (form.getBulkIds().isEmpty()) {
            popups.alert("bulkAction.nothing.workflow");
            return MESSAGES_VIEW;
        }

        return "workflow_bulkDeleteConfirm_ajax";
    }

    @GetMapping("/bulkDelete.action")
    public String confirmDeleteRedesigned(BulkActionForm form, Admin admin, Popups popups, Model model) {
        if (form.getBulkIds().isEmpty()) {
            popups.alert("bulkAction.nothing.workflow");
            return MESSAGES_VIEW;
        }

        ServiceResult<List<Workflow>> result = workflowService.getAllowedForDeletion(Set.copyOf(form.getBulkIds()), admin.getCompanyID());
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        MvcUtils.addDeleteAttrs(model, result.getResult().stream().map(Workflow::getShortname).toList(),
                "workflow.delete", "workflow.delete.question.new",
                "bulkAction.delete.workflow", "bulkAction.delete.workflow.question");
        return DELETE_VIEW;
    }

    @PostMapping("/bulkDelete.action")
    public Object bulkDelete(BulkActionForm form, Admin admin, Popups popups) {
        try {
            Collection<Integer> removedIds = workflowService.bulkDelete(new HashSet<>(form.getBulkIds()), admin.getCompanyID());

            writeUserActivityLog(admin, "delete workflows", String.format("Workflows IDs %s", StringUtils.join(removedIds, ", ")));
            popups.success(SELECTION_DELETED_MSG);

            return admin.isRedesignedUiUsed() && form.getBulkIds().size() > 1
                    ? ResponseEntity.ok(new DataResponseDto<>(removedIds, popups))
                    : REDIRECT_TO_LIST;
        } catch (Exception e) {
            logger.error("Workflow Bulk deletion error", e);
            popups.alert(ERROR_MSG);
        }

        return admin.isRedesignedUiUsed() && form.getBulkIds().size() > 1
                ? ResponseEntity.ok(new DataResponseDto<>(popups, false))
                : MESSAGES_VIEW;
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @PostMapping("/confirmBulkDeactivate.action")
    public String confirmBulkDeactivate(@ModelAttribute("bulkForm") BulkActionForm form, Popups popups) {
        if (form.getBulkIds().isEmpty()) {
            popups.alert("bulkAction.nothing.workflow");
        }

        return "workflow_bulkDeactivateConfirm_ajax";
    }

    @PostMapping("/bulkDeactivate.action")
    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public String bulkDeactivate(Admin admin, BulkActionForm form, Popups popups) {
        Set<Integer> workflowIdsToDeactivate = new HashSet<>();
        List<Workflow> workflows = workflowService.getWorkflowsByIds(new HashSet<>(form.getBulkIds()), admin.getCompanyID());

        for (Workflow workflow : workflows) {
            switch (workflow.getStatus()) {
                case STATUS_ACTIVE:
                case STATUS_TESTING:
                    workflowIdsToDeactivate.add(workflow.getWorkflowId());
                    break;
                default:
                    break;
            }
        }

        final Map<Integer, ChangingWorkflowStatusResult> changingResults = workflowService.bulkDeactivate(workflowIdsToDeactivate, admin.getCompanyID());

        popups.success("default.changes_saved");

        for (Workflow workflow : workflows) {
            final ChangingWorkflowStatusResult changingResult = changingResults.get(workflow.getWorkflowId());
            if(changingResult == null || !changingResult.isChanged()) {
                continue;
            }
            
            changingResult.getMessages().forEach(popups::warning);

            switch (workflow.getStatus()) {
                case STATUS_ACTIVE:
                    writeUserActivityLog(admin, "do deactivate campaign", getWorkflowDescription(workflow));
                    if(changingResult.isAnyMailingDeactivated()) {
                        writeUserActivityLog(admin, "do deactivate containing mailings", getWorkflowDescription(workflow));
                    }
                    break;

                case STATUS_TESTING:
                    writeUserActivityLog(admin, "do stop test campaign", getWorkflowDescription(workflow));
                    break;
                default:
                    break;
            }
        }

        return REDIRECT_TO_LIST;
    }

    @PostMapping("/changeActiveness.action")
    public ResponseEntity<DataResponseDto<List<Object>>> changeActiveness(@RequestParam(required = false) Set<Integer> ids, @RequestParam boolean activate, Admin admin, Popups popups) {
        validateSelectedIds(ids);

        Map<Integer, ServiceResult<ChangingWorkflowStatusResult>> result = workflowService.setActiveness(ids, admin, activate);

        JSONArray jsonArray = new JSONArray();

        result.forEach((id, res) -> {
            ChangingWorkflowStatusResult changingResult = res.getResult();

            if (changingResult.isChanged()) {
                JSONObject statusJson = new JSONObject();
                statusJson.put("id", id);
                statusJson.put("status", changingResult.getNewStatus().getName());

                jsonArray.put(statusJson);
            }

            if (res.isSuccess() && changingResult.isChanged()) {
                String workflowDescription = getWorkflowDescription(changingResult.getWorkflow());

                if (activate) {
                    writeUserActivityLog(admin, "do activate campaign", workflowDescription);
                } else {
                    if (Workflow.WorkflowStatus.STATUS_TESTING.equals(changingResult.getOldStatus())) {
                        writeUserActivityLog(admin, "do stop test campaign", workflowDescription);
                    } else {
                        writeUserActivityLog(admin, "do deactivate campaign", workflowDescription);
                        if (changingResult.isAnyMailingDeactivated()) {
                            writeUserActivityLog(admin, "do deactivate containing mailings", workflowDescription);
                        }
                    }
                }
            }
        });

        long successfulCount = result.values().stream()
                .filter(ServiceResult::isSuccess)
                .count();

        if (successfulCount == 0) {
            result.values().stream()
                    .flatMap(r -> CollectionUtils.union(r.getResult().getMessages(), r.getErrorMessages()).stream())
                    .forEach(popups::alert);
        } else {
            if (successfulCount < ids.size()) {
                popups.warning(Message.of(activate ? "warning.bulkAction.general.activate" : "warning.bulkAction.general.deactivate", ids.size() - successfulCount));
            }

            popups.success(CHANGES_SAVED_MSG);
        }


        return ResponseEntity.ok(new DataResponseDto<>(jsonArray.toList(), popups));
    }

    private void validateSelectedIds(Set<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new RequestErrorException(NOTHING_SELECTED_MSG);
        }
    }

    @PostMapping("/save.action")
    public String save(Admin admin, @ModelAttribute("workflowForm") WorkflowForm workflowForm,
                       @RequestParam(value = "forwardName", required = false) String forwardName,
                       @RequestParam(value = "forwardParams", required = false) String forwardParams,
                       @RequestParam(value = "forwardTargetItemId", required = false) String forwardTargetItemId,
                       @RequestParam(value = "showStatistic", required = false) boolean showStatistic,
                       @RequestParam(required = false) Boolean startTimeAdjusted,
                       RedirectAttributes redirectModel,
                       HttpSession session,
                       Popups popups) throws Exception {
        List<Message> errors = new ArrayList<>();
        List<Message> warnings = new ArrayList<>();

        Workflow newWorkflow = getWorkflow(workflowForm, admin);
        Workflow existingWorkflow = workflowService.getWorkflow(newWorkflow.getWorkflowId(), newWorkflow.getCompanyId());

        Workflow.WorkflowStatus existingStatus = existingWorkflow != null ? existingWorkflow.getStatus() : Workflow.WorkflowStatus.STATUS_NONE;
        Workflow.WorkflowStatus newStatus = newWorkflow.getStatus() != Workflow.WorkflowStatus.STATUS_NONE ? newWorkflow.getStatus() : Workflow.WorkflowStatus.STATUS_OPEN;
        boolean isActivePausedOrTesting = newStatus == Workflow.WorkflowStatus.STATUS_ACTIVE || newStatus == STATUS_PAUSED || newStatus == Workflow.WorkflowStatus.STATUS_TESTING;
        boolean isDuringPause = isDuringPause(existingStatus, newStatus);
        boolean isUnpausing = WorkflowUtils.isUnpausing(existingStatus, newStatus);

        if (((admin.isRedesignedUiUsed() && StringUtils.isNotBlank(forwardParams)) || StringUtils.isNotEmpty(forwardName)) && StringUtils.length(newWorkflow.getShortname()) < 3) {
            newWorkflow.setShortname(INCOMPLETE_WORKFLOW_NAME);
        }

        List<WorkflowIcon> newIcons = getIcons(workflowForm);

        // Running or complete campaign should never be saved.
        if (existingStatus.isChangeable() && allowedToSave(existingWorkflow, newIcons, popups, existingStatus, newStatus)) {
            // Set OPEN_STATUS until validation passed and workflow is activated.
            newWorkflow.setStatus(Workflow.WorkflowStatus.STATUS_OPEN);
            
            if (WorkflowUtils.isDuringPause(existingStatus, newStatus) && existingWorkflow != null) {
                newIcons = updateMailingAndStopIcons(existingWorkflow.getWorkflowIcons(), newIcons);
            }

            if (admin.isRedesignedUiUsed()) {
                workflowService.restoreCreatedMailings(newIcons, existingWorkflow, admin);
            }

            workflowService.saveWorkflow(admin, newWorkflow, newIcons, isDuringPause, isUnpausing);
            newWorkflow.setStatus(newStatus);

            if (existingWorkflow == null) {
                writeUserActivityLog(admin, "create campaign", getWorkflowDescription(newWorkflow));
            } else {
                writeWorkflowChangeLog(admin, existingWorkflow, newWorkflow);
            }

            List<WorkflowIcon> icons = newWorkflow.getWorkflowIcons();
            if (StringUtils.isNotEmpty(forwardName) && !isDuringPause) { // if pausing then skip to forward after status is set 
                if (admin.isRedesignedUiUsed()) {
                    return getForwardRedesigned(forwardName, forwardParams, forwardTargetItemId, newWorkflow.getWorkflowId(), icons, redirectModel);
                }
                return getForward(forwardName, forwardParams, forwardTargetItemId, newWorkflow.getWorkflowId(), icons, redirectModel);
            }
            
            errors.addAll(validateWorkflow(admin, icons, newWorkflow.getWorkflowId(), existingStatus, newStatus));

            boolean isValid = errors.isEmpty();
            if (!admin.isRedesignedUiUsed()) {
                isValid = isValid && !checkAndSetDuplicateMailing(admin, redirectModel, icons, isActivePausedOrTesting);
            }
            setStatus(admin, newWorkflow, existingWorkflow, errors, warnings, isValid);
            
            if (StringUtils.isNotEmpty(forwardName) && isValid && isDuringPause) {
                if (admin.isRedesignedUiUsed()) {
                    return getForwardRedesigned(forwardName, forwardParams, forwardTargetItemId, newWorkflow.getWorkflowId(), icons, redirectModel);
                }
                return getForward(forwardName, forwardParams, forwardTargetItemId, newWorkflow.getWorkflowId(), icons, redirectModel);
            }
            
            if (errors.isEmpty()) {
                popups.success("default.changes_saved");
            }

            workflowForm.setWorkflowId(newWorkflow.getWorkflowId());
        } else {
            if (existingWorkflow == null) {
            	throw new IllegalStateException("Unexpected empty existingWorkflow");
            }

            if (StringUtils.isNotEmpty(forwardName)) {
                if (admin.isRedesignedUiUsed()) {
                    return getForwardRedesigned(forwardName, forwardParams, forwardTargetItemId, existingWorkflow.getWorkflowId(),
                            existingWorkflow.getWorkflowIcons(), redirectModel);
                }
                return getForward(forwardName, forwardParams, forwardTargetItemId, existingWorkflow.getWorkflowId(),
                        existingWorkflow.getWorkflowIcons(), redirectModel);
            }

            if (!popups.hasAlertPopups() && validateStatusTransition(existingStatus, newStatus, errors)) {
                if (WorkflowUtils.isPausing(existingStatus, newStatus)) {
                    workflowService.savePausedSchemaForUndo(existingWorkflow, admin.getAdminID());
                } else if (WorkflowUtils.isStoppingOnPause(existingStatus, newStatus)) {
                    workflowService.deletePauseUndoEntry(existingWorkflow.getWorkflowId(), admin.getCompanyID());
                }
                final ChangingWorkflowStatusResult changingResult =
                        workflowService.changeWorkflowStatus(existingWorkflow.getWorkflowId(), existingWorkflow.getCompanyId(), newStatus);
                errors.addAll(changingResult.getMessages());
                writeWorkflowStatusChangeLog(newWorkflow, existingWorkflow, admin);
                if(changingResult.isAnyMailingDeactivated()) {
                    writeUserActivityLog(admin, "do deactivate containing mailings", getWorkflowDescription(newWorkflow));
                }
            }
        }

        if (isActivePausedOrTesting) {
            errors.forEach(popups::alert);
        } else {
            errors.forEach(popups::warning);
        }

        warnings.forEach(popups::warning);
        updateForwardParameters(session, forwardTargetItemId, workflowForm.getWorkflowId(), forwardParams);
        if (admin.isRedesignedUiUsed() && StringUtils.isNotBlank(forwardParams) && StringUtils.isNotBlank(forwardTargetItemId)) {
            WorkflowParametersHelper.addEditedElemRedirectAttrs(redirectModel, session, Integer.parseInt(forwardTargetItemId));
        }

        if (showStatistic) {
            if (workflowService.existsAtLeastOneFilledMailingIcon(getIcons(workflowForm))) {
                if (admin.isRedesignedUiUsed()) {
                    return String.format("redirect:/workflow/%d/statistic.action", workflowForm.getWorkflowId()); 
                } else {
                    redirectModel.addFlashAttribute("showStatisticsImmediately", true);
                }
            } else {
                popups.alert("error.workflow.noStatistics.title");
            }
        }
        if (Boolean.TRUE.equals(startTimeAdjusted)) {
            popups.info("workflow.startdate.changed");
        }
        return String.format("redirect:/workflow/%d/view.action", workflowForm.getWorkflowId());
    }

    private boolean allowedToSave(Workflow currentWorkflow, List<WorkflowIcon> newIcons, Popups popups,
                                 Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) {
        if (WorkflowUtils.isStoppingOnPause(oldStatus, newStatus)) {
            return false; // save not needed while stopping of the paused workflow
        }
        if (currentWorkflow == null) {
            return true; // new workflow allowed to save
        }
        if (oldStatus == STATUS_PAUSED // paused campaign can't be saved if it contain illegal changes
                && validationService.containNotAllowedPauseChanges(currentWorkflow.getWorkflowIcons(), newIcons)) {
            popups.alert("error.workflow.pause.change");
            return false;
        }
        return true;
    }

    // Only mailings and stop icons can be changed on save during pause.
    // This method substitutes all old mailing or stop icons with new icons from form
    // and prevents other icons from changing.
    private List<WorkflowIcon> updateMailingAndStopIcons(List<WorkflowIcon> oldIcons, List<WorkflowIcon> newIcons) {
        newIcons.stream()
                .filter(newIcon -> WorkflowUtils.isMailingIcon(newIcon) || WorkflowUtils.isStopIcon(newIcon))
                .forEach(updatedIcon -> {
                    int index = ListUtils.indexOf(oldIcons, oldIcon -> oldIcon.getId() == updatedIcon.getId());
                    if (index != -1) {
                        oldIcons.set(index, updatedIcon);
                    }
                });
        return oldIcons;
    }

    private boolean isDuringPause(Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) {
        return WorkflowUtils.isDuringPause(oldStatus, newStatus);
    }

    @PostMapping("/{id:\\d+}/autoUnpause.action")
    @ResponseBody
    public BooleanResponseDto autoUnpause(@PathVariable int id, Admin admin, Popups popups) throws Exception {
        Workflow workflow = workflowService.getWorkflow(id, admin.getCompanyID());
        Admin pauseAdmin = workflowService.getPauseAdmin(workflow.getWorkflowId(), workflow.getCompanyId());

        ServiceResult<List<UserAction>> result = workflowActivationService.autoUnpauseWorkflow(workflow, pauseAdmin);
        if (result.isSuccess()) {
            result.getResult().forEach(ua -> writeUserActivityLog(pauseAdmin, ua));
        }
        result.getWarningMessages().forEach(popups::warning);
        result.getErrorMessages().forEach(popups::alert);
        return new BooleanResponseDto(popups, result.isSuccess());
    }

    @GetMapping("/validateDependency.action")
    public ResponseEntity<Map<String, Object>> validateDependency(Admin admin, WorkflowDependencyValidationForm form) {
        WorkflowDependencyType type = form.getType();
        int workflowId = form.getWorkflowId();

        // A workflowId = 0 value is reserved for a new workflow.
        if (type == null || workflowId < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Type parameter is missing or invalid.
        }
        WorkflowDependency dependency = null;

        if (form.getEntityId() > 0) {
            dependency = type.forId(form.getEntityId());
        } else if (StringUtils.isNotEmpty(form.getEntityName())) {
            dependency = type.forName(form.getEntityName());
        }

        if (dependency == null) {
            // Either identifier or a name is required.
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        JSONObject data = new JSONObject();
        data.put("valid", workflowService.validateDependency(admin.getCompanyID(), workflowId, dependency));
        return new ResponseEntity<>(data.toMap(), HttpStatus.OK);
    }

    @GetMapping("/getCurrentAdminTime.action")
    public ResponseEntity<Map<String, Object>> getCurrentAdminTime(Admin admin) {
        GregorianCalendar calendar = new GregorianCalendar(AgnUtils.getTimeZone(admin));

        final JSONObject resultJson = new JSONObject();
        resultJson.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        resultJson.put("minute",  calendar.get(Calendar.MINUTE));
        return ResponseEntity.ok(resultJson.toMap());
    }

    @GetMapping(value = "/getMailingsByWorkStatus.action", produces = HttpUtils.APPLICATION_JSON_UTF8)
    public @ResponseBody ResponseEntity<?> getMailingsByWorkStatus(Admin admin,
                            @RequestParam(value = "mailingId", required = false, defaultValue = "0") int mailingId,
                            @RequestParam("mailingTypes") String mailingTypes,
                            @RequestParam(value = "mediatypes", required = false) String mediatypes,
                            @RequestParam("status") String status,
                            @RequestParam(value = "mailingStatus", required = false) String mailingStatus,
                            @RequestParam(value = "takeMailsForPeriod", required = false) String takeMailsForPeriodParam,
                            @RequestParam(value = "parentMailingId", required = false, defaultValue = "0") int parentMailingId,
                            @RequestParam("sort") String sort,
                            @RequestParam("order") String order,
                            @RequestParam(value = "mailingsInCampaign", required = false) String mailingsInCampaign
    ) throws Exception {
        int companyId = admin.getCompanyID();

        if (StringUtils.isEmpty(mailingTypes)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<MailingType> mailingTypeList = new ArrayList<>();
        for (int value : StringUtil.buildListFormCommaSeparatedValueString(mailingTypes)) {
            mailingTypeList.add(MailingType.fromCode(value));
        }
        boolean takeMailsForPeriod = Boolean.parseBoolean(takeMailsForPeriodParam);

        List<Map<String, Object>> mailings = workflowService.getAllMailings(admin, mailingTypeList,
                status, mailingStatus, takeMailsForPeriod, sort, order);

        if (StringUtils.isNotBlank(mailingsInCampaign)) {
            mailings.addAll(workflowService.getMailings(companyId, mailingsInCampaign));
        }

        boolean parentNotInList = !workflowService.isParentMailingIdExistsInList(parentMailingId, mailings);

        if (parentNotInList && (parentMailingId != 0) && (parentMailingId != mailingId)) {
            Map<String, Object> mailingData = workflowService.getMailingWithWorkStatus(parentMailingId, companyId);
            mailings.add(mailingData);
        }

        if (StringUtils.isNotBlank(mediatypes)) {
            List<Integer> requiredMediaTypes = StringUtil.buildListFormCommaSeparatedValueString(mediatypes);
            mailings = workflowService.filterWithRequiredMediaTypes(mailings, requiredMediaTypes);
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

        return new ResponseEntity<>(objectMapper.writeValueAsString(mailings), HttpStatus.OK);
    }

    @GetMapping("/copy.action")
    public String copy(Admin admin, Model model, @RequestParam int workflowId, @RequestParam boolean isWithContent) {
        Workflow existedWorkflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
        Workflow workflow = workflowService.copyWorkflow(admin, workflowId, isWithContent);

        writeUserActivityLog(admin, "copy campaign " + (isWithContent ? "with" : "without") + " content",
                getWorkflowDescription(existedWorkflow) + " copied as " + getWorkflowDescription(workflow));

        prepareViewPage(admin, model, workflow.getWorkflowId());

        return String.format("redirect:/workflow/%d/view.action", workflow.getWorkflowId());
    }

    @GetMapping("/getAllMailingSorted.action")
    public ResponseEntity<List<LightweightMailing>> getAllMailingSorted(Admin admin, @RequestParam("sortField") String sortField,
                                                                        @RequestParam("sortDirection") String sortDirection) {
        List<LightweightMailing> mailings = workflowService.getAllMailingsSorted(admin, sortField, sortDirection);

        return new ResponseEntity<>(mailings, HttpStatus.OK);
    }

    @GetMapping("/getWorkflowContent.action")
    public ResponseEntity<List<WorkflowIcon>> getWorkflowContent(Admin admin, @RequestParam int workflowId, @RequestParam boolean isWithContent) {
        // We either reset all the icon content/settings or have to clone used mailings (if any).
        List<WorkflowIcon> icons = workflowService.getIconsForClone(admin, workflowId, isWithContent);

        if (icons == null) {
            icons = Collections.emptyList();
        }

        return ResponseEntity.ok(icons);
    }

    @GetMapping("/getSampleWorkflowContent.action")
    public ResponseEntity<List<WorkflowIcon>> getSampleWorkflowContent(@RequestParam String type, @RequestParam boolean gridEnabled) {
        try {
            return ResponseEntity.ok(SampleWorkflowFactory.createSampleWorkflow(SampleWorkflowType.from(type), gridEnabled));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/autoOptWorkflowSample.action")
    public ResponseEntity<List<WorkflowIcon>> autoOptWorkflowSample(@RequestParam(defaultValue = "2") int mailingsCount,
                                                                    @RequestParam boolean gridEnabled, Admin admin) {
        Locale locale = admin.getLocale();
        List<WorkflowIcon> icons = SampleWorkflowFactory.autoOptWorkflowSample(mailingsCount, gridEnabled, locale);
        return CollectionUtils.isEmpty(icons)
                ? ResponseEntity.badRequest().build()
                : ResponseEntity.ok(icons);
    }

    @GetMapping("/mailing/{mailingId:\\d+}/info.action")
    public @ResponseBody Map<String, Object> getMailingInfo(@PathVariable int mailingId, Admin admin) {
        int companyId = admin.getCompanyID();
        Mailing mailing = workflowService.getMailing(mailingId, companyId);

        Map<String, Object> mailingData = new HashMap<>();
        if(mailing.getId() > 0) {
            mailingData.put("mailinglistId", mailing.getMailinglistID());
            putTargetsToMailingData(admin, mailing.getTargetGroups(), mailingData);
            mailingData.put("planDate", mailing.getPlanDate());
            mailingData.put("campaignId", mailing.getCampaignID());

            mailingData.put("mailingType", mailing.getMailingType().getCode());

            // Get send date.
            DeliveryStat deliveryStat = deliveryStatService.getDeliveryStats(companyId, mailingId, mailing.getMailingType());
            if (deliveryStat.getScheduledSendTime() != null) {
                Calendar sendDate = DateUtilities.calendar(deliveryStat.getScheduledSendTime(), TimeZone.getTimeZone(admin.getAdminTimezone()));
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

        return mailingData;
    }

    protected void putTargetsToMailingData(Admin admin, Collection<Integer> targetGroups, Map<String, Object> mailingData) {
        mailingData.put("targetGroupIds", targetGroups);
    }

    @GetMapping("/viewOnlyElements.action")
    public String viewOnlyElements(@ModelAttribute("workflowForm") WorkflowForm form, Admin admin, Model model, Popups popups) {
        prepareViewPage(admin, model, form.getWorkflowId());
        loadWorkflow(form, admin, popups);
        writeUserActivityLog(admin, "view campaign", getWorkflowDescription(form));
        return "workflow_view_only_elements";
    }

    @GetMapping("/{workflowId:\\d+}/generatePDF.action")
    public ResponseEntity<byte[]> generatePDF(Admin admin, @PathVariable int workflowId, HttpSession session) throws Exception {
        String hostUrl = configService.getPreviewBaseUrl();
        String url = hostUrl + "/workflow/viewOnlyElements.action;jsessionid=" + session.getId() + "?workflowId=" + workflowId;

        String workflowName = workflowService.getWorkflow(workflowId, admin.getCompanyID()).getShortname();
        File pdfFile = pdfService.generatePDF(admin, url, true, HttpUtils.escapeFileName(workflowName), "workflow.single", WORKFLOW_CUSTOM_CSS_STYLE, "wmLoadFinished");

        ResponseEntity<byte[]> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        if (pdfFile != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(workflowName, workflowName + ".pdf");

            if (pdfFile.exists()) {
                response = new ResponseEntity<>(Files.readAllBytes(pdfFile.toPath()), headers, HttpStatus.OK);
                try {
                    pdfFile.delete();
                } catch (Exception e) {
                    logger.error("Cannot delete temporary pdf file: " + pdfFile.getAbsolutePath(), e);
                }
                writeUserActivityLog(admin, "export workflow", "WorkflowID: " + workflowId);
            }
        }

        return response;
    }

    @GetMapping("/mailing/{mailingId:\\d+}/thumbnail.action")
    public ResponseEntity<Integer> getMailingThumbnail(@PathVariable int mailingId, Admin admin) {
        int companyId = admin.getCompanyID();
        int componentId = componentDao.getImageComponent(companyId, mailingId, MailingComponentType.ThumbnailImage);
        return ResponseEntity.ok(componentId);
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private String getForward(String forwardName, String forwardParams, String forwardTargetItemId, int workflowId,
                                    List<WorkflowIcon> icons, Model model) {
        
        String redirectUrl = getRedirectUrl(forwardName, forwardTargetItemId);
        
        if (StringUtils.isEmpty(redirectUrl)) {
            return "redirect:/workflow/" + workflowId + "/view.action";
        }

        Map<String, String> paramsMap = AgnUtils.getParamsMap(forwardParams);

        // Validate and normalize nodeId parameter.
        int iconId = NumberUtils.toInt(paramsMap.get(WorkflowParametersHelper.WORKFLOW_NODE_ID));
        if (iconId > 0 && icons.stream().anyMatch(i -> i.getId() == iconId)) {
            paramsMap.put(WorkflowParametersHelper.WORKFLOW_NODE_ID, Integer.toString(iconId));
        } else {
            paramsMap.put(WorkflowParametersHelper.WORKFLOW_NODE_ID, "");
        }

        model.addAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS, AgnUtils.getParamsString(paramsMap));
        model.addAttribute(WorkflowParametersHelper.WORKFLOW_ID, workflowId);
        model.addAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID, NumberUtils.toInt(forwardTargetItemId));
        
        return "redirect:" + redirectUrl;
    }

    private String getForwardRedesigned(String forwardName, String forwardParams, String forwardTargetItemId, int workflowId,
                              List<WorkflowIcon> icons, Model model) {
        Map<String, String> paramsMap = AgnUtils.getParamsMap(forwardParams);

        // Validate and normalize nodeId parameter.
        int iconId = NumberUtils.toInt(paramsMap.get(WorkflowParametersHelper.WORKFLOW_NODE_ID));
        if (iconId > 0 && icons.stream().anyMatch(i -> i.getId() == iconId)) {
            paramsMap.put(WorkflowParametersHelper.WORKFLOW_NODE_ID, Integer.toString(iconId));
        } else {
            paramsMap.put(WorkflowParametersHelper.WORKFLOW_NODE_ID, "");
        }

        model.addAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_PARAMS, AgnUtils.getParamsString(paramsMap))
                .addAttribute(WorkflowParametersHelper.WORKFLOW_ID, workflowId)
                .addAttribute(WorkflowParametersHelper.WORKFLOW_FORWARD_TARGET_ITEM_ID, NumberUtils.toInt(forwardTargetItemId));

        return "redirect:" + WorkflowForward.from(forwardName).getUrl(forwardTargetItemId);
    }

    public static void updateForwardParameters(HttpSession session, String targetItemId, int workflowId, String forwardParams) {
        WorkflowParameters workflowParameters = new WorkflowParameters();

        workflowParameters.setWorkflowForwardTargetItemId(NumberUtils.toInt(targetItemId));
        workflowParameters.setWorkflowId(workflowId);
        workflowParameters.setWorkflowForwardParams(StringUtils.trimToEmpty(forwardParams));

        AgnUtils.saveWorkflowForwardParamsToSession(session, workflowParameters, true);
    }

    private void loadWorkflow(WorkflowForm workflowForm, Admin admin, Popups popups) {
        workflowForm.setUsingActivatedWorkflow("");
        workflowForm.setUsingActivatedWorkflowName("");
        workflowForm.setPartOfActivatedWorkflow("");
        workflowForm.setPartOfActivatedWorkflowName("");
        Workflow workflow = workflowService.getWorkflow(workflowForm.getWorkflowId(), admin.getCompanyID());
        if (INCOMPLETE_WORKFLOW_NAME.equals(workflow.getShortname())) {
            workflow.setShortname("");
        }

        switch (workflowForm.getStatus()) {
            case STATUS_ACTIVE:
                workflowForm.setStatusMaybeChangedTo(STATUS_INACTIVE);
                break;

            case STATUS_OPEN, STATUS_INACTIVE, STATUS_TESTED:
                workflowForm.setStatusMaybeChangedTo(STATUS_ACTIVE);
                break;

            case STATUS_NONE, STATUS_TESTING, STATUS_COMPLETE:
                workflowForm.setStatusMaybeChangedTo(STATUS_NONE);
                break;
			case STATUS_FAILED, STATUS_TESTING_FAILED: //$FALL-THROUGH$
			default:
				break;
        }

        List<WorkflowIcon> icons = workflow.getWorkflowIcons();
        // check if there are deleted mailings used in current workflow
        if (workflowService.hasDeletedMailings(icons, workflow.getCompanyId()) && popups.size() != 0) {
            popups.warning("error.workflow.containsDeletedContent");
        }

        workflowForm.setWorkflowSchema(workflowDataParser.serializeWorkflowIcons(icons));
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private String getRedirectUrl(String forwardName, String forwardTargetItemId) {
        switch (StringUtils.defaultString(forwardName)) {
            case FORWARD_USERFORM_CREATE:
                return "/webform/new.action";
            case FORWARD_USERFORM_EDIT:
                return "/webform/0/view.action";

            case FORWARD_TARGETGROUP_CREATE:
            case FORWARD_TARGETGROUP_CREATE_QB:
                return "/target/create.action";

            case FORWARD_TARGETGROUP_EDIT:
            case FORWARD_TARGETGROUP_EDIT_QB:
                return "/target/" + forwardTargetItemId + "/view.action";
                
            case FORWARD_ARCHIVE_CREATE:
                return "/mailing/archive/create.action";
            
            case FORWARD_MAILING_CREATE:
                return "/mailing/create.action";

            case FORWARD_MAILING_EDIT:
                return "/mailing/" + forwardTargetItemId + "/settings.action";
            case FORWARD_MAILING_COPY:
                return "/mailing/" + forwardTargetItemId + "/copy.action";
            case FORWARD_AUTOIMPORT_CREATE:
                return "/auto-import/create.action";
            case FORWARD_AUTOIMPORT_EDIT:
                return "/auto-import/" + forwardTargetItemId + "/view.action";
            case FORWARD_AUTOEXPORT_CREATE:
                return "/auto-export/create.action";
            case FORWARD_AUTOEXPORT_EDIT:
                return "/auto-export/" + forwardTargetItemId + "/view.action";
            default:
                return "";
        }
    }

    private Workflow getWorkflow(WorkflowForm form, Admin admin) {
        Workflow workflow = conversionService.convert(form, Workflow.class);
        workflow.setWorkflowId(form.getWorkflowId());
        workflow.setCompanyId(admin.getCompanyID());
        return workflow;
    }

    private void prepareViewPage(Admin admin, Model model, int workflowId) {
        // @todo we need to think whether we need to set all that to request or is it better to get that by ajax requests when it is needed
        int companyId = admin.getCompanyID();
        List<TargetLight> allTargets = workflowService.getAllTargets(companyId);
        model.addAttribute("profileFields", workflowService.getProfileFields(companyId));
        model.addAttribute("profileFieldsHistorized", recipientFieldService.getHistorizedFields(companyId));
        model.addAttribute("isMailtrackingActive", companyDao.isMailtrackingActive(companyId));
        model.addAttribute("admins", workflowService.getAdmins(companyId));
        model.addAttribute("allTargets", allTargets);
        model.addAttribute("campaigns", campaignDao.getCampaignList(companyId, "lower(shortname)", 1));
        model.addAttribute("allMailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("allUserForms", workflowService.getAllUserForms(companyId));
        }
        model.addAttribute("localeDateTimePattern", admin.getDateTimeFormat().toPattern());
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        model.addAttribute("adminTimezone", admin.getAdminTimezone());
        model.addAttribute("allWorkflows", workflowService.getWorkflowsOverview(admin));
        model.addAttribute("hasDeepTrackingTables", workflowService.hasCompanyDeepTrackingTables(companyId));
        model.addAttribute("allAutoImports", autoImportService == null
                ? new ArrayList<AutoImportLight>()
                : autoImportService.getListOfAutoImportsForWorkflow(workflowId, companyId));
        model.addAttribute("allAutoExports", autoExportService == null ? new ArrayList<AutoExport>() : autoExportService.getAutoExports(admin));
        if (!admin.isRedesignedUiUsed()) {
            model.addAttribute("allMailings", workflowService.getAllMailings(admin));
        }
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("splitTypes", SPLIT_TYPES);
        }
        addExtendedModelAttrs(admin, model, allTargets);
    }

    protected void addExtendedModelAttrs(Admin admin, Model model, List<TargetLight> allTargets) {
        model.addAttribute("accessLimitTargetId", 0);
        model.addAttribute("isExtendedAltgEnabled", false);
    }

    private Collection<Message> validateWorkflow(Admin admin, List<WorkflowIcon> icons, int workflowId, Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus) throws Exception {
        return validationService.validate(workflowId, icons, oldStatus, newStatus, admin).getErrorMessages();
    }

    private boolean validateStatusTransition(Workflow.WorkflowStatus currentStatus, Workflow.WorkflowStatus newStatus, List<Message> errors) {
        SimpleServiceResult result = validationService.validateStatusTransition(currentStatus, newStatus);
        errors.addAll(result.getErrorMessages());
        return result.isSuccess();
    }
    
    protected void writeUserActivityLog(Admin admin, String action, String description)  {
        writeUserActivityLog(admin, new UserAction(action, description));
    }

    protected void writeUserActivityLog(Admin admin, UserAction userAction)  {
        if (Objects.nonNull(userActivityLogService)) {
            userActivityLogService.writeUserActivityLog(admin, userAction, logger);
        } else {
            logger.error("Missing userActivityLogService in " + this.getClass().getSimpleName());
            logger.info(String.format("Userlog: %s %s %s", admin.getUsername(), userAction.getAction(),
                    userAction.getDescription()));
        }
    }

    private String getWorkflowDescription(WorkflowForm workflowForm) {
        return workflowForm.getShortname() + " (" + workflowForm.getWorkflowId() + ")";
    }

    private String getWorkflowDescription(Workflow workflow) {
        return WorkflowUtils.getWorkflowDescription(workflow);
    }

    private List<WorkflowIcon> getIcons(WorkflowForm form) {
        return workflowService.getIcons(form.getWorkflowSchema());
    }

    /**
     * Write to a User Activity Log a changes made to the workflow.
     * @param admin a current user entity.
     * @param oldWorkflow a workflow entity before the changes.
     * @param newWorkflow a workflow entity after the changes.
     */
    private void writeWorkflowChangeLog(Admin admin, Workflow oldWorkflow, Workflow newWorkflow) {
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

    private boolean writeWorkflowIconsChangeLog(Workflow newWorkflow, Workflow oldWorkflow, Admin admin) {
        List<WorkflowIcon> newIcons = newWorkflow.getWorkflowIcons();
        List<WorkflowIcon> oldIcons = oldWorkflow.getWorkflowIcons();

        boolean isChanged = false;

        for (WorkflowIcon newIcon : newIcons) {

            if(newIcon.getType() == WorkflowIconType.Constants.START_ID ||
                    newIcon.getType() == WorkflowIconType.Constants.STOP_ID) {
                // start and stop icon changes will be logged form workflow object fields in writeWorkflowChangeLog method.
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

    private boolean writeWorkflowIconChangeLog(WorkflowIcon newIcon, WorkflowIcon oldIcon, Workflow workflow, Admin admin) {
        boolean isChanged = false;

        if(newIcon.getType() == WorkflowIconType.Constants.RECIPIENT_ID) {
            WorkflowRecipientImpl newRecipientIcon = (WorkflowRecipientImpl) newIcon;
            WorkflowRecipientImpl oldRecipientIcon = (WorkflowRecipientImpl) oldIcon;

            if(writeWorkflowRecipientTargetChangeLog(newRecipientIcon, oldRecipientIcon, admin, workflow)) {
                isChanged = true;
            }

            if(writeWorkflowRecipientMailingListLog(newRecipientIcon, oldRecipientIcon, admin, workflow)) {
                isChanged = true;
            }

        }

        if(!isChanged && !newIcon.equals(oldIcon)) {
            String iconName = getIconNameByTypeId(newIcon);
            writeUserActivityLog(admin, "edit campaign " + iconName, getWorkflowDescription(workflow));
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
            case WorkflowIconType.Constants.RECIPIENT_ID: return WorkflowIconType.Constants.RECIPIENT_VALUE;
            case WorkflowIconType.Constants.ARCHIVE_ID: return WorkflowIconType.Constants.ARCHIVE_VALUE;
            case WorkflowIconType.Constants.FORM_ID: return WorkflowIconType.Constants.FORM_VALUE;
            case WorkflowIconType.Constants.MAILING_ID: return WorkflowIconType.Constants.MAILING_VALUE;
            case WorkflowIconType.Constants.ACTION_BASED_MAILING_ID: return WorkflowIconType.Constants.ACTION_BASED_MAILING_VALUE;
            case WorkflowIconType.Constants.MAILING_MEDIATYPE_SMS_ID: return WorkflowIconType.Constants.MAILING_MEDIATYPE_SMS_VALUE;
            case WorkflowIconType.Constants.MAILING_MEDIATYPE_POST_ID: return WorkflowIconType.Constants.MAILING_MEDIATYPE_POST_VALUE;
            case WorkflowIconType.Constants.DATE_BASED_MAILING_ID: return WorkflowIconType.Constants.DATE_BASED_MAILING_VALUE;
            case WorkflowIconType.Constants.FOLLOWUP_MAILING_ID: return WorkflowIconType.Constants.FOLLOWUP_MAILING_VALUE;
            case WorkflowIconType.Constants.IMPORT_ID: return WorkflowIconType.Constants.IMPORT_VALUE;
            case WorkflowIconType.Constants.EXPORT_ID: return WorkflowIconType.Constants.EXPORT_VALUE;
            case WorkflowIconType.Constants.SPLIT_ID: return WorkflowIconType.Constants.SPLIT_VALUE;
            default: throw new RuntimeException("Unknown icon type: " + icon.getType());
        }
    }

    private boolean writeWorkflowRecipientMailingListLog(WorkflowRecipientImpl newIcon, WorkflowRecipientImpl oldIcon, Admin admin, Workflow workflow) {
        boolean isChanged = newIcon.getMailinglistId() != oldIcon.getMailinglistId();

        if(isChanged) {
            writeUserActivityLog(admin, "edit campaign recipient",
                    getWorkflowDescription(workflow) + "; recipient icon with id: " + newIcon.getId() + "; mailing list changed");
        }

        return isChanged;
    }

    private boolean writeWorkflowRecipientTargetChangeLog(WorkflowRecipientImpl newIcon, WorkflowRecipientImpl oldIcon, Admin admin, Workflow workflow) {
        List<Integer> oldTargets = ListUtils.emptyIfNull(oldIcon.getTargets());
        List<Integer> newTargets = ListUtils.emptyIfNull(newIcon.getTargets());

        if (!oldTargets.equals(newTargets)) {
            writeUserActivityLog(admin, "edit campaign recipient",
                    getWorkflowDescription(workflow) + "; recipient icon with id: " + newIcon.getId() + "; target group changed");
            return true;
        }

        return false;
    }

    private boolean writeWorkflowStartChangeLog(Workflow newWorkflow, Workflow oldWorkflow, Admin admin) {
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

    private boolean writeWorkflowEndChangeLog(Workflow newWorkflow, Workflow oldWorkflow, Admin admin) {
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

    private boolean writeWorkflowFieldChangeLog(Admin admin, String descriptionPrefix, String oldValue, String newValue) {
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

    private boolean writeWorkflowStatusChangeLog(Workflow workflow, Workflow existedWorkflow, Admin admin) {
        if (workflow.getWorkflowId() == 0) {
            writeUserActivityLog(admin, "create campaign", getWorkflowDescription(workflow));
            return false;
        }

        Workflow.WorkflowStatus currentStatus = existedWorkflow.getStatus(), newStatus = workflow.getStatus();
        String statusAction = null;

        if(currentStatus == newStatus) {
            return false;
        }

        if ((currentStatus == Workflow.WorkflowStatus.STATUS_ACTIVE && newStatus == Workflow.WorkflowStatus.STATUS_INACTIVE)
                || WorkflowUtils.isStoppingOnPause(currentStatus, newStatus)) {
            statusAction = "do deactivate campaign";

        } else if ((currentStatus == Workflow.WorkflowStatus.STATUS_INACTIVE || currentStatus == Workflow.WorkflowStatus.STATUS_OPEN)
                && newStatus == Workflow.WorkflowStatus.STATUS_ACTIVE) {
            statusAction ="do activate campaign";

        } else if (WorkflowUtils.isPausing(currentStatus, newStatus)) {
            statusAction ="do pause campaign";

        } else if (WorkflowUtils.isUnpausing(currentStatus, newStatus)) {
            statusAction ="do unpause campaign";

        } else if (currentStatus == Workflow.WorkflowStatus.STATUS_TESTING && newStatus == Workflow.WorkflowStatus.STATUS_OPEN) {
            statusAction = "do stop test campaign";

        } else if (newStatus == Workflow.WorkflowStatus.STATUS_TESTING) {
            statusAction = "do start test campaign";
        }

        boolean isUpdated = false;
        if(statusAction != null){
            writeUserActivityLog(admin, statusAction, getWorkflowDescription(workflow));
            isUpdated = true;
        }

        return isUpdated;
    }

    // TODO: EMMGUI-714: remove after old design will be removed, cuz code moved to the WorkflowValidationService
    private boolean checkAndSetDuplicateMailing(Admin admin, Model model, List<WorkflowIcon> icons, boolean isActiveOrTesting) {
        List<Mailing> duplicatedMailings = mailingService.getDuplicateMailing(icons, admin.getCompanyID());

        if (duplicatedMailings.isEmpty()) {
            return false;
        }

        if (model instanceof RedirectAttributes attributes) {
            attributes.addFlashAttribute("affectedMailingsMessageType", isActiveOrTesting ? GuiConstants.MESSAGE_TYPE_ALERT : GuiConstants.MESSAGE_TYPE_WARNING);
            attributes.addFlashAttribute("affectedMailingsMessageKey", "error.workflow.mailingIsUsingInSeveralIcons");
            attributes.addFlashAttribute("affectedMailings", duplicatedMailings);
        } else {
            model.addAttribute("affectedMailingsMessageType", isActiveOrTesting ? GuiConstants.MESSAGE_TYPE_ALERT : GuiConstants.MESSAGE_TYPE_WARNING);
            model.addAttribute("affectedMailingsMessageKey", "error.workflow.mailingIsUsingInSeveralIcons");
            model.addAttribute("affectedMailings", duplicatedMailings);
        }

        return true;
    }

    private void setStatus(Admin admin, Workflow workflow, Workflow existingWorkflow, List<Message> errors, List<Message> warnings, boolean isValid) throws Exception {
        Workflow.WorkflowStatus currentStatus = existingWorkflow != null ? existingWorkflow.getStatus() : Workflow.WorkflowStatus.STATUS_NONE;
        Workflow.WorkflowStatus newStatus = workflow.getStatus();

        final int workflowId = workflow.getWorkflowId();

        if (isValid && (admin.isRedesignedUiUsed() || validateStatusTransition(currentStatus, newStatus, errors))) {
            if (newStatus == Workflow.WorkflowStatus.STATUS_ACTIVE || newStatus == Workflow.WorkflowStatus.STATUS_TESTING) {
                boolean testing = newStatus == Workflow.WorkflowStatus.STATUS_TESTING;
                boolean unpausing = WorkflowUtils.isUnpausing(currentStatus, newStatus);

                List<UserAction> userActions = new ArrayList<>();
                workflowService.deleteWorkflowTargetConditions(admin.getCompanyID(), workflowId);
                if (workflowActivationService.activateWorkflow(workflowId, admin, testing, unpausing, false, warnings, errors, userActions)) {
                    for (UserAction action : userActions) {
                        writeUserActivityLog(admin, action);
                    }
                } else {
                    newStatus = testing ? Workflow.WorkflowStatus.STATUS_TESTING_FAILED : Workflow.WorkflowStatus.STATUS_FAILED;
                }
            }
        } else {
            newStatus = currentStatus == Workflow.WorkflowStatus.STATUS_NONE ? Workflow.WorkflowStatus.STATUS_OPEN : currentStatus;
        }

        workflow.setStatus(newStatus);
        workflowService.changeWorkflowStatus(workflowId, admin.getCompanyID(), newStatus);
    }

    private static class UpperCaseKeySerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String key, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeFieldName(key.toUpperCase());
        }
    }

    @GetMapping("/{workflowId:\\d+}/statistic.action")
    public String statistic(@PathVariable int workflowId, @ModelAttribute("mailingStatisticForm") MailingStatisticForm form,
                            Admin admin, Model model, HttpSession session) {
        if (DateMode.NONE.equals(form.getDateMode())) {
            form.setDateMode(DateMode.SELECT_MONTH);
        }

        if (form.getMonth() == -1) {
            form.setMonth(YearMonth.now().getMonth());
        }
        if (form.getYear() == 0) {
            form.setYear(YearMonth.now().getYear());
        }

        Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());

        model.addAttribute("statWorkflowId", workflowId);
        model.addAttribute("workflowName", workflow.getShortname());
        model.addAttribute("workflowStatMailings", workflowStatisticsService.getStatMailings(workflowId, admin));
        model.addAttribute("birtUrl", workflowStatisticsService.getWorkflowStatUrl(workflow, admin, session.getId(), form));
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getYearList(workflowStatisticsService.getStartYear(workflowId, admin)));
        model.addAttribute("isWorkflowStatistics", true);

        return "stats_mailing_view";
    }
}
