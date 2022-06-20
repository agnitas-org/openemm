/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.servicemail;

/**
 * Exception indicating some error with an operation on service mails.
 */
public class ServiceMailException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -181771381767018865L;

	/**
	 * Creates new exception.
	 */
	public ServiceMailException() {
		// Nothing to do here
	}

	/**
	 * Creates new exception with error message.
	 * @param message error message
	 */
	public ServiceMailException(String message) {
		super(message);
	}

	/**
	 * Creates new exception with cause.
	 * 
	 * @param cause cause
	 */
	public ServiceMailException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates new exception with error message and cause.
	 * 
	 * @param message error message
	 * @param cause cause
	 */
	public ServiceMailException(String message, Throwable cause) {
		super(message, cause);
	}

}
