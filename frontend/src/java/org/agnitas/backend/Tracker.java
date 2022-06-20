/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

/**
 * Keep track of configured tracker to add to for url tracking
 */
public class Tracker {
	private String name;
	private String code;

	/**
	 * Constructor
	 *
	 * @param name the name of the tracker
	 * @param code the tracker specific code to use
	 */
	public Tracker(String nName, String nCode) {
		name = nName;
		code = nCode;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
}

