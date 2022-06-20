/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.checks;

/**
 * Exception indicating errors on runtime checks of Velocity scripts.
 */
@Deprecated // After completion of EMM-8360, this class can be removed without replacement
public class VelocityCheckerException extends Exception {
	/** Serial version UID. */
	private static final long serialVersionUID = 2710246852864510005L;

	/**
	 * Instantiates a new velocity checker exception.
	 */
	public VelocityCheckerException() {
		// Do nothing
	}

	/**
	 * Instantiates a new velocity checker exception.
	 *
	 * @param message the message
	 */
	public VelocityCheckerException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new velocity checker exception.
	 *
	 * @param cause the cause
	 */
	public VelocityCheckerException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new velocity checker exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public VelocityCheckerException(String message, Throwable cause) {
		super(message, cause);
	}
}
