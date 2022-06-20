/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.departments.dao;

import java.util.List;

import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentNameException;

public interface DepartmentDao {
	List<Department> listAllDepartments();

	Department getDepartmentByID(final int departmentID) throws UnknownDepartmentIdException;

	Department getDepartmentByShortname(String shortname) throws UnknownDepartmentNameException;

	Department createDepartment(String shortname, String description, boolean supervisorBindingToCompany0Allowed, boolean loginWithoutUserPermissionAllowed) throws Exception;
}
