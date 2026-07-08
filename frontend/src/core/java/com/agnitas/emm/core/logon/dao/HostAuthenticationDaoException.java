/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.dao;

/**
 * Exception indicating errors in host authentication.
 */
public class HostAuthenticationDaoException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -480500488670475335L;

	/**
	 * Instantiates a new host authentication dao exception.
	 */
	public HostAuthenticationDaoException() {
		super();
	}

	/**
	 * Instantiates a new host authentication dao exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public HostAuthenticationDaoException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new host authentication dao exception.
	 *
	 * @param message the message
	 */
	public HostAuthenticationDaoException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new host authentication dao exception.
	 *
	 * @param cause the cause
	 */
	public HostAuthenticationDaoException(Throwable cause) {
		super(cause);
	}

	
}
