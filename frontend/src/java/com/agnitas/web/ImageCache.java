/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.TimeoutLRUMap;
import org.springframework.beans.factory.annotation.Required;

public class ImageCache {
	private ConfigService configService;
	protected TimeoutLRUMap<String, DeliverableImage> imageCache;

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	private TimeoutLRUMap<String, DeliverableImage> getImageCache() {
		if (imageCache == null) {
			imageCache = new TimeoutLRUMap<>(configService.getIntegerValue(ConfigValue.HostedImageMaxCache), configService.getLongValue(ConfigValue.HostedImageMaxCacheTimeMillis));
		}

		return imageCache;
	}

	public DeliverableImage get(String cacheKey) {
		return getImageCache().get(cacheKey);
	}

	public void put(String cacheKey, DeliverableImage deliverableImage) {
		getImageCache().put(cacheKey, deliverableImage);
	}

	public void put(String cacheKey, DeliverableImage deliverableImage, int timeoutMillis) {
		getImageCache().put(cacheKey, deliverableImage, timeoutMillis);
	}

	public void clean() {
		getImageCache().clean();
	}
	
	public void remove(String cacheKey) {
		getImageCache().remove(cacheKey);
	}
}
