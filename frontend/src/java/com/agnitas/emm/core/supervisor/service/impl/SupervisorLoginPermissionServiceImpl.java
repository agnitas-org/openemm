/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.supervisor.service.SupervisorLoginPermissionService;
import com.agnitas.emm.core.supervisor.service.UnknownSupervisorLoginPermissionException;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.departments.service.DepartmentService;
import com.agnitas.emm.core.supervisor.beans.SupervisorLoginPermissionTableItem;
import com.agnitas.emm.core.supervisor.common.SupervisorException;
import com.agnitas.emm.core.supervisor.dao.GrantedSupervisorLoginDao;

public final class SupervisorLoginPermissionServiceImpl implements SupervisorLoginPermissionService {
	
	private GrantedSupervisorLoginDao grantSupervisorLoginDao;
	private DepartmentService departmentService;

	@Override
	public final void grantLoginPermissionToDepartment(final ComAdmin admin, final int departmentID, final Date expireDate) throws SupervisorException, UnknownDepartmentIdException {
		Objects.requireNonNull(expireDate, "Expire date cannot be null");
		
		// Try to load department to know, if is exists
		this.departmentService.getDepartmentByID(departmentID);
		
		this.grantSupervisorLoginDao.grantSupervisorLoginToDepartment(admin.getAdminID(), departmentID, expireDate);
	}

	@Override
	public final void grantLoginPermissionToAllDepartments(final ComAdmin admin, final Date expireDate) throws SupervisorException {
		Objects.requireNonNull(expireDate, "Expire date cannot be null");
		
		this.grantSupervisorLoginDao.grantSupervisorLoginToAllDepartments(admin.getAdminID(), expireDate);
	}

	@Override
	public final void grantUnlimitedLoginPermissionToDepartment(final ComAdmin admin, final int departmentID) throws SupervisorException, UnknownDepartmentIdException {
		// Try to load department to know, if is exists
		this.departmentService.getDepartmentByID(departmentID);
		
		this.grantSupervisorLoginDao.grantSupervisorLoginToDepartment(admin.getAdminID(), departmentID, null);
	}

	@Override
	public final void grantUnlimitedLoginPermissionToAllDepartments(final ComAdmin admin) throws SupervisorException {
		this.grantSupervisorLoginDao.grantSupervisorLoginToAllDepartments(admin.getAdminID(), null);
	}

	@Override
	public final void revokeSupervisorLoginPermission(final int adminID, final int permissionID) throws UnknownSupervisorLoginPermissionException {
		this.grantSupervisorLoginDao.revokeSupervisorLoginPermission(adminID, permissionID);
	}

	@Override
	public final List<SupervisorLoginPermissionTableItem> listActiveSupervisorLoginPermissions(final ComAdmin admin) {
		return this.grantSupervisorLoginDao.listActiveSupervisorLoginPermissions(admin.getAdminID());	
	}
	
	@Override
	public final Department getDepartmentForLoginPermission(final int permissionID) throws UnknownSupervisorLoginPermissionException, UnknownDepartmentIdException {
		final Integer departmentID = this.grantSupervisorLoginDao.getDepartmentIdForLoginPermission(permissionID);
		
		if(departmentID != null) { 
			return this.departmentService.getDepartmentByID(departmentID);
		} else {
			return null;
		}
			
	}

	// ------------------------------------------------------------------ Dependency Injection
	
	@Required
	public final void setGrantSupervisorLoginDao(final GrantedSupervisorLoginDao dao) {
		this.grantSupervisorLoginDao = Objects.requireNonNull(dao, "GrantSupervisorLoginDao cannot be null");
	}
	
	@Required
	public final void setDepartmentService(final DepartmentService service) {
		this.departmentService = Objects.requireNonNull(service, "Department service is null");
	}

}
