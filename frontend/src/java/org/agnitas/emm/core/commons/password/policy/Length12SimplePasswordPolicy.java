/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
 *   <li>Minimum length of 12 characters</li>
 * </ul>
 */
public final class Length12SimplePasswordPolicy extends AbstractPasswordPolicy {
	
	/** Minimum length of password. */
	public static final int MINIMUM_PASSWORD_LENGTH = 12;

	@Override
	public final Set<PolicyViolation> checkPolicyViolations(String password) {
		if (password.length() < MINIMUM_PASSWORD_LENGTH) {
			final Set<PolicyViolation> violation = new HashSet<>();
			violation.add(PolicyViolation.TOO_SHORT);
			
			return violation;
		} else {
			return null;
		}
	}

}
