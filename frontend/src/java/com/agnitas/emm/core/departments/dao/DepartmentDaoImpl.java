/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.departments.dao;

import java.util.List;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentNameException;

public final class DepartmentDaoImpl extends BaseDaoImpl implements DepartmentDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(DepartmentDaoImpl.class);
	
	@Override
	public final List<Department> listAllDepartments() {
		return select(logger, "SELECT * FROM department_tbl", new DepartmentRowMapper());
	}

	@Override
	public final Department getDepartmentByID(final int departmentID) throws UnknownDepartmentIdException {
		final List<Department> list = select(logger, "SELECT * FROM department_tbl WHERE department_id=?", new DepartmentRowMapper(), departmentID);
		
		if(list.isEmpty()) {
			throw new UnknownDepartmentIdException(departmentID);
		} else {
			return list.get(0);
		}
	}
	
	@Override
	public final Department getDepartmentByShortname(final String shortname) throws UnknownDepartmentNameException {
		final List<Department> list = select(logger, "SELECT * FROM department_tbl WHERE LOWER(slug) = LOWER(?)", new DepartmentRowMapper(), shortname);
		
		if (list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public final Department createDepartment(String shortname, String description, boolean supervisorBindingToCompany0Allowed, boolean loginWithoutUserPermissionAllowed) throws Exception {
		int newDepartmentID = selectInt(logger, "SELECT MAX(department_id) + 1 FROM department_tbl");
		
		if (1 == update(logger, "INSERT INTO department_tbl (department_id, slug, description, cid_0_allowed, no_permission_required) VALUES (?, ?, ?, ?, ?)", newDepartmentID, shortname, description, supervisorBindingToCompany0Allowed ? 1 : 0, loginWithoutUserPermissionAllowed ? 1 : 0)) {
			return new Department(newDepartmentID, shortname, supervisorBindingToCompany0Allowed, loginWithoutUserPermissionAllowed);
		} else {
			throw new Exception("Cannot create department");
		}
	}
}
