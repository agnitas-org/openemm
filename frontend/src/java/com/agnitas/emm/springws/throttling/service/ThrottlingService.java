/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.service;

import com.agnitas.emm.springws.WebserviceUserDetails;

/**
 * Service interface for limiting WS API calls. 
 */
public interface ThrottlingService {

	/**
	 * Checks if API call is possible.
	 * 
	 * @param webserviceUser WS user
	 * @param endpointName name of invoked endpoint
	 * 
	 * @return <code>true</code> if API call is possible
	 * 
	 * @throws ThrottlingServiceException on errors during processing
	 */
	boolean checkAndTrack(final WebserviceUserDetails webserviceUser, final String endpointName) throws ThrottlingServiceException;

}
