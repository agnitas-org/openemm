/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.ComMailing;
import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.MailingSendOptions;
import com.agnitas.beans.MediatypeEmail;
import com.agnitas.beans.impl.ComMailingImpl;
import com.agnitas.beans.impl.MaildropEntryImpl;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTrackableLinkDao;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPriorityService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.messages.Message;
import com.agnitas.service.ComMailingSendService;
import com.agnitas.util.ClassicTemplateGenerator;
import org.agnitas.beans.Mailing;
import org.agnitas.beans.MailingComponent;
import org.agnitas.beans.Mailinglist;
import org.agnitas.dao.MaildropStatusDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.OnepixelDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.GuiConstants;
import org.agnitas.util.Tuple;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Required;

public class ComMailingSendServiceImpl implements ComMailingSendService {
    /** The logger. */
    private static final transient Logger logger = Logger.getLogger(ComMailingSendServiceImpl.class);

    private ComMailingDao mailingDao;
    private ComCompanyDao companyDao;
    private MailinglistDao mailinglistDao;
    private ClassicTemplateGenerator classicTemplateGenerator;
    private ComTrackableLinkDao trackableLinkDao;
    private MaildropStatusDao maildropStatusDao;
    private OnepixelDao onepixelDao;
    private MaildropService maildropService;
    private MailingService mailingService;
    private ComMailingBaseService mailingBaseService;
    private MailingPriorityService mailingPriorityService;
    private ConfigService configService;

    @Override
    public void sendMailing(int mailingId, @VelocityCheck int companyId, MailingSendOptions options, ActionMessages messages, ActionErrors errors, List<UserAction> userActions) {
    	if (companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
    		errors.add("global", new ActionMessage("error.company.mailings.sent.forbidden"));
    		return;
    	}

        ComMailing mailing = (ComMailing) mailingDao.getMailing(mailingId, companyId);
    
        MailingTypes mailingType = MailingTypes.getByCode(mailing.getMailingType());
        if (mailing.getId() == 0) {
            logger.error("Mailing #" + mailingId + " (companyId #" + companyId + ") not found so cannot be sent");
            return;
        }

        MaildropEntry drop = new MaildropEntryImpl();
        boolean world = false;
        boolean admin = false;
        boolean test = false;

        switch (options.getDeliveryType()) {
        case WORLD:
            world = true;
            switch (mailingType) {
            case NORMAL:
                case FOLLOW_UP:
                drop.setStatus(MaildropStatus.WORLD.getCode());
                admin = true;
                test = true;
                break;

                case DATE_BASED:
                drop.setStatus(MaildropStatus.DATE_BASED.getCode());
                break;

                case ACTION_BASED:
                drop.setStatus(MaildropStatus.ACTION_BASED.getCode());
                break;
            }
            break;

        case TEST:
            drop.setStatus(MaildropStatus.TEST.getCode());
            admin = true;
            test = true;
            break;

        case ADMIN:
            drop.setStatus(MaildropStatus.ADMIN.getCode());
            admin = true;
            break;
        }

        if (drop.getStatus() == MaildropStatus.WORLD.getCode() || drop.getStatus() == MaildropStatus.DATE_BASED.getCode() ||
            drop.getStatus() == MaildropStatus.ACTION_BASED.getCode()) {
            if (companyDao.checkDeeptrackingAutoActivate(companyId)) {
                trackableLinkDao.activateDeeptracking(companyId, mailingId);
            }
        }

        Mailinglist aList = mailinglistDao.getMailinglist(mailing.getMailinglistID(), companyId);
        int maxAdminMails = companyDao.getMaxAdminMails(companyId);

        int blocksize = options.getBlockSize();
        int stepping = 0;
        
        if (configService.getBooleanValue(ConfigValue.ForceSteppingBlocksize, companyId)) {
        	blocksize = configService.getIntegerValue(ConfigValue.DefaultBlocksizeValue, companyId);
		}

        if (mailingType == MailingTypes.NORMAL || mailingType == MailingTypes.FOLLOW_UP) {
            Tuple<Integer, Integer> blocksizeAndStepping = AgnUtils.makeBlocksizeAndSteppingFromBlocksize(blocksize, options.getDefaultStepping());
            blocksize = blocksizeAndStepping.getFirst();
            stepping = blocksizeAndStepping.getSecond();
        } else {
            blocksize = 0;
        }

        int activeRecipientsCount = mailinglistDao.getNumberOfActiveSubscribers(admin, test, world, mailing.getTargetID(), aList.getCompanyID(), aList.getId());
        if (activeRecipientsCount == 0) {
            messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.mailing.no_subscribers"));
        } else if (!world && activeRecipientsCount > maxAdminMails) {
            errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.send.admin.maxMails", maxAdminMails));
            return;
        }

        if (mailingDao.hasEmail(mailing.getId())) {
            MediatypeEmail param = mailing.getEmailParam();

            // Check the text version of mailing.
            if (isContentBlank(mailing, mailing.getTextTemplate())) {
                if (mailingService.isTextVersionRequired(companyId, mailingId)) {
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.no_text_version"));
                    return;
                } else {
                    messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, new ActionMessage("error.mailing.no_text_version"));
                }
            }

            // Check the HTML version unless mail format is "only text".
            if (param.getMailFormat() >= ComMailing.INPUT_TYPE_HTML) {
                if (isContentBlank(mailing, mailing.getHtmlTemplate())) {
                    errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.no_html_version"));
                    return;
                }
            }

            if (StringUtils.isBlank(param.getSubject())) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.subject.too_short"));
                return;
            }

            String senderAddress = null;
            try {
                senderAddress = param.getFromAdr();
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }

            if (StringUtils.isBlank(senderAddress)) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.sender_adress"));
                return;
            }
        }

        Date sendDate = options.getDate();
        Date genDate;

        drop.setSendDate(sendDate);

        if (options.isGenerateAtSendDate()) {
            genDate = new Date(sendDate.getTime());
        } else {
            Date now = new Date();

            if (DateUtil.isSendDateForImmediateDelivery(sendDate)) {
                genDate = now;
            } else {
                genDate = DateUtils.addMinutes(sendDate, -mailingService.getMailGenerationMinutes(companyId));
                if (DateUtilities.isPast(genDate)) {
                    genDate = now;
                }
            }
        }

        MediatypeEmail emailParam = mailing.getEmailParam();

        if (emailParam != null) {
            if (options.getFollowupFor() > 0) {
                emailParam.setFollowupFor(Integer.toString(options.getFollowupFor()));
            }
            emailParam.setDoublechecking(options.isDoubleChecking());
            emailParam.setSkipempty(options.isSkipEmpty());
            mailingDao.saveMailing(mailing, false);
        }

        if (mailing.getMailingType() != Mailing.TYPE_FOLLOWUP && world && this.maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Mailing id: " + mailing.getId() + " is already scheduled");
            }
            return;
        }

        int startGen = MaildropEntry.GEN_NOW;

        switch (MailingTypes.getByCode(mailing.getMailingType())) {
            case NORMAL:
                if (world && DateUtil.isDateForImmediateGeneration(genDate) && isPrioritized(companyId, mailingId)) {
                    startGen = MaildropEntry.GEN_SCHEDULED;
                }
                break;

            case DATE_BASED:
                if (test) {
                    // Set genstatus equals 0 to trigger WM-specific test sending mode of backend for date-based mailings.
                    startGen = MaildropEntry.GEN_SCHEDULED;
                }
                break;
        }

        if (!DateUtil.isDateForImmediateGeneration(genDate)) {
            switch (MailingTypes.getByCode(mailing.getMailingType())) {
                case NORMAL:
                case FOLLOW_UP:
                    startGen = MaildropEntry.GEN_SCHEDULED;
                    updateStatusByMaildrop(mailingId, drop);
                    break;
            }
        }

        drop.setGenStatus(startGen);
        drop.setGenDate(genDate);
        drop.setGenChangeDate(new Date());
        drop.setMailingID(mailing.getId());
        drop.setCompanyID(mailing.getCompanyID());
        drop.setStepping(stepping);
        drop.setBlocksize(blocksize);
        drop.setMaxRecipients(options.getMaxRecipients());

        // Remove an existing maildrop entries
        maildropStatusDao.cleanup(mailing.getMaildropStatus());
        mailing.getMaildropStatus().add(drop);
        mailingDao.saveMailing(mailing, false);

        if (startGen == MaildropEntry.GEN_NOW && drop.getStatus() != MaildropStatus.ACTION_BASED.getCode() && drop.getStatus() != MaildropStatus.DATE_BASED.getCode()) {
            classicTemplateGenerator.generate(mailingId, options.getAdminId(), companyId, true, true);
            ((ComMailingImpl)mailing).triggerMailing(drop.getId());
            updateStatusByMaildrop(mailingId, drop);
        }

        onepixelDao.deleteAdminAndTestOpenings(mailing.getId(), mailing.getCompanyID());
        trackableLinkDao.deleteAdminAndTestClicks(mailing.getId(), mailing.getCompanyID());
        mailingDao.cleanTestDataInSuccessTbl(mailing.getId(), mailing.getCompanyID());
        mailingDao.cleanTestDataInMailtrackTbl(mailing.getId(), mailing.getCompanyID());

        if (logger.isInfoEnabled()) {
            logger.info("Send mailing id: " + mailing.getId() + " type: " + drop.getStatus());
        }

        userActions.add(new UserAction("send mailing", String.format("mailing: %s (%d) type: %c", mailing.getShortname(), mailing.getId(), drop.getStatus())));
    }

    @Override
    public void sendMailing(int mailingId, @VelocityCheck int companyId, MailingSendOptions options, List<Message> warnings, List<Message> messages, List<UserAction> userActions) {
        if (companyId == 1 && !configService.getBooleanValue(ConfigValue.System_License_AllowMailingSendForMasterCompany)) {
            messages.add(Message.of("global", new ActionMessage("error.company.mailings.sent.forbidden")));
            return;
        }

        ComMailing mailing = (ComMailing) mailingDao.getMailing(mailingId, companyId);

        MailingTypes mailingType = MailingTypes.getByCode(mailing.getMailingType());
        if (mailing.getId() == 0) {
            logger.error("Mailing #" + mailingId + " (companyId #" + companyId + ") not found so cannot be sent");
            return;
        }

        MaildropEntry drop = new MaildropEntryImpl();
        boolean world = false;
        boolean admin = false;
        boolean test = false;

        switch (options.getDeliveryType()) {
            case WORLD:
                world = true;
                switch (mailingType) {
                    case NORMAL:
                    case FOLLOW_UP:
                        drop.setStatus(MaildropStatus.WORLD.getCode());
                        admin = true;
                        test = true;
                        break;

                    case DATE_BASED:
                        drop.setStatus(MaildropStatus.DATE_BASED.getCode());
                        break;

                    case ACTION_BASED:
                        drop.setStatus(MaildropStatus.ACTION_BASED.getCode());
                        break;
                }
                break;

            case TEST:
                drop.setStatus(MaildropStatus.TEST.getCode());
                admin = true;
                test = true;
                break;

            case ADMIN:
                drop.setStatus(MaildropStatus.ADMIN.getCode());
                admin = true;
                break;
        }

        if (drop.getStatus() == MaildropStatus.WORLD.getCode() || drop.getStatus() == MaildropStatus.DATE_BASED.getCode() ||
                drop.getStatus() == MaildropStatus.ACTION_BASED.getCode()) {
            if (companyDao.checkDeeptrackingAutoActivate(companyId)) {
                trackableLinkDao.activateDeeptracking(companyId, mailingId);
            }
        }

        Mailinglist aList = mailinglistDao.getMailinglist(mailing.getMailinglistID(), companyId);
        int maxAdminMails = companyDao.getMaxAdminMails(companyId);

        int blocksize = options.getBlockSize();
        int stepping = 0;

        if (configService.getBooleanValue(ConfigValue.ForceSteppingBlocksize, companyId)) {
            blocksize = configService.getIntegerValue(ConfigValue.DefaultBlocksizeValue, companyId);
        }

        if (mailingType == MailingTypes.NORMAL || mailingType == MailingTypes.FOLLOW_UP) {
            Tuple<Integer, Integer> blocksizeAndStepping = AgnUtils.makeBlocksizeAndSteppingFromBlocksize(blocksize, options.getDefaultStepping());
            blocksize = blocksizeAndStepping.getFirst();
            stepping = blocksizeAndStepping.getSecond();
        } else {
            blocksize = 0;
        }

        int activeRecipientsCount = mailinglistDao.getNumberOfActiveSubscribers(admin, test, world, mailing.getTargetID(), aList.getCompanyID(), aList.getId());
        if (activeRecipientsCount == 0) {
            warnings.add(Message.of("error.mailing.no_subscribers"));
        } else if (!world && activeRecipientsCount > maxAdminMails) {
            messages.add(Message.of(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.send.admin.maxMails", maxAdminMails)));
            return;
        }

        if (mailingDao.hasEmail(mailing.getId())) {
            MediatypeEmail param = mailing.getEmailParam();

            // Check the text version of mailing.
            if (isContentBlank(mailing, mailing.getTextTemplate())) {
                if (mailingService.isTextVersionRequired(companyId, mailingId)) {
                    messages.add(Message.of(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.no_text_version")));
                    return;
                } else {
                    warnings.add(Message.of("error.mailing.no_text_version"));
                }
            }

            // Check the HTML version unless mail format is "only text".
            if (param.getMailFormat() >= ComMailing.INPUT_TYPE_HTML) {
                if (isContentBlank(mailing, mailing.getHtmlTemplate())) {
                    messages.add(Message.of(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.no_html_version")));
                    return;
                }
            }

            if (StringUtils.isBlank(param.getSubject())) {
                messages.add(Message.of(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.subject.too_short")));
                return;
            }

            String senderAddress = null;
            try {
                senderAddress = param.getFromAdr();
            } catch (Exception e) {
                logger.error("Error occurred: " + e.getMessage(), e);
            }

            if (StringUtils.isBlank(senderAddress)) {
                messages.add(Message.of(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.mailing.sender_adress")));
                return;
            }
        }

        Date sendDate = options.getDate();
        Date genDate;

        drop.setSendDate(sendDate);

        if (options.isGenerateAtSendDate()) {
            genDate = new Date(sendDate.getTime());
        } else {
            Date now = new Date();

            if (DateUtil.isSendDateForImmediateDelivery(sendDate)) {
                genDate = now;
            } else {
                genDate = DateUtils.addMinutes(sendDate, -mailingService.getMailGenerationMinutes(companyId));
                if (DateUtilities.isPast(genDate)) {
                    genDate = now;
                }
            }
        }

        MediatypeEmail emailParam = mailing.getEmailParam();

        if (emailParam != null) {
            if (options.getFollowupFor() > 0) {
                emailParam.setFollowupFor(Integer.toString(options.getFollowupFor()));
            }
            emailParam.setDoublechecking(options.isDoubleChecking());
            emailParam.setSkipempty(options.isSkipEmpty());
            mailingDao.saveMailing(mailing, false);
        }

        if (mailing.getMailingType() != Mailing.TYPE_FOLLOWUP && world && this.maildropService.isActiveMailing(mailing.getId(), mailing.getCompanyID())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Mailing id: " + mailing.getId() + " is already scheduled");
            }
            return;
        }

        int startGen = MaildropEntry.GEN_NOW;

        switch (MailingTypes.getByCode(mailing.getMailingType())) {
            case NORMAL:
                if (world && DateUtil.isDateForImmediateGeneration(genDate) && isPrioritized(companyId, mailingId)) {
                    startGen = MaildropEntry.GEN_SCHEDULED;
                }
                break;

            case DATE_BASED:
                if (test) {
                    // Set genstatus equals 0 to trigger WM-specific test sending mode of backend for date-based mailings.
                    startGen = MaildropEntry.GEN_SCHEDULED;
                }
                break;
        }

        if (!DateUtil.isDateForImmediateGeneration(genDate)) {
            switch (MailingTypes.getByCode(mailing.getMailingType())) {
                case NORMAL:
                case FOLLOW_UP:
                    startGen = MaildropEntry.GEN_SCHEDULED;
                    updateStatusByMaildrop(mailingId, drop);
                    break;
            }
        }

        drop.setGenStatus(startGen);
        drop.setGenDate(genDate);
        drop.setGenChangeDate(new Date());
        drop.setMailingID(mailing.getId());
        drop.setCompanyID(mailing.getCompanyID());
        drop.setStepping(stepping);
        drop.setBlocksize(blocksize);
        drop.setMaxRecipients(options.getMaxRecipients());

        // Remove an existing maildrop entries
        maildropStatusDao.cleanup(mailing.getMaildropStatus());
        mailing.getMaildropStatus().add(drop);
        mailingDao.saveMailing(mailing, false);

        if (startGen == MaildropEntry.GEN_NOW && drop.getStatus() != MaildropStatus.ACTION_BASED.getCode() && drop.getStatus() != MaildropStatus.DATE_BASED.getCode()) {
            classicTemplateGenerator.generate(mailingId, options.getAdminId(), companyId, true, true);
            ((ComMailingImpl)mailing).triggerMailing(drop.getId());
            updateStatusByMaildrop(mailingId, drop);
        }

        onepixelDao.deleteAdminAndTestOpenings(mailing.getId(), mailing.getCompanyID());
        trackableLinkDao.deleteAdminAndTestClicks(mailing.getId(), mailing.getCompanyID());
        mailingDao.cleanTestDataInSuccessTbl(mailing.getId(), mailing.getCompanyID());
        mailingDao.cleanTestDataInMailtrackTbl(mailing.getId(), mailing.getCompanyID());

        if (logger.isInfoEnabled()) {
            logger.info("Send mailing id: " + mailing.getId() + " type: " + drop.getStatus());
        }

        userActions.add(new UserAction("send mailing", String.format("mailing: %s (%d) type: %c", mailing.getShortname(), mailing.getId(), drop.getStatus())));
    }

    private boolean isContentBlank(Mailing mailing, MailingComponent template) {
        return mailingBaseService.isContentBlank(template.getEmmBlock(), mailing.getDynTags());
    }

    private boolean isPrioritized(@VelocityCheck int companyId, int mailingId) {
        if (mailingPriorityService != null) {
            return mailingPriorityService.getMailingPriority(companyId, mailingId) > 0;
        }

        return false;
    }

    private void updateStatusByMaildrop(int mailingID, MaildropEntry drop) {
        String status = "scheduled";
        if (drop.getStatus() == MaildropStatus.TEST.getCode()) {
            status = "test";
        } else if (drop.getStatus() == MaildropStatus.ADMIN.getCode()) {
            status = "admin";
        }
        mailingDao.updateStatus(mailingID, status);
    }

    @Override
    public void deactivateMailing(int mailingId, int companyId) {
        Mailing mailing = mailingDao.getMailing(mailingId, companyId);
        maildropStatusDao.cleanup(mailing.getMaildropStatus());
    }

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setCompanyDao(ComCompanyDao companyDao) {
        this.companyDao = companyDao;
    }

    @Required
    public void setMailinglistDao(MailinglistDao mailinglistDao) {
        this.mailinglistDao = mailinglistDao;
    }

    @Required
    public void setClassicTemplateGenerator(ClassicTemplateGenerator classicTemplateGenerator) {
        this.classicTemplateGenerator = classicTemplateGenerator;
    }

    @Required
    public void setMaildropStatusDao(MaildropStatusDao maildropStatusDao) {
        this.maildropStatusDao = maildropStatusDao;
    }

    @Required
    public void setTrackableLinkDao(ComTrackableLinkDao trackableLinkDao) {
        this.trackableLinkDao = trackableLinkDao;
    }

    @Required
    public void setOnepixelDao(OnepixelDao onepixelDao) {
        this.onepixelDao = onepixelDao;
    }

    @Required
    public final void setMaildropService(final MaildropService service) {
        this.maildropService = service;
    }

    @Required
    public void setMailingService(MailingService mailingService) {
        this.mailingService = mailingService;
    }

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }

    public void setMailingPriorityService(MailingPriorityService mailingPriorityService) {
        this.mailingPriorityService = mailingPriorityService;
    }

    @Required
    public void setConfigService(final ConfigService service) {
        this.configService = service;
    }
}
