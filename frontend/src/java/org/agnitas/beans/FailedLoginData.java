/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

/**
 * Interface for retrieving informations about failed logins.
 * These informations are
 * - number of failed logins since last successful login (Consider this like a counter
 *   that get reset to 0 in successful logins.)
 * - elapsed time since last failed login
 * 
 * Successful logins during a lock period are not noted.
 */
public interface FailedLoginData {
	/**
	 * Returns the number of failed logins since last successful login.
	 * 
	 * @return number of failed logins
	 */
	public int getNumFailedLogins();
	
	/**
	 * Returns elapsed time since last failed login.
	 * 
	 * @return elapsed time since last failed login in seconds
	 */
	public int getLastFailedLoginTimeDifference();
	
	/**
	 * Set number of failed logins since last login.
	 * 
	 * @param numFailedLogins number of failed logins since last login 
	 */
	public void setNumFailedLogins(int numFailedLogins);
	
	/**
	 * Set elapsed time since last failed login
	 * @param timeDifference elapsed time since last failed login in seconds 
	 */
	public void setLastFailedLoginTimeDifference(int timeDifference);
}
