/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.mapper;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.Mailing;
import com.agnitas.beans.MailingComponent;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.Mediatype;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailing.MailingSortCriteria;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.emm.core.mailing.settings.EmailMediaTypePatch;
import com.agnitas.emm.core.mailing.settings.MediaTypePatch;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.AttachmentResponse;
import com.agnitas.emm.mcp.v1.mailing.content.MailingContentResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingParameterRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.FollowupType;
import com.agnitas.emm.mcp.v1.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.MailFormat;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingCharset;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingFullResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingLightResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingMediaType;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.OnePixelPosition;
import com.agnitas.emm.mcp.v1.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.UpdateMailingParameterRequest;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageResponse;
import com.agnitas.emm.mcp.v1.mailing.links.MailingLinkResponse;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupResponse;
import com.agnitas.util.importvalues.Charset;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, imports = {NumberUtils.class})
public interface MailingMcpMapper extends McpMapper {

    // Used for CreateMailingRequest's plain (non-JsonNullable) fields, which are all mapped
    // into the patch command as "present" — but only when actually provided;
    default <T> PatchField<T> map(T value) {
        return value != null ? PatchField.of(value) : PatchField.absent();
    }

    @Mapping(target = "sendDate", source = "senddate")
    @Mapping(target = "name", source = "shortname")
    @Mapping(target = "type", source = "mailingType")
    MailingLightResponse toLightResponse(Mailing mailing);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "statuses", expression = "java(mapStatuses(query == null ? null : query.statuses()))")
    @Mapping(target = "types", expression = "java(mapTypes(query == null ? null : query.types()))")
    @Mapping(source = "pageable.size", target = "rownums")
    @Mapping(source = "query.name", target = "searchNameStr")
    @Mapping(source = "query.mailinglistIds", target = "mailingLists")
    @Mapping(target = "sortCriteria", expression = "java(toSortCriteria(pageable.sortBy()))")
    @Mapping(target = "direction", expression = "java(Boolean.FALSE.equals(pageable.ascending()) ? \"DESC\" : \"ASC\")")
    MailingsListProperties toListProperties(ListMailingsRequest query, McpPageable pageable);

    // Only whitelisted criteria may reach the DAO, which builds its ORDER BY clause from them
    @SuppressWarnings("unused") // used in toListProperties
    default MailingSortCriteria toSortCriteria(String sortBy) {
        return MailingSortCriteria.from(sortBy).orElse(MailingSortCriteria.MAILING_ID);
    }

    @Mapping(target = "statuses", ignore = true)
    ListMailingsRequest toListRequest(ListTemplatesRequest request);

    default MailingLightResponse toMailingLightResponse(Map<String, Object> source, boolean isTemplate) {
        return new MailingLightResponse(
                (Integer) source.get("mailingid"),
                MailingType.getByCode((Integer) source.get("mailing_type")).name(),
                (String) source.get("shortname"),
                (String) source.get("description"),
                Optional.ofNullable((Timestamp) source.get("senddate"))
                        .map(Timestamp::toInstant)
                        .orElse(null),
                isTemplate ? null : Optional.ofNullable((Number) source.get("recipientsCount"))
                        .map(Number::longValue)
                        .orElse(null)
        );
    }

    @SuppressWarnings("unused") // used in toListProperties
    default String mapTypes(Set<MailingType> types) {
        if (CollectionUtils.isEmpty(types)) {
            return null;
        }
        return types.stream()
                .map(t -> String.valueOf(t.getCode()))
                .collect(Collectors.joining(","));
    }

    @SuppressWarnings("unused") // used in toListProperties
    default List<String> mapStatuses(Set<MailingStatus> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return Collections.emptyList();
        }

        return statuses.stream()
                .map(MailingStatus::getDbKey)
                .toList();
    }

    // region Patch and create commands
    @Mapping(target = "archived", expression = "java(PatchField.absent())")
    @Mapping(target = "planDate", expression = "java(PatchField.absent())")
    @Mapping(target = "splitId", expression = "java(PatchField.absent())")
    @Mapping(target = "useDynamicTemplate", expression = "java(PatchField.absent())")
    @Mapping(target = "referenceContentSettings", expression = "java(PatchField.absent())")
    @Mapping(target = "frequencyCounterDisabled", expression = "java(PatchField.absent())")
    @Mapping(target = "activeMediaTypesOrder", expression = "java(map(request.mediaTypes(), this::toMediaTypes))")
    @Mapping(target = "mediaTypes", expression = "java(map(mediaTypes))")
    PatchMailingSettingsCommand toPatchCommand(PatchMailingRequest request, Map<MediaTypes, MediaTypePatch> mediaTypes);

    @Mapping(target = "archived", expression = "java(PatchField.absent())")
    @Mapping(target = "planDate", expression = "java(PatchField.absent())")
    @Mapping(target = "splitId", expression = "java(PatchField.absent())")
    @Mapping(target = "useDynamicTemplate", expression = "java(PatchField.absent())")
    @Mapping(target = "referenceContentSettings", expression = "java(PatchField.absent())")
    @Mapping(target = "frequencyCounterDisabled", expression = "java(PatchField.absent())")
    @Mapping(target = "activeMediaTypesOrder", expression = "java(map(toMediaTypes(request.mediaTypes())))")
    @Mapping(target = "mediaTypes", expression = "java(map(mediaTypes))")
    PatchMailingSettingsCommand toPatchCommand(CreateMailingRequest request, Map<MediaTypes, MediaTypePatch> mediaTypes);

    @Mapping(target = "withHtmlTemplate", ignore = true)
    @Mapping(target = "onePixel", expression = "java(map(request.measureOpeningRate(), this::toOnePixelValue))")
    @Mapping(target = "charset", expression = "java(map(request.charset(), this::toCoreCharset))")
    @Mapping(target = "format", expression = "java(map(request.format(), this::toCoreMailType))")
    @Mapping(target = "followupType", expression = "java(map(request.followupType(), this::toCoreFollowUpType))")
    EmailMediaTypePatch toEmailPatch(PatchMailingRequest request);

    @Mapping(target = "withHtmlTemplate", ignore = true)
    @Mapping(target = "onePixel", expression = "java(map(toOnePixelValue(request.measureOpeningRate())))")
    @Mapping(target = "charset", expression = "java(map(toCoreCharset(request.charset())))")
    @Mapping(target = "format", expression = "java(map(toCoreMailType(request.format())))")
    @Mapping(target = "followupType", expression = "java(map(toCoreFollowUpType(request.followupType())))")
    EmailMediaTypePatch toEmailPatch(CreateMailingRequest request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    MailingParameter toMailingParam(CreateMailingParameterRequest request);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    MailingParameter toMailingParam(UpdateMailingParameterRequest request);

    List<MailingParameter> toMailingParams(List<UpdateMailingParameterRequest> request);

    default PatchField<List<MailingParameter>> toMailingParams(JsonNullable<List<UpdateMailingParameterRequest>> source) {
        return map(source, this::toMailingParams);
    }

    default PatchField<List<MailingParameter>> map(List<CreateMailingParameterRequest> value) {
        return map(CollectionUtils.emptyIfNull(value).stream().map(this::toMailingParam).toList());
    }

    default List<MediaTypes> toMediaTypes(List<MailingMediaType> source) {
        return source.stream().map(this::toMediaType).toList();
    }

    default MediaTypes toMediaType(MailingMediaType mediaType) {
        return MediaTypes.valueOf(mediaType.name());
    }

    default Charset toCoreCharset(MailingCharset charset) {
        return charset == null ? null : charset.getCoreVal();
    }

    default MailType toCoreMailType(MailFormat format) {
        return format == null ? null : format.getCoreConst();
    }

    default FollowUpType toCoreFollowUpType(FollowupType type) {
        return type == null ? null : type.getCoreVal();
    }

    default String toOnePixelValue(OnePixelPosition position) {
        return position == null ? null : position.name().toLowerCase();
    }
    // endregion

    // region Response
    @Mapping(target = "smsTemplate", ignore = true) // overridden in extended scope
    @Mapping(target = "smsSenderAddress", ignore = true) // overridden in extended scope
    @Mapping(target = "rcsTemplate", ignore = true) // overridden in extended scope
    @Mapping(target = "name", source = "shortname")
    @Mapping(target = "mailinglistId", source = "mailinglistID")
    @Mapping(target = "contentType", source = "mailingContentType")
    @Mapping(target = "clickTriggerId", source = "clickActionID")
    @Mapping(target = "openTriggerId", source = "openActionID")
    @Mapping(target = "archiveId", source = "campaignID")
    @Mapping(target = "subject", source = "emailParam.subject")
    @Mapping(target = "preHeader", source = "emailParam.preHeader")
    @Mapping(target = "senderAddress", source = "emailParam.fromEmail")
    @Mapping(target = "senderName", source = "emailParam.fromFullname")
    @Mapping(target = "replyName", source = "emailParam.replyFullname")
    @Mapping(target = "replyAddress", source = "emailParam.replyEmail")
    @Mapping(target = "measureOpeningRate", source = "emailParam.onepixel")
    @Mapping(target = "envelopeEmail", source = "emailParam.envelopeEmail")
    @Mapping(target = "followupType", source = "emailParam.followUpMethod")
    @Mapping(target = "format", expression = "java(MailFormat.from(mailing.getEmailParam().getMailFormat()))")
    @Mapping(target = "followupForId", expression = "java(NumberUtils.toInt(mailing.getEmailParam().getFollowupFor(), 0))")
    @Mapping(target = "charset", source = "emailParam.charset")
    @Mapping(target = "mediaTypes", expression = "java(toMediaTypes(mailing))")
    @Mapping(target = "htmlTemplate", expression = "java(toHtmlTemplate(mailing))")
    @Mapping(target = "textTemplate", expression = "java(toTextTemplate(mailing))")
    @Mapping(target = "sendDate", source = "senddate")
    MailingResponse internalToResponse(Mailing mailing);

    default MailingResponse toResponse(Mailing mailing) {
        return internalToResponse(mailing);
    }

    default String toHtmlTemplate(Mailing mailing) {
        MailingComponent component = mailing.getHtmlTemplate();
        return component != null ? component.getEmmBlock() : mailing.getEmailParam().getHtmlTemplate();
    }

    default String toTextTemplate(Mailing mailing) {
        MailingComponent component = mailing.getTextTemplate();
        return component != null ? component.getEmmBlock() : mailing.getEmailParam().getTemplate();
    }

    default List<MailingMediaType> toMediaTypes(Mailing mailing) {
        return mailing.getMediatypes().values().stream()
                .filter(Objects::nonNull)
                .filter(Mediatype::isActive)
                .sorted(Comparator.comparingInt(Mediatype::getPriority))
                .map(Mediatype::getMediaType)
                .map(MediaTypes::name)
                .map(this::toMailingMediaType)
                .filter(Objects::nonNull)
                .toList();
    }

    private MailingMediaType toMailingMediaType(String name) {
        try {
            return MailingMediaType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    default FollowupType toFollowupType(String type) {
        return StringUtils.isEmpty(type) ? null : FollowupType.from(FollowUpType.fromKey(type));
    }

    default OnePixelPosition toOnePixelPosition(String value) {
        return value == null ? null : OnePixelPosition.valueOf(value.toUpperCase());
    }
    // endregion

    MailingFullResponse toFullResponse(
            MailingResponse response,
            List<TargetGroupResponse> targets,
            List<ImageResponse> images,
            List<AttachmentResponse> attachments,
            List<MailingContentResponse> contentBlocks,
            List<MailingLinkResponse> links
    );
}
