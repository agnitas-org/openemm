/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.web;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;
import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agnitas.beans.Admin;
import com.agnitas.beans.DeliveryStat;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingSendOptions;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.Target;
import com.agnitas.beans.impl.TargetImpl;
import com.agnitas.dao.DkimDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.components.entity.RecipientEmailStatus;
import com.agnitas.emm.core.components.entity.TestRunOption;
import com.agnitas.emm.core.components.form.MailingSendForm;
import com.agnitas.emm.core.components.form.MailingTestSendForm;
import com.agnitas.emm.core.components.form.SecurityAndNotificationsSettingsForm;
import com.agnitas.emm.core.components.service.MailingBlockSizeService;
import com.agnitas.emm.core.components.service.MailingDependencyService;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.bean.MailingDependentType;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSizeCalculationService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.emm.core.mailing.web.MailingSendSecurityOptions;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.serverprio.server.ServerPrioService;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConversionException;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.workflow.beans.impl.WorkflowDateBasedMailingImpl;
import com.agnitas.emm.core.workflow.beans.parameters.WorkflowParametersHelper;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.premium.service.PremiumFeaturesService;
import com.agnitas.emm.premium.web.SpecialPremiumFeature;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingLightService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.NumericUtil;
import com.agnitas.util.Tuple;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class MailingSendController implements XssCheckAware {

    private static final Logger LOGGER = LogManager.getLogger(MailingSendController.class);
    private static final String SEND_STATS_LOADING_KEY = "mailing_load_send_stats";
    private static final String MAILING_SUBJECT_ATTR = "mailingSubject";

    protected final ConfigService configService;
    protected final MailingService mailingService;
    protected final MailingSendService mailingSendService;
    protected final AutoImportService autoImportService;
    private final MailingDeliveryBlockingService mailingDeliveryBlockingService;
    private final BirtReportService birtReportService;
    private final MailingSizeCalculationService mailingSizeCalculationService;
    private final MailingRecipientsService mailingRecipientsService;
    private final MailingDeliveryStatService deliveryStatService;
    private final MailingDependencyService mailingDependencyService;
    private final UserActivityLogService userActivityLogService;
    private final MailingStatisticsDao mailingStatisticsDao;
    private final MailingBaseService mailingBaseService;
    private final MailingBlockSizeService blockSizeService;
    private final BounceFilterService bounceFilterService;
    private final MailingStopService mailingStopService;
    private final ConversionService conversionService;
    private final TargetCopyService targetCopyService;
    private final MaildropService maildropService;
    private final TargetService targetService;
    private final GridServiceWrapper gridService;
    private final MailinglistDao mailinglistDao;
    private final MailingDao mailingDao;
    private final TargetDao targetDao;
    private final WebStorage webStorage;
    private final DkimDao dkimDao;
    private final PremiumFeaturesService premiumFeatureService;
    private final ServerPrioService serverPrioService;
    private final MailinglistApprovalService mailinglistApprovalService;
    private final WorkflowService workflowService;

    public MailingSendController(MailingRecipientsService mailingRecipientsService, MailingDao mailingDao, MailingBaseService mailingBaseService, GridServiceWrapper gridService,
                                 TargetService targetService, ConfigService configService, MaildropService maildropService, MailingDeliveryStatService deliveryStatService,
                                 TargetDao targetDao, MailingService mailingService, MailingSizeCalculationService mailingSizeCalculationService,
                                 MailingDependencyService mailingDependencyService, WebStorage webStorage, UserActivityLogService userActivityLogService, MailingBlockSizeService blockSizeService,
                                 MailingStopService mailingStopService, MailinglistDao mailinglistDao, @Autowired(required = false) AutoImportService autoImportService, DkimDao dkimDao,
                                 MailingDeliveryBlockingService mailingDeliveryBlockingService, MailingStatisticsDao mailingStatisticsDao, @Qualifier("BounceFilterService") BounceFilterService bounceFilterService,
                                 MailingSendService mailingSendService, ConversionService conversionService, TargetCopyService targetCopyService,
                                 PremiumFeaturesService premiumFeaturesService, ServerPrioService serverPrioService, MailinglistApprovalService mailinglistApprovalService,
                                 BirtReportService birtReportService, WorkflowService workflowService) {

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
        this.mailingSendService = mailingSendService;
        this.conversionService = conversionService;
        this.targetCopyService = targetCopyService;
        this.premiumFeatureService = premiumFeaturesService;
        this.serverPrioService = serverPrioService;
        this.mailinglistApprovalService = mailinglistApprovalService;
        this.birtReportService = birtReportService;
        this.workflowService = workflowService;
    }

    @RequestMapping("/{mailingId:\\d+}/view.action")
    public String view(@PathVariable("mailingId") int mailingId, Admin admin, Model model, @ModelAttribute("form") MailingSendForm form,
                       @ModelAttribute("testForm") MailingTestSendForm testForm, Popups popups) {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        fillFormWithMailingData(admin, mailing, form);
        fillModelData(mailing, admin, model, form);
        loadDeliveryStatistics(admin, mailing, form, model);
        if (admin.isRedesignedUiUsed()) {
            fillFormForDeliverySettingsView(form, admin, mailing);
            fillFormForDateBasedDeliverySettingsView(form, admin, mailing);
        } else {
            loadDependents(admin, form, mailing, model);
            setSendButtonsControlAttributes(admin, mailing, model);

            AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
            model.addAttribute("adminTargetGroupList", targetDao.getTestAndAdminTargetLights(admin.getAdminID(), companyID));
        }

        if (mailing.getMailingType() == MailingType.INTERVAL) {
            addIntervalSettingsToForm(form, companyID);
        }

        if (testForm.getTestRunOption() == null) {
            testForm.setTestRunOption(TestRunOption.fromId(configService.getIntegerValue(ConfigValue.DefaultTestRunOption, companyID)));
        }

        // delivery time settings
        GregorianCalendar currentDate = new GregorianCalendar(AgnUtils.getTimeZone(admin));
        form.setSendHour(currentDate.get(Calendar.HOUR_OF_DAY));
        form.setSendMinute(currentDate.get(Calendar.MINUTE));

        if (admin.isRedesignedUiUsed() && mailingService.hasMailingStatus(mailingId, MailingStatus.INSUFFICIENT_VOUCHERS, companyID)) {
            popups.alert("GWUA.mailing.canceled.dueToInsufficientVouchers");
        }

        return "mailing_send";
    }

    private void setSendButtonsControlAttributes(Admin admin, Mailing mailing, Model model) {
        boolean mailingLockedAndNotSent = mailingService.isMailingLocked(mailing) && !mailingSendService.isMailingActiveOrSent(mailing);
        boolean adminHasApprovePermission = admin.permissionAllowed(Permission.MAILING_CAN_ALLOW);
        boolean isThresholdClearanceExceeded = mailing.getMailingType() == MailingType.DATE_BASED
                && mailingService.isThresholdClearanceExceeded(admin.getCompanyID(), mailing.getId());

        model.addAttribute("canSendOrActivateMailing", mailingSendService.canSendOrActivateMailing(admin, mailing));
        model.addAttribute("approvePossible", adminHasApprovePermission && mailingLockedAndNotSent);
        model.addAttribute("approveRequestPossible", !adminHasApprovePermission && mailingLockedAndNotSent);
        model.addAttribute("isThresholdClearanceExceeded", isThresholdClearanceExceeded);
    }

    @PostMapping("/datebased/activation/confirm.action")
    public String confirmDateBasedActivation(@ModelAttribute("form") MailingSendForm form, Admin admin, Model model, Popups popups) {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), companyID);

        if (admin.isRedesignedUiUsed()) {
            SimpleServiceResult result = mailingBaseService.checkContentNotBlank(mailing);
            popups.addPopups(result);

            if (!result.isSuccess() || !validateNeedDkimKey(admin, mailing, popups) || !validateNeedTarget(mailing, popups)
                    || !isValidSecuritySettings(admin, form.getSecuritySettings(), popups)) {
                return MESSAGES_VIEW;
            }
        } else {
            if (!isValidSecuritySettings(admin, form.getSecuritySettings(), popups)) {
                return MESSAGES_VIEW;
            }
        }

        if (mailingService.isDateBasedMailingWasSentToday(form.getMailingID())) {
            model.addAttribute("isAlreadySentToday", true);
        } else {
            String targetGroupsAsString = String.join(", ",
                    targetService.getTargetNamesByIds(companyID, new HashSet<>(mailing.getTargetGroups())));

            model.addAttribute("targetGroups", targetGroupsAsString);
            model.addAttribute(MAILING_SUBJECT_ATTR, getMailingSubject(mailing));
            addMailingSizeAttributes(mailing, admin, model);
        }

        form.setShortname(mailing.getShortname());

        return "date_based_activation_confirm";
    }

    @PostMapping("/activate-date-based.action")
    public String activateDateBased(Admin admin, @ModelAttribute MailingSendForm form, HttpServletRequest req, Popups popups) {
        int companyID = admin.getCompanyID();
        int mailingId = form.getMailingID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        if (!isValidSecuritySettings(admin, form.getSecuritySettings(), popups)) {
            return MESSAGES_VIEW;
        }

        Runnable activationCallback = () -> activateDateBasedMailing(form, mailing, admin, popups);
        activateMailing(mailing, admin, form, WorkflowParametersHelper.isWorkflowDriven(req), popups, activationCallback);

        return redirectToSendView(mailingId);
    }

    private boolean isValidSecuritySettings(Admin admin, SecurityAndNotificationsSettingsForm form, Popups popups) {
        if (form.isEnableNotifications()) {
            if (StringUtils.isBlank(form.getClearanceEmail())) {
                popups.alert("error.email.empty");
                return false;
            }
        } else if (!StringUtils.isBlank(form.getClearanceEmail()) || form.isEnableNoSendCheckNotifications() || form.getClearanceThreshold() != null) {
            popups.alert("error.notification.off");
            return false;
        }

        if (!AgnUtils.isValidEmailAddresses(form.getClearanceEmail())) {
            popups.alert("error.email.wrong");
            return false;
        }

        if (form.getClearanceThreshold() != null && form.getClearanceThreshold() <= 0) {
            popups.alert("grid.errors.wrong.int", I18nString.getLocaleString("mailing.autooptimization.threshold", admin.getLocale()));
            return false;
        }

        return true;
    }

    private void activateDateBasedMailing(MailingSendForm form, Mailing mailing, Admin admin, Popups popups) {
        form.setDate(parseFormSendDate(admin, form));
        MailingSendOptions options = conversionService.convert(form, MailingSendOptions.class);

        if (saveSecurityAndNotificationsSettings(form.getSecuritySettings(), mailing)) {
            ServiceResult<UserAction> result = mailingSendService.activateDateBasedMailing(mailing, options, admin);
            popups.addPopups(result);

            if (result.isSuccess()) {
                UserAction userAction = result.getResult();
                writeUserActivityLog(admin, userAction.getAction(), userAction.getDescription());
            }
        } else {
            LOGGER.error("Saving security settings failed!");
            popups.alert(ERROR_MSG);
        }
    }

    private boolean saveSecurityAndNotificationsSettings(SecurityAndNotificationsSettingsForm form, Mailing mailing) {
        MailingSendSecurityOptions options = MailingSendSecurityOptions.builder()
                .setNoSendNotificationEnabled(form.isEnableNoSendCheckNotifications())
                .withNotifications(form.isEnableNotifications(), form.getClearanceEmail())
                .setClearanceThreshold(form.getClearanceThreshold())
                .build();

        boolean saved = mailingService.saveSecuritySettings(mailing.getCompanyID(), mailing.getId(), options);

        if (saved) {
            mailing.setClearanceEmail(options.getClearanceEmail());
            mailing.setClearanceThreshold(options.getClearanceThreshold());
            mailing.setStatusmailOnErrorOnly(options.isEnableNoSendCheckNotifications());
        }

        return saved;
    }

    @PostMapping("/test/saveTarget.action")
    public @ResponseBody BooleanResponseDto saveTestRunTarget(Admin admin, MailingTestSendForm form, Popups popups) {
        return new BooleanResponseDto(popups, saveTestRunTarget(form, admin, popups));
    }

    private boolean saveTestRunTarget(MailingTestSendForm form, Admin admin, Popups popups) {
        if (isInvalidTestSendTargetValues(form, admin, popups)) {
            return false;
        }
        List<Message> errors = new ArrayList<>();
        List<UserAction> userActions = new ArrayList<>();

        int id = saveTestRunTarget(form, admin, errors, userActions);

        userActions.forEach(ua -> writeUserActivityLog(admin, ua));
        errors.forEach(popups::alert);
        return id > 0;
    }

    private int saveTestRunTarget(MailingTestSendForm form, Admin admin, List<Message> errors, List<UserAction> userActions) {
        Target oldTarget = targetDao.getTargetByName(form.getTargetName(), admin.getCompanyID());
        if (oldTarget != null && !oldTarget.isAdminTestDelivery()) {
            errors.add(Message.of("error.target.namealreadyexists"));
            return 0;
        }
        return trySaveTestRunTarget(form, admin, errors, userActions, oldTarget);
    }

    private int trySaveTestRunTarget(MailingTestSendForm form, Admin admin, List<Message> errors, List<UserAction> userActions, Target oldTarget) {
        try {
            Target newTarget = getTestRunTargetToSave(oldTarget, form, admin);
            return targetService.saveTarget(admin, newTarget, oldTarget, errors, userActions);
        } catch (QueryBuilderToEqlConversionException e) {
            LOGGER.error("Could not convert query builder rule.", e);
        } catch (Exception e) {
            LOGGER.error("Could not save target group.");
        }
        return 0;
    }

    private Target getTestRunTargetToSave(Target oldTarget, MailingTestSendForm form, Admin admin) {
        Target newTarget;
        if (oldTarget == null) {
            newTarget = new TargetImpl();
            newTarget.setCompanyID(admin.getCompanyID());
            newTarget.setAdminTestDelivery(true);
            newTarget.setTargetName(form.getTargetName());
        } else {
            newTarget = targetCopyService.copyTargetGroup(oldTarget, new TargetImpl());
        }
        newTarget.setEQL(generateTargetEqlFromRecipientEmails(form.getMailingTestRecipients()));
        return newTarget;
    }

    private String generateTargetEqlFromRecipientEmails(String[] testRecipients) {
        return Arrays.stream(testRecipients)
                .map(email -> "`email` = '" + email + "'")
                .collect(Collectors.joining(" OR "));
    }

    private boolean isInvalidTestSendTargetValues(MailingTestSendForm form, Admin admin, Popups popups) {
        validateTestSendTargetName(form, popups);
        validateTestSendTargetEmails(form, admin, popups);
        return popups.hasAlertPopups();
    }

    private void validateTestSendTargetEmails(MailingTestSendForm form, Admin admin, Popups popups) {
        if (ArrayUtils.isEmpty(form.getMailingTestRecipients())) {
            popups.alert("error.email.empty");
            return;
        }
        getInvalidTestRunEmails(form.getMailingTestRecipients())
                .forEach(email -> popups.exactAlert(getInvalidEmailExactMsg(admin, email)));
    }

    private void validateTestSendTargetName(MailingTestSendForm form, Popups popups) {
        if (StringUtils.length(form.getTargetName()) < 3) {
            popups.alert("error.name.too.short");
        } else if (!targetService.checkIfTargetNameIsValid(form.getTargetName())) {
            popups.alert("error.target.namenotallowed");
        }
    }

    private String getInvalidEmailExactMsg(Admin admin, String email) {
        return I18nString.getLocaleString("error.email.invalid", admin.getLocale()) + ": " + email;
    }

    private List<String> getInvalidTestRunEmails(String[] mailingTestRecipients) {
        return Arrays.stream(mailingTestRecipients)
                .filter(email -> !AgnUtils.isEmailValid(email))
                .toList();
    }

    protected String redirectToSendView(int mailingId) {
        return String.format("redirect:/mailing/send/%s/view.action", mailingId);
    }

    @PostMapping("/{mailingId:\\d+}/send-admin.action")
    public String sendAdmin(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute MailingTestSendForm form, Popups popups) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        if (mailingService.containsInvalidTargetGroups(companyID, mailing.getId())) {
            popups.alert("error.mailing.containsInvaidTargetGroups");
        } else {
	        MailingSendOptions sendOptions = MailingSendOptions.builder()
	                .setAdminTargetGroupId(form.getAdminTargetGroupID())
	                .build();

	        ServiceResult<UserAction> result = mailingSendService.sendAdminMailing(mailing, sendOptions);
	        popups.addPopups(result);

	        if (!result.isSuccess()) {
	            return MESSAGES_VIEW;
	        }

	        UserAction userAction = result.getResult();
	        writeUserActivityLog(admin, userAction.getAction(), userAction.getDescription());
	        checkLimitationForSend(companyID, popups);
        }

        return redirectToSendView(mailingId);
    }

    @PostMapping("/{mailingId:\\d+}/send-test.action")
    public String sendTest(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute MailingTestSendForm form, Popups popups, RedirectAttributes ra) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        mailingSendService.validateForTestRun(form, mailingId, companyID);
        ServiceResult<UserAction> result = mailingSendService.sendTestMailing(mailing, form, admin);
        popups.addPopups(result);

        if (!result.isSuccess()) {
            return MESSAGES_VIEW;
        }

        UserAction userAction = result.getResult();
        writeUserActivityLog(admin, userAction.getAction(), userAction.getDescription());
        checkLimitationForSend(companyID, popups);

        ra.addFlashAttribute("testForm", form);

        if (form.isRequestApproval()) {
            writeUserActivityLog(admin, getMailingApprovalUserAction(mailingId, form));
        }
        return redirectToSendView(mailingId);
    }

    private static UserAction getMailingApprovalUserAction(int mailingId, MailingTestSendForm form) {
        return new UserAction("mailing approval requested", getMailingApprovalRequestUalDescr(mailingId, form));
    }

    private static String getMailingApprovalRequestUalDescr(int mailingId, MailingTestSendForm form) {
        StringBuilder descr = new StringBuilder("Mailing id = ").append(mailingId).append(", ");
        if (ArrayUtils.isNotEmpty(form.getMailingTestRecipients())) {
            descr.append("Recipients: ").append(String.join(", ", form.getMailingTestRecipients()));
        } else if (form.getAdminTargetGroupID() > 0) {
            descr.append("Target = ").append(form.getAdminTargetGroupID());
        }
        return descr.toString();
    }

    @GetMapping("/{mailingId:\\d+}/delivery/settings/view.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
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
        addFollowupAttributes(mailing, model);
        fillModelData(mailing, admin, model, form);

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

            if (autoImportService != null) {
                model.addAttribute("autoImports", autoImportService.listAutoImports(companyID));
            }

            model.addAttribute("targetGroupNames", targetDao.getTargetNamesByIds(companyID, getMailingTargets(mailing)));
            model.addAttribute("isMailtrackExtended", configService.getBooleanValue(ConfigValue.MailtrackExtended, companyID));
            model.addAttribute("sendingSpeedOptions", mailingSendService.getAvailableSendingSpeedOptions(companyID));

            return new ModelAndView("mailing_delivery_settings", model.asMap());
        };

        return new Pollable<>(pollingUid, Pollable.LONG_TIMEOUT, new ModelAndView(String.format("redirect:/mailing/send/%d/delivery/settings/view.action", mailingId), form.toMap()), worker);
    }

    private void fillFormForDeliverySettingsView(MailingSendForm form, Admin admin, Mailing mailing) {
        int companyID = admin.getCompanyID();

        if (!admin.isRedesignedUiUsed()) {
            fillSendStatistics(form, mailing, companyID);
            form.setTotalSentCount(form.getSentStatistics().values().stream()
                    .reduce(0, Integer::sum));

            if (admin.getCompany().isAutoMailingReportSendActivated()) {
                enableAutomaticReports(form);
            }
        }

        form.setReportSendEmail(StringUtils.defaultIfEmpty(admin.getStatEmail(), admin.getEmail()));
        form.setCheckForDuplicateRecords(configService.getBooleanValue(ConfigValue.PrefillCheckboxSendDuplicateCheck, companyID));
        form.setCleanupTestsBeforeDelivery(configService.getBooleanValue(ConfigValue.CleanAdminAndTestRecipientsActivities, companyID));

        if (admin.isRedesignedUiUsed()) {
            Optional<MaildropEntry> maildrop = maildropService.findMaildrop(mailing.getId(), companyID, MaildropStatus.WORLD, MaildropStatus.DATE_BASED);

            maildrop.ifPresent(m -> {
                form.setBlocksize(AgnUtils.getSelectedBlocksizeByBlocksizeAndStepping(m.getBlocksize(), m.getStepping()));
                form.setMaxRecipients(String.valueOf(m.getMaxRecipients()));
                form.setSendDate(admin.getDateFormat().format(m.getSendDate()));
                form.setSendTime(getTimeStrFromDate(m.getSendDate(), admin));
            });

            if (mailingSendService.canSendOrActivateMailing(admin, mailing)) {
                if (admin.getCompany().isAutoMailingReportSendActivated()) {
                    enableAutomaticReports(form);
                }
            } else {
                fillAutomaticReportSettings(form, mailing);
            }
        }
    }

    private void fillAutomaticReportSettings(MailingSendForm form, Mailing mailing) {
        Map<BirtReportType, Integer> automaticReportsMap = birtReportService.getMailingAutomaticReportIdsMap(mailing.getId(), mailing.getCompanyID());

        form.setReportSendAfter24h(automaticReportsMap.containsKey(BirtReportType.TYPE_AFTER_MAILING_24HOURS));
        form.setReportSendAfter48h(automaticReportsMap.containsKey(BirtReportType.TYPE_AFTER_MAILING_48HOURS));
        form.setReportSendAfter1Week(automaticReportsMap.containsKey(BirtReportType.TYPE_AFTER_MAILING_WEEK));

        if (!automaticReportsMap.isEmpty()) {
            form.setReportSendEmail(String.join(", ", birtReportService.getMailingAutomaticReportEmails(automaticReportsMap.values())));
        }
    }

    private void enableAutomaticReports(MailingSendForm form) {
        form.setReportSendAfter24h(true);
        form.setReportSendAfter48h(true);
        form.setReportSendAfter1Week(true);
    }

    // TODO: EMMGUI-714: remove after remove of old design
    private void fillSendStatistics(MailingSendForm form, Mailing mailing, int companyId) {
        Map<Integer, Integer> sentStatistics = mailingStatisticsDao.getSendStats(mailing, companyId);

        int textEmailsCount = sentStatistics.remove(MailingStatisticsDao.SEND_STATS_TEXT);
        int htmlEmailsCount = sentStatistics.remove(MailingStatisticsDao.SEND_STATS_HTML);
        int offlineHtmlEmailsCount = sentStatistics.remove(MailingStatisticsDao.SEND_STATS_OFFLINE);
        int totalCount = textEmailsCount + htmlEmailsCount + offlineHtmlEmailsCount;

        for (Map.Entry<Integer, Integer> entry : sentStatistics.entrySet()) {
            form.setSentStatisticsItem(entry.getKey(), entry.getValue());
        }

        form.setTextEmailsCount(textEmailsCount);
        form.setHtmlEmailsCount(htmlEmailsCount);
        form.setOfflineHtmlEmailsCount(offlineHtmlEmailsCount);
        form.setSentStatisticsItem(0, totalCount);
    }

    private void addRecipientsCountAttrs(Mailing mailing, int companyId, Model model) {
        Map<Integer, Integer> sentStatistics = mailingStatisticsDao.getSendStats(mailing, companyId);

        model.addAttribute("totalSendCount", sentStatistics.values().stream().reduce(0, Integer::sum));
        model.addAttribute("textEmailsCount", sentStatistics.remove(MailingStatisticsDao.SEND_STATS_TEXT));
        model.addAttribute("htmlEmailsCount", sentStatistics.remove(MailingStatisticsDao.SEND_STATS_HTML));
        model.addAttribute("offlineHtmlEmailsCount", sentStatistics.remove(MailingStatisticsDao.SEND_STATS_OFFLINE));

        model.addAttribute("mediaTypesRecipientsCountMap", sentStatistics);
    }

    @GetMapping("/{mailingId:\\d+}/date-based/options/configure.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
    public String configureDateBasedSendingOptions(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute("form") MailingSendForm form,
                                                   Model model, Popups popups, HttpServletRequest req) throws Exception {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        SimpleServiceResult result = mailingBaseService.checkContentNotBlank(mailing);
        popups.addPopups(result);

        if (!result.isSuccess() || !validateNeedDkimKey(admin, mailing, popups)
                || (!isWorkflowDriven(mailingId, companyID, req) && !validateNeedTarget(mailing, popups))) {
            return redirectToSendView(mailingId);
        }

        fillFormWithMailingData(admin, mailing, form);
        fillModelData(mailing, admin, model, form);

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

        if (mailingService.isMissingNecessaryTargetGroup(mailing)) {
            popups.alert("error.mailing.rulebased_without_target");
        }

        if (form.getWorkflowId() != 0) {
            popups.warning("warning.workflow.delivery.stop");
        }

        fillFormForDateBasedDeliverySettingsView(form, admin, mailing);
        fillModelForDateBasedDeliverySettings(model, mailing);

        return "date_based_delivery_settings";
    }

    private boolean isWorkflowDriven(int mailingId, int companyId, HttpServletRequest req) {
        return WorkflowParametersHelper.isWorkflowDriven(req) || mailingBaseService.getWorkflowId(mailingId, companyId) > 0;
    }

    private void fillModelForDateBasedDeliverySettings(Model model, Mailing mailing) {
        int companyID = mailing.getCompanyID();

        if (autoImportService != null) {
            model.addAttribute("autoImports", autoImportService.listAutoImports(companyID));
        }

        model.addAttribute("targetGroupNames", targetDao.getTargetNamesByIds(companyID, getMailingTargets(mailing)));
        model.addAttribute("isMailtrackExtended", configService.getBooleanValue(ConfigValue.MailtrackExtended, companyID));
        model.addAttribute("sendingSpeedOptions", mailingSendService.getAvailableSendingSpeedOptions(companyID));
    }

    private Set<Integer> getMailingTargets(Mailing mailing) {
        Set<Integer> targetGroups = (Set<Integer>) mailing.getTargetGroups();
        if (targetGroups == null) {
            targetGroups = new HashSet<>();
        }

        return targetGroups;
    }

    private void fillFormForDateBasedDeliverySettingsView(MailingSendForm form, Admin admin, Mailing mailing) {
        if (!admin.isRedesignedUiUsed()) {
            form.setCheckForDuplicateRecords(configService.getBooleanValue(ConfigValue.PrefillCheckboxSendDuplicateCheck, admin.getCompanyID()));
            form.setCleanupTestsBeforeDelivery(configService.getBooleanValue(ConfigValue.CleanAdminAndTestRecipientsActivities, admin.getCompanyID()));
        } else {
            if (mailing.getMailingType() != MailingType.DATE_BASED) {
                return;
            }
        }

        if (form.getWorkflowId() > 0 && admin.isRedesignedUiUsed()) {
            workflowService.findMailingIcon(form.getWorkflowId(), mailing.getId(), WorkflowDateBasedMailingImpl.class)
                    .ifPresent(icon -> {
                        if (icon.isEnableNotifications()) {
                            form.getSecuritySettings().setClearanceThreshold(icon.getClearanceThreshold());
                            form.getSecuritySettings().setEnableNoSendCheckNotifications(icon.isEnableNoSendCheckNotifications());
                            form.getSecuritySettings().setClearanceEmail(String.join(" ", icon.getClearanceEmails()));
                        }
                    });
        } else {
            form.getSecuritySettings().setClearanceThreshold(mailing.getClearanceThreshold());
            form.getSecuritySettings().setEnableNoSendCheckNotifications(mailing.isStatusmailOnErrorOnly());
            form.getSecuritySettings().setClearanceEmail(mailing.getClearanceEmail());
        }
    }

    @PostMapping("/confirm-send-world.action")
    public Object confirmSendWorld(@ModelAttribute("form") MailingSendForm form, Popups popups, Admin admin, Model model) {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(form.getMailingID(), companyID);

        if (admin.isRedesignedUiUsed()) {
            SimpleServiceResult result = mailingBaseService.checkContentNotBlank(mailing);
            popups.addPopups(result);

            if (!result.isSuccess() || !validateNeedDkimKey(admin, mailing, popups) || !checkLimitationForSend(companyID, popups)) {
                return MESSAGES_VIEW;
            }

            if (!validateFormBeforeSendWorldMailing(form, popups)) {
             return new ModelAndView(MESSAGES_VIEW, HttpStatus.BAD_REQUEST);
            }
        } else {
            if (!NumericUtil.matchedUnsignedIntegerPattern(form.getMaxRecipients())) {
                popups.alert("error.maxRecipients.notNumeric");
                return String.format("redirect:/mailing/send/%s/delivery/settings/view.action", form.getMailingID());
            }

            if (!checkLimitationForSend(companyID, popups)) {
                return redirectToSendView(form.getMailingID());
            }
        }

        Date sendDate = parseFormSendDate(admin, form);

        mailingSendService.checkIfMailingCanBeSend(mailing, sendDate, AgnUtils.getTimeZone(admin));
        fillFormWithMailingData(admin, mailing, form);

        int recipientsCount = calculateRecipientsCount(form, mailing, admin);
        addModelAttributesForConfirmSending(mailing, admin, recipientsCount, sendDate, model);

        loadSteppingBlocksize(form, mailing.getMaildropStatus());

        return "mailing_send_confirm_ajax";
    }

    private boolean validateFormBeforeSendWorldMailing(MailingSendForm form, Popups popups) {
        if (!NumericUtil.matchedUnsignedIntegerPattern(form.getMaxRecipients())) {
            popups.alert("error.maxRecipients.notNumeric");
            return false;
        }

        if (form.isAutoReportEnabled()) {
            List<String> reportEmails = form.getReportEmails();
            if (reportEmails.isEmpty()) {
                popups.fieldError("reportSendEmail", "error.default.required");
                return false;
            }

            if (reportEmails.stream().anyMatch(e -> !AgnUtils.isEmailValid(e))) {
                popups.fieldError("reportSendEmail", "error.invalid.email");
                return false;
            }
        }

        return true;
    }

    private void addModelAttributesForConfirmSending(Mailing mailing, Admin admin, int recipientsCount, Date sendDate, Model model) {
        model.addAttribute("potentialSendDate", admin.getDateFormat().format(sendDate));
        model.addAttribute("potentialSendTime", admin.getTimeFormat().format(sendDate));
        model.addAttribute("adminDateFormat", admin.getDateFormat().toPattern());
        model.addAttribute(MAILING_SUBJECT_ATTR, getMailingSubject(mailing));
        addMailingSizeAttributes(mailing, admin, model);

        NumberFormat formatter = NumberFormat.getNumberInstance(admin.getLocale());
        model.addAttribute("recipientsCount", formatter.format(recipientsCount));

        model.addAttribute("mailinglistShortname", mailinglistDao.getMailinglistName(mailing.getMailinglistID(), admin.getCompanyID()));
    }

    private Date parseFormSendDate(Admin admin, MailingSendForm form) {
        TimeZone timeZone = AgnUtils.getTimeZone(admin);
        GregorianCalendar calendar = new GregorianCalendar(timeZone);

        if (StringUtils.isBlank(form.getSendDate())) {
            LocalDateTime currentDate = LocalDateTime.now(ZoneId.of(timeZone.getID()));
            calendar.set(currentDate.getYear(), currentDate.getMonthValue() - 1, currentDate.getDayOfMonth(), form.getSendHour(), form.getSendMinute());
        } else {
            if (admin.isRedesignedUiUsed()) {
                calendar.setTime(parseSendDateStr(form.getSendDate(), admin));
                calendar.set(Calendar.HOUR_OF_DAY, form.getSendHour());
                calendar.set(Calendar.MINUTE, form.getSendMinute());
            } else {
                int year = Integer.parseInt(form.getSendDate().substring(0, 4));
                int month = Integer.parseInt(form.getSendDate().substring(4, 6)) - 1;
                int day = Integer.parseInt(form.getSendDate().substring(6, 8));

                calendar.set(year, month, day, form.getSendHour(), form.getSendMinute());
            }
        }

        Date sendDate = calendar.getTime();
        form.setDate(sendDate);

        return sendDate;
    }

    private Date parseSendDateStr(String dateStr, Admin admin) {
        try {
            return admin.getDateFormat().parse(dateStr);
        } catch (ParseException e) {
            LOGGER.error("Error when parsing send date for mailing! Input string - {}", dateStr);
            throw new IllegalStateException(e);
        }
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

    private int calculateRecipientsCount(MailingSendForm form, Mailing mailing, Admin admin) {
        int recipientsAmount;

        if (admin.isRedesignedUiUsed()) {
            recipientsAmount = mailingStatisticsDao.getSendStats(mailing, admin.getCompanyID()).values().stream()
                    .reduce(0, Integer::sum);
        } else {
            recipientsAmount = form.getTextEmailsCount() + form.getHtmlEmailsCount() + form.getOfflineHtmlEmailsCount();
            // Sum up other mediatypes
            fillSendStatistics(form, mailing, admin.getCompanyID());

            for (Map.Entry<Integer, Integer> entry : form.getSentStatistics().entrySet()) {
                if (entry.getKey() > 0 && entry.getValue() > 0) {
                    recipientsAmount += entry.getValue();
                }
            }
        }

        if (Integer.parseInt(form.getMaxRecipients()) <= 0) {
            return recipientsAmount;
        }

        return Math.min(Integer.parseInt(form.getMaxRecipients()), recipientsAmount);
    }

    @PostMapping("/send-world.action")
    public String sendWorldMailing(@ModelAttribute MailingSendForm form, Admin admin, Popups popups) {
        int companyID = admin.getCompanyID();
        int mailingId = form.getMailingID();

        if (form.isCleanupTestsBeforeDelivery()) {
        	mailingSendService.clearTestActionsData(mailingId, companyID);
        }

        Mailing mailingToSend = mailingDao.getMailing(mailingId, companyID);

        if (validateMailingSize(mailingToSend, popups, admin) && checkLimitationForSend(companyID, popups)) {
            Date sendDate = parseFormSendDate(admin, form);

            if (isPostMailing(mailingToSend)) {
                createPostTrigger(mailingToSend, sendDate);
            } else {
                MailingSendOptions sendOptions = conversionService.convert(form, MailingSendOptions.class);
                ServiceResult<UserAction> result = mailingSendService.sendWorldMailing(mailingToSend, sendOptions, admin);
                popups.addPopups(result);

                if (!result.isSuccess()) {
                    return MESSAGES_VIEW;
                }

                UserAction userAction = result.getResult();
                writeUserActivityLog(admin, userAction.getAction(), userAction.getDescription());
            }
        }

        return redirectToSendView(mailingId);
    }

    protected boolean createPostTrigger(Mailing mailing, Date sendDate) {
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

    @GetMapping("/{mailingId:\\d+}/deactivate/confirm.action")
    public String confirmDeactivation(@PathVariable int mailingId, Admin admin, Model model) {
        model.addAttribute("shortname", mailingDao.getMailingName(mailingId, admin.getCompanyID()));
        return "mailing_deactivation_confirm";
    }

    @PostMapping("/deactivate.action")
    public String deactivate(@ModelAttribute MailingSendForm form, Admin admin, HttpServletRequest req) {
        int mailingId = form.getMailingID();
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());

        boolean deactivated = mailingSendService.deactivateMailing(mailing, admin.getCompanyID(), WorkflowParametersHelper.isWorkflowDriven(req));

        if (deactivated) {
            mailingDeliveryBlockingService.unblock(mailingId);
            writeUserActivityLog(admin, "do cancel mailing",
                    String.format("Mailing type: %s. %s", mailing.getMailingType().name(), getTriggerMailingDescription(mailing)));
        }

        return redirectToSendView(mailingId);
    }

    @GetMapping("/{mailingId:\\d+}/delivery-status-box/load.action")
    public String loadDeliveryStatusBox(@PathVariable("mailingId") int mailingId, Admin admin, @ModelAttribute("form") MailingSendForm form, Model model) {
        int companyID = admin.getCompanyID();
        Mailing mailing = mailingDao.getMailing(mailingId, companyID);
        boolean mailingLockedAndNotSent = mailingService.isMailingLocked(mailing) && !mailingSendService.isMailingActiveOrSent(mailing);
        boolean adminCannotApproveOrSendAnyway = !admin.permissionAllowed(Permission.MAILING_CAN_ALLOW) && !admin.permissionAllowed(Permission.MAILING_CAN_SEND_ALWAYS);

        form.setMailingID(mailingId);
        form.setWorkStatus(mailingDao.getStatus(companyID, mailingId).getDbKey());
        loadDeliveryStatistics(admin, mailing, form, model);

        model.addAttribute("isPostMailing", isPostMailing(mailing));
        model.addAttribute("copyCancelledMailingEnabled", isCopyCancelledMailingEnabled(admin, mailing));
        model.addAttribute("isTransmissionRunning", mailingDao.isTransmissionRunning(mailingId));
        model.addAttribute("needsOutsideApproval", adminCannotApproveOrSendAnyway && mailingLockedAndNotSent);

        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("mailingType", mailing.getMailingType());
            model.addAttribute("worldMailingSend", maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID()));

            if (mailing.getMailingType() == MailingType.ACTION_BASED) {
                model.addAttribute("bounceFilterNames", loadBounceFiltersNames(companyID, mailingId));
            }
        }

        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);

        return "mailing_delivery_status_box";
    }
    final boolean isCopyCancelledMailingEnabled(final Admin admin, final int mailingId) {
    	final Mailing mailing = this.mailingService.getMailing(admin.getCompanyID(), mailingId);

    	return isCopyCancelledMailingEnabled(admin, mailing);
    }

    private boolean isCopyCancelledMailingEnabled(final Admin admin, final Mailing mailing) {
    	if(this.premiumFeatureService.isFeatureRightsAvailable(SpecialPremiumFeature.AUTOMATION, admin.getCompanyID())) {
    		return admin.getCompany().getMailtracking() != 0;
    	} else {
    		final List<MaildropEntry> worldEntries = mailing.getMaildropStatus().stream()
    				.filter(entry -> entry.getStatus() == 'W')
    				.toList();

    		final Date pauseDate = this.serverPrioService.getDeliveryPauseDate(0, mailing.getId());

    		if(pauseDate != null) {
	    		final ZonedDateTime pauseDateTime = ZonedDateTime.ofInstant(pauseDate.toInstant(), ZoneId.systemDefault());

	    		for(final MaildropEntry entry : worldEntries) {
	    			if(entry.getGenStatus() == MaildropGenerationStatus.FINISHED.getCode() || entry.getGenStatus() == MaildropGenerationStatus.WORKING.getCode()) {
	    				final ZonedDateTime sendDateLimit = ZonedDateTime.ofInstant(entry.getSendDate().toInstant(), ZoneId.systemDefault()).minusMinutes(5);

	    				return pauseDateTime.isBefore(sendDateLimit);
	    			}
	    		}
    		}

    		return true;
    	}
    }

    @GetMapping("/{mailingId:\\d+}/unlock.action")
    public String unlock(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) {
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

    private void checkFollowUpBaseMailingState(int mailingId, Admin admin, Popups popups) {
        String followUpFor = mailingDao.getFollowUpFor(mailingId);

        if (StringUtils.isNotEmpty(followUpFor)) {
            int baseMailingId = Integer.parseInt(followUpFor);

            boolean isBasicMailingSent = mailingDao.getLastSendDate(baseMailingId) != null;

            if (!isBasicMailingSent) {
                popups.warning("warning.mailing.followup.basemail_was_not_sent");
            } else {
                List<Mailing> availableBaseMailings = mailingDao.getMailings(admin.getCompanyID(), admin.getAdminID(), MailingLightService.TAKE_ALL_MAILINGS, "W", true);

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
    // TODO: EMMGUI-714: remove when old design will be removed
    @Deprecated(forRemoval = true)
    public String saveStatusmailRecipients(@ModelAttribute MailingSendForm form, Popups popups) {
        saveStatusMailRecipients(form.getMailingID(), form.getStatusmailRecipients(), popups);
        return redirectToSendView(form.getMailingID());
    }

    @PostMapping("/{mailingId:\\d+}/save-statusmail-recipients.action")
    @PermissionMapping("saveStatusmailRecipients")
    public @ResponseBody BooleanResponseDto saveStatusmailRecipientsRedesigned(@PathVariable int mailingId, @RequestParam("emails") String emails, Popups popups) {
        saveStatusMailRecipients(mailingId, emails, popups);
        return new BooleanResponseDto(popups, !popups.hasAlertPopups());
    }

    private void saveStatusMailRecipients(int mailingId, String emails, Popups popups) {
        RecipientEmailStatus status = mailingRecipientsService.saveStatusMailRecipients(mailingId, emails);

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
    }

    @GetMapping("/{mailingId:\\d+}/actionbased/activation/confirm.action")
    public String confirmActionBasedActivation(@PathVariable int mailingId, Admin admin, Model model) {
        Mailing mailing = mailingDao.getMailing(mailingId, admin.getCompanyID());

        model.addAttribute(MAILING_SUBJECT_ATTR, getMailingSubject(mailing));
        model.addAttribute("shortname", mailing.getShortname());
        addMailingSizeAttributes(mailing, admin, model);

        return "action_based_activation_confirm";
    }

    @PostMapping("/activate-action-based.action")
    public String activateActionBased(Admin admin, @ModelAttribute("form") MailingSendForm form, HttpServletRequest req, Popups popups) {
        int companyID = admin.getCompanyID();
        int mailingId = form.getMailingID();

        Mailing mailing = mailingDao.getMailing(mailingId, companyID);

        Runnable activationCallback = () -> {
            MailingSendOptions sendOptions = MailingSendOptions.builder().build();
            ServiceResult<UserAction> result = mailingSendService.activateActionBasedMailing(mailing, sendOptions);
            popups.addPopups(result);

            if (result.isSuccess()) {
                UserAction userAction = result.getResult();
                writeUserActivityLog(admin, userAction.getAction(), userAction.getDescription());
            }
        };

        activateMailing(mailing, admin, form, WorkflowParametersHelper.isWorkflowDriven(req), popups, activationCallback);

        return redirectToSendView(mailingId);
    }

    private void activateMailing(Mailing mailing, Admin admin, MailingSendForm form, boolean isWorkflowDriven, Popups popups, Runnable activationCallback) {
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
            mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.DISABLE, null);
        }

        if (!popups.hasAlertPopups()) {
            mailingDao.updateStatus(mailing.getCompanyID(), mailing.getId(), MailingStatus.ACTIVE, null);
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

        return "mailing_cancel_generation_question_ajax";
    }

    @PostMapping("/{mailingId:\\d+}/cancel.action")
    public String cancel(@PathVariable("mailingId") int mailingId, Admin admin) {
        int companyID = admin.getCompanyID();
        LightweightMailing mailing = mailingDao.getLightweightMailing(companyID, mailingId);

        if (mailing != null) {
            if (mailingSendService.cancelMailingDelivery(mailingId, companyID)) {
                writeUserActivityLog(admin, "do cancel mailing",
                        String.format("Mailing type: %s. %s (%d)", mailing.getMailingType().name(), mailing.getShortname(), mailingId));

                mailingDao.updateStatus(companyID, mailingId, MailingStatus.CANCELED, null);
                mailingDao.deleteMailtrackDataForMailing(companyID, mailingId);
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

        return "mailing_resume_generation_question_ajax";
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
    public String confirmResumeByCopy(@PathVariable("mailingId") int mailingId, Admin admin, Model model, Popups popups) {
    	if(isCopyCancelledMailingEnabled(admin, mailingId)) {
	        model.addAttribute("mailingId", mailingId);
	        model.addAttribute("mailingShortname", mailingDao.getMailingName(mailingId, admin.getCompanyID()));

	        return "mailing_resume_generation_by_copy_question_ajax";
    	} else {
    		popups.alert("error.mailgeneration.resumeByCopy.generic");

    		return String.format("forward:/mailing/send/%d/view.action", mailingId);
    	}
    }

    @PostMapping("/{mailingId:\\d+}/resume-by-copy.action")
    public String resumeByCopy(@PathVariable("mailingId") int mailingId, Admin admin, Popups popups) throws MailingStopServiceException {
    	if(isCopyCancelledMailingEnabled(admin, mailingId)) {
    		int newMailingID = mailingStopService.copyMailingForResume(admin, mailingId);
    		return String.format("redirect:/mailing/%d/settings.action?keepForward=true", newMailingID);
    	} else {
    		popups.alert("error.mailgeneration.resumeByCopy.generic");

    		return String.format("forward:/mailing/send/%d/view.action", mailingId);
    	}
    }

    // TODO: replace with model attributes
    private void fillFormWithMailingData(Admin admin, Mailing mailing, MailingSendForm form) {
        int workflowId = mailingBaseService.getWorkflowId(mailing.getId(), mailing.getCompanyID());
        int gridTemplateId = gridService.getGridTemplateIdByMailingId(mailing.getId());

        form.setShortname(mailing.getShortname());
        form.setWorkflowId(workflowId);
        form.setMailingGrid(gridTemplateId > 0);
        form.setMailingID(mailing.getId());
        form.setHasDeletedTargetGroups(targetService.hasMailingDeletedTargetGroups(mailing));
        form.setIsTemplate(mailing.isIsTemplate());
        if (!admin.isRedesignedUiUsed()) {
            form.setIsMailingUndoAvailable(mailingBaseService.checkUndoAvailable(mailing.getId()));
        }
        form.setMailingtype(mailing.getMailingType().getCode());
        form.setWorldMailingSend(maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID()));
        form.setPrioritizationDisallowed(!mailing.isPrioritizationAllowed());
        form.setEncryptedSend(mailing.isEncryptedSend());
        form.setStatusmailOnErrorOnly(mailing.isStatusmailOnErrorOnly());
        form.setStatusmailRecipients(mailing.getStatusmailRecipients());
        form.setTemplateId(gridTemplateId);

        MailingStatus workStatus = mailingDao.getStatus(admin.getCompanyID(), form.getMailingID());
        if (mailing.getMailingType() == MailingType.INTERVAL && !MailingStatus.ACTIVE.equals(workStatus)) {
            // only active or disable is allowed for interval mailings
            form.setWorkStatus(MailingStatus.DISABLE.getDbKey());
        } else {
            form.setWorkStatus(workStatus.getDbKey());
        }

        form.setAutoImportId(mailingDeliveryBlockingService.findBlockingAutoImportId(mailing.getId()));
    }

    private void addFollowupAttributes(Mailing mailing, Model model) {
        MediatypeEmail emailParam;
        if (mailing.getMailingType() == MailingType.FOLLOW_UP && (emailParam = mailing.getEmailParam()) != null) {
            model.addAttribute("followupFor", emailParam.getFollowupFor());
            model.addAttribute("followUpType", emailParam.getFollowUpMethod());
        }
    }

    protected void fillModelData(Mailing mailing, Admin admin, Model model, MailingSendForm form) {
        int companyID = admin.getCompanyID();

        model.addAttribute("templateId", form.getTemplateId());
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("mailinglistDisabled", !mailinglistApprovalService.isAdminHaveAccess(admin, mailing.getMailinglistID()));
        } else {
            model.addAttribute("limitedRecipientOverview", mailingBaseService.isLimitedRecipientOverview(admin, mailing.getId()));
        }
        model.addAttribute("isMailingGrid", form.getTemplateId() > 0);
        model.addAttribute("sendDateStr", getPlanDateStr(mailing, admin));
        model.addAttribute("sendTimeStr", getPlanTimeStr(mailing, admin));
        if (mailing.getEmailParam() != null && mailing.getEmailParam().isRequestApproval()) {
            model.addAttribute("approvalsCount", CollectionUtils.size(mailing.getEmailParam().getApprovers()));
        }
        addMailingSizeAttributes(mailing, admin, model);

        if (admin.isRedesignedUiUsed()) {
            MailingStatus status = mailingDao.getStatus(companyID, mailing.getId());

            if (MailingStatus.SENT.equals(status)) {
                maildropService.findMaildrop(mailing.getId(), companyID, MaildropStatus.WORLD).ifPresent(m -> {
                    model.addAttribute("totalSentCount", deliveryStatService.getSentMails(m.getId()));
                });
            } else {
                if (Stream.of(MailingStatus.SENDING, MailingStatus.IN_GENERATION, MailingStatus.GENERATION_FINISHED).noneMatch(s -> s.equals(status))) {
                    addRecipientsCountAttrs(mailing, companyID, model);
                }
            }

            addFollowupAttributes(mailing, model);
            model.addAttribute("isMailtrackExtended", configService.getBooleanValue(ConfigValue.MailtrackExtended, companyID));
            model.addAttribute("sendingSpeedOptions", mailingSendService.getAvailableSendingSpeedOptions(companyID));
            loadDependents(admin, form, mailing, model);
            setSendButtonsControlAttributes(admin, mailing, model);
            AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
            model.addAttribute("adminTargetGroupList", targetDao.getTestAndAdminTargetLights(admin.getAdminID(), companyID));
        }
    }

    private static String getPlanDateStr(Mailing mailing, Admin admin) {
        SimpleDateFormat dateFormat = DateUtilities.getFormat(admin.getDateFormat().toPattern(), TimeZone.getTimeZone(admin.getAdminTimezone()));
        return mailing.getPlanDate() == null
                ? dateFormat.format(new Date())
                : dateFormat.format(mailing.getPlanDate());
    }

    private static String getPlanTimeStr(Mailing mailing, Admin admin) {
        Date planDate = mailing.getPlanDate();
        return planDate == null || new SimpleDateFormat("HH:mm:ss").format(planDate).equals("00:00:00") // plan time was not set
                ? getTimeStrFromDate(new Date(), admin)
                : getTimeStrFromDate(planDate, admin);
    }

    private static String getTimeStrFromDate(Date date, Admin admin) {
        return DateUtilities.getFormat("HH:mm", TimeZone.getTimeZone(admin.getAdminTimezone())).format(date);
    }

    private void addMailingSizeAttributes(Mailing mailing, Admin admin, Model model) {
        Tuple<Long, Long> mailingSize = mailingSizeCalculationService.calculateSize(mailing, admin);

        model.addAttribute("approximateMaxDeliverySize", mailingSize.getFirst());
        model.addAttribute("approximateMaxSizeIncludingImages", mailingSize.getSecond());
        model.addAttribute("errorSizeThreshold", configService.getLongValue(ConfigValue.MailingSizeErrorThresholdBytes, admin.getCompanyID()));
        model.addAttribute("warningSizeThreshold", configService.getLongValue(ConfigValue.MailingSizeWarningThresholdBytes, admin.getCompanyID()));
    }

    protected void addIntervalSettingsToForm(MailingSendForm mailingForm, int companyId) {
        // Nothing to do
    }

    private void loadDependents(Admin admin, MailingSendForm form, Mailing mailing, Model model) {
        if (mailing.getMailingType() != MailingType.ACTION_BASED) {
            return;
        }

        int mailingId = mailing.getId();
        int companyID = admin.getCompanyID();
        List<MailingDependentType> types = List.of(MailingDependentType.values());

        if (admin.isRedesignedUiUsed()) {
            FormUtils.syncNumberOfRows(webStorage, WebStorage.MAILING_SEND_DEPENDENTS_OVERVIEW, form);
        } else {
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

            types = mailingDependencyService.detectActiveFilters(
                    form.getFilterTypes(),
                    MailingDependentType.ACTION,
                    MailingDependentType.WORKFLOW,
                    MailingDependentType.BOUNCE_FILTER
            );
        }

        model.addAttribute("dependents", mailingDependencyService.load(companyID, mailingId, types));
        model.addAttribute("bounceFilterNames", loadBounceFiltersNames(companyID, mailingId));
    }

    private String loadBounceFiltersNames(int companyID, int mailingId) {
        List<BounceFilterDto> bounceFilters = bounceFilterService.getDependentBounceFiltersWithActiveAutoResponder(companyID, mailingId);
        return bounceFilterService.getBounceFilterNames(bounceFilters);
    }

    private void loadDeliveryStatistics(Admin admin, Mailing mailing, MailingSendForm form, Model model) {
        int mailingId = mailing.getId();

        DeliveryStat deliveryStat = deliveryStatService.getDeliveryStats(admin.getCompanyID(), mailingId, mailing.getMailingType());
        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("deliveryStat", deliveryStat);
        } else {
            form.setDeliveryStat(deliveryStat);
        }
        model.addAttribute("targetGroupsNames", targetDao.getTargetNamesByIds(admin.getCompanyID(), targetService.getTargetIdsFromExpression(mailing)));
    }

    private boolean validateMailingSize(Mailing mailing, Popups popups, Admin admin) {
        Tuple<Long, Long> mailingSize = mailingSizeCalculationService.calculateSize(mailing, admin);
        long deliverySize = mailingSize.getFirst();

        long maximumMailingSizeAllowed = configService.getLongValue(ConfigValue.MailingSizeErrorThresholdBytes, admin.getCompanyID());
        if (deliverySize > maximumMailingSizeAllowed) {
            popups.alert("error.mailing.size.large", maximumMailingSizeAllowed);
            return false;
        }

        long warningMailingSize = configService.getLongValue(ConfigValue.MailingSizeWarningThresholdBytes, admin.getCompanyID());
        if (deliverySize > warningMailingSize) {
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

    protected void writeUserActivityLog(Admin admin, UserAction userAction) {
        userActivityLogService.writeUserActivityLog(admin, userAction, LOGGER);
    }
}
