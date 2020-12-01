/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password.policy;

import org.agnitas.emm.core.commons.password.PolicyViolationException;
import org.agnitas.emm.core.commons.password.PolicyViolation;

/**
 * Password policy for webservice user with these requirements:
 * 
 * <ul>
 *   <li>Minimum length of 32 characters</li>
 * </ul>
 */
public final class WebservicePasswordPolicy implements PasswordPolicy {

	/** Minimum length of password. */
	public static final int MINIMUM_PASSWORD_LENGTH = 32;

	@Override
	public final void checkPassword(final String newPassword) throws PolicyViolationException {
		if (newPassword.length() < MINIMUM_PASSWORD_LENGTH) {
			throw new PolicyViolationException(PolicyViolation.TOO_SHORT);
		}
	}

}
