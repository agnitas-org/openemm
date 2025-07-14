/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.service;

import com.agnitas.emm.wsmanager.common.WebserviceUserException;

/**
 * Excepting indicating a duplicate username.
 */
public class WebserviceUserAlreadyExistsException extends WebserviceUserException {

	/** Serial version UID. */
	private static final long serialVersionUID = 8110348764495683806L;
	
	/** Duplicate username. */
	private final String username;

	/**
	 * Creates a new exception.
	 * 
	 * @param username duplicate username
	 */
	public WebserviceUserAlreadyExistsException(final String username) {
		super("Webservice user already exists: " + username);
		
		this.username = username;
	}
	
	/**
	 * Returns the duplicate username.
	 * 
	 * @return duplicate username
	 */
	public String getUsername() {
		return this.username;
	}
}
