/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.converter;

import java.awt.Dimension;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.components.dto.FormUploadComponentDto;
import com.agnitas.service.MimeTypeService;
import com.agnitas.util.ImageUtils;

@Component
public class FormUploadComponentDtoToFormComponentConverter implements Converter<FormUploadComponentDto, FormComponent> {

	private final MimeTypeService mimeTypeService;

	public FormUploadComponentDtoToFormComponentConverter(MimeTypeService mimeTypeService) {
		this.mimeTypeService = mimeTypeService;
	}

	@Override
	public FormComponent convert(FormUploadComponentDto dto) {
		FormComponent component = new FormComponent();

		component.setName(dto.getFileName());
		component.setDescription(dto.getDescription());
		component.setOverwriteExisting(dto.isOverwriteExisting());
		component.setType(FormComponent.FormComponentType.IMAGE);
		component.setMimeType(mimeTypeService.getMimetypeForFile(dto.getFileName()));

		byte[] data = dto.getData();
		component.setData(data);

		Dimension dimension = ImageUtils.getImageDimension(data);
		component.setWidth((int) dimension.getWidth());
		component.setHeight((int) dimension.getHeight());
		return component;
	}
}
