/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.departments.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.departments.beans.Department;
import com.agnitas.emm.core.departments.dao.DepartmentDao;
import com.agnitas.emm.core.departments.exceptions.UnknownDepartmentIdException;

public final class DepartmentServiceImpl implements DepartmentService {

	private DepartmentDao departmentDao;
	
	@Override
	public final List<Department> listAllDepartments() {
		return this.departmentDao.listAllDepartments();
	}

	@Override
	public final Department getDepartmentByID(final int departmentID) throws UnknownDepartmentIdException {
		return this.departmentDao.getDepartmentByID(departmentID);
	}

	@Required
	public final void setDepartmentDao(final DepartmentDao dao) {
		this.departmentDao = Objects.requireNonNull(dao, "Department DAO cannot be null");
	}
	
}
