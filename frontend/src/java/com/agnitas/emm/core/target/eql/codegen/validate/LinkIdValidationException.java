/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.validate;

/**
 * Exception indicating some error when trying to validate some link ID.
 */
public class LinkIdValidationException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 1496980606272896161L;

	/**
	 * Instantiates a new link id validation exception.
	 */
	public LinkIdValidationException() {
		super();
	}

	/**
	 * Instantiates a new link id validation exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public LinkIdValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new link id validation exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public LinkIdValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new link id validation exception.
	 *
	 * @param message the message
	 */
	public LinkIdValidationException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new link id validation exception.
	 *
	 * @param cause the cause
	 */
	public LinkIdValidationException(Throwable cause) {
		super(cause);
	}

}
