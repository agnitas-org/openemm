/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans.impl;

import org.agnitas.beans.FailedLoginData;

/**
 * Class for storing informations about failed logins.
 * Stored are number of failed logins since the last 
 * successful logins and the elapsed time since the last failed login.
 * 
 * Successful logins during a lock period are not noted.
 */
public class FailedLoginDataImpl implements FailedLoginData {
	/**
	 * Number of failed logins 
	 */
	protected int numFailedLogins;
	
	/**
	 * Elapsed time since last failed login in seconds
	 */
	protected int lastFailedLoginTimeDifference;
	
	@Override
	public int getLastFailedLoginTimeDifference() {
		return this.lastFailedLoginTimeDifference;
	}

	@Override
	public int getNumFailedLogins() {
		return this.numFailedLogins;
	}

	@Override
	public void setLastFailedLoginTimeDifference(int timeDifference) {
		this.lastFailedLoginTimeDifference = timeDifference;	
	}

	@Override
	public void setNumFailedLogins(int numFailedLogins) {
		this.numFailedLogins = numFailedLogins;
	}
}
