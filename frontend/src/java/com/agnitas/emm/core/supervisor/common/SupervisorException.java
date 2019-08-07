/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.common;

/**
 * Exception indicating errors with supervisors.
 */
public class SupervisorException extends Exception {
	
	/** Serial version UID. */
	private static final long serialVersionUID = -8868226750250844606L;

	/**
	 * Instantiates a new supervisor exception.
	 */
	public SupervisorException() {
		// Nothing to do here
	}

	/**
	 * Instantiates a new supervisor exception.
	 *
	 * @param message the message
	 */
	public SupervisorException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new supervisor exception.
	 *
	 * @param cause the cause
	 */
	public SupervisorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new supervisor exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public SupervisorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new supervisor exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public SupervisorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
