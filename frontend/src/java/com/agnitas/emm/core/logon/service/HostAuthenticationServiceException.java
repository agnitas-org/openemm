/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

/**
 * Exception indicating errors in host authentication service.
 */
public class HostAuthenticationServiceException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 1295071466222971402L;

	/**
	 * Creates a new exception.
	 */
	public HostAuthenticationServiceException() {
	}

	/**
	 * Creates a new exception with error message.
	 * 
	 * @param message error message
	 */
	public HostAuthenticationServiceException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with cause.
	 * 
	 * @param cause the cause
	 */
	public HostAuthenticationServiceException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new exception with error message and cause.
	 * 
	 * @param message error message
	 * @param cause the cause
	 */
	public HostAuthenticationServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
