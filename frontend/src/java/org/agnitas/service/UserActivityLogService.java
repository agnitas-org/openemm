/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.time.LocalDate;
import java.util.List;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.LoggedUserAction;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;

public interface UserActivityLogService {
	PaginatedListImpl<LoggedUserAction> getUserActivityLogByFilter(Admin admin, String username, int action, LocalDate fromDate, LocalDate toDate, String description, int pageNumber, int rownums, String sort, String direction, List<AdminEntry> admins) throws Exception;

	/**
	 * Write user activity log for given {@link Admin}.
	 *
	 * @param admin admin
	 * @param action action
	 * @param description description of action
	 */
	void writeUserActivityLog(Admin admin, String action, String description);

	/**
	 * Write user activity log for given {@link Admin}.
	 *
	 * @param admin admin
	 * @param action user action
	 */
	void writeUserActivityLog(Admin admin, UserAction action);

    /**
     * Write user activity log for given {@link Admin}.
     *
     * Trace errors in case of service failure to caller log.
     *
     * @param admin admin
     * @param action action
     * @param description description of action
     * @param callerLog caller logging class to report in case of service failure
     */
    void writeUserActivityLog(Admin admin, String action, String description, Logger callerLog);

    /**
     * Write user activity log for given {@link Admin}.
     *
     * Trace errors in case of service failure to caller log.
     *
     * @param admin admin
     * @param action user action
     * @param callerLog caller logging class to report in case of service failure
     */
    void writeUserActivityLog(Admin admin, UserAction action, Logger callerLog);
}
