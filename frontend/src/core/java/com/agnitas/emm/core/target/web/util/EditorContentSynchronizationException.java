/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.web.util;

/**
 * Base Exception class indicating an error while synchronizing the data of all
 * target group editors.
 */
public class EditorContentSynchronizationException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -359566965030420748L;

	/**
	 * Creates a new exception.
	 */
	public EditorContentSynchronizationException() {
		super();
	}

	/**
	 * Creates a new exception with error message and cause.
	 * 
	 * @param message error message
	 * @param cause cause
	 */
	public EditorContentSynchronizationException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new exception with error message.
	 * 
	 * @param message error message
	 */
	public EditorContentSynchronizationException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with cause.
	 * 
	 * @param cause cause
	 */
	public EditorContentSynchronizationException(Throwable cause) {
		super(cause);
	}

}
