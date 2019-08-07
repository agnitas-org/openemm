/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking;

/**
 * Enumeration containing all login status to be displayed.
 */
public enum LoginStatus {
	/** Successful login. */
	SUCCESS(10),
	
	/** Failed login. */
	FAIL(20),

	/** Successful login, but blocked due to security restrictions. */
	SUCCESS_BUT_BLOCKED(40);
	
	/** Code of status. */
	private final int statusCode;
	
	/**
	 * Creates a new login status. 
	 * 
	 * @param statusCode code of login status
	 */
	LoginStatus( int statusCode) {
		this.statusCode = statusCode;
	}
	
	/**
	 * Returns the status code of the login status.
	 * 
	 * @return status code of login status
	 */
	public int getStatusCode() {
		return this.statusCode;
	}
	
	/**
	 * Converts the status code to the login status.
	 * If the status code is invalid, null is returned.
	 * 
	 * @param code status code
	 * 
	 * @return LoginStatus or null
	 */
	public static LoginStatus getLoginStatusFromStatusCode( int code) {
		for( LoginStatus status : LoginStatus.values())
			if( status.getStatusCode() == code)
				return status;
		
		return null;
	}
}
