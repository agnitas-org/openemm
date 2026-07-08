/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.wsmanager.bean;

import java.util.Optional;
import java.util.OptionalInt;

public final class WebserviceUserSettings {
	
	private final int defaultDataSourceID;
	private final OptionalInt requestRateLimit;
	private final OptionalInt bulkSizeLimit;
	private final OptionalInt maxResultListSize;
	private final Optional<String> apiCallLimits;
	
	public WebserviceUserSettings(final int defaultDataSourceID, final Integer requestRateLimitOrNull, final Integer bulkSizeLimitOrNull, final Integer maxResultListSizeOrNull, final String apiCallLimits) {
		this.defaultDataSourceID = defaultDataSourceID;
		this.requestRateLimit = requestRateLimitOrNull != null ? OptionalInt.of(requestRateLimitOrNull) : OptionalInt.empty();
		this.bulkSizeLimit = bulkSizeLimitOrNull != null ? OptionalInt.of(bulkSizeLimitOrNull) : OptionalInt.empty();
		this.maxResultListSize = maxResultListSizeOrNull != null ? OptionalInt.of(maxResultListSizeOrNull) : OptionalInt.empty();
		this.apiCallLimits = Optional.ofNullable(apiCallLimits);
	}

	public final int getDefaultDataSourceID() {
		return defaultDataSourceID;
	}

	public final OptionalInt getRequestRateLimit() {
		return requestRateLimit;
	}

	public final OptionalInt getBulkSizeLimit() {
		return bulkSizeLimit;
	}

	public final OptionalInt getMaxResultListSize() {
		return maxResultListSize;
	}

	public final Optional<String> getApiCallLimitsSpec() {
		return this.apiCallLimits;
	}

}
