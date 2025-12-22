/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.builder.impl.exception;

/**
 * Exception indicating error during conversion of UID to its string representation.
 */
public class UIDStringBuilderException extends Exception {
	private static final long serialVersionUID = 781952222460084298L;

	/**
	 * Creates a new instance.
	 * 
	 * @param cause Throwable causing this exception
	 */
	public UIDStringBuilderException( Throwable cause) {
		super( cause);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param message Error message
	 */
	public UIDStringBuilderException( String message) {
		super( message);
	}
}
