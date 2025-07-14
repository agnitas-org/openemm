/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.tokenbucket;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.agnitas.emm.util.quota.api.QuotaLimitExceededException;
import com.agnitas.emm.util.quota.api.QuotaService;
import com.agnitas.emm.util.quota.api.QuotaServiceException;

import io.github.bucket4j.Bucket;


/**
 * <p>
 * Implementation of {@link QuotaService} using <a href="https://github.com/vladimir-bukhtoyarov/bucket4j">Bucket4j</a>.
 * </p>
 * 
 * <p>
 * This implementation uses an implementation of the 
 * <a href="https://en.wikipedia.org/wiki/Token_bucket">Token Bucket algorithm</a>
 * and allows multiple stages of rate limiting.
 * </p>
 */
public final class Bucket4jQuotaService implements QuotaService {
	
	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(Bucket4jQuotaService.class);

	/** Manager for token buckets. */
	private BucketManager bucketManger;
	
	/** Invocation costs for webservices. */
	private ApiInvocationCosts invocationCosts;
	
	
	
	@Override
	public final void checkAndTrack(final String username, final int companyID, final String apiServiceName) throws QuotaLimitExceededException, QuotaServiceException {
		try {
			final Bucket bucket = this.bucketManger.getOrCreateBucket(username, companyID);
			
			// Determine costs for invocation (atleast 1 token!)
			final int costs = Math.max(1, this.invocationCosts.invocationCostsForApi(companyID, apiServiceName));
			
			final boolean consumed = bucket.tryConsume(costs);
			if(!consumed) {
				LOGGER.error(String.format("API quota exceeded. User:[%s]", username));
				
				throw new QuotaLimitExceededException(username, companyID, apiServiceName);
			}
			
		} catch(final BucketManagerException e) {
			throw new QuotaServiceException(e);
		}
	}
	
	/**
	 * Set bucket manager.
	 * 
	 * @param manager bucket manager
	 */
	public final void setBucketManager(final BucketManager manager) {
		this.bucketManger = Objects.requireNonNull(manager, "BucketManager is null");
	}
	
	/**
	 * Set invocation costs.
	 * 
	 * @param costs invocation costs
	 */
	public final void setApiInvocationCosts(final ApiInvocationCosts costs) {
		this.invocationCosts = Objects.requireNonNull(costs, "ApiInvocationCosts is null");
	}

}
