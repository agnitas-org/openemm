/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import com.agnitas.beans.Admin;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.PollingUid;
import com.agnitas.dao.ComDkimDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.components.entity.RecipientEmailStatus;
import com.agnitas.emm.core.components.form.MailingSendForm;
import com.agnitas.emm.core.components.form.MailingTestSendForm;
import com.agnitas.emm.core.components.service.MailingBlockSizeService;
import com.agnitas.emm.core.components.service.MailingDependencyService;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.bean.MailingDependentType;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSizeCalculationService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.NumericUtil;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.Tuple;
import org.agnitas.web.StrutsActionBase;
import org.agnitas.web.forms.WorkflowParametersHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class MailingSendController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingSendController.class);
    private static final String SEND_STATS_LOADING_KEY = "mailing_load_send_stats";
    private static final String MESSAGES_VIEW = "messages";
    private static final String MAILING_SUBJECT_ATTR = "mailingSubject";

    protected final MailingSendService mailingSendService;
    protected final ConfigService configService;
    protected final MailingService mailingService;
    private final MailingDeliveryBlockingService mailingDeliveryBlockingService;
    private final MailingSizeCalculationService mailingSizeCalculationService;
    private final MailingRecipientsService mailingRecipientsService;
    private final ComMailingDeliveryStatService deliveryStatService;
    private final MailingDependencyService mailingDependencyService;
    private final UserActivityLogService userActivityLogService;
    private final MailingStatisticsDao mailingStatisticsDao;
    private final ComMailingBaseService mailingBaseService;
    private final MailingBlockSizeService blockSizeService;
    private final BounceFilterService bounceFilterService;
    private final MailingStopService mailingStopService;
    private final AutoImportService autoImportService;
    private final MaildropService maildropService;
    private final ComTargetService targetService;
    private final GridServiceWrapper gridService;
    private final MailinglistDao mailinglistDao;
    private final ComMailingDao mailingDao;
    private final ComTargetDao targetDao;
    private final WebStorage webStorage;
    private final ComDkimDao dkimDao;

    public MailingSendController(MailingRecipientsService mailingRecipientsService, ComMailingDao mailingDao, ComMailingBaseService mailingBaseService, GridServiceWrapper gridService,
                                 ComTargetService targetService, ConfigService configService, MaildropService maildropService, ComMailingDeliveryStatService deliveryStatService,
                                 ComTargetDao targetDao, MailingService mailingService, MailingSendService mailingSendService, MailingSizeCalculationService mailingSizeCalculationService,
                                 MailingDependencyService mailingDependencyService, WebStorage webStorage, UserActivityLogService userActivityLogService, MailingBlockSizeService blockSizeService,
                                 MailingStopService mailingStopService, MailinglistDao mailinglistDao, AutoImportService autoImportService, ComDkimDao dkimDao,
                                 MailingDeliveryBlockingService mailingDeliveryBlockingService, MailingStatisticsDao mailingStatisticsDao, BounceFilterService bounceFilterService) {
        this.mailingRecipientsService = mailingRecipientsService;

        this.mailingDao = mailingDao;
        this.mailingBaseService = mailingBaseService;
        this.gridService = gridService;
        this.targetService = targetService;
        this.configService = configService;
        this.maildropService = maildropService;
        this.deliveryStatService = deliveryStatService;
        this.targetDao = targetDao;
        this.mailingService = mailingService;
        this.mailingSendService = mailingSendService;
        this.mailingSizeCalculationService = mailingSizeCalculationService;
        this.mailingDependencyService = mailingDependencyService;
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.blockSizeService = blockSizeService;
        this.mailingStopService = mailingStopService;
        this.autoImportService = autoImportService;
        this.mailinglistDao = mailinglistDao;
        this.mailingDeliveryBlockingService = mailingDeliveryBlockingService;
        this.dkimDao = dkimDao;
        this.mailingStatisticsDao = mailingStatisticsDao;
        this.bounceFilterService = bounceFilterService;
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(@PathVariable("mailingId") int mailingId, Admin admin, Model model, @ModelAttribute("form") MailingSendForm form, @ModelAttribute("testForm") MailingTestSendForm testForm) throws Exception {
        int companyID = admin.getCompanyID();

        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        fillFormWithMailingData(admin, mailing, form);
        loadDependents(companyID, form, mailing, model);
        fillModelData(mailing, admin, model, form.getTemplateId());
        loadDeliveryStatistics(admin, mailing, form, model);

        setSendButtonsControlAttributes(admin, mailing, model);

        // delivery time settings
        GregorianCalendar currentDate = new GregorianCalendar(AgnUtils.getTimeZone(admin));
        form.setSendHour(currentDate.get(Calendar.HOUR_OF_DAY));
        form.setSendMinute(currentDate.get(Calendar.MINUTE));

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        model.addAttribute("adminTargetGroupList", targetDao.getTestAndAdminTargetLights(companyID));

        if (mailing.getMailingType() == MailingType.INTERVAL) {
            addIntervalSettingsToForm(form, companyID);
        }

        return "mailing_send_new";
    }

    private void setSendButtonsControlAttributes(Admin admin, Mailing mailing, Model model) throws Exception {
        boolean canEnableSendWorldMailing = admin.permissionAllowed(Permission.MAILING_CAN_ALLOW) && mailingService.isMailingLocked(mailing);
        boolean isThresholdClearanceExceeded = mailing.getMailingType() == MailingType.DATE_BASED
                && mailingService.isThresholdClearanceExceeded(admin.getCompanyID(), mailing.getId());

        
        model.addAttribute("canSendWorldMailing", mailingSendService.canSendOrActivateMailing(admin, mailing));		// TODO Replace usage of attribute "canSendWorldMailing" by "canSendOrActivateMailing" in JSPs
        
        model.addAttribute("canSendOrActivateMailing", mailingSendService.canSendOrActivateMailing(admin, mailing));
        model.addAttribute("canEnableSendWorldMailing", canEnableSendWorldMailing);
        model.addAttribute("isThresholdClearanceExceeded", isThresholdClearanceExceeded);
    }

    @PostMapping("/datebased/activation/confirm.action")
    public String confirmDateBasedActivation(@ModelAttribute("form") MailingSendForm form, Admin admin, Model model, Popups popups) {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), companyID);

        if (!validateNeedTarget(mailing, popups)) {
            return MESSAGES_VIEW;
        }
        initFormValuesBeforeConfirmActivation(mailing, admin, form);
        String targetGroupsAsString = String.join(", ",
                targetService.getTargetNamesByIds(companyID, new HashSet<>(mailing.getTargetGroups())));

        model.addAttribute(MAILING_SUBJECT_ATTR, getMailingSubject(mailing));
        model.addAttribute("targetGroups", targetGroupsAsString);

        return "date_based_activation_confirm";
    }

    @PostMapping("/activate-date-based.action")
    public String activateDateBased(Admin admin, @ModelAttribute MailingSendForm form, HttpServletRequest req, Popups popups) throws Exception {
        int companyID = admin.getCompanyID();
        int mailingId = form.getMailingID();

        Mailing mailing = mailingDao.getMailing(mailingId, companyID);
        Runnable activationCallback = () -> {
            Date sendDate = parseFormSendDate(admin, form);
            mailingSendService.activateDateBasedMailing(admin, mailing, popups, sendDate, form.getAutoImportId());
        };

        activateMailing(mailing, admin, form, WorkflowParametersHelper.isWorkflowDriven(req), popups, activationCallback);

        return redirectToSendView(mailingId);
    }

    private String redirectToSendView(int mailingId) {
        return String.format("redirect:/mailing/send/%s/view.action", mailingId);
    }

    @PostMapping("/{mailingId:\\d+}/send-admin.action")
    public String sendAdmin(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute MailingTestSendForm form, Popups popups) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        mailingSendService.sendAdminMailing(mailing, admin, form, popups);
        checkLimitationForSend(companyID, popups);

        return redirectToSendView(mailingId);
    }

    @PostMapping("/{mailingId:\\d+}/send-test.action")
    public String sendTest(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute MailingTestSendForm form, Popups popups, RedirectAttributes ra) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        mailingSendService.sendTestMailing(mailing, form, admin, popups);
        checkLimitationForSend(companyID, popups);

        ra.addFlashAttribute("testForm", form);
        return redirectToSendView(mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/delivery/settings/view.action")
    public Object viewDeliverySettings(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute("form") MailingSendForm form, Model model, Popups popups, HttpSession session) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        SimpleServiceResult result = mailingBaseService.checkContentNotBlank(mailing);
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        if (!validateNeedDkimKey(admin, mailing, popups)) {
            return redirectToSendView(mailingId);
        }

        fillFormWithMailingData(admin, mailing, form);
        updateFollowupInfoIfNecessary(mailing, model);
        fillModelData(mailing, admin, model, form.getTemplateId());

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

        if (mailingService.isMissingNecessaryTargetGroup(mailing)) {
            popups.alert("error.mailing.rulebased_without_target");
        }

        if (form.getWorkflowId() != 0) {
            popups.warning("warning.workflow.delivery.stop");
        }

        PollingUid pollingUid = new PollingUid(session.getId(), SEND_STATS_LOADING_KEY + mailingId);

        Callable<ModelAndView> worker = () -> {
            fillFormForDeliverySettingsView(form, admin, mailing);

            Set<Integer> targetGroups = (Set<Integer>) mailing.getTargetGroups();
            if (targetGroups == null) {
                targetGroups = new HashSet<>();
            }

            model.addAttribute("autoImports", autoImportService.listAutoImports(companyID));

            model.addAttribute("targetGroupNames", targetDao.getTargetNamesByIds(companyID, targetGroups));
            model.addAttribute("isMailtrackExtended", configService.getBooleanValue(ConfigValue.MailtrackExtended, companyID));

            return new ModelAndView("mailing_delivery_settings", model.asMap());
        };

        return new Pollable<>(pollingUid, Pollable.LONG_TIMEOUT, new ModelAndView(String.format("redirect:/mailing/send/%d/delivery/settings/view.action", mailingId), form.toMap()), worker);
    }

    private void fillFormForDeliverySettingsView(MailingSendForm form, Admin admin, Mailing mailing) throws Exception {
        int companyID = admin.getCompanyID();
        Map<Integer, Integer> sentStatistics = mailingStatisticsDao.getSendStats(mailing, companyID);
        String reportEmail = admin.getStatEmail();

        if (reportEmail == null || reportEmail.isEmpty()) {
            reportEmail = admin.getEmail();
        }

        if (admin.getCompany().isAutoMailingReportSendActivated()) {
            form.setReportSendAfter24h(true);
            form.setReportSendAfter48h(true);
            form.setReportSendAfter1Week(true);
        }

        fillSentStatistics(form, sentStatistics);
        Integer totalSentCount = form.getSentStatistics().values()
                .stream()
                .reduce(0, Integer::sum);

        form.setTotalSentCount(totalSentCount);
        form.setReportSendEmail(reportEmail);
        form.setCheckForDuplicateRecords(configService.getBooleanValue(ConfigValue.PrefillCheckboxSendDuplicateCheck, companyID));
    }

    private void fillSentStatistics(MailingSendForm form, Map<Integer, Integer> sentStatistics) {
        int textEmailsCount = sentStatistics.get(MailingStatisticsDao.SEND_STATS_TEXT);
        int htmlEmailsCount = sentStatistics.get(MailingStatisticsDao.SEND_STATS_HTML);
        int offlineHtmlEmailsCount = sentStatistics.get(MailingStatisticsDao.SEND_STATS_OFFLINE);
        int totalCount = textEmailsCount + htmlEmailsCount + offlineHtmlEmailsCount;

        sentStatistics.remove(MailingStatisticsDao.SEND_STATS_TEXT);
        sentStatistics.remove(MailingStatisticsDao.SEND_STATS_HTML);
        sentStatistics.remove(MailingStatisticsDao.SEND_STATS_OFFLINE);

        for (Map.Entry<Integer, Integer> entry : sentStatistics.entrySet()) {
            form.setSentStatisticsItem(entry.getKey(), entry.getValue());
        }

        form.setTextEmailsCount(textEmailsCount);
        form.setHtmlEmailsCount(htmlEmailsCount);
        form.setOfflineHtmlEmailsCount(offlineHtmlEmailsCount);
        form.setSentStatisticsItem(0, totalCount);
    }

    @PostMapping("/confirm-send-world.action")
    public String confirmSendWorld(@ModelAttribute("form") MailingSendForm form, Popups popups, Admin admin, Model model) throws Exception {
        int mailingId = form.getMailingID();

        if (!NumericUtil.matchedUnsignedIntegerPattern(form.getMaxRecipients())) {
            popups.alert("error.maxRecipients.notNumeric");
            return String.format("redirect:/mailing/send/%s/delivery/settings/view.action", mailingId);
        }

        int companyID = admin.getCompanyID();

        if (!checkLimitationForSend(companyID, popups)) {
            return redirectToSendView(mailingId);
        }

        Date sendDate = parseFormSendDate(admin, form);
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        mailingSendService.checkIfMailingCanBeSend(mailing, sendDate, AgnUtils.getTimeZone(admin));

        fillFormWithMailingData(admin, mailing, form);

        model.addAttribute("potentialSendDate", admin.getDateFormat().format(sendDate));
        model.addAttribute("potentialSendTime", admin.getTimeFormat().format(sendDate));
        model.addAttribute("adminDateFormat", admin.getDateFormat().toPattern());
        model.addAttribute(MAILING_SUBJECT_ATTR, getMailingSubject(mailing));

        int recipientsCount = calculateRecipientsCount(form, mailing, companyID);

        NumberFormat formatter = NumberFormat.getNumberInstance(admin.getLocale());
        model.addAttribute("recipientsCount", formatter.format(recipientsCount));

        model.addAttribute("mailinglistShortname", mailinglistDao.getMailinglistName(mailing.getMailinglistID(), companyID));
        loadSteppingBlocksize(form, mailing.getMaildropStatus());

        return "mailing_send_confirm_ajax_new";
    }

    private Date parseFormSendDate(Admin admin, MailingSendForm form) {
        GregorianCalendar sendDate = new GregorianCalendar(AgnUtils.getTimeZone(admin));

        int year = Integer.parseInt(form.getSendDate().substring(0, 4));
        int month = Integer.parseInt(form.getSendDate().substring(4, 6)) - 1;
        int day = Integer.parseInt(form.getSendDate().substring(6, 8));

        sendDate.set(year, month, day, form.getSendHour(), form.getSendMinute());

        return sendDate.getTime();
    }

    private void loadSteppingBlocksize(MailingSendForm form, Set<MaildropEntry> maildrops) {
        for (MaildropEntry drop : maildrops) {
            if (drop.getStatus() == MaildropStatus.WORLD.getCode()) {
                form.setStepping(drop.getStepping());
                form.setBlocksize(drop.getBlocksize());
            }
        }

        int calculatedBlockSize = blockSizeService.calculateBlocksize(form.getStepping(), form.getBlocksize());
        form.setBlocksize(calculatedBlockSize);
    }

    private int calculateRecipientsCount(MailingSendForm form, Mailing mailing, int companyID) throws Exception {
        int recipientsAmount = form.getTextEmailsCount() + form.getHtmlEmailsCount() + form.getOfflineHtmlEmailsCount();

        // Sum up other mediatypes
        Map<Integer, Integer> sentStatistics = mailingStatisticsDao.getSendStats(mailing, companyID);
        fillSentStatistics(form, sentStatistics);

        for (Map.Entry<Integer, Integer> entry : form.getSentStatistics().entrySet()) {
            if (entry.getKey() > 0 && entry.getValue() > 0) {
                recipientsAmount += entry.getValue();
            }
        }

        if (Integer.parseInt(form.getMaxRecipients()) <= 0) {
            return recipientsAmount;
        }

        return Math.min(Integer.parseInt(form.getMaxRecipients()), recipientsAmount);
    }

    @PostMapping("/send-world.action")
    public String sendWorldMailing(@ModelAttribute MailingSendForm form, Admin admin, Popups popups) throws Exception {
        int companyID = admin.getCompanyID();
        int mailingId = form.getMailingID();

        Mailing mailingToSend = mailingDao.getMailing(mailingId, companyID);

        if (validateMailingSize(mailingToSend, popups, admin) && checkLimitationForSend(companyID, popups)) {
            Date sendDate = parseFormSendDate(admin, form);

            if (isPostMailing(mailingToSend)) {
                createPostTrigger(mailingToSend, sendDate);
            } else {
                mailingSendService.sendWorldMailing(admin, form, popups, mailingToSend, sendDate);
            }
        }

        return redirectToSendView(mailingId);
    }

    protected boolean createPostTrigger(Mailing mailing, Date sendDate) throws Exception {
        return false;
    }

    protected boolean isPostMailing(Mailing mailing) {
        return false;
    }

    protected boolean checkLimitationForSend(int companyId, Popups popups) {
        if (mailingSendService.isLimitationForSendExists(companyId)) {
            popups.alert("error.company.mailings.sent.forbidden");
            return false;
        }

        return true;
    }

    @PostMapping("/deactivate/confirm.action")
    public String confirmDeactivation(@ModelAttribute("form") MailingSendForm form, Admin admin) {
        form.setShortname(mailingDao.getMailingName(form.getMailingID(), admin.getCompanyID()));
        return "mailing_deactivation_confirm";
    }

    @PostMapping("/deactivate.action")
    public String deactivate(@ModelAttribute MailingSendForm form, Admin admin, HttpServletRequest req) {
        int mailingId = form.getMailingID();
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());

        if (mailingSendService.deactivateMailing(mailing, admin.getCompanyID(), WorkflowParametersHelper.isWorkflowDriven(req))) {
            mailingDeliveryBlockingService.unblock(mailingId);
            writeUserActivityLog(admin, "do cancel mailing",
                    String.format("Mailing type: %s. %s", mailing.getMailingType().name(), getTriggerMailingDescription(mailing)));
        }

        return redirectToSendView(mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/delivery-status-box/load.action")
    public String loadDeliveryStatusBox(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute("form") MailingSendForm form, Model model) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        form.setMailingID(mailingId);
        form.setWorkStatus(mailingDao.getWorkStatus(companyID, mailingId));
        loadDeliveryStatistics(admin, mailing, form, model);

        model.addAttribute("isPostMailing", isPostMailing(mailing));
        model.addAttribute("mailtrackingEnabled", admin.getCompany().getMailtracking() != 0);
        model.addAttribute("isTransmissionRunning", mailingDao.isTransmissionRunning(mailingId));

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

        return "mailing_delivery_status_box";
    }

    @GetMapping("/{mailingId:\\d+}/unlock.action")
    public String unlock(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) throws Exception {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());

        if (mailing != null) {
            mailingSendService.unlockMailing(mailing);
            writeMailingApprovalLog(mailing, admin);

            if (mailing.getMailingType() == MailingType.FOLLOW_UP) {
                checkFollowUpBaseMailingState(mailing.getId(), admin, popups);
            }
        }

        return redirectToSendView(mailingId);
    }

    private void writeMailingApprovalLog(Mailing mailing, Admin admin) {
        writeUserActivityLog(admin, "approval", String.format("Mailing %s approved", getTriggerMailingDescription(mailing)));
        LOGGER.warn(
                "Mailing {} approved by {}.",
                getTriggerMailingDescription(mailing),
                admin.getFullUsername()
        );
    }

    private void checkFollowUpBaseMailingState(int mailingId, Admin admin, Popups popups) throws Exception {
        String followUpFor = mailingDao.getFollowUpFor(mailingId);

        if (StringUtils.isNotEmpty(followUpFor)) {
            int baseMailingId = Integer.parseInt(followUpFor);

            boolean isBasicMailingSent = mailingDao.getLastSendDate(baseMailingId) != null;

            if (!isBasicMailingSent) {
                popups.warning("warning.mailing.followup.basemail_was_not_sent");
            } else {
                List<Mailing> availableBaseMailings = mailingDao.getMailings(admin.getCompanyID(), admin.getAdminID(), ComMailingLightService.TAKE_ALL_MAILINGS, "W", true);

                boolean isBaseMailingAvailable = availableBaseMailings.stream()
                        .anyMatch(m -> m.getId() == baseMailingId);

                if (!isBaseMailingAvailable) {
                    popups.alert("error.mailing.followup.basemail_data_not_exists");
                }
            }
        }
    }

    @PostMapping("/{mailingId:\\d+}/resume-sending.action")
    public String resumeSending(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) {
        mailingService.resumeDateBasedSending(admin.getCompanyID(), mailingId);
        popups.success("default.changes_saved");

        return MESSAGES_VIEW;
    }

    @PostMapping("/save-statusmail-recipients.action")
    public String saveStatusmailRecipients(@ModelAttribute MailingSendForm form, Popups popups) {
        RecipientEmailStatus status = mailingRecipientsService.saveStatusMailRecipients(form.getMailingID(), form.getStatusmailRecipients());

        switch (status) {
            case OK:
                popups.success("default.changes_saved");
                break;
            case DUPLICATED:
                popups.alert("error.email.duplicated");
                break;
            case BLACKLISTED:
                popups.alert("error.email.blacklisted");
                break;
            case WRONG:
                popups.alert("error.email.wrong");
                break;
            default:
                break;
        }

        return redirectToSendView(form.getMailingID());
    }

    @PostMapping("/actionbased/activation/confirm.action")
    public String confirmActionBasedActivation(@ModelAttribute("form") MailingSendForm form, Admin admin, Model model) {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), companyID);

        initFormValuesBeforeConfirmActivation(mailing, admin, form);
        model.addAttribute(MAILING_SUBJECT_ATTR, getMailingSubject(mailing));

        return "action_based_activation_confirm";
    }

    private void initFormValuesBeforeConfirmActivation(Mailing mailing, Admin admin, MailingSendForm form) {
        Tuple<Long, Long> mailingSizes = mailingSizeCalculationService.calculateSize(mailing, admin);

        form.setShortname(mailing.getShortname());
        form.setApproximateMaxSizeWithoutExternalImages(mailingSizes.getFirst());
        form.setApproximateMaxSize(mailingSizes.getSecond());
    }

    @PostMapping("/activate-action-based.action")
    public String activateActionBased(Admin admin, @ModelAttribute("form") MailingSendForm form, HttpServletRequest req, Popups popups) throws Exception {
        int companyID = admin.getCompanyID();
        int mailingId = form.getMailingID();

        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        Runnable activationCallback = () -> {
            mailingSendService.activateActionBasedMailing(admin, mailing, popups);
        };

        activateMailing(mailing, admin, form, WorkflowParametersHelper.isWorkflowDriven(req), popups, activationCallback);

        return redirectToSendView(mailingId);
    }

    private void activateMailing(Mailing mailing, Admin admin, MailingSendForm form, boolean isWorkflowDriven, Popups popups, Runnable activationCallback) throws Exception {
        int companyID = admin.getCompanyID();

        if (!isWorkflowDriven && validateMailingSize(mailing, popups, admin) && checkLimitationForSend(companyID, popups)
                && validateNeedTarget(mailing, popups)) {

            if (mailingService.containsInvalidTargetGroups(companyID, mailing.getId())) {
                popups.alert("error.mailing.containsInvaidTargetGroups");
            } else {
                if (targetsHaveDisjunction(admin, mailing)) {
                    popups.warning("warning.mailing.target.disjunction");
                }

                if (isPostMailing(mailing)) {
                    Date sendDate = parseFormSendDate(admin, form);

                    if (createPostTrigger(mailing, sendDate)) {
                        String actionDescription = String.format("Mailing type: %s, at: %s. %s",
                                mailing.getMailingType().name(),
                                DateUtilities.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK).format(sendDate),
                                getTriggerMailingDescription(mailing));

                        writeUserActivityLog(admin, "do schedule post mailing", actionDescription);
                    }
                } else {
                    activationCallback.run();
                }
            }
        } else {
            mailingDao.updateStatus(mailing, MailingStatus.DISABLE);
        }

        if (!popups.hasAlertPopups()) {
            mailingDao.updateStatus(mailing, MailingStatus.ACTIVE);
        }
    }

    private boolean targetsHaveDisjunction(Admin admin, Mailing mailing) {
        if (mailing.getMailingType() == MailingType.DATE_BASED && CollectionUtils.size(mailing.getTargetGroups()) > 1) {
            return !mailingService.isMailingTargetsHaveConjunction(admin, mailing);
        }

        return false;
    }

    @RequestMapping("/{mailingId:\\d+}/confirm-cancel.action")
    public String confirmCancel(@PathVariable("mailingId") int mailingId, Admin admin, Model model) {
        model.addAttribute("mailingId", mailingId);
        model.addAttribute("mailingShortname", mailingDao.getMailingName(mailingId, admin.getCompanyID()));

        return "mailing_cancel_generation_question_ajax_new";
    }

    @PostMapping("/{mailingId:\\d+}/cancel.action")
    public String cancel(@PathVariable("mailingId") int mailingId, Admin admin) {
        int companyID = admin.getCompanyID();
        LightweightMailing mailing = mailingDao.getLightweightMailing(companyID, mailingId);

        if (mailing != null) {
            if (mailingSendService.cancelMailingDelivery(mailingId, companyID)) {
                writeUserActivityLog(admin, "do cancel mailing",
                        String.format("Mailing type: %s. %s (%d)", mailing.getMailingType().name(), mailing.getShortname(), mailingId));

                mailingDao.updateStatus(mailingId, MailingStatus.CANCELED);
            }
        } else {
            LOGGER.warn("mailing cancel: could not load mailing with ID: {}", mailingId);
        }

        mailingDeliveryBlockingService.unblock(mailingId);

        return redirectToSendView(mailingId);
    }

    @RequestMapping("/{mailingId:\\d+}/confirm-resume.action")
    public String confirmResume(@PathVariable("mailingId") int mailingId, Admin admin, Model model) {
        model.addAttribute("mailingId", mailingId);
        model.addAttribute("mailingShortname", mailingDao.getMailingName(mailingId, admin.getCompanyID()));

        return "mailing_resume_generation_question_ajax_new";
    }

    @PostMapping("/{mailingId:\\d+}/resume.action")
    public String resume(@PathVariable("mailingId") int mailingId, Admin admin) {
        int companyID = admin.getCompanyID();

        mailingDeliveryBlockingService.resumeBlockingIfNeeded(mailingId, companyID);

        try {
            if (mailingStopService.resumeMailing(admin.getCompanyID(), mailingId)) {
                String mailingName = mailingDao.getMailingName(mailingId, companyID);
                writeUserActivityLog(admin, "do resume mailing", String.format("Mailing: %s (%d)", mailingName, mailingId));
            }
        } catch (MailingStopServiceException e) {
            LOGGER.error(String.format("Error resuming mailing %d", mailingId), e);
        }

        return redirectToSendView(mailingId);
    }

    @RequestMapping("/{mailingId:\\d+}/confirm-resume-by-copy.action")
    public String confirmResumeByCopy(@PathVariable("mailingId") int mailingId, Admin admin, Model model) {
        model.addAttribute("mailingId", mailingId);
        model.addAttribute("mailingShortname", mailingDao.getMailingName(mailingId, admin.getCompanyID()));

        return "mailing_resume_generation_by_copy_question_ajax_new";
    }

    @PostMapping("/{mailingId:\\d+}/resume-by-copy.action")
    public String resumeByCopy(@PathVariable("mailingId") int mailingId, Admin admin) throws MailingStopServiceException {
        int newMailingID = mailingStopService.copyMailingForResume(admin, mailingId);
        return admin.permissionAllowed(Permission.MAILING_SETTINGS_MIGRATION)
                ? String.format("redirect:/mailing/%d/settings.action?keepForward=true", newMailingID)
                : String.format("redirect:/mailingbase.do?action=%s&keepForward=true&mailingID=%s", StrutsActionBase.ACTION_VIEW, newMailingID);
    }

    private void fillFormWithMailingData(Admin admin, Mailing mailing, MailingSendForm form) {
        int workflowId = mailingBaseService.getWorkflowId(mailing.getId(), mailing.getCompanyID());
        int gridTemplateId = gridService.getGridTemplateIdByMailingId(mailing.getId());
        Tuple<Long, Long> mailingSizes = mailingSizeCalculationService.calculateSize(mailing, admin);

        form.setShortname(mailing.getShortname());
        form.setWorkflowId(workflowId);
        form.setMailingGrid(gridTemplateId > 0);
        form.setMailingID(mailing.getId());
        form.setHasDeletedTargetGroups(targetService.hasMailingDeletedTargetGroups(mailing));
        form.setIsTemplate(mailing.isIsTemplate());
        form.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailing.getId()));
        form.setMailingtype(mailing.getMailingType().getCode());
        form.setWorldMailingSend(maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID()));
        form.setApproximateMaxSizeWithoutExternalImages(mailingSizes.getFirst());
        form.setApproximateMaxSize(mailingSizes.getSecond());
        form.setSizeWarningThreshold(configService.getLongValue(ConfigValue.MailingSizeWarningThresholdBytes, admin.getCompanyID()));
        form.setPrioritizationDisallowed(!mailing.isPrioritizationAllowed());
        form.setEncryptedSend(mailing.isEncryptedSend());
        form.setStatusmailOnErrorOnly(mailing.isStatusmailOnErrorOnly());
        form.setStatusmailRecipients(mailing.getStatusmailRecipients());
        form.setSizeErrorThreshold(configService.getLongValue(ConfigValue.MailingSizeErrorThresholdBytes, admin.getCompanyID()));
        form.setTemplateId(gridTemplateId);

        String workStatus = mailingDao.getWorkStatus(admin.getCompanyID(), form.getMailingID());
        form.setWorkStatus(workStatus);

        if (mailing.getMailingType() == MailingType.INTERVAL) {
            if (workStatus == null || !workStatus.equals(MailingStatus.ACTIVE.getDbKey())) {
                // only active or disable is allowed for interval mailings
                form.setWorkStatus(MailingStatus.DISABLE.getDbKey());
            }
        } else if (mailing.getMailingType() == MailingType.DATE_BASED) {
            int requiredAutoImportId = mailingDeliveryBlockingService.findBlockingAutoImportId(mailing.getId());
            form.setAutoImportId(requiredAutoImportId);
        }
    }

    private void updateFollowupInfoIfNecessary(Mailing mailing, Model model) {
        MediatypeEmail emailParam;
        if (mailing.getMailingType() == MailingType.FOLLOW_UP && (emailParam = mailing.getEmailParam()) != null) {
            model.addAttribute("followupFor", emailParam.getFollowupFor());
            model.addAttribute("followUpType", emailParam.getFollowUpMethod());
        }
    }

    protected void fillModelData(Mailing mailing, Admin admin, Model model, int gridTemplateId) throws Exception {
        model.addAttribute("templateId", gridTemplateId);
        model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailing.getId()));
        model.addAttribute("isMailingGrid", gridTemplateId > 0);
    }

    protected void addIntervalSettingsToForm(MailingSendForm mailingForm, int companyId) {
        // Nothing to do
    }

    private void loadDependents(int companyID, MailingSendForm form, Mailing mailing, Model model) {
        int mailingId = mailing.getId();
        if (mailing.getMailingType() == MailingType.ACTION_BASED) {
            webStorage.access(WebStorage.MAILING_SEND_DEPENDENTS_OVERVIEW, entry -> {
                if (form.getNumberOfRows() <= 0) {
                    form.setNumberOfRows(entry.getRowsCount());
                    form.setFilterTypes(entry.getFilterTypes().toArray(new String[0]));
                } else {
                    entry.setRowsCount(form.getNumberOfRows());
                    if (form.getFilterTypes() == null) {
                        entry.setFilterTypes(null);
                    } else {
                        entry.setFilterTypes(Arrays.asList(form.getFilterTypes()));
                    }
                }
            });

            List<MailingDependentType> types = mailingDependencyService.detectActiveFilters(
                    form.getFilterTypes(),
                    MailingDependentType.ACTION,
                    MailingDependentType.WORKFLOW,
                    MailingDependentType.BOUNCE_FILTER
            );

            model.addAttribute("dependents", mailingDependencyService.load(companyID, mailingId, types));
            model.addAttribute("bounceFilterNames", loadBounceFiltersNames(companyID, mailingId));
        }
    }

    private String loadBounceFiltersNames(int companyID, int mailingId) {
        List<BounceFilterDto> bounceFilters = bounceFilterService.getDependentBounceFiltersWithActiveAutoResponder(companyID, mailingId);
        return bounceFilterService.getBounceFilterNames(bounceFilters);
    }

    private void loadDeliveryStatistics(Admin admin, Mailing mailing, MailingSendForm form, Model model) {
        int mailingId = mailing.getId();

        form.setDeliveryStat(deliveryStatService.getDeliveryStats(admin.getCompanyID(), mailingId, mailing.getMailingType()));
        model.addAttribute("targetGroupsNames", targetDao.getTargetNamesByIds(admin.getCompanyID(), targetService.getTargetIdsFromExpression(mailing)));
    }

    private boolean validateMailingSize(Mailing mailing, Popups popups, Admin admin) {
        Tuple<Long, Long> mailingSizes = mailingSizeCalculationService.calculateSize(mailing, admin);

        Long approximateMaxSizeWithoutImages = mailingSizes.getFirst();
        long maximumMailingSizeAllowed = configService.getLongValue(ConfigValue.MailingSizeErrorThresholdBytes, admin.getCompanyID());

        if (approximateMaxSizeWithoutImages > maximumMailingSizeAllowed) {
            popups.alert("error.mailing.size.large", maximumMailingSizeAllowed);
            return false;
        }

        long approximateMaxSize = mailingSizes.getSecond();
        long warningMailingSize = configService.getLongValue(ConfigValue.MailingSizeWarningThresholdBytes, admin.getCompanyID());

        if (approximateMaxSize > warningMailingSize) {
            popups.warning("warning.mailing.size.large", warningMailingSize);
        }

        return true;
    }

    private boolean validateNeedTarget(Mailing mailing, Popups popups) {
        if (CollectionUtils.isEmpty(mailing.getTargetGroups()) && mailing.getMailingType() == MailingType.DATE_BASED) {
            popups.alert("error.mailing.rulebased_without_target");
            return false;
        }
        return true;
    }

    private boolean validateNeedDkimKey(Admin admin, Mailing mailing, Popups popups) {
        MediatypeEmail mediatypeEmail = mailing.getEmailParam();

        // No further check, if media type "Email" is not active
        if (!Mediatype.isActive(mediatypeEmail)) {
            return true;
        }

        int companyID = admin.getCompanyID();

        String fromAddress = mediatypeEmail.getFromEmail();
        String senderDomain = AgnUtils.getDomainFromEmail(fromAddress);

        if (configService.getBooleanValue(ConfigValue.DkimGlobalActivation, companyID) && !dkimDao.existsDkimKeyForDomain(companyID, senderDomain)) {
            String configValue = configService.getValue(ConfigValue.SendMailingWithoutDkimCheck, companyID);

            if ("warning".equalsIgnoreCase(configValue)) {
                mailingSendService.sendEmail(admin, senderDomain);
                popups.warning("warning.mailing.mandatoryDkimKeyMissing", senderDomain);
            } else if ("error".equalsIgnoreCase(configValue)) {
                mailingSendService.sendEmail(admin, senderDomain);
                popups.alert("error.mailing.mandatoryDkimKeyMissing", senderDomain);

                return false;
            }
        }

        return true;
    }

    private String getMailingSubject(Mailing mailing) {
        return ((MediatypeEmail) mailing.getMediatypes().get(0)).getSubject();
    }

    private String getTriggerMailingDescription(Mailing mailing) {
        return String.format("%s (%d)", mailing.getShortname(), mailing.getId());
    }

    protected void writeUserActivityLog(Admin admin, String action, String description) {
        userActivityLogService.writeUserActivityLog(admin, new UserAction(action, description), LOGGER);
    }
}
