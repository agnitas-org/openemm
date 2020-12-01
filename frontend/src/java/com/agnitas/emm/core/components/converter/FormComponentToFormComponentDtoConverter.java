/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.converter;

import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.components.dto.FormComponentDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FormComponentToFormComponentDtoConverter implements Converter<FormComponent, FormComponentDto> {

	@Override
	public FormComponentDto convert(FormComponent formComponent) {
		FormComponentDto dto = new FormComponentDto();
		dto.setId(formComponent.getId());
		dto.setName(formComponent.getName());
		dto.setDescription(formComponent.getDescription());
		dto.setCreationDate(formComponent.getCreationDate());
		dto.setWidth(formComponent.getWidth());
		dto.setHeight(formComponent.getHeight());
		dto.setDataSize(formComponent.getDataSize());
		dto.setMimeType(formComponent.getMimeType());
		return dto;
	}
}
