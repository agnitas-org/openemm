/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.backend;

import	java.util.ArrayList;
import	java.util.List;
import	java.util.Set;

import	org.agnitas.util.Log;

/** One record for the URL collection
 */
public class URL {
	/** the unique ID */
	private long		 id;
	/** the URL itself */
	private String		url;
	/** in which part we should use the URL */
	private long		 usage;
	/** internal flag */
	private boolean		adminLink;
	/** original URL */
	private String		originalURL;
	/** if the URL should be enriched by static values */
	private boolean		staticValue;
	/** List of columns referenced by embedded hash tags */
	private List <String>	columns;

	/** Constructor
	 * @param nId    the unique ID
	 * @param nUrl   the URL
	 * @param nUsage the usage
	 */
	public URL (long nId, String nUrl, long nUsage) {
		id = nId;
		url = nUrl;
		usage = nUsage;
		adminLink = false;
		originalURL = null;
		staticValue = false;
		columns = null;
	}
	
	public long getId () {
		return id;
	}
	
	public String getUrl () {
		return url;
	}
	
	public long getUsage () {
		return usage;
	}

	public boolean getAdminLink () {
		return adminLink;
	}
	
	public void setAdminLink (boolean nAdminLink) {
		adminLink = nAdminLink;
	}

	public String getOriginalURL () {
		return originalURL;
	}
	
	public void setOriginalURL (String nOriginalURL) {
		originalURL = nOriginalURL != null && nOriginalURL.length () > 0 ? nOriginalURL : null;
	}

	public boolean getStaticValue () {
		return staticValue;
	}
	
	public void setStaticValue (boolean nStaticValue) {
		staticValue = nStaticValue;
	}
	
	public void addColumn (Column c) {
		if (columns == null) {
			columns = new ArrayList <> ();
		}
		columns.add (c.getQname ());
	}

	public void requestFields (Data data, Set <String> predef) {
		if (columns != null) {
			String	list = columns
				.stream ()
				.peek ((column) -> predef.add (column))
				.reduce ((s, e) -> s + ", " + e)
				.orElse (null);
			if (list != null) {
				data.logging (Log.DEBUG, "data", "Found columns " + list + " from hash tag in url " + url);
			}
		}
	}
	
	public String getStaticValueColumns () {
		if (columns != null) {
			return columns.stream ().reduce ((s, e) -> s + "," + e).orElse (null);
		}
		return null;
	}
}
