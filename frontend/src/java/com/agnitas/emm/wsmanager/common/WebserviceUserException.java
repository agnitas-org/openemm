/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.common;

/**
 * Base class for any exceptions indicating some problem with
 * a webservice user (like unknown username).
 */
public class WebserviceUserException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 2180723311087668575L;

	/**
	 * Instantiates a new webservice user exception.
	 */
	public WebserviceUserException() {
		super();
	}

	/**
	 * Instantiates a new webservice user exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public WebserviceUserException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new webservice user exception.
	 *
	 * @param message the message
	 */
	public WebserviceUserException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new webservice user exception.
	 *
	 * @param cause the cause
	 */
	public WebserviceUserException(Throwable cause) {
		super(cause);
	}

}
