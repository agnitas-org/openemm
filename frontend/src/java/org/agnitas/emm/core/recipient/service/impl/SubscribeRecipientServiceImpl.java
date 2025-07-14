/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.impl;

import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.util.Date;
import java.util.Optional;

import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.mailing.service.MailgunOptions;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingException;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.emm.core.widget.beans.SubscribeWidgetSettings;
import com.agnitas.emm.core.widget.form.SubscribeWidgetForm;
import com.agnitas.emm.mobilephone.MobilephoneNumber;
import com.agnitas.emm.mobilephone.service.MobilephoneNumberWhitelist;
import com.agnitas.messages.Message;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.DatasourceDescription;
import com.agnitas.beans.Recipient;
import com.agnitas.beans.factory.BindingEntryFactory;
import com.agnitas.beans.factory.RecipientFactory;
import com.agnitas.beans.impl.DatasourceDescriptionImpl;
import com.agnitas.emm.core.datasource.enums.SourceGroupType;
import com.agnitas.emm.common.UserStatus;
import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.recipient.service.SubscribeRecipientService;
import org.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import org.agnitas.emm.core.recipient.service.SubscriberLimitExceededException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class SubscribeRecipientServiceImpl implements SubscribeRecipientService {

    private static final Logger logger = LogManager.getLogger(SubscribeRecipientServiceImpl.class);

    private final RecipientFactory recipientFactory;
    private final CompanyTokenService companyTokenService;
    private final ConfigService configService;
    private final RecipientService recipientService;
    private final RecipientFieldService recipientFieldService;
    private final MobilephoneNumberWhitelist mobilephoneNumberWhitelist;
    private final BlacklistService blacklistService;
    private final SubscriberLimitCheck subscriberLimitCheck;
    private final BindingEntryFactory bindingEntryFactory;
    private final SendActionbasedMailingService sendActionbasedMailingService;
    private final DataSourceService dataSourceService;

    public SubscribeRecipientServiceImpl(RecipientFactory recipientFactory, CompanyTokenService companyTokenService, ConfigService configService,
                                         RecipientService recipientService, RecipientFieldService recipientFieldService,
                                         MobilephoneNumberWhitelist mobilephoneNumberWhitelist, BlacklistService blacklistService,
                                         SubscriberLimitCheck subscriberLimitCheck, BindingEntryFactory bindingEntryFactory,
                                         SendActionbasedMailingService sendActionbasedMailingService, DataSourceService dataSourceService) {
        this.recipientFactory = recipientFactory;
        this.companyTokenService = companyTokenService;
        this.configService = configService;
        this.recipientService = recipientService;
        this.recipientFieldService = recipientFieldService;
        this.mobilephoneNumberWhitelist = mobilephoneNumberWhitelist;
        this.blacklistService = blacklistService;
        this.subscriberLimitCheck = subscriberLimitCheck;
        this.bindingEntryFactory = bindingEntryFactory;
        this.sendActionbasedMailingService = sendActionbasedMailingService;
        this.dataSourceService = dataSourceService;
    }

    @Override
    public SimpleServiceResult subscribe(SubscribeWidgetForm form, SubscribeWidgetSettings settings) {
        Integer companyId = companyTokenService.findCompanyIdToken(settings.getCompanyToken());
        if (companyId == null) {
            return SimpleServiceResult.simpleError(Message.of(ERROR_MSG));
        }

        SimpleServiceResult validationResult = validateBeforeSave(form, companyId);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        try {
            Optional<Recipient> recipient = saveRecipient(form, settings, companyId);
            if (recipient.isEmpty()) {
                return SimpleServiceResult.simpleError();
            }

            sendDoiMailing(recipient.get(), settings.getDoiMailingId(), companyId);
            return SimpleServiceResult.simpleSuccess();
        } catch (SendActionbasedMailingException sme) {
            logger.error("Error occurred when send DOI mailing: %d".formatted(settings.getDoiMailingId()), sme);
            return SimpleServiceResult.simpleWarning();
        } catch (Exception e) {
            logger.error("Error occurred when save new recipient!", e);
            return SimpleServiceResult.simpleError(Message.exact("Error occurred when save new recipient!"));
        }
    }

    private void createBinding(int recipientId, SubscribeWidgetSettings settings, int companyId) {
        BindingEntry entry = bindingEntryFactory.newBindingEntry();

        entry.setCustomerID(recipientId);
        entry.setMediaType(MediaTypes.EMAIL.getMediaCode());
        entry.setMailinglistID(settings.getMailinglistId());
        entry.setUserType(BindingEntry.UserType.World.getTypeCode());
        entry.setUserStatus(UserStatus.WaitForConfirm.getStatusCode());
        entry.setUserRemark("Opt-In-IP: " + settings.getRemoteAddress());
        entry.setReferrer(settings.getReferrer());

        entry.insertNewBindingInDB(companyId);
    }

    private SimpleServiceResult validateBeforeSave(SubscribeWidgetForm form, int companyId) {
        ServiceResult<String> phoneNumberCheckResult = checkAndNormalizeMobilePhoneNumber(form.getSmsNumber(), companyId);
        if (!phoneNumberCheckResult.isSuccess()) {
            return SimpleServiceResult.of(phoneNumberCheckResult);
        } else {
            form.setSmsNumber(phoneNumberCheckResult.getResult());
        }

        if (blacklistService.blacklistCheck(form.getEmail(), companyId)) {
            return SimpleServiceResult.simpleError(Message.exact("E-Mail address blacklisted"));
        }

        if (recipientService.existsWithEmail(form.getEmail(), companyId)) {
            return SimpleServiceResult.simpleError(Message.exact("Recipient with such email already exists!"));
        }

        try {
            subscriberLimitCheck.checkSubscriberLimit(companyId);
        } catch (SubscriberLimitExceededException subscriberLimitExceededException) {
            return SimpleServiceResult.simpleError(Message.exact("Subscriber limit exceeded!"));
        }

        return SimpleServiceResult.simpleSuccess();
    }

    private ServiceResult<String> checkAndNormalizeMobilePhoneNumber(String phoneNumber, int companyId) {
        if (StringUtils.isBlank(phoneNumber)) {
            return ServiceResult.success(phoneNumber);
        }

        try {
            final MobilephoneNumber number = new MobilephoneNumber(phoneNumber.trim());

            // If parsing was successful, check that number is allowed
            if (mobilephoneNumberWhitelist.isWhitelisted(number, companyId)) {
                return ServiceResult.success(number.toString());
            }

            return ServiceResult.error(Message.exact("Phone number not allowed!"));
        } catch (NumberFormatException e) {
            return ServiceResult.error(Message.exact("Phone number with incorrect format!"));
        }
    }

    private Optional<Recipient> saveRecipient(SubscribeWidgetForm form, SubscribeWidgetSettings settings, int companyId) throws Exception {
        Recipient recipient = prepareRecipient(form, companyId);

        if (!recipientService.updateRecipientInDB(recipient)) {
            return Optional.empty();
        }

        createBinding(recipient.getCustomerID(), settings, companyId);
        return Optional.of(recipient);
    }

    private Recipient prepareRecipient(SubscribeWidgetForm form, int companyId) {
        Recipient recipient = recipientFactory.newRecipient();
        recipient.setCompanyID(companyId);

        if (configService.getBooleanValue(ConfigValue.UseRecipientFieldService, companyId)) {
            recipient.setCustDBStructure(recipientFieldService.getRecipientDBStructure(companyId));
        } else {
            recipient.setCustDBStructure(recipientService.getRecipientDBStructure(companyId));
        }

        boolean doNotTrack = configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, companyId) || form.isTrackingVeto();
        int datasourceID = getDatasourceID(companyId);

        recipient.setCustParameters(RecipientStandardField.Gender.getColumnName(), String.valueOf(form.getGender().getStorageValue()));
        recipient.setCustParameters(RecipientStandardField.Firstname.getColumnName(), form.getFirstName().trim());
        recipient.setCustParameters(RecipientStandardField.Lastname.getColumnName(), form.getLastName().trim());
        recipient.setCustParameters(RecipientStandardField.Mailtype.getColumnName(), String.valueOf(form.getMailType().getIntValue()));
        recipient.setCustParameters(RecipientStandardField.Email.getColumnName(), form.getEmail().trim().toLowerCase());
        recipient.setCustParameters(RecipientStandardField.DoNotTrack.getColumnName(), doNotTrack ? "1" : "0");
        recipient.setCustParameters("SMSNUMBER", form.getSmsNumber());
        recipient.setCustParameters(RecipientStandardField.DatasourceID.getColumnName(), String.valueOf(datasourceID));
        recipient.setCustParameters(RecipientStandardField.LatestDatasourceID.getColumnName(), String.valueOf(datasourceID));

        return recipient;
    }

    private int getDatasourceID(int companyID) {
        String description = "Widget: subscribe";

        DatasourceDescription existingDataSource = dataSourceService.getByDescription(SourceGroupType.AutoinsertForms, description, companyID);
        if (existingDataSource != null) {
            return existingDataSource.getId();
        }

        DatasourceDescriptionImpl datasourceDescription = new DatasourceDescriptionImpl();

        datasourceDescription.setId(0);
        datasourceDescription.setCompanyID(companyID);
        datasourceDescription.setSourceGroupType(SourceGroupType.AutoinsertForms);
        datasourceDescription.setCreationDate(new Date());
        datasourceDescription.setDescription(description);
        datasourceDescription.setDescription2("SubscribeRecipientServiceImpl");
        return dataSourceService.save(datasourceDescription);
    }

    private void sendDoiMailing(Recipient recipient, int mailingId, int companyId) throws SendActionbasedMailingException {
        final MailgunOptions mailgunOptions = new MailgunOptions();
        mailgunOptions.withAllowedUserStatus(UserStatus.WaitForConfirm);

        sendActionbasedMailingService.sendActionbasedMailing(
                companyId,
                mailingId,
                recipient.getCustomerID(),
                0,
                mailgunOptions
        );
    }

}
