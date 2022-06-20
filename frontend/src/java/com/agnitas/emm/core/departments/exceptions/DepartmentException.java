/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.departments.exceptions;

/**
 * Exception indicating an error with departments.
 */
public class DepartmentException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -4325633322744221217L;

	/**
	 * Instantiates a new department exception.
	 */
	public DepartmentException() {
		super();	
	}

	/**
	 * Instantiates a new department exception.
	 *
	 * @param message the message
	 */
	public DepartmentException(final String message) {
		super(message);
	}

	/**
	 * Instantiates a new department exception.
	 *
	 * @param cause the cause
	 */
	public DepartmentException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new department exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public DepartmentException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
