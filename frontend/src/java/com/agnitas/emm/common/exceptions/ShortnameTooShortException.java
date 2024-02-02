/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.common.exceptions;

public final class ShortnameTooShortException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = -7912703344957790191L;
	
	private final String shortname;
	
	public ShortnameTooShortException(final String shortname) {
		super(String.format("Shortname too short: %s", shortname));
		
		this.shortname = shortname;
	}
	
	public final String getShortname() {
		return this.shortname;
	}
	
}
