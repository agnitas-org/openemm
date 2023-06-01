/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import org.agnitas.emm.core.logintracking.service.LoginTrackServiceException;

import com.agnitas.beans.Admin;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Helper class to save login data in HTTP request.
 */
public class LoginTrackServiceRequestHelper {
		
	/** Default value for minimum period of days for login data. */
	public static final int DEFAULT_LOGIN_MIN_PERIOD_DAYS = 14;
	
	/** Name of request attribute. */
	public static final String LOGIN_DATA_ATTRIBUTE_NAME = "login_tracking_list";
	
	public static final String FAILED_LOGINS_ATTRIBUTE_NAME = "failed_logins";

	// ---------------------------------------------------------------------- Business Code
	/**
	 * Determines the login data since the last successful login (before the current one), at least the data of the last {@minPeriodDays} days.
	 * The login data is set in HTTP request.
	 * 
	 * @param request HttpServletRequest to store login data
	 * @param admin admin to list for
	 * @param minPeriodDays minimum number of days to list login data
	 * 
	 * @throws LoginTrackServiceException on errors accessing login data
	 */
	public void setLoginTrackingDataToRequest(HttpServletRequest request, Admin admin, int minPeriodDays) throws LoginTrackServiceException {
		Calendar before14DaysCal = new GregorianCalendar();
		before14DaysCal.add(Calendar.DAY_OF_YEAR, -minPeriodDays);
		Date before14Days = before14DaysCal.getTime();
		
		final Optional<LoginData> lastLoginOptional = this.loginTrackService.findLastSuccessfulLogin(admin.getUsername(), true);
		
		List<LoginData> list;
		
		if (lastLoginOptional.isPresent()) {
			final LoginData lastLogin = lastLoginOptional.get();
			
			if(before14Days.before( lastLogin.getLoginTime())) {
				list = this.loginTrackService.listLoginAttemptsSince(admin.getUsername(), before14Days);
			} else {
				list = this.loginTrackService.listLoginAttemptsSince(admin.getUsername(), lastLogin.getLoginTime());
			}
		} else {
			list = this.loginTrackService.listLoginAttemptsSince(admin.getUsername(), before14Days);
		}
		
		request.setAttribute( LOGIN_DATA_ATTRIBUTE_NAME, list);
	}
	
	/**
	 * Removes data about failed logins from request.
	 * 
	 * @param request HTTP request
	 */
	public void removeFailedLoginWarningFromRequest( HttpServletRequest request) {
		request.getSession().removeAttribute(FAILED_LOGINS_ATTRIBUTE_NAME);
	}
	
	// ---------------------------------------------------------------------- Dependency Injection
	/** Service for accessing login tracking data. */
	private LoginTrackService loginTrackService;
	
	/**
	 * Set the service for accessing login tracking data.
	 * 
	 * @param service service for accessing login tracking data
	 */
	public void setLoginTrackService( LoginTrackService service) {
		this.loginTrackService = service;
	}

}
