/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public final class OneOf {

	public static final boolean oneIntOf(final int value, final int... values) {
		for(int i : values) {
			if(value == i) {
				return true;
			}
		}
		
		return false;
	}

	@SafeVarargs
	public static final <T> boolean oneObjectOf(final T value, final T... values) {
		for(final T t : values) {
			if(value == null) {
				if(t == null) {
					return true;
				}
			} else {
				if(value.equals(t)) {
					return true;
				}
			}
		}
		
		return false;
	}

}
