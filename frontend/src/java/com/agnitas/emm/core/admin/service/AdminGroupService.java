/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.service;

import java.util.List;

import com.agnitas.beans.AdminGroup;

import com.agnitas.beans.Admin;

public interface AdminGroupService {
    List<AdminGroup> getAdminGroupsByCompanyIdAndDefault(int companyId, Admin admin, Admin adminIdToEdit);

	AdminGroup getAdminGroup(int adminGroupID, int companyToLimitPremiumPermissionsFor);

	AdminGroup getAdminGroupByName(String adminGroupName, int companyToLimitPremiumPermissionsFor);

	boolean deleteAdminGroup(int companyID, int adminGroupIdToDelete);
	
    int saveAdminGroup(AdminGroup adminGroup);

	boolean adminGroupExists(int companyID, String string);
}
