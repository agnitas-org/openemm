/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util;

import java.util.NoSuchElementException;

/**
 * Sorting direction.
 */
public enum SortDirection {
	
	/** Sort ascending. */
	ASCENDING("asc"),
	
	/** Sort descending. */
	DESCENDING("desc");
	
	private final String id;
	
	SortDirection(final String id) {
		this.id = id;
	}
	
	public final String getId() {
		return this.id;
	}
	
	public static final SortDirection fromId(final String id) {
		for(final SortDirection direction : values()) {
			if(direction.id.equalsIgnoreCase(id)) {
				return direction;
			}
		}
		
		throw new NoSuchElementException();
	}
	
	public static final SortDirection fromId(final String id, final SortDirection defaultValue) {
		for(final SortDirection direction : values()) {
			if(direction.id.equalsIgnoreCase(id)) {
				return direction;
			}
		}
		
		return defaultValue;
	}
}
