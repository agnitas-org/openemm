/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.Map;

import com.agnitas.backend.exceptions.ItemException;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParser;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParserException;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;

public class Item {
	private Data	data;
	private String	table;
	private String	keycolumn;
	private String	keysource;
	private String	keyvalue;
	private String	column;
	private Column	value;
	
	public Item (Data nData, Map <String, String> parameter, boolean hashtag) throws ItemException {
		data = nData;
		table = parameter.get ("table");
		keycolumn = parameter.get ("keycolumn");
		keysource = parameter.get (hashtag ? "item" : "keysource");
		keyvalue = parameter.get ("keyvalue");
		column = parameter.get ("column");
		value = null;
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
		if ((keyvalue != null) && keyvalue.startsWith ("@")) {
			keyvalue = resolveValue (keyvalue);
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

		Reference ref = null;
		if (data.references != null) {
			ref = data.references.get(table);
		}

		if (ref == null) {
			throw new ItemException ("unknown table \"" + table + "\" selected");
		}
		
                Map <String, Column>	row = ref.get (keycolumn, keyvalue);

		if (row == null) {
			throw new ItemException (table + "." + keycolumn + "=" + keyvalue + ": not found");
		}
		value = row.get (column);
		if (value == null) {
			throw new ItemException (table + "." + column + ": selected column not part of table");
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
	public Column value () {
		return value;
	}

	public static Map <String, Integer>	phs = Map.of (
		"label", GridCustomPlaceholderType.Label.getId (),
		"text", GridCustomPlaceholderType.Text.getId (),
		"image", GridCustomPlaceholderType.Image.getId (),
		"link", GridCustomPlaceholderType.Link.getId (),
		"image_link", GridCustomPlaceholderType.ImageLink.getId (),
		"image-link", GridCustomPlaceholderType.ImageLink.getId (),
		"color", GridCustomPlaceholderType.Color.getId (),
		"colour", GridCustomPlaceholderType.Color.getId (),
		"select", GridCustomPlaceholderType.Select.getId (),
		"check", GridCustomPlaceholderType.Check.getId ()
	);
	private String resolveValue (String source) {
		String[]	parts = source.split (":", 2);
		
		if (parts.length == 2) {
			switch (parts[0]) {
			case "@ph":
				String		name;
				int		type;
				String[]	subparts = parts[1].split (":", 2);
				
				if ((subparts.length == 2) && phs.containsKey (subparts[0].toLowerCase ())) {
					name = subparts[1];
					type = phs.get (subparts[0].toLowerCase ());
				} else {
					name = parts[1];
					type = 0;
				}
				return data.gridContentByName (name, type);
			}
		}
		return source;
	}
}
