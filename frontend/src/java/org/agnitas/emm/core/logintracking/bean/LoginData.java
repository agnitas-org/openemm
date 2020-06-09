/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.bean;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.agnitas.emm.core.logintracking.LoginStatus;

/**
 * Login attempt.
 */
public final class LoginData {

	/** Tracking ID. */
	private final int trackId;
	
	/** Time of login attempt. */
	private final Date loginTime;
	
	/** IP of login attempt. */
	private final String loginIP;
	
	/** Status of login attempt. */
	private final LoginStatus loginStatus;
	
	/** User name. */
	private final Optional<String> username;
	
	/**
	 * Instantiates a new login data.
	 *
	 * @param trackId ID of tracking record
	 * @param loginTime the login time
	 * @param loginIP the login IP
	 * @param loginStatus the login status
	 * @param usernameOrNull user name or <code>null</code>
	 */
	public LoginData(final int trackId, final Date loginTime, final String loginIP, final LoginStatus loginStatus, final String usernameOrNull) {
		this.trackId = trackId;
		this.loginTime = Objects.requireNonNull(loginTime);
		this.loginIP = Objects.requireNonNull(loginIP);
		this.loginStatus = Objects.requireNonNull(loginStatus);
		this.username = Optional.ofNullable(usernameOrNull);
	}

	public int getLoginTrackId() {
		return this.trackId;
	}
	
	public Date getLoginTime() {
		return this.loginTime;
	}

	public String getLoginIP() {
		return this.loginIP;
	}

	public LoginStatus getLoginStatus() {
		return this.loginStatus;
	}

	public Optional<String> getUsername() {
		return this.username;
	}
}
