/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public final class StringUtil {

	public static final int firstIndexOf(final String string, final char... values) {
		return firstIndexOf(string, 0, values);
	}

	public static final int firstIndexOf(final String string, final int startIndex, final char... values) {
		int index = -1;
		
		for(final char value : values) {
			final int tempIndex = string.indexOf(value, startIndex);
			
			if(tempIndex != -1) {
				if(index == -1) {
					index = tempIndex;
				} else {
					index = Math.min(index, tempIndex);
				}
			}
		}
		
		return index;
	}

	public static final int firstIndexOf(final String string, final String... values) {
		return firstIndexOf(string, 0, values);
	}
	
	public static final int firstIndexOf(final String string, final int startIndex, final String... values) {
		int index = -1;
		
		for(final String value : values) {
			final int tempIndex = string.indexOf(value);
			
			if(tempIndex != -1) {
				if(index == -1) {
					index = tempIndex;
				} else {
					index = Math.min(index, tempIndex);
				}
			}
		}
		
		return index;
	}
	
}
