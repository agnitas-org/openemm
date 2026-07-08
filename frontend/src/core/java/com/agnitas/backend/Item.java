/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.backend.exceptions.ItemException;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParser;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParserException;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.util.Tuple;

public class Item {
	private static final String	GLOBAL_NAME = "*";
	private Data			data;
	private Reference		ref;
	private String			table;
	private String			keycolumn;
	private String			keysource;
	private String			keyvalue;
	private String			column;
	private Map <String, Column>	values;
	
	public Item (Data nData, Map <String, String> parameter, boolean hashtag) throws ItemException {
		data = nData;
		ref = null;
		table = parameter.get ("table");
		keycolumn = parameter.get ("keycolumn");
		keysource = parameter.get (hashtag ? "item" : "keysource");
		keyvalue = parameter.get ("keyvalue");
		column = parameter.get ("column");
		if ((table == null) || (keycolumn == null) || (keyvalue == null)) {
			Map <String, String>	item = data.mailing.item ();
			
			if (item != null) {
				if (table == null) {
					table = item.get ("@table");
				}
				if (keycolumn == null) {
					keycolumn = item.get ("@column");
				}
				if ((keyvalue == null) && (keysource != null)) {
					keyvalue = item.get (keysource);
				}
			}
		}
		if (table == null) {
			throw new ItemException ("missing table");
		}
		if (keycolumn == null) {
			throw new ItemException ("missing key column");
		}
		if (keyvalue == null) {
			throw new ItemException ("missing key value");
		}
		if (column == null) {
			throw new ItemException ("missing value column");
		}
		if (data.references != null) {
			ref = data.references.get(table);
		}
		if (ref == null) {
			throw new ItemException ("unknown table \"" + table + "\" selected");
		}

		Map <String, String>	keyvalues = new HashMap <> ();

		if (keyvalue.startsWith ("@")) {
			resolveValues (keyvalue, keyvalues);
		} else {
			keyvalues.put (GLOBAL_NAME, keyvalue);
		}
		values = new HashMap <> ();
		for (Map.Entry <String, String> kv : keyvalues.entrySet ()) {
			String			name = kv.getKey ();
			String			selectvalue = kv.getValue ();
	                Map <String, Column>	row = ref.get (keycolumn, selectvalue);

			if (row == null) {
				throw new ItemException (table + "." + keycolumn + "=" + selectvalue + ": not found");
			}
			
			Column			value = row.get (column);
			
			if (value == null) {
				throw new ItemException (table + "." + column + ": selected column not part of table");
			}
			values.put (name, value);
		}
	}
	public Item (Data nData, Map <String, String> parameter) throws ItemException {
		this (nData, parameter, false);
	}
	public Item (Data nData, String hashtagParameter) throws KeyValueListParserException, ItemException {
		this (nData, KeyValueListParser.parseKeyValueList (hashtagParameter), true);
	}
	
	public String table () {
		return table;
	}
	public String keycolumn () {
		return keycolumn;
	}
	public String keyvalue () {
		return keyvalue;
	}
	public String column () {
		return column;
	}
	public Column value (String name) {
		Column	value = null;
		
		if (name != null) {
			value = values.get (name);
		}
		if ((value == null) && (! GLOBAL_NAME.equals (name))) {
			value = values.get (GLOBAL_NAME);
		}
		return value;
	}
	public List <String> valuesForNames () {
		return values
			.keySet ()
			.stream ()
			.filter (k -> ! GLOBAL_NAME.equals (k))
			.toList();
	}
	
	public static Map <String, GridCustomPlaceholderType> PLACEHOLDERS = Map.of (
		"label", GridCustomPlaceholderType.Label,
		"text", GridCustomPlaceholderType.Text,
		"image", GridCustomPlaceholderType.Image,
		"link", GridCustomPlaceholderType.Link,
		"image_link", GridCustomPlaceholderType.ImageLink,
		"image-link", GridCustomPlaceholderType.ImageLink,
		"color", GridCustomPlaceholderType.Color,
		"colour", GridCustomPlaceholderType.Color,
		"select", GridCustomPlaceholderType.Select,
		"check", GridCustomPlaceholderType.Check
	);

	public static Tuple<String, GridCustomPlaceholderType> parsePlaceholderKeyValue(String keyValue) {
		if (keyValue == null || keyValue.isBlank()) {
			return Tuple.of(null, null);
		}

		String[] parts = keyValue.split(":", 2);

		if (parts.length != 2) {
			return Tuple.of(null, null);
		}

		String placeholderName;
		GridCustomPlaceholderType placeholderType;
		String[] subParts = parts[1].split(":", 2);

		if ((subParts.length == 2) && PLACEHOLDERS.containsKey(subParts[0].toLowerCase())) {
			placeholderName = subParts[1];
			placeholderType = PLACEHOLDERS.get(subParts[0].toLowerCase());
		} else {
			placeholderName = parts[1];
			placeholderType = GridCustomPlaceholderType.Label;
		}

		return Tuple.of(placeholderName, placeholderType);
	}

	private void resolveValues (String source, Map <String, String> keyvalues) {
		String[]	parts = source.split (":", 2);
		
		if (parts.length == 2) {
			switch (parts[0]) {
			case "@ph":
				String		name;
				int		type;
				String[]	subparts = parts[1].split (":", 2);
				
				if ((subparts.length == 2) && PLACEHOLDERS.containsKey (subparts[0].toLowerCase ())) {
					name = subparts[1];
					type = PLACEHOLDERS.get (subparts[0].toLowerCase ()).getId();
				} else {
					name = parts[1];
					type = 0;
				}
				String			defaultKey = GLOBAL_NAME;
				Map <Long, String>	contentByName = data.gridContent (name, type);

				if (contentByName != null) {
					for (Map.Entry <Long, String> kv : contentByName.entrySet ()) {
						String		dname = data.gridDivIdToDynName (kv.getKey ());
						String		value = kv.getValue ();

						keyvalues.put (dname, value);
						if (defaultKey != null) {
							keyvalues.put (defaultKey, value);
							defaultKey = null;
						}
					}
				}
			}
		}
	}
}
