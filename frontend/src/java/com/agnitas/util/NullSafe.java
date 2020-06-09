/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

/**
 * Null-safe implementations of some convenience methods.
 */
public final class NullSafe {

	/**
	 * Compares given objects. Returns <code>true</code> if both objects are equals by {@link Object#equals(Object)}
	 * or both objects are <code>null</code>.
	 * 
	 * @param <T> Type of objects
	 * @param obj0 first object
	 * @param obj1 second object
	 * 
	 * @return <code>true</code> if both objects are equal or both objects are <code>null</code>
	 * 
	 * @see Object#equals(Object)
	 */
	public static final <T> boolean equals(final T obj0, final T obj1) {
		if(obj0 == null) {
			return obj1 == null;
		} else {
			return obj1 != null && obj0.equals(obj1);
		}
	}
}
