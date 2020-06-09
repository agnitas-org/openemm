/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

public class MapUtils {
	public static <K extends Comparable<K>, V> LinkedHashMap<K, V> sort(Map<K, V> map) {
		LinkedHashMap<K, V> returnMap = new LinkedHashMap<>();
		List<K> keys = new LinkedList<>(map.keySet());
		Collections.sort(keys);
		for (K key : keys) {
			returnMap.put(key, map.get(key));
		}
		return returnMap;
	}

	public static <K, V> LinkedHashMap<K, V> sort(Map<K, V> map, Comparator<K> comparator) {
		LinkedHashMap<K, V> returnMap = new LinkedHashMap<>();
		List<K> keys = new LinkedList<>(map.keySet());
		Collections.sort(keys, comparator);
		for (K key : keys) {
			returnMap.put(key, map.get(key));
		}
		return returnMap;
	}
	
	public static <K, V extends Comparable<V>> LinkedHashMap<K, V> sortByValues(Map<K, V> map) {
		LinkedHashMap<K, V> returnMap = new LinkedHashMap<>();
		List<V> values = new LinkedList<>(map.values());
		Collections.sort(values);
		for (V value : values) {
			for (Entry<K, V> entry : map.entrySet()) {
				if (value == entry.getValue()) {
					returnMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return returnMap;
	}
	
	public static <K, V> LinkedHashMap<K, V> sortEntries(Map<K, V> map, Comparator<Entry<K, V>> comparator) {
		LinkedHashMap<K, V> returnMap = new LinkedHashMap<>();
		List<Entry<K, V>> entries = new LinkedList<>(map.entrySet());
		Collections.sort(entries, comparator);
		for (Entry<K, V> entry : entries) {
			returnMap.put(entry.getKey(), entry.getValue());
		}
		return returnMap;
	}

	public static <K extends Comparable<K>, V, L extends LinkedHashMap<K, V>> L sort(L map) {
         @SuppressWarnings("unchecked")
         L returnMap = (L) map.clone();
         returnMap.clear();
         List<K> keys = new LinkedList<>(map.keySet());
         Collections.sort(keys);
         for (K key : keys) {
             returnMap.put(key, map.get(key));
         }
         return returnMap;
     }

     public static <K, V, L extends LinkedHashMap<K, V>> L sort(L map, Comparator<K> comparator) {
         @SuppressWarnings("unchecked")
         L returnMap = (L) map.clone();
         returnMap.clear();
         List<K> keys = new LinkedList<>(map.keySet());
         Collections.sort(keys, comparator);
         for (K key : keys) {
             returnMap.put(key, map.get(key));
         }
         return returnMap;
     }

     public static <K, V, L extends LinkedHashMap<K, V>> L sortEntries(L map, Comparator<Entry<K, V>> comparator) {
         @SuppressWarnings("unchecked")
         L returnMap = (L) map.clone();
         returnMap.clear();
         List<Entry<K, V>> entries = new LinkedList<>(map.entrySet());
         Collections.sort(entries, comparator);
         for (Entry<K, V> entry : entries) {
             returnMap.put(entry.getKey(), entry.getValue());
         }
         return returnMap;
     }

     public static <K, V, L extends HashMap<K, V>> L filterEntries(L map, Predicate<Entry<K, V>> predicate) {
         @SuppressWarnings("unchecked")
         L returnMap = (L) map.clone();
         returnMap.clear();
         for (Entry<K, V> entry : map.entrySet()) {
        	 if (predicate.evaluate(entry))
        		 returnMap.put(entry.getKey(), entry.getValue());
         }
         return returnMap;
     }
     
     /**
      * This method sums up all the keys inside a map which refer to the same value.
      * The keys are stored in a List for each found value.
      * 
      * @param map
      * @return
      */
     public static <K, V> Map<V, List<K>> groupKeysByValue(Map<K, V> map) {
    	 Map<V, List<K>> returnMap = new HashMap<>();
    	 for (Entry<K, V> entry : map.entrySet()) {
    		 if (!returnMap.containsKey(entry.getValue())) {
    			 returnMap.put(entry.getValue(), new ArrayList<>());
    		 }
    		 
    		 returnMap.get(entry.getValue()).add(entry.getKey());
    	 }
    	 return returnMap;
     }
     
     /**
      * This method sums up all the String keys inside a map which refer to the same value.
      * The keys are sorted, concatenated and used as new keys in the resulting map.
      * 
      * @param map
      * @param separator
      * @return
      */
     public static <V> Map<String, V> joinStringKeysByValue(Map<String, V> map, String separator) {
    	 Map<V, List<String>> groupedMap = groupKeysByValue(map);
    	 Map<String, V> returnMap = new HashMap<>();
    	 for (Entry<V, List<String>> entry : groupedMap.entrySet()) {
    		 // Sort Listentries caseinsensitive
    		 Collections.sort(entry.getValue(), String.CASE_INSENSITIVE_ORDER);
    		 returnMap.put(StringUtils.join(entry.getValue(), separator), entry.getKey());
    	 }
    	 return returnMap;
     }
}
