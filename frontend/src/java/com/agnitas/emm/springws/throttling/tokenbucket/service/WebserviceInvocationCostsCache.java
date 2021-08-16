/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;

import com.agnitas.emm.springws.throttling.tokenbucket.common.WebserviceInvocationCosts;

public final class WebserviceInvocationCostsCache implements WebserviceInvocationCosts {
	
	private LocalDateTime lastCacheClearTime;
	private final WebserviceInvocationCosts costs;
	private final ConfigService configService;
	private final Map<String, Integer> cacheMap;
	
	public WebserviceInvocationCostsCache(final WebserviceInvocationCosts costs, final ConfigService configService) {
		this.costs = Objects.requireNonNull(costs, "WebserviceInvocationCosts is null");
		this.configService = Objects.requireNonNull(configService, "ConfigService is null");
		this.cacheMap = new HashMap<>();
		
		this.lastCacheClearTime = LocalDateTime.now();		
	}

	@Override
	public final int executionCostsForEndpoint(final int companyID, final String endpointName) {
		final String cacheKey = toCacheKey(companyID, endpointName);
		
		synchronized(this.cacheMap) {
			clearCacheIfNeeded();
			
			return this.cacheMap.computeIfAbsent(cacheKey, key -> costs.executionCostsForEndpoint(companyID, endpointName));
		}
	}
	
	private static final String toCacheKey(final int companyID, final String endpointName) {
		return String.format("%d-%s", companyID, endpointName);
	}
	
	private final void clearCacheIfNeeded() {
		final Duration timeSinceLastCacheClear = Duration.between(this.lastCacheClearTime, LocalDateTime.now());
		final long secondsSinceLastCacheClear = timeSinceLastCacheClear.toMillis() / 1000;
		
		if(secondsSinceLastCacheClear > this.configService.getIntegerValue(ConfigValue.Webservices.WebserviceCostsCacheRetentionSeconds)) {
			this.cacheMap.clear();
			this.lastCacheClearTime = LocalDateTime.now();
		}
	}
	
}
