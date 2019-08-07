/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.scripthelper.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This bean contains fields and methods for manipulating Parameters from 
 * ScriptHelper Methods.
 */
public class ScriptHelperParameter {
	private HashMap<String, HashMap<String, String>> itemMap;
	
	public ScriptHelperParameter(String prefix, HashMap<String, String> requestParameter) {
		itemMap = new HashMap<>();
		findItems(requestParameter, prefix);
	}
	
	public HashMap<String, String> getBlockAtPosition(String index) {
		HashMap<String, String> returnMap = new HashMap<>();
		// loop over all items
		for (Entry<String, HashMap<String, String>> entry : itemMap.entrySet()) {
			Map<String, String> valueMap = entry.getValue();
			returnMap.put(entry.getKey(), valueMap.get(index));
		}
		return returnMap;
	}
	
	public List<String> getItemNames() {
		ArrayList<String> itemNamesList = new ArrayList<>();		
		itemNamesList.addAll(itemMap.keySet());
		return itemNamesList;
	}
	
	public String getItemValue(String itemName, int index) {
		String returnValue = null;
		HashMap<String, String> tmpMap = itemMap.get(itemName);
		if (tmpMap != null) {
			returnValue = tmpMap.get(Integer.toString(index-1));	// warning, correct index!			
		}
		return returnValue;
	}
	
	public String getItemValue(String itemName, String stringIndex) {
		int index = -1;
		try {
			index = Integer.parseInt(stringIndex);
		} catch (NumberFormatException e) {
			return null;
		}
		return getItemValue(itemName, index);
	}
	
	/**
	 * returns the amount of Blocks (eg. if the highest index = 2, the block count = 2)
	 * @return
	 */
	public int getBlockCount() {		
		Iterator<HashMap<String, String>> it = itemMap.values().iterator();
		HashMap<String, String> tmpMap = it.next();
		return tmpMap.keySet().size();
	}
	
	private void findItems(HashMap<String, String> paramMap, String prefix) {
		for (Entry<String, String> entry : paramMap.entrySet()) {
			// if the result is 3, we have a valid item (!!! NO TRIM IS DONE SO FAR!!!).
			String[] item = checkIfItemMatch(entry.getKey(), prefix);			
			if (item != null && item.length == 3) {
				addItemToMap(itemMap, item, entry.getValue());
			}		
		}
	}
	
	/**
	 * This method finds the appropriate list for the given key and adds the value to the list. 
	 * @param itemMap
	 * @param value
	 */
	private void addItemToMap(HashMap<String, HashMap<String, String>> itemMap,String[] item, String value) {
		// find the appropriate list. Generate a new one, if not exists.
		String key = item[1].trim();
		HashMap<String, String> valueMap = itemMap.get(key);
		if (valueMap == null) {
			valueMap = new HashMap<>();
			itemMap.put(key, valueMap);	// add list to hashmap.
		}
		// find position
		int index = Integer.parseInt(item[2].trim());
		valueMap.put((Integer.toString(index-1)), value);		
	}
	
	/**
	 * This method checks, if the given String matches our Item-Rules:
	 * Every item must have the following form: "<prefix>_<itemname>_<index>=<value>"
	 * This method checks the key-part (<prefix>_<itemname>_<index>)
	 * If the item conforms our rules.
	 * @param prefix
	 * @return
	 */
	private String[] checkIfItemMatch(String item, String prefix) {
		if (item == null || item.length() == 0) {
			return null;
		}		
		String prefixToUpper = prefix.toUpperCase();
		String[] splitted = item.split("_");
		if (splitted.length != 3) {
			return null;
		}
		if (splitted[0].trim().toUpperCase().equals(prefixToUpper)) {
			try {
				String tmpString = splitted[2].trim();
				int result = Integer.parseInt(tmpString);								
				if (result < 0) {
					return null;
				}
				return splitted;	// return the item name.
			} catch (NumberFormatException e) {
				return null;
			} 
		}
		return null;
	}	
}
