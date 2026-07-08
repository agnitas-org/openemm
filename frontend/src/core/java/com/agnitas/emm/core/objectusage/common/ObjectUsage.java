/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.common;

import java.util.Map;
import java.util.Objects;

/**
 * Information about a single usage.
 */
public final class ObjectUsage {

	private final ObjectUsageType type;
	private final int id;
	private final String name;
	private Map<String, Object> details;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param type type of object user
	 * @param id ID of object user
	 * @param usageName name of object user
	 * 
	 * @throws NullPointerException if type or name is <code>null</code>
	 */
	public ObjectUsage(ObjectUsageType type, int id, String usageName) {
		this.type = Objects.requireNonNull(type, "Object usage type is null");
		this.id = id;
		this.name = Objects.requireNonNull(usageName, "Object usage name is null");
	}

    public ObjectUsage(ObjectUsageType type, int userId, String userName, Map<String, Object> details) {
        this(type, userId, userName);
        this.details = details;
  	}

	public ObjectUsageType getType() {
		return type;
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return this.name;
	}

    public Map<String, Object> getDetails() {
        return details;
    }
}
