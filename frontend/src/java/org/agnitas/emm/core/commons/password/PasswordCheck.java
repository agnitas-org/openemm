/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

import com.agnitas.beans.ComAdmin;
import com.agnitas.service.SimpleServiceResult;

/**
 * Interface for password checking code.
 */
public interface PasswordCheck {

	/**
	 * Check admin password. Checks some constraint (length, structure). If {@code admin} is not {@code null}, then 
	 * it's checked, that given password differs from current admin password.
	 * 
	 * @param password password to check
	 * @param admin admin for comparison with current password or {@code null}
	 * @param handler handler for dealing with errors
	 * 
	 * @return {@code true} if password is ok, otherwise {@code false}
	 */
	boolean checkAdminPassword(String password, ComAdmin admin, PasswordCheckHandler handler);

	/**
	 * Check admin password. Checks some constraint (length, structure). If {@code admin} is not {@code null}, then
	 * it's checked, that given password differs from current admin password.
	 *
	 * @param password password to check
	 * @param admin admin for comparison with current password or {@code null}
	 *
	 * @return a {@link SimpleServiceResult} instance.
	 */
	SimpleServiceResult checkAdminPassword(String password, ComAdmin admin);

    /**
     * Check if admin password changed.
     * Method checked if given password differs from existed.
     *
     * @param password password to check (new password)
     * @param username user login
     *
     * @return {@code true} if password changed, otherwise {@code false}
     */
    boolean passwordChanged(String username, String password);

}
