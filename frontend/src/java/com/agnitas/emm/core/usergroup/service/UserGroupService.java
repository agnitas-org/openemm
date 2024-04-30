/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.PaginatedListImpl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;

public interface UserGroupService {
    
    PaginatedListImpl<UserGroupDto> getUserGroupPaginatedList(Admin admin, String sort, String sortDirection, int page, int rownumber);
    
    UserGroupDto getUserGroup(Admin admin, int userGroupId);
    
    int saveUserGroup(Admin admin, UserGroupDto userGroupDto) throws Exception;
    
    boolean isShortnameUnique(String shortname, int userGroupId, int companyId);
    
    boolean isUserGroupPermissionChangeable(Admin admin, Permission permission, Set<Permission> companyPermissions);
    
    List<String> getUserGroupPermissionCategories(int groupId, int groupCompanyId, Admin admin);
    
    List<String> getAdminNamesOfGroup(int userGroupId, int companyId);
    
    List<String> getGroupNamesUsingGroup(int userGroupId, int companyId);
    
    boolean deleteUserGroup(int userGroupId, Admin admin);
    
    Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(Admin admin, int groupId, int groupCompanyId);

	List<AdminGroup> getAdminGroupsByCompanyId(int companyID);

	Collection<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyID, AdminGroup adminGroup);

	AdminGroup getAdminGroup(int userGroupId, int companyID);

    int copyUserGroup(int id, Admin admin) throws Exception;
}
