/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.content;

import java.util.List;
import java.util.Map;

import com.agnitas.beans.DynamicTag;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.impl.DynamicTagContentImpl;
import com.agnitas.beans.impl.DynamicTagImpl;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailingcontent.dto.PatchMailingContentCommand;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface MailingContentRestMapper extends RestMapper {

    @Mapping(source = "dynName", target = "name")
    @Mapping(source = "dynContent", target = "content")
    MailingContentResponse toMailingContentResponse(DynamicTag dynTag);

    @Mapping(source = "targetID", target = "targetId")
    @Mapping(source = "dynOrder", target = "order")
    @Mapping(source = "dynContent", target = "text")
    TargetContentResponse toTargetContentResponse(DynamicTagContent dynTagContent);

    default List<TargetContentResponse> toTargetContentResponse(Map<Integer, DynamicTagContent> dynContent) {
        return dynContent == null
                ? List.of()
                : dynContent.values().stream()
                .map(this::toTargetContentResponse)
                .toList();
    }

    List<MailingContentResponse> toMailingContentResponseList(List<DynamicTag> dynamicTags);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    DynamicTagImpl toDynTag(CreateMailingContentRequest request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "order", target = "dynOrder")
    @Mapping(source = "targetId", target = "targetID")
    @Mapping(source = "text", target = "dynContent")
    DynamicTagContentImpl toDynContent(TargetContentUpsertRequest request);

    PatchMailingContentCommand toPatchCommand(PatchMailingContentRequest request);

    List<DynamicTagContent> toDynContentList(List<TargetContentUpsertRequest> list);

    default PatchField<List<DynamicTagContent>> mapContentField(JsonNullable<List<TargetContentUpsertRequest>> source) {
        if (source == null || !source.isPresent()) {
            return PatchField.absent();
        }
        List<TargetContentUpsertRequest> sourceList = source.get();
        if (sourceList == null) {
            return PatchField.of(null);
        }
        return PatchField.of(toDynContentList(sourceList));
    }
}
