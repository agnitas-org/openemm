/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Date;
import java.util.List;

import org.agnitas.beans.FailedLoginData;
import org.agnitas.emm.core.logintracking.bean.LoginData;

/**
 * Interface for accessing login tracking information.
 * The records contains IP-address, used user name, login status (success, failed, etc.) and a time stamp.
 */
public interface LoginTrackDao {
	/**
	 * Returns informations abound failed logins for a given IP address.
	 * @param ipAddress IP address to retrieve login informations
	 * @return login informations
	 */
	FailedLoginData getFailedLoginData(String ipAddress);
	
	/**
	 * Track successful login. All data about the login (IP address, user name, time stamp, etc.)
	 * are recorded.
	 * 
	 * @param ipAddress IP address of host
	 * @param username used user name
	 */
	void trackSuccessfulLogin(String ipAddress, String username);
	
	/**
	 * Track failed login. All data about the login (IP address, user name, time stamp, etc.)
	 * are recorded.
	 * 
	 * @param ipAddress IP address of host
	 * @param username used user name
	 */
	void trackFailedLogin(String ipAddress, String username);
	
	/**
	 * Track successful during lock period login. All data about the login (IP address, user name, time stamp, etc.)
	 * are recorded.
	 * 
	 * @param ipAddress IP address of host
	 * @param username used user name
	 */
	void trackLoginDuringBlock(String ipAddress, String username);
	
	/**
	 * Deletes old login records. A record is supposed to be old, if its time stamp exceeds the given retention time.
	 * To prevent locking the persistence system behind, a maximum number of records can be specified to be deleted 
	 * with one method call.
	 *  
	 * @param retentionTime number of days a records must be stored
	 * @param maxRecords maximum number of records to be deleted at one method call
	 * @return number of records deleted
	 */
	int deleteOldRecords(int retentionTime, int maxRecords) throws Exception;

	/**
	 * Returns the last successful login. 
	 * Returning {@code null} means that there is no successful login.
	 * If the record for the current login has already been written, set {@code skipLastSuccess} to true. Otherwise
	 * you well get the record for the current login.
	 * 
	 * @param username user name
	 * @param skipLastSuccess set to true, if record for current login has already been written
	 * 
	 * @return login track data of last successful login
	 */
	LoginData getLastSuccessfulLogin( String username, boolean skipLastSuccess);

	/**
	 * Counts the failed login attempts since a given time stamp.
	 * If {@code since} is null, all failed logins are counted.
	 * 
	 * @param username user name
	 * @param since count fails since this time
	 * 
	 * @return number of failed logins
	 */
	int countFailedLogins(String username, Date since);

	/**
	 * Returns all login attempts for given user since given time.
	 * 
	 * @param username user name
	 * @param since return all login attempts since this time
	 * 
	 * @return list of login attempts ordered descending by time stamp
	 */
	List<LoginData> getLoginAttemptsSince(String username, Date since);
	
}
