/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import com.agnitas.userform.trackablelinks.bean.TrackableUserFormLink;

@Component
public class TrackableUserFormLinkToFormTrackableLinkDtoConverter implements Converter<TrackableUserFormLink, FormTrackableLinkDto> {

	@Override
	public FormTrackableLinkDto convert(TrackableUserFormLink link) {
		FormTrackableLinkDto dto = new FormTrackableLinkDto();
		dto.setId(link.getId());
		dto.setShortname(link.getShortname());
		dto.setUrl(link.getFullUrl());
		dto.setTrackable(link.getUsage());
		dto.setProperties(link.getProperties());
		return dto;
	}
}
