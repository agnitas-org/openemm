/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password.policy;

import java.util.HashSet;
import java.util.Set;

import org.agnitas.emm.core.commons.password.PolicyViolation;

/**
 * Password policy with these requirements:
 * 
 * <ul>
 *   <li>Minimum length of 8 characters</li>
 *   <li>Password must contain:
 *   	<ul>
 *   		<li>lower case letters</li>
 *       	<li>upper case letters</li>
 *       	<li>digits</li>
 *       	<li>special characters</li>
 *	    </ul>
 *   </li>
 * </ul>
 */
public final class Length8MixedPasswordPolicy extends AbstractPasswordPolicy {

	/** Minimum length of password. */
	public static final int MINIMUM_PASSWORD_LENGTH = 8;

	@Override
	public final Set<PolicyViolation> checkPolicyViolations(final String newPassword) {
		final Set<PolicyViolation> violations = new HashSet<>();
		
		if (newPassword.length() < MINIMUM_PASSWORD_LENGTH) {
			violations.add(PolicyViolation.TOO_SHORT);
		}
		
		if (!newPassword.matches(".*\\p{Lower}.*")) {
			violations.add(PolicyViolation.NO_LOWER_CASE);
		} 

		if (!newPassword.matches(".*\\p{Upper}.*")) {
			violations.add(PolicyViolation.NO_UPPER_CASE);
		} 

		if (!newPassword.matches(".*\\p{Digit}.*")) {
			violations.add(PolicyViolation.NO_DIGITS);
		} 

		if (!newPassword.matches(".*[^\\p{Alnum}].*")) {	// Every character, that is not alpha or digit is treated as special character
			violations.add(PolicyViolation.NO_SPECIAL);
		} 
		
		return violations;
	}

}
