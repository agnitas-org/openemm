/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.converter;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComCompany;
import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.dto.CompanySettingsDto;
import com.agnitas.emm.core.company.form.CompanyViewForm;

@Component
public class ComCompanyToCompanyViewFormConverter implements Converter<ComCompany, CompanyViewForm> {
    @SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComCompanyToCompanyViewFormConverter.class);

    private ConfigService configService;

    public ComCompanyToCompanyViewFormConverter(ConfigService configService) {
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
        settingsDto.setStatisticsExpireDays(comCompany.getExpireStat());
        settingsDto.setRecipientExpireDays(comCompany.getExpireRecipient());
        settingsDto.setBusiness(comCompany.getBusiness());
        settingsDto.setHasActivatedAccessAuthorization(configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, comCompany.getId()));
        settingsDto.setHasExtendedSalutation(BooleanUtils.toBoolean(comCompany.getSalutationExtended()));
        settingsDto.setExecutiveAdministrator(comCompany.getStatAdmin());
        settingsDto.setTechnicalContacts(comCompany.getContactTech());
        settingsDto.setHasDataExportNotify(BooleanUtils.toBoolean(comCompany.getExportNotifyAdmin()));
        settingsDto.setMaxFailedLoginAttempts(comCompany.getMaxLoginFails());
        settingsDto.setBlockIpTime(comCompany.getLoginBlockTime());

        // todo: check is it necessary. the reason is: comCompany.getLocaleLanguage()
        String localeLanguage = configService.getValue(ConfigValue.LocaleLanguage, comCompany.getId());
        settingsDto.setLanguage(StringUtils.defaultIfEmpty(localeLanguage, "none"));

        // todo: check is it necessary. the reason is: comCompany.getLocaleTimezone()
        String localeTimezone = configService.getValue(ConfigValue.LocaleTimezone, comCompany.getId());
        settingsDto.setTimeZone(StringUtils.defaultIfEmpty(localeTimezone, "none"));

        // todo: check is it necessary. the reason is: comCompany.isForceSending()
        settingsDto.setHasForceSending(configService.getBooleanValue(ConfigValue.ForceSending, comCompany.getId()));

        settingsDto.setHasRecipientsCleanup(configService.getBooleanValue(ConfigValue.CleanRecipientsWithoutBinding, comCompany.getId()));
        settingsDto.setHasTrackingVeto(configService.getBooleanValue(ConfigValue.AnonymizeTrackingVetoRecipients, comCompany.getId()));
        settingsDto.setSector(comCompany.getSector());
        settingsDto.setBusiness(comCompany.getBusiness());
        settingsDto.setHasTwoFactorAuthentication(configService.getBooleanValue(ConfigValue.HostAuthentication, comCompany.getId()));
        return settingsDto;
    }
}
