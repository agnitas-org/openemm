/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WebservicePermissions {

	private final List<WebservicePermission> allPermissions;
	private final Map<String, List<WebservicePermission>> permissions;
	
	public WebservicePermissions(final List<WebservicePermission> permissions) {
		this.allPermissions = Collections.unmodifiableList(new ArrayList<>(permissions));
		this.permissions = mapPermissionsToCategories(permissions);
		checkUniquePermissions();
	}
	
	private static final Map<String, List<WebservicePermission>> mapPermissionsToCategories(final List<WebservicePermission> list) {
		final Map<String, List<WebservicePermission>> result = new HashMap<>();
		
		for(final WebservicePermission permission : list) {
			final List<WebservicePermission> mappedList = result.computeIfAbsent(permission.getCategoryOrNull(), key -> new ArrayList<>());
			
			mappedList.add(permission);
		}
		
		return result;
	}
	
	private final void checkUniquePermissions() {
		final Set<String> set = new HashSet<>();
		
		for(final WebservicePermission permission : this.allPermissions) {
			if(set.contains(permission.getEndpointName())) {
				throw new IllegalArgumentException(String.format("Duplicate permission: %s", permission.getEndpointName()));
			}
			
			set.add(permission.getEndpointName());
		}
	}
	
	public final Set<String> getCategories() {
		return this.permissions.keySet();	
	}
	
	public final List<WebservicePermission> getPermissionsForCategory(final String category) {
		return this.permissions.get(category);
	}
	
	public final List<WebservicePermission> getUncategorizedPermissions() {
		return this.permissions.get(null);
	}
	
	public final List<WebservicePermission> getAllPermissions() {
		return this.allPermissions;
	}
	
}
