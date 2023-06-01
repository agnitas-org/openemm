/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.dao;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.supervisor.beans.Supervisor;

/**
 * Low-level database access for host authentication data.
 */
public interface ComHostAuthenticationDao {

	/**
	 * Returns security code for pending host authentication.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * 
	 * @return security code for pending authentication
	 * 
	 * @throws NoSecurityCodeHostAuthenticationDaoException if there is no pending host authentication for given admin and host
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public String getSecurityCode(Admin admin, String hostID) throws HostAuthenticationDaoException, NoSecurityCodeHostAuthenticationDaoException;

	/**
	 * Returns security code for pending host authentication.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * 
	 * @return security code for pending authentication
	 * 
	 * @throws NoSecurityCodeHostAuthenticationDaoException if there is no pending host authentication for given supervisor and host
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public String getSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException, NoSecurityCodeHostAuthenticationDaoException;

	/**
	 * Writes a security code for a pending host authentication.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * @param securityCode security code
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public void writePendingSecurityCode(Admin admin, String hostID, String securityCode) throws HostAuthenticationDaoException;
	
	/**
	 * Writes a security code for a pending host authentication.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * @param securityCode security code
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public void writePendingSecurityCode(Supervisor supervisor, String hostID, String securityCode) throws HostAuthenticationDaoException;

	/**
	 * Checks if host is authenticated for given user.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * 
	 * @return {@code true} if host is authenticated, otherwise {@code false}
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public boolean isHostAuthenticated(Admin admin, String hostID) throws HostAuthenticationDaoException;
	
	/**
	 * Checks if host is authenticated for given supervisor.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * 
	 * @return {@code true} if host is authenticated, otherwise {@code false}
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public boolean isHostAuthenticated(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException;
	
	/**
	 * Writes data for a host authentication. Either a new host authentication is written or the expiration date of an
	 * existing host authentication is updated.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * @param expiresInDays duration of validity in days of host authentication
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public void writeHostAuthentication(Admin admin, String hostID, int expiresInDays) throws HostAuthenticationDaoException;

	/**
	 * Writes data for a host authentication. Either a new host authentication is written or the expiration date of an
	 * existing host authentication is updated.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * @param expiresInDays duration of validity in days of host authentication
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public void writeHostAuthentication(Supervisor supervisor, String hostID, int expiresInDays) throws HostAuthenticationDaoException;

	/**
	 * Removes pending security code for admin and host.
	 * 
	 * @param admin admin
	 * @param hostID host ID
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public void removePendingSecurityCode(Admin admin, String hostID) throws HostAuthenticationDaoException;

	/**
	 * Removes pending security code for supervisor and host.
	 * 
	 * @param supervisor supervisor
	 * @param hostID host ID
	 * 
	 * @throws HostAuthenticationDaoException on errors during processing
	 */
	public void removePendingSecurityCode(Supervisor supervisor, String hostID) throws HostAuthenticationDaoException;

	/**
	 * Remove all expired host authentications.
	 */
	public void removeExpiredHostAuthentications();

	/**
	 * Remove all pending host authentications old than given maximum age.
	 * 
	 * @param maxPendingHostAuthenticationsAgeMinutes maximum age of a pending host authentication in minutes
	 */
	public void removeExpiredPendingsAuthentications(final int maxPendingHostAuthenticationsAgeMinutes);

	public void removeAuthentictedHost(final String hostId);
}
