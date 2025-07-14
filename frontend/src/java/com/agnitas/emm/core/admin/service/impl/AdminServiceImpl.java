/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.CompaniesConstraints;
import com.agnitas.beans.Company;
import com.agnitas.beans.EmmLayoutBase;
import com.agnitas.beans.impl.AdminEntryImpl;
import com.agnitas.beans.impl.AdminImpl;
import com.agnitas.beans.impl.AdminPreferencesImpl;
import com.agnitas.beans.impl.CompanyStatus;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.dao.AdminDao;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.dao.AdminPreferencesDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.EmmLayoutBaseDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionType;
import com.agnitas.emm.core.admin.AdminException;
import com.agnitas.emm.core.admin.form.AdminForm;
import com.agnitas.emm.core.admin.service.AdminNotifier;
import com.agnitas.emm.core.admin.service.AdminSavingResult;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.admin.service.PermissionFilter;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.password.PasswordReminderState;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.news.enums.NewsType;
import com.agnitas.emm.core.permission.service.PermissionService;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.Tuple;
import com.agnitas.util.preferences.PreferenceItem;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdminServiceImpl implements AdminService {
	
	private static final Logger logger = LogManager.getLogger(AdminServiceImpl.class);

	protected AdminDao adminDao;
	protected CompanyDao companyDao;
	protected AdminPreferencesDao adminPreferencesDao;
	protected AdminGroupDao adminGroupDao;
	protected ConfigService configService;
	private PermissionFilter permissionFilter;
	protected EmmLayoutBaseDao emmLayoutBaseDao;
	protected PermissionService permissionService;
	private AdminNotifier adminNotifier;
	private BulkActionValidationService<Integer, Admin> bulkActionValidationService;

	public void setAdminDao(AdminDao adminDao) {
		this.adminDao = adminDao;
	}
	
	public void setCompanyDao(CompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setAdminPreferencesDao(AdminPreferencesDao adminPreferencesDao) {
		this.adminPreferencesDao = adminPreferencesDao;
	}

	public void setBulkActionValidationService(BulkActionValidationService<Integer, Admin> bulkActionValidationService) {
		this.bulkActionValidationService = bulkActionValidationService;
	}

	public void setAdminGroupDao(AdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}
	
	public void setAdminNotifier(AdminNotifier notifier) {
		this.adminNotifier = Objects.requireNonNull(notifier, "AdminNotifier is null");
	}
	
	public void setPermissionFilter(PermissionFilter permissionFilter) {
		this.permissionFilter = Objects.requireNonNull(permissionFilter, "Permission filter is null");
	}

	public void setEmmLayoutBaseDao(EmmLayoutBaseDao emmLayoutBaseDao) {
		this.emmLayoutBaseDao = emmLayoutBaseDao;
	}
	
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	// ----------------------------------------------------------------------------------------------------------------

	@Override
	public Optional<Admin> getAdminByName(final String username) {
		try {
			final Admin admin = adminDao.getAdmin(username);
			
			return Optional.of(admin);
		} catch(final Exception e) {
			logger.error(String.format("Error reading admin by name (name = '%s')", username), e);
			
			return Optional.empty();
		}
	}
	
	@Override
	public final Optional<Admin> findAdminByCredentials(final String username, final String password) {
		final Admin admin = this.adminDao.getAdminByLogin(username, password);
		
		return Optional.ofNullable(admin);
	}
	
	@Override
	public Admin getAdminByNameForSupervisor(String username, String supervisorName, String password) throws AdminException, SupervisorException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> mapIdToUsernameByCompanyAndEmail(int companyId) {
		List<AdminEntry> admins = adminDao.getAllAdminsByCompanyIdOnlyHasEmail(companyId);
		Map<String, String> adminsMap = new HashMap<>();
		for (AdminEntry admin : admins) {
			adminsMap.put(String.valueOf(admin.getId()), admin.getUsername());
		}
		return adminsMap;
	}

	@Override
	public Map<Integer, String> getAdminsNamesMap(int companyId) {
		return adminDao.getAdminsNamesMap(companyId);
	}

	@Override
	public ServiceResult<Admin> isPossibleToDeleteAdmin(final int adminId, final int companyId) {
		final Admin admin = getAdmin(adminId, companyId);
		if(admin == null) {
			return ServiceResult.error(Message.of("error.general.missing"));
		}
		final Company company = admin.getCompany();
		if(company != null && company.getStatAdmin() == adminId && CompanyStatus.ACTIVE == company.getStatus()) {
			return new ServiceResult<>(admin, false, Message.of("error.admin.delete.executive", company.getShortname()));
		}

		return ServiceResult.success(admin);
	}

	@Override
	public ServiceResult<Admin> delete(Admin admin, int adminIdToDelete) {
		Admin adminToDelete = adminDao.getAdmin(adminIdToDelete, admin.getCompanyID());

		if (adminToDelete == null) {
			return new ServiceResult<>(null, false);
		}

		if(!adminDao.delete(adminToDelete)) {
			return new ServiceResult<>(adminToDelete, false);
		}
		adminPreferencesDao.delete(adminIdToDelete);

		if (logger.isInfoEnabled()) {
			logger.info("Admin " + adminIdToDelete + " deleted");
		}

		return new ServiceResult<>(adminToDelete, true);
	}
	
	@Override
	public boolean deleteAdmin(final int adminID, final int companyID) {
		final boolean result = adminDao.delete(adminID, companyID);

		if (!result) {
			logger.info(String.format("Cannot delete unknown admin ID %d (company ID %d)", adminID, companyID));
			return false;
		}
		
		adminPreferencesDao.delete(adminID);
		
		if (logger.isInfoEnabled()) {
			logger.info("Admin " + adminID + " deleted");
		}
		
		return result;
	}

	@Override
	public AdminSavingResult saveAdmin(AdminForm form, boolean restfulUser, Admin editorAdmin) {
		int savingAdminID = form.getAdminID();
		int savingCompanyID = form.getCompanyID();
		int editorCompanyID = editorAdmin.getCompanyID();

		if (savingCompanyID != editorCompanyID) {
			List<Company> allowedCompanies = companyDao.getCreatedCompanies(editorCompanyID);

			boolean validCompany = allowedCompanies.stream().anyMatch(company -> company.getId() == savingCompanyID);

			if (!validCompany) {
				return AdminSavingResult.error(new Message("error.permissionDenied"));
			}
		}

		Admin savingAdmin;
		AdminPreferences savingAdminPreferences;
		boolean isNew = savingAdminID == 0;
		boolean isPasswordChanged = false;

		if (isNew) {
			savingAdmin = new AdminImpl();
			savingAdmin.setCompany(companyDao.getCompany(savingCompanyID));
			savingAdminPreferences = new AdminPreferencesImpl();
		} else {
			savingAdmin = adminDao.getAdmin(savingAdminID, savingCompanyID);

			if (passwordChanged(savingAdmin.getUsername(), form.getPassword())) {
				isPasswordChanged = true;
				savingAdmin.setLastPasswordChange(new Timestamp(new Date().getTime()));
			}

			savingAdminPreferences = adminPreferencesDao.getAdminPreferences(savingAdminID);
		}

		if (StringUtils.isNotBlank(form.getPassword())) {
			savingAdmin.setPasswordForStorage(form.getPassword());
		}
        logger.info("Username: {} PasswordLength: {}", form.getUsername(), form.getPassword().length());
        final String oldEmail = savingAdmin.getEmail(); // keep it before mapping from form
        final boolean isEmailChanged = savingAdmin.getAdminID() != 0 && !StringUtils.equals(oldEmail, form.getEmail()); // keep it before mapping from form

		List<AdminGroup> adminGroups = new ArrayList<>();
		for (int groupId : form.getGroupIDs()) {
			AdminGroup group = adminGroupDao.getAdminGroup(groupId, savingCompanyID);
			adminGroups.add(group);
		}
		savingAdmin.setGroups(adminGroups);
		map(savingAdmin, form, editorAdmin);

		final boolean passwordChanged = savingAdmin.getAdminID() != 0 && savingAdmin.getPasswordForStorage() != null;
		
		savingAdmin.setRestful(restfulUser);
		
		try {
			adminDao.save(savingAdmin);
		} catch (Exception e) {
			logger.error("Error saving admin", e);
			return AdminSavingResult.error(new Message("error.admin.save"));
		}

		final AdminPreferences formAdminPreferences = form.getAdminPreferences();

		savingAdminPreferences.setAdminID(savingAdmin.getAdminID());
		savingAdminPreferences.setMailingContentView(formAdminPreferences.getMailingContentView());
		savingAdminPreferences.setDashboardMailingsView(formAdminPreferences.getDashboardMailingsView());
		savingAdminPreferences.setMailingSettingsView(formAdminPreferences.getMailingSettingsView());
		savingAdminPreferences.setLivePreviewPosition(formAdminPreferences.getLivePreviewPosition());
		savingAdminPreferences.setStatisticLoadType(formAdminPreferences.getStatisticLoadType());

		adminPreferencesDao.save(savingAdminPreferences);

        logger.info("saveAdmin: admin {}", form.getAdminID());
		
		if (passwordChanged) {
			this.adminNotifier.notifyAdminAboutChangedPassword(savingAdmin);
		}

        if (isEmailChanged) {
            adminNotifier.notifyAdminAboutChangedEmail(savingAdmin, oldEmail);
        }

		return AdminSavingResult.success(savingAdmin, isPasswordChanged);
	}

    @Override
	public Tuple<List<String>, List<String>> saveAdminPermissions(int companyID, int savingAdminID, Collection<String> tokens, int editorAdminID) {
		tokens = CollectionUtils.emptyIfNull(tokens);
		Admin editorAdmin = adminDao.getAdmin(editorAdminID, companyID);
		Admin savingAdmin = adminDao.getAdmin(savingAdminID, companyID);

		Map<String, Boolean> permissionChangeable = new HashMap<>();
		
		Set<Permission> companyPermissions = companyDao.getCompanyPermissions(savingAdmin.getCompanyID());

		for (Permission permission : permissionService.getAllPermissions()) {
			String permissionName = permission.toString();
			boolean isChangeable;
			
			if (savingAdmin.permissionAllowedByGroups(permission)) {
				isChangeable = false;
			} else if (permission.getPermissionType() == PermissionType.Premium) {
				isChangeable = companyPermissions.contains(permission);
			} else if (permission.getPermissionType() == PermissionType.System) {
				isChangeable = companyPermissions.contains(permission) && (editorAdmin.permissionAllowed(permission) || editorAdmin.permissionAllowed(Permission.MASTER_SHOW) || editorAdmin.getAdminID() == 1);
			} else if (permission.getPermissionType() == PermissionType.Migration && !editorAdmin.permissionAllowed(Permission.SHOW_MIGRATION_PERMISSIONS)) {
				isChangeable = false;
			} else {
				isChangeable = true;
			}
			permissionChangeable.put(permissionName, isChangeable);
		}

		Set<String> adminPermissions = savingAdmin.getAdminPermissions().stream().map(Permission::getTokenString).collect(Collectors.toSet());

		List<String> addedPermissions = ListUtils.removeAll(tokens, adminPermissions);
		List<String> removedPermissions = ListUtils.removeAll(adminPermissions, tokens);

		for (int i = removedPermissions.size() - 1; i >= 0; i--) {
			String permissionToken = removedPermissions.get(i);
			if (!permissionChangeable.getOrDefault(permissionToken, false)) {
				Permission permission = Permission.getPermissionByToken(permissionToken);
				if (permission.getPermissionType() == PermissionType.System) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					removedPermissions.remove(permissionToken);
				} else if (permission.getPermissionType() == PermissionType.Migration) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					removedPermissions.remove(permissionToken);
				} else if (savingAdmin.permissionAllowedByGroups(permission)) {
					// Group permissions must be changed via group GUI, so they are visible but not disabled in admin GUI, so keep it unchanged
					removedPermissions.remove(permissionToken);
				} else if (permission.getPermissionType() == PermissionType.Premium && !companyPermissions.contains(permission)) {
					// Current users company does not have this right, but user to edit has it.
					// This happens only for the emm-master user, who makes changes in some foreign company
					// Just leave it unchanged
					removedPermissions.remove(permissionToken);
				} else {
					logger.error("Invalid right removal attempt for adminID " + savingAdminID + " by adminID " + editorAdmin.getAdminID() + ": " + permissionToken);
					return null;
				}
			}
		}

		for (int i = addedPermissions.size() - 1; i >= 0; i--) {
			String permissionToken = addedPermissions.get(i);
			if (!permissionChangeable.getOrDefault(permissionToken, false)) {
				Permission permission = Permission.getPermissionByToken(permissionToken);
				if (permission.getPermissionType() == PermissionType.System) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					addedPermissions.remove(permissionToken);
				} else if (permission.getPermissionType() == PermissionType.Migration) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					addedPermissions.remove(permissionToken);
				} else {
					logger.error("Invalid right granting attempt for adminID " + savingAdminID + " by adminID " + editorAdmin.getAdminID() + ": " + permissionToken);
					return null;
				}
			}
		}

		//Apply permissions changes
		adminPermissions.removeAll(removedPermissions);
		adminPermissions.addAll(addedPermissions);

		// Save new rights
		adminDao.saveAdminRights(savingAdminID, adminPermissions);

		return new Tuple<>(addedPermissions, removedPermissions);
	}

	@Override
	public void grantPermission(Admin admin, Permission permission) {
		// if permission was granted from another session, then just grant it to user object
		if (!adminDao.isPermissionGranted(admin.getAdminID(), permission)) {
			adminDao.grantPermission(admin.getAdminID(), permission);
		}
		admin.getAdminPermissions().add(permission);
	}

	@Override
	public void revokePermission(Admin admin, Permission permission) {
		adminDao.revokePermission(admin.getAdminID(), permission);
		admin.getAdminPermissions().remove(permission);
	}

	@Override
	public Admin getAdmin(int adminID, int companyID){
		return adminDao.getAdmin(adminID, companyID);
	}

	@Override
	public boolean adminExists(String username){
		return adminDao.adminExists(username) || adminDao.checkBlacklistedAdminNames(username);
	}
	
	@Override
	public boolean isGuiAdminLimitReached(int companyID) {
		return adminDao.getNumberOfGuiAdmins(companyID) >= configService.getIntegerValue(ConfigValue.UserAllowed, companyID);
	}

	@Override
	public PaginatedListImpl<AdminEntry> getAdminList(
			int companyID,
			String searchFirstName,
			String searchLastName,
			String searchEmail,
			String searchCompanyName,
			Integer filterCompanyId,
			Integer filterAdminGroupId,
			Integer filterMailinglistId,
			String filterLanguage,
			DateRange creationDate,
			DateRange lastLoginDate,
			String username,
			String sort,
			String direction,
			int pageNumber,
			int pageSize){
		return adminDao.getAdminList(companyID, searchFirstName, searchLastName, searchEmail, searchCompanyName, filterCompanyId, filterAdminGroupId, filterMailinglistId, filterLanguage, creationDate, lastLoginDate, username, sort, direction, pageNumber, pageSize, false);
	}

	@Override
	public PaginatedListImpl<AdminEntry> getList(int companyId, String sort, String dir, int pageNumber, int pageSize) {
		return adminDao.getList(companyId, sort, dir, pageNumber, pageSize);
	}

	@Override
	public List<AdminEntry> getAdminEntriesForUserActivityLog(Admin admin) {
		List<AdminEntry> admins = Collections.singletonList(new AdminEntryImpl(admin));

		if (admin.permissionAllowed(Permission.MASTERLOG_SHOW)) {
			admins = adminDao.getAllAdmins();
			admins.addAll(adminDao.getAllWsAdmins());
		} else if (admin.permissionAllowed(Permission.ADMINLOG_SHOW)) {
			admins = adminDao.getAllAdminsByCompanyId(admin.getCompanyID());
			admins.addAll(adminDao.getAllWsAdminsByCompanyId(admin.getCompanyID()));
		}
		return admins;
	}

	@Override
	public List<AdminEntry> getAdminEntriesForUserActivityLog(Admin admin, UserActivityLogService.UserType type) {
		boolean masterLogShowAllowed = admin.permissionAllowed(Permission.MASTERLOG_SHOW);
		boolean adminLogShowAllowed = admin.permissionAllowed(Permission.ADMINLOG_SHOW);

		if (!masterLogShowAllowed && !adminLogShowAllowed) {
			return List.of(new AdminEntryImpl(admin));
		}

		if (UserActivityLogService.UserType.SOAP.equals(type)) {
			return masterLogShowAllowed
					? adminDao.getAllWsAdmins()
					: adminDao.getAllWsAdminsByCompanyId(admin.getCompanyID());
		}

		boolean isRestful = UserActivityLogService.UserType.REST.equals(type);

		if (masterLogShowAllowed) {
			return adminDao.getAllAdmins(isRestful);
		}

		return adminDao.getAllAdminsByCompanyId(isRestful, admin.getCompanyID());
	}

	@Override
	public List<AdminEntry> findAllByEmailPart(String email, int companyID) {
		return adminDao.findAllByEmailPart(email, companyID);
	}

	@Override
	public List<AdminEntry> findAllByEmailPart(String email) {
		return adminDao.findAllByEmailPart(email);
	}

	@Override
	public AdminEntry findByEmail(String email, int companyId) {
		return adminDao.findByEmail(email, companyId);
	}

	@Override
	public ServiceResult<List<Admin>> getAllowedForDeletion(Set<Integer> ids, int companyID) {
		return bulkActionValidationService.checkAllowedForDeletion(ids, id -> isPossibleToDeleteAdmin(id, companyID));
	}

	@Override
	public List<Admin> delete(Set<Integer> ids, int companyId) {
		return getAllowedForDeletion(ids, companyId).getResult()
				.stream()
				.filter(a -> deleteAdmin(a.getAdminID(), a.getCompanyID()))
				.toList();
	}

	@Override
	public SimpleServiceResult delete(int id, int companyId) {
		ServiceResult<List<Admin>> deletionCheck = getAllowedForDeletion(Set.of(id), companyId);
		if (deletionCheck.isSuccess()) {
			deletionCheck.getResult().forEach(a -> deleteAdmin(a.getAdminID(), a.getCompanyID()));
		}

		return SimpleServiceResult.of(deletionCheck);
	}

	@Override
	public void updateEmail(String email, int id, int companyId) {
		adminDao.updateEmail(email, id, companyId);
	}

	@Override
	public List<AdminGroup> getAdminGroups(int companyID, Admin admin) {
		return adminGroupDao.getAdminGroupsByCompanyIdAndDefault(companyID, admin.getGroupIds());
	}
	
	@Override
	public List<Company> getCreatedCompanies(int companyID) {
		return companyDao.getCreatedCompanies(companyID);
	}

	@Override
	public boolean adminGroupExists(int companyId, String groupname) {
		return adminGroupDao.adminGroupExists(companyId, groupname);
	}
	
	private boolean passwordChanged(String username, String password) {
		Admin admin = adminDao.getAdminByLogin(username, password);
		return !(StringUtils.isEmpty(password) || (admin != null && admin.getAdminID() > 0));
	}
	
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}

	@Override
	public PasswordState getPasswordState(Admin admin) {
		int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, admin.getCompanyID());
		if (expirationDays <= 0) {
			// Expiration is disabled for company.
			return PasswordState.VALID;
		}

		// A password is valid for N days after its last change.
		Date expirationDate = DateUtils.addDays(admin.getLastPasswordChange(), expirationDays);

		if (DateUtilities.isPast(expirationDate)) {
			int expirationLockDays = configService.getIntegerValue(ConfigValue.UserPasswordFinalExpirationDays, admin.getCompanyID());

			// A password is still can be used to log in during N days after its expiration.
			Date expirationLockDate = DateUtils.addDays(expirationDate, expirationLockDays);

			if (DateUtilities.isPast(expirationLockDate)) {
				return PasswordState.EXPIRED_LOCKED;
			} else {
				return PasswordState.EXPIRED;
			}
		} else {
			int expirationWarningDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireNotificationDays, admin.getCompanyID());

			// User should get a warning message for N days before password is expired.
			Date expirationWarningDate = DateUtils.addDays(expirationDate, -expirationWarningDays);

			if (DateUtilities.isPast(expirationWarningDate)) {
				return PasswordState.EXPIRING;
			} else {
				return PasswordState.VALID;
			}
		}
	}

	@Override
	public Date computePasswordExpireDate(Admin admin) {
		int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, admin.getCompanyID());
		if (expirationDays <= 0) {
			// Expiration is disabled for company.
			return null;
		}

		return DateUtils.addDays(admin.getLastPasswordChange(), expirationDays);
	}

	@Override
	public boolean setPassword(int adminId, int companyId, String password) {
		Admin admin = adminDao.getAdmin(adminId, companyId);

		if (admin == null) {
			return false;
		}

		admin.setPasswordForStorage(password);
		admin.setLastPasswordChange(DateUtils.round(new Date(), Calendar.SECOND));

		adminDao.save(admin);
		this.adminNotifier.notifyAdminAboutChangedPassword(admin);

		return true;
	}

	@Override
	public void setDefaultPreferencesSettings(AdminPreferences preferences) {
		preferences.setDashboardMailingsView(PreferenceItem.DASHBOARD_MAILING.getDefaultValue());
		preferences.setLivePreviewPosition(PreferenceItem.MAILING_LIVE_PREVIEW.getDefaultValue());
		preferences.setMailingContentView(PreferenceItem.CONTENTBLOCKS.getDefaultValue());
		preferences.setMailingSettingsView(PreferenceItem.MAILING_SETTINGS.getDefaultValue());
		preferences.setStatisticLoadType(PreferenceItem.STATISTIC_LOADTYPE.getDefaultValue());
	}

	@Override
	public boolean checkBlacklistedAdminNames(String username) {
		return adminDao.checkBlacklistedAdminNames(username);
	}
    
    @Override
    public Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(Admin admin, Admin adminToEdit) {
		PermissionsOverviewData.Builder builder = PermissionsOverviewData.builder();
		builder.setAdmin(admin);
		builder.setAdminToEdit(adminToEdit);
		builder.setVisiblePermissions(permissionFilter.getAllVisiblePermissions());
		builder.setLicenseType(configService.getValue(ConfigValue.System_License_Type));
		builder.setCompanyPermissions(companyDao.getCompanyPermissions(adminToEdit.getCompanyID()));
		builder.setPermissionInfos(permissionService.getPermissionInfos());
		
		return builder.build().getPermissionsCategories();
    }

	@Override
	public AdminPreferences getAdminPreferences(final int adminId) {
		return adminPreferencesDao.getAdminPreferences(adminId);
	}

	@Override
	public List<EmmLayoutBase> getEmmLayoutsBase(Admin admin) {
		return emmLayoutBaseDao.getEmmLayoutsBase(admin.getCompanyID()).stream()
				.sorted(Comparator.comparingInt(emmLayoutBase -> emmLayoutBase.getThemeType().getCode()))
				.toList();
	}

	@Override
	public boolean isDarkmodeEnabled(final Admin admin) {
		final EmmLayoutBase layout = emmLayoutBaseDao.getEmmLayoutBase(admin.getCompanyID(), admin.getLayoutBaseID());
		return layout.getThemeType() == EmmLayoutBase.ThemeType.DARK || layout.getThemeType() == EmmLayoutBase.ThemeType.DARK_CONTRAST;
	}

	@Override
	public final List<AdminEntry> listAdminsByCompanyID(final int companyID) {
		return adminDao.getAllAdminsByCompanyIdOnly(companyID);
	}

	@Override
	public final boolean updateNewsDate(final int adminID, final Date newsDate, final NewsType type) {
		return adminDao.updateNewsDate(adminID, newsDate, type);
	}

	@Override
	public final Admin getOldestAdminOfCompany(final int companyId) {
		return adminDao.getOldestAdminOfCompany(companyId);
	}

	@Override
	public void save(Admin admin) {
		final boolean passwordChanged = admin.getAdminID() != 0 && admin.getPasswordForStorage() != null;
		
		adminDao.save(admin);
		
		if (passwordChanged) {
			adminNotifier.notifyAdminAboutChangedPassword(admin);
		}
	}

	@Override
	public boolean isAdminPassword(Admin admin, String password) {
		return adminDao.isAdminPassword(admin, password);
	}

	@Override
	public boolean isEnabled(Admin admin) {
		return adminDao.isEnabled(admin);
	}

	@Override
	public Admin getAdminByLogin(String name, String password) {
		return adminDao.getAdminByLogin(name, password);
	}

	protected void mapExtendedFields(final Admin target, final AdminForm sourceForm, final Admin editorAdmin) {
		logger.debug("Not supported for OpenEMM");
	}

	private void map(final Admin target, final AdminForm sourceForm, final Admin editorAdmin) {
		target.setUsername(sourceForm.getUsername());
		target.setFullname(sourceForm.getFullname());
		target.setFirstName(sourceForm.getFirstname());
		target.setEmployeeID(sourceForm.getEmployeeID());
		target.setAdminCountry(sourceForm.getAdminLocale().getCountry());
		target.setAdminLang(sourceForm.getAdminLocale().getLanguage());
		target.setAdminTimezone(sourceForm.getAdminTimezone());
		target.setStatEmail(sourceForm.getStatEmail());
		target.setCompanyName(sourceForm.getCompanyName());
		target.setEmail(sourceForm.getEmail());
		target.setLayoutBaseID(sourceForm.getLayoutBaseId());
		if (editorAdmin.isRedesignedUiUsed()) {
			target.setLayoutType(sourceForm.getUiLayoutType());
		}
		target.setGender(sourceForm.getGender());
		target.setTitle(sourceForm.getTitle());
		target.setAdminPhone(sourceForm.getAdminPhone());
        target.setPasswordReminderState(getPasswordReminderStateToSave(target, sourceForm));

		mapExtendedFields(target, sourceForm, editorAdmin);
	}

    private static PasswordReminderState getPasswordReminderStateToSave(Admin target, AdminForm form) {
        if (!form.isPasswordReminderEnabled()) {
            return PasswordReminderState.NOT_REQUIRED;
        }
        PasswordReminderState passwordReminderState = target.getPasswordReminderState();
        if (passwordReminderState == null || passwordReminderState == PasswordReminderState.NOT_REQUIRED) {
            return PasswordReminderState.REQUIRED;
        }
        return passwordReminderState;
    }

	@Override
	public int getAccessLimitTargetId(Admin admin) {
		return 0;
	}

	@Override
    public boolean isExtendedAltgEnabled(Admin admin) {
        return false;
    }

    @Override
	public int getAdminWelcomeMailingId(String language) {
		return adminDao.getAdminWelcomeMailingId(language);
	}

	@Override
	public int getPasswordResetMailingId(String language) {
		return adminDao.getPasswordResetMailingId(language);
	}

	@Override
	public int getPasswordChangedMailingId(String language) {
		return adminDao.getPasswordChangedMailingId(language);
	}

	@Override
	public int getSecurityCodeMailingId(String language) {
		return adminDao.getSecurityCodeMailingId(language);
	}

	@Override
	public int getOpenEmmDemoAccountWaitingMailingID(String language) {
		return adminDao.getOpenEmmDemoAccountWaitingMailingID(language);
	}

	@Override
	public int getOpenEmmDemoAccountDataMailingID(String language) {
		return adminDao.getOpenEmmDemoAccountDataMailingID(language);
	}
	
	@Override
	public PaginatedListImpl<AdminEntry> getRestfulUserList(
			int companyID,
			String searchFirstName,
			String searchLastName,
			String searchEmail,
			String searchCompanyName,
			Integer filterCompanyId,
			Integer filterAdminGroupId,
			Integer filterMailinglistId,
			String filterLanguage,
			DateRange creationDate,
			DateRange lastLoginDate,
			String username,
			String sort,
			String direction,
			int pageNumber,
			int pageSize){
		return adminDao.getAdminList(companyID, searchFirstName, searchLastName, searchEmail, searchCompanyName, filterCompanyId, filterAdminGroupId, filterMailinglistId, filterLanguage, creationDate, lastLoginDate, username, sort, direction, pageNumber, pageSize, true);
	}

	@Override
	public List<Admin> getAdmins(int companyID, boolean restful) {
		return adminDao.getAdmins(companyID, restful);
	}

	@Override
	public List<Integer> getAccessLimitingAdmins(int accessLimitingTargetGroupID) {
		return null;
	}

	@Override
	public int getNumberOfGuiAdmins(int companyID){
		return adminDao.getNumberOfGuiAdmins(companyID);
	}

	@Override
	public int getNumberOfRestfulUsers(int companyID) {
		return adminDao.getNumberOfRestfulUsers(companyID);
	}

	@Override
	public void deleteAdminPermissionsForCompany(int companyID) {
		adminDao.deleteAdminPermissionsForCompany(companyID);
	}

	@Override
	public List<Map<String, Object>> getAdminsLight(int companyID, boolean restful) {
		return adminDao.getAdminsLight(companyID, restful);
	}

	@Override
	public void saveDashboardLayout(String layout, Admin admin) {
		adminDao.saveDashboardLayout(layout, admin.getAdminID());
	}

	@Override
	public String getDashboardLayout(Admin admin) {
		return adminDao.getDashboardLayout(admin.getAdminID());
	}

    @Override
    public void warnAdminsAboutPasswordExpiration(CompaniesConstraints constraints) {
        getAdminsToWarnAboutPasswordExpiration(constraints)
                .forEach(admin -> adminNotifier.notifyAboutPasswordExpiration(admin));
    }
    
    private List<Admin> getAdminsToWarnAboutPasswordExpiration(CompaniesConstraints constraints) {
        return companyDao.getActiveCompanies(constraints).stream()
                .map(this::getAdminsToWarnAboutPasswordExpiration)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<Admin> getAdminsToWarnAboutPasswordExpiration(int companyId) {
        return getAdmins(companyId, false).stream()
                .filter(admin -> admin.getPasswordReminderState() == PasswordReminderState.REQUIRED)
                .filter(admin -> StringUtils.isNotBlank(admin.getEmail()))
                .filter(admin -> PasswordState.EXPIRING == getPasswordState(admin))
                .collect(Collectors.toList());
    }

    @Override
    public int getPasswordExpirationMailingId(String language) {
        return adminDao.getPasswordExpirationMailingId(language);
    }

    @Override
    public void setPasswordReminderState(int adminId, PasswordReminderState state) {
        adminDao.setPasswordReminderState(adminId, state);
    }

    @Override
    public int getEmailChangedMailingId(String language) {
        return adminDao.getEmailChangedMailingId(language);
    }
}
