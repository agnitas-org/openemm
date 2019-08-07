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
 * Interface containing informations of a login attempt.
 */
public interface LoginData {
	
	/**
	 * Returns the ID of the login track record.
	 * 
	 * @return ID of tracking record
	 */
	public int getLoginTrackId();
	
	/** 
	 * Returns time stamp of login. 
	 *
	 * @return time stamp of login 
	 */
	public Date getLoginTime();

	/**
	 * Returns IP of login.
	 * 
	 * @return IP of login
	 */
	public String getLoginIP();

	/**
	 * Returns the login status (success, failed, ...).
	 * 
	 * @return login status
	 */
	public LoginStatus getLoginStatus();
	
	/**
	 * Returns the use rname of the login attempt.
	 * 
	 * @return user name 
	 */
	public String getUsername();
}
