/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.logon.service;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ClientHostIdService {
	
	/**
	 * Create a new random (and hopefully unique) host ID.
	 * 
	 * @return host ID
	 */
	public String createHostId();

	/**
	 * Returns the client host ID.
	 * {@link Optional#empty()} is returned, if client has no host ID.
	 * 
	 * @param request HTTP request
	 * 
	 * @return client host ID or {@link Optional#empty()}
	 */
	public Optional<String> getClientHostId(final HttpServletRequest request);
	
	/**
	 * Creates and publishes the cookie containing the host ID.
	 * 
	 * @param hostId host ID
	 * @param companyId company ID
	 * @param response HTTP response
	 */
	public void createAndPublishHostAuthenticationCookie(final String hostId, final int companyId, final HttpServletResponse response);
		
}
