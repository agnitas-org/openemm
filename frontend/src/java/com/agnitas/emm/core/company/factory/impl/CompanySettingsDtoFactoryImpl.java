/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.factory.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.anonymization.RecipientAnonymizationSettings;
import org.agnitas.emm.core.commons.anonymization.RecipientCleanupTracking;
import org.agnitas.emm.core.commons.anonymization.RecipientDeletion;
import org.agnitas.emm.core.commons.password.PasswordExpireSettings;
import org.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.company.dto.CompanySettingsDto;
import com.agnitas.emm.core.company.enums.LoginlockSettings;
import com.agnitas.emm.core.company.factory.CompanySettingsDtoFactory;
import com.agnitas.emm.core.logon.common.HostAuthenticationCookieExpirationSettings;

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
        companySettingsDto.setRecipientCleanupTracking(RecipientCleanupTracking.DEFAULT.getRecipientCleanupTracking());
        companySettingsDto.setRecipientDeletion(RecipientDeletion.DEFAULT.getRecipientDeletion());
        companySettingsDto.setHostauthCookieExpireDays(HostAuthenticationCookieExpirationSettings.DEFAULT.getExpireDays());
        companySettingsDto.setRecipientExpireDays(configService.getIntegerValue(ConfigValue.ExpireRecipient));
        companySettingsDto.setStatisticsExpireDays(configService.getIntegerValue(ConfigValue.ExpireStatisticsMax));

        return companySettingsDto;
    }
}
