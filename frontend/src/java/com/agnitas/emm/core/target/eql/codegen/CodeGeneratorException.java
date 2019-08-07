/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen;

/**
 * Exception indicating an error during code generation.
 */
public class CodeGeneratorException extends Exception { // Made "abstract" to avoid direct instantiation of this class. Use sub-classes!

	/** Serial version UID. */
	private static final long serialVersionUID = -8137215251854992310L;

	/**
	 * Instantiates a new code generator exception.
	 */
	public CodeGeneratorException() {
		// Nothing to do here
	}

	/**
	 * Instantiates a new code generator exception.
	 *
	 * @param message the message
	 */
	public CodeGeneratorException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new code generator exception.
	 *
	 * @param cause the cause
	 */
	public CodeGeneratorException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new code generator exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public CodeGeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new code generator exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param enableSuppression the enable suppression
	 * @param writableStackTrace the writable stack trace
	 */
	public CodeGeneratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
