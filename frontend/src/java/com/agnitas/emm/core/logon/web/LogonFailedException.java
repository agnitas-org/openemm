/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.web;

import com.agnitas.emm.core.logon.service.LogonServiceException;

/**
 * Exception indication a failed login attempt.
 * 
 * Login can fail for different reasons:
 * <ul>
 *   <li>Invalid combination of username and password</li>
 *   <li>Blocked account due to too many failed login attempts.</li>
 *   <li>Disabled account</li>
 *   <li>...</li>
 * </ul>
 */
public class LogonFailedException extends LogonServiceException {
		
	/** Serial version UID. */
	private static final long serialVersionUID = -8857275136682031996L;
	private boolean supervisorLogon;
	
	/**
	 * Creates a new exception.
	 * @param supervisorLogon 
	 */
	public LogonFailedException(boolean supervisorLogon) {
		super("Login failed");
		this.supervisorLogon = supervisorLogon;
	}

	public boolean isSupervisorLogon() {
		return supervisorLogon;
	}
}
