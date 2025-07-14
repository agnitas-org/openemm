/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgnitasEmailValidatorWithWhitespace implements EmailValidator{
	private static final String SPECIAL_CHARS_REGEXP = "\\p{Cntrl}\\(\\)<>@,;:'\\\\\\\"\\.\\[\\]";
	private static final String VALID_CHARS_REGEXP = "[^\\s" + SPECIAL_CHARS_REGEXP + "]";
	private static final String QUOTED_USER_REGEXP = "(\"[^\"]*\")";
	private static final String WORD_REGEXP = "((" + VALID_CHARS_REGEXP + "|')+( (" + VALID_CHARS_REGEXP + "|')+)*|" + QUOTED_USER_REGEXP + ")";

	private static final String DOMAIN_PART_REGEX = "\\p{Alnum}(?>[\\p{Alnum}-]*\\p{Alnum})*";
	private static final String TOP_DOMAIN_PART_REGEX = "\\p{Alpha}{2,}";
	private static final String DOMAIN_NAME_REGEX = "^(?:" + DOMAIN_PART_REGEX + "\\.)+" + "(" + TOP_DOMAIN_PART_REGEX + ")$";

	/**
	 * Regular expression for parsing email addresses.
	 */
	private static final String EMAIL_REGEX = "^\\s*?([^@ ][^@]*)@(.+?)\\s*$";

	private static final String USER_REGEX = "^\\s*" + WORD_REGEXP + "(\\." + WORD_REGEXP + ")*$";

	/** Regular expression pattern for parsing email addresses. */
	private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

	private static final Pattern USER_PATTERN = Pattern.compile(USER_REGEX);

	private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile(DOMAIN_NAME_REGEX);

	/** Singleton instance. */
	private static final AgnitasEmailValidatorWithWhitespace INSTANCE = new AgnitasEmailValidatorWithWhitespace();

	public static AgnitasEmailValidatorWithWhitespace getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isValidUtf8Email(String emailAddress) {
		return isValid(emailAddress);
	}

	@Override
	public boolean isValid(String emailAddress) {
		Matcher m = EMAIL_PATTERN.matcher(emailAddress);
				
		// Check, if email address matches outline structure
		if(!m.matches()) {
			return false;
		}
		
		// Check if user-part is valid
		if(!isValidUser(m.group(1))) {
			return false;
		}

		return isValidDomain(m.group(2));
	}
    
    public boolean isValidUser(String user) {
    	return USER_PATTERN.matcher(user).matches();
    }
    
    public boolean isValidDomain(String domain) {
    	String asciiDomainName;
		try {
			asciiDomainName = java.net.IDN.toASCII(domain);
		} catch (Exception e) {
			// invalid domain name like abc@.ch
			return false;
		}

    	// Do not allow ".local" top level domain
    	if (asciiDomainName.toLowerCase().endsWith(".local")) {
    		return false;
    	}
    	
    	return DOMAIN_NAME_PATTERN.matcher(asciiDomainName).matches();
    }
}
