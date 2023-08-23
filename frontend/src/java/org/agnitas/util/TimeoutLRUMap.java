/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.map.LRUMap;

public class TimeoutLRUMap<K, V> implements java.io.Serializable {
	private static final long serialVersionUID = -1755144418309829988L;

	/**
	 * Holds value of property timeout.
	 */
	private long timeoutInMillis;

	private LRUMap<K, TimeoutObject> internalMap;

	/**
	 * Creates a new instance of TimeoutLRUMap
	 */
	public TimeoutLRUMap() {
		timeoutInMillis = 5000; // Milliseconds
		internalMap = new LRUMap<>(1000);
	}

	/**
	 * Creates a new instance of TimeoutLRUMap
	 * 
	 * @param capacity
	 * @param timeoutInMillis
	 */
	public TimeoutLRUMap(int capacity, long timeoutInMillis) {
		setTimeout(timeoutInMillis);
		internalMap = new LRUMap<>(capacity);
	}

	/**
	 * Saves a key with value and default timeout period
	 */
	public synchronized K put(K key, V value) {
		return put(key, value, timeoutInMillis);
	}


	/**
	 * WARNING: use {@link #generateContentImageKey(int, int)} for generation key or implement another one here.
	 * Saves a key with value and explicit timeout period.
	 *
	 * @param key            - key of entry.
	 * @param value          - cached value.
	 * @param validityPeriod - time to live of cached entry.
	 * @return key.
	 */
	public synchronized K put(K key, V value, long validityPeriod) {
		TimeoutObject aObject = new TimeoutObject();
		aObject.object = value;
		aObject.validUntil = System.currentTimeMillis() + validityPeriod;
		internalMap.put(key, aObject);
		return key;
	}

	/**
	 * Gets the value from a key
	 */
	public synchronized V get(K key) {
		TimeoutObject timeoutObject = internalMap.get(key);
		if (timeoutObject != null) {
			if (System.currentTimeMillis() < timeoutObject.validUntil) {
				return timeoutObject.object;
			} else {
				internalMap.remove(key);
			}
		}
		return null;
	}

	/**
	 * Check for existence of key
	 */
	public synchronized boolean containsKey (K key) {
		TimeoutObject timeoutObject = internalMap.get(key);

		if (timeoutObject == null) {
			return false;
		}

		if (System.currentTimeMillis() < timeoutObject.validUntil) {
			return true;
		} else {
			internalMap.remove(key);
			return false;
		}
	}

	/**
	 * Removes unused objects.
	 */
	public synchronized int cleanupGarbage() {
		int removedCount = 0;
		long time = System.currentTimeMillis();

		// Watch out, because every get() may change the internalMap.
		// Therefore we store the keys in a separate list and iterate that, instead of iterating over the keyset.
		List<K> keyList = new ArrayList<>(internalMap.keySet());
		
		for (K key : keyList) {
			TimeoutObject aObject = internalMap.get(key);
			if (time >= aObject.validUntil) {
				internalMap.remove(key);
				removedCount++;
			}
		}

		return removedCount;
	}

    /**
     * Clean cache.
     */
    public synchronized void clean(){
        internalMap.clear();
    }

	/**
	 * Removes cached entry. May be Used on updating.
	 *
	 * @param key - key of cached entry.
	 */
	public synchronized void remove(K key) {
		internalMap.remove(key);
	}

	/**
	 * Getter for property timeout.
	 * 
	 * @return Value of property timeout.
	 * 
	 */
	public long getTimeout() {
		return timeoutInMillis;
	}

	/**
	 * Setter for property timeout.
	 * 
	 * @param timeoutInMillis
	 *            New value of property timeout.
	 */
	public void setTimeout(long timeoutInMillis) {
		this.timeoutInMillis = timeoutInMillis;
	}

	public synchronized int size() {
		return internalMap.size();
	}

	private class TimeoutObject {
		public V object;
		public long validUntil;
	}

	/**
	 * Generates entry key for media pool content images.
	 *
	 * @param companyID - id of current company
	 * @param imageName - name of requested image
	 * @return key for new entry
	 */
	public static String generateContentImageKey(int companyID, String imageName) {
		StringBuilder cacheKeyBuilder = new StringBuilder();
		cacheKeyBuilder.append("media-");
		cacheKeyBuilder.append(companyID);
		cacheKeyBuilder.append("-");
		cacheKeyBuilder.append(imageName);
		return cacheKeyBuilder.toString();
	}
}
