/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.dao;

/**
 * Base class for any exception indicating some internal 
 * error in {@link WebserviceUserDao} implementation.
 * 
 *  This can be
 *  <ul>
 *    <li>lost DB connection</li>
 *    <li>invalid SQL statment</li>
 *    <li>Null pointers</li>
 *    <li>...</li>
 *  </ul>
 *  
 *  Not covered by this exception:
 *  <ul>
 *    <li>Unknown username</li>
 *    <li>Duplicate username</li>
 *    <li>...</li>
 *  </ul>
 *  
 */
public class WebserviceUserDaoException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 5969302823968236565L;

	/**
	 * Instantiates a new webservice user dao exception.
	 */
	public WebserviceUserDaoException() {
		super();
	}

	/**
	 * Instantiates a new webservice user dao exception.
	 *
	 * @param message the message
	 */
	public WebserviceUserDaoException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new webservice user dao exception.
	 *
	 * @param cause the cause
	 */
	public WebserviceUserDaoException(Throwable cause) {
		super(cause);
	}

	/**
	 * Instantiates a new webservice user dao exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public WebserviceUserDaoException(String message, Throwable cause) {
		super(message, cause);
	}

}
