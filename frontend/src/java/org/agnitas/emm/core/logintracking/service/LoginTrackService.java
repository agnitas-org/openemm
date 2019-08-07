/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.service;

import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.loginmanager.dto.BlockedAddressDto;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.logintracking.bean.LoginData;

/**
 * Service for handling login data.
 */
public interface LoginTrackService {
	
	/**
	 * Returns the number of failed logins since the last successful login.
	 * Set {@code skipLastSuccess} if the method is called after the record for the current login has been written, otherwise you will
	 * get 0.
	 *
	 * @param username user name to check
	 * @param skipLastSuccess set to true, if record for current login has already been written
	 *
	 * @return number of failed logins since last successful
	 *
	 * @throws LoginTrackServiceException on errors accessing login data
	 */
	int getNumFailedLoginsSinceLastSuccessful( String username, boolean skipLastSuccess) throws LoginTrackServiceException;
	
	/**
	 * Returns an ordered list of login informations since the given time.
	 * The list is ordered by login time descending.
	 *
	 * @param username user name to get login informations
	 * @param since get all login data since this time
	 *
	 * @return list of login informations to be displayed
	 *
	 * @throws LoginTrackServiceException on errors accessing login data
	 */
	List<LoginData> getLoginData(String username, Date since) throws LoginTrackServiceException;

	/**
	 * Returns the  data of the last successful login.
	 * Set {@code skipLastSuccess} if the method is called after the record for the current login has been written, otherwise you will
	 * get the current login.
	 *
	 * @param username user name
	 * @param skipLastSuccess set to true, if record for current login has already been written
	 *
	 * @return last successful login or null
	 *
	 * @throws LoginTrackServiceException on errors accessing login data
	 */
	LoginData getLastSuccessfulLogin(String username, boolean skipLastSuccess) throws LoginTrackServiceException;

	/**
	 * Check if given IP is blocked.
	 *
	 * @param hostIpAddress IP address
	 * @param maxLoginFails maximum number of failed login allowed before an IP gets blocked
	 * @param loginBlockTime number of seconds an IP is blocked since last failed attempt
	 *
	 * @return true if IP is blocked otherwise false
	 */
	boolean isIPLogonBlocked(String hostIpAddress, int maxLoginFails, int loginBlockTime);

	/**
	 * Write tracking record about successful login.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	void trackLoginSuccessful(String ipAddress, String username);

	/**
	 * Write tracking record about failed login.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	void trackLoginFailed(String ipAddress, String username);

	/**
	 * Write tracking record about successful login while IP address is blocked.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	void trackLoginSuccessfulButBlocked(String ipAddress, String username);
	
	default boolean unlockBlockedAddress(int blockedAddressId) {
		throw new UnsupportedOperationException("Not yet supported!");
	}
	
	default PaginatedListImpl<BlockedAddressDto> getBlockedIPListAfterSuccessfulLogin(String sort, String order, int pageNumber, int pageSize) {
		throw new UnsupportedOperationException("Not yet supported!");
	}
	
	default BlockedAddressDto getBlockedAddress(int blockedIpAddressId) {
		throw new UnsupportedOperationException("Not yet supported!");
	}
}
