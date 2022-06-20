/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.beanshell;

/**
 * Class containing methods used in BeanShell expressions.
 * 
 * This class is used as abstraction layer to 3rd party code.
 */
public final class BeanShellRuntimeUtils {

	/**
	 * Compares two string ignoring case and respecting <code>null</code> values.
	 * 
	 * Result values are:
	 * <ul>
	 *   <li>compareStringsIgnoreCase(null, null) = 0</li>
	 *   <li>compareStringsIgnoreCase(s0, null) = 1</li>
	 *   <li>compareStringsIgnoreCase(null, s1) = -1</li>
	 *   <li>compareStringsIgnoreCase(s0, s1) = s0.compareToIgnoreCase(s1)</li>
	 * <ul>
	 * 
	 * @param s0 first string or <code>null</code>
	 * @param s1 second string or <code>null</code>
	 * 
	 * @return result value
	 * 
	 * @see String#compareToIgnoreCase(String)
	 */
	public static final int compareStringsIgnoreCase(final String s0, final String s1) {
		if(s0 == null && s1 == null) {
			return 0;
		} else if(s0 != null && s1 != null) {
			return s0.compareToIgnoreCase(s1);
		} else if(s0 != null) {
			return 1;
		} else {
			return -1;
		}
	}
	
	public static final boolean containsIgnoreCase(final String s0, final Object obj) {
		if(s0 != null && obj != null) {
			return s0.toLowerCase().indexOf(obj.toString().toLowerCase()) != -1;
		} else
			return false;
	}
	
	public static final boolean startsWithIgnoreCase(final String s0, final Object obj) {
		if(s0 != null && obj != null) {
			return s0.toLowerCase().startsWith(obj.toString().toLowerCase());
		} else
			return false;
	}
	
	public static final String concat(final Object o0, final Object o1) {
		final String s0 = o0 != null ? o0.toString() : "";
		final String s1 = o1 != null ? o1.toString() : "";
		
		return s0 + s1;
	}
	
	/**
	 * Compares two objects respecting <code>null</code> values.
	 * 
	 * Result values are:
	 * <ul>
	 *   <li>compare(null, null) = 0</li>
	 *   <li>compare(obj0, null) = 1</li>
	 *   <li>compare(null, obj1) = -1</li>
	 *   <li>compare(obj0, obj1) = obj0.compare(obj1)</li>
	 * <ul>
	 * 
	 * @param obj0 first object or <code>null</code>
	 * @param obj1 second object or <code>null</code>
	 * 
	 * @return result value
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public static final <T extends Comparable<T>> int compare(final T obj0, final T obj1) {
		if(obj0 == null && obj1 == null) {
			return 0;
		} else if(obj0 != null && obj1 != null) {
			return obj0.compareTo(obj1);
		} else if(obj0 != null) {
			return 1;
		} else {
			return -1;
		}
	}
	
	public static final boolean isEmpty(final Object value) {
		return value == null || "".equals(value.toString());
	}
	
}
