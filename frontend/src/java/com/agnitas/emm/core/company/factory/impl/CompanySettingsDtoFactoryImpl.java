/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.factory.impl;

import java.util.Objects;

import com.agnitas.emm.core.components.entity.TestRunOption;
import org.agnitas.emm.core.commons.anonymization.RecipientAnonymizationSettings;
import org.agnitas.emm.core.commons.password.PasswordExpireSettings;
import org.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.company.dto.CompanySettingsDto;
import com.agnitas.emm.core.company.enums.LoginlockSettings;
import com.agnitas.emm.core.company.factory.CompanySettingsDtoFactory;
import com.agnitas.emm.core.logon.common.HostAuthenticationCookieExpirationSettings;
import com.agnitas.emm.core.trackablelinks.common.LinkTrackingMode;

public class CompanySettingsDtoFactoryImpl implements CompanySettingsDtoFactory {

	private ConfigService configService;
	
	@Required
    public final void setConfigService(final ConfigService service) {
    	this.configService = Objects.requireNonNull(service, "Config Service cannot be null");
    }
	
    @Override
    public CompanySettingsDto getDefault() {
        CompanySettingsDto companySettingsDto = new CompanySettingsDto();

        companySettingsDto.setTimeZone("Europe/Berlin");
        companySettingsDto.setLanguage("de_DE");
        companySettingsDto.setHasTwoFactorAuthentication(true);
        companySettingsDto.setLoginlockSettingsName(LoginlockSettings.DEFAULT.getName());
        companySettingsDto.setPasswordPolicyName(PasswordPolicies.DEFAULT_POLICY.getPolicyName());
        companySettingsDto.setPasswordExpireDays(PasswordExpireSettings.DEFAULT.getExpireDays());
        companySettingsDto.setRecipientAnonymization(RecipientAnonymizationSettings.DEFAULT.getRecipientAnonymization());
        companySettingsDto.setHostauthCookieExpireDays(HostAuthenticationCookieExpirationSettings.DEFAULT.getExpireDays());
        companySettingsDto.setExpireRecipient(configService.getIntegerValue(ConfigValue.ExpireRecipient));
        companySettingsDto.setStatisticsExpireDays(configService.getIntegerValue(ConfigValue.ExpireStatisticsMax));
        companySettingsDto.setSendPasswordChangedNotification(AgnUtils.interpretAsBoolean(ConfigValue.SendPasswordChangedNotification.getDefaultValue()));
        companySettingsDto.setSendEncryptedMailings(AgnUtils.interpretAsBoolean(ConfigValue.SendEncryptedMailings.getDefaultValue()));

        companySettingsDto.setDefaultLinkExtension(ConfigValue.DefaultLinkExtension.getDefaultValue());
        companySettingsDto.setLinkcheckerLinktimeout(Integer.parseInt(ConfigValue.Linkchecker_Linktimeout.getDefaultValue()));
        companySettingsDto.setLinkcheckerThreadcount(Integer.parseInt(ConfigValue.Linkchecker_Threadcount.getDefaultValue()));
        companySettingsDto.setMailingUndoLimit(Integer.parseInt(ConfigValue.MailingUndoLimit.getDefaultValue()));
        companySettingsDto.setPrefillCheckboxSendDuplicateCheck(AgnUtils.interpretAsBoolean(ConfigValue.PrefillCheckboxSendDuplicateCheck.getDefaultValue()));
        companySettingsDto.setFullviewFormName(ConfigValue.FullviewFormName.getDefaultValue());

        companySettingsDto.setTrackingVetoAllowTransactionTracking(AgnUtils.interpretAsBoolean(ConfigValue.TrackingVetoAllowTransactionTracking.getDefaultValue()));
        companySettingsDto.setDeleteSuccessfullyImportedFiles(AgnUtils.interpretAsBoolean(ConfigValue.DeleteSuccessfullyImportedFiles.getDefaultValue()));
        companySettingsDto.setEnableAltgExtended(AgnUtils.interpretAsBoolean(ConfigValue.TargetAccessLimitExtended.getDefaultValue()));
        companySettingsDto.setImportAlwaysInformEmail(ConfigValue.ImportAlwaysInformEmail.getDefaultValue());
        companySettingsDto.setExportAlwaysInformEmail(ConfigValue.ExportAlwaysInformEmail.getDefaultValue());
        companySettingsDto.setBccEmail(ConfigValue.DefaultBccEmail.getDefaultValue());
        companySettingsDto.setAnonymizeAllRecipients(AgnUtils.interpretAsBoolean(ConfigValue.AnonymizeAllRecipients.getDefaultValue()));
        companySettingsDto.setCleanRecipientsWithoutBinding(AgnUtils.interpretAsBoolean(ConfigValue.CleanRecipientsWithoutBinding.getDefaultValue()));
        companySettingsDto.setRecipientCleanupTracking(Integer.parseInt(ConfigValue.CleanTrackingData.getDefaultValue()));
        companySettingsDto.setRecipientDeletion(Integer.parseInt(ConfigValue.DeleteRecipients.getDefaultValue()));
        companySettingsDto.setRecipientEmailInUseWarning(AgnUtils.interpretAsBoolean(ConfigValue.RecipientEmailInUseWarning.getDefaultValue()));
        companySettingsDto.setAllowEmailWithWhitespace(AgnUtils.interpretAsBoolean(ConfigValue.AllowEmailWithWhitespace.getDefaultValue()));
        companySettingsDto.setAllowEmptyEmail(AgnUtils.interpretAsBoolean(ConfigValue.AllowEmptyEmail.getDefaultValue()));
        companySettingsDto.setExpireStatistics(Integer.parseInt(ConfigValue.ExpireStatistics.getDefaultValue()));
        companySettingsDto.setExpireSuccess(Integer.parseInt(ConfigValue.ExpireSuccess.getDefaultValue()));
        companySettingsDto.setExpireRecipient(Integer.parseInt(ConfigValue.ExpireRecipient.getDefaultValue()));
        companySettingsDto.setExpireBounce(Integer.parseInt(ConfigValue.ExpireBounce.getDefaultValue()));
        companySettingsDto.setExpireUpload(Integer.parseInt(ConfigValue.ExpireUpload.getDefaultValue()));
        companySettingsDto.setWriteCustomerOpenOrClickField(AgnUtils.interpretAsBoolean(ConfigValue.WriteCustomerOpenOrClickField.getDefaultValue()));
        companySettingsDto.setDefaultCompanyLinkTrackingMode(LinkTrackingMode.getDefault().getMode());
        companySettingsDto.setDefaultBlockSize(Integer.parseInt(ConfigValue.DefaultBlocksizeValue.getDefaultValue()));
        companySettingsDto.setDefaultTestRunOption(TestRunOption.fromId(Integer.parseInt(ConfigValue.DefaultTestRunOption.getDefaultValue())));
        companySettingsDto.setUserBasedFavoriteTargets(AgnUtils.interpretAsBoolean(ConfigValue.UserBasedFavoriteTargets.getDefaultValue()));
        companySettingsDto.setFilterRecipientsOverviewForActiveRecipients(AgnUtils.interpretAsBoolean(ConfigValue.FilterRecipientsOverviewForActiveRecipients.getDefaultValue()));
        companySettingsDto.setNormalizeEmails(!AgnUtils.interpretAsBoolean(ConfigValue.AllowUnnormalizedEmails.getDefaultValue()));

        int maxFieldsByLicense = configService.getIntegerValue(ConfigValue.MaxFields);
        int maxFieldsByDefault = Integer.parseInt(ConfigValue.MaxFields.getDefaultValue());
        if (maxFieldsByLicense == -1) {
        	companySettingsDto.setMaxFields(maxFieldsByDefault);
        } else {
        	companySettingsDto.setMaxFields(maxFieldsByLicense);
        }

        return companySettingsDto;
    }
}
