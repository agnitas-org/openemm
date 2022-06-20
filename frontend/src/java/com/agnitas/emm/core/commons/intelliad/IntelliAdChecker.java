/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.intelliad;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to check the tracking strings for IntelliAd.
 */
public class IntelliAdChecker {

	/** Regular expression string for checking format of tracking string. */
	private static final String PATTERN_STRING = "^\\d+-\\d+-\\d+-\\d+-\\d+-\\d+$";
	
	/** Pattern for regular expression matching. */
	private static final Pattern PATTERN = Pattern.compile( PATTERN_STRING);
	
	/**
	 * Checks, if given String matches the base format of the IntelliAd tracking string.
	 * 
	 * @param string String to check
	 * 
	 * @return true, if format matches
	 */
	public boolean isValidTrackingString( String string) {
		Matcher m = PATTERN.matcher( string);
		
		return m.matches();
	}

}
