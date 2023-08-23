/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.useractivitylog.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.useractivitylog.bean.RestfulUserActivityAction;
import com.agnitas.emm.core.useractivitylog.bean.SoapUserActivityAction;
import com.agnitas.emm.core.useractivitylog.dao.LoggedUserAction;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.emm.core.useractivitylog.dao.SoapUserActivityLogDao;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.SqlPreparedStatementManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;

import static java.text.MessageFormat.format;

/**
 * Implementation of {@link UserActivityLogService}.
 * This implementation accesses the activity data in database.
 */
public class DbUserActivityLogServiceImpl implements UserActivityLogService {
	
	private final UserActivityLogDao generalUserActivityLogDao;
	private final RestfulUserActivityLogDao restfulUserActivityLogDao;
	private final SoapUserActivityLogDao soapUserActivityLogDao;

	public DbUserActivityLogServiceImpl(UserActivityLogDao generalUserActivityLogDao, RestfulUserActivityLogDao restfulUserActivityLogDao, SoapUserActivityLogDao soapUserActivityLogDao) {
		this.generalUserActivityLogDao = generalUserActivityLogDao;
		this.restfulUserActivityLogDao = restfulUserActivityLogDao;
		this.soapUserActivityLogDao = soapUserActivityLogDao;
	}

	@Override
	public PaginatedListImpl<LoggedUserAction> getUserActivityLogByFilter(Admin admin, String username, int action, LocalDate fromDate, LocalDate toDate,
																		  String description, int pageNumber, int pageSize, String sortColumn,
																		  String sortDirection, List<AdminEntry> visibleAdmins) throws Exception {
		ZoneId zoneId = AgnUtils.getZoneId(admin);
		Date from = DateUtilities.toDate(fromDate, zoneId);
		Date to = DateUtilities.toDate(toDate.plusDays(1), zoneId);
		return generalUserActivityLogDao.getUserActivityEntries(visibleAdmins, username, action, from, to, description, sortColumn, sortDirection, pageNumber, pageSize);
	}

	@Override
	public PaginatedListImpl<RestfulUserActivityAction> getRestfulUserActivityLogByFilter(Admin admin, String username, LocalDate fromDate, LocalDate toDate,
																						  String description, int pageNumber, int pageSize, String sortColumn,
																						  String sortDirection, List<AdminEntry> visibleAdmins) throws Exception {
		ZoneId zoneId = AgnUtils.getZoneId(admin);
		Date from = DateUtilities.toDate(fromDate, zoneId);
		Date to = DateUtilities.toDate(toDate.plusDays(1), zoneId);
		return restfulUserActivityLogDao.getUserActivityEntries(visibleAdmins, username, from, to, description, sortColumn, sortDirection, pageNumber, pageSize);
	}

	@Override
	public PaginatedListImpl<SoapUserActivityAction> getSoapUserActivityLogByFilter(Admin admin, String username, LocalDate fromDate, LocalDate toDate,
																					int pageNumber, int pageSize, String sortColumn, String sortDirection,
																					List<AdminEntry> visibleAdmins) throws Exception {
		ZoneId zoneId = AgnUtils.getZoneId(admin);
		Date from = DateUtilities.toDate(fromDate, zoneId);
		Date to = DateUtilities.toDate(toDate.plusDays(1), zoneId);

		return soapUserActivityLogDao.getUserActivityEntries(visibleAdmins, username, from, to, sortColumn, sortDirection, pageNumber, pageSize);
	}

	@Override
	public SqlPreparedStatementManager prepareSqlStatementForEntriesRetrieving(List<AdminEntry> visibleAdmins, String selectedAdmin,
																			   int selectedAction, Date from, Date to, String description,
																			   UserType userType) throws Exception {
		if (UserType.REST.equals(userType)) {
			return restfulUserActivityLogDao.prepareSqlStatementForEntriesRetrieving(visibleAdmins, selectedAdmin, from, to, description);
		}

		if (UserType.SOAP.equals(userType)) {
			return soapUserActivityLogDao.prepareSqlStatementForEntriesRetrieving(visibleAdmins, selectedAdmin, from, to);
		}

		return generalUserActivityLogDao.prepareSqlStatementForEntriesRetrieving(visibleAdmins, selectedAdmin, selectedAction, from, to, description);
	}

	@Override
	public void writeUserActivityLog(Admin admin, String action, String description) {
		generalUserActivityLogDao.writeUserActivityLog(admin, action, description);
		generalUserActivityLogDao.addAdminUseOfFeature(admin, action.trim(), new Date());
	}

	@Override
	public void writeUserActivityLog(Admin admin, UserAction action) {
		writeUserActivityLog(admin, action.getAction(), action.getDescription());
	}

    @Override
    public void writeUserActivityLog(Admin admin, String action, String description, Logger callerLog) {
        try {
            this.writeUserActivityLog(admin,action,description);
        } catch (Exception e) {
            callerLog.error(format("Error writing ActivityLog: {0}", e.getMessage()), e);
            callerLog.info("Userlog: {} {} {}", admin.getUsername(), action, description);
        }
    }

    @Override
	public void writeUserActivityLog(Admin admin, UserAction action, Logger callerLog) {
		writeUserActivityLog(admin, action.getAction(), action.getDescription(), callerLog);
	}
}
