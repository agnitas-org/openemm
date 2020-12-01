/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.logon.web.CannotSendSecurityCodeException;

/**
 * Interface for host authentication service.
 * 
 * This service is used to authenticate a specific host from which
 * the user tries to login.
 * 
 * For unknown hosts, the user must authenticate it by typing a security code.
 */
public interface ComHostAuthenticationService {
	
	/**
	 * Checks, if host authentication is enabled for given company ID.
	 * 
	 * @param companyID company ID to check, if host authentication is enabled.
	 * 
	 * @return true, if host authentication is enabled for given company ID, otherwise false
	 */
	boolean isHostAuthenticationEnabled( int companyID);

	/**
	 * Checks, if there is a successful authentication for user and host.
	 * 
	 * @param admin {@link ComAdmin}
	 * @param hostId host ID
	 * 
	 * @return true if host authentication is successful otherwise false
	 * 
	 * @throws HostAuthenticationServiceException on errors during authentication
	 */
	boolean isHostAuthenticated(ComAdmin admin, String hostId) throws HostAuthenticationServiceException;

	/**
	 * Sends security code to given user.
	 * 
	 * If there is already a security code and the security code is still valid (not out-dated), the same security code is re-sent.
	 * Otherwise, a new security code will be generated and sent.
	 * 
	 * If the security code cannot be sent to user (missing email address, ...) a {@link CannotSendSecurityCodeException} is thrown. 
	 * 
	 * @param admin admin to send security code to
	 * @param hostID host ID
	 *
	 * @throws CannotSendSecurityCodeException when security code cannot be sent by some reasons
	 * @throws HostAuthenticationServiceException on errors during processing
	 */
	void sendSecurityCode(ComAdmin admin, String hostID) throws HostAuthenticationServiceException;

	/**
	 * Write host authentication for given admin and host ID.
	 * 
	 * @param admin admin
	 * @param hostId host ID
	 * 
	 * @throws HostAuthenticationServiceException on errors during processing
	 */
	void writeHostAuthentication(ComAdmin admin, String hostId) throws HostAuthenticationServiceException;

	/**
	 * Get security code for pending authentication.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * 
	 * @return pending security code
	 * 
	 * @throws HostAuthenticationServiceException on errors during processing
	 */
	String getPendingSecurityCode(ComAdmin admin, String hostID) throws HostAuthenticationServiceException;

	/**
	 * Removes all expired data for host authentication. 
	 */
	void removeAllExpiredData();

	void removeAuthentictedHost(String hostId);
	
}
