/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.beans;

import java.util.Date;
import java.util.Objects;

public final class SupervisorLoginPermissionTableItem {

	private final int permissionID;
	private final Date granted;
	private final Date expireDate;
	@SuppressWarnings("unused")
	private final boolean allDeparments;
	private final String departmentSlugOrNull;
	
	public SupervisorLoginPermissionTableItem(final int permissionID, final boolean allDepartments, final String departmentSlugOrNull, final Date granted, final Date expires) {
		this.permissionID = permissionID;
		this.granted = Objects.requireNonNull(granted, "'granted 'cannot be null");
		this.expireDate = expires;
		this.allDeparments = allDepartments;
		this.departmentSlugOrNull = departmentSlugOrNull;
	}
	
	public final int getPermissionID() {
		return this.permissionID;
	}
	
	public final boolean isAllDepartmentsPermission() {
		return this.departmentSlugOrNull == null;
	}

	public final String getDepartmentSlugOrNull() {
		return this.departmentSlugOrNull;
	}

	public final Date getGranted() {
		return granted;
	}

	public final Date getExpireDate() {
		return expireDate;
	}
	
	
}
