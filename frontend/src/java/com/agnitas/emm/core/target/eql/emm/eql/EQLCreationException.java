/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.emm.eql;

public class EQLCreationException extends Exception {
	private static final long serialVersionUID = -602158478295491421L;

	public EQLCreationException(String message) {
		super(message);
	}
	
	/**
	 * Instantiates a new generate target eql exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public EQLCreationException(String message, Throwable cause) {
		super(message, cause);
	}
}
