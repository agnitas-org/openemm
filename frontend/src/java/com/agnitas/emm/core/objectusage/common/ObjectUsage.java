/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.objectusage.common;

import java.util.Objects;

/**
 * Information about a single usage.
 */
public final class ObjectUsage {

	/** Type of object user. */
	private final ObjectUserType objectUserType;
	
	/** ID of object user. */
	private final int objectUserID;
	
	/** Name of object user. */
	private final String objectUserName;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param objectUserType type of object user
	 * @param objectUserID ID of object user
	 * @param objectUserName name of object user
	 * 
	 * @throws NullPointerException if type or name is <code>null</code>
	 */
	public ObjectUsage(final ObjectUserType objectUserType, final int objectUserID, final String objectUserName) {
		this.objectUserType = Objects.requireNonNull(objectUserType, "Object user type is null");
		this.objectUserID = objectUserID;
		this.objectUserName = Objects.requireNonNull(objectUserName, "Object user name is null");
	}

	/**
	 * Returns the type of the object user.
	 * 
	 * @return type of object user
	 */
	public final ObjectUserType getObjectUserType() {
		return objectUserType;
	}

	/**
	 * Returns the ID of the object user.
	 * 
	 * @return ID of object user
	 */
	public final int getObjectUserID() {
		return objectUserID;
	}
	
	/**
	 * Returns the name of the object user.
	 * 
	 * @return name of object user
	 */
	public final String getObjectUserName() {
		return this.objectUserName;
	}
	
}
