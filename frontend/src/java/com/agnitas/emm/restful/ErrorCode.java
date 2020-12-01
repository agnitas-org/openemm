/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful;

/**
 * ErrorCodes
 */
public enum ErrorCode {
	UNKNOWN(0),                   // Unknown reason
	USER_AUTHENTICATION_ERROR(1), // Username does not exist / wrong password / user locked
	USER_AUTHORIZATION_ERROR(2),  // User has not the needed rights
	REQUEST_DATA_ERROR(3),        // Invalid data
	MAX_LOAD_EXCEED_ERROR(4),     // Too many concurrent requests
	INTERNAL_SERVER_ERROR(5);     // Server error

	private final int code;
	
	private ErrorCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
}
