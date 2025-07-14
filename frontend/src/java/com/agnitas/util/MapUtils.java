/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

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
	public static <K,V> void reorderLinkedHashMap(final LinkedHashMap<K, V> map, final Comparator<Map.Entry<K,V>> order) {
		final List<Map.Entry<K,V>> orderedList = new ArrayList<>(map.entrySet());
		orderedList.sort(order);
		
		map.clear();
		for(Map.Entry<K, V> entry : orderedList) {
			map.put(entry.getKey(), entry.getValue());
		}
	}

	public static <K, V extends Comparable<V>> LinkedHashMap<K, V> sortByValues(Map<K, V> map) {
		LinkedHashMap<K, V> returnMap = new LinkedHashMap<>();
		List<V> values = new LinkedList<>(map.values());
		Collections.sort(values);
		for (V value : values) {
			for (Map.Entry<K, V> entry : map.entrySet()) {
				if (value == entry.getValue()) {
					returnMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return returnMap;
	}

	/**
	 * This method sums up all the String keys inside a map which refer to the same value.
	 * The keys are sorted, concatenated and used as new keys in the resulting map.
	 */
	public static <V> Map<String, V> joinStringKeysByValue(Map<String, V> map, String separator) {
		Map<V, List<String>> groupedMap = groupKeysByValue(map);
		Map<String, V> returnMap = new HashMap<>();
		for (Map.Entry<V, List<String>> entry : groupedMap.entrySet()) {
			// Sort Listentries caseinsensitive
			Collections.sort(entry.getValue(), String.CASE_INSENSITIVE_ORDER);
			returnMap.put(StringUtils.join(entry.getValue(), separator), entry.getKey());
		}
		return returnMap;
	}

	/**
	 * This method sums up all the keys inside a map which refer to the same value.
	 * The keys are stored in a List for each found value.
	 */
	public static <K, V> Map<V, List<K>> groupKeysByValue(Map<K, V> map) {
		Map<V, List<K>> returnMap = new HashMap<>();
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if (!returnMap.containsKey(entry.getValue())) {
				returnMap.put(entry.getValue(), new ArrayList<>());
			}

			returnMap.get(entry.getValue()).add(entry.getKey());
		}
		return returnMap;
	}
	
}
