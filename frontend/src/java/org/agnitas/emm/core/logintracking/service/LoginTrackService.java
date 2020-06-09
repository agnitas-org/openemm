/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.logintracking.LoginStatus;
import org.agnitas.emm.core.logintracking.bean.LoginData;
import org.agnitas.emm.core.logintracking.service.impl.LoginTrackSortCriterion;

import com.agnitas.emm.core.loginmanager.entity.BlockedAddressData;
import com.agnitas.emm.util.SortDirection;

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
	public void trackLoginSuccessful(final String ipAddress, final String username);

	/**
	 * Write tracking record about failed login.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	public void trackLoginFailed(final String ipAddress, final String username);

	/**
	 * Write tracking record about successful login while IP address is blocked.
	 *
	 * @param ipAddress IP address
	 * @param username user name
	 */
	public void trackLoginSuccessfulButBlocked(final String ipAddress, final String username);
	
	/**
	 * Unlock given IP address.
	 * 
	 * @param ipAddress IP address to unlock
	 */
	public void unlockIpAddress(final String ipAddress);

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
	public boolean isIpAddressLocked(final String ipAddress, final int companyID);
	
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
	public int countFailedLoginsSinceLastSuccess(final String username, final boolean skipFirstIfSuccess);

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
	public Optional<LoginData> findLastSuccessfulLogin(String username, boolean skipFirstIfSuccess);
	
	/**
	 * Returns an ordered list of login informations since the given time.
	 * The list is ordered by login time descending.
	 *
	 * @param username user name to get login informations
	 * @param sinceOrNull get all login data since this time or <code>null</code> to get all login data
	 *
	 * @return list of login informations to be displayed
	 */
	public List<LoginData> listLoginAttemptsSince(final String username, final Date sinceOrNull);

	/**
	 * Returns the blocked IP address for given tracking ID.
	 * 
	 * If tracking ID is unknown or tracking ID references login data with a login state other than {@link LoginStatus#SUCCESS_BUT_BLOCKED}, {@link Optional#empty()} is returned.
	 * 
	 * @param trackingId tracking ID
	 * 
	 * @return blocked address data or {@link Optional#empty()}
	 */
	public Optional<BlockedAddressData> findBlockedAddressByTrackingID(int trackingId);
	
	/**
	 * Unlocks blocked IP address by tracking ID.
	 * 
	 * Does nothing, if tracking ID is unknown or tracking ID references login data with a login state other than {@link LoginStatus#SUCCESS_BUT_BLOCKED}.
	 * 
	 * @param trackingID tracking ID
	 * 
	 * @return <code>true</code> if IP address has been unlocked.
	 * 
	 * @see #unlockIpAddress(String)
	 */
	@Deprecated // Block by IP using unlockIpAddress()
	public boolean unlockBlockedAddressByTrackingId(final int trackingID);

	/**
	 * Lists blocked IP addresses.
	 * 
	 * @return List of blocked IP addresses
	 */
	public List<BlockedAddressData> listBlockedIpAddresses();

	// -------------------------------------------------------------------------------------------------------------------------------------------- Deprecated methods
	
	
	

	@Deprecated // TODO Remove from service. PaginatedListImpl is part of the presentation layer.
	public PaginatedListImpl<BlockedAddressData> getBlockedIPListAfterSuccessfulLogin(final LoginTrackSortCriterion criterion, final SortDirection order, int pageNumber, int pageSize);

}
