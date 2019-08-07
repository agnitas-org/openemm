/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.useractivitylog.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.LoggedUserAction;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.useractivitylog.dao.UserActivityLogDao;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;

/**
 * Implementation of {@link UserActivityLogService}.
 * This implementation accesses the activity data in database.
 */
public class DbUserActivityLogServiceImpl implements UserActivityLogService {
	/** The logger. */
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger( DbUserActivityLogServiceImpl.class);
	
	/** Name of property holding date of user activity. */
	public static final String DATE_PROPERTY = "date";
	
	/** Name of property holding username of user activity. */
	public static final String USER_PROPERTY = "username";
	
	/** Name of property holding action of user activity. */
	public static final String ACTION_PROPERTY = "action"; 
	
	/** Name of property holding description of user activity. */
	public static final String DESCRIPTION_PROPERTY = "description";
	
	/** Name of property holding displayed name of user. */
	public static final String SHOWN_NAME_PROPERTY = "shownName";

	// ----------------------------------------------------------------------------------------------------------- Business Code
	
	@Override
	public PaginatedListImpl<LoggedUserAction> getUserActivityLogByFilter(ComAdmin admin, String username, int action, LocalDate fromDate, LocalDate toDate,
																		  String description, int pageNumber, int pageSize, String sortColumn,
																		  String sortDirection, List<AdminEntry> visibleAdmins) throws Exception {
		ZoneId zoneId = AgnUtils.getZoneId(admin);
		Date from = DateUtilities.toDate(fromDate, zoneId);
		Date to = DateUtilities.toDate(toDate.plusDays(1), zoneId);
		return userActivityLogDao.getUserActivityEntries(visibleAdmins, username, action, from, to, description, sortColumn, sortDirection, pageNumber, pageSize);
	}
	
	@Override
	public void writeUserActivityLog(ComAdmin admin, String action, String description) {
		userActivityLogDao.writeUserActivityLog(admin, action, description);
		userActivityLogDao.addAdminUseOfFeature(admin, action.trim(), new Date());
	}

	@Override
	public void writeUserActivityLog(ComAdmin admin, UserAction action) {
		writeUserActivityLog(admin, action.getAction(), action.getDescription());
	}

    @Override
    public void writeUserActivityLog(ComAdmin admin, String action, String description, Logger callerLog) {
        try {
            this.writeUserActivityLog(admin,action,description);
        } catch (Exception e) {
            callerLog.error("Error writing ActivityLog: " + e.getMessage(), e);
            callerLog.info("Userlog: " + admin.getUsername() + " " + action + " " +  description);
        }
    }

    @Override
	public void writeUserActivityLog(ComAdmin admin, UserAction action, Logger callerLog) {
		writeUserActivityLog(admin, action.getAction(), action.getDescription(), callerLog);
	}

    // ----------------------------------------------------------------------------------------------------------- Dependency Injection
	
	/** DAO for accessing user activity data. */
	private UserActivityLogDao userActivityLogDao;
	
	/**
	 * Set DAO for accessing user activity data.
	 * 
	 * @param dao DAO for accessing user activity data
	 */
	@Required
	public void setUserActivityLogDao( UserActivityLogDao dao) {
		this.userActivityLogDao = dao;
	}
}
