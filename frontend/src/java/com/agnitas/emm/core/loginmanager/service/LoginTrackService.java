/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.agnitas.emm.core.loginmanager.bean.LoginData;

/**
 * Service for handling login data.
 */
public interface LoginTrackService {

	/**
	 * Write tracking record about successful login.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	void trackLoginSuccessful(final String ipAddress, final String username);

	/**
	 * Write tracking record about failed login.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	void trackLoginFailed(final String ipAddress, final String username);

	/**
	 * Write tracking record about successful login while IP address is blocked.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	void trackLoginSuccessfulButBlocked(final String ipAddress, final String username);
	
	/**
	 * Checks, if given IP address is locked.
	 * 
	 * Maximum number of failed logins, observation period and lock duration is determined by given company ID.
	 * 
	 * @param ipAddress IP address to check
	 * @param companyID ID of company (or 0 for fallback)
	 * 
	 * @return <code>true</code> if IP address is locked, otherwise <code>false</code>
	 */
	boolean isIpAddressLocked(final String ipAddress, final int companyID);
	
	/**
	 * Counts the number of failed logins since last successful login.
	 * 
	 * Set {@code skipLastSuccess} if the method is called after the record for the current login has been written, otherwise you will
	 * get 0.
	 * 
	 * @param username user name to check
	 * @param skipFirstIfSuccess set to <code>true</code>, the first records is skipped, if it represents a successful login
	 *
	 * @return number of failed logins since last successful
	 */
	int countFailedLoginsSinceLastSuccess(final String username, final boolean skipFirstIfSuccess);

	/**
	 * Returns the  data of the last successful login.
	 * Set {@code skipLastSuccess} if the method is called after the record for the current login has been written, otherwise you will
	 * get the current login.
	 *
	 * @param username user name
	 * @param skipFirstIfSuccess set to <code>true</code>, the first records is skipped, if it represents a successful login
	 *
	 * @return last successful login or null
	 */
	Optional<LoginData> findLastSuccessfulLogin(String username, boolean skipFirstIfSuccess);
	
	/**
	 * Returns an ordered list of login informations since the given time.
	 * The list is ordered by login time descending.
	 *
	 * @param username user name to get login informations
	 * @param sinceOrNull get all login data since this time or <code>null</code> to get all login data
	 *
	 * @return list of login informations to be displayed
	 */
	List<LoginData> listLoginAttemptsSince(final String username, final Date sinceOrNull);
}
