/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.converter;

import static com.agnitas.util.AgnUtils.unescapeForRFC5322;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.agnitas.beans.Company;
import com.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.dto.CompanySettingsDto;
import com.agnitas.emm.core.company.enums.LoginlockSettings;
import com.agnitas.emm.core.company.form.CompanyViewForm;
import com.agnitas.emm.core.components.entity.AdminTestMarkPlacementOption;
import com.agnitas.emm.core.components.entity.TestRunOption;
import com.agnitas.post.PostalField;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CompanyToCompanyViewFormConverter implements Converter<Company, CompanyViewForm> {

    private final ConfigService configService;

    public CompanyToCompanyViewFormConverter(final ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public CompanyViewForm convert(Company company) {
        CompanyViewForm companyViewForm = new CompanyViewForm();
        companyViewForm.setCompanyInfoDto(convertInfo(company));
        companyViewForm.setCompanySettingsDto(convertSettings(company));
        return companyViewForm;
    }

    private CompanyInfoDto convertInfo(Company company) {
        CompanyInfoDto companyInfoDto = new CompanyInfoDto();
        companyInfoDto.setId(company.getId());
        companyInfoDto.setName(company.getShortname());
        companyInfoDto.setDescription(company.getDescription());
        return companyInfoDto;
    }

    private CompanySettingsDto convertSettings(Company company) {
        CompanySettingsDto settingsDto = new CompanySettingsDto();
        settingsDto.setListHelpUrl(company.getListHelpUrl());
        settingsDto.setHasMailTracking(BooleanUtils.toBoolean(company.getMailtracking()));
        settingsDto.setStatisticsExpireDays(configService.getIntegerValue(ConfigValue.ExpireStatistics, company.getId()));
        settingsDto.setExpireRecipient(configService.getIntegerValue(ConfigValue.ExpireRecipient, company.getId()));
        settingsDto.setBusiness(company.getBusiness());
        settingsDto.setHasActivatedAccessAuthorization(configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, company.getId()));
        settingsDto.setHasExtendedSalutation(BooleanUtils.toBoolean(company.getSalutationExtended()));
        settingsDto.setExecutiveAdministrator(company.getStatAdmin());
        settingsDto.setTechnicalContacts(company.getContactTech());
        settingsDto.setSystemMessageEmails(company.getSystemMessageEmails());

        // todo: check is it necessary. the reason is: company.getLocaleLanguage()
        String localeLanguage = configService.getValue(ConfigValue.LocaleLanguage, company.getId());
        settingsDto.setLanguage(StringUtils.defaultIfEmpty(localeLanguage, "none"));

        // todo: check is it necessary. the reason is: company.getLocaleTimezone()
        String localeTimezone = configService.getValue(ConfigValue.LocaleTimezone, company.getId());
        settingsDto.setTimeZone(StringUtils.defaultIfEmpty(localeTimezone, "none"));

        // todo: check is it necessary. the reason is: company.isForceSending()
        settingsDto.setHasForceSending(configService.getBooleanValue(ConfigValue.ForceSending, company.getId()));

        settingsDto.setCleanRecipientsWithoutBinding(configService.getBooleanValue(ConfigValue.CleanRecipientsWithoutBinding, company.getId()));
        settingsDto.setRecipientAnonymization(configService.getIntegerValue(ConfigValue.CleanRecipientsData, company.getId()));
        settingsDto.setRecipientCleanupTracking(configService.getIntegerValue(ConfigValue.CleanTrackingData, company.getId()));
        settingsDto.setRecipientDeletion(configService.getIntegerValue(ConfigValue.DeleteRecipients, company.getId()));
        settingsDto.setHasTrackingVeto(configService.getBooleanValue(ConfigValue.AnonymizeTrackingVetoRecipients, company.getId()));
        settingsDto.setSector(company.getSector());
        settingsDto.setBusiness(company.getBusiness());
        settingsDto.setHasTwoFactorAuthentication(configService.getBooleanValue(ConfigValue.HostAuthentication, company.getId()));
        settingsDto.setMaxAdminMails(configService.getIntegerValue(ConfigValue.MaxAdminMails, company.getId()));
        settingsDto.setMaxFields(configService.getIntegerValue(ConfigValue.MaxFields, company.getId()));
        
        // Settings for login tracking
        final Optional<LoginlockSettings> settingsOptional = LoginlockSettings.fromSettings(
        		configService.getIntegerValue(ConfigValue.LoginTracking.WebuiMaxFailedAttempts, company.getId()),
        		configService.getIntegerValue(ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, company.getId()) / 60);
        settingsDto.setLoginlockSettingsName(settingsOptional.isPresent() ? settingsOptional.get().getName() : "UNDEFINED");
        
        // Password policy
        settingsDto.setPasswordPolicyName(getPasswordPolicyName(company.getId()));
        settingsDto.setPasswordExpireDays(this.configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, company.getId()));
        
        // 2FA cookie
        settingsDto.setHostauthCookieExpireDays(this.configService.getIntegerValue(ConfigValue.HostAuthenticationHostIdCookieExpireDays, company.getId()));

        settingsDto.setSendPasswordChangedNotification(configService.getBooleanValue(ConfigValue.SendPasswordChangedNotification, company.getId()));
        settingsDto.setSendEncryptedMailings(configService.getBooleanValue(ConfigValue.SendEncryptedMailings, company.getId()));
        settingsDto.setEnableHoneypotIntermediatePage(configService.getBooleanValue(ConfigValue.Honeypot.EnableIntermediatePage, company.getId()));

        settingsDto.setDefaultLinkExtension(configService.getValue(ConfigValue.DefaultLinkExtension, company.getId()));
        settingsDto.setLinkcheckerLinktimeout(configService.getIntegerValue(ConfigValue.Linkchecker_Linktimeout, company.getId()));
        settingsDto.setLinkcheckerThreadcount(configService.getIntegerValue(ConfigValue.Linkchecker_Threadcount, company.getId()));
        settingsDto.setMailingUndoLimit(configService.getIntegerValue(ConfigValue.MailingUndoLimit, company.getId()));
        settingsDto.setPrefillCheckboxSendDuplicateCheck(configService.getBooleanValue(ConfigValue.PrefillCheckboxSendDuplicateCheck, company.getId()));
        settingsDto.setFullviewFormName(configService.getValue(ConfigValue.FullviewFormName, company.getId()));

        settingsDto.setTrackingVetoAllowTransactionTracking(configService.getBooleanValue(ConfigValue.TrackingVetoAllowTransactionTracking, company.getId()));
        settingsDto.setDeleteSuccessfullyImportedFiles(configService.getBooleanValue(ConfigValue.DeleteSuccessfullyImportedFiles, company.getId()));
        settingsDto.setEnableAltgExtended(configService.isExtendedAltgEnabled(company.getId()));
        settingsDto.setImportAlwaysInformEmail(configService.getValue(ConfigValue.ImportAlwaysInformEmail, company.getId()));
        settingsDto.setNormalizeEmails(!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, company.getId()));
        settingsDto.setExportAlwaysInformEmail(configService.getValue(ConfigValue.ExportAlwaysInformEmail, company.getId()));
        settingsDto.setBccEmail(configService.getValue(ConfigValue.DefaultBccEmail, company.getId()));
        settingsDto.setAnonymizeAllRecipients(configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, company.getId()));
        settingsDto.setRecipientEmailInUseWarning(configService.getBooleanValue(ConfigValue.RecipientEmailInUseWarning, company.getId()));
        settingsDto.setAllowEmailWithWhitespace(configService.getBooleanValue(ConfigValue.AllowEmailWithWhitespace, company.getId()));
        settingsDto.setAllowEmptyEmail(configService.getBooleanValue(ConfigValue.AllowEmptyEmail, company.getId()));
        settingsDto.setExpireStatistics(configService.getIntegerValue(ConfigValue.ExpireStatistics, company.getId()));
        settingsDto.setExpireSuccess(configService.getIntegerValue(ConfigValue.ExpireSuccess, company.getId()));
        settingsDto.setExpireRecipient(configService.getIntegerValue(ConfigValue.ExpireRecipient, company.getId()));
        settingsDto.setExpireBounce(configService.getIntegerValue(ConfigValue.ExpireBounce, company.getId()));
        settingsDto.setExpireUpload(configService.getIntegerValue(ConfigValue.ExpireUpload, company.getId()));
        settingsDto.setWriteCustomerOpenOrClickField(configService.getBooleanValue(ConfigValue.WriteCustomerOpenOrClickField, company.getId()));
        settingsDto.setDefaultCompanyLinkTrackingMode(configService.getIntegerValue(ConfigValue.TrackableLinkDefaultTracking, company.getId()));
        settingsDto.setDefaultBlockSize(configService.getIntegerValue(ConfigValue.DefaultBlocksizeValue, company.getId()));
        settingsDto.setDefaultTestRunOption(TestRunOption.fromId(configService.getIntegerValue(ConfigValue.DefaultTestRunOption, company.getId())));
        settingsDto.setUserBasedFavoriteTargets(configService.isUserBasedFavoriteTargets(company.getId()));
        settingsDto.setFilterRecipientsOverviewForActiveRecipients(configService.getBooleanValue(ConfigValue.FilterRecipientsOverviewForActiveRecipients, company.getId()));
        settingsDto.setCleanAdminAndTestRecipientsActivity(configService.getBooleanValue(ConfigValue.CleanAdminAndTestRecipientsActivities, company.getId()));
        settingsDto.setIndividualLinkTrackingForMailings(configService.getBooleanValue(ConfigValue.IndividualLinkTrackingForAllMailings, company.getId()));
        settingsDto.setAutoDeeptracking(configService.isAutoDeeptracking(company.getId()));
        
        settingsDto.setHtmlContentAllowed(configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, company.getId()));

        settingsDto.setUseDefaultAddressFieldsForPost(configService.getBooleanValue(ConfigValue.UseDefaulAdressFieldsForPost, company.getId()));
        settingsDto.setPostalFieldsMappings(getPostalFieldsMappings(company.getId()));
        settingsDto.setMailingMinimumApprovals(configService.getIntegerValue(ConfigValue.MailingMinimumApprovals, company.getId()));
        settingsDto.setShowAllDashboardCalendarMailings(configService.getBooleanValue(ConfigValue.DashboardCalendarShowALlEntries, company.getId()));

        settingsDto.setAdminTestMarkPlacement(AdminTestMarkPlacementOption.find(configService.getValue(ConfigValue.Backend_AdminTestMark, company.getId())));

        executeIfNotDefaultValueSet(ConfigValue.Backend_AdminTestMarkSubjectAdmin, company.getId(), settingsDto::setAdminMailSubjectMark);
        executeIfNotDefaultValueSet(ConfigValue.Backend_AdminTestMarkSubjectTest, company.getId(), settingsDto::setTestMailSubjectMark);
        executeIfNotDefaultValueSet(ConfigValue.Backend_AdminTestMarkToAdmin, company.getId(), val -> settingsDto.setAdminMailToAddressMark(unescapeForRFC5322(val)));
        executeIfNotDefaultValueSet(ConfigValue.Backend_AdminTestMarkToTest, company.getId(), val -> settingsDto.setTestMailToAddressMark(unescapeForRFC5322(val)));
        settingsDto.setResponseInboxEnabled(configService.getBooleanValue(ConfigValue.EnableResponseInbox, company.getId()));
        settingsDto.setVouchersMandatory(configService.getBooleanValue(ConfigValue.Voucher.Vouchers_Mandatory, company.getId()));
        settingsDto.setVoucherEnableReport(configService.getBooleanValue(ConfigValue.Voucher.ReportEnable, company.getId()));
        settingsDto.setVoucherCcReportEmails(configService.getValue(ConfigValue.Voucher.ReportCc, company.getId()));
        settingsDto.setVoucherBccReportEmails(configService.getValue(ConfigValue.Voucher.ReportBcc, company.getId()));

        return settingsDto;
    }

    private Map<PostalField, String> getPostalFieldsMappings(int companyId) {
    	Map<PostalField, String> returnMap = new HashMap<>();
    	for (PostalField postalField : PostalField.values()) {
    		String clientsDefaultPostalFieldName = configService.getValue(postalField.getConfigValue(), companyId);
    		returnMap.put(postalField, clientsDefaultPostalFieldName);
    	}
        return returnMap;
    }

	private String getPasswordPolicyName(final int companyId) {
		final String policyName = configService.getValue(ConfigValue.PasswordPolicy, companyId);
		return PasswordPolicies.findByName(policyName).getPolicyName();
	}

    private void executeIfNotDefaultValueSet(ConfigValue configValue, int companyId, Consumer<String> function) {
        String adminSubjectMark = configService.getValue(configValue, companyId);
        if (!adminSubjectMark.equals(configValue.getDefaultValue())) {
            function.accept(adminSubjectMark);
        }
    }
}
