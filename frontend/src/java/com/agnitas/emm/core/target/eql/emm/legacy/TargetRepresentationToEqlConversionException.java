/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.legacy;

/**
 * Exception indicating an error during conversion of legacy target groups to EQL.
 */
public class TargetRepresentationToEqlConversionException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 8291552812786391311L;

	/**
	 * Instantiates a new target representation to eql conversion exception.
	 */
	public TargetRepresentationToEqlConversionException() {
		super();
	}

	/**
	 * Instantiates a new target representation to eql conversion exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public TargetRepresentationToEqlConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Instantiates a new target representation to eql conversion exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public TargetRepresentationToEqlConversionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new target representation to eql conversion exception.
	 *
	 * @param message the message
	 */
	public TargetRepresentationToEqlConversionException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new target representation to eql conversion exception.
	 *
	 * @param cause the cause
	 */
	public TargetRepresentationToEqlConversionException(Throwable cause) {
		super(cause);
	}

}
