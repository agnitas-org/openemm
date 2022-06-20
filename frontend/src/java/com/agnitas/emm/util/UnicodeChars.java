/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util;

/**
 * Utility methods for Unicode characters.
 */
public final class UnicodeChars {

	/** Right-to-left mark. */
	public static final char RIGHT_TO_LEFT_MARK = '\u200f';
	
	/** Left-to-right mark. */
	public static final char LEFT_TO_RIGHT_MARK = '\u200e';
	
	/** Zero-width space. */
	public static final char ZERO_WIDTH_SPACE = '\u200b';
	
	/**
	 * Removes all right-to-left and left-to-right marks from given string.
	 * 
	 * @param s String
	 * 
	 * @return stripped string or <code>null</code> if given String was <code>null</code>
	 */
	public static final String removeTextDirectionMarks(final String s) {
		if(s == null) {
			return null;
		}
		
		return s
				.replace(Character.toString(LEFT_TO_RIGHT_MARK), "")
				.replace(Character.toString(RIGHT_TO_LEFT_MARK), "");
	}
	
	/**
	 * Removes all zero-width spaces from given string.
	 * 
	 * @param s String
	 * 
	 * @return stripped string or <code>null</code> if given String was <code>null</code>
	 */
	public static final String removeZeroWidthSpaces(final String s) {
		if(s == null) {
			return null;
		}
		
		return s.replace(Character.toString(ZERO_WIDTH_SPACE), "");
	}
	
	/**
	 * Removes all critical symbols from given string.
	 * <ul>
	 *   <li>right-to-left marks</li>
	 *   <li>left-to-right marks</li>
	 *   <li>zero-width spaces</li>
	 * </ul>
	 * 
	 * @param s String
	 * 
	 * @return stripped string or <code>null</code> if given string was <code>null</code>
	 */
	public static final String removeCriticalSymbols(final String s) {
		return removeZeroWidthSpaces(removeTextDirectionMarks(s));
	}
}
