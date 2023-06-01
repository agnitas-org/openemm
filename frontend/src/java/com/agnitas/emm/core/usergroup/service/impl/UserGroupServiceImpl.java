/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.service.impl;

import static com.agnitas.emm.core.admin.web.PermissionsOverviewData.ROOT_ADMIN_ID;
import static com.agnitas.emm.core.admin.web.PermissionsOverviewData.ROOT_GROUP_ID;
import static com.agnitas.emm.core.usergroup.web.UserGroupController.NEW_USER_GROUP_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.Admin;
import com.agnitas.dao.AdminGroupDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionInfo;
import com.agnitas.emm.core.PermissionType;
import com.agnitas.emm.core.admin.service.PermissionFilter;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.company.service.ComCompanyService;
import com.agnitas.emm.core.permission.service.PermissionService;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.service.UserGroupService;
import com.agnitas.messages.I18nString;
import com.agnitas.service.ExtendedConversionService;

public class UserGroupServiceImpl implements UserGroupService {
    
    private static final Logger logger = LogManager.getLogger(UserGroupServiceImpl.class);

    private static final int USER_FORM_NAME_MAX_LENGTH = 99;

    private AdminGroupDao userGroupDao;
    private ComCompanyService companyService;
    private ExtendedConversionService conversionService;
    private PermissionFilter permissionFilter;
	protected PermissionService permissionService;
	
	@Required
	public void setUserGroupDao(AdminGroupDao userGroupDao) {
		this.userGroupDao = userGroupDao;
	}
	
	@Required
	public void setCompanyService(ComCompanyService companyService) {
		this.companyService = companyService;
	}
	
	@Required
	public void setConversionService(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	@Required
	public void setPermissionFilter(PermissionFilter permissionFilter) {
		this.permissionFilter = permissionFilter;
	}
	
	@Required
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}
    
    @Override
    public PaginatedListImpl<UserGroupDto> getUserGroupPaginatedList(Admin admin, String sort, String sortDirection, int page, int rownumber) {
        PaginatedListImpl<AdminGroup> userGroups = userGroupDao.getAdminGroupsByCompanyIdInclCreator(admin.getCompanyID(), admin.getAdminID(), sort, sortDirection, page, rownumber);
		return conversionService.convertPaginatedList(userGroups, AdminGroup.class, UserGroupDto.class);
    }
    
    @Override
    public UserGroupDto getUserGroup(Admin admin, int userGroupId) {
        AdminGroup adminGroup = userGroupDao.getAdminGroup(userGroupId, admin.getCompanyID());
        if (adminGroup == null) {
            return null;
        }

        LinkedHashMap<String, PermissionInfo> permissionInfos = permissionService.getPermissionInfos();
        UserGroupDto userGroupDto = conversionService.convert(adminGroup, UserGroupDto.class);
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
    public int saveUserGroup(Admin admin, UserGroupDto userGroupDto) throws Exception {
        int userGroupId = userGroupDto.getUserGroupId();
    
        Set<String> groupPermissions = userGroupDao.getGroupPermissionsTokens(userGroupId);
        Set<Permission> companyPermissions = companyService.getCompanyPermissions(userGroupDto.getCompanyId());
        
        List<String> allPermissionCategories = getUserGroupPermissionCategories(userGroupId, userGroupDto.getCompanyId(), admin);
        Set<String> allChangeablePermissions = getAllChangeablePermissions(allPermissionCategories, companyPermissions, admin);
    
        Set<String> selectedPermissions = userGroupDto.getGrantedPermissions();
        
        List<String> addedPermissions = ListUtils.removeAll(selectedPermissions, groupPermissions);
        List<String> removedPermissions = ListUtils.removeAll(groupPermissions, selectedPermissions);

        for (String permissionToken: removedPermissions) {
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
                } else {
                    logger.error("Invalid right removal attempt for adminGroupID " + userGroupId + " by adminID " + admin.getAdminID() + ": " + permissionToken);
                    return -1;
                }
            }
        }

        LinkedHashMap<String, PermissionInfo> permissionInfos = permissionService.getPermissionInfos();
        for (String permissionToken: addedPermissions) {
            if (!allChangeablePermissions.contains(permissionToken)) {
                Permission permission = Permission.getPermissionByToken(permissionToken);
                String category = permissionInfos.get(permission.getTokenString()).getCategory();
                if ((StringUtils.equals(Permission.CATEGORY_KEY_SYSTEM, category))) {
                    // User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
                    selectedPermissions.remove(permissionToken);
                } else {
                    logger.error("Invalid right granting attempt for adminGroupID " + userGroupId + " by adminID " + admin.getAdminID() + ": " + permissionToken);
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
    
    @Override
    public List<String> getUserGroupPermissionCategories(int groupId, int groupCompanyId, Admin admin) {
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
    
    @Override
    public boolean isUserGroupPermissionChangeable(Admin admin, Permission permission, Set<Permission> companyPermissions) {
        if (permission.getPermissionType() == PermissionType.Premium) {
            return companyPermissions.contains(permission);
        } else if (permission.getPermissionType() == PermissionType.System) {
            return companyPermissions.contains(permission)
            		&& (admin.permissionAllowed(permission) ||
	                    admin.permissionAllowed(Permission.MASTER_SHOW) ||
	                    admin.getAdminID() == ROOT_ADMIN_ID);
        } else {
        	return true;
        }
    }
    
    @Override
    public List<String> getAdminNamesOfGroup(int userGroupId, int companyId) {
        if (userGroupId > -1 && companyId > 0) {
            return userGroupDao.getAdminsOfGroup(companyId, userGroupId);
        } else {
        	return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> getGroupNamesUsingGroup(int userGroupId, int companyId) {
        if (userGroupId > -1 && companyId > 0) {
        	return userGroupDao.getGroupNamesUsingGroup(companyId, userGroupId);
        } else {
        	return new ArrayList<>();
        }
    }
    
    @Override
    public boolean deleteUserGroup(int userGroupId, Admin admin) {
        int companyId = admin.getCompanyID();
        AdminGroup adminGroup = userGroupDao.getAdminGroup(userGroupId, companyId);
        if (adminGroup == null || (adminGroup.getCompanyID() != companyId && admin.getAdminID() != ROOT_ADMIN_ID)) {
            return false;
        }
        
        return userGroupDao.delete(adminGroup.getCompanyID(), userGroupId) > 0;
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
	public List<AdminGroup> getAdminGroupsByCompanyId(int companyID) {
		return userGroupDao.getAdminGroupsByCompanyId(companyID);
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
    public int copyUserGroup(final int id, final Admin admin) throws Exception {
        UserGroupDto group = getUserGroup(admin, id);
        if (group == null) {
            throw new IllegalArgumentException("userGroup == null (invalid id)");
        }

        group.setUserGroupId(NEW_USER_GROUP_ID);
        group.setShortname(generateUserGroupCopyName(group.getShortname(), admin.getCompanyID(), admin.getLocale()));
        group.setCompanyId(admin.getCompanyID());
        return saveUserGroup(admin, group);
    }

    private String generateUserGroupCopyName(String name, int companyId, Locale locale) {
        return AgnUtils.getUniqueCloneName(name, I18nString.getLocaleString("mailing.CopyOf", locale) + " ",
                USER_FORM_NAME_MAX_LENGTH, newName -> userGroupDao.adminGroupExists(companyId, newName));
    }
}
