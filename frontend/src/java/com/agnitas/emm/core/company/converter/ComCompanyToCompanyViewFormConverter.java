/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.converter;

import java.util.Optional;

import org.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComCompany;
import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.dto.CompanySettingsDto;
import com.agnitas.emm.core.company.enums.LoginlockSettings;
import com.agnitas.emm.core.company.form.CompanyViewForm;

@Component
public class ComCompanyToCompanyViewFormConverter implements Converter<ComCompany, CompanyViewForm> {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComCompanyToCompanyViewFormConverter.class);

    private final ConfigService configService;

    public ComCompanyToCompanyViewFormConverter(final ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public CompanyViewForm convert(ComCompany comCompany) {
        CompanyViewForm companyViewForm = new CompanyViewForm();
        companyViewForm.setCompanyInfoDto(convertInfo(comCompany));
        companyViewForm.setCompanySettingsDto(convertSettings(comCompany));
        return companyViewForm;
    }

    private CompanyInfoDto convertInfo(ComCompany comCompany) {
        CompanyInfoDto companyInfoDto = new CompanyInfoDto();
        companyInfoDto.setId(comCompany.getId());
        companyInfoDto.setName(comCompany.getShortname());
        companyInfoDto.setDescription(comCompany.getDescription());
        return companyInfoDto;
    }

    private CompanySettingsDto convertSettings(ComCompany comCompany) {
        CompanySettingsDto settingsDto = new CompanySettingsDto();
        settingsDto.setHasMailTracking(BooleanUtils.toBoolean(comCompany.getMailtracking()));
        settingsDto.setStatisticsExpireDays(configService.getIntegerValue(ConfigValue.ExpireStatistics, comCompany.getId()));
        settingsDto.setRecipientExpireDays(configService.getIntegerValue(ConfigValue.ExpireRecipient, comCompany.getId()));
        settingsDto.setBusiness(comCompany.getBusiness());
        settingsDto.setHasActivatedAccessAuthorization(configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, comCompany.getId()));
        settingsDto.setHasExtendedSalutation(BooleanUtils.toBoolean(comCompany.getSalutationExtended()));
        settingsDto.setExecutiveAdministrator(comCompany.getStatAdmin());
        settingsDto.setTechnicalContacts(comCompany.getContactTech());
        settingsDto.setHasDataExportNotify(BooleanUtils.toBoolean(comCompany.getExportNotifyAdmin()));

        // todo: check is it necessary. the reason is: comCompany.getLocaleLanguage()
        String localeLanguage = configService.getValue(ConfigValue.LocaleLanguage, comCompany.getId());
        settingsDto.setLanguage(StringUtils.defaultIfEmpty(localeLanguage, "none"));

        // todo: check is it necessary. the reason is: comCompany.getLocaleTimezone()
        String localeTimezone = configService.getValue(ConfigValue.LocaleTimezone, comCompany.getId());
        settingsDto.setTimeZone(StringUtils.defaultIfEmpty(localeTimezone, "none"));

        // todo: check is it necessary. the reason is: comCompany.isForceSending()
        settingsDto.setHasForceSending(configService.getBooleanValue(ConfigValue.ForceSending, comCompany.getId()));

        settingsDto.setHasRecipientsCleanup(configService.getBooleanValue(ConfigValue.CleanRecipientsWithoutBinding, comCompany.getId()));
        settingsDto.setRecipientAnonymization(configService.getBooleanAsInteger(ConfigValue.CleanRecipientsData, comCompany.getId(), 30, -1));
        settingsDto.setRecipientCleanupTracking(configService.getIntegerValue(ConfigValue.CleanTrackingData, comCompany.getId()));
        settingsDto.setRecipientDeletion(configService.getIntegerValue(ConfigValue.DeleteRecipients, comCompany.getId()));
        settingsDto.setHasTrackingVeto(configService.getBooleanValue(ConfigValue.AnonymizeTrackingVetoRecipients, comCompany.getId()));
        settingsDto.setSector(comCompany.getSector());
        settingsDto.setBusiness(comCompany.getBusiness());
        settingsDto.setHasTwoFactorAuthentication(configService.getBooleanValue(ConfigValue.HostAuthentication, comCompany.getId()));
        settingsDto.setMaxAdminMails(configService.getIntegerValue(ConfigValue.MaxAdminMails, comCompany.getId()));
        
        // Settings for login tracking
        final Optional<LoginlockSettings> settingsOptional = LoginlockSettings.fromSettings(
        		configService.getIntegerValue(ConfigValue.LoginTracking.WebuiMaxFailedAttempts, comCompany.getId()),
        		configService.getIntegerValue(ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, comCompany.getId()) / 60);
        settingsDto.setLoginlockSettingsName(settingsOptional.isPresent() ? settingsOptional.get().getName() : "UNDEFINED");
        
        // Password policy
        settingsDto.setPasswordPolicyName(getPasswordPolicyName(comCompany.getId()));
        settingsDto.setPasswordExpireDays(this.configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, comCompany.getId()));
        
        // 2FA cookie
        settingsDto.setHostauthCookieExpireDays(this.configService.getIntegerValue(ConfigValue.HostAuthenticationHostIdCookieExpireDays, comCompany.getId()));
        
        return settingsDto;
    }

	private String getPasswordPolicyName(final int companyId) {
		final String policyName = configService.getValue(ConfigValue.PasswordPolicy, companyId);
		return PasswordPolicies.findByName(policyName).getPolicyName();
	}
}
