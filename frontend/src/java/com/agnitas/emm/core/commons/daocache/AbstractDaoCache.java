/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.daocache;

import com.agnitas.util.TimeoutLRUMap;

/**
 * Basic implementation of the DAOCache interface providing caching functionality, but only abstract access
 * to the DAO.
 *
 * @param <T> type of items returned by the DAO
 */
public abstract class AbstractDaoCache<T> implements DaoCache<T> {

	// --------------------------------------------------- Dependency Injection
	/** Cache providing aging of the stored elements. */
	private TimeoutLRUMap<Integer, T> cache;

	/**
	 * Set the cache map.
	 * 
	 * @param cache cache map
	 */
	public void setCache(TimeoutLRUMap<Integer, T> cache) {
		this.cache = cache;
	}
	
	public boolean isCacheInitialized() {
		return cache != null;
	}

	// --------------------------------------------------- Business Logic
	@Override
	public T getItem(int id) {
		if (cache == null) {
			// When no cache is set, access DAO directly 
			return getItemFromDao( id);
		} else {
			// First, try to get the requested item from the cache map
			T item = cache.get( id);

			if (item == null) {
				// If no item was returned, get the item from the DAO
				item = getItemFromDao( id);
				
				if (item != null) {
					// and put it into the cache
					cache.put( id, item);
				}
			}

			return item;
		}
	}

	/**
	 * Get item from DAO.
	 * 
	 * @param id item ID
	 * 
	 * @return item
	 */
	protected abstract T getItemFromDao(int id);
}
