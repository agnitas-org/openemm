/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;

public interface UserGroupService {
    
    PaginatedListImpl<UserGroupDto> getUserGroupPaginatedList(ComAdmin admin, String sort, String sortDirection, int page, int rownumber);
    
    UserGroupDto getUserGroup(ComAdmin admin, int userGroupId);
    
    int saveUserGroup(ComAdmin admin, UserGroupDto userGroupDto) throws Exception;
    
    boolean isShortnameUnique(String shortname, int userGroupId, @VelocityCheck int companyId);
    
    boolean isUserGroupPermissionChangeable(ComAdmin admin, Permission permission, Set<Permission> companyPermissions);
    
    List<String> getUserGroupPermissionCategories(int groupId, int groupCompanyId, ComAdmin admin);
    
    List<String> getAdminNamesOfGroup(int userGroupId, @VelocityCheck int companyId);
    
    boolean deleteUserGroup(int userGroupId, ComAdmin admin);
    
    Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(ComAdmin admin, int groupId, int groupCompanyId);
}
