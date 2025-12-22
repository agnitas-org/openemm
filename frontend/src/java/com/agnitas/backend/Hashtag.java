/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.backend.exceptions.ItemException;
import com.agnitas.emm.core.hashtag.keyvaluelist.KeyValueListParserException;
import com.agnitas.util.Log;

public class Hashtag {
	private String	hashtag;
	private enum Type {
		Column,
		Static,
		Unhandled
	}
	private Type	type;
	private	String	column;
	private String	value;
	private Hashtag (Data data, String nHashtag) throws ItemException {
		hashtag = nHashtag;
		type = Type.Unhandled;
		column = null;
		value = null;
		parse (data);
	}
	
	protected boolean isColumn () {
		return type == Type.Column;
	}
	
	protected boolean isStatic () {
		return type == Type.Static;
	}
	
	protected String column () {
		return column;
	}
	
	protected String value () {
		return value;
	}
	
	private void parse (Data data) throws ItemException {
		String[] elements = hashtag.split(":");

		if (elements.length > 0) {
			switch (elements[0].toUpperCase ()) {
				case "MAILING_ID":
				case "URL_ID":
				case "SENDDATE-UNENCODED":
				case "DATE":
				case "MAILING":
				case "MAILING-UNENCODED":
				case "AGNUID":
				case "PUBID":
					break;
				case "ITEM":
					if (elements.length > 1) {
						try {
							value = (new Item (data, hashtag.split (":", 2)[1])).value ().get ();
							type = Type.Static;
						} catch (KeyValueListParserException e) {
							data.logging (Log.ERROR, "hashtag", "Failed to parse \"" + hashtag + "\": " + e.toString ());
							throw new ItemException ("failed to parse \"" + hashtag + "\": " + e.toString ());
						}
					}
					break;
				default:
					type = Type.Column;
					column = elements[elements.length - 1].trim ().toLowerCase ();
					break;
			}
		}
	}
	
	public static List <Hashtag> parse (Data data, String url) throws ItemException {
		List <Hashtag>	rc = new ArrayList<>();

		if (url == null) {
			return rc;
		}

		int urlLength = url.length();

		for (int pos = 0; pos < urlLength; ) {
			int start = url.indexOf("##", pos);

			if (start != -1) {
				int end = url.indexOf("##", start + 2);

				if (end != -1) {
					String	pure = url.substring (start + 2, end);
					Hashtag	hashtag = data.findHashtag (pure);
					
					if (hashtag == null) {
						hashtag = new Hashtag (data, pure);
						data.storeHashtag (pure, hashtag);
					}
					rc.add (hashtag);
					pos = end + 2;
				} else {
					pos = urlLength;
				}
			} else {
				pos = urlLength;
			}
		}
		return rc;
	}
}
