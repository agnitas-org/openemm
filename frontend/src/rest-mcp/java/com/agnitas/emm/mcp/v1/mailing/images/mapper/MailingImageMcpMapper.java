/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.images.mapper;

import com.agnitas.emm.core.components.dto.MailingImageDto;
import com.agnitas.emm.core.components.dto.UploadMailingImageDto;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.common.mapper.UrlContentMcpMapper;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageLightResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.UploadMailingImageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MailingImageMcpMapper extends McpMapper, UrlContentMcpMapper {

    @Mapping(target = "fileName", source = "dto.name")
    @Mapping(target = "link", source = "dto.link.fullUrl")
    @Mapping(target = "sizeBytes", source = "dto.size")
    ImageResponse toResponse(MailingImageDto dto, String downloadUrl);

    @Mapping(target = "fileName", source = "name")
    @Mapping(target = "link", source = "link.fullUrl")
    @Mapping(target = "sizeBytes", source = "size")
    ImageLightResponse toLightResponse(MailingImageDto dto);

    @Mapping(target = "link", source = "linkTarget")
    @Mapping(target = "mobileBase", source = "mobileVersionOf")
    @Mapping(target = "file", expression = "java(toMultipartFile(request.url()))")
    UploadMailingImageDto toDto(UploadMailingImageRequest request);

}
