/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.dao;

import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.supervisor.beans.SupervisorLoginPermissionTableItem;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;

/**
 * DAO for granted supervisor-logins.
 */
public interface GrantedSupervisorLoginDao {

	/**
	 * Checks, if supervisor has permission to login as EMM user.
	 * 
	 * @param supervisorID ID of supervisor 
	 * @param admin admin
	 * 
	 * @return <code>true</code> if supervisor is allowed to login as EMM user
	 */
	boolean isSupervisorLoginGranted(final int supervisorID, final Admin admin);
	
	/**
	 * Grant permission for supervisors to login as EMM users to all supervisors of given department.
	 *  
	 * @param adminID admin ID
	 * @param departmentID ID of department
	 * @param expireDate exipre date
	 */
	void grantSupervisorLoginToDepartment(final int adminID, final int departmentID, final Date expireDate);

	/**
	 * Grant permission to all supervisors to login as user.
	 *  
	 * @param adminID admin ID
	 * @param expireDate exipre date
	 */
	void grantSupervisorLoginToAllDepartments(final int adminID, final Date expireDate);

	List<SupervisorLoginPermissionTableItem> listActiveSupervisorLoginPermissions(final int adminID);

	void deleteOldGrants(final int expireDays);

	void revokeSupervisorLoginPermission(final int adminID, final int permissionID) throws UnknownSupervisorLoginPermissionException;

	/**
	 * Returns the ID of the department, for that the login permission is granted. If permission is granted to all deparments, <code>null</code> is returned.
	 * 
	 * @param permissionID ID of login permission
	 * 
	 * @return ID of department or <code>null</code>
	 * 
	 * @throws UnknownSupervisorLoginPermissionException if ID of login permission is unknown
	 */
	Integer getDepartmentIdForLoginPermission(final int permissionID) throws UnknownSupervisorLoginPermissionException;
}
