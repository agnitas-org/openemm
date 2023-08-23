/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingSendOptions;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.TrackableLinkDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.components.entity.MailGenerationOptimizationMode;
import com.agnitas.emm.core.components.entity.TestRunOption;
import com.agnitas.emm.core.components.form.MailingTestSendForm;
import com.agnitas.emm.core.components.logger.MailingSendLogWriter;
import com.agnitas.emm.core.components.service.MailingBlockSizeService;
import com.agnitas.emm.core.components.service.MailingReportScheduleService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.components.service.MailingTriggerService;
import com.agnitas.emm.core.maildrop.MaildropGenerationStatus;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.forms.MailingIntervalSettingsForm;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.MailingStopServiceException;
import com.agnitas.exception.ValidationException;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.StringUtil;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.MailingStatus;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.OnepixelDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.mailing.service.MailingModel;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

@Service("MailingSendService")
public class MailingSendServiceImpl implements MailingSendService {

    private static final Logger logger = LogManager.getLogger(MailingSendServiceImpl.class);

    protected final MailingSendLogWriter logWriter;
    protected final ComMailingDao mailingDao;
    protected final MaildropStatusDao maildropStatusDao;
    protected MailingService mailingService;
    protected final MaildropService maildropService;
    private final MailingTriggerService mailingTriggerService;
    private final MailingBlockSizeService blockSizeService;
    private final ConfigService configService;
    private final MailinglistDao mailinglistDao;
    private final OnepixelDao onepixelDao;
    private final TrackableLinkDao trackableLinkDao;
    private final ComRecipientDao recipientDao;
    private final ComCompanyDao companyDao;
    private final ComMailingBaseService mailingBaseService;
    private final BlacklistService blacklistService;
    private final MailingReportScheduleService mailingReportScheduleService;
    private final MailingDeliveryBlockingService mailingDeliveryBlockingService;
    private final BounceFilterService bounceFilterService;
    private final JavaMailService javaMailService;
    private final MailingStopService mailingStopService;

    @Autowired
    public MailingSendServiceImpl(MailingSendLogWriter logWriter, ComMailingDao mailingDao, MaildropStatusDao maildropStatusDao, MailingTriggerService mailingTriggerService,
                                  MailingBlockSizeService blockSizeService, ConfigService configService, MailinglistDao mailinglistDao, MailingService mailingService,
                                  MaildropService maildropService, OnepixelDao onepixelDao, TrackableLinkDao trackableLinkDao, ComRecipientDao recipientDao, ComCompanyDao companyDao,
                                  ComMailingBaseService mailingBaseService, BlacklistService blacklistService, MailingReportScheduleService mailingReportScheduleService,
                                  MailingDeliveryBlockingService mailingDeliveryBlockingService, BounceFilterService bounceFilterService, JavaMailService javaMailService,
                                  MailingStopService mailingStopService) {

        this.logWriter = logWriter;
        this.mailingDao = mailingDao;
        this.maildropStatusDao = maildropStatusDao;
        this.mailingTriggerService = mailingTriggerService;
        this.blockSizeService = blockSizeService;
        this.configService = configService;
        this.mailinglistDao = mailinglistDao;
        this.mailingService = mailingService;
        this.maildropService = maildropService;
        this.onepixelDao = onepixelDao;
        this.trackableLinkDao = trackableLinkDao;
        this.recipientDao = recipientDao;
        this.companyDao = companyDao;
        this.mailingBaseService = mailingBaseService;
        this.blacklistService = blacklistService;
        this.mailingReportScheduleService = mailingReportScheduleService;
        this.mailingDeliveryBlockingService = mailingDeliveryBlockingService;
        this.bounceFilterService = bounceFilterService;
        this.javaMailService = javaMailService;
        this.mailingStopService = mailingStopService;
    }

    @Override
    public ServiceResult<UserAction> sendMailing(Mailing mailing, MailingSendOptions options, Admin admin) throws Exception {
        int companyId = mailing.getCompanyID();
        if (isLimitationForSendExists(companyId)) {
            return ServiceResult.error(Message.of("error.company.mailings.sent.forbidden"));
        }

        if (DeliveryType.TEST.equals(options.getDeliveryType())) {
            return trySendTestMailing(mailing, Collections.emptyList(), options);
        }

        if (DeliveryType.ADMIN.equals(options.getDeliveryType())) {
            return sendAdminMailing(mailing, options);
        }

        if (MailingType.NORMAL.equals(mailing.getMailingType()) || MailingType.FOLLOW_UP.equals(mailing.getMailingType())) {
            return sendWorldMailing(mailing, options, admin);
        }

        if (MailingType.DATE_BASED.equals(mailing.getMailingType())) {
            return activateDateBasedMailing(mailing, options);
        }

        if (MailingType.ACTION_BASED.equals(mailing.getMailingType())) {
            return activateActionBasedMailing(mailing, options);
        }

        return ServiceResult.error();
    }

    @Override
    public ServiceResult<UserAction> sendWorldMailing(Mailing mailing, MailingSendOptions sendOptions, Admin admin) throws Exception {
        int companyID = mailing.getCompanyID();
        int mailingID = mailing.getId();
        MailingType mailingType = mailing.getMailingType();

        if (mailingType != MailingType.FOLLOW_UP && maildropService.isActiveMailing(mailingID, companyID)) {
            return ServiceResult.error();
        }

        int numberOfRecipients = mailinglistDao.getNumberOfActiveWorldSubscribers(mailing.getTargetID(), companyID, mailing.getMailinglistID());

        SimpleServiceResult result = isRequiredDataAndComponentsExists(mailing, numberOfRecipients);

        if (!result.isSuccess()) {
            return ServiceResult.error(result.getErrorMessages());
        }

        activateDeeptrackingIfPossible(mailingID, companyID);

        if (!sendOptions.isFromWorkflow()) {
            scheduleReport(admin, mailing, sendOptions);
        }

        updateMediatypeEmail(mailing, sendOptions);

        int generationStatus = MaildropGenerationStatus.NOW.getCode();
        Date generationDate = detectGenerationDate(companyID, sendOptions.getDate(), sendOptions.isFromWorkflow());

        if (isDateForImmediateDelivery(generationDate)) {
            if (mailingType == MailingType.NORMAL && isPrioritized(mailing)) {
                generationStatus = MaildropGenerationStatus.SCHEDULED.getCode();
            }
        } else {
            generationStatus = MaildropGenerationStatus.SCHEDULED.getCode();
            mailing.setSenddate(sendOptions.getDate());
            mailingDao.updateStatus(mailing, MailingStatus.SCHEDULED);
        }

        MaildropEntry maildrop = prepareMaildropForWorldDelivery(mailing, sendOptions, generationDate, generationStatus);

        if (sendOptions.isFromWorkflow()) {
            maildropStatusDao.cleanup(mailing.getMaildropStatus());
        }

        int maildropId = maildropStatusDao.saveMaildropEntry(maildrop);

        if (!sendOptions.isFromWorkflow()) {
            mailingDeliveryBlockingService.blockByAutoImport(mailingID, sendOptions.getRequiredAutoImport(), maildropId);
        }

        if (generationStatus == MaildropGenerationStatus.NOW.getCode()) {
            mailingTriggerService.triggerMailing(maildropId, mailingType);
            mailingDao.updateStatus(mailing, MailingStatus.SCHEDULED);
        }

        clearTestActionsData(mailingID, companyID);

        if (logger.isInfoEnabled()) {
            logger.info("send mailing id: {} type: {}", mailingID, MaildropStatus.WORLD.getCode());
        }

        UserAction action;
        if (mailingType == MailingType.INTERVAL) {
            action = createIntervalMailingActivationAction(mailing, maildrop.getSendDate(), "send to world recipients");
        } else {
            action = createScheduleMailingAction(mailing, maildrop.getSendDate());
        }

        return new ServiceResult<>(action, true, result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
    }

    protected ServiceResult<UserAction> trySendTestMailing(Mailing mailing, List<String> testRecipients, MailingSendOptions sendOptions) throws Exception {
        int numberOfRecipients;
        if (!testRecipients.isEmpty()) {
            numberOfRecipients = testRecipients.size();
        } else {
            numberOfRecipients = mailinglistDao.getNumberOfActiveTestSubscribers(mailing.getTargetID(), mailing.getCompanyID(), mailing.getMailinglistID());
        }

        SimpleServiceResult result = isRequiredDataAndComponentsExists(mailing, numberOfRecipients);
        UserAction action = null;
        if (result.isSuccess()) {
            int maxAdminMails = configService.getIntegerValue(ConfigValue.MaxAdminMails, mailing.getCompanyID());

            if (isRecipientsCountLimitExceeds(numberOfRecipients, maxAdminMails)) {
                return ServiceResult.error(Message.of("error.mailing.send.admin.maxMails", maxAdminMails));
            }

            sendTest(mailing, testRecipients, MaildropStatus.TEST, sendOptions);
            action = createUserActionForTestMailingSent(mailing, new Date());
        }

        return new ServiceResult<>(action, result.isSuccess(), result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
    }

    private void sendTest(Mailing mailing, List<String> testRecipients, MaildropStatus maildropStatus, MailingSendOptions sendOptions) throws Exception {
        updateMediatypeEmail(mailing, sendOptions);

        int companyId = mailing.getCompanyID();
        int mailingId = mailing.getId();
        MailingType type = mailing.getMailingType();

        MaildropEntry maildrop = prepareMaildropForTestDelivery(mailing, maildropStatus, sendOptions);

        if (sendOptions.isFromWorkflow()) {
            // Remove an existing maildrop entries
            maildropStatusDao.cleanup(mailing.getMaildropStatus());
        }

        int maildropId = maildropStatusDao.saveMaildropEntry(maildrop);

        List<Integer> savedTestRecipients = saveTestRecipients(testRecipients, mailing);
        maildropService.selectTestRecipients(companyId, maildropId, savedTestRecipients);

        if (!(sendOptions.isFromWorkflow() && MailingType.DATE_BASED.equals(type))) {
            mailingTriggerService.triggerMailing(maildropId, type);
            updateStatusByMaildropStatus(mailing, maildropStatus);
        }

        if (sendOptions.isFromWorkflow()) {
            clearTestActionsData(mailingId, companyId);
        }

        if (logger.isInfoEnabled()) {
            logger.info("send mailing id: {} type: {}", mailingId, maildropStatus.getCode());
        }
    }

    private UserAction createUserActionForTestMailingSent(Mailing mailing, Date sendDate) {
        MailingType mailingType = mailing.getMailingType();

        if (mailingType == MailingType.ACTION_BASED) {
            return createActionBasedActivationAction(mailing);
        }

        if (mailingType == MailingType.DATE_BASED) {
            return createDateBasedActivationAction(mailing, sendDate);
        }

        if (mailingType == MailingType.NORMAL || mailingType == MailingType.FOLLOW_UP) {
            return createScheduleMailingAction(mailing, sendDate);
        }

        return null;
    }

    private boolean isRecipientsCountLimitExceeds(int activeRecipientsCount, int limit) {
        return activeRecipientsCount > limit;
    }

    private UserAction createActionBasedActivationAction(Mailing mailing) {
        return new UserAction("do activate mailing", String.format("Mailing type: %s. %s", mailing.getMailingType().name(), getTriggerMailingDescription(mailing)));
    }

    private UserAction createDateBasedActivationAction(Mailing mailing, Date sendDate) {
        return new UserAction("do activate mailing", String.format("Mailing type: %s, at: %s. %s",
                mailing.getMailingType().name(),
                DateUtilities.getDateFormat(DateFormat.SHORT, Locale.UK).format(sendDate),
                getTriggerMailingDescription(mailing)));
    }

    private UserAction createScheduleMailingAction(Mailing mailing, Date sendDate) {
        String sendActionDescription = "send to world recipients";

        return new UserAction("do schedule mailing", String.format("Mailing type: %s, at: %s. %s",
                mailing.getMailingType().name(),
                DateUtilities.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK).format(sendDate),
                getRegularMailingDescription(mailing, sendActionDescription)));
    }

    protected UserAction createIntervalMailingActivationAction(Mailing mailing, Date sendDate, String actionDescription) {
        return new UserAction("do activate mailing", String.format("Mailing type: %s, next start at: %s. %s",
                mailing.getMailingType().name(),
                DateUtilities.getDateTimeFormat(DateFormat.MEDIUM, DateFormat.SHORT, Locale.UK).format(sendDate),
                getRegularMailingDescription(mailing, actionDescription)));
    }

    private String getRegularMailingDescription(Mailing mailing, String sendActionDescription) {
        return String.format(
                "%s (%d) (delivery type: %s)",
                mailing.getShortname(),
                mailing.getId(),
                sendActionDescription
        );
    }

    private String getTriggerMailingDescription(Mailing mailing) {
        return String.format("%s (%d)", mailing.getShortname(), mailing.getId());
    }

    private SimpleServiceResult isRequiredDataAndComponentsExists(Mailing mailing, int numberOfRecipients) {
        if (!isMailinglistExists(mailing.getMailinglistID(), mailing.getCompanyID())) {
            return SimpleServiceResult.simpleError(Message.of("noMailinglistAssigned"));
        }

        if (numberOfRecipients == 0 && mailing.getMailingType() == MailingType.NORMAL) {
            return SimpleServiceResult.simpleError(Message.of("error.mailing.no_subscribers"));
        }

        List<Message> warnings = new ArrayList<>();

        if (mailingDao.hasEmail(mailing.getCompanyID(), mailing.getId())) {
            MediatypeEmail param = mailing.getEmailParam();

            if (!isTextVersionExists(mailing)) {
                if (mailingService.isTextVersionRequired(mailing.getCompanyID(), mailing.getId())) {
                    return SimpleServiceResult.simpleError(Message.of("error.mailing.no_text_version"));
                } else {
                    warnings.add(Message.of("error.mailing.no_text_version"));
                }
            }

            if (!isHtmlVersionExists(mailing)) {
                return SimpleServiceResult.simpleError(Message.of("error.mailing.no_html_version"));
            }

            if (StringUtils.isBlank(param.getSubject())) {
                return SimpleServiceResult.simpleError(Message.of("error.mailing.subject.too_short"));
            }

            if (!isSenderAddressExists(param)) {
                return SimpleServiceResult.simpleError(Message.of("error.mailing.sender_adress"));
            }
        }

        return SimpleServiceResult.simpleWarning(warnings);
    }

    private boolean isMailinglistExists(int mailinglistId, int companyId) {
        Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistId, companyId);
        return mailinglist != null;
    }

    private boolean isTextVersionExists(Mailing mailing) {
        return !isContentBlank(mailing, mailing.getTextTemplate());
    }

    private boolean isHtmlVersionExists(Mailing mailing) {
        // Check the HTML version unless mail format is "only text".
        return mailing.getEmailParam().getMailFormat() == MailingModel.Format.TEXT.getCode()
                || !isContentBlank(mailing, mailing.getHtmlTemplate());
    }

    private boolean isSenderAddressExists(MediatypeEmail param) {
        String senderAddress = null;
        try {
            senderAddress = param.getFromAdr();
        } catch (Exception e) {
            logger.error(String.format("Error occurred: %s", e.getMessage()), e);
        }

        return !StringUtils.isBlank(senderAddress);
    }

    @Override
    public ServiceResult<UserAction> sendTestMailing(Mailing mailing, MailingTestSendForm form, Admin admin) throws Exception {
        TestRunOption testRunOption = form.getTestRunOption();
        MailingSendOptions.Builder sendOptionsBuilder = MailingSendOptions.builder()
                .setAdminTargetGroupId(form.getAdminTargetGroupID());

        if (testRunOption == TestRunOption.RECIPIENT) {
            checkTestRecipients(admin.getCompanyID(), getTestSingleRecipients(form));
        } else if (testRunOption == TestRunOption.SEND_TO_SELF && isNotEmpty(form.getMailingTestRecipients())) { // personalized test GWUA-5664
            int recipientOfAdmin = recipientDao.getOrCreateRecipientOfAdmin(admin);
            sendOptionsBuilder.setOverwriteTestRecipientId(recipientOfAdmin);
        } else if (testRunOption == TestRunOption.SEND_TO_SELF) {
            form.setMailingTestRecipients(new String[]{admin.getEmail()});
        }
        return trySendTestMailing(mailing, Arrays.asList(form.getMailingTestRecipients()), sendOptionsBuilder.build());
    }

    @Override
    public ServiceResult<UserAction> sendAdminMailing(Mailing mailing, MailingSendOptions sendOptions) throws Exception {
        int numberOfRecipients = mailinglistDao.getNumberOfActiveAdminSubscribers(mailing.getTargetID(), mailing.getCompanyID(), mailing.getMailinglistID());

        SimpleServiceResult result = isRequiredDataAndComponentsExists(mailing, numberOfRecipients);
        UserAction userAction = null;

        if (result.isSuccess()) {
            int maxAdminMails = configService.getIntegerValue(ConfigValue.MaxAdminMails, mailing.getCompanyID());

            if (isRecipientsCountLimitExceeds(numberOfRecipients, maxAdminMails)) {
                return ServiceResult.error(Message.of("error.mailing.send.admin.maxMails", maxAdminMails));
            }

            sendTest(mailing, Collections.emptyList(), MaildropStatus.ADMIN, sendOptions);
            userAction = createUserActionForTestMailingSent(mailing, new Date());
        }

        return new ServiceResult<>(userAction, result.isSuccess(), result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
    }

    private void checkTestRecipients(int companyId, List<String> addresses) {
        if (addresses.isEmpty()) {
            throw new ValidationException("enterEmailAddresses");
        }

        Set<Message> errors = new HashSet<>();
        boolean invalidEmailExists = false;

        for (String address : addresses) {
            if (AgnUtils.isEmailValid(address)) {
                if (blacklistService.blacklistCheckCompanyOnly(address, companyId)) {
                    errors.add(Message.of("error.email.blacklisted", address));
                }
            } else if (!invalidEmailExists) {
                invalidEmailExists = true;
                errors.add(Message.of("error.invalid.email"));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    @Override
    public boolean isLimitationForSendExists(int companyId) {
        return companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany);
    }

    private MaildropEntry prepareMaildropForTestDelivery(Mailing mailing, MaildropStatus maildropStatus, MailingSendOptions options) {
        int companyId = mailing.getCompanyID();
        MailingType type = mailing.getMailingType();

        Date now = new Date();
        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(now);
        maildrop.setGenChangeDate(now);
        maildrop.setStatus(maildropStatus.getCode());
        maildrop.setSendDate(now);
        maildrop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
        maildrop.setMailingID(mailing.getId());
        maildrop.setCompanyID(companyId);
        maildrop.setAdminTestTargetID(Math.max(0, options.getAdminTargetGroupId()));
        maildrop.setOverwriteTestRecipient(options.getOverwriteTestRecipientId());

        if (options.isFromWorkflow() && MailingType.DATE_BASED.equals(type)) {
            // Set genstatus equals 0 to trigger WM-specific test sending mode of backend for date-based mailings.
            maildrop.setGenStatus(MaildropGenerationStatus.SCHEDULED.getCode());
        }

        if (type == MailingType.NORMAL || type == MailingType.FOLLOW_UP) {
            Tuple<Integer, Integer> result = blockSizeService.calculateBlocksizeStepping(companyId, 0, 0);

            maildrop.setStepping(result.getSecond());
            maildrop.setBlocksize(result.getFirst());
        }

        return maildrop;
    }

    private MaildropEntry prepareMaildropForWorldDelivery(Mailing mailing, MailingSendOptions sendOptions, Date generationDate, int generationStatus) {
        int companyID = mailing.getCompanyID();
        int mailingID = mailing.getId();
        MailingType mailingType = mailing.getMailingType();

        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(generationDate);
        maildrop.setMailGenerationOptimization(getMaildropGenerationCode(sendOptions.getGenerationOptimization()));
        maildrop.setStatus(MaildropStatus.WORLD.getCode());
        maildrop.setMaxRecipients(sendOptions.getMaxRecipients());
        maildrop.setSendDate(sendOptions.getDate());
        maildrop.setGenStatus(generationStatus);
        maildrop.setGenChangeDate(new Date());
        maildrop.setMailingID(mailingID);
        maildrop.setCompanyID(companyID);

        if (sendOptions.getGenerationOptimization() == MailGenerationOptimizationMode.NONE.getCode()
                && (mailingType == MailingType.NORMAL || mailingType == MailingType.FOLLOW_UP)) {
            Tuple<Integer, Integer> result = blockSizeService.calculateBlocksizeStepping(companyID, sendOptions.getStepping(), sendOptions.getBlockSize());

            maildrop.setStepping(result.getSecond());
            maildrop.setBlocksize(result.getFirst());
        }

        return maildrop;
    }

    @Override
    public ServiceResult<UserAction> activateDateBasedMailing(Mailing mailing, MailingSendOptions sendOptions) {
        int companyID = mailing.getCompanyID();
        int mailingID = mailing.getId();

        if (maildropService.isActiveMailing(mailingID, companyID)) {
            return ServiceResult.error();
        }

        int numberOfRecipients = mailinglistDao.getNumberOfActiveWorldSubscribers(mailing.getTargetID(), companyID, mailing.getMailinglistID());
        SimpleServiceResult result = isRequiredDataAndComponentsExists(mailing, numberOfRecipients);

        if (!result.isSuccess()) {
            return ServiceResult.error(result.getErrorMessages());
        }

        activateDeeptrackingIfPossible(mailingID, companyID);
        MaildropEntry maildrop = prepareDateBasedMaildrop(mailing, sendOptions.getDate(), sendOptions.isFromWorkflow());

        try {
            if (sendOptions.isFromWorkflow()) {
                maildropStatusDao.cleanup(mailing.getMaildropStatus());
                updateMediatypeEmail(mailing, sendOptions);
            }

            int maildropId = maildropStatusDao.saveMaildropEntry(maildrop);

            if (!sendOptions.isFromWorkflow()) {
                mailingDeliveryBlockingService.blockByAutoImport(mailingID, sendOptions.getRequiredAutoImport(), maildropId);
            }

            clearTestActionsData(mailingID, companyID);

            if (logger.isInfoEnabled()) {
                logger.info("send mailing id: {} type: {}", mailingID, MaildropStatus.DATE_BASED.getCode());
            }

            return new ServiceResult<>(createDateBasedActivationAction(mailing, maildrop.getSendDate()), true, result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
        } catch (Exception e) {
            logger.error(String.format("Error occurred during activation of databased mailing! ID - %d", mailingID), e);
            return ServiceResult.error();
        }
    }

    private MaildropEntry prepareDateBasedMaildrop(Mailing mailing, Date sendDate, boolean isFromWorkflow) {
        sendDate = truncateDateSecondsAndMilliseconds(sendDate);
        Date generationDate = detectGenerationDate(mailing.getCompanyID(), sendDate, isFromWorkflow);

        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(generationDate);
        maildrop.setStatus(MaildropStatus.DATE_BASED.getCode());
        maildrop.setSendDate(sendDate);
        maildrop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
        maildrop.setGenChangeDate(new Date());
        maildrop.setMailingID(mailing.getId());
        maildrop.setCompanyID(mailing.getCompanyID());

        return maildrop;
    }

    @Override
    public ServiceResult<UserAction> activateActionBasedMailing(Mailing mailing, MailingSendOptions sendOptions) {
        int companyID = mailing.getCompanyID();
        int mailingID = mailing.getId();

        if (maildropService.isActiveMailing(mailingID, companyID)) {
            return ServiceResult.error();
        }

        int numberOfRecipients = mailinglistDao.getNumberOfActiveWorldSubscribers(mailing.getTargetID(), companyID, mailing.getMailinglistID());

        SimpleServiceResult result = isRequiredDataAndComponentsExists(mailing, numberOfRecipients);

        if (!result.isSuccess()) {
            return ServiceResult.error(result.getErrorMessages());
        }

        activateDeeptrackingIfPossible(mailingID, companyID);
        MaildropEntry maildrop = prepareActionBasedMaildrop(mailing);

        try {
            if (sendOptions.isFromWorkflow()) {
                updateMediatypeEmail(mailing, sendOptions);
                maildropStatusDao.cleanup(mailing.getMaildropStatus());
            }

            maildropStatusDao.saveMaildropEntry(maildrop);
            clearTestActionsData(mailingID, companyID);

            if (logger.isInfoEnabled()) {
                logger.info("send mailing id: {} type: {}", mailingID, MaildropStatus.ACTION_BASED.getCode());
            }

            return new ServiceResult<>(createActionBasedActivationAction(mailing), true, result.getSuccessMessages(), result.getWarningMessages(), result.getErrorMessages());
        } catch (Exception e) {
            logger.error(String.format("Error occurred during activation of actionbased mailing! ID - %d", mailingID), e);
            return ServiceResult.error();
        }
    }

    private MaildropEntry prepareActionBasedMaildrop(Mailing mailing) {
        Date now = new Date();
        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(now);
        maildrop.setStatus(MaildropStatus.ACTION_BASED.getCode());
        maildrop.setSendDate(now);
        maildrop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
        maildrop.setGenChangeDate(now);
        maildrop.setMailingID(mailing.getId());
        maildrop.setCompanyID(mailing.getCompanyID());

        return maildrop;
    }

    private String getMaildropGenerationCode(int generationModeCode) {
        MailGenerationOptimizationMode mode = MailGenerationOptimizationMode.fromCode(generationModeCode);
        if (mode != null) {
            return mode.getMaildropCode();
        }

        return null;
    }

    private void updateMediatypeEmail(Mailing mailing, MailingSendOptions sendOptions) {
        MediatypeEmail emailParam = mailing.getEmailParam();

        if (emailParam != null) {
            emailParam.setDoublechecking(sendOptions.isCheckForDuplicateRecords());
            emailParam.setSkipempty(sendOptions.isSkipWithEmptyTextContent());

            if (sendOptions.getFollowupFor() > 0) {
                emailParam.setFollowupFor(String.valueOf(sendOptions.getFollowupFor()));
            }

            mailingDao.saveMailing(mailing, false);
        }
    }

    private void clearTestActionsData(int mailingID, int companyId) {
        // when the world mailing is sent we need to remove
        // - admin/test clicks
        // - admin/test openings
        // - admin/test data from success_xxx_tbl
        // as we don't want to show that in statistics
        onepixelDao.deleteAdminAndTestOpenings(mailingID, companyId);
        trackableLinkDao.deleteAdminAndTestClicks(mailingID, companyId);
        mailingDao.cleanTestDataInSuccessTbl(mailingID, companyId);
        mailingDao.cleanTestDataInMailtrackTbl(mailingID, companyId);
    }

    private boolean isDateForImmediateDelivery(Date sendDate) {
        // Move "current time" into future, so we get a fairness period
        Date boundDate = DateUtilities.addMinutesToDate(new Date(), FAIRNESS_PERIOD_IN_MINUTES);
        return boundDate.after(sendDate);
    }

    private Date detectGenerationDate(int companyId, Date sendDate, boolean isFromWorkflow) {
        if (isFromWorkflow) {
            // since the decision icon creates a target group, like "mailing opened / not opened",
            // then the generation should start at the time of sending, because if it happens earlier,
            // then the recipients will not be found
            return sendDate;
        }

        if (isDateForImmediateDelivery(sendDate)) {
            return new Date();
        }

        Date generationDate = DateUtils.addMinutes(sendDate, -mailingService.getMailGenerationMinutes(companyId));

        if (DateUtilities.isPast(generationDate)) {
            return new Date();
        }

        return generationDate;
    }

    private Date truncateDateSecondsAndMilliseconds(Date date) {
        Calendar calendar = DateUtilities.calendar(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    protected List<Integer> saveTestRecipients(List<String> addresses, Mailing mailing) throws Exception {
        if (CollectionUtils.isNotEmpty(addresses)) {
            return recipientDao.insertTestRecipients(mailing.getCompanyID(), mailing.getMailinglistID(), UserStatus.Suspend, "Test recipient for delivery test", addresses);
        }

        return Collections.emptyList();
    }

    private List<String> getTestSingleRecipients(MailingTestSendForm form) {
        String[] addresses = form.getMailingTestRecipients();

        if (addresses != null) {
            String[] notBlankAddresses = StringUtil.excludeBlank(addresses);
            addresses = Arrays.stream(notBlankAddresses)
                    .distinct()
                    .toArray(String[]::new);

            form.setMailingTestRecipients(addresses);

            return Arrays.asList(addresses);
        }

        return Collections.emptyList();
    }

    private void activateDeeptrackingIfPossible(int mailingID, int companyID) {
        if (companyDao.checkDeeptrackingAutoActivate(companyID)) {
            trackableLinkDao.activateDeeptracking(companyID, mailingID);
        }
    }

    private void scheduleReport(Admin admin, Mailing mailing, MailingSendOptions sendOptions) throws Exception {
        if (sendOptions.isReportSendAfter24h()) {
            mailingReportScheduleService.scheduleNewReport(AgnUtils.splitAndTrimList(sendOptions.getReportSendEmail()), admin, mailing, sendOptions.getDate(), BirtReportType.TYPE_AFTER_MAILING_24HOURS);
        }

        if (sendOptions.isReportSendAfter48h()) {
            mailingReportScheduleService.scheduleNewReport(AgnUtils.splitAndTrimList(sendOptions.getReportSendEmail()), admin, mailing, sendOptions.getDate(), BirtReportType.TYPE_AFTER_MAILING_48HOURS);
        }

        if (sendOptions.isReportSendAfter1Week()) {
            mailingReportScheduleService.scheduleNewReport(AgnUtils.splitAndTrimList(sendOptions.getReportSendEmail()), admin, mailing, sendOptions.getDate(), BirtReportType.TYPE_AFTER_MAILING_WEEK);
        }
    }

    private boolean isContentBlank(Mailing mailing, MailingComponent template) {
        if (template == null) {
            return true;
        }

        return mailingBaseService.isContentBlank(template.getEmmBlock(), mailing.getDynTags());
    }

    protected boolean isPrioritized(Mailing mailing) {
        return false;
    }

    private void updateStatusByMaildropStatus(Mailing mailing, MaildropStatus status) {
        if (status.getCode() == MaildropStatus.TEST.getCode()) {
            mailingDao.updateStatus(mailing, MailingStatus.TEST);
        } else if (status.getCode() == MaildropStatus.ADMIN.getCode()) {
            mailingDao.updateStatus(mailing, MailingStatus.ADMIN);
        }
    }

    @Override
    public boolean deactivateMailing(Mailing mailing, int companyId, boolean isWorkflowDriven) {
        if (mailing == null) {
            return false;
        }

        if (isWorkflowDriven && (mailing.getMailingType() == MailingType.ACTION_BASED || mailing.getMailingType() == MailingType.DATE_BASED)) {
            if (logger.isWarnEnabled()) {
                logger.warn(String.format("Tried to deactivate %s mailing with driven workflow!", mailing.getMailingType().getWebserviceCode()));
            }

            return false;
        }

        if (mailing.getMailingType() == MailingType.ACTION_BASED && bounceFilterService.isMailingUsedInBounceFilterWithActiveAutoResponder(companyId, mailing.getId())) {
            return false;
        }

        maildropStatusDao.cleanup(mailing.getMaildropStatus());
        mailingDao.saveMailing(mailing, false);
        mailingDao.updateStatus(mailing, MailingStatus.DISABLE);

        return true;
    }

    @Override
    public void unlockMailing(Mailing mailing) {
        mailing.setLocked(0);

        mailingDao.saveMailing(mailing, false);
        mailingDao.updateStatus(mailing, MailingStatus.READY);
    }

    @Override
    public void deactivateIntervalMailing(int mailingId, Admin admin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleServiceResult activateIntervalMailing(MailingIntervalSettingsForm intervalSettings, int mailingId, Admin admin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSendOrActivateMailing(Admin admin, Mailing mailing) throws Exception {
        if (!maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
            if (admin.permissionAllowed(Permission.MAILING_CAN_SEND_ALWAYS)) {
                return true;
            }

            return !mailingService.isMailingLocked(mailing);
        }

        return false;
    }

    @Override
    public boolean isMailingActiveOrSent(Mailing mailing) throws Exception {
        return maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID());
    }

    @Override
    public boolean cancelMailingDelivery(int mailingID, int companyId) {
        try {
            return mailingStopService.stopMailing(companyId, mailingID, false);
        } catch (MailingStopServiceException e) {
            logger.error(String.format("Error stopping mailing %d", mailingID), e);
            return false;
        }
    }

    @Override
    public void sendEmail(Admin admin, String senderDomain) {
        int companyID = admin.getCompanyID();

        String toAddressList = configService.getValue(ConfigValue.Mailaddress_Support, companyID);
        Company company = companyDao.getCompany(companyID);
        String companyAdminMail = company.getContactTech();

        if (StringUtils.isNotBlank(companyAdminMail)) {
            toAddressList += ", " + companyAdminMail;
        }

        javaMailService.sendEmail(0, null, null, null, null, null, toAddressList, null,
                I18nString.getLocaleString("mandatoryDkimKeyMissing.subject", admin.getLocale()),
                I18nString.getLocaleString("mandatoryDkimKeyMissing.text", admin.getLocale(), String.format("%s (CID: %d)", company.getShortname(), companyID), senderDomain),
                I18nString.getLocaleString("mandatoryDkimKeyMissing.text", admin.getLocale(), String.format("%s (CID: %d)", company.getShortname(), companyID), senderDomain),
                "UTF-8");
    }

    @Override
    public void checkIfMailingCanBeSend(Mailing mailing, Date sendDate, TimeZone timeZone) throws Exception {
        List<String> errors = new ArrayList<>();

        int fairnessPeriod = configService.getIntegerValue(ConfigValue.SendFairnessMinutes);

        Date currentDate = new GregorianCalendar(timeZone).getTime();
        currentDate = DateUtilities.addMinutesToDate(currentDate, -fairnessPeriod);

        boolean isDateValid = DateUtilities.millisecondsToMinutes(currentDate.getTime()) <= DateUtilities.millisecondsToMinutes(sendDate.getTime());
        if (isDateValid) {
            if (mailingService.isMissingNecessaryTargetGroup(mailing)) {
                errors.add("error.mailing.rulebased_without_target");
            }
        } else {
            errors.add("error.you_choose_a_time_before_the_current_time");
        }

        if (mailing.getMailingType() == MailingType.FOLLOW_UP
                && mailingService.isFollowupMailingDateBeforeDate(mailing, sendDate)) {
            errors.add("error.mailing.followup.senddate_before_basemail");
        }

        if (mailingService.containsInvalidTargetGroups(mailing.getCompanyID(), mailing.getId())) {
            errors.add("error.mailing.containsInvaidTargetGroups");
        }

        Mailinglist mailinglist = mailinglistDao.getMailinglist(mailing.getMailinglistID(), mailing.getCompanyID());
        if (mailinglist == null) {
            errors.add("noMailinglistAssigned");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toArray(String[]::new));
        }
    }
}
