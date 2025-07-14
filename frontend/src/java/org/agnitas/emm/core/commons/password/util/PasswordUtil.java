/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.emm.core.commons.password.util;

import org.apache.commons.lang3.StringUtils;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.admin.service.AdminService;

/**
 * Utility class for handling passwords.
 */
public final class PasswordUtil {

    /**
     * Check if admin password changed.
     * Method checked if given password differs from existed.
     *
     * @param adminService service for handling EMM users
     * @param password password to check (new password)
     * @param username user login
     *
     * @return {@code true} if password changed, otherwise {@code false}
     */
    public static boolean passwordChanged(final AdminService adminService, final String username, final String password) {
    	final Admin admin = adminService.getAdminByLogin(username, password);

    	if (StringUtils.isEmpty(password) || (admin != null && admin.getAdminID() > 0)) {
            return false;
        } else {
            return true;
        }
    }

}
