/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumericUtil {

	private static final Pattern UNSIGNED_INTEGER_PATTERN = Pattern.compile("^\\s*\\d*\\s*$");
	
	public static final boolean matchedUnsignedIntegerPattern(final String string) {
		if(string == null)
			return false;
		
		final Matcher m = UNSIGNED_INTEGER_PATTERN.matcher(string);
		
		return m.matches();
	}
	
	
}
