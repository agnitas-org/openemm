/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.agnitas.util.DateUtilities;

/**
 * This type of Map is used to keep the references to long running Future-Threads.
 * But in case the user, who started these Threads, does not acquire the Thread-results after a given time, the results are discarded and RAM can be cleared by GC
 * This implementation does not keep the memory perfectly clear of old data, but does prevent memory leaks with canceled GUI actions.
 *
 * @param <T>
 */
public class FutureHolderMap implements Map<String, Future<?>> {
	/**
	 * Default timeout in millis
	 */
	private static int DEFAULT_TIMEOUT_AFTER_THREAD_IS_DONE = 5000;

	private int timeoutAfterThreadIsDone;
	private Map<String, Date> timeMap = new ConcurrentHashMap<>();
	private Map<String, Future<?>> dataMap = new ConcurrentHashMap<>();

	/**
	 * Constructor with default timeout
	 */
	public FutureHolderMap() {
		timeoutAfterThreadIsDone = DEFAULT_TIMEOUT_AFTER_THREAD_IS_DONE;
	}
	
	/**
	 * Constructor to override the default timeout
	 * 
	 * @param timeoutAfterThreadIsDone
	 */
	public FutureHolderMap(int timeoutAfterThreadIsDone) {
		this.timeoutAfterThreadIsDone = timeoutAfterThreadIsDone;
	}

	@Override
	public int size() {
		return dataMap.size();
	}

	@Override
	public boolean isEmpty() {
		return dataMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return dataMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return dataMap.containsValue(value);
	}

	@Override
	public Future<?> get(Object key) {
		clearTimeoutObjectsFromDataMap();
		return dataMap.get(key);
	}

	@Override
	public Future<?> put(String key, Future<?> value) {
		clearTimeoutObjectsFromDataMap();
		return dataMap.put(key, value);
	}

	@Override
	public Future<?> remove(Object key) {
		clearTimeoutObjectsFromDataMap();
		timeMap.remove(key);
		return dataMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Future<?>> m) {
		clearTimeoutObjectsFromDataMap();
		dataMap.putAll(m);
	}

	@Override
	public void clear() {
		timeMap.clear();
		dataMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return dataMap.keySet();
	}

	@Override
	public Collection<Future<?>> values() {
		return dataMap.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Future<?>>> entrySet() {
		return dataMap.entrySet();
	}

	private void clearTimeoutObjectsFromDataMap() {
		Date timeLimit = DateUtilities.getDateOfMillisAgo(timeoutAfterThreadIsDone);
		for (Map.Entry<String, Future<?>> entry : dataMap.entrySet()) {
			Date doneTime = timeMap.get(entry.getKey());
			if (doneTime != null && doneTime.before(timeLimit)) {
				timeMap.remove(entry.getKey());
				dataMap.remove(entry.getKey());
			} else if (entry.getValue().isDone() && doneTime == null) {
				timeMap.put(entry.getKey(), new Date());
			}
		}
	}
}
