/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.core.components.form.UploadMailingAttachmentForm;

@Component
public class UploadMailingAttachmentFormToUploadMailingAttachmentDtoConverter implements Converter<UploadMailingAttachmentForm, UploadMailingAttachmentDto> {

    @Override
    public UploadMailingAttachmentDto convert(UploadMailingAttachmentForm source) {
        UploadMailingAttachmentDto dto = new UploadMailingAttachmentDto();

        dto.setName(source.getAttachmentName());
        dto.setAttachmentFile(source.getAttachment());
        dto.setBackgroundFile(source.getBackgroundAttachment());
        dto.setType(source.getType());
        dto.setTargetId(source.getTargetId());
        dto.setUseUpload(source.isUsePdfUpload());
        dto.setUploadId(source.getPdfUploadId());

        return dto;
    }
}
