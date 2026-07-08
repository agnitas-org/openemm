/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.converter;

import com.agnitas.beans.MailingComponent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;

@Component
public class MailingComponentToMailingImageDtoConverter implements Converter<MailingComponent, MailingImageDto> {

    private final TrackableLinkService linkService;

    public MailingComponentToMailingImageDtoConverter(TrackableLinkService linkService) {
        this.linkService = linkService;
    }

    @Override
	public MailingImageDto convert(MailingComponent component) {
    	MailingImageDto dto = new MailingImageDto();
		dto.setId(component.getId());
		dto.setMobile(component.isMobileImage());
		dto.setName(component.getComponentName());
		dto.setDescription(component.getDescription());
		dto.setCreationDate(component.getTimestamp());
		dto.setMimeType(component.getMimeType());
        dto.setType(component.getType());
        dto.setPresent(component.getPresent());
		dto.setSize(component.getSize());
        dto.setLink(linkService.getTrackableLink(component.getCompanyID(), component.getUrlID()));
		return dto;
	}
}
