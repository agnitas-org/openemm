/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.dto;

import java.util.List;

import com.agnitas.beans.LinkProperty;

public class BaseTrackableLinkDto {

	private int id;

	private String url;

	private String shortname;

	private int trackable;

	private List<LinkProperty> properties;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public List<LinkProperty> getProperties() {
		return properties;
	}

	public void setProperties(List<LinkProperty> properties) {
		this.properties = properties;
	}

	public void setTrackable(int trackable) {
		this.trackable = trackable;
	}

	public int getTrackable() {
		return trackable;
	}
}
