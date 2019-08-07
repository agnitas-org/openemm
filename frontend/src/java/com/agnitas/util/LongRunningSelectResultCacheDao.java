/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.FutureTask;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.apache.log4j.Logger;

public class LongRunningSelectResultCacheDao extends BaseDaoImpl {
	private static final int DEFAULT_RESULTVALIDSECONDS = 5 * 60; // 5 minutes

	/**
	 * BIRT does not use Spring so the cache must be kept as a static variable
	 */
	private static final ConcurrentMap<TimedKey, FutureTask<List<Map<String, Object>>>> cache = new ConcurrentHashMap<>();
	
	public List<Map<String, Object>> selectLongRunning(final Logger logger, final String statement, final Object... parameter) throws Exception {
		return selectLongRunning(logger, DEFAULT_RESULTVALIDSECONDS, statement, parameter);
	}
	
	public List<Map<String, Object>> selectLongRunning(final Logger logger, int cacheTimeInSeconds, final String statement, final Object... parameter) throws Exception {
		String cacheKey = statement + "\n" + Arrays.toString(parameter);
		
		try {
			for (TimedKey timedKey : new ArrayList<TimedKey>(cache.keySet())) {
				if (timedKey.getValidUntil() != null && timedKey.getValidUntil().before(new Date())) {
					// Clean up outtimed keys. Keep locked keys with validUntil null, because they are not done yet.
					cache.remove(timedKey);
				} else if (timedKey.getKeyString().equals(cacheKey)) {
					// Found some cached entry, which executes or already executed this select
					return cache.get(timedKey).get();
				}
			}
			
			// Create new worker for cache entry
			FutureTask<List<Map<String, Object>>> futureTask = new FutureTask<>(new Callable<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> call() throws Exception {
					return select(logger, statement, parameter);
				}
			});
			TimedKey lockKey = new TimedKey(cacheKey, null);
			cache.put(lockKey, futureTask);
			futureTask.run();
			List<Map<String, Object>> result = futureTask.get();
			
			// Cache the new result
			Date validUntil = new Date(new Date().getTime() + (cacheTimeInSeconds * 1000));
			cache.put(new TimedKey(cacheKey, validUntil), futureTask);
			cache.remove(lockKey);
			
			return result;
		} catch (Exception e) {
			for (TimedKey key : new ArrayList<TimedKey>(cache.keySet())) {
				if (key.getKeyString().equals(cacheKey)) {
					cache.remove(key);
				}
			}
			throw e;
		}
	}
	
	/**
	 * Key class for caching within a Map.<br />
	 * All member variables of objects used as Map-keys MUST be unmodifiable.
	 */
	private class TimedKey {
		private final String keyString;
		private final Date validUntil;
		
		public TimedKey(String keyString, Date validUntil) {
			this.validUntil = validUntil;
			this.keyString = keyString;
		}
		
		public String getKeyString() {
			return keyString;
		}
		
		public Date getValidUntil() {
			return validUntil;
		}
	}
}
