/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import com.agnitas.emm.springws.WebserviceUserDetails;

import io.github.bucket4j.Bucket;

/**
 * Storage for token buckets.
 */
public interface BucketManager {

	/**
	 * Returns the token bucket for given webservice user or creates a new one.
	 * 
	 * @param webserviceUser webservice user
	 * 
	 * @return token bucket for given webservice user
	 * @throws BucketManagerException on errors during processing 
	 */
	public Bucket getOrCreateBucket(final WebserviceUserDetails webserviceUser) throws BucketManagerException;
	
}
