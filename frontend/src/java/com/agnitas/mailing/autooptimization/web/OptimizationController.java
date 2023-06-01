/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtstatistics.optimization.dto.OptimizationStatisticDto;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.workflow.beans.WorkflowDecision;
import com.agnitas.mailing.autooptimization.form.OptimizationForm;
import com.agnitas.mailing.autooptimization.validation.OptimizationFormValidator;
import com.agnitas.mailing.autooptimization.beans.ComOptimization;
import com.agnitas.mailing.autooptimization.beans.impl.ComOptimizationImpl;
import com.agnitas.mailing.autooptimization.service.ComOptimizationScheduleService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.mailing.autooptimization.service.OptimizationIsFinishedException;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.impl.MaildropDeleteException;
import org.agnitas.emm.core.commons.util.Constants;
import org.agnitas.emm.core.mailing.MailingAllReadySentException;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/optimization")
@PermissionMapping("optimization")
public class OptimizationController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(OptimizationController.class);
    private static final DateFormat FORMATTER = new SimpleDateFormat(Constants.DATE_PATTERN_FULL);

    private final ComOptimizationService optimizationService;
    private final UserActivityLogService userActivityLogService;
    private final ComOptimizationScheduleService optimizationScheduleService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final BirtStatisticsService birtStatisticsService;
    private final OptimizationFormValidator formValidator;

    public OptimizationController(ComOptimizationService optimizationService, ComOptimizationScheduleService optimizationScheduleService,
                                  MailinglistApprovalService mailinglistApprovalService, BirtStatisticsService birtStatisticsService,
                                  UserActivityLogService userActivityLogService, OptimizationFormValidator formValidator) {

        this.optimizationService = optimizationService;
        this.optimizationScheduleService = optimizationScheduleService;
        this.userActivityLogService = userActivityLogService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.birtStatisticsService = birtStatisticsService;
        this.formValidator = formValidator;
    }

    @RequestMapping("/list.action")
    public String list(Admin admin, @ModelAttribute("form") OptimizationForm optimizationForm, Model model) {
        List<ComOptimization> optimizationsList = optimizationService.list(optimizationForm.getCampaignID(), admin.getCompanyID());
        model.addAttribute("optimizations", optimizationsList);

        return "campaign_autooptimization_list";
    }

    @GetMapping("/{id:\\d+}/view.action")
    public String view(Admin admin, @PathVariable int id, @ModelAttribute("optimizationForm") OptimizationForm form, Model model, RedirectAttributes ra) {
        int companyID = admin.getCompanyID();
        ComOptimization optimization = optimizationService.get(id, companyID);

        if (optimization == null) {
            logger.warn("Optimization view: Not found by id: {}", id);

            ra.addFlashAttribute("optimizationForm", form);
            return "redirect:/optimization/create.action";
        }

        setupFormData(form, optimization, admin);
        setCommonModelAttributes(companyID, optimization, admin, model);

        writeUserActivityLog(admin, new UserAction("view Auto-Optimization", getOptimizationDescription(optimization)));
        return "campaign_autooptimization_view";
    }

    @GetMapping("/create.action")
    public String create(Admin admin, @ModelAttribute("optimizationForm") OptimizationForm optimizationForm, Model model) {
        int companyID = admin.getCompanyID();

        ComOptimization optimization = new ComOptimizationImpl();
        optimization.setCampaignID(optimizationForm.getCampaignID());
        optimization.setCampaignName(optimizationForm.getCampaignName());
        optimization.setCompanyID(companyID);
        optimizationForm.setOptimization(optimization);
        optimizationForm.setTargetMode(Mailing.TARGET_MODE_AND);

        setCommonModelAttributes(companyID, optimization, admin, model);

        return "campaign_autooptimization_view";
    }

    @GetMapping("/{id:\\d+}/confirmDelete.action")
    public String confirmDelete(@PathVariable("id") int optimizationId, @ModelAttribute("optimizationForm") OptimizationForm form, Admin admin) {
        ComOptimization optimization = optimizationService.get(optimizationId, admin.getCompanyID());
        optimization.setCampaignName(form.getCampaignName());

        form.setOptimization(optimization);

        return "campaign_autooptimization_confirm_delete_ajax";
    }

    @RequestMapping(value = "/delete.action", method = {RequestMethod.POST, RequestMethod.DELETE})
    public String delete(Admin admin, @ModelAttribute OptimizationForm form, Popups popups, RedirectAttributes ra) {
        ComOptimization optimization = optimizationService.get(form.getOptimizationID(), form.getCompanyID());

        try {
            optimizationService.delete(optimization);

            popups.success("default.selection.deleted");
            writeUserActivityLog(admin, new UserAction("delete Auto-Optimization", getOptimizationDescription(optimization)));
        } catch (MaildropDeleteException e) {
            logger.error("Could not delete optimization with ID: {}", form.getOptimizationID());
            popups.alert("mailing.autooptimization.errors.delete", form.getShortname());
        }

        ra.addFlashAttribute("form", form);
        return "redirect:/optimization/list.action";
    }

    @PostMapping("/save.action")
    public String save(Admin admin, @ModelAttribute OptimizationForm form, Popups popups, RedirectAttributes ra) {
        if (!canSave(form, popups)) {
            return "messages";
        }

        int idBeforeSave = form.getOptimizationID();
        boolean isNewEntry = idBeforeSave == 0;

        ComOptimization cachedOptimization = null;
        if (!isNewEntry) {
            cachedOptimization = optimizationService.get(idBeforeSave, admin.getCompanyID());
        }

        ComOptimization savedOptimization = saveOptimization(form, admin);

        popups.success("default.changes_saved");

        if (isNewEntry) {
            writeUserActivityLog(admin, new UserAction("create Auto-Optimization", getOptimizationDescription(savedOptimization)));
        } else if (cachedOptimization != null) {
            writeAutoOptimizationChangesLog(savedOptimization, cachedOptimization, admin);
        } else {
            logger.error("Log Auto-Optimization changes error. No cached optimization");
        }

        ra.addFlashAttribute("optimizationForm", form);
        return String.format("redirect:/optimization/%d/view.action", savedOptimization.getId());
    }

    @PostMapping("/schedule.action")
    public String schedule(Admin admin, @ModelAttribute OptimizationForm form, Popups popups, RedirectAttributes ra) {
        if (!formValidator.validateSchedule(admin, form, popups)) {
            return "messages";
        }

        ComOptimization optimization = optimizationService.get(form.getOptimizationID(), admin.getCompanyID());

        try {
            optimization.setTestMailingsSendDate(admin.getDateTimeFormat().parse(form.getTestMailingsSendDateAsString()));
            optimization.setSendDate(admin.getDateTimeFormat().parse(form.getResultSendDateAsString()));
            optimizationScheduleService.scheduleOptimization(optimization);

            String testMailingsSendDate = FORMATTER.format(optimization.getTestMailingsSendDate());
            String sendDate = FORMATTER.format(optimization.getSendDate());

            writeUserActivityLog(admin, new UserAction("do schedule Auto-Optimization",
                    String.format("%s started. Send date of the test mailings: %s, send date of the final mailing: %s",
                            getOptimizationDescription(optimization),
                            testMailingsSendDate,
                            sendDate))
            );

            popups.success("default.changes_saved");

        } catch (ParseException e) {
            logger.error("Could not parse date : {}", form.getResultSendDateAsString(), e);
            popups.alert("mailing.autooptimization.errors.resultsenddate", admin.getDateTimeFormat().toPattern());

        } catch (MailingAllReadySentException e) {
            logger.error("Could not schedule optimization. One of the test mailings has been allready sent ! Optimization-ID: {}", optimization.getId());
            popups.alert("mailing.autooptimization.errors.schedule");
        } catch (OptimizationIsFinishedException e) {
            logger.error("Could not schedule optimization. Optimization has not the right state. Optimization-ID: {} Status-ID:  {}", optimization.getId(), optimization.getStatus());
            popups.alert("mailing.autooptimization.errors.schedule");
        } catch (MaildropDeleteException e) {
            logger.error("Could not schedule optimization. Previous unschedule failed ! Optimization-ID: {} Status-ID: {}", optimization.getId(), optimization.getStatus());
            popups.alert("mailing.autooptimization.errors.schedule");
        }

        ra.addFlashAttribute("optimizationForm", form);
        return String.format("redirect:/optimization/%d/view.action", form.getOptimizationID());
    }

    @RequestMapping("/unSchedule.action")
    public String unSchedule(Admin admin, @ModelAttribute OptimizationForm form, Popups popups, RedirectAttributes ra) {
        ComOptimization optimization = optimizationService.get(form.getOptimizationID(), admin.getCompanyID());

        try {
            optimizationScheduleService.unscheduleOptimization(optimization);
            writeUserActivityLog(admin, new UserAction("do unschedule Auto-Optimization", getOptimizationDescription(optimization) + " stopped"));
        } catch (MaildropDeleteException e) {
            logger.error(String.format("Could not unschedule optimization.%d Status-ID:  %d", optimization.getId(), optimization.getStatus()));
            popups.alert("mailing.autooptimization.errors.unschedule");

            return "messages";
        }

        popups.success("default.changes_saved");

        form.setTestMailingsSendDateAsString(null);
        form.setResultSendDateAsString(null);

        ra.addFlashAttribute("optimizationForm", form);
        return String.format("redirect:/optimization/%d/view.action", form.getOptimizationID());
    }

    private void setupFormData(OptimizationForm form, ComOptimization optimization, Admin admin) {
        form.setTargetExpression(optimization.getTargetExpression());
        form.setTargetMode(optimization.getTargetMode());

        if (optimization.getThreshold() > 0) {
            form.setThresholdString(Integer.toString(optimization.getThreshold()));
        }

        if (optimization.getSendDate() != null) {
            form.setResultSendDateAsString(admin.getDateTimeFormat().format(optimization.getSendDate()));
        }

        if (optimization.getTestMailingsSendDate() != null) {
            form.setTestMailingsSendDateAsString(admin.getDateTimeFormat().format(optimization.getTestMailingsSendDate()));
        }

        optimization.setCampaignName(form.getCampaignName());
        form.setOptimization(optimization);

        try {
            form.setReportUrl(getReportUrl(admin, form));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ComOptimization saveOptimization(OptimizationForm form, Admin admin) {
        ComOptimization optimization = form.getOptimization();

        optimization.setCompanyID(admin.getCompanyID());
        optimization.setTargetExpression(form.getTargetExpression());
        optimization.setTargetMode(form.getTargetMode());

        try {
            String testMailingsSendDateAsString = form.getTestMailingsSendDateAsString();
            if (StringUtils.isNotBlank(testMailingsSendDateAsString)) {
                optimization.setTestMailingsSendDate(admin.getDateTimeFormat().parse(testMailingsSendDateAsString));
            }
        } catch (ParseException e) {
            logger.error("Could not parse date : {}", form.getTestMailingsSendDateAsString(), e);
        }

        try {
            String resultSendDateAsString = form.getResultSendDateAsString();
            if (StringUtils.isNotBlank(resultSendDateAsString)) {
                optimization.setSendDate(admin.getDateTimeFormat().parse(resultSendDateAsString));
            }
        } catch (ParseException e) {
            logger.error("Could not parse date : {}", form.getResultSendDateAsString(), e);
        }

        optimization.setThreshold(NumberUtils.toInt(form.getThresholdString(), 0));

        int newOptimizationID = optimizationService.save(optimization);
        optimization.setId(newOptimizationID);

        return optimization;
    }

    private boolean canSave(OptimizationForm form, Popups popups) {
        boolean isValid = formValidator.validateName(form, popups);
        isValid &= formValidator.validateGroups(form, popups);
        isValid &= formValidator.validateThreshold(form, popups);

        return isValid;
    }

    public String getReportUrl(Admin admin, OptimizationForm optimizationForm) throws Exception {
        OptimizationStatisticDto optimizationStatisticDto = new OptimizationStatisticDto();

        optimizationStatisticDto.setFormat("html");
        optimizationStatisticDto.setMailingId(optimizationForm.getGroup1());
        optimizationStatisticDto.setOptimizationId(optimizationForm.getOptimizationID());
        optimizationStatisticDto.setCompanyId(optimizationForm.getCompanyID());
        optimizationStatisticDto.setRecipientType(CommonKeys.TYPE_ALL_SUBSCRIBERS);

        return birtStatisticsService.getOptimizationStatisticUrl(admin, optimizationStatisticDto);
    }

    private void setCommonModelAttributes(int companyID, ComOptimization optimization, Admin admin, Model model) {
        model.addAttribute("splitTypes", optimizationService.getSplitTypeList(companyID, optimization.getSplitType(), admin.getAdminLang()));
        model.addAttribute("mailingLists", mailinglistApprovalService.getEnabledMailinglistsForAdmin(admin));
        model.addAttribute("targets", optimizationService.getTargets(optimization.getTargetExpression(), companyID));
        model.addAttribute("chosenTargets", optimizationService.getChosenTargets(optimization.getTargetExpression(), companyID));
        model.addAttribute("groups", optimizationService.getTestMailingList(optimization));

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
    }

    private void writeAutoOptimizationChangesLog(ComOptimization newOptimization, ComOptimization oldOptimization, Admin admin) {
        try {
            String description = getOptimizationDescription(oldOptimization);

            // Log name changes
            if (!StringUtils.equals(oldOptimization.getShortname(), newOptimization.getShortname())) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s renamed as %s", description, newOptimization.getShortname())));
            }

            // Log description changes
            String oldDescription = StringUtils.trimToEmpty(oldOptimization.getDescription());
            String newDescription = StringUtils.trimToEmpty(newOptimization.getDescription());

            if (!oldDescription.equals(newDescription)) {
                String action;
                if (oldDescription.isEmpty()) {
                    action = "added";
                } else {
                    if (newDescription.isEmpty()) {
                        action = "removed";
                    } else {
                        action = "changed";
                    }
                }
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. Description %s", description, action)));
            }

            // Log mailing list changes
            if (oldOptimization.getMailinglistID() != newOptimization.getMailinglistID()) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. Mailing list changed from ID = %d to ID = %d",
                        description, oldOptimization.getMailinglistID(), newOptimization.getMailinglistID())));
            }

            // Log target group changes
            String oldTargetsExpr = StringUtils.trimToEmpty(oldOptimization.getTargetExpression());
            String newTargetsExpr = StringUtils.trimToEmpty(newOptimization.getTargetExpression());

            if (!oldTargetsExpr.equals(newTargetsExpr)) {
                Set<String> oldTargets = new HashSet<>(Arrays.asList(oldTargetsExpr.split("[,\\s]+")));
                Set<String> newTargets = new HashSet<>(Arrays.asList(oldTargetsExpr.split("[,\\s]+")));

                // Log removed groups
                for (String targetId : oldTargets) {
                    if (!newTargets.contains(targetId)) {
                        writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. Removed target group with ID = %s", description, targetId)));
                    }
                }

                // Log added groups
                for (String targetId : newTargets) {
                    if (!oldTargets.contains(targetId)) {
                        writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. Added target group with ID = %s", description, targetId)));
                    }
                }
            }

            // Log decision-criteria changes
            if (oldOptimization.getEvalType() != newOptimization.getEvalType()) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization",
                        String.format("%s. Decision-Criteria changed from %s to %s",
                                description,
                                getDecisionCriteriaName(oldOptimization.getEvalType()),
                                getDecisionCriteriaName(newOptimization.getEvalType())))
                );
            }

            // Log threshold changes
            if (oldOptimization.getThreshold() != newOptimization.getThreshold()) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. Threshold changed from %d to %d",
                        description, oldOptimization.getThreshold(), newOptimization.getThreshold())));
            }

            // Log "Check for duplicate records in emails" checked/unchecked
            if (oldOptimization.isDoubleCheckingActivated() != newOptimization.isDoubleCheckingActivated()) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. Check for duplicate records in emails %s",
                        description, newOptimization.isDoubleCheckingActivated() ? "checked" : "unchecked")));
            }

            // Log list-split changes
            if (!StringUtils.equals(oldOptimization.getSplitType(), newOptimization.getSplitType())) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s. List-Split changed", description)));
            }

            // Log test mailing changes
            if (oldOptimization.getGroup1() != newOptimization.getGroup1()) {
                writeTestGroupChanges(oldOptimization, 1, oldOptimization.getGroup1(), newOptimization.getGroup1(), admin);
            }
            if (oldOptimization.getGroup2() != newOptimization.getGroup2()) {
                writeTestGroupChanges(oldOptimization, 2, oldOptimization.getGroup2(), newOptimization.getGroup2(), admin);
            }
            if (oldOptimization.getGroup3() != newOptimization.getGroup3()) {
                writeTestGroupChanges(oldOptimization, 3, oldOptimization.getGroup3(), newOptimization.getGroup3(), admin);
            }
            if (oldOptimization.getGroup4() != newOptimization.getGroup4()) {
                writeTestGroupChanges(oldOptimization, 4, oldOptimization.getGroup4(), newOptimization.getGroup4(), admin);
            }
            if (oldOptimization.getGroup5() != newOptimization.getGroup5()) {
                writeTestGroupChanges(oldOptimization, 5, oldOptimization.getGroup5(), newOptimization.getGroup5(), admin);
            }

            if (logger.isInfoEnabled()) {
                logger.info("saveAuto-Optimization: save {}", description);
            }

        } catch (Exception e) {
            logger.error("Log Auto-Optimization changes error {}", e.getMessage(), e);
        }
    }

    private void writeTestGroupChanges(ComOptimization optimization, int groupIndex, int oldGroupId, int newGroupId, Admin admin) {
        if (oldGroupId == newGroupId) {
            return;
        }

        String description = String.format("%s. Test-Group %d", getOptimizationDescription(optimization), groupIndex);

        if (oldGroupId == 0) {
            writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s added, ID = %d", description, newGroupId)));
        } else {
            if (newGroupId == 0) {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s removed", description)));
            } else {
                writeUserActivityLog(admin, new UserAction("edit Auto-Optimization", String.format("%s changed from ID = %d to ID = %d",
                        description, oldGroupId, newGroupId)));
            }
        }
    }

    private String getOptimizationDescription(ComOptimization optimization) {
        return String.format("%s (%d)", optimization.getShortname(), optimization.getId());
    }

    private String getDecisionCriteriaName(WorkflowDecision.WorkflowAutoOptimizationCriteria criteria) {
        switch (criteria) {
            case AO_CRITERIA_CLICKRATE:
                return "Click rate";
            case AO_CRITERIA_OPENRATE:
                return "Open rate";
            case AO_CRITERIA_REVENUE:
                return "Revenue";
            default:
                return "Unknown Decision-Criteria";
        }
    }

    private void writeUserActivityLog(Admin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction, logger);
    }
}
