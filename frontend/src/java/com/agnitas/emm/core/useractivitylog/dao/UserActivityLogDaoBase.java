/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import com.agnitas.beans.Admin;

import java.util.Date;

public interface UserActivityLogDaoBase {

    /**
     * Increment feature use counter. Update feature last used date.
     * @param admin current user
     * @param feature name of used feature
     * @param date timestamp, when feature was used.
     */
    void addAdminUseOfFeature(Admin admin, String feature, Date date);
}
