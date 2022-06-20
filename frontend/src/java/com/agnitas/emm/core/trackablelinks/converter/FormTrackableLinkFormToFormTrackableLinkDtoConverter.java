/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.converter;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.LinkProperty;
import com.agnitas.emm.core.trackablelinks.dto.ExtensionProperty;
import com.agnitas.emm.core.trackablelinks.dto.FormTrackableLinkDto;
import com.agnitas.emm.core.trackablelinks.form.FormTrackableLinkForm;
import com.agnitas.service.ExtendedConversionService;

@Component
public class FormTrackableLinkFormToFormTrackableLinkDtoConverter implements Converter<FormTrackableLinkForm, FormTrackableLinkDto> {

	private ExtendedConversionService conversionService;

	public FormTrackableLinkFormToFormTrackableLinkDtoConverter(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public FormTrackableLinkDto convert(FormTrackableLinkForm form) {
		FormTrackableLinkDto dto = new FormTrackableLinkDto();
		dto.setId(form.getId());
		dto.setUrl(form.getUrl());
		dto.setShortname(form.getName());
		dto.setTrackable(form.getTrackable());
		List<LinkProperty> extensions = conversionService.convert(form.getExtensions(), ExtensionProperty.class, LinkProperty.class);
		dto.setProperties(extensions);
		return dto;
	}
}
