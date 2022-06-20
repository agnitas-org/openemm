/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.logintracking.service;

/**
 * Exception indicating an error in {@link LoginTrackService}.
 */
public class LoginTrackServiceException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 3936161378762014798L;

	
	/**
	 * Instantiates a new failed login service exception.
	 */
	public LoginTrackServiceException() {
		super();
	}

	/**
	 * Instantiates a new failed login service exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public LoginTrackServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new failed login service exception.
	 *
	 * @param message the message
	 */
	public LoginTrackServiceException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new failed login service exception.
	 *
	 * @param cause the cause
	 */
	public LoginTrackServiceException(Throwable cause) {
		super(cause);
	}

}
