/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.service.impl;

import static com.agnitas.emm.core.admin.web.PermissionsOverviewData.ROOT_ADMIN_ID;
import static com.agnitas.emm.core.admin.web.PermissionsOverviewData.ROOT_GROUP_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComAdminGroupDao;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.PermissionFilter;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.company.service.ComCompanyService;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.service.UserGroupService;
import com.agnitas.service.ExtendedConversionService;

@Service("UserGroupService")
public class UserGroupServiceImpl implements UserGroupService {
    
    private static final Logger logger = Logger.getLogger(UserGroupServiceImpl.class);

    private ComAdminGroupDao userGroupDao;
    
    private ComCompanyService companyService;
    
    private ExtendedConversionService conversionService;
    
    private PermissionFilter permissionFilter;
    
    public UserGroupServiceImpl(ComAdminGroupDao userGroupDao, ComCompanyService companyService, ExtendedConversionService conversionService, PermissionFilter permissionFilter) {
        this.userGroupDao = userGroupDao;
        this.companyService = companyService;
        this.conversionService = conversionService;
        this.permissionFilter = permissionFilter;
    }
    
    @Override
    public PaginatedListImpl<UserGroupDto> getUserGroupPaginatedList(ComAdmin admin, String sort, String sortDirection, int page, int rownumber) {
        PaginatedListImpl<AdminGroup> userGroups = userGroupDao.getAdminGroupsByCompanyIdInclCreator(admin.getCompanyID(), admin.getAdminID(), sort, sortDirection, page, rownumber);
		return conversionService.convertPaginatedList(userGroups, AdminGroup.class, UserGroupDto.class);
    }
    
    @Override
    public UserGroupDto getUserGroup(ComAdmin admin, int userGroupId) {
        AdminGroup adminGroup = userGroupDao.getAdminGroup(userGroupId, admin.getCompanyID());
        if(adminGroup == null) {
            return null;
        }
        
        UserGroupDto userGroupDto = conversionService.convert(adminGroup, UserGroupDto.class);
        List<String> allowedPermissionsCategories = getUserGroupPermissionCategories(userGroupDto.getUserGroupId(), userGroupDto.getCompanyId(), admin);
        if(!allowedPermissionsCategories.isEmpty()) {
            Set<Permission> groupPermissions = adminGroup.getGroupPermissions();
            Set<Permission> companyPermissions = companyService.getCompanyPermissions(admin.getCompanyID());
            
            Set<String> permissionGranted = Permission.getAllPermissionsAndCategories().keySet().stream()
                    .filter(permission -> allowedPermissionsCategories.contains(permission.getCategory()))
                    .filter(permission -> Permission.permissionAllowed(groupPermissions, companyPermissions, permission))
                    .map(Permission::toString)
                    .collect(Collectors.toSet());
            
            userGroupDto.setGrantedPermissions(permissionGranted);
        }
        
        
        return userGroupDto;
    }
    
    @Override
    public int saveUserGroup(ComAdmin admin, UserGroupDto userGroupDto) throws Exception {
        int userGroupId = userGroupDto.getUserGroupId();
    
        Set<String> groupPermissions = userGroupDao.getGroupPermissionsTokens(userGroupId);
        Set<Permission> companyPermissions = companyService.getCompanyPermissions(userGroupDto.getCompanyId());
        List<String> premiumCategories = Arrays.asList(Permission.ORDERED_PREMIUM_RIGHT_CATEGORIES);
        
        List<String> allPermissionCategories = getUserGroupPermissionCategories(userGroupId, userGroupDto.getCompanyId(), admin);
        Set<String> allChangeablePermissions = getAllChangeablePermissions(allPermissionCategories, companyPermissions, admin);
    
        Set<String> selectedPermissions = userGroupDto.getGrantedPermissions();
        
        List<String> addedPermissions = ListUtils.removeAll(selectedPermissions, groupPermissions);
        List<String> removedPermissions = ListUtils.removeAll(groupPermissions, selectedPermissions);
        
        for (String permissionToken: removedPermissions) {
            Permission permission = Permission.getPermissionByToken(permissionToken);
            if (!allChangeablePermissions.contains(permissionToken) && permission != null) {
                String category = permission.getCategory();
                if (StringUtils.equals(Permission.CATEGORY_KEY_SYSTEM, category) || StringUtils.equals(Permission.CATEGORY_KEY_OTHERS, category)) {
                    // User is not allowed to change this permission of special category and may also not see it in GUI, so keep it unchanged
                    selectedPermissions.add(permissionToken);
                } else if (premiumCategories.contains(category) && !companyPermissions.contains(permission)) {
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

        for (String permissionToken: addedPermissions) {
            if (!allChangeablePermissions.contains(permissionToken)) {
                Permission permission = Permission.getPermissionByToken(permissionToken);
                String category = permission.getCategory();
                if ((StringUtils.equals(Permission.CATEGORY_KEY_SYSTEM, category) || StringUtils.equals(Permission.CATEGORY_KEY_OTHERS, category))) {
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
                || userGroupDao.adminGroupExists(companyId, newShortname) == 0;
    }
    
    @Override
    public List<String> getUserGroupPermissionCategories(int groupId, int groupCompanyId, ComAdmin admin) {
        List<String> permissionCategories = new ArrayList<>();
        if(groupId == ROOT_GROUP_ID || groupCompanyId == admin.getCompanyID() || groupCompanyId == admin.getCompany().getCreatorID() || admin.getAdminID() == ROOT_ADMIN_ID) {
            permissionCategories = Stream.concat(
                    Arrays.stream(Permission.ORDERED_STANDARD_RIGHT_CATEGORIES),
                    Arrays.stream(Permission.ORDERED_PREMIUM_RIGHT_CATEGORIES))
                    .collect(Collectors.toList());
    
            if (admin.permissionAllowed(Permission.MASTER_SHOW) || admin.getAdminID() == ROOT_ADMIN_ID) {
                permissionCategories.add(Permission.CATEGORY_KEY_SYSTEM);
                permissionCategories.add(Permission.CATEGORY_KEY_OTHERS);
            }
        }

        return permissionCategories;
    }
    
    @Override
    public boolean isUserGroupPermissionChangeable(ComAdmin admin, Permission permission, Set<Permission> companyPermissions) {
        String category = permission.getCategory();
        if (Arrays.asList(Permission.ORDERED_STANDARD_RIGHT_CATEGORIES).contains(category)) {
            return true;
        } else if (Arrays.asList(Permission.ORDERED_PREMIUM_RIGHT_CATEGORIES).contains(category)) {
            return companyPermissions.contains(permission);
        } else {
            return admin.permissionAllowed(permission) ||
                    admin.permissionAllowed(Permission.MASTER_SHOW) ||
                    admin.getAdminID() == ROOT_ADMIN_ID;
        }
    }
    
    @Override
    public List<String> getAdminNamesOfGroup(int userGroupId, @VelocityCheck int companyId) {
        List<String> adminNames = new ArrayList<>();
        if(userGroupId > -1 && companyId > 0) {
            adminNames = userGroupDao.getAdminsOfGroup(companyId, userGroupId);
        }
        
        return adminNames;
    }
    
    @Override
    public boolean deleteUserGroup(int userGroupId, ComAdmin admin) {
        int companyId = admin.getCompanyID();
        AdminGroup adminGroup = userGroupDao.getAdminGroup(userGroupId, admin.getCompanyID());
        if (adminGroup == null || (adminGroup.getCompanyID() != companyId && admin.getAdminID() != ROOT_ADMIN_ID)) {
            return false;
        }
        
        return userGroupDao.delete(adminGroup.getCompanyID(), userGroupId) > 0;
    }
    
    @Override
    public Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(ComAdmin admin, int groupId, int groupCompanyId) {
        PermissionsOverviewData.Builder builder = PermissionsOverviewData.builder();
        
        builder.setGroupPermissions(true);
        builder.setAllowedForGroup(groupId == ROOT_GROUP_ID ||
                groupCompanyId == admin.getCompanyID() ||
                groupCompanyId == admin.getCompany().getCreatorID() ||
                admin.getAdminID() == ROOT_ADMIN_ID);
    
        builder.setAdmin(admin);
        builder.setCompanyPermissions(companyService.getCompanyPermissions(groupCompanyId));
        builder.setVisiblePermissions(permissionFilter.getAllVisiblePermissions());
    
        return builder.build().getPermissionsCategories();
    }
    
    private Set<String> getAllChangeablePermissions(List<String> permissionCategories, Set<Permission> companyPermissions, ComAdmin admin) {
        return Permission.getAllPermissionsAndCategories().keySet().stream()
                .filter(permission -> permissionCategories.contains(permission.getCategory()))
                .filter(permission -> isUserGroupPermissionChangeable(admin, permission, companyPermissions))
                .map(Permission::toString)
                .collect(Collectors.toSet());
    }
    
}
