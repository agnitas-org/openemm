/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.profilefields;

public class ProfileFieldException extends Exception {
	private static final long serialVersionUID = -3083472592154530420L;

	public ProfileFieldException() {
		super();
	}

	public ProfileFieldException(final String message,final Throwable cause) {
		super(message, cause);
	}

	public ProfileFieldException(final String message) {
		super(message);
	}

	public ProfileFieldException(final Throwable cause) {
		super(cause);
	}
}
