/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.bean;

import java.util.Date;

import org.agnitas.emm.core.logintracking.LoginStatus;

/**
 * Implementation of {@link LoginData}.
 */
public class LoginDataImpl implements LoginData {

	/** Tracking ID. */
	private final int trackId;
	
	/** Time of login attempt. */
	private final Date loginTime;
	
	/** IP of login attempt. */
	private final String loginIP;
	
	/** Status of login attempt. */
	private final LoginStatus loginStatus;
	
	/** User name. */
	private final String username;
	
	/**
	 * Instantiates a new login data.
	 *
	 * @param trackId ID of tracking record
	 * @param loginTime the login time
	 * @param loginIP the login IP
	 * @param loginStatus the login status
	 * @param username user name
	 */
	public LoginDataImpl( int trackId, Date loginTime, String loginIP, LoginStatus loginStatus, String username) {
		this.trackId = trackId;
		this.loginTime = loginTime;
		this.loginIP = loginIP;
		this.loginStatus = loginStatus;
		this.username = username;
	}

	@Override
	public int getLoginTrackId() {
		return this.trackId;
	}
	
	@Override
	public Date getLoginTime() {
		return this.loginTime;
	}

	@Override
	public String getLoginIP() {
		return this.loginIP;
	}

	@Override
	public LoginStatus getLoginStatus() {
		return this.loginStatus;
	}

	@Override
	public String getUsername() {
		return this.username;
	}
}
