/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import com.agnitas.beans.AdminPreferences;

public interface AdminPreferencesDao {

    /**
     * Return admin preferences by adminId.
     *
     * @param adminId
     *            The id of the admin which preferences should be deleted.
     */
	AdminPreferences getAdminPreferences(int adminId);

    /**
     * Saves an admin preferences.
     *
     * @param adminPreferences
     *            The admin preferences that should be saved.
     * @return 
     */
	int save(AdminPreferences adminPreferences);

    /**
     * Deletes an admin preferences.
     *
     * @param adminId
     *            The id of the admin which preferences should be deleted.
     * @return true
     */
	int delete(int adminId);
}
