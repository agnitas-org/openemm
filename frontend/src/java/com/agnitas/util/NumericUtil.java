/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public final class NumericUtil {

	private static final Pattern UNSIGNED_INTEGER_PATTERN = Pattern.compile("^\\s*\\d*\\s*$");
	
	public static boolean matchedUnsignedIntegerPattern(final String string) {
		if(string == null)
			return false;
		
		final Matcher m = UNSIGNED_INTEGER_PATTERN.matcher(string);
		
		return m.matches();
	}

	/**
	 * Tries parsing a double value. If parsing failed, a default value is returned.
	 *
	 * <b>Beware: This method does some strange detection of decimal separator symbol based on
	 * number of commas and periods found in the text!</b>
	 *
	 * @param text text to parse
	 * @param defaultValue default value if parsing failed
	 *
	 * @return parsed double value or default value
	 */
	public static Double tryParseDouble(String text, double defaultValue) {
		try {
			// Try to detect german number with comma and fix it (Locale is not available here)
			int numberOfCommas = StringUtils.countMatches(text, ",");
			if (numberOfCommas == 1) {
				int numberOfDots = StringUtils.countMatches(text, ".");
				if (numberOfDots == 0) {
					text = text.replace(",", ".");
				}
			}

			return Double.parseDouble(text);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

}
