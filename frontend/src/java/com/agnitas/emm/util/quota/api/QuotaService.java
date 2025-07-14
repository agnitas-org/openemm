/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.api;

/**
 * Service interface for limiting API calls. 
 */
public interface QuotaService {

	/**
	 * Checks if API call is possible.
	 * 
	 * If API call is possible, this method terminates without exception.
	 * A {@link QuotaLimitExceededException} is thrown, if API call is not possible due to quotas.
	 * 
	 * @param username user name
	 * @param companyID company ID of user
	 * @param apiServiceName name of requested API service
	 * 
	 * @throws QuotaLimitExceededException if quota limit has been exceeded. 
	 * @throws QuotaServiceException on errors during processing
	 */
	void checkAndTrack(final String username, final int companyID, final String apiServiceName) throws QuotaLimitExceededException, QuotaServiceException;

}
