/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.attachments;

import com.agnitas.emm.core.components.dto.MailingAttachmentDto;
import com.agnitas.emm.core.components.dto.UploadMailingAttachmentDto;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface MailingAttachmentRestMapper extends RestMapper {

    @Mapping(target = "originalSizeBytes", source = "originalSize")
    @Mapping(target = "emailSizeBytes", source = "emailSize")
    AttachmentResponse toResponse(MailingAttachmentDto dto);

    @Mapping(target = "backgroundFile", source = "pdfBackgroundFile")
    @Mapping(target = "useUpload", ignore = true)
    @Mapping(target = "uploadId", ignore = true)
    @Mapping(target = "attachmentFile", source = "attachment")
    @Mapping(target = "name", expression = "java(getName(request))")
    UploadMailingAttachmentDto toDto(UploadAttachmentRequest request);

    default String getName(UploadAttachmentRequest request) {
        return StringUtils.defaultIfBlank(request.name(), request.attachment().getOriginalFilename());
    }
}
