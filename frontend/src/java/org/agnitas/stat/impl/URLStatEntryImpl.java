/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.stat.impl;

import org.agnitas.stat.URLStatEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class URLStatEntryImpl implements URLStatEntry {
	private static final transient Logger logger = LogManager.getLogger(URLStatEntryImpl.class);

	protected int urlID = 0;
	protected String url;
	protected String shortname;
	protected int clicks;
	protected int clicksNetto;

	@Override
	public int getUrlID() {
		return urlID;
	}

	@Override
	public String getShortname() {
		return shortname;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public int getClicks() {
		return clicks;
	}

	@Override
	public void setUrlID(int urlID) {
		this.urlID = urlID;
	}

	@Override
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	@Override
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	@Override
	public int getClicksNetto() {
		return clicksNetto;
	}

	@Override
	public void setClicksNetto(int clicksNetto) {
		this.clicksNetto = clicksNetto;
	}

	@Override
	public int compareTo(URLStatEntry otherURLStatEntry) {
		try {
			if (otherURLStatEntry == null) {
				return -1;
			} else if (clicksNetto < otherURLStatEntry.getClicksNetto()) {
				return -1;
			} else if (clicksNetto == otherURLStatEntry.getClicksNetto()) {
				return 0;
			} else if (clicksNetto > otherURLStatEntry.getClicksNetto()) {
				return 1;
			} else {
				return -1;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return -1;
		}
	}

	public boolean equals(URLStatEntry otherURLStatEntry) {
		return otherURLStatEntry != null && urlID == otherURLStatEntry.getUrlID();
	}
}
