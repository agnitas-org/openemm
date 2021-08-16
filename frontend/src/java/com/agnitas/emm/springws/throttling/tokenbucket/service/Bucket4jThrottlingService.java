/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.springws.WebserviceUserDetails;
import com.agnitas.emm.springws.throttling.service.ThrottlingService;
import com.agnitas.emm.springws.throttling.service.ThrottlingServiceException;
import com.agnitas.emm.springws.throttling.tokenbucket.common.WebserviceInvocationCosts;

import io.github.bucket4j.Bucket;


/**
 * <p>
 * Implementation of {@link ThrottlingService} using <a href="https://github.com/vladimir-bukhtoyarov/bucket4j">Bucket4j</a>.
 * </p>
 * 
 * <p>
 * This implementation uses an implementation of the 
 * <a href="https://en.wikipedia.org/wiki/Token_bucket">Token Bucket algorithm</a>
 * and allows multiple stages of rate limiting.
 * </p>
 */
public final class Bucket4jThrottlingService implements ThrottlingService {
	
	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(Bucket4jThrottlingService.class);

	/** Manager for token buckets. */
	private BucketManager bucketManger;
	
	/** Invocation costs for webservices. */
	private WebserviceInvocationCosts invocationCosts;
	
	@Override
	public final synchronized boolean checkAndTrack(final WebserviceUserDetails webserviceUser, final String endpointName) throws ThrottlingServiceException {
		try {
			final Bucket bucket = this.bucketManger.getOrCreateBucket(webserviceUser);
			
			// Determine costs for invocation (atleast 1 token!)
			final int costs = Math.max(1, this.invocationCosts.executionCostsForEndpoint(webserviceUser.getCompanyID(), endpointName));
			
			final boolean consumed = bucket.tryConsume(costs);
			if(!consumed) {
				LOGGER.error(String.format("WS-request rejected: rate limit exceeded. User:[%s]", webserviceUser.getUsername()));
			}
	
			return consumed;
		} catch(final BucketManagerException e) {
			throw new ThrottlingServiceException(e);
		}
	}
	
	/**
	 * Set bucket manager.
	 * 
	 * @param manager bucket manager
	 */
	@Required
	public final void setBucketManager(final BucketManager manager) {
		this.bucketManger = Objects.requireNonNull(manager, "BucketManager is null");
	}
	
	/**
	 * Set invocation costs.
	 * 
	 * @param costs invocation costs
	 */
	@Required
	public final void setWebserviceInvocationCosts(final WebserviceInvocationCosts costs) {
		this.invocationCosts = Objects.requireNonNull(costs, "WebserviceInvocationCosts is null");
	}

}
