/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.service.impl;

import java.util.List;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;

import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldsCache;

public class RecipientFieldsCacheImpl implements RecipientFieldsCache {
	private ConfigService configService;
	
	private TimeoutLRUMap<Integer, List<RecipientFieldDescription>> cache;
	
	public RecipientFieldsCacheImpl(ConfigService configService) {
		this.configService = configService;
	}
	
	/**
	 * Lazy init the cache Map, otherwise the startup of the Spring context will fail
	 * @return
	 */
	private TimeoutLRUMap<Integer, List<RecipientFieldDescription>> getCacheMap() {
		if (cache == null) {
			cache = new TimeoutLRUMap<>(this.configService.getIntegerValue(ConfigValue.RecipientFieldsMaxCache), this.configService.getLongValue(ConfigValue.RecipientFieldsMaxCacheTimeMillis));
		}
		return cache;
	}

	@Override
	public void put(int companyID, List<RecipientFieldDescription> cacheValues) {
		getCacheMap().put(companyID, cacheValues);
	}

	@Override
	public List<RecipientFieldDescription> get(int companyID) {
		return getCacheMap().get(companyID);
	}
}
