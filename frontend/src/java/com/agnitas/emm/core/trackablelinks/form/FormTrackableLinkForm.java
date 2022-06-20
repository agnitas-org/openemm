/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.form;

import java.util.ArrayList;
import java.util.List;

import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;

public class FormTrackableLinkForm {

	private int id;

	private String url;

	private String name;

	private int trackable;

	private List<ExtensionProperty> extensions = new ArrayList<>();

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTrackable() {
		return trackable;
	}

	public void setTrackable(int trackable) {
		this.trackable = trackable;
	}

	public List<ExtensionProperty> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<ExtensionProperty> extensions) {
		this.extensions = extensions;
	}
}
