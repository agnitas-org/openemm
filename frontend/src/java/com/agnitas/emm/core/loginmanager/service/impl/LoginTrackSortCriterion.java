/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.service.impl;

import java.util.NoSuchElementException;

public enum LoginTrackSortCriterion {

	USERNAME("username"),
	IP_ADDRESS("ip_address");
	
	private final String id;
	
	LoginTrackSortCriterion(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	
	public static LoginTrackSortCriterion fromId(String id) {
		for (LoginTrackSortCriterion criterion : values()) {
			if (criterion.id.equalsIgnoreCase(id)) {
				return criterion;
			}
		}
		
		throw new NoSuchElementException();
	}
	
	public static LoginTrackSortCriterion fromId(String id, LoginTrackSortCriterion defaultValue) {
		for(final LoginTrackSortCriterion criterion : values()) {
			if(criterion.id.equalsIgnoreCase(id)) {
				return criterion;
			}
		}
		
		return defaultValue;
	}
	
}
