/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.departments.beans;

import java.util.Objects;

/**
 * Information on a department.
 */
public final class Department {

	/** ID of department. */
	private final int id;
	
	/** Department slug. (Used for i18n, ...) */
	private final String slug;
	
	/** Flag, supervisor bindings to company ID 0 are respected. */
	private final boolean supervisorBindingToCompany0Allowed;
	
	/** Flag, if login to users of same company without permission is allowed. */
	private final boolean loginWithoutUserPermissionAllowed;
	
	/**
	 * Creates a new department instance.
	 * 
	 * @param id department ID
	 * @param slug department slug
	 * @param supervisorBindingToCompany0Allowed flag if supervisor bindings to company ID 0 are respected
	 */
	public Department(final int id, final String slug, final boolean supervisorBindingToCompany0Allowed, final boolean loginWithoutUserPermissionAllowed) {
		this.id = id;
		this.slug = Objects.requireNonNull(slug, "Department slug cannot be null");
		this.supervisorBindingToCompany0Allowed = supervisorBindingToCompany0Allowed;
		this.loginWithoutUserPermissionAllowed = loginWithoutUserPermissionAllowed;
	}

	/**
	 * Returns the department ID.
	 * 
	 * @return department ID
	 */
	public final int getId() {
		return id;
	}

	/**
	 * Returns the department slug.
	 * 
	 * @return department slug
	 */
	public final String getSlug() {
		return slug;
	}

	/**
	 * Returns the flag, if supervisor bindings to company ID 0 are respected.
	 * 
	 * @return flag, if supervisor bindings to company ID 0 are respected
	 */
	public final boolean isSupervisorBindingToCompany0Allowed() {
		return supervisorBindingToCompany0Allowed;
	}
	
	public final boolean isLoginWithoutUserPermissionAllowed() {
		return this.loginWithoutUserPermissionAllowed;
	}

}
