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
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.Mediatype;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.FollowUpType;
import com.agnitas.emm.common.MailingStatus;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailing.MailingSortCriteria;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.core.mailing.dto.PatchMailingSettingsCommand;
import com.agnitas.emm.core.mailing.settings.EmailMediatypePatch;
import com.agnitas.emm.core.mailing.settings.MediatypePatch;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingParameterRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.CreateMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.FollowupType;
import com.agnitas.emm.mcp.v1.mailing.dto.ListMailingsRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.ListTemplatesRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.MailFormat;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingCharset;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingLightResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingMediatype;
import com.agnitas.emm.mcp.v1.mailing.dto.MailingResponse;
import com.agnitas.emm.mcp.v1.mailing.dto.OnePixelPosition;
import com.agnitas.emm.mcp.v1.mailing.dto.PatchMailingRequest;
import com.agnitas.emm.mcp.v1.mailing.dto.UpdateMailingParameterRequest;
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
    @Mapping(target = "statuses", expression = "java(mapStatuses(query.statuses()))")
    @Mapping(target = "types", expression = "java(mapTypes(query.types()))")
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

    default PageResponse<MailingLightResponse> toPageResponse(PaginatedList<Map<String, Object>> source) {
        return toPageResponse(source, this::toMailingLightResponse);
    }

    default MailingLightResponse toMailingLightResponse(Map<String, Object> source) {
        return new MailingLightResponse(
                (Integer) source.get("mailingid"),
                MailingType.getByCode((Integer) source.get("mailing_type")).name(),
                (String) source.get("shortname"),
                (String) source.get("description"),
                Optional.ofNullable((Timestamp) source.get("senddate"))
                        .map(Timestamp::toInstant)
                        .orElse(null),
                Optional.ofNullable((Number) source.get("recipientsCount"))
                        .map(Number::longValue)
                        .orElse(0L)
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
    @Mapping(target = "activeMediatypesOrder", expression = "java(map(request.mediatypes(), this::toMediaTypes))")
    @Mapping(target = "mediatypes", expression = "java(map(mediatypes))")
    PatchMailingSettingsCommand toPatchCommand(PatchMailingRequest request, Map<MediaTypes, MediatypePatch> mediatypes);

    @Mapping(target = "archived", expression = "java(PatchField.absent())")
    @Mapping(target = "planDate", expression = "java(PatchField.absent())")
    @Mapping(target = "splitId", expression = "java(PatchField.absent())")
    @Mapping(target = "useDynamicTemplate", expression = "java(PatchField.absent())")
    @Mapping(target = "referenceContentSettings", expression = "java(PatchField.absent())")
    @Mapping(target = "frequencyCounterDisabled", expression = "java(PatchField.absent())")
    @Mapping(target = "activeMediatypesOrder", expression = "java(map(toMediaTypes(request.mediatypes())))")
    @Mapping(target = "mediatypes", expression = "java(map(mediatypes))")
    PatchMailingSettingsCommand toPatchCommand(CreateMailingRequest request, Map<MediaTypes, MediatypePatch> mediatypes);

    @Mapping(target = "withHtmlTemplate", ignore = true)
    @Mapping(target = "onePixel", expression = "java(map(request.measureOpeningRate(), this::toOnePixelValue))")
    @Mapping(target = "charset", expression = "java(map(request.charset(), this::toCoreCharset))")
    @Mapping(target = "format", expression = "java(map(request.format(), this::toCoreMailType))")
    @Mapping(target = "followupType", expression = "java(map(request.followupType(), this::toCoreFollowUpType))")
    EmailMediatypePatch toEmailPatch(PatchMailingRequest request);

    @Mapping(target = "withHtmlTemplate", ignore = true)
    @Mapping(target = "onePixel", expression = "java(map(toOnePixelValue(request.measureOpeningRate())))")
    @Mapping(target = "charset", expression = "java(map(toCoreCharset(request.charset())))")
    @Mapping(target = "format", expression = "java(map(toCoreMailType(request.format())))")
    @Mapping(target = "followupType", expression = "java(map(toCoreFollowUpType(request.followupType())))")
    EmailMediatypePatch toEmailPatch(CreateMailingRequest request);

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

    default List<MediaTypes> toMediaTypes(List<MailingMediatype> source) {
        return source.stream().map(this::toMediaType).toList();
    }

    default MediaTypes toMediaType(MailingMediatype mediatype) {
        return MediaTypes.valueOf(mediatype.name());
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
    @Mapping(target = "mediatypes", expression = "java(toMediatypes(mailing))")
    @Mapping(target = "htmlTemplate", source = "emailParam.htmlTemplate")
    @Mapping(target = "textTemplate", source = "emailParam.template")
    @Mapping(target = "sendDate", source = "senddate")
    MailingResponse internalToResponse(Mailing mailing);

    default MailingResponse toResponse(Mailing mailing) {
        return internalToResponse(mailing);
    }

    default List<MailingMediatype> toMediatypes(Mailing mailing) {
        return mailing.getMediatypes().values().stream()
                .filter(Objects::nonNull)
                .filter(Mediatype::isActive)
                .sorted(Comparator.comparingInt(Mediatype::getPriority))
                .map(Mediatype::getMediaType)
                .map(MediaTypes::name)
                .map(this::toMailingMediatype)
                .filter(Objects::nonNull)
                .toList();
    }

    private MailingMediatype toMailingMediatype(String name) {
        try {
            return MailingMediatype.valueOf(name);
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

}
