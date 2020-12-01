/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import org.agnitas.emm.core.commons.password.policy.PasswordPolicy;

import com.agnitas.beans.ComAdmin;
import com.agnitas.service.SimpleServiceResult;

/**
 * Interface for password checking code.
 */
public interface PasswordCheck {

	/**
	 * Check admin password. Checks some constraint (length, structure).
	 * 
	 * @param password password to check
	 * @param admin admin for comparison with current password
	 * @param handler handler for dealing with errors
	 * 
	 * @return {@code true} if password is ok, otherwise {@code false}
	 */
	boolean checkAdminPassword(String password, ComAdmin admin, PasswordCheckHandler handler);
	boolean checkNewAdminPassword(final String password, final int companyID, final PasswordCheckHandler handler);

	/**
	 * Check admin password. Checks some constraint (length, structure).
	 *
	 * @param password password to check
	 * @param admin admin for comparison with current password
	 *
	 * @return a {@link SimpleServiceResult} instance.
	 */
	SimpleServiceResult checkAdminPassword(String password, ComAdmin admin);
	SimpleServiceResult checkNewAdminPassword(final String password, final PasswordPolicy passwordPolicy);

}
