/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.AdminEntryImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionMessage;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.ComAdminPreferences;
import com.agnitas.beans.ComCompany;
import com.agnitas.beans.impl.ComAdminImpl;
import com.agnitas.beans.impl.ComAdminPreferencesImpl;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.dao.ComAdminPreferencesDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.AdminException;
import com.agnitas.emm.core.admin.service.AdminSavingResult;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordState;
import com.agnitas.emm.core.supervisor.beans.Supervisor;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.common.SupervisorLoginFailedException;
import com.agnitas.emm.core.supervisor.dao.ComSupervisorDao;
import com.agnitas.emm.core.supervisor.dao.GrantedSupervisorLoginDao;
import com.agnitas.web.ComAdminForm;

public class AdminServiceImpl implements AdminService {
	private static final transient Logger logger = Logger.getLogger(AdminServiceImpl.class);

	protected ComAdminDao adminDao;
	protected ComSupervisorDao supervisorDao;
	protected ComCompanyDao companyDao;
	protected ComAdminPreferencesDao adminPreferencesDao;
	protected ComAdminGroupDao adminGroupDao;
	protected GrantedSupervisorLoginDao grantedSupervisorLoginDao;
	protected ConfigService configService;

	@Required
	public void setAdminDao(ComAdminDao adminDao) {
		this.adminDao = adminDao;
	}

	@Required
	public void setSupervisorDao(ComSupervisorDao dao) {
		this.supervisorDao = dao;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setAdminPreferencesDao(ComAdminPreferencesDao adminPreferencesDao) {
		this.adminPreferencesDao = adminPreferencesDao;
	}

	@Required
	public void setAdminGroupDao(ComAdminGroupDao adminGroupDao) {
		this.adminGroupDao = adminGroupDao;
	}
	
	/**
	 * Sets DAO for handling granted supervisor logins.
	 * 
	 * @param dao DAO for handling granted supervisor logins
	 */
	@Required
	public final void setGrantedSupervisorLoginDao(final GrantedSupervisorLoginDao dao) {
		this.grantedSupervisorLoginDao = Objects.requireNonNull(dao, "DAO for granted supervisor logins cannot be null");
	}

	// ----------------------------------------------------------------------------------------------------------------

	@Override
	public ComAdmin getAdminByNameForSupervisor(final String username, final String supervisorName, final String password) throws AdminException, SupervisorException {
		final Supervisor supervisor = supervisorDao.getSupervisor(supervisorName, password);

		// Check if supervisor exists
		if (supervisor == null) {
			if (logger.isInfoEnabled()) {
				logger.info("Invalid supervisor " + supervisorName + "?");
			}
			
			final String msg = String.format("Unknown supervisor '%s'", supervisorName);
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new SupervisorLoginFailedException(username, supervisorName);
		}

		final ComAdmin admin = adminDao.getByNameAndActiveCompany(username);
		
		// Check if admin exists
		if (admin == null) {
			final String msg = String.format("Unknown supervisor '%s'", username);
			
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new AdminException(msg);
		}
			
		// Check, if supervisor is allowed to login to copmany of given admin
		if (!isSupervisorLoginAllowedForCompany(admin, supervisor)) {
			final String msg = String.format("Access for supervisor '%s' not granted for company ID %d", supervisorName, admin.getCompanyID());
			
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
			
			throw new SupervisorException(msg);
		}
		
		// Check that supervisor has permission to login as EMM user
		if (this.configService.getBooleanValue(ConfigValue.SupervisorRequiresLoginPermission, admin.getCompanyID()) && !this.grantedSupervisorLoginDao.isSupervisorLoginGranted(supervisor.getId(), admin)) {
			final String msg = String.format("Supervisor '%s' has not permission to login as user '%s'", supervisor.getSupervisorName(), admin.getUsername());
			
			if (logger.isInfoEnabled()) {
				logger.info(msg);
			}
				
			throw new SupervisorException(msg);
		}
		
		// Assign supervisor to EMM user
		admin.setSupervisor(supervisor);
		
		// Log supervisor access
		supervisorDao.logSupervisorLogin(supervisor.getId(), admin.getCompanyID());

		return admin;
		
	}

	private final boolean isSupervisorLoginAllowedForCompany(final ComAdmin admin, final Supervisor supervisor) {
		final List<Integer> allowedCompanyIDs = supervisorDao.getAllowedCompanyIDs(supervisor.getId());
		
		return allowedCompanyIDs.contains(0) || allowedCompanyIDs.contains(admin.getCompanyID());
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
	public Supervisor getSupervisor(String supervisorName) {
		return supervisorDao.getSupervisor(supervisorName);
	}

	@Override
	public List<Tuple<Integer, String>> getAdminsUsernames(int companyID) {
		return adminDao.getAdminsUsernames(companyID);
	}

	@Override
	public Map<Integer, String> getAdminsNamesMap(@VelocityCheck int companyId) {
		return adminDao.getAdminsNamesMap(companyId);
	}

	@Override
	public void deleteAdmin(int companyID, int deletingAdminID, int editorAdminID) {
		ComAdmin admin = adminDao.getAdmin(deletingAdminID, companyID);

		if (admin != null) {
			adminDao.delete(admin);
			adminPreferencesDao.delete(deletingAdminID);

			if (logger.isInfoEnabled()) {
				logger.info("Admin " + deletingAdminID + " deleted");
			}
		}
	}

	@Override
	public AdminSavingResult saveAdmin(ComAdminForm form, ComAdmin editorAdmin) {
		int savingAdminID = form.getAdminID();
		int savingCompanyID = form.getCompanyID();
		int savingGroupID = form.getGroupID();
		int editorCompanyID = editorAdmin.getCompanyID();

		if (savingCompanyID != editorCompanyID) {
			List<ComCompany> allowedCompanies = companyDao.getCreatedCompanies(editorCompanyID);

			boolean validCompany = allowedCompanies.stream().anyMatch(comCompany -> comCompany.getId() == savingCompanyID);

			if (!validCompany) {
				return AdminSavingResult.error(new ActionMessage("error.permissionDenied"));
			}
		}

		ComAdmin savingAdmin;
		ComAdminPreferences savingAdminPreferences;
		boolean isNew = savingAdminID == 0;
		boolean isPasswordChanged = false;

		if (isNew) {
			savingAdmin = new ComAdminImpl();
			savingAdmin.setCompany(companyDao.getCompany(savingCompanyID));
			savingAdminPreferences = new ComAdminPreferencesImpl();
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

		AdminGroup group = adminGroupDao.getAdminGroup(savingGroupID);

		if (logger.isInfoEnabled()) {
			logger.info("Username: " + form.getUsername() + " PasswordLength: " + form.getPassword().length());
		}

		savingAdmin.setUsername(form.getUsername());
		savingAdmin.setFullname(form.getFullname());
		savingAdmin.setFirstName(form.getFirstname());
		savingAdmin.setAdminCountry(form.getAdminLocale().getCountry());
		savingAdmin.setAdminLang(form.getAdminLocale().getLanguage());
		savingAdmin.setAdminTimezone(form.getAdminTimezone());
		savingAdmin.setGroup(group);
		savingAdmin.setStatEmail(form.getStatEmail());
		savingAdmin.setCompanyName(form.getCompanyName());
		savingAdmin.setEmail(form.getEmail());
		savingAdmin.setLayoutBaseID(form.getLayoutBaseId());
		savingAdmin.setGender(form.getGender());
		savingAdmin.setTitle(form.getTitle());
		savingAdmin.setOneTimePassword(form.isOneTimePassword());
		savingAdmin.setAdminPhone(form.getAdminPhone());

		savingAdminPreferences.setStartPage(form.getStartPage());
		savingAdminPreferences.setMailingContentView(form.getMailingContentView());
		savingAdminPreferences.setDashboardMailingsView(form.getDashboardMailingsView());
		savingAdminPreferences.setNavigationLocation(form.getNavigationLocation());
		savingAdminPreferences.setMailingSettingsView(form.getMailingSettingsView());
		savingAdminPreferences.setLivePreviewPosition(form.getLivePreviewPosition());
		savingAdminPreferences.setStatisticLoadType(form.getStatisticLoadType());

		try {
			adminDao.save(savingAdmin);
		} catch (Exception e) {
			logger.error("Error saving admin", e);
			return AdminSavingResult.error(new ActionMessage("error.admin.save"));
		}

		adminPreferencesDao.save(savingAdminPreferences);

		if (logger.isInfoEnabled()) {
			logger.info("saveAdmin: admin " + form.getAdminID());
		}

		return AdminSavingResult.success(savingAdmin, isPasswordChanged);
	}

	@Override
	public Tuple<List<String>, List<String>> saveAdminPermissions(int companyID, int savingAdminID, Collection<String> tokens, int editorAdminID) {
		ComAdmin editorAdmin = adminDao.getAdmin(editorAdminID, companyID);
		ComAdmin savingAdmin = adminDao.getAdmin(savingAdminID, companyID);

		List<String> permissionCategories = new ArrayList<>();
		Map<String, Boolean> permissionChangeable = new HashMap<>();

		List<String> standardCategories = Arrays.asList(Permission.ORDERED_STANDARD_RIGHT_CATEGORIES);
		List<String> premiumCategories = Arrays.asList(Permission.ORDERED_PREMIUM_RIGHT_CATEGORIES);
		permissionCategories.addAll(standardCategories);
		permissionCategories.addAll(premiumCategories);
		if (editorAdmin.permissionAllowed(Permission.MASTER_SHOW) || editorAdmin.getAdminID() == 1) {
			permissionCategories.add(Permission.CATEGORY_KEY_SYSTEM);
			permissionCategories.add(Permission.CATEGORY_KEY_OTHERS);
		}
		
		Set<Permission> companyPermissions = companyDao.getCompanyPermissions(savingAdmin.getCompanyID());

		for (Map.Entry<Permission, String> permissionEntry : Permission.getAllPermissionsAndCategories().entrySet()) {
			String categoryName = permissionEntry.getValue();
			if (permissionCategories.contains(categoryName)) {
				Permission permission = permissionEntry.getKey();
				String permissionName = permission.toString();
				boolean isChangeable;
				if (savingAdmin.getGroup().permissionAllowed(permission)) {
					isChangeable = false;
				} else if (standardCategories.contains(categoryName)) {
					isChangeable = true;
				} else if (premiumCategories.contains(categoryName)) {
					isChangeable = companyPermissions.contains(permission);
				} else {
					isChangeable = editorAdmin.permissionAllowed(permission) || editorAdmin.permissionAllowed(Permission.MASTER_SHOW) || editorAdmin.getAdminID() == 1;
				}
				permissionChangeable.put(permissionName, isChangeable);
			}
		}

		Set<String> adminPermissions = savingAdmin.getAdminPermissions().stream().map(Permission::getTokenString).collect(Collectors.toSet());

		List<String> addedPermissions = ListUtils.removeAll(tokens, adminPermissions);
		List<String> removedPermissions = ListUtils.removeAll(adminPermissions, tokens);

		for (int i = removedPermissions.size() - 1; i >= 0; i--) {
			String permissionToken = removedPermissions.get(i);
			if (!permissionChangeable.getOrDefault(permissionToken, false)) {
				Permission permission = Permission.getPermissionByToken(permissionToken);
				String category = permission.getCategory();
				if (Permission.CATEGORY_KEY_SYSTEM.equals(category) || Permission.CATEGORY_KEY_OTHERS.equals(category)) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					removedPermissions.remove(permissionToken);
				} else if (savingAdmin.getGroup().permissionAllowed(permission)) {
					// Group permissions must be changed via group GUI, so they are visible but not disabled in admin GUI, so keep it unchanged
					removedPermissions.remove(permissionToken);
				} else if (premiumCategories.contains(category) && !companyPermissions.contains(permission)) {
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
				String category = permission.getCategory();
				if (Permission.CATEGORY_KEY_SYSTEM.equals(category) || Permission.CATEGORY_KEY_OTHERS.equals(category)) {
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
	public ComAdmin getAdmin(int adminID, int companyID){
		return adminDao.getAdmin(adminID, companyID);
	}

	@Override
	public int getNumberOfAdmins(){
		return adminDao.getNumberOfAdmins();
	}

	@Override
	public boolean adminExists(String username){
		return adminDao.adminExists(username);
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
			String sort,
			String direction,
			int pageNumber,
			int pageSize){
		return adminDao.getAdminList(companyID, searchFirstName, searchLastName, searchEmail, searchCompanyName, filterCompanyId, filterAdminGroupId, filterMailinglistId, filterLanguage, sort, direction, pageNumber, pageSize);
	}
	
	@Override
	public List<AdminEntry> getAdminEntriesForUserActivityLog(ComAdmin admin) {
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
	public List<AdminGroup> getAdminGroups(@VelocityCheck int companyID) {
		return adminGroupDao.getAdminGroupsByCompanyIdAndDefault(companyID);
	}
	
	@Override
	public List<ComCompany> getCreatedCompanies(@VelocityCheck int companyID) {
		return companyDao.getCreatedCompanies(companyID);
	}

	@Override
	public int adminGroupExists(@VelocityCheck int companyId, String groupname) {
		return adminGroupDao.adminGroupExists(companyId, groupname);
	}
	
	private boolean passwordChanged(String username, String password) {
		ComAdmin admin = adminDao.getAdminByLogin(username, password);
		return !(StringUtils.isEmpty(password) || (admin != null && admin.getAdminID() > 0));
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}

	@Override
	public PasswordState getPasswordState(ComAdmin admin) {
		if (admin.isOneTimePassword()) {
			// One-time password must be changed immediately upon log on.
			return PasswordState.ONE_TIME;
		}

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
	public Date computePasswordExpireDate(ComAdmin admin) {
		// Password gets expired once user logs in.
		if (admin.isOneTimePassword()) {
			return null;
		}

		int expirationDays = configService.getIntegerValue(ConfigValue.UserPasswordExpireDays, admin.getCompanyID());
		if (expirationDays <= 0) {
			// Expiration is disabled for company.
			return null;
		}

		return DateUtils.addDays(admin.getLastPasswordChange(), expirationDays);
	}

	@Override
	public boolean setPassword(int adminId, @VelocityCheck int companyId, String password) {
		ComAdmin admin = adminDao.getAdmin(adminId, companyId);

		if (admin == null) {
			return false;
		}

		admin.setPasswordForStorage(password);
		admin.setLastPasswordChange(DateUtils.round(new Date(), Calendar.SECOND));
		admin.setOneTimePassword(false);

		try {
			adminDao.save(admin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return true;
	}
}
