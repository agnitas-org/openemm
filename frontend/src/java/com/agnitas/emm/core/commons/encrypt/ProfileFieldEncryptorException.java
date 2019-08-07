/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encrypt;

/**
 * Base class for all exceptions of the {@link ProfileFieldEncryptor}.
 */
public class ProfileFieldEncryptorException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 8175677218642176797L;

	/**
	 * Creates new exception.
	 */
	public ProfileFieldEncryptorException() {
		super();
	}

	/**
	 * Creates new exception with error message and root cause.
	 * 
	 * @param message error message
	 * @param cause the cause
	 */
	public ProfileFieldEncryptorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates new exception with error message.
	 * 
	 * @param message error message
	 */
	public ProfileFieldEncryptorException(String message) {
		super(message);
	}

	/**
	 * Creates new exception with root cause.
	 * 
	 * @param cause the cause
	 */
	public ProfileFieldEncryptorException(Throwable cause) {
		super(cause);
	}

}
