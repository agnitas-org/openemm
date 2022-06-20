/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.bean;

import java.util.Objects;

public final class WebservicePermissionGroup {

	private final int id;
	private final String name;
	
	public WebservicePermissionGroup(final int id, final String name) {
		this.id = id;
		this.name = Objects.requireNonNull(name, "Permission group name is null");
	}

	public final int getId() {
		return id;
	}

	public final String getName() {
		return name;
	}
	
	@Override
	public final int hashCode() {
		return id;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		if(obj != null && obj instanceof WebservicePermissionGroup) {
			final WebservicePermissionGroup group = (WebservicePermissionGroup) obj;
			
			return this.id == group.id;
		} else {
			return false;
		}
	}
	
}
