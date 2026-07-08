/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.AdminGroup;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.usergroup.dto.UserGroupDto;
import com.agnitas.emm.core.usergroup.form.UserGroupOverviewFilter;

public interface AdminGroupDao {
	/**
     * Loads an AdminGroup identified by admin group id.
     * @return The AdminGroup or null on failure.
     */
	AdminGroup getAdminGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor);

    AdminGroup getUserGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor);

    List<AdminGroup> getAdminGroupsByCompanyId(int companyId);

    /**
     * Loads list of AdminGroups for specified company id
     * @param companyId the companyID for the AdminGroups
     * @param includeDeleted whether to get entries that marked as deleted
     * @return List of AdminGroups or empty list
     */
	List<AdminGroup> getAdminGroupsByCompanyId( int companyId, boolean includeDeleted);

	List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyId, List<Integer> additionalAdminGroupIds);

    int getUsersCount(int groupId, int companyId);

    PaginatedList<UserGroupDto> getAdminGroupsByCompanyIdInclCreator(UserGroupOverviewFilter filter);

    int saveAdminGroup(AdminGroup adminGroup);
    
    int delete(int companyId, int adminGroupId);

    void markDeleted(int groupId, int companyId);

    boolean adminGroupExists(int companyId, String username);

    List<String> getAdminsOfGroup(int companyId, int groupId);
    
    List<AdminGroup> getAdminGroupsByAdminID(int companyID, int adminId);
    
    Set<String> getGroupPermissionsTokens(int adminGroupId);

	AdminGroup getAdminGroupByName(String adminGroupName, int companyID);

	List<String> getGroupNamesUsingGroup(int companyId, int groupId);

    void restore(Set<Integer> ids, int companyId);

    List<Integer> getMarkedAsDeletedBefore(Date date, int companyId);
}
