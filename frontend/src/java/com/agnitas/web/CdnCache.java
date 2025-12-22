/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.TimeoutLRUMap;
public class CdnCache {
	private ConfigService configService;
	protected TimeoutLRUMap<String, CdnImage> cdnCache;

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public TimeoutLRUMap<String, CdnImage> getCdnCache() {
		if (cdnCache == null) {
			cdnCache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.CdnMaxCache), configService.getLongValue(ConfigValue.CdnMaxCacheTimeMillis));
		}

		return cdnCache;
	}

	public CdnImage get(String cacheKey) {
		return getCdnCache().get(cacheKey);
	}

	public void put(String cacheKey, CdnImage cdnImage) {
		getCdnCache().put(cacheKey, cdnImage);
	}

	public void clean() {
		getCdnCache().clean();
	}
}
