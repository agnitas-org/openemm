/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
public class FormTrackableLinkDtoToFormTrackableLinkFormConverter implements Converter<FormTrackableLinkDto, FormTrackableLinkForm> {

	private ExtendedConversionService conversionService;

	public FormTrackableLinkDtoToFormTrackableLinkFormConverter(ExtendedConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public FormTrackableLinkForm convert(FormTrackableLinkDto dto) {
		FormTrackableLinkForm form = new FormTrackableLinkForm();
		form.setId(dto.getId());
		form.setUrl(dto.getUrl());
		form.setName(dto.getShortname());
		form.setTrackable(dto.getTrackable());
		List<ExtensionProperty> extensions = conversionService.convert(dto.getProperties(), LinkProperty.class, ExtensionProperty.class);
		form.setExtensions(extensions);
		return form;
	}
}
