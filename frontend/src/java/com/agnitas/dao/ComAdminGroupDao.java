/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.List;
import java.util.Set;

import org.agnitas.beans.AdminGroup;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;

public interface ComAdminGroupDao {

	/**
     * Loads an AdminGroup identified by admin group id.
     * @param groupID
     *          The id of the AdminGroup that should be loaded.
     * @return  The AdminGroup or null on failure.
     */
	AdminGroup getAdminGroup(int groupID, int companyToLimitPremiumPermissionsFor);

    /**
     * Loads list of AdminGroups for specified company id
     * @param companyId
     *          The companyID for the AdminGroups.
     * @return List of AdminGroups or empty list
     */
	List<AdminGroup> getAdminGroupsByCompanyId( @VelocityCheck int companyId);

	List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(@VelocityCheck int companyId);

	PaginatedListImpl<AdminGroup> getAdminGroupsByCompanyIdInclCreator(@VelocityCheck int companyId, int adminId, String sort, String direction, int page, int rownums);
    
    int saveAdminGroup(AdminGroup adminGroup) throws Exception;
    
    int delete(@VelocityCheck int companyId, int adminGroupId);
    
    int adminGroupExists(@VelocityCheck int companyId, String username);

    List<String> getAdminsOfGroup(@VelocityCheck int companyId, int groupId);
    
    List<AdminGroup> getAdminGroupByAdminID(int adminId);
    
    Set<String> getGroupPermissionsTokens(int adminGroupId);
}
