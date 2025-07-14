/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service;

/**
 * Base class for exceptions indicating errors in service layer.
 */
public class WebserviceUserServiceException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 3617398314737439684L;

	/**
	 * Instantiates a new webservice user service exception.
	 */
	public WebserviceUserServiceException() {
		super();
	}

	/**
	 * Instantiates a new webservice user service exception.
	 *
	 * @param message the message
	 */
	public WebserviceUserServiceException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new webservice user service exception.
	 *
	 * @param cause the cause
	 */
	public WebserviceUserServiceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new webservice user service exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public WebserviceUserServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
