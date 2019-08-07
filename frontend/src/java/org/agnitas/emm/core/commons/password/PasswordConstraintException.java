/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.password;

/**
 * Exception indication violation of a contraint on passwords.
 */
public class PasswordConstraintException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 6551291765269605934L;

	/**
	 * Instantiates a new password constraint exception.
	 */
	public PasswordConstraintException() {
		super();
	}

	/**
	 * Instantiates a new password constraint exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public PasswordConstraintException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new password constraint exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public PasswordConstraintException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new password constraint exception.
	 *
	 * @param message the message
	 */
	public PasswordConstraintException(String message) {
		super(message);
	}
	
	/**
	 * Instantiates a new password constraint exception.
	 *
	 * @param cause the cause
	 */
	public PasswordConstraintException(Throwable cause) {
		super(cause);
	}

}
