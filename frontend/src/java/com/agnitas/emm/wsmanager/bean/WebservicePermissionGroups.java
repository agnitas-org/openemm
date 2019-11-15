/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public final class WebservicePermissionGroups {
	
	private final List<WebservicePermissionGroup> allGroups;
	
	public WebservicePermissionGroups(final Collection<WebservicePermissionGroup> groups) {
		this.allGroups = Collections.unmodifiableList(new ArrayList<>(groups));
		
		if(new HashSet<>(this.allGroups).size() != this.allGroups.size()) {
			throw new IllegalArgumentException("Same permission group contained more than once");
		}
	}

	public final List<WebservicePermissionGroup> getAllPermissionGroups() {
		return this.allGroups;
	}
}
