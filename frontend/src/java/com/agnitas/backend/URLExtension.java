/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keep track of URL extension for links to be extended during
 * mailing generation, if the link should not be meassured
 */
public class URLExtension {
	public static class URLEntry {
		private String key;
		private String value;
		private List<String> columns;

		public URLEntry(Data data, String nKey, String nValue) {
			key = nKey;
			value = nValue;
			columns = StringOps.findColumnsInHashtags(value);
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public void collectColumns(Set<String> collection) {
			columns.stream().forEach((c) -> collection.add(c));
		}
	
		public String getStaticValueColumns (Data data) {
			return columns
				.stream ()
				.map ((f) -> data.columnByName (f))
				.filter ((c) -> c != null)
				.map ((c) -> c.getQname ())
				.reduce ((s, e) -> s + "," + e)
				.orElse (null);
		}
	}

	private Data data;
	private Map<Long, List<URLEntry>> entries;
	private Set<String> fields;

	public URLExtension(Data nData) {
		data = nData;
		entries = new HashMap<>();
		fields = new HashSet<>();
	}

	public int count() {
		return entries.size();
	}

	/**
	 * Add an extension key/value pair for an URL which
	 * is identified by its id.
	 *
	 * @param urlID the ID of the URL for which this extension is used
	 * @param key   the key of the key/value pair to be added ..
	 * @param value .. and the value
	 */
	public void add(long urlID, String key, String value) {
		List<URLEntry> e = entries.get(urlID);
		URLEntry entry = new URLEntry(data, key, value);

		if (e == null) {
			e = new ArrayList<>();
			entries.put(urlID, e);
		}
		e.add(entry);
		entry.collectColumns(fields);
	}

	/**
	 * Add all columns used by this extension to be included
	 * in main query of the customer profile.
	 *
	 * @param predef a set to add each found column to
	 */
	public void requestFields(Set<String> predef) {
		fields.stream()
				.filter(field -> data.columnByName(field) != null)
				.forEach(predef::add);
	}

	/**
	 * returns a list of ell IDs of URLs for which an
	 * extension is known
	 *
	 * @return the list of all known URLs as a set
	 */
	public Set<Long> getURLIDs() {
		return entries.keySet();
	}

	/**
	 * return a list of all key/value pairs
	 * for one url
	 *
	 * @param urlID the id of the URL to return the parameter for
	 * @return a list of the parameter
	 */
	public List<URLEntry> getParameter(long urlID) {
		return entries.get(urlID);
	}
}
