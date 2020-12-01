/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.globalblacklist.forms.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator of email patterns for blacklist.
 */
public final class BlacklistEmailPatternValidator {

	/** Accepts any pattern with exactly one &quot;@&quot; and not containing spaces. */  
	private static final Pattern PATTERN = Pattern.compile("^[^@\\s]+@([^@\\s]+)$");
	
	/**
	 * Validates given email pattern.
	 * 
	 * The pattern must contains exactly one "@" and must not contain any whitespace.
	 * 
	 * @param emailPattern email pattern
	 * 
	 * @return <code>true</code> if pattern is valid, otherwise <code>false</code>
	 */
	public static final boolean validateEmailPattern(final String emailPattern) {
		final Matcher m = PATTERN.matcher(emailPattern);
		
		return m.matches() && isValidDomainPattern(m.group(1));
	}
    
	/**
	 * Checks if given domain is a valid domain pattern.
	 * 
	 * @param domainPattern
	 * 
	 * @return <code>true</code> if given domain pattern is valid, otherwise <code>false</code>
	 */
    private static final boolean isValidDomainPattern(final String domainPattern) {
		try {
			final String patternWithoutWildcard = domainPattern
					.replace('%', 'a') 	// Replace "%" wildcards by a letter
					.replace('*', 'a');	// Replace "*" wildcards by a letter
			
			java.net.IDN.toASCII(patternWithoutWildcard);

			return true;
		} catch (final Exception e) {
			// invalid domain name like abc@.ch
			return false;
		}
    }

}
