/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.tokenbucket;

import io.github.bucket4j.Bucket;

/**
 * Storage for token buckets.
 */
public interface BucketManager {

	/**
	 * Returns the token bucket for given user or creates a new one.
	 * 
	 * @param username name of invoking user
	 * @param companyId company ID of invoking user
	 * 
	 * @return token bucket for given user
	 * @throws BucketManagerException on errors during processing 
	 */
	Bucket getOrCreateBucket(final String username, final int companyId) throws BucketManagerException;
	
}
