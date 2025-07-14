/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util;

import java.util.NoSuchElementException;

public enum SortDirection {
	
	ASCENDING("asc", "default.order.ascending"),
	DESCENDING("desc", "default.order.descending");
	
	private final String id;
	private final String messageKey;
	
	SortDirection(String id, String messageKey) {
		this.id = id;
        this.messageKey = messageKey;
    }
	
	public String getId() {
		return this.id;
	}

	public String getMessageKey() {
		return this.messageKey;
	}
	
	public static SortDirection fromId(String id) {
		for (SortDirection direction : values()) {
			if(direction.id.equalsIgnoreCase(id)) {
				return direction;
			}
		}
		
		throw new NoSuchElementException();
	}
	
	public static SortDirection fromId(String id, SortDirection defaultValue) {
		for(SortDirection direction : values()) {
			if (direction.id.equalsIgnoreCase(id)) {
				return direction;
			}
		}
		
		return defaultValue;
	}
}
