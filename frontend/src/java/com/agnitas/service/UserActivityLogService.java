/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;
import java.util.Set;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.useractivitylog.bean.RestfulUserActivityAction;
import com.agnitas.emm.core.useractivitylog.bean.SoapUserActivityAction;
import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import com.agnitas.emm.core.useractivitylog.dao.LoggedUserAction;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.SoapUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.util.SqlPreparedStatementManager;
import org.apache.logging.log4j.Logger;

public interface UserActivityLogService {

	enum UserType {
		GUI, SOAP, REST
	}

	void deleteActivity(List<Admin> admins);

	void deleteSoapActivity(Set<String> usernames);

	Set<String> getAvailableUsernames(Admin admin, UserType userType);

	PaginatedList<LoggedUserAction> getUserActivityLogByFilter(UserActivityLogFilter filter, Admin admin);

	PaginatedList<RestfulUserActivityAction> getRestfulUserActivityLogByFilter(RestfulUserActivityLogFilter filter, Admin admin);

	PaginatedList<SoapUserActivityAction> getSoapUserActivityLogByFilter(SoapUserActivityLogFilter filter, Admin admin);

	SqlPreparedStatementManager prepareSqlStatementForDownload(UserActivityLogFilterBase filter, UserType userType, Admin admin);

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
