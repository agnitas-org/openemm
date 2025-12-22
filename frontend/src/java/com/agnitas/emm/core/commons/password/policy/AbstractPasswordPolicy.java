/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password.policy;

import java.util.Set;

import com.agnitas.emm.core.commons.password.PolicyViolation;
import com.agnitas.emm.core.commons.password.PolicyViolationException;

/**
 * Abstract class for easier checking policy violations.
 */
public abstract class AbstractPasswordPolicy implements PasswordPolicy {

	@Override
	public final void checkPassword(final String newPassword) throws PolicyViolationException {
		final Set<PolicyViolation> violations = checkPolicyViolations(newPassword);
		
		if(violations != null && !violations.isEmpty()) {
			throw new PolicyViolationException(violations);
		}
	}

	/**
	 * Check password for violations. Violations are collected and returned as a {@link Set}.
	 * If no violation has been found, the returned valued can either be <code>null</code> or an empty {@link Set}.
	 * 
	 * @param password password to check
	 * 
	 * @return (possible empty) set of policy violations or <code>null</code>
	 */
	public abstract Set<PolicyViolation> checkPolicyViolations(final String password);

}
