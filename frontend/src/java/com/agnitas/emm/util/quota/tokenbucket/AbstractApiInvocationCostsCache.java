/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.util.quota.tokenbucket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Cache for API invocation costs.
 * 
 * This class is abstract in order to make cache-clear control more general.
 */
public abstract class AbstractApiInvocationCostsCache implements ApiInvocationCosts {
	
	/** Time at which the cache was last cleared. */
	private LocalDateTime lastCacheClearTime;
	
	/** Source for API invocation costs, when costs are not in cache. */
	private final ApiInvocationCosts costs;
	
	/** Cache. */
	private final Map<String, Integer> cacheMap;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param costs source for API invocation costs
	 */
	public AbstractApiInvocationCostsCache(final ApiInvocationCosts costs) {
		this.costs = Objects.requireNonNull(costs, "ApiInvocationCosts is null");
		this.cacheMap = new HashMap<>();
		
		this.lastCacheClearTime = LocalDateTime.now();		
	}

	@Override
	public final int invocationCostsForApi(final int companyID, final String apiServiceName) {
		final String cacheKey = toCacheKey(companyID, apiServiceName);
		
		synchronized(this.cacheMap) {
			clearCacheIfNeeded();
			
			return this.cacheMap.computeIfAbsent(cacheKey, key -> costs.invocationCostsForApi(companyID, apiServiceName));
		}
	}
	
	/**
	 * Returns a cache key from given data.
	 * 
	 * @param companyID company ID
	 * @param apiServiceName name of API service
	 * 
	 * @return cache key
	 */
	public String toCacheKey(final int companyID, final String apiServiceName) {
		return String.format("%d-%s", companyID, apiServiceName);
	}
	
	/**
	 * Clears cache if needed.
	 */
	private final void clearCacheIfNeeded() {
		final Duration timeSinceLastCacheClear = Duration.between(this.lastCacheClearTime, LocalDateTime.now());
		final long secondsSinceLastCacheClear = timeSinceLastCacheClear.getSeconds();
		
		if(shouldClearCache(secondsSinceLastCacheClear)) {
			this.cacheMap.clear();
			this.lastCacheClearTime = LocalDateTime.now();
		}
	}

	/**
	 * Returns <code>true</code>, if cache should be cleared.
	 * 
	 * @param secondsSinceLastCacheClear seconds this cache was cleared the last time.
	 * 
	 * @return <code>true</code> if cache should be cleared
	 */
	public abstract boolean shouldClearCache(final long secondsSinceLastCacheClear);
	
}
