/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.ecs.backend.service;

public class UrlMaker {
	private int mailingID;
	private int urlID;
	private String prefix;


	public UrlMaker(int mailingID) {
		this.mailingID = mailingID;
	}
	/** 
	 * Checks an URL if it should be used static
	 * @return if the url is static
	 */
	private final boolean isStaticUrl(final String url) {
	        return (! url.endsWith ("?")) && (! url.endsWith ("&"));
	}
	public final void setURLID(final long url) {
		this.urlID = (int)url;
	}
	public final void setPrefix(final String nPrefix) {
		this.prefix = nPrefix;
	}

	public final void setCustomerID(final long customerID) {
//		this.customerID = (int)customerID;
	}

	public final String makeUID() {
		return Integer.toString(urlID);
	}

	public final String makeURL(final String base, final long url) {
		setURLID(url);
		return String.format("%s?uid=%s", base, makeUID());
	}
}
