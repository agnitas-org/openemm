/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.common;

import java.util.NoSuchElementException;
import java.util.Objects;

public enum UsedFor {

	REQUEST_FILTER("filter"),
	RESOURCE("resource");
	
	private final String id;
	
	private UsedFor(final String id) {
		this.id = Objects.requireNonNull(id);
	}
	
	public static final UsedFor fromId(final String id) {
		// Default
		if(id == null || "".equals(id)) {
			return REQUEST_FILTER;
		}
			
		
		for(final UsedFor c : values()) {
			if(c.id.equalsIgnoreCase(id)) {
				return c;
			}
		}
		
		throw new NoSuchElementException(id);
	}
	
}
