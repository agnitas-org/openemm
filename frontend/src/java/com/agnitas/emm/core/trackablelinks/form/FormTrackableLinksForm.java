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
import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import org.springframework.web.util.UriComponentsBuilder;

public class FormTrackableLinksForm {

	private List<FormTrackableLinkDto> links = new ArrayList<>();

	private List<ExtensionProperty> commonExtensions = new ArrayList<>();

	private int trackable;

	public List<FormTrackableLinkDto> getLinks() {
		return links;
	}

	public void setLinks(List<FormTrackableLinkDto> links) {
		this.links = links;
	}

	public List<ExtensionProperty> getCommonExtensions() {
		return commonExtensions;
	}

	public void setCommonExtensions(List<ExtensionProperty> commonExtensions) {
		this.commonExtensions = commonExtensions;
	}

	public String getCommonExtensionsString() {
		UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
		for (ExtensionProperty commonExtension : commonExtensions) {
			builder.queryParam(commonExtension.getName(), commonExtension.getValue());
		}

		return builder.build().getQuery();
	}

	public int isTrackable() {
		return trackable;
	}

	public void setTrackable(int trackable) {
		this.trackable = trackable;
	}
}
