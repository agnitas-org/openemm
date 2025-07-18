/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Target;
import com.agnitas.beans.Company;
import com.agnitas.beans.TargetLight;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminSavingResult;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.company.dto.CompanyAdminDto;
import com.agnitas.emm.core.company.dto.CompanyInfoDto;
import com.agnitas.emm.core.company.dto.CompanySettingsDto;
import com.agnitas.emm.core.company.enums.Business;
import com.agnitas.emm.core.company.enums.Language;
import com.agnitas.emm.core.company.enums.LoginlockSettings;
import com.agnitas.emm.core.company.enums.Sector;
import com.agnitas.emm.core.company.form.CompanyCreateForm;
import com.agnitas.emm.core.company.form.CompanyViewForm;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.components.entity.AdminTestMarkPlacementOption;
import com.agnitas.emm.core.components.entity.TestRunOption;
import com.agnitas.emm.core.logon.common.HostAuthenticationCookieExpirationSettings;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.premium.web.SpecialPremiumFeature;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.LicenseError;
import com.agnitas.web.mvc.Popups;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.factory.CompanyFactory;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.common.LicenseType;
import com.agnitas.emm.core.target.exception.TargetGroupPersistenceException;
import org.agnitas.emm.core.commons.password.policy.PasswordPolicies;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import static com.agnitas.util.AgnUtils.escapeForRFC5322;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class CompanyServiceImpl implements CompanyService {

    private static final Logger logger = LogManager.getLogger(CompanyServiceImpl.class);

    private RecipientProfileHistoryService recipientProfileHistoryService;
    private UserActivityLogService userActivityLogService;
    protected ExtendedConversionService conversionService;
    private CompanyFactory companyFactory;
    private ConfigService configService;
    private AdminService adminService;
    private AdminGroupService adminGroupService;
    @Qualifier("BounceFilterService")
    private BounceFilterService bounceFilterService;
    protected CompanyDao companyDao;
    private CompanyTokenService companyTokenService;
    private TargetDao targetDao;

    @Override
    public boolean isMailtrackingActive(int companyId) {
        return companyDao.isMailtrackingActive(companyId);
    }

    @Override
    public boolean isCompanyExisting(int companyId) {
        return companyId > 0 && companyDao.isCompanyExist(companyId);
    }

    @Override
    public List<Integer> getCompaniesIds() {
        return companyDao.getCompaniesIds();
    }

    @Override
    public List<CompanyEntry> getActiveCompanyEntries(boolean allowTransitionStatus) {
        return companyDao.getActiveCompaniesLight(allowTransitionStatus);
    }

    @Override
    public List<CompanyEntry> getActiveOwnCompanyEntries(int companyId, boolean allowTransitionStatus) {
        return companyDao.getActiveOwnCompaniesLight(companyId, allowTransitionStatus);
    }

    @Override
    public List<Company> getCreatedCompanies(int companyId) {
        return companyDao.getCreatedCompanies(companyId);
    }

    @Override
    public List<CompanyEntry> findAllByEmailPart(String email, int companyID) {
        return companyDao.findAllByEmailPart(email, companyID);
    }

    @Override
    public List<CompanyEntry> findAllByEmailPart(String email) {
        return companyDao.findAllByEmailPart(email);
    }

    @Override
    public void updateTechnicalContact(String email, int id) {
        companyDao.updateTechnicalContact(email, id);
    }

    @Override
    public boolean initTables(int companyID) {
        if (!companyDao.initTables(companyID)) {
            logger.error("Cannot create tables for company id: " + companyID);
            companyDao.updateCompanyStatus(companyID, CompanyStatus.LOCKED);
            return false;
        }

        return true;
    }

    @Override
    public final List<Company> listActiveCompanies() {
        return companyDao.getAllActiveCompaniesWithoutMasterCompany();
    }

    @Override
    public final Company getCompany(int companyID) {
        return companyDao.getCompany(companyID);
    }

    @Override
    public CompanyInfoDto getLight(int companyID) {
        CompanyEntry companyLight = companyDao.getCompanyLight(companyID);
        return conversionService.convert(companyLight, CompanyInfoDto.class);
    }

    @Override
    public boolean addExecutiveAdmin(int companyID, int executiveAdminID) {
        return companyDao.addExecutiveAdmin(companyID, executiveAdminID);
    }

    @Override
    public int getPriorityCount(int companyId) {
        return companyDao.getPriorityCount(companyId);
    }

    @Override
    public void setPriorityCount(int companyId, int value) {
        companyDao.setPriorityCount(companyId, Math.max(0, value));
    }

    @Override
    public Set<Permission> getCompanyPermissions(int companyId) {
        return companyDao.getCompanyPermissions(companyId);
    }

    @Override
    public PaginatedListImpl<CompanyInfoDto> getCompanyList(int companyID, String sort, String direction, int page, int rownums) {
        PaginatedListImpl<CompanyEntry> companyList = companyDao.getCompanyList(companyID, sort, direction, page, rownums);
        return conversionService.convertPaginatedList(companyList, CompanyEntry.class, CompanyInfoDto.class);
    }

    @Override
    public CompanyViewForm getCompanyForm(int companyId) {
        Company company = getCompany(companyId);
        return conversionService.convert(company, CompanyViewForm.class);
    }

    @Override
    public List<AdminEntry> getAdmins(int companyId) {
        return ListUtils.emptyIfNull(adminService.listAdminsByCompanyID(companyId));
    }

    @Override
    public PaginatedListImpl<AdminEntry> getAdmins(PaginationForm form, int companyId) {
        return adminService.getList(companyId,  form.getSort(), form.getDir(), form.getPage(), form.getNumberOfRows());
    }

    @Override
    public int create(Admin admin, CompanyCreateForm form, Popups popups, String sessionId) throws Exception {
        Company company = companyFactory.newCompany();
        company.setCreatorID(admin.getCompanyID());
        company.setStatus(CompanyStatus.ACTIVE);
        company.setRdirDomain(configService.getValue(ConfigValue.DefaultRdirDomain));
        company.setMailloopDomain(configService.getValue(ConfigValue.DefaultMailloopDomain));
        company.setEnabledUIDVersion(configService.getIntegerValue(ConfigValue.UidVersionForNewCompanies));

        setupInfo(company, form.getCompanyInfoDto());
        setupSettings(company, form.getCompanySettingsDto());
        // save new new company
        companyDao.saveCompany(company);

        // save config values
        saveConfigValuesForNewCompany(admin, company.getId(), form.getCompanySettingsDto());

        // create new table for new company
        initTableAndCopyTemplates(company, sessionId, admin);

        // create new user for new company
        int executiveAdminId = createExecutiveAdmin(admin, form.getCompanyInfoDto(), form.getCompanyAdminDto(), company.getId(), popups);
        addExecutiveAdmin(company.getId(), executiveAdminId);
        
        createStandardBounceFilter(company.getId(), TimeZone.getTimeZone(form.getCompanySettingsDto().getTimeZone()));
        
        // Create random token
        companyTokenService.assignRandomToken(company.getId(), false);
        
        return company.getId();
    }

    @Override
    public int update(Admin admin, CompanyViewForm form) throws Exception {
        Company company = companyDao.getCompany(form.getCompanyInfoDto().getId());
        UserAction companyChangesLog = getCompanyChangesLog(form, company);
        setupInfo(company, form.getCompanyInfoDto());
        setupSettings(company, form.getCompanySettingsDto());

        // saving company
        companyDao.saveCompany(company);

        // save config values
        saveConfigValues(admin, company.getId(), form.getCompanySettingsDto());

        companyTokenService.assignRandomToken(company.getId(), false);

        // write UAL
        userActivityLogService.writeUserActivityLog(admin, companyChangesLog);
        return company.getId();
    }
    
    @Override
    public CompanyStatus getStatus(int companyID) {
    	Company company = companyDao.getCompany(companyID);
		return company.getStatus();
    }

    @Override
    public boolean deleteCompany(int companyIdForRemove) {
        return false;
    }
    
    @Override
    public boolean deactivateCompany(int companyIdForDeactivation) {
        Company company = companyDao.getCompany(companyIdForDeactivation);
        if (Objects.nonNull(company) && company.getStatus() == CompanyStatus.ACTIVE) {
            companyDao.updateCompanyStatus(company.getId(), CompanyStatus.LOCKED);
            if (logger.isInfoEnabled()) {
                logger.info("Company: " + companyIdForDeactivation + " deactivated");
            }
            return true;
        } else {
        	return false;
        }
    }
    
    @Override
    public boolean reactivateCompany(int companyIdForReactivation) {
        Company company = companyDao.getCompany(companyIdForReactivation);

        if (Objects.nonNull(company) && (company.getStatus() == CompanyStatus.TODELETE || company.getStatus() == CompanyStatus.LOCKED)) {
            doReactivateCompany(companyIdForReactivation);
            return true;
        } else {
        	return false;
        }
    }

    @Override
    public boolean markCompanyForDeletion(final int companyId) {
        final Company company = companyDao.getCompany(companyId);

        if (Objects.nonNull(company) && company.getStatus() == CompanyStatus.LOCKED) {
            companyDao.updateCompanyStatus(company.getId(), CompanyStatus.TODELETE);

            if (logger.isInfoEnabled()) {
                logger.info(String.format("Company %d marked for deletion", company.getId()));
            }

            return true;
        } else {
        	return false;
        }
    }

    protected void doReactivateCompany(int id) {
        // Check maximum number of companies
        int maximumNumberOfCompanies = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCompanies);
        int gracefulExtension = configService.getIntegerValue(ConfigValue.System_License_MaximumNumberOfCompanies_Graceful);
        if (maximumNumberOfCompanies >= 0) {
            int numberOfCompanies = getNumberOfCompanies();
            if (numberOfCompanies >= maximumNumberOfCompanies) {
                if (gracefulExtension > 0
                        && numberOfCompanies < maximumNumberOfCompanies + gracefulExtension) {
                    logger.warn("Invalid Number of tenants. Current value is " + numberOfCompanies + ". Limit is " + maximumNumberOfCompanies + ". Gracefully " + gracefulExtension + " more accounts have been permitted");
                } else {
                    throw new LicenseError("Invalid Number of accounts", maximumNumberOfCompanies, numberOfCompanies);
                }
            }
        }

        companyDao.updateCompanyStatus(id, CompanyStatus.ACTIVE);
        if (logger.isInfoEnabled()) {
            logger.info("Company: {} reactivated", id);
        }
    }

    @Override
    public int getCompanyDatasource(int companyId) {
        return companyDao.getCompanyDatasource(companyId);
    }

    @Override
    public boolean isCompanyNameUnique(int companyId, String shortname) {
        String actualShortname = companyDao.getShortName(companyId);
        if(StringUtils.isNotEmpty(actualShortname) && StringUtils.equals(actualShortname, shortname)) {
            return true;
        }

        return StringUtils.isNotEmpty(shortname) && companyDao.isCompanyNameUnique(shortname);
    }

    @Override
    public boolean isCreatorId(int companyId, int creatorId) {
        return companyId > 0 && companyDao.isCreatorId(companyId, creatorId);
    }

    private void setupInfo(Company company, CompanyInfoDto companyInfoDto) {
        company.setShortname(companyInfoDto.getName());
        company.setDescription(companyInfoDto.getDescription());
    }
    
    private void setupSettings(Company company, CompanySettingsDto settingsDto) {
        company.setSalutationExtended(BooleanUtils.toInteger(settingsDto.isHasExtendedSalutation()));
        company.setStatAdmin(settingsDto.getExecutiveAdministrator());
        company.setContactTech(settingsDto.getTechnicalContacts());

        company.setSecretKey(RandomStringUtils.randomAscii(32));
        company.setSector(settingsDto.getSector());
        company.setBusiness(settingsDto.getBusiness());
    }

    protected void saveConfigValuesForNewCompany(Admin admin, int companyId, CompanySettingsDto settings) {
        saveConfigValues(admin, companyId, settings);
    }
    
    protected void saveConfigValues(Admin admin, int companyId, CompanySettingsDto settings) {
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.DashboardCalendarShowALlEntries, companyId, admin, settings.isShowAllDashboardCalendarMailings());
        if (admin.permissionAllowed(Permission.COMPANY_AUTHENTICATION)) {
            // Write 2FA settings
            writeHostAuthSettings(settings, companyId, admin);
        }

        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.ForceSending, companyId, admin, settings.isHasForceSending());

        if (!settings.getLanguage().equalsIgnoreCase(Language.NONE.toString())) {
            checkChangeAndLogCompanyInfoValue(ConfigValue.LocaleLanguage, companyId, admin, settings.getLanguage());

            String locale = settings.getLanguage();
            String localeCountry = locale.substring(locale.indexOf('_') + 1);
            checkChangeAndLogCompanyInfoValue(ConfigValue.LocaleCountry, companyId, admin, localeCountry);
        } else {
        	checkChangeAndLogCompanyInfoValue(ConfigValue.LocaleLanguage, companyId, admin, null);
        	checkChangeAndLogCompanyInfoValue(ConfigValue.LocaleCountry, companyId, admin, null);
        }

        if (!StringUtils.equalsIgnoreCase(settings.getTimeZone(), "0")) {
        	checkChangeAndLogCompanyInfoValue(ConfigValue.LocaleTimezone, companyId, admin, settings.getTimeZone());
        } else {
        	checkChangeAndLogCompanyInfoValue(ConfigValue.LocaleTimezone, companyId, admin, null);
        }

        if (admin.permissionAllowed(Permission.CLEANUP_RECIPIENT_DATA)) {
        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.CleanRecipientsData, companyId, admin, settings.getRecipientAnonymization());
        }
        
        if (admin.permissionAllowed(Permission.CLEANUP_RECIPIENT_TRACKING) || admin.permissionAllowed(Permission.CLEANUP_RECIPIENT_DATA)) {
        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.CleanTrackingData, companyId, admin, settings.getRecipientCleanupTracking());
        }
        
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.CleanRecipientsWithoutBinding, companyId, admin, settings.isCleanRecipientsWithoutBinding());
        
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.AnonymizeTrackingVetoRecipients, companyId, admin, settings.isHasTrackingVeto());

        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, companyId, admin, settings.isHasActivatedAccessAuthorization());
        
        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.DeleteRecipients, companyId, admin, settings.getRecipientDeletion());
        
        // Login lock settings
        writeLoginLockSettingsConfigValues(settings, companyId, admin);
        
        // Write password policy
        writePasswordSecuritySettings(settings, companyId, admin);

        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.SendPasswordChangedNotification, companyId, admin, settings.isSendPasswordChangedNotification());
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.SendEncryptedMailings, companyId, admin, settings.isSendEncryptedMailings());

        if (settings.getMaxAdminMails() > 0) {
        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.MaxAdminMails, companyId, admin, settings.getMaxAdminMails()) ;
        } else {
        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.MaxAdminMails, companyId, admin, Integer.parseInt(ConfigValue.MaxAdminMails.getDefaultValue()));
        }
        
        if (settings.getMaxFields() > 0) {
        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.MaxFields, companyId, admin, settings.getMaxFields()) ;
        } else {
        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.MaxFields, companyId, admin, Integer.parseInt(ConfigValue.MaxFields.getDefaultValue()));
        }
        
        checkChangeAndLogCompanyInfoValue(ConfigValue.ImportAlwaysInformEmail, companyId, admin, settings.getImportAlwaysInformEmail());
        
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.AllowUnnormalizedEmails, companyId, admin, !settings.isNormalizeEmails());
        
        checkChangeAndLogCompanyInfoValue(ConfigValue.ExportAlwaysInformEmail, companyId, admin, settings.getExportAlwaysInformEmail());
        
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.AnonymizeAllRecipients, companyId, admin, settings.isAnonymizeAllRecipients());
        
        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, companyId, admin, settings.isHtmlContentAllowed());
        
        if (admin.permissionAllowed(Permission.COMPANY_SETTINGS_INTERN)) {      
            checkChangeAndLogCompanyInfoValue(ConfigValue.DefaultLinkExtension, companyId, admin, settings.getDefaultLinkExtension());
	        
            checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.Linkchecker_Linktimeout, companyId, admin, settings.getLinkcheckerLinktimeout());
	        
	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.Linkchecker_Threadcount, companyId, admin, settings.getLinkcheckerThreadcount());
	        
	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.MailingUndoLimit, companyId, admin, settings.getMailingUndoLimit());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.PrefillCheckboxSendDuplicateCheck, companyId, admin, settings.isPrefillCheckboxSendDuplicateCheck());
	        
	        checkChangeAndLogCompanyInfoValue(ConfigValue.FullviewFormName, companyId, admin, settings.getFullviewFormName());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.TrackingVetoAllowTransactionTracking, companyId, admin, settings.isTrackingVetoAllowTransactionTracking());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.DeleteSuccessfullyImportedFiles, companyId, admin, settings.isDeleteSuccessfullyImportedFiles());

	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.CleanTrackingData, companyId, admin, settings.getRecipientCleanupTracking());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.RecipientEmailInUseWarning, companyId, admin, settings.isRecipientEmailInUseWarning());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.AllowEmailWithWhitespace, companyId, admin, settings.isAllowEmailWithWhitespace());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.AllowEmptyEmail, companyId, admin, settings.isAllowEmptyEmail());
	        
	    	if (admin.permissionAllowed(Permission.MAILING_EXPIRE)) {
	        	int expireMaximum = configService.getIntegerValue(ConfigValue.ExpireStatisticsMax);
	    		int newExpireStatistics = Math.max(1, Math.min(expireMaximum, settings.getStatisticsExpireDays()));
	        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.ExpireStatistics, companyId, admin, newExpireStatistics);
	    	}
	        
	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.ExpireStatistics, companyId, admin, settings.getExpireStatistics());
	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.ExpireSuccess, companyId, admin, settings.getExpireSuccess());

        	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.ExpireRecipient, companyId, admin, settings.getExpireRecipient());
	        
	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.ExpireBounce, companyId, admin, settings.getExpireBounce());
	        
	        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.ExpireUpload, companyId, admin, settings.getExpireUpload());
	        
	        checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.WriteCustomerOpenOrClickField, companyId, admin, settings.isWriteCustomerOpenOrClickField());
        }

        if (admin.permissionAllowed(Permission.COMPANY_DEFAULT_STEPPING)) {
            int defaultBlockSize = settings.getDefaultBlockSize();

            checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.DefaultBlocksizeValue, companyId, admin, defaultBlockSize);
            checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.ForceSteppingBlocksize, companyId, admin,defaultBlockSize > 0);
        }

        if (admin.permissionAllowed(Permission.MAILING_SEND_ADMIN_TARGET) || configService.getIntegerValue(ConfigValue.DefaultTestRunOption) != TestRunOption.TARGET.getId()) {
            checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.DefaultTestRunOption, companyId, admin, settings.getDefaultTestRunOption().getId());
        }

        if (admin.isRedesignedUiUsed()) {
            checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.IndividualLinkTrackingForAllMailings, companyId, admin, settings.isIndividualLinkTrackingForMailings());

            checkChangeAndLogCompanyInfoValue(ConfigValue.Backend_AdminTestMark, companyId, admin, settings.getAdminTestMarkPlacement().getStorageValue());

            if (settings.getAdminTestMarkPlacement().equals(AdminTestMarkPlacementOption.TO_ADDRESS) || settings.getAdminTestMarkPlacement().equals(AdminTestMarkPlacementOption.BOTH)) {
                checkChangeAndLogCompanyInfoValue(
                        ConfigValue.Backend_AdminTestMarkToAdmin,
                        companyId,
                        admin,
                        escapeForRFC5322(defaultIfBlank(settings.getAdminMailToAddressMark(), ConfigValue.Backend_AdminTestMarkToAdmin.getDefaultValue()))
                );
                checkChangeAndLogCompanyInfoValue(
                        ConfigValue.Backend_AdminTestMarkToTest,
                        companyId,
                        admin,
                        escapeForRFC5322(defaultIfBlank(settings.getTestMailToAddressMark(), ConfigValue.Backend_AdminTestMarkToTest.getDefaultValue()))
                );
            }

            if (settings.getAdminTestMarkPlacement().equals(AdminTestMarkPlacementOption.SUBJECT) || settings.getAdminTestMarkPlacement().equals(AdminTestMarkPlacementOption.BOTH)) {
                checkChangeAndLogCompanyInfoValue(
                        ConfigValue.Backend_AdminTestMarkSubjectAdmin,
                        companyId,
                        admin,
                        defaultIfBlank(settings.getAdminMailSubjectMark(), ConfigValue.Backend_AdminTestMarkSubjectAdmin.getDefaultValue())
                );
                checkChangeAndLogCompanyInfoValue(
                        ConfigValue.Backend_AdminTestMarkSubjectTest,
                        companyId,
                        admin,
                        defaultIfBlank(settings.getTestMailSubjectMark(), ConfigValue.Backend_AdminTestMarkSubjectTest.getDefaultValue())
                );
            }
            checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.EnableResponseInbox, companyId, admin, settings.isResponseInboxEnabled());
        }

        if (settings.isRegenerateTargetSqlOnce()) {
        	regenerateTargetSql(companyId);
        	settings.setRegenerateTargetSqlOnce(false);
        }

        companyDao.setAutoDeeptracking(companyId, settings.isAutoDeeptracking());
    }

	private void writePasswordSecuritySettings(final CompanySettingsDto settings, final int companyId, final Admin admin) {
    	// Password policy
    	checkChangeAndLogCompanyInfoValue(ConfigValue.PasswordPolicy, companyId, admin, PasswordPolicies.findByName(settings.getPasswordPolicyName()).getPolicyName());
        checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.UserPasswordExpireDays, companyId, admin, settings.getPasswordExpireDays());
    }

	private void writeLoginLockSettingsConfigValues(final CompanySettingsDto companySettings, final int companyId, final Admin admin) {
    	if (companyId != 0) { // Do not overwrite global settings
    		final Optional<LoginlockSettings> settingsOptional = LoginlockSettings.fromName(companySettings.getLoginlockSettingsName());
    		
    		if(settingsOptional.isPresent()) {
    			final LoginlockSettings settings = settingsOptional.get();
    			
    			checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.LoginTracking.WebuiIpBlockTimeSeconds, companyId, admin, settings.getLockTimeMinutes() * 60);
    			
    			checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.LoginTracking.WebuiMaxFailedAttempts, companyId, admin, settings.getMaxFailedAttempts());
    		} else {
        		try {
        			throw new Exception("Stack trace");	// Throw exception to get stack trace
        		} catch(final Exception e) {
        			logger.error(String.format("Invalid loginlock settings name: '%s'", companySettings.getLoginlockSettingsName()), e);
        		}
    		}
    	} else {
    		try {
    			throw new Exception("Stack trace");	// Throw exception to get stack trace
    		} catch(final Exception e) {
    			logger.error("Attempt to overwrite global login tracking settings", e);
    		}
    	}
    }
    
    private void writeHostAuthSettings(final CompanySettingsDto settings, final int companyId, final Admin admin) {
    	// Write global 2FA enable state
    	checkChangeAndLogCompanyInfoBooleanValue(ConfigValue.HostAuthentication, companyId, admin, settings.isHasTwoFactorAuthentication());

        // Write 2FA cookie expiration
    	final HostAuthenticationCookieExpirationSettings cookieExpireSettings = HostAuthenticationCookieExpirationSettings.findByExpireDays(settings.getHostauthCookieExpireDays());
    	checkChangeAndLogCompanyInfoIntegerValue(ConfigValue.HostAuthenticationHostIdCookieExpireDays, companyId, admin, cookieExpireSettings.getExpireDays());
    }

    protected void checkChangeAndLogCompanyInfoValue(ConfigValue configValue, final int companyId, final Admin admin, String newValue) {
		String currentValue = configService.getValue(configValue, companyId);
		if (currentValue == null) {
			currentValue = "";
    	}
    	if (newValue == null) {
    		newValue = "";
    	}
		if (!StringUtils.equals(currentValue, newValue)) {
			configService.writeOrDeleteIfDefaultValue(configValue, companyId, newValue, "Changed by: " + admin.getUsername() + (admin.isSupervisor() ? "/" + admin.getSupervisor().getSupervisorName() : ""));
			userActivityLogService.writeUserActivityLog(admin, "Company setting changed", configValue.getName() + ": " + currentValue + " => " + newValue);
		}
	}

	protected void checkChangeAndLogCompanyInfoBooleanValue(ConfigValue configValue, final int companyId, final Admin admin, boolean newValue) {
		boolean currentValue = configService.getBooleanValue(configValue, companyId);
		if (currentValue != newValue) {
			configService.writeOrDeleteIfDefaultValue(configValue, companyId, newValue ? "true" : "false", "Changed by: " + admin.getUsername() + (admin.isSupervisor() ? "/" + admin.getSupervisor().getSupervisorName() : ""));
			userActivityLogService.writeUserActivityLog(admin, "Company setting changed", configValue.getName() + ": " + currentValue + " => " + newValue);
		}
	}

	protected void checkChangeAndLogCompanyInfoIntegerValue(ConfigValue configValue, final int companyId, final Admin admin, int newValue) {
		int currentValue = configService.getIntegerValue(configValue, companyId);
		if (currentValue != newValue) {
			configService.writeOrDeleteIfDefaultValue(configValue, companyId, Integer.toString(newValue), "Changed by: " + admin.getUsername() + (admin.isSupervisor() ? "/" + admin.getSupervisor().getSupervisorName() : ""));
			userActivityLogService.writeUserActivityLog(admin, "Company setting changed", configValue.getName() + ": " + currentValue + " => " + newValue);
		}
	}

    private void initTableAndCopyTemplates(Company company, String sessionId, Admin admin) throws Exception {
        int companyId = company.getId();

        if (initTables(companyId)) {
            logger.info("Company: " + companyId + " created");

            LicenseType licenseType = LicenseType.getLicenseTypeByID(configService.getValue(ConfigValue.System_License_Type));
			if (licenseType == LicenseType.Inhouse || licenseType == LicenseType.OpenEMM || licenseType == LicenseType.OpenEMM_Plus) {
                if (StringUtils.isBlank(configService.getValue(ConfigValue.RecipientProfileFieldHistory, companyId))) {
                    // Setup RecipientProfileFieldHistory for inhouse instances only
                    recipientProfileHistoryService.enableProfileFieldHistory(companyId);
                }

                // Check Automation Package
                if (company.getMailtracking() <= 0) {
                    company.setMailtracking(1);
                    companyDao.saveCompany(company);
                }
                companyDao.changeFeatureRights(SpecialPremiumFeature.AUTOMATION.getName(), companyId, true, "New Inhouse client creation");

                checkRetargeting(company);
            }

            generateMissingTemplateThumbnails(admin, sessionId, companyId);
        }

        logger.info("Cannot successfully create new company: " + companyId);
    }

    void generateMissingTemplateThumbnails(Admin admin, String sessionId, int companyId) {
        // do nothing
    }

    void checkRetargeting(Company company) {
    	// do nothing
    }

    private int createExecutiveAdmin(Admin admin, CompanyInfoDto companyInfo, CompanyAdminDto companyAdmin, int newCompanyID, Popups popups) {
        AdminForm adminForm = new AdminForm();
        adminForm.setAdminID(0);
        adminForm.setCompanyID(newCompanyID);
        adminForm.setCompanyName(companyInfo.getName());
        adminForm.setEmail(companyAdmin.getEmail());
        adminForm.setStatEmail(companyAdmin.getStatisticEmail());
        adminForm.setTitle(companyAdmin.getTitle());
        adminForm.setGender(companyAdmin.getSalutation());
        adminForm.setUsername(companyAdmin.getUsername());
        adminForm.setFirstname(companyAdmin.getFirstName());
        adminForm.setFullname(companyAdmin.getLastName());
        adminForm.setPassword(companyAdmin.getPassword());

        if (adminGroupService.adminGroupExists(admin.getCompanyID(), "Administrator")) {
            List<Integer> adminGroupIds = new ArrayList<>();
            adminGroupIds.add(adminGroupService.getAdminGroupByName("Administrator", admin.getCompanyID()).getGroupID());
        	adminForm.setGroupIDs(adminGroupIds);
        }

        if (companyAdmin.getLanguage() != null) {
            int aPos = companyAdmin.getLanguage().indexOf('_');
            String lang = companyAdmin.getLanguage().substring(0, aPos);
            String country = companyAdmin.getLanguage().substring(aPos + 1);
            adminForm.setAdminLocale(new Locale(lang, country));
        }

        adminForm.setAdminTimezone(companyAdmin.getTimeZone());

        AdminSavingResult result = adminService.saveAdmin(adminForm, false, admin);

        if (result.isSuccess()) {
            Admin savedAdmin = result.getResult();
            return savedAdmin.getAdminID();
        } else {
            popups.alert(result.getError());
            return 0;
        }
    }

    private UserAction getCompanyChangesLog(CompanyViewForm newCompany, Company oldCompany) {
        StringBuilder description = new StringBuilder();
        description.append("ID(").append(newCompany.getCompanyInfoDto().getId()).append(")");

        String oldCompanyShortname = oldCompany.getShortname();
        if (!oldCompanyShortname.equals(newCompany.getCompanyInfoDto().getName())) {
            description.append(" Short Name(")
                    .append(oldCompanyShortname)
                    .append(" changed to ")
                    .append(newCompany.getCompanyInfoDto().getName())
                    .append(")");
        }

        String oldDescription = StringUtils.trimToEmpty(oldCompany.getDescription());
        String newDescription = StringUtils.trimToEmpty(newCompany.getCompanyInfoDto().getDescription());
        if (!oldDescription.equals(newDescription)) {
            description.append(" Description(")
                    .append(oldDescription)
                    .append(" changed to ")
                    .append(newCompany.getCompanyInfoDto().getDescription())
                    .append(")");
        }
 
        int oldSectorIndex = oldCompany.getSector();
        int newSectorIndex = newCompany.getCompanySettingsDto().getSector();
        if (oldSectorIndex != newSectorIndex) {
            description.append(" Sector(")
                    .append(Sector.getById(oldSectorIndex))
                    .append(" changed to ")
                    .append(Sector.getById(newSectorIndex))
                    .append(")");
        }

        int oldBusinessIndex = oldCompany.getBusiness();
        if (oldBusinessIndex != newCompany.getCompanySettingsDto().getBusiness()) {
            description.append(" Business(")
                    .append(Business.getById(oldBusinessIndex))
                    .append(" changed to ")
                    .append(Business.getById(newCompany.getCompanySettingsDto().getBusiness()))
                    .append(")");
        }

        return new UserAction("edit company", description.toString());
    }
    
    private void createStandardBounceFilter(final int companyId, final TimeZone adminTimeZone) throws Exception {
		BounceFilterDto standardBounceFilter = new BounceFilterDto();
		standardBounceFilter.setShortName("Standard-Filter");
		bounceFilterService.saveBounceFilter(companyId, adminTimeZone, standardBounceFilter, true);
	}
	
    @Override
    public boolean createFrequencyFields(int companyID) {
    	return companyDao.createFrequencyFields(companyID);
    }

    public void setRecipientProfileHistoryService(RecipientProfileHistoryService recipientProfileHistoryService) {
        this.recipientProfileHistoryService = recipientProfileHistoryService;
    }

    public void setUserActivityLogService(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    public void setConversionService(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void setCompanyFactory(CompanyFactory companyFactory) {
        this.companyFactory = companyFactory;
    }

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    public void setAdminGroupService(AdminGroupService adminGroupService) {
        this.adminGroupService = adminGroupService;
    }

    public void setCompanyDao(CompanyDao companyDao) {
        this.companyDao = companyDao;
    }

	public void setBounceFilterService(BounceFilterService bounceFilterService) {
		this.bounceFilterService = bounceFilterService;
	}
    
	public final void setCompanyTokenService(final CompanyTokenService service) {
		this.companyTokenService = Objects.requireNonNull(service, "CompanyTokenService is null");
	}

    public void setTargetDao(TargetDao targetDao) {
        this.targetDao = targetDao;
    }

	@Override
	public int getNumberOfCompanies() {
		return companyDao.getNumberOfCompanies();
	}

    @Override
    public String getTechnicalContact(int companyId) {
        return getCompany(companyId).getContactTech();
    }

    private void regenerateTargetSql(int companyID) {
    	for (TargetLight targetLight : targetDao.getTargetLights(companyID)) {
			if (!targetLight.isLocked()) {
	    		try {
					Target target = targetDao.getTarget(targetLight.getId(), companyID);
					targetDao.saveTarget(target);
				} catch (TargetGroupPersistenceException e) {
					e.printStackTrace();
				}
			}
    	}
	}
}
