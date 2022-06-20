/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.daocache;

import org.agnitas.util.TimeoutLRUMap;

/**
 * Basic implementation of the DAOCache interface providing caching functionality, but only abstract access
 * to the DAO.
 *
 * @param <T> type of items returned by the DAO
 */
public abstract class AbstractCompanyBasedDaoCache<T> implements CompanyBasedDaoCache<T> {

	public static final class IdWithCompanyID {
		private final int id;
		private final int companyID;
		
		public IdWithCompanyID(final int id, final int companyID) {
			this.id = id;
			this.companyID = companyID;
		}
		
		@Override
		public final int hashCode() {
			return id + companyID;
		}
		
		@Override
		public final boolean equals(final Object obj) {
			if(!(obj instanceof IdWithCompanyID)) {
				return false;
			}
			
			final IdWithCompanyID idObj = (IdWithCompanyID) obj;
			
			return idObj.id == this.id && idObj.companyID == this.companyID;
		}
	}

	/** Cache providing aging of the stored elements. */
	private TimeoutLRUMap<IdWithCompanyID, T> cache = null;

	public AbstractCompanyBasedDaoCache() {
	}
	
	public void setCache(TimeoutLRUMap<IdWithCompanyID, T> cache) {
		this.cache = cache;
	}
	
	public boolean isCacheInitialized() {
		return cache != null;
	}

	// --------------------------------------------------- Business Logic
	@Override
	public final T getItem(final int id, final int companyID) {
		final IdWithCompanyID idOBj = new IdWithCompanyID(id, companyID);
		
		if (cache == null) {
			// When no cache is set, access DAO directly
			return getItemFromDao(id, companyID);
		} else if (cache.containsKey(idOBj)) {
			return cache.get(idOBj);
		} else {
			// Get the item from the DAO
			T item = getItemFromDao(id, companyID);
			// and put it into the cache, even if it was null
			cache.put(idOBj, item);
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
	protected abstract T getItemFromDao(final int id, final int companyID);
}
