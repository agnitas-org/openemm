/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.service.impl;

import static com.agnitas.emm.core.admin.web.PermissionsOverviewData.ROOT_ADMIN_ID;
import static com.agnitas.emm.core.admin.web.PermissionsOverviewData.ROOT_GROUP_ID;
import static com.agnitas.emm.core.usergroup.web.UserGroupController.NEW_USER_GROUP_ID;
import static com.agnitas.emm.core.usergroup.web.UserGroupController.ROOT_COMPANY_ID;
import static com.agnitas.util.Const.Mvc.ERROR_MSG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.emm.common.service.BulkActionValidationService;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionInfo;
import com.agnitas.emm.core.PermissionType;
import com.agnitas.emm.core.admin.service.PermissionFilter;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.permission.service.PermissionService;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter;
import com.agnitas.emm.core.usergroup.service.UserGroupService;
import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.ServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserGroupServiceImpl implements UserGroupService {

	private static final Logger logger = LogManager.getLogger(UserGroupServiceImpl.class);

	private static final int USER_FORM_NAME_MAX_LENGTH = 99;

	private final ConfigService configService;
	private final AdminGroupDao userGroupDao;
	private final CompanyService companyService;
	private final ExtendedConversionService conversionService;
	private final PermissionFilter permissionFilter;
	private final PermissionService permissionService;
	private final BulkActionValidationService<Integer, UserGroupDto> bulkActionValidationService;
	
	public UserGroupServiceImpl(
            ConfigService configService,
            AdminGroupDao userGroupDao,
            CompanyService companyService,
            ExtendedConversionService conversionService,
            PermissionFilter permissionFilter,
            PermissionService permissionService, BulkActionValidationService<Integer, UserGroupDto> bulkActionValidationService) {
		this.configService = configService;
		this.userGroupDao = userGroupDao;
		this.companyService = companyService;
		this.conversionService = conversionService;
		this.permissionFilter = permissionFilter;
		this.permissionService = permissionService;
        this.bulkActionValidationService = bulkActionValidationService;
    }

	@Override
	public PaginatedList<UserGroupDto> overview(UserGroupOverviewFilter filter) {
		return userGroupDao.getAdminGroupsByCompanyIdInclCreator(filter);
	}

	@Override
	public UserGroupDto getUserGroup(Admin admin, int userGroupId) {
		AdminGroup adminGroup = userGroupDao.getUserGroup(userGroupId, admin.getCompanyID());
		if (adminGroup == null) {
			return null;
		}

		LinkedHashMap<String, PermissionInfo> permissionInfos = permissionService.getPermissionInfos();
		UserGroupDto userGroupDto = conversionService.convert(adminGroup, UserGroupDto.class);
		userGroupDto.setUsersCount(userGroupDao.getUsersCount(userGroupId, admin.getCompanyID()));
		List<String> allowedPermissionsCategories = getUserGroupPermissionCategories(userGroupDto.getUserGroupId(), userGroupDto.getCompanyId(), admin);
		if (!allowedPermissionsCategories.isEmpty()) {
			Set<Permission> groupPermissions = adminGroup.getGroupPermissions();
			Set<Permission> companyPermissions = companyService.getCompanyPermissions(admin.getCompanyID());
			
			Set<String> permissionGranted = permissionService.getAllPermissions().stream()
					.filter(permission -> allowedPermissionsCategories.contains(permissionInfos.get(permission.getTokenString()).getCategory()))
					.filter(permission -> Permission.permissionAllowed(groupPermissions, companyPermissions, permission))
					.map(Permission::toString)
					.collect(Collectors.toSet());
			
			userGroupDto.setGrantedPermissions(permissionGranted);
		}

		return userGroupDto;
	}
	
	@Override
	public int saveUserGroup(Admin admin, UserGroupDto userGroupDto) {
		int userGroupId = userGroupDto.getUserGroupId();
	
		Set<String> groupPermissions = userGroupDao.getGroupPermissionsTokens(userGroupId);
		Set<Permission> companyPermissions = companyService.getCompanyPermissions(userGroupDto.getCompanyId());
		
		List<String> allPermissionCategories = getUserGroupPermissionCategories(userGroupId, userGroupDto.getCompanyId(), admin);
		Set<String> allChangeablePermissions = getAllChangeablePermissions(allPermissionCategories, companyPermissions, admin);
	
		Set<String> selectedPermissions = userGroupDto.getGrantedPermissions();
		
		List<String> addedPermissions = ListUtils.removeAll(selectedPermissions, groupPermissions);
		List<String> removedPermissions = ListUtils.removeAll(groupPermissions, selectedPermissions);

		for (String permissionToken : removedPermissions) {
			Permission permission = Permission.getPermissionByToken(permissionToken);
			if (!allChangeablePermissions.contains(permissionToken) && permission != null) {
				if (permission.getPermissionType() == PermissionType.System) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					selectedPermissions.add(permissionToken);
				} else if (permission.getPermissionType() == PermissionType.Premium && !companyPermissions.contains(permission)) {
					// Current users company does not have this right, but usergroup to edit has it.
					// This happens only for the emm-master user, who makes changes in some foreign company
					// Just leave it unchanged
					selectedPermissions.add(permissionToken);
				} else if (permission.getPermissionType() == PermissionType.Migration && !admin.permissionAllowed(Permission.SHOW_MIGRATION_PERMISSIONS)) {
					// Just leave it unchanged
					selectedPermissions.add(permissionToken);
				} else {
					logger.error("Invalid right removal attempt for adminGroupID {} by adminID {}: {}", userGroupId, admin.getAdminID(), permissionToken);
					return -1;
				}
			}
		}

		for (String permissionToken : addedPermissions) {
			if (!allChangeablePermissions.contains(permissionToken)) {
				Permission permission = Permission.getPermissionByToken(permissionToken);
				if (permission.getPermissionType() == PermissionType.System) {
					// User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
					selectedPermissions.remove(permissionToken);
				} else {
					logger.error("Invalid right granting attempt for adminGroupID {} by adminID {}: {}", userGroupId, admin.getAdminID(), permissionToken);
					return -1;
				}
			}
		}
		
		userGroupDto.setGrantedPermissions(selectedPermissions);
		AdminGroup adminGroup = conversionService.convert(userGroupDto, AdminGroup.class);
		return userGroupDao.saveAdminGroup(adminGroup);
	}
	
	@Override
	public boolean isShortnameUnique(String newShortname, int userGroupId, int companyId) {
		AdminGroup userGroup = userGroupDao.getAdminGroup(userGroupId, companyId);
		String oldShortname = userGroup != null ? userGroup.getShortname() : "";
		
		return StringUtils.equals(oldShortname, newShortname)
				|| !userGroupDao.adminGroupExists(companyId, newShortname);
	}
	
	private List<String> getUserGroupPermissionCategories(int groupId, int groupCompanyId, Admin admin) {
		if (groupId == ROOT_GROUP_ID || groupCompanyId == admin.getCompanyID() || groupCompanyId == admin.getCompany().getCreatorID() || admin.getAdminID() == ROOT_ADMIN_ID) {
			List<String> permissionCategories = new ArrayList<>(permissionService.getAllCategoriesOrdered());
	
			if (!admin.permissionAllowed(Permission.MASTER_SHOW) && admin.getAdminID() != ROOT_ADMIN_ID) {
				permissionCategories.remove(Permission.CATEGORY_KEY_SYSTEM);
			}
			
			return permissionCategories;
		} else {
			return new ArrayList<>();
		}
	}
	
	private boolean isUserGroupPermissionChangeable(Admin admin, Permission permission, Set<Permission> companyPermissions) {
		if (permission.getPermissionType() == PermissionType.Premium) {
			return companyPermissions.contains(permission);
		} else if (permission.getPermissionType() == PermissionType.System) {
			return companyPermissions.contains(permission)
					&& (admin.permissionAllowed(permission) ||
						admin.permissionAllowed(Permission.MASTER_SHOW) ||
						admin.getAdminID() == ROOT_ADMIN_ID);
		} else if (permission.getPermissionType() == PermissionType.Migration && !admin.permissionAllowed(Permission.SHOW_MIGRATION_PERMISSIONS)) {
			return false;
		} else {
			return true;
		}
	}
	
	protected List<String> getAdminNamesOfGroup(int userGroupId, int companyId) {
		if (userGroupId > -1 && companyId > 0) {
			return userGroupDao.getAdminsOfGroup(companyId, userGroupId);
		} else {
			return new ArrayList<>();
		}
	}
	
	private List<String> getGroupNamesUsingGroup(int userGroupId, int companyId) {
		if (userGroupId > -1 && companyId > 0) {
			return userGroupDao.getGroupNamesUsingGroup(companyId, userGroupId);
		} else {
			return new ArrayList<>();
		}
	}
	
	@Override
	public Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(Admin admin, int groupId, int groupCompanyId) {
		PermissionsOverviewData.Builder builder = PermissionsOverviewData.builder();
		builder.setGroupPermissions(true);
		builder.setAllowedForGroup(groupId == ROOT_GROUP_ID ||
				groupCompanyId == admin.getCompanyID() ||
				groupCompanyId == admin.getCompany().getCreatorID() ||
				admin.getAdminID() == ROOT_ADMIN_ID);
	
		builder.setAdmin(admin);
		builder.setGroupToEdit(userGroupDao.getAdminGroup(groupId, groupCompanyId));
		builder.setCompanyPermissions(companyService.getCompanyPermissions(groupCompanyId));
		builder.setVisiblePermissions(permissionFilter.getAllVisiblePermissions());
		builder.setPermissionInfos(permissionService.getPermissionInfos());
		builder.setLicenseType(configService.getValue(ConfigValue.System_License_Type));
	
		return builder.build().getPermissionsCategories();
	}
	
	private Set<String> getAllChangeablePermissions(List<String> permissionCategories, Set<Permission> companyPermissions, Admin admin) {
		LinkedHashMap<String, PermissionInfo> permissionInfos = permissionService.getPermissionInfos();
		return permissionService.getAllPermissions().stream()
				.filter(permission -> permissionCategories.contains(permissionInfos.get(permission.getTokenString()).getCategory()))
				.filter(permission -> isUserGroupPermissionChangeable(admin, permission, companyPermissions))
				.map(Permission::toString)
				.collect(Collectors.toSet());
	}

	@Override
	public Collection<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyID, AdminGroup adminGroup) {
		return userGroupDao.getAdminGroupsByCompanyIdAndDefault(companyID, adminGroup != null ? adminGroup.getParentGroupIds() : null);
	}

	@Override
	public AdminGroup getAdminGroup(int userGroupId, int companyID) {
		return userGroupDao.getAdminGroup(userGroupId, companyID);
	}

	@Override
	public int copyUserGroup(int id, Admin admin) {
		UserGroupDto group = getUserGroup(admin, id);
		if (group == null) {
			throw new IllegalArgumentException("userGroup == null (invalid id)");
		}

		group.setUserGroupId(NEW_USER_GROUP_ID);
		group.setShortname(generateUserGroupCopyName(group.getShortname(), admin.getCompanyID(), admin.getLocale()));
		group.setCompanyId(admin.getCompanyID());
		return saveUserGroup(admin, group);
	}

	@Override
	public ServiceResult<List<UserGroupDto>> getAllowedGroupsForDeletion(Set<Integer> ids, Admin admin) {
		return bulkActionValidationService.checkAllowedForDeletion(ids, id -> getUserGroupForDeletion(id, admin));
	}

	@Override
	public List<UserGroupDto> markDeleted(Set<Integer> bulkIds, Admin admin) {
		List<UserGroupDto> allowedGroups = getAllowedGroupsForDeletion(bulkIds, admin).getResult();
		allowedGroups.forEach(g -> userGroupDao.markDeleted(g.getUserGroupId(), g.getCompanyId()));
		return allowedGroups;
	}

	@Override
	public void restore(Set<Integer> ids, int companyId) {
		userGroupDao.restore(ids, companyId);
	}

	private ServiceResult<UserGroupDto> getUserGroupForDeletion(int id, Admin admin) {
		int adminCompanyId = admin.getCompanyID();
		UserGroupDto userGroup = getUserGroup(admin, id);
		if (userGroup == null || userGroup.getUserGroupId() <= NEW_USER_GROUP_ID) {
			return ServiceResult.errorKeys("error.general.missing");
		}

		if (userGroup.getCompanyId() != adminCompanyId && adminCompanyId != ROOT_COMPANY_ID) {
			return ServiceResult.errorKeys(ERROR_MSG);
		}

		List<String> adminNames = getAdminNamesOfGroup(id, adminCompanyId);
		List<String> groupNames = getGroupNamesUsingGroup(id, adminCompanyId);

		if (CollectionUtils.isNotEmpty(adminNames)) {
			String adminNamesString = StringUtils.abbreviate(StringUtils.join(adminNames, ", "), 64);
			return ServiceResult.error(Message.of("error.group.delete.hasAdmins", adminNamesString));
		}
		if (CollectionUtils.isNotEmpty(groupNames)) {
			String groupNamesString = StringUtils.abbreviate(StringUtils.join(groupNames, ", "), 64);
			return ServiceResult.error(Message.of("error.group.delete.hasGroups", groupNamesString));
		}

		return ServiceResult.success(userGroup);
	}

	private String generateUserGroupCopyName(String name, int companyId, Locale locale) {
		return AgnUtils.getUniqueCloneName(name, I18nString.getLocaleString("mailing.CopyOf", locale) + " ",
				USER_FORM_NAME_MAX_LENGTH, newName -> userGroupDao.adminGroupExists(companyId, newName));
	}

	@Override
	public void removeMarkedAsDeletedBefore(Date date, int companyId) {
		userGroupDao.getMarkedAsDeletedBefore(date, companyId)
			.forEach(id -> userGroupDao.delete(companyId, id));
	}
}
