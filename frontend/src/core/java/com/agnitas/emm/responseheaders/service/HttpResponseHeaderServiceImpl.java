/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.responseheaders.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.agnitas.emm.responseheaders.common.HttpHeaderConfig;
import com.agnitas.emm.responseheaders.common.UsedFor;
import com.agnitas.emm.responseheaders.dao.HeaderConfigCache;

public final class HttpResponseHeaderServiceImpl implements HttpResponseHeaderService {
	
	private final HeaderConfigCache cache;
	
	public HttpResponseHeaderServiceImpl(final HeaderConfigCache cache) {
		this.cache = Objects.requireNonNull(cache);
	}

	@Override
	public List<HttpHeaderConfig> listHeaderConfigsForFilter() {
		return cache.listHeaderConfigs()
				.stream()
				.filter(config -> config.getUsedFor() == UsedFor.REQUEST_FILTER)
				.collect(Collectors.toList());
	}

	@Override
	public List<HttpHeaderConfig> listHeaderConfigs(final UsedFor usedFor, final int companyID) {
		return cache.listHeaderConfigs()
				.stream()
				.filter(config -> config.getUsedFor() == usedFor && config.getCompanyID() == companyID)
				.collect(Collectors.toList());
	}

}
