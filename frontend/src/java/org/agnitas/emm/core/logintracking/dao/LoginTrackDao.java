/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.agnitas.emm.core.logintracking.LoginStatus;
import org.agnitas.emm.core.logintracking.bean.LoginData;

/**
 * Interface for accessing login tracking information.
 * The records contains IP-address, used user name, login status (success, failed, etc.) and a time stamp.
 */
public interface LoginTrackDao {
	
	/**
	 * Tracks given login status. All data about the login (IP address, user name, time stamp, etc.)
	 * are recorded.
	 * 
	 * @param ipAddress IP address of host
	 * @param username used user name
	 * @param loginStatus loginStatus
	 */
	public void trackLoginStatus(final String ipAddress, final String username, final LoginStatus loginStatus);
	
	/**
	 * Lists all login records since given date.
	 * 
	 * @param sinceOrNull timestamp of oldest record or <code>null</code> to list all records
	 * 
	 * @return list of login records
	 */
	public List<LoginData> listLoginData(final Date sinceOrNull);
	
	/**
	 * Lists all login records since given timestamp for a given IP address.
	 * List is sorted descending by timestamp.
	 * 
	 * @param ipAddress IP address
	 * @param sinceOrNull timestamp of oldest record or <code>null</code> to list all records
	 * 
	 * @return list of logins since given timestamp
	 */
	public List<LoginData> listLoginDataByIpAddress(final String ipAddress, final Date sinceOrNull);
	
	/**
	 * Lists all login records since given timestamp for a given user name.
	 * List is sorted descending by timestamp.
	 * 
	 * @param username user name
	 * @param sinceOrNull timestamp of oldest record or <code>null</code> to list all records
	 * 
	 * @return list of logins since given timestamp
	 */
	public List<LoginData> listLoginDataByUsername(final String username, final Date sinceOrNull);

	/**
	 * Deletes old login records. A record is supposed to be old, if its time stamp exceeds the given retention time.
	 * To prevent locking the persistence system behind, a maximum number of records can be specified to be deleted 
	 * with one method call.
	 *  
	 * @param retentionTime number of days a records must be stored
	 * @param maxRecords maximum number of records to be deleted at one method call
	 * @return number of records deleted
	 */
	public int deleteOldRecords(int retentionTime, int maxRecords);
	
	/**
	 * Returns login tracking data by tracking ID.
	 * If tracking ID is unknown, {@link Optional#empty()} is returned.
	 * 
	 * @param trackingID tracking ID
	 * 
	 * @return Optional containing tracking data or empty Optional
	 */
	public Optional<LoginData> findLoginDataByTrackingID(final int trackingID);

}
