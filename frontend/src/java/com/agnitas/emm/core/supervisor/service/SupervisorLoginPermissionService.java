/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.service;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.supervisor.beans.SupervisorLoginPermissionTableItem;
import com.agnitas.emm.core.supervisor.common.SupervisorException;

public interface SupervisorLoginPermissionService {

	void grantLoginPermissionToDepartment(final Admin admin, final int departmentID, final Date expireDate) throws SupervisorException, UnknownDepartmentIdException;
	void grantLoginPermissionToAllDepartments(final Admin admin, final Date expireDate) throws SupervisorException;
	void grantUnlimitedLoginPermissionToDepartment(final Admin admin, final int departmentID) throws SupervisorException, UnknownDepartmentIdException;
	void grantUnlimitedLoginPermissionToAllDepartments(final Admin admin) throws SupervisorException;
	
	void revokeSupervisorLoginPermission(int adminID, int permissionID) throws UnknownSupervisorLoginPermissionException;
	
	List<SupervisorLoginPermissionTableItem> listActiveSupervisorLoginPermissions(final Admin admin);
	
	/**
	 * Returns the department for login permission. If permission was granted to all departments, <code>null</code> is returned.
	 *
	 * @param permissionID ID of login permission
	 *
	 * @return Department or <code>null</code>
	 *
	 * @throws UnknownSupervisorLoginPermissionException if login permission id unkonwn
	 * @throws UnknownDepartmentIdException if department is unknown
	 */
	Department getDepartmentForLoginPermission(final int permissionID) throws UnknownSupervisorLoginPermissionException, UnknownDepartmentIdException;

}
