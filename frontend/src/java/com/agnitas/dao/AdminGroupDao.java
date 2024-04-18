/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter;
import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.PaginatedListImpl;

public interface AdminGroupDao {
	/**
     * Loads an AdminGroup identified by admin group id.
     * @param groupID
     *          The id of the AdminGroup that should be loaded.
     * @return  The AdminGroup or null on failure.
     */
	AdminGroup getAdminGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor);

    AdminGroup getUserGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor);

    /**
     * Loads list of AdminGroups for specified company id
     * @param companyId
     *          The companyID for the AdminGroups.
     * @return List of AdminGroups or empty list
     */
	List<AdminGroup> getAdminGroupsByCompanyId( int companyId);

	List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyId, List<Integer> additionalAdminGroupIds);

    PaginatedListImpl<UserGroupDto> getAdminGroupsByCompanyIdInclCreator(UserGroupOverviewFilter filter);

    PaginatedListImpl<AdminGroup> getAdminGroupsByCompanyIdInclCreator(int companyId, int adminId, String sort, String direction, int page, int rownums);
    
    int saveAdminGroup(AdminGroup adminGroup) throws Exception;
    
    int delete(int companyId, int adminGroupId);
    
    boolean adminGroupExists(int companyId, String username);

    List<String> getAdminsOfGroup(int companyId, int groupId);
    
    List<AdminGroup> getAdminGroupsByAdminID(int companyID, int adminId);
    
    Set<String> getGroupPermissionsTokens(int adminGroupId);

	AdminGroup getAdminGroupByName(String adminGroupName, int companyID);

	Set<String> getParentGroupsPermissionTokens(int adminGroupId);

	List<Integer> getParentGroupIds(int adminGroupId);

	List<String> getGroupNamesUsingGroup(int companyId, int groupId);
}
