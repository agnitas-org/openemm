/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.password.policy;

import com.agnitas.emm.core.commons.password.PolicyViolationException;

/**
 * Password policy.
 * 
 * Sub-classes implements different rules on user passwords.
 * The rules do not include that the new password must differ from the old one. This
 * is a general requirement to passwords.
 */
public interface PasswordPolicy {

	/**
	 * Called when user password has to be set.
	 * 
	 * @param newPassword new password
	 * 
	 * @throws PolicyViolationException on violation(s) of password policy
	 */
	public void checkPassword(final String newPassword) throws PolicyViolationException;

}
