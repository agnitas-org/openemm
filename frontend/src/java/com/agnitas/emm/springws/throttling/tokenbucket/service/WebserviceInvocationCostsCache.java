/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.springws.throttling.tokenbucket.service;

import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;

import com.agnitas.emm.util.quota.tokenbucket.AbstractApiInvocationCostsCache;
import com.agnitas.emm.util.quota.tokenbucket.ApiInvocationCosts;

public final class WebserviceInvocationCostsCache extends AbstractApiInvocationCostsCache {

	private final ConfigService configService;
	
	public WebserviceInvocationCostsCache(final ApiInvocationCosts costs, final ConfigService configService) {
		super(costs);

		this.configService = Objects.requireNonNull(configService, "ConfigService is null");
	}

	@Override
	public final boolean shouldClearCache(final long secondsSinceLastCacheClear) {
		return secondsSinceLastCacheClear > this.configService.getIntegerValue(ConfigValue.Webservices.WebserviceCostsCacheRetentionSeconds);
	}
	
}
