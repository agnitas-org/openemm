/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpSession;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComMailing;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.ComCampaignDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingComponentDao;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.workflow.beans.Workflow;
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
import com.agnitas.emm.core.workflow.service.GenerationPDFService;
import com.agnitas.emm.core.workflow.web.forms.WorkflowDependencyValidationForm;
import com.agnitas.emm.core.workflow.web.forms.WorkflowForm;
import com.agnitas.messages.Message;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.editors.IntEnumEditor;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.BulkActionFrom;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.agnitas.web.forms.WorkflowParameters;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_ACTIVE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_INACTIVE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_NONE;
import static com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus.STATUS_OPEN;

@Controller
@RequestMapping("/workflow")
@PermissionMapping("workflow")
public class WorkflowController {
    private static final Logger logger = Logger.getLogger(WorkflowController.class);

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

    public static final String WORKFLOW_CUSTOM_CSS_STYLE = ".body{background-color: #fff;} #viewPort { display: inline-block !important; width: 100% !important;}";

    private ComWorkflowService workflowService;
    private ComWorkflowValidationService validationService;
    private ComWorkflowActivationService workflowActivationService;
    private ComWorkflowStatisticsService workflowStatisticsService;
    private AutoImportService autoImportService;
    private AutoExportService autoExportService;
    private ComWorkflowDataParser workflowDataParser;
    private ComCampaignDao campaignDao;
    private ComMailingDeliveryStatService deliveryStatService;
    private ComMailingComponentDao componentDao;
    private GenerationPDFService generationPDFService;
    private ComCompanyDao companyDao;
    private ConfigService configService;
    private WebStorage webStorage;
    private MailinglistApprovalService mailinglistApprovalService;
    private UserActivityLogService userActivityLogService;
    private ConversionService conversionService;
    private MailingService mailingService;


    public WorkflowController(ComWorkflowService workflowService, ComWorkflowValidationService validationService,
                              ComWorkflowActivationService workflowActivationService, ComWorkflowStatisticsService workflowStatisticsService,
                              @Autowired(required = false) AutoImportService autoImportService, @Autowired(required = false) AutoExportService autoExportService, ComWorkflowDataParser workflowDataParser,
                              ComCampaignDao campaignDao, ComMailingDeliveryStatService deliveryStatService, ComMailingComponentDao componentDao,
                              GenerationPDFService generationPDFService, ComCompanyDao companyDao, ConfigService configService,
                              WebStorage webStorage, MailinglistApprovalService mailinglistApprovalService, UserActivityLogService userActivityLogService,
                              ConversionService conversionService, MailingService mailingService) {
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
        this.generationPDFService = generationPDFService;
        this.companyDao = companyDao;
        this.configService = configService;
        this.webStorage = webStorage;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
        this.mailingService = mailingService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(WorkflowDependencyType.class, new IntEnumEditor<>(WorkflowDependencyType.class));
    }

    @GetMapping("/create.action")
    public String create(ComAdmin admin, Model model) throws Exception {
        WorkflowForm workflowForm = new WorkflowForm();
        workflowForm.setStatus(STATUS_OPEN);
        workflowForm.setStatusMaybeChangedTo(STATUS_ACTIVE);
        workflowForm.setWorkflowSchema("[]");
        prepareViewPage(admin, model);

        model.addAttribute("workflowForm", workflowForm);

        return "workflow_view";
    }

    @RequestMapping("/{id:\\d+}/view.action")
    public String view(ComAdmin admin, @PathVariable int id, Model model,
                       @RequestParam(name = "forwardParams", required = false) String forwardParams) throws Exception {
        if(id == 0) {
            return "redirect:/workflow/create.action";
        }

        WorkflowForm form = null;
        if(model.asMap().containsKey("workflowForm")) {
            form = (WorkflowForm) model.asMap().get("mailinglistForm");
        } else {
            Workflow workflow = workflowService.getWorkflow(id, admin.getCompanyID());
            if (Objects.nonNull(workflow)) {
                form = conversionService.convert(workflow, WorkflowForm.class);

            }
        }

        if(form == null) {
            form = new WorkflowForm();
        } else {
            writeUserActivityLog(admin, "mailing list view", getWorkflowDescription(form));
        }

        prepareViewPage(admin, model);
        model.addAttribute("workflowForm", form);
        
        model.addAllAttributes(AgnUtils.getParamsMap(forwardParams));

        return "workflow_view";
    }

    @GetMapping("/list.action")
    public String list(ComAdmin admin, @ModelAttribute("workflowForm") PaginationForm form, Model model) {
        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.WORKFLOW_OVERVIEW, form);

        JSONArray workflows = workflowService.getWorkflowListJson(admin.getCompanyID());
        model.addAttribute("workflowsJson", workflows);

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
    public String confirmDelete(ComAdmin admin, @PathVariable("id") int workflowId, WorkflowForm workflowForm, Popups popups) {
        Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
        if (workflow == null) {
            popups.alert("Error");
        } else {
            workflowForm.setWorkflowId(workflow.getWorkflowId());
            workflowForm.setShortname(workflow.getShortname());
        }
        return "workflow_delete_ajax";
    }

    @RequestMapping("/{id:\\d+}/delete.action")
    public String delete(ComAdmin admin, @PathVariable("id") int workflowId, Popups popups) {
        Workflow workflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
        if (workflow.getStatus() != Workflow.WorkflowStatus.STATUS_ACTIVE && workflow.getStatus() != Workflow.WorkflowStatus.STATUS_TESTING) {
            workflowService.deleteWorkflow(workflowId, admin.getCompanyID());

            popups.success("default.selection.deleted");
            writeUserActivityLog(admin, "delete campaign", getWorkflowDescription(workflow));
        } else {
            popups.alert("error.workflow.nodesShouldBeDisabledBeforeDeleting");
        }
        return "redirect:/workflow/list.action";
    }

    @PostMapping("/confirmBulkDelete.action")
    public String confirmBulkDelete(@ModelAttribute("bulkForm") BulkActionFrom form, Popups popups) {
        if (form.getBulkIds().size() == 0) {
            popups.alert("bulkAction.nothing.workflow");
        }

        return "workflow_bulkDeleteConfirm_ajax";
    }

    @PostMapping("/bulkDelete.action")
    public String bulkDelete(ComAdmin admin, BulkActionFrom form, Popups popups) {
        Set<Integer> workflowIdsToDelete = new HashSet<>();
        List<Workflow> workflows = workflowService.getWorkflowsByIds(new HashSet<>(form.getBulkIds()), admin.getCompanyID());

        for (Workflow workflow : workflows) {
            if (workflow.getStatus() != Workflow.WorkflowStatus.STATUS_ACTIVE && workflow.getStatus() != Workflow.WorkflowStatus.STATUS_TESTING) {
                workflowIdsToDelete.add(workflow.getWorkflowId());
            }
        }

        if (workflowIdsToDelete.size() != form.getBulkIds().size()) {
            popups.alert("error.workflow.nodesShouldBeDisabledBeforeDeleting");
        } else {
            workflowService.bulkDelete(workflowIdsToDelete, admin.getCompanyID());
            popups.success("default.selection.deleted");

            for (Workflow workflow : workflows) {
                if (workflowIdsToDelete.contains(workflow.getWorkflowId())) {
                    writeUserActivityLog(admin, "delete campaign", getWorkflowDescription(workflow));
                }
            }
        }

        return "redirect:/workflow/list.action";
    }

    @PostMapping("/confirmBulkDeactivate.action")
    public String confirmBulkDeactivate(@ModelAttribute("bulkForm") BulkActionFrom form, Popups popups) {
        if (form.getBulkIds().size() == 0) {
            popups.alert("bulkAction.nothing.workflow");
        }

        return "workflow_bulkDeactivateConfirm_ajax";
    }

    @PostMapping("/bulkDeactivate.action")
    public String bulkDeactivate(ComAdmin admin, BulkActionFrom form, Popups popups) throws Exception {
        Set<Integer> workflowIdsToDeactivate = new HashSet<>();
        List<Workflow> workflows = workflowService.getWorkflowsByIds(new HashSet<>(form.getBulkIds()), admin.getCompanyID());

        for (Workflow workflow : workflows) {
            switch (workflow.getStatus()) {
                case STATUS_ACTIVE:
                case STATUS_TESTING:
                    workflowIdsToDeactivate.add(workflow.getWorkflowId());
                    break;
                default:break;
            }
        }

        workflowService.bulkDeactivate(workflowIdsToDeactivate, admin.getCompanyID());

        popups.success("default.changes_saved");

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

        return "redirect:/workflow/list.action";
    }

    @PostMapping("/save.action")
    public String save(ComAdmin admin, @ModelAttribute("workflowForm") WorkflowForm workflowForm,
                       @RequestParam(value = "forwardName", required = false) String forwardName,
                       @RequestParam(value = "forwardParams", required = false) String forwardParams,
                       @RequestParam(value = "forwardTargetItemId", required = false) String forwardTargetItemId,
                       RedirectAttributes redirectModel,
                       HttpSession session,
                       Popups popups) throws Exception {
        List<Message> errors = new ArrayList<>();
        List<Message> warnings = new ArrayList<>();

        Workflow newWorkflow = getWorkflow(workflowForm, admin);
        Workflow existingWorkflow = workflowService.getWorkflow(newWorkflow.getWorkflowId(), newWorkflow.getCompanyId());

        Workflow.WorkflowStatus existingStatus = existingWorkflow != null ? existingWorkflow.getStatus() : Workflow.WorkflowStatus.STATUS_NONE;
        Workflow.WorkflowStatus newStatus = newWorkflow.getStatus() != Workflow.WorkflowStatus.STATUS_NONE ? newWorkflow.getStatus() : Workflow.WorkflowStatus.STATUS_OPEN;
        boolean isActiveOrTesting = newStatus == Workflow.WorkflowStatus.STATUS_ACTIVE || newStatus == Workflow.WorkflowStatus.STATUS_TESTING;

        if (StringUtils.isNotEmpty(forwardName) && StringUtils.length(newWorkflow.getShortname()) < 3) {
            newWorkflow.setShortname(INCOMPLETE_WORKFLOW_NAME);
        }

        // Running or complete campaign should never be saved.
        if (existingStatus.isChangeable()) {
            // Set OPEN_STATUS until validation passed and workflow is activated.
            newWorkflow.setStatus(Workflow.WorkflowStatus.STATUS_OPEN);
            workflowService.saveWorkflow(admin, newWorkflow, getIcons(workflowForm));
            newWorkflow.setStatus(newStatus);

            if (existingWorkflow == null) {
                writeUserActivityLog(admin, "create campaign", getWorkflowDescription(newWorkflow));
            } else {
                writeWorkflowChangeLog(admin, existingWorkflow, newWorkflow);
            }

            List<WorkflowIcon> icons = newWorkflow.getWorkflowIcons();
            if (StringUtils.isNotEmpty(forwardName)) {
                return getForward(forwardName, forwardParams, forwardTargetItemId, newWorkflow.getWorkflowId(), icons, redirectModel);
            }
            
            errors.addAll(validateWorkflow(admin, icons, newWorkflow.getWorkflowId(), newStatus));
            checkAndSetDuplicateMailing(admin, redirectModel, icons, isActiveOrTesting);

            boolean isValid = errors.isEmpty() && !redirectModel.containsAttribute("affectedMailings");
            setStatus(admin, newWorkflow, existingWorkflow, errors, warnings, isValid);
            
            if (errors.isEmpty()) {
                popups.success("default.changes_saved");
            }

            workflowForm.setWorkflowId(newWorkflow.getWorkflowId());
        } else {
            assert (existingWorkflow != null);

            if (StringUtils.isNotEmpty(forwardName)) {
                return getForward(forwardName, forwardParams, forwardTargetItemId, existingWorkflow.getWorkflowId(),
                        existingWorkflow.getWorkflowIcons(), redirectModel);
            }

            if (validateStatusTransition(existingStatus, newStatus, errors)) {
                workflowService.changeWorkflowStatus(existingWorkflow.getWorkflowId(), existingWorkflow.getCompanyId(), newStatus);
            }
            writeWorkflowStatusChangeLog(newWorkflow, existingWorkflow, admin);
        }

        if (isActiveOrTesting) {
            errors.forEach(popups::alert);
        } else {
            errors.forEach(popups::warning);
        }

        warnings.forEach(popups::warning);
        updateForwardParameters(session, forwardTargetItemId, workflowForm.getWorkflowId(), forwardParams);

        return String.format("redirect:/workflow/%d/view.action", workflowForm.getWorkflowId());
    }

    @PostMapping("/validateDependency.action")
    public ResponseEntity<?> validateDependency(ComAdmin admin, WorkflowDependencyValidationForm form) {
        WorkflowDependencyType type = form.getType();
        int workflowId = form.getWorkflowId();

        // A workflowId = 0 value is reserved for a new workflow.
        if (type == null || workflowId < 0) {
            // Type parameter is missing or invalid.
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            WorkflowDependency dependency = null;

            if (form.getEntityId() > 0) {
                dependency = type.forId(form.getEntityId());
            } else if (StringUtils.isNotEmpty(form.getEntityName())) {
                dependency = type.forName(form.getEntityName());
            }

            if (dependency == null) {
                // Either identifier or a name is required.
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                JSONObject data = new JSONObject();
                data.element("valid", workflowService.validateDependency(admin.getCompanyID(), workflowId, dependency));
                return new ResponseEntity<>(data, HttpStatus.OK);
            }
        }
    }

    @GetMapping("/getCurrentAdminTime.action")
    public ResponseEntity<String> getCurrentAdminTime(ComAdmin admin) {
        GregorianCalendar calendar = new GregorianCalendar(AgnUtils.getTimeZone(admin));
        int hours  = calendar.get(GregorianCalendar.HOUR_OF_DAY);
        int minutes = calendar.get(GregorianCalendar.MINUTE);
        return ResponseEntity.ok("{\"hour\":" + hours + ", \"minute\":" + minutes + "}");
    }

    @PostMapping("/getMailingLinks.action")
    public ResponseEntity<?> getMailingLinks(ComAdmin admin, @RequestParam int mailingId) {
        Map<Integer, String> links = workflowService.getMailingLinks(mailingId, admin.getCompanyID());

        JSONObject orderedLinks = new JSONObject();
        int index = 0;

        for (Map.Entry<Integer, String> entry : links.entrySet()) {
            JSONObject data = new JSONObject();

            data.element("id", entry.getKey());
            data.element("url", entry.getValue());

            orderedLinks.element(Integer.toString(index++), data);
        }

        return new ResponseEntity<>(orderedLinks, HttpStatus.OK);
    }

    @PostMapping(value = "/getMailingsByWorkStatus.action", produces = HttpUtils.APPLICATION_JSON_UTF8)
    public @ResponseBody ResponseEntity<?> getMailingsByWorkStatus(ComAdmin admin,
                            @RequestParam(value = "mailingId", required = false, defaultValue = "0") int mailingId,
                            @RequestParam("mailingTypes") String mailingTypes,
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
        } else {
            List<Integer> mailingTypeList = com.agnitas.reporting.birt.external.utils.StringUtils.buildListFormCommaSeparatedValueString(mailingTypes);
            boolean takeMailsForPeriod = Boolean.parseBoolean(takeMailsForPeriodParam);

            List<Map<String, Object>> mailings = workflowService.getAllMailings(companyId, mailingTypeList,
                    status, mailingStatus, takeMailsForPeriod, sort, order);


            if (StringUtils.isNotBlank(mailingsInCampaign)) {
                mailings.addAll(workflowService.getMailings(companyId, mailingsInCampaign));
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

            return new ResponseEntity<>(objectMapper.writeValueAsString(mailings), HttpStatus.OK);
        }
    }

    @GetMapping("/copy.action")
    public String copy(ComAdmin admin, Model model, @RequestParam int workflowId, @RequestParam boolean isWithContent) throws Exception {
        Workflow existedWorkflow = workflowService.getWorkflow(workflowId, admin.getCompanyID());
        Workflow workflow = workflowService.copyWorkflow(admin, workflowId, isWithContent);

        writeUserActivityLog(admin, "copy campaign " + (isWithContent ? "with" : "without") + " content",
                getWorkflowDescription(existedWorkflow) + " copied as " + getWorkflowDescription(workflow));

        prepareViewPage(admin, model);

        return String.format("redirect:/workflow/%d/view.action", workflow.getWorkflowId());
    }

    @PostMapping("/getAllMailingSorted.action")
    public ResponseEntity<List<LightweightMailing>> getAllMailingSorted(ComAdmin admin, @RequestParam("sortField") String sortField,
                                                                        @RequestParam("sortDirection") String sortDirection) {
        List<LightweightMailing> mailings = workflowService.getAllMailingsSorted(admin.getCompanyID(), sortField, sortDirection);

        return new ResponseEntity<>(mailings, HttpStatus.OK);
    }

    @PostMapping("/getWorkflowContent.action")
    public ResponseEntity<List<WorkflowIcon>> getWorkflowContent(ComAdmin admin, @RequestParam int workflowId, @RequestParam boolean isWithContent) {
        // We either reset all the icon content/settings or have to clone used mailings (if any).
        List<WorkflowIcon> icons = workflowService.getIconsForClone(admin, workflowId, isWithContent);

        if (icons == null) {
            icons = Collections.emptyList();
        }

        return ResponseEntity.ok(icons);
    }

    @PostMapping("/getSampleWorkflowContent.action")
    public ResponseEntity<List<WorkflowIcon>> getSampleWorkflowContent(@RequestParam String type) {
        List<WorkflowIcon> icons = ComSampleWorkflowFactory.createSampleWorkflow(type);

        if (icons == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(icons);
    }

    @PostMapping("/loadStatistics.action")
    public ResponseEntity<Map<Integer, List<String>>> loadStatistics(ComAdmin admin, @RequestParam int workflowId) {
        Map<Integer, List<String>> stats = workflowStatisticsService.getWorkflowStats(workflowId, admin.getCompanyID(), admin.getLocale());
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @PostMapping("/getMailingContent.action")
    public @ResponseBody Map<String, Object> getMailingContent(ComAdmin admin, @RequestParam int mailingId) {
        int companyId = admin.getCompanyID();
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

    @GetMapping("/viewOnlyElements.action")
    public String viewOnlyElements(@ModelAttribute("workflowForm") WorkflowForm form, ComAdmin admin, Model model,
                                   @RequestParam("showStatistics") String showStatistics, Popups popups) throws Exception {

        prepareViewPage(admin, model);
        model.addAttribute("showStatistics", showStatistics);

        loadWorkflow(form, admin, popups);
        writeUserActivityLog(admin, "view campaign", getWorkflowDescription(form));

        return "workflow_view_only_elements";
    }


    @GetMapping("/{workflowId:\\d+}/generatePDF.action")
    public ResponseEntity<byte[]> generatePDF(ComAdmin admin, @PathVariable int workflowId,
                                              @RequestParam("showStatistics") String showStatistics) throws Exception {
        String jsessionid = RequestContextHolder.getRequestAttributes().getSessionId();
        String hostUrl = configService.getValue(ConfigValue.SystemUrl);
        String url = hostUrl + "/workflow/viewOnlyElements.action;jsessionid=" + jsessionid + "?workflowId=" + workflowId + "&showStatistics=" + showStatistics;

        String workflowName = workflowService.getWorkflow(workflowId, admin.getCompanyID()).getShortname();
        File pdfFile = generationPDFService.generatePDF(configService.getValue(ConfigValue.WkhtmlToPdfToolPath), url, HttpUtils.escapeFileName(workflowName), admin, "wmLoadFinished", "Landscape", "workflow.single", WORKFLOW_CUSTOM_CSS_STYLE);

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

    @PostMapping("/getMailingThumbnail.action")
    public ResponseEntity<Integer> getMailingThumbnail(ComAdmin admin, @RequestParam int mailingId) {
        int companyId = admin.getCompanyID();
        int componentId = componentDao.getImageComponent(companyId, mailingId, MailingComponentType.ThumbnailImage.getCode());

        return ResponseEntity.ok(componentId);
    }

    private String getForward(String forwardName, String forwardParams, String forwardTargetItemId, int workflowId,
                                    List<WorkflowIcon> icons, Model model) {
        
        String redirectUrl = getRedirectUrl(forwardName);
        
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

    public static void updateForwardParameters(HttpSession session, String targetItemId, int workflowId, String forwardParams) {
        WorkflowParameters workflowParameters = new WorkflowParameters();

        workflowParameters.setWorkflowForwardTargetItemId(NumberUtils.toInt(targetItemId));
        workflowParameters.setWorkflowId(workflowId);
        workflowParameters.setWorkflowForwardParams(StringUtils.trimToEmpty(forwardParams));

        AgnUtils.saveWorkflowForwardParamsToSession(session, workflowParameters, true);
    }

    private void loadWorkflow(WorkflowForm workflowForm, ComAdmin admin, Popups popups) {
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

            case STATUS_OPEN:
            case STATUS_INACTIVE:
            case STATUS_TESTED:
                workflowForm.setStatusMaybeChangedTo(STATUS_ACTIVE);
                break;

            case STATUS_NONE:
            case STATUS_TESTING:
            case STATUS_COMPLETE:
                workflowForm.setStatusMaybeChangedTo(STATUS_NONE);
                break;
        }

        List<WorkflowIcon> icons = workflow.getWorkflowIcons();
        // check if there are deleted mailings used in current workflow
        if (workflowService.hasDeletedMailings(icons, workflow.getCompanyId()) && popups.size() != 0) {
            popups.warning("error.workflow.containsDeletedContent");
        }

        workflowForm.setWorkflowSchema(workflowDataParser.serializeWorkflowIcons(icons));
    }

    private String getRedirectUrl(String forwardName) {
        switch (StringUtils.defaultString(forwardName)) {
            case FORWARD_USERFORM_CREATE:
            case FORWARD_USERFORM_EDIT:
                return "/userform.do?action=2";
            
            case FORWARD_REPORT_CREATE:
                return "/statistics/report/new.action";
            case FORWARD_REPORT_EDIT:
                return "/statistics/report/0/view.action";
    
            case FORWARD_TARGETGROUP_CREATE:
            case FORWARD_TARGETGROUP_EDIT:
                return "/target.do?action=2";
            case FORWARD_TARGETGROUP_CREATE_QB:
                return "/targetQB.do?method=create";
            case FORWARD_TARGETGROUP_EDIT_QB:
                return "/targetQB.do?method=show";
                
            case FORWARD_ARCHIVE_CREATE:
                return "/campaign.do?action=4";
            
            case FORWARD_MAILING_CREATE:
                return "/mwStart.do?action=init";
            case FORWARD_MAILING_EDIT:
                return "/mailingbase.do?action=2&isTemplate=false";
            case FORWARD_MAILING_COPY:
                return "/mailingbase.do?action=10";
            
            case FORWARD_AUTOIMPORT_CREATE:
                return "/autoimport.do?method=create";
            case FORWARD_AUTOIMPORT_EDIT:
                return "/autoimport.do?method=view";
            case FORWARD_AUTOEXPORT_CREATE:
                return "/autoexport.do?method=create";
            case FORWARD_AUTOEXPORT_EDIT:
                return "/autoexport.do?method=view";
                default:
                    return "";
        }
    }

    private Workflow getWorkflow(WorkflowForm form, ComAdmin admin) {
        Workflow workflow = conversionService.convert(form, Workflow.class);
        workflow.setWorkflowId(form.getWorkflowId());
        workflow.setCompanyId(admin.getCompanyID());
        return workflow;
    }

    private void prepareViewPage(ComAdmin admin, Model model) throws Exception {
        // @todo we need to think whether we need to set all that to request or is it better to get that by ajax requests when it is needed
        int companyId = admin.getCompanyID();

        model.addAttribute("profileFields", workflowService.getProfileFields(companyId));
        model.addAttribute("profileFieldsHistorized", workflowService.getHistorizedProfileFields(companyId));
        model.addAttribute("isMailtrackingActive", companyDao.isMailtrackingActive(companyId));
        model.addAttribute("admins", workflowService.getAdmins(companyId));
        model.addAttribute("allReports", workflowService.getAllReports(companyId));
        model.addAttribute("allTargets", workflowService.getAllTargets(companyId));
        model.addAttribute("campaigns", campaignDao.getCampaignList(companyId, "lower(shortname)", 1));
        model.addAttribute("allMailinglists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        model.addAttribute("allUserForms", workflowService.getAllUserForms(companyId));
        model.addAttribute("localeDateNTimePattern", getLocaleDateNTimeFormat(admin).toPattern());
        model.addAttribute("localeDatePattern", ((SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, admin.getLocale())).toPattern());
        model.addAttribute("adminTimezone", admin.getAdminTimezone());
        model.addAttribute("allWorkflows", workflowService.getWorkflowsOverview(companyId));
        model.addAttribute("hasDeepTrackingTables", workflowService.hasCompanyDeepTrackingTables(companyId));
        model.addAttribute("allAutoImports", autoImportService == null ? new ArrayList<AutoImportLight>() : autoImportService.listAutoImports(companyId));
        model.addAttribute("allAutoExports", autoExportService == null ? new ArrayList<AutoExport>() : autoExportService.getAutoExportsOverview(admin));
        model.addAttribute("allMailings", workflowService.getAllMailings(companyId));
        model.addAttribute("isEnableTrackingVeto", configService.getBooleanValue(ConfigValue.EnableTrackingVeto, companyId));
    }

    private boolean validateStatusTransition(Workflow.WorkflowStatus currentStatus, Workflow.WorkflowStatus newStatus, List<Message> errors) {
        switch (currentStatus) {
            case STATUS_COMPLETE:
                errors.add(Message.of("error.workflow.campaignStatusHasNotBeUpdatedAfterCompleted"));
                return false;

            case STATUS_ACTIVE:
            case STATUS_TESTING:
                if (newStatus == Workflow.WorkflowStatus.STATUS_OPEN || newStatus == Workflow.WorkflowStatus.STATUS_INACTIVE) {
                    return true;
                }
                errors.add(Message.of("error.workflow.SaveActivatedWorkflow"));
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

    private SimpleDateFormat getLocaleDateNTimeFormat(ComAdmin admin) {
        return (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, admin.getLocale());
    }
    
    protected void writeUserActivityLog(ComAdmin admin, String action, String description)  {
        writeUserActivityLog(admin, new UserAction(action, description));
    }

    protected void writeUserActivityLog(ComAdmin admin, UserAction userAction)  {
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
        return workflow.getShortname() + " (" + workflow.getWorkflowId() + ")";
    }

    private List<WorkflowIcon> getIcons(WorkflowForm form) {
        String schema = form.getWorkflowSchema();

        if (StringUtils.isNotBlank(schema)) {
            return workflowDataParser.deSerializeWorkflowIconsList(schema);
        }

        return Collections.emptyList();
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

    private boolean writeWorkflowStatusChangeLog(Workflow workflow, Workflow existedWorkflow, ComAdmin admin) {
        if (workflow.getWorkflowId() == 0) {
            writeUserActivityLog(admin, "create campaign", getWorkflowDescription(workflow));
            return false;
        }

        Workflow.WorkflowStatus currentStatus = existedWorkflow.getStatus(), newStatus = workflow.getStatus();
        String statusAction = null;

        if(currentStatus == Workflow.WorkflowStatus.STATUS_ACTIVE && newStatus == Workflow.WorkflowStatus.STATUS_INACTIVE) {
            statusAction = "do deactivate campaign";

        } else if ((currentStatus == Workflow.WorkflowStatus.STATUS_INACTIVE || currentStatus == Workflow.WorkflowStatus.STATUS_OPEN)
                && newStatus == Workflow.WorkflowStatus.STATUS_ACTIVE) {
            statusAction ="do activate campaign";

        } else if (currentStatus == Workflow.WorkflowStatus.STATUS_TESTING && newStatus == Workflow.WorkflowStatus.STATUS_OPEN) {
            statusAction = "do stop test campaign";

        } else if (currentStatus == Workflow.WorkflowStatus.STATUS_OPEN && newStatus == Workflow.WorkflowStatus.STATUS_TESTING) {
            statusAction = "do start test campaign";
        }

        boolean isUpdated = false;
        if(statusAction != null){
            writeUserActivityLog(admin, statusAction, getWorkflowDescription(workflow));
            isUpdated = true;
        }

        return isUpdated;
    }

    private List<Message> validateWorkflow(ComAdmin admin, List<WorkflowIcon> icons, int workflowId, Workflow.WorkflowStatus status) {
        assert (admin != null);
        List<Message> messages = new ArrayList<>();

        final int companyId = admin.getCompanyID();
        final boolean isMailtrackingActive = companyDao.isMailtrackingActive(companyId);
        final TimeZone timezone = TimeZone.getTimeZone(admin.getAdminTimezone());
        final boolean isTesting = status == Workflow.WorkflowStatus.STATUS_TESTING;

        if (!validationService.isAllIconsFilled(icons)) {
            messages.add(Message.of("error.workflow.nodesShouldBeFilled"));
            if (icons.isEmpty()) {
                return messages;
            }
        }
        if (validationService.isStartIconMissing(icons) || validationService.isStopIconMissing(icons)) {
            messages.add(Message.of("error.workflow.campaignShouldHaveAtLeastOneStartAndEnd"));
        }
        if (!validationService.validateReminderRecipients(icons)) {
            messages.add(Message.of("error.email.invalid"));
        }
        if (!validationService.hasRecipient(icons)) {
            messages.add(Message.of("error.workflow.campaignShouldHaveRecipient"));
        }
        if (!validationService.isNotStartDateInPast(icons, timezone)) {
            messages.add(Message.of("error.workflow.campaignShouldNotHaveStartDateInPast"));
        }
        if (!validationService.isNotStopDateInPast(icons, timezone)) {
            messages.add(Message.of("error.workflow.campaignShouldNotHaveStopDateInPast"));
        }
        if (!validationService.noStartDateLaterEndDate(icons, timezone)) {
            messages.add(Message.of("error.validation.enddate"));
        }
        if (validationService.isReminderDateInThePast(icons, timezone)) {
            messages.add(Message.of("error.workflow.reminderDateInPast"));
        }
        if (!validationService.hasIconsConnections(icons)) {
            messages.add(Message.of("error.workflow.nodesShouldHaveIncomingAndOutgoingArrows"));
        }
        if (validationService.isAutoImportMisused(icons)) {
            messages.add(Message.of("error.workflow.autoImport.start.event"));
        }
        if (validationService.isAutoImportHavingShortDeadline(icons)) {
            messages.add(Message.of("error.workflow.autoImport.delay.tooShort", WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT));
        }
        if (validationService.decisionsHaveTwoOutgoingConnections(icons)) {
            if (!isMailtrackingActive && !validationService.decisionNegativePathConnectionShouldLeadToStopIcon(icons)) {
                messages.add(Message.of("error.workflow.decisionNegativePathConnectionShouldLeadToStopIcon"));
            }
        } else {
            messages.add(Message.of("error.workflow.decisionsShouldHaveTwoOutgoingConnections"));
        }
        if (!validationService.noParallelBranches(icons)) {
            messages.add(Message.of("error.workflow.ParallelBranches"));
        }
        if (!validationService.parametersSumNotHigher100(icons)) {
            messages.add(Message.of("error.workflow.ParametersSumNotHigher100"));
        }
        if (!validationService.noParallelCampaigns(icons)) {
            messages.add(Message.of("error.workflow.NoParallelCampaigns"));
        }
        if (!validationService.noMailingsBeforeRecipient(icons)) {
            messages.add(Message.of("error.workflow.NoMailingsBeforeRecipient"));
        }
        if (!validationService.checkMailingTypesCompatible(icons)) {
            messages.add(Message.of("error.workflow.mixedMailingTypes"));
        }
        if (!validationService.isFixedDeadlineUsageCorrect(icons)) {
            messages.add(Message.of("error.workflow.fixedDeadlineIsNotPermitted"));
        }
        if (!validationService.isDeadlineDateCorrect(icons, timezone)) {
            messages.add(Message.of("error.workflow.deadlineDateTooEarly"));
        }
        if (!validationService.mailingHasOnlyOneRecipient(icons)) {
            messages.add(Message.of("error.workflow.mailingHasOneRecipient"));
        }
        if (!validationService.campaignHasOnlyOneMailingList(icons)) {
            messages.add(Message.of("error.workflow.oneMailingList"));
        }
        if (!validationService.campaignHasOnlyMailingsAssignedToThisWorkflow(icons, companyId, workflowId)) {
            messages.add(Message.of("error.workflow.mailingIsUsingInAnotherCampaign"));
        }

        if (!validationService.isImportIsNotActive(icons, companyId)) {
            messages.add(Message.of("error.workflow.importIsActive"));
        }

        if (!validationService.isExportIsNotActive(icons, companyId)) {
            messages.add(Message.of("error.workflow.exportIsActive"));
        }

        if (validationService.containsSentMailings(icons, companyId)) {
            messages.add(Message.of("error.workflow.containsSentMailings"));
        }
        if (!isTesting) {
            if (validationService.isInvalidDelayForDateBase(icons)) {
                messages.add(Message.of("error.workflow.dateBased.invalid.delay"));
            }
        }

        if (workflowService.hasDeletedMailings(icons, companyId)) {
            messages.add(Message.of("error.workflow.containsDeletedContent"));
        }


        int mailingTrackingDataExpirationPeriod = 0;
        if (isMailtrackingActive) {
            mailingTrackingDataExpirationPeriod = companyDao.getSuccessDataExpirePeriod(companyId);
        }

        if (!validationService.noLoops(icons)) {
            messages.add(Message.of("error.workflow.NoLoops"));
            return messages;
        }

        if (!validationService.validateAutoOptimizationStructure(icons)) {
            messages.add(Message.of("error.workflow.connection.notAllowed"));
        }

        messages.addAll(validationService.validateStartTrigger(icons, companyId));
        messages.addAll(validateMailingTrackingUsage(icons, companyId, mailingTrackingDataExpirationPeriod));
        messages.addAll(validateReferencedProfileFields(icons, companyId));
        messages.addAll(validateOperatorsInDecisions(icons, companyId));

        return messages;
    }

    private List<Message> validateOperatorsInDecisions(List<WorkflowIcon> icons, int companyId) {
        List<Message> messages = new ArrayList<>();

        for (WorkflowIcon icon : icons) {
            if (icon.getType() == WorkflowIconType.DECISION.getId() && icon.isFilled()) {
                WorkflowDecision decision = (WorkflowDecision) icon;
                int criteriaId = decision.getDecisionCriteria().getId();
                if (criteriaId == WorkflowDecision.WorkflowDecisionCriteria.DECISION_PROFILE_FIELD.getId()) {
                    messages.addAll(validationService.validateDecisionRules(decision, companyId));
                }
            }
        }

        return messages;
    }

    private void checkAndSetDuplicateMailing(ComAdmin admin, Model model, List<WorkflowIcon> icons, boolean isActiveOrTesting) {
        List<Mailing> duplicatedMailings = mailingService.getDuplicateMailing(icons, admin.getCompanyID());
        if (!duplicatedMailings.isEmpty()) {
            if (model instanceof RedirectAttributes) {
                RedirectAttributes attributes = (RedirectAttributes) model;
                attributes.addFlashAttribute("affectedMailingsMessageType", isActiveOrTesting ? GuiConstants.MESSAGE_TYPE_ALERT : GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
                attributes.addFlashAttribute("affectedMailingsMessageKey", "error.workflow.mailingIsUsingInSeveralIcons");
                attributes.addFlashAttribute("affectedMailings", duplicatedMailings);
            } else {
                model.addAttribute("affectedMailingsMessageType", isActiveOrTesting ? GuiConstants.MESSAGE_TYPE_ALERT : GuiConstants.MESSAGE_TYPE_WARNING_PERMANENT);
                model.addAttribute("affectedMailingsMessageKey", "error.workflow.mailingIsUsingInSeveralIcons");
                model.addAttribute("affectedMailings", duplicatedMailings);
            }
        }
    }

    private List<Message> validateMailingTrackingUsage(List<WorkflowIcon> icons, @VelocityCheck int companyId, int trackingDays) {
        List<Message> messages = new ArrayList<>();

        // It's possible to show a separate error message for each case (e.g. listing names of affected mailings)
        // but for now just get rid of duplicated messages.
        Set<ComWorkflowValidationService.MailingTrackingUsageErrorType> reportedErrors = new HashSet<>();

        validationService.checkFollowupMailing(icons, companyId, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                messages.add(translateToActionMessage(e, trackingDays));
            }
        });

        validationService.checkMailingTrackingUsage(icons, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                messages.add(translateToActionMessage(e, trackingDays));
            }
        });

        validationService.checkMailingsReferencedInDecisions(icons, companyId, trackingDays).forEach(e -> {
            if (reportedErrors.add(e.getErrorType())) {
                messages.add(translateToActionMessage(e, trackingDays));
            }
        });

        return messages;
    }

    private List<Message> validateReferencedProfileFields(List<WorkflowIcon> workflowIcons, int companyId) {
        List<String> columns;
        List<Message> messages = new ArrayList<>();

        columns = validationService.checkTrackableProfileFields(workflowIcons, companyId);
        if (columns.size() > 0) {
            messages.add(Message.of("error.workflow.profiledb.missingTrackableColumns", "<br>" + StringUtils.join(columns, "<br>")));
        }

        columns = validationService.checkProfileFieldsUsedInConditions(workflowIcons, companyId);
        if (columns.size() > 0) {
            messages.add(Message.of("error.workflow.profiledb.missingColumnsForConditions", "<br>" + StringUtils.join(columns, "<br>")));
        }

        return messages;
    }

    private Message translateToActionMessage(ComWorkflowValidationService.MailingTrackingUsageError error, int trackingDays) {
        MailingTypes mailingType = MailingTypes.getByCode(error.getMailingType());
        switch (error.getErrorType()) {
            case BASE_MAILING_NOT_FOUND:
            case DECISION_MAILING_INVALID:
                if (mailingType == MailingTypes.ACTION_BASED || mailingType == MailingTypes.DATE_BASED) {
                    return Message.of("error.workflow.baseMailingNeedActivated", error.getMailingName());
                } else {
                    return Message.of("error.workflow.baseMailingNeedsSent", error.getMailingName());
                }

            case BASE_MAILING_DISORDERED:
                return Message.of("error.workflow.baseMailingAtFirst", error.getMailingName());

            case DECISION_MAILING_DISORDERED:
                return Message.of("error.workflow.decision.requiresMailingBefore", error.getMailingName());

            case MAILING_TRACKING_DISABLED:
                return Message.of("error.workflow.trackingRequired");

            case EXPIRATION_PERIOD_EXCEEDED:
                return Message.of("error.workflow.trackingtime", trackingDays);
                
			default:
				return Message.of("Error");
        }
    }

    private void setStatus(ComAdmin admin, Workflow workflow, Workflow existingWorkflow, List<Message> errors, List<Message> warnings, boolean isValid) throws Exception {
        Workflow.WorkflowStatus currentStatus = existingWorkflow != null ? existingWorkflow.getStatus() : Workflow.WorkflowStatus.STATUS_NONE;
        Workflow.WorkflowStatus newStatus = workflow.getStatus();

        final int workflowId = workflow.getWorkflowId();

        if (isValid && validateStatusTransition(currentStatus, newStatus, errors)) {
            if (newStatus == Workflow.WorkflowStatus.STATUS_ACTIVE || newStatus == Workflow.WorkflowStatus.STATUS_TESTING) {
                boolean testing = newStatus == Workflow.WorkflowStatus.STATUS_TESTING;

                List<UserAction> userActions = new ArrayList<>();
                workflowService.deleteWorkflowTargetConditions(admin.getCompanyID(), workflowId);
                if (workflowActivationService.activateWorkflow(workflowId, admin, testing, warnings, errors, userActions)) {

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
}
