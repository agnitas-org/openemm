/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.admin.web.PermissionsOverviewData;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter;
import com.agnitas.service.ServiceResult;

public interface UserGroupService {

    PaginatedList<UserGroupDto> overview(UserGroupOverviewFilter filter);

    UserGroupDto getUserGroup(Admin admin, int userGroupId);
    
    int saveUserGroup(Admin admin, UserGroupDto userGroupDto);
    
    boolean isShortnameUnique(String shortname, int userGroupId, int companyId);
    
    Map<String, PermissionsOverviewData.PermissionCategoryEntry> getPermissionOverviewData(Admin admin, int groupId, int groupCompanyId);

	Collection<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyID, AdminGroup adminGroup);

	AdminGroup getAdminGroup(int userGroupId, int companyID);

    int copyUserGroup(int id, Admin admin);

    ServiceResult<List<UserGroupDto>> getAllowedGroupsForDeletion(Set<Integer> ids, Admin admin);

    List<UserGroupDto> markDeleted(Set<Integer> bulkIds, Admin admin);

    void restore(Set<Integer> ids, int companyId);

    void removeMarkedAsDeletedBefore(Date date, int companyId);

}
