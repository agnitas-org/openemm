/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.loginmanager.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.agnitas.emm.core.loginmanager.bean.LoginData;
import com.agnitas.emm.core.loginmanager.bean.LoginTrackData;
import com.agnitas.emm.core.loginmanager.bean.LoginTrackSettings;
import com.agnitas.emm.core.loginmanager.dao.LoginTrackDao;
import com.agnitas.emm.core.loginmanager.dao.LoginTrackSettingsDao;
import com.agnitas.emm.core.loginmanager.enums.LoginStatus;
import com.agnitas.emm.core.loginmanager.service.LoginTrackService;
import com.agnitas.util.OneOf;

/**
 * Implementation of {@link LoginTrackService}.
 */
public class LoginTrackServiceImpl implements LoginTrackService {

	/**
	 * DAO for login tracking.
	 */
	protected LoginTrackDao loginTrackDao;
	
	private LoginTrackSettingsDao settingsDao;

	/**
	 * Set DAO for login tracking.
	 *
	 * @param dao DAO for login tracking
	 */
	public final void setLoginTrackDao(final LoginTrackDao dao) {
		this.loginTrackDao = Objects.requireNonNull(dao, "LoginTrackDao is null");
	}
	
	public final void setLoginTrackSettingsDao(final LoginTrackSettingsDao dao) {
		this.settingsDao = Objects.requireNonNull(dao, "Login track settings DAO is null");
	}
	
	// -------------------------------------------------------------------------------------------------- Business Code
	@Override
	public void trackLoginSuccessful(final String ipAddress, final String username) {
		this.loginTrackDao.trackLoginStatus(ipAddress, username, LoginStatus.SUCCESS);
	}
	
	@Override
	public void trackLoginFailed(final String ipAddress, final String username) {
		this.loginTrackDao.trackLoginStatus(ipAddress, username, LoginStatus.FAIL);
	}
	
	@Override
	public void trackLoginSuccessfulButBlocked(final String ipAddress, final String username) {
		this.loginTrackDao.trackLoginStatus(ipAddress, username, LoginStatus.SUCCESS_BUT_BLOCKED);
	}
	
	/**
	 * Reads the login tracking data recorded for the IP address since given time (or all if given timestamp is <code>null</code>).
	 * 
	 * @param ipAddress IP address
	 * @param sinceOrNull timestamp or <code>null</code>
	 * 
	 * @return login tracking data
	 */
	private LoginTrackData readLoginTrackDataByIpAddress(final String ipAddress, final Date sinceOrNull) {
		final List<LoginData> loginTrackList = this.loginTrackDao.listLoginDataByIpAddress(ipAddress, sinceOrNull);
		
		return LoginTrackData.from(loginTrackList);
	}
	
	/**
	 * Reads the login tracking data recorded for the user name since given time (or all if given timestamp is <code>null</code>).
	 * 
	 * @param username user name
	 * @param sinceOrNull timestamp or <code>null</code>
	 * 
	 * @return login tracking data
	 */
	private LoginTrackData readLoginTrackDataByUsername(final String username, final Date sinceOrNull) {
		final List<LoginData> loginTrackList = this.loginTrackDao.listLoginDataByUsername(username, sinceOrNull);
		
		return LoginTrackData.from(loginTrackList);
	}

	@Override
	public boolean isIpAddressLocked(final String ipAddress, final int companyID) {
		final LoginTrackSettings settings = this.settingsDao.readLoginTrackSettings(companyID);
		
		return isIpAddressLocked(ipAddress, settings);
	}

	/**
	 * Checks, if given IP address is locked.
	 * 
	 * @param ipAddress IP address to check
	 * @param settings lock settings
	 * 
	 * @return <code>true</code> if IP address is locked, otherwise <code>false</code>
	 */
	private boolean isIpAddressLocked(final String ipAddress, final LoginTrackSettings settings) {
		// Compute time values
		final Date now = new Date();
		final long nowMillis = now.getTime();
		final Date observationStartTime = new Date(nowMillis - settings.getObservationTimeSeconds() * 1000);

		// Read the login track data
		final LoginTrackData allData = this.readLoginTrackDataByIpAddress(ipAddress, observationStartTime);
		final LoginTrackData failureData = allData.trimToFailuresBeforeSuccessfulLogin();
		
		// No failed login? IP is not locked
		if(failureData.isEmpty()) {
			return false;
		}
		
		// Count number of failed logins in observation period
		final long count = failureData.stream()
				.filter(data -> data.getLoginTime().after(observationStartTime))
				.count();
		
		// Count below maximum allowed number of failed logins? IP is not locked
		if(count <= settings.getMaxFailedLogins()) {
			return false;
		}
		
		// Maximum exceeded, check if last login far enough in the past
		final Date lockTimeEnd = new Date(failureData.getLoginData(0).getLoginTime().getTime() + settings.getLockTimeSeconds() * 1000);
		return !now.after(lockTimeEnd);
	}

	@Override
	public int countFailedLoginsSinceLastSuccess(final String username, final boolean skipFirstIfSuccess) {
		final LoginTrackData allData = this.readLoginTrackDataByUsername(username, null);

		final int startIndex = skipFirstIfSuccess && !allData.isEmpty() && allData.getLoginData(0).getLoginStatus() == LoginStatus.SUCCESS ? 1 : 0;
		
		int count = 0;
		for(int i = startIndex; i < allData.size(); i++) {
			final LoginData data = allData.getLoginData(i);
			
			if(data.getLoginStatus() == LoginStatus.FAIL || data.getLoginStatus() == LoginStatus.SUCCESS_BUT_BLOCKED) {
				count++;
			}
			
			if(data.getLoginStatus() == LoginStatus.SUCCESS) {
				break;
			}
		}
		
		return count;
	}
	
	@Override
	public Optional<LoginData> findLastSuccessfulLogin(String username, boolean skipFirstIfSuccess) {
		final LoginTrackData allData = this.readLoginTrackDataByUsername(username, null);
		final int startIndex = skipFirstIfSuccess && !allData.isEmpty() && allData.getLoginData(0).getLoginStatus() == LoginStatus.SUCCESS ? 1 : 0;
		
		return allData.stream()
				.skip(startIndex)
				.filter(loginData -> loginData.getLoginStatus() == LoginStatus.SUCCESS)
				.findFirst();
	}

	@Override
	public List<LoginData> listLoginAttemptsSince(final String username, final Date sinceOrNull) {
		final LoginTrackData allData = this.readLoginTrackDataByUsername(username, sinceOrNull);
		
		return allData.stream()
				.filter(data -> OneOf.oneObjectOf(data.getLoginStatus(), LoginStatus.SUCCESS, LoginStatus.FAIL, LoginStatus.SUCCESS_BUT_BLOCKED))
				.collect(Collectors.toList());
	}
}
