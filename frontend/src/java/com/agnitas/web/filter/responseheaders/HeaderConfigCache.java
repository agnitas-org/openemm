/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.filter.responseheaders;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cache for header configuration.
 */
final class HeaderConfigCache {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(HeaderConfigCache.class);

	/** Source to load header configuration from. */
	private final HeaderConfigSource headerConfigSource;
	
	/** Number of milliseconds, after which the cache is outdated. */
	private final int cachePeriodMillis;
	
	/** Timestamp of last time the cache has been updated. */
	private long lastCacheUpdateMillis;
	
	/** Cached header configuration. */
	private List<HttpHeaderConfig> currentConfig;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param source source to load header configuration from
	 * @param cachePeriodMillis number of milliseconds to outdate cache
	 */
	public HeaderConfigCache(final HeaderConfigSource source, final int cachePeriodMillis) {
		this.headerConfigSource = Objects.requireNonNull(source, "HeaderConfigSource is null");
		this.cachePeriodMillis = cachePeriodMillis;
		
		listHeaderConfigs(); // Force loading data from source
	}
	
	/**
	 * Lists header configuration.
	 * 
	 * If cache data is missing or outdated, data is loaded before.
	 *  
	 * @return cached configuration
	 */
	public final List<HttpHeaderConfig> listHeaderConfigs() {
		if(loadFromSource()) {
			try {
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info("Refreshing HTTP header config cache");
				}
				
				this.currentConfig = Collections.unmodifiableList(headerConfigSource.loadHeaderConfiguration());
			} catch(final Exception e) {
				LOGGER.error("Error updating header configuration cache", e);
			}
			this.lastCacheUpdateMillis = System.currentTimeMillis();
		}
		
		return this.currentConfig;
	}
	
	/**
	 * Checks, if configuration must be loaded from source.
	 * 
	 * @return <code>true</code> if configuration must be loaded from source
	 */
	private synchronized final boolean loadFromSource() {
		/*
		 * Load from source if
		 * - cache is empty (currentConfig == null)
		 * - cache is configured not to cache (cachePeriodMillis == -1)
		 * - or cache is outdated (cachePeriodMillis > 0 && current time ahead of cache period)
		 * 
		 * Does not load from source if cache is loaded and configured to be permanent (cachePeriodMillis = 0)
		 */
		return this.currentConfig == null			
				|| (cachePeriodMillis == -1)
				|| (cachePeriodMillis > 0 && System.currentTimeMillis() > lastCacheUpdateMillis + cachePeriodMillis);
	}
}
