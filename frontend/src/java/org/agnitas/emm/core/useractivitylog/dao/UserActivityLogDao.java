/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.useractivitylog.dao;

import java.util.Date;
import java.util.List;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.LoggedUserAction;

import com.agnitas.beans.ComAdmin;

/**
 * Interface for accessing user activity log.
 */
public interface UserActivityLogDao {
	void writeUserActivityLog(ComAdmin admin, String action, String description);

	PaginatedListImpl<LoggedUserAction> getUserActivityEntries(List<AdminEntry> visibleAdmins, String selectedAdmin, int selectedAction, Date from, Date to, String description, String sortColumn, String sortDirection, int pageNumber, int pageSize) throws Exception;

	/**
	 * Increment feature use counter. Update feature last used date.
	 * @param admin current user
	 * @param feature name of used feature
	 * @param date timestamp, when feature was used.
     */
	void addAdminUseOfFeature(ComAdmin admin, String feature, Date date);
}
