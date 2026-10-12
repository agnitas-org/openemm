/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.content.mapper;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.impl.DynamicTagContentImpl;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailingcontent.dto.PatchMailingContentCommand;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.mailing.content.MailingContentResponse;
import com.agnitas.emm.mcp.v1.mailing.content.TargetContentResponse;
import com.agnitas.emm.mcp.v1.mailing.content.dto.PatchMailingContentRequest;
import com.agnitas.emm.mcp.v1.mailing.content.dto.UpdateTargetContentRequest;
import org.mapstruct.BeanMapping;
import org.openapitools.jackson.nullable.JsonNullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MailingContentMcpMapper extends McpMapper {

    @Mapping(source = "dynName", target = "name")
    @Mapping(source = "dynContent", target = "content")
    MailingContentResponse toResponse(DynamicTag source);

    List<MailingContentResponse> toResponses(List<DynamicTag> source);

    @Mapping(source = "targetID", target = "targetId")
    @Mapping(source = "dynOrder", target = "order")
    @Mapping(source = "dynContent", target = "text")
    TargetContentResponse toTargetResponse(DynamicTagContent source);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "order", target = "dynOrder")
    @Mapping(source = "targetId", target = "targetID")
    @Mapping(source = "text", target = "dynContent")
    DynamicTagContentImpl toDynContent(UpdateTargetContentRequest source);

    default List<TargetContentResponse> toTargetContentList(Map<Integer, DynamicTagContent> dynContent) {
        if (dynContent == null) {
            return List.of();
        }

        return dynContent.values()
                .stream()
                .map(this::toTargetResponse)
                .toList();
    }

    default List<DynamicTagContent> toDynContentList(List<UpdateTargetContentRequest> source) {
        return source.stream()
                .<DynamicTagContent>map(this::toDynContent)
                .toList();
    }

    default PatchField<List<DynamicTagContent>> mapContent(JsonNullable<List<UpdateTargetContentRequest>> source) {
        if (source == null || source.isUndefined()) {
            return PatchField.absent();
        }
        List<UpdateTargetContentRequest> value = source.orElse(null);
        return PatchField.of(value != null ? toDynContentList(value) : null);
    }

    @Mapping(source = "request.disableLinkExtension", target = "disableLinkExtension")
    @Mapping(source = "request.content", target = "content")
    PatchMailingContentCommand toPatchCommand(PatchMailingContentRequest request);
}
