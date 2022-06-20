/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.daocache;

/**
 * Interface for wrapping DAO classes by a cache.
 *
 * @param <T> type of items returned by the wrapped DAO
 */
public interface DaoCache<T> {
	
	/**
	 * Returns the item with the given ID. When the item is stored in the cache, the cached
	 * item is returned. Otherwise, the items is taken from the DAO, stored in the cache
	 * and returned.
	 * 
	 * @param id item ID
	 * 
	 * @return item
	 */
    public T getItem( int id);
}
