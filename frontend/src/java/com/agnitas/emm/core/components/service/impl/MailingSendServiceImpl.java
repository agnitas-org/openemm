/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Company;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.Mailing;
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
import com.agnitas.emm.core.components.form.MailingSendForm;
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
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.StringUtil;
import com.agnitas.web.mvc.Popups;
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
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class MailingSendServiceImpl implements MailingSendService {

    private static final Logger logger = LogManager.getLogger(MailingSendServiceImpl.class);

    protected final MailingSendLogWriter logWriter;
    protected final ComMailingDao mailingDao;
    protected final MaildropStatusDao maildropStatusDao;
    protected final MailingService mailingService;
    private final MailingTriggerService mailingTriggerService;
    private final MailingBlockSizeService blockSizeService;
    private final ConfigService configService;
    private final MailingStopService mailingStopService;
    private final MailinglistDao mailinglistDao;
    private final MaildropService maildropService;
    private final OnepixelDao onepixelDao;
    private final TrackableLinkDao trackableLinkDao;
    private final ComRecipientDao recipientDao;
    private final ComCompanyDao companyDao;
    private final ComMailingBaseService mailingBaseService;
    private final BounceFilterService bounceFilterService;
    private final BlacklistService blacklistService;
    private final JavaMailService javaMailService;
    private final MailingReportScheduleService mailingReportScheduleService;
    private final MailingDeliveryBlockingService mailingDeliveryBlockingService;

    @Autowired
    public MailingSendServiceImpl(MailingSendLogWriter logWriter, ComMailingDao mailingDao, MaildropStatusDao maildropStatusDao, MailingTriggerService mailingTriggerService, MailingBlockSizeService blockSizeService, ConfigService configService, MailingStopService mailingStopService, MailinglistDao mailinglistDao, MailingService mailingService, MaildropService maildropService, OnepixelDao onepixelDao, TrackableLinkDao trackableLinkDao, ComRecipientDao recipientDao, ComCompanyDao companyDao, ComMailingBaseService mailingBaseService, BounceFilterService bounceFilterService, BlacklistService blacklistService, JavaMailService javaMailService, MailingReportScheduleService mailingReportScheduleService, MailingDeliveryBlockingService mailingDeliveryBlockingService) {
        this.logWriter = logWriter;
        this.mailingDao = mailingDao;
        this.maildropStatusDao = maildropStatusDao;
        this.mailingTriggerService = mailingTriggerService;
        this.blockSizeService = blockSizeService;
        this.configService = configService;
        this.mailingStopService = mailingStopService;
        this.mailinglistDao = mailinglistDao;
        this.mailingService = mailingService;
        this.maildropService = maildropService;
        this.onepixelDao = onepixelDao;
        this.trackableLinkDao = trackableLinkDao;
        this.recipientDao = recipientDao;
        this.companyDao = companyDao;
        this.mailingBaseService = mailingBaseService;
        this.bounceFilterService = bounceFilterService;
        this.blacklistService = blacklistService;
        this.javaMailService = javaMailService;
        this.mailingReportScheduleService = mailingReportScheduleService;
        this.mailingDeliveryBlockingService = mailingDeliveryBlockingService;
    }

    @Override
    public void checkIfMailingCanBeSend(Mailing mailing, Date sendDate, TimeZone timeZone) throws Exception {
        List<String> errors = new ArrayList<>();

        int fairnessPeriod = configService.getIntegerValue(ConfigValue.SendFairnessMinutes);

        Date currentDate = new GregorianCalendar(timeZone).getTime();
        DateUtilities.addMinutesToDate(currentDate, -fairnessPeriod);

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
    public boolean cancelMailingDelivery(int mailingID, int companyId) {
        try {
            return mailingStopService.stopMailing(companyId, mailingID, false);
        } catch (MailingStopServiceException e) {
            logger.error(String.format("Error stopping mailing %d", mailingID), e);
            return false;
        }
    }

    @Override
    public boolean saveEncryptedState(int mailingId, int companyId, boolean isEncryptedSend) {
        return false;
    }

    @Override
    public boolean isWorldMailingCanBeSend(Admin admin, Mailing mailing) throws Exception {
        if ((mailing.getMailingType() == MailingType.NORMAL || mailing.getMailingType() == MailingType.FOLLOW_UP)
                && !maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
            if (admin.permissionAllowed(Permission.MAILING_CAN_SEND_ALWAYS)) {
                return true;
            }

            return !mailingService.isMailingLocked(mailing);
        }

        return false;
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
    public void sendTestMailing(Mailing mailing, MailingTestSendForm form, Admin admin, Popups popups) throws Exception {
        if (form.getAdminTargetGroupID() == ADMIN_TARGET_SINGLE_RECIPIENT) {
            checkTestRecipients(admin.getCompanyID(), getTestSingleRecipients(form));
        }

        trySendTestMailing(mailing, admin, form, popups);
    }

    @Override
    public void sendAdminMailing(Mailing mailing, Admin admin, MailingTestSendForm form, Popups popups) throws Exception {
        int numberOfRecipients = mailinglistDao.getNumberOfActiveAdminSubscribers(mailing.getTargetID(), mailing.getCompanyID(), mailing.getMailinglistID());

        checkIfPossibleToSendMailing(admin.getCompanyID(), mailing, popups, numberOfRecipients);
        sendTest(admin, mailing, Collections.emptyList(), form.getAdminTargetGroupID(), MaildropStatus.ADMIN);
    }

    protected void trySendTestMailing(Mailing mailing, Admin admin, MailingTestSendForm form, Popups popups) throws Exception {
        List<String> testRecipients = Arrays.asList(form.getMailingTestRecipients());

        int numberOfRecipients;
        if (!testRecipients.isEmpty()) {
            numberOfRecipients = testRecipients.size();
        } else {
            numberOfRecipients = mailinglistDao.getNumberOfActiveTestSubscribers(mailing.getTargetID(), mailing.getCompanyID(), mailing.getMailinglistID());
        }

        checkIfPossibleToSendMailing(admin.getCompanyID(), mailing, popups, numberOfRecipients);
        sendTest(admin, mailing, testRecipients, form.getAdminTargetGroupID(), MaildropStatus.TEST);
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
    public SimpleServiceResult activateIntervalMailing(MailingIntervalSettingsForm intervalSettings,
                                                       int mailingId, Admin admin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleServiceResult saveIntervalSettings(MailingIntervalSettingsForm intervalSettings,
                                                    Mailing mailing, Admin admin) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deactivateIntervalMailing(int mailingId, Admin admin) {
        // nothing to do for OpenEMM
    }

    @Override
    public boolean isLimitationForSendExists(int companyId) {
        return companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany);
    }

    @Override
    public void unlockMailing(Mailing mailing) {
        mailing.setLocked(0);

        mailingDao.saveMailing(mailing, false);
        mailingDao.updateStatus(mailing, MailingStatus.READY);
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

    private void checkIfPossibleToSendMailing(int companyId, Mailing mailing, Popups popups, int numberOfRecipients) {
        checkIfMailinglistExists(mailing.getMailinglistID(), mailing.getCompanyID());

        if (numberOfRecipients == 0 && mailing.getMailingType() == MailingType.NORMAL) {
            throw new ValidationException("error.mailing.no_subscribers");
        }

        if (mailingDao.hasEmail(companyId, mailing.getId())) {
            MediatypeEmail param = mailing.getEmailParam();

            checkIfTextVersionExists(mailing, popups);
            checkIfHtmlVersionExists(mailing);
            checkSubject(param);
            checkSenderAddress(param);
        }
    }

    private void checkIfMailinglistExists(int mailinglistId, int companyId) {
        Mailinglist mailinglist = mailinglistDao.getMailinglist(mailinglistId, companyId);

        if (mailinglist == null) {
            throw new ValidationException("noMailinglistAssigned");
        }
    }

    private void checkIfTextVersionExists(Mailing mailing, Popups popups) {
        if (isContentBlank(mailing, mailing.getTextTemplate())) {
            if (mailingService.isTextVersionRequired(mailing.getCompanyID(), mailing.getId())) {
                throw new ValidationException("error.mailing.no_text_version");
            } else {
                popups.warning("error.mailing.no_text_version");
            }
        }
    }

    private void checkIfHtmlVersionExists(Mailing mailing) {
        MediatypeEmail param = mailing.getEmailParam();

        // Check the HTML version unless mail format is "only text".
        if (param.getMailFormat() != MailingModel.Format.TEXT.getCode() && isContentBlank(mailing, mailing.getHtmlTemplate())) {
            throw new ValidationException("error.mailing.no_html_version");
        }
    }

    private void checkSubject(MediatypeEmail param) {
        if (StringUtils.isBlank(param.getSubject())) {
            throw new ValidationException("error.mailing.subject.too_short");
        }
    }

    private void checkSenderAddress(MediatypeEmail param) {
        String senderAddress = null;
        try {
            senderAddress = param.getFromAdr();
        } catch (Exception e) {
            logger.error(String.format("Error occurred: %s", e.getMessage()), e);
        }

        if (StringUtils.isBlank(senderAddress)) {
            throw new ValidationException("error.mailing.sender_adress");
        }
    }

    private void sendTest(Admin admin, Mailing mailing, List<String> testRecipients, int adminTargetGroupId, MaildropStatus maildropStatus) throws Exception {
        updateMediatypeEmail(mailing, false, false);

        Date now = new Date();
        int companyId = admin.getCompanyID();
        int mailingId = mailing.getId();
        MailingType type = mailing.getMailingType();

        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(now);
        maildrop.setGenChangeDate(now);
        maildrop.setMailGenerationOptimization(MailGenerationOptimizationMode.NONE.getMaildropCode());
        maildrop.setStatus(maildropStatus.getCode());
        maildrop.setSendDate(now);
        maildrop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
        maildrop.setMailingID(mailingId);
        maildrop.setCompanyID(companyId);
        maildrop.setAdminTestTargetID(Math.max(0, adminTargetGroupId));

        if (type == MailingType.NORMAL || type == MailingType.FOLLOW_UP) {
            Tuple<Integer, Integer> result = blockSizeService.calculateBlocksizeStepping(companyId, 0, 0);

            maildrop.setStepping(result.getSecond());
            maildrop.setBlocksize(result.getFirst());
        }

        int maildropId = maildropStatusDao.saveMaildropEntry(maildrop);
        saveTestRecipientsAndSetForMaildrop(companyId, mailing.getMailinglistID(), maildropId, testRecipients);

        mailingTriggerService.triggerMailing(maildropId, type);
        updateStatusByMaildropStatus(mailing, MaildropStatus.fromCode(maildropStatus.getCode()));

        if (logger.isInfoEnabled()) {
            logger.info(String.format("send mailing id: %d type: %s", mailingId, MaildropStatus.fromCode(maildropStatus.getCode())));
        }

        logWriter.writeLogsAboutTestMailingSent(mailing, admin, now);
    }

    @Override
    public void sendWorldMailing(Admin admin, MailingSendForm form, Popups popups, Mailing mailing, Date sendDate) throws Exception {
        int companyID = admin.getCompanyID();
        int mailingID = mailing.getId();
        MailingType mailingType = mailing.getMailingType();

        if (mailingType != MailingType.FOLLOW_UP && maildropService.isActiveMailing(mailingID, companyID)) {
            return;
        }

        int numberOfRecipients = mailinglistDao.getNumberOfActiveWorldSubscribers(mailing.getTargetID(), companyID, mailing.getMailinglistID());
        checkIfPossibleToSendMailing(companyID, mailing, popups, numberOfRecipients);

        activateDeeptrackingIfPossible(mailingID, companyID);
        scheduleReport(form, admin, mailing, sendDate);
        updateMediatypeEmail(mailing, form.isCheckForDuplicateRecords(), form.isSkipWithEmptyTextContent());

        int generationStatus = MaildropGenerationStatus.NOW.getCode();
        Date generationDate = new Date();

        if (!needsInImmediateDelivery(sendDate)) {
            generationDate = detectGenerationDate(companyID, sendDate);
        }

        if (needsInImmediateDelivery(generationDate)) {
            if (mailingType == MailingType.NORMAL && isPrioritized(mailing)) {
                generationStatus = MaildropGenerationStatus.SCHEDULED.getCode();
            }
        } else {
            generationStatus = MaildropGenerationStatus.SCHEDULED.getCode();
            mailing.setSenddate(sendDate);
            mailingDao.updateStatus(mailing, MailingStatus.SCHEDULED);
        }

        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(generationDate);
        maildrop.setMailGenerationOptimization(getMaildropGenerationCode(form.getGenerationOptimization()));
        maildrop.setStatus(MaildropStatus.WORLD.getCode());
        maildrop.setMaxRecipients(Integer.parseInt(form.getMaxRecipients()));
        maildrop.setSendDate(sendDate);
        maildrop.setGenStatus(generationStatus);
        maildrop.setGenChangeDate(new Date());
        maildrop.setMailingID(mailingID);
        maildrop.setCompanyID(companyID);

        if (form.getGenerationOptimization() == MailGenerationOptimizationMode.NONE.getCode()
                && (mailingType == MailingType.NORMAL || mailingType == MailingType.FOLLOW_UP)) {
            Tuple<Integer, Integer> result = blockSizeService.calculateBlocksizeStepping(companyID, form.getStepping(), form.getBlocksize());

            maildrop.setStepping(result.getSecond());
            maildrop.setBlocksize(result.getFirst());
        }

        int maildropId = maildropStatusDao.saveMaildropEntry(maildrop);
        mailingDeliveryBlockingService.blockByAutoImport(mailingID, form.getAutoImportId(), maildropId);

        if (generationStatus == MaildropGenerationStatus.NOW.getCode()) {
            mailingTriggerService.triggerMailing(maildropId, mailingType);
            mailingDao.updateStatus(mailing, MailingStatus.SCHEDULED);
        }

        clearTestActionsData(mailingID, companyID);

        if (logger.isInfoEnabled()) {
            logger.info(String.format("send mailing id: %d type: %s", mailingID, MaildropStatus.WORLD.getCode()));
        }

        if (mailingType == MailingType.INTERVAL) {
            logWriter.writeIntervalMailingActivationLog(admin, mailing, sendDate, "send to world recipients");
        } else {
            logWriter.writeScheduleMailingLog(admin, mailing, sendDate);
        }
    }

    @Override
    public void activateDateBasedMailing(Admin admin, Mailing mailing, Popups popups, Date sendDate, int autoImportId) {
        int companyID = admin.getCompanyID();
        int mailingID = mailing.getId();

        if (maildropService.isActiveMailing(mailingID, companyID)) {
            return;
        }

        int numberOfRecipients = mailinglistDao.getNumberOfActiveWorldSubscribers(mailing.getTargetID(), companyID, mailing.getMailinglistID());
        checkIfPossibleToSendMailing(companyID, mailing, popups, numberOfRecipients);

        activateDeeptrackingIfPossible(mailingID, companyID);

        sendDate = truncateDateSecondsAndMilliseconds(sendDate);
        Date generationDate = new Date();

        if (!needsInImmediateDelivery(sendDate)) {
            generationDate = detectGenerationDate(companyID, sendDate);
        }

        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(generationDate);
        maildrop.setMailGenerationOptimization(MailGenerationOptimizationMode.NONE.getMaildropCode());
        maildrop.setStatus(MaildropStatus.DATE_BASED.getCode());
        maildrop.setSendDate(sendDate);
        maildrop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
        maildrop.setGenChangeDate(new Date());
        maildrop.setMailingID(mailingID);
        maildrop.setCompanyID(companyID);

        try {
            int maildropId = maildropStatusDao.saveMaildropEntry(maildrop);
            mailingDeliveryBlockingService.blockByAutoImport(mailingID, autoImportId, maildropId);

            clearTestActionsData(mailingID, companyID);

            if (logger.isInfoEnabled()) {
                logger.info(String.format("send mailing id: %d type: %s", mailingID, MaildropStatus.DATE_BASED.getCode()));
            }

            logWriter.writeDateBasedActivationLog(admin, mailing, sendDate);
        } catch (Exception e) {
            logger.error(String.format("Error occurred during activation of databased mailing! ID - %d", mailingID), e);
        }
    }

    @Override
    public void activateActionBasedMailing(Admin admin, Mailing mailing, Popups popups) {
        int companyID = admin.getCompanyID();
        int mailingID = mailing.getId();

        if (maildropService.isActiveMailing(mailingID, companyID)) {
            return;
        }

        int numberOfRecipients = mailinglistDao.getNumberOfActiveWorldSubscribers(mailing.getTargetID(), companyID, mailing.getMailinglistID());
        checkIfPossibleToSendMailing(companyID, mailing, popups, numberOfRecipients);

        activateDeeptrackingIfPossible(mailingID, companyID);

        Date now = new Date();
        MaildropEntry maildrop = new MaildropEntryImpl();

        maildrop.setGenDate(now);
        maildrop.setMailGenerationOptimization(MailGenerationOptimizationMode.NONE.getMaildropCode());
        maildrop.setStatus(MaildropStatus.ACTION_BASED.getCode());
        maildrop.setSendDate(now);
        maildrop.setGenStatus(MaildropGenerationStatus.NOW.getCode());
        maildrop.setGenChangeDate(now);
        maildrop.setMailingID(mailingID);
        maildrop.setCompanyID(companyID);

        try {
            maildropStatusDao.saveMaildropEntry(maildrop);
            clearTestActionsData(mailingID, companyID);

            if (logger.isInfoEnabled()) {
                logger.info(String.format("send mailing id: %d type: %s", mailingID, MaildropStatus.ACTION_BASED.getCode()));
            }

            logWriter.writeActionBasedActivationLog(admin, mailing);
        } catch (Exception e) {
            logger.error(String.format("Error occurred during activation of actionbased mailing! ID - %d", mailingID), e);
        }
    }

    private String getMaildropGenerationCode(int generationModeCode) {
        MailGenerationOptimizationMode mode = MailGenerationOptimizationMode.fromCode(generationModeCode);
        if (mode != null) {
            return mode.getMaildropCode();
        }

        return null;
    }

    private void updateMediatypeEmail(Mailing mailing, boolean checkForDuplicateRecords, boolean skipWithEmptyTextContent) {
        MediatypeEmail emailParam = mailing.getEmailParam();

        if (emailParam != null) {
            emailParam.setDoublechecking(checkForDuplicateRecords);
            emailParam.setSkipempty(skipWithEmptyTextContent);
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

    private boolean needsInImmediateDelivery(Date sendDate) {
        // Move "current time" into future, so we get a fairness period
        Date boundDate = DateUtilities.addMinutesToDate(new Date(), FAIRNESS_PERIOD_IN_MINUTES);
        return !boundDate.before(sendDate);
    }

    private Date detectGenerationDate(int companyId, Date sendDate) {
        Calendar sendCalendar = DateUtilities.calendar(sendDate);
        GregorianCalendar now = new GregorianCalendar();

        sendCalendar.add(Calendar.MINUTE, -mailingService.getMailGenerationMinutes(companyId));

        if (sendCalendar.before(now)) {
            return now.getTime();
        }

        return sendCalendar.getTime();
    }

    private Date truncateDateSecondsAndMilliseconds(Date date) {
        Calendar calendar = DateUtilities.calendar(date);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    protected void saveTestRecipientsAndSetForMaildrop(int companyId, int mailingListId, int maildropStatusId, List<String> addresses) throws Exception {
        if (CollectionUtils.isNotEmpty(addresses)) {
            List<Integer> customerIds = recipientDao.insertTestRecipients(companyId, mailingListId, UserStatus.Suspend, "Test recipient for delivery test", addresses);
            maildropService.selectTestRecipients(companyId, maildropStatusId, customerIds);
        }
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

    private void scheduleReport(MailingSendForm form, Admin admin, Mailing mailing, Date sendDate) throws Exception {
        if (form.isReportSendAfter24h()) {
            mailingReportScheduleService.scheduleNewReport(AgnUtils.splitAndTrimList(form.getReportSendEmail()), admin, mailing, sendDate, BirtReportType.TYPE_AFTER_MAILING_24HOURS);
        }

        if (form.isReportSendAfter48h()) {
            mailingReportScheduleService.scheduleNewReport(AgnUtils.splitAndTrimList(form.getReportSendEmail()), admin, mailing, sendDate, BirtReportType.TYPE_AFTER_MAILING_48HOURS);
        }

        if (form.isReportSendAfter1Week()) {
            mailingReportScheduleService.scheduleNewReport(AgnUtils.splitAndTrimList(form.getReportSendEmail()), admin, mailing, sendDate, BirtReportType.TYPE_AFTER_MAILING_WEEK);
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
}
