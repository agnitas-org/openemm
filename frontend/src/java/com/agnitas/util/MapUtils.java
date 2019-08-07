/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Some utility methods for maps.
 */
public class MapUtils {

	/**
	 * Reorders the entries of a LinkedHashMap (an ordered map) using given comparator.
	 * 
	 * @param map map to reorder
	 * @param order comparator defining new order
	 */
	public static final <K,V> void reorderLinkedHashMap(final LinkedHashMap<K, V> map, final Comparator<Map.Entry<K,V>> order) {
		final List<Map.Entry<K,V>> orderedList = new ArrayList<>(map.entrySet());
		orderedList.sort(order);
		
		map.clear();
		for(Map.Entry<K, V> entry : orderedList) {
			map.put(entry.getKey(), entry.getValue());
		}
	}
	
}
