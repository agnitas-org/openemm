/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encoder;

/**
 * Excepting indicating an error during encoding data.
 */
public class EncodingException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 2289727447503506863L;

	/**
	 * Creates a new encoding exception.
	 */
	public EncodingException() {
		super();
	}

	/**
	 * Creates a new encoding exception with error message and cause.
	 * 
	 * @param message error message
	 * @param cause the cause
	 */
	public EncodingException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new encoding exception with error message.
	 * 
	 * @param message error message
	 */
	public EncodingException(final String message) {
		super(message);
	}

	/**
	 * Creates a new encoding exception with cause.
	 * 
	 * @param cause the cause
	 */
	public EncodingException(final Throwable cause) {
		super(cause);
	}
	
}
