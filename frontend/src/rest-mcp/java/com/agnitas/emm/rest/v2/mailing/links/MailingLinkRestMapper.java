/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.links;

import java.util.List;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.TrackableLink;
import com.agnitas.emm.core.trackablelinks.dto.PatchLinkCommand;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import com.agnitas.util.LinkUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface MailingLinkRestMapper extends RestMapper {

    @Mapping(target = "extensions", source = "properties")
    @Mapping(target = "retargeting", source = "deepTracking")
    @Mapping(target = "tracking", source = "usage")
    @Mapping(target = "url", expression = "java(toFullUrl(link))")
    @Mapping(target = "staticLink", source = "staticValue")
    @Mapping(target = "triggerId", source = "actionID")
    @Mapping(target = "description", source = "shortname")
    @Mapping(target = "administrative", source = "adminLink")
    LinkResponse toResponse(TrackableLink link);

    default String toFullUrl(TrackableLink link) {
        return LinkUtils.getPlainFullUrlWithExtensions(link.getFullUrl(), link.getProperties());
    }

    default LinkTracking toLinkTracking(int code) {
        return LinkTracking.from(code);
    }

    default int map(LinkTracking tracking) {
        return tracking == null ? 0 : tracking.getCode();
    }

    default LinkRetargeting toLinkRetargeting(int code) {
        return LinkRetargeting.from(code);
    }

    default int map(LinkRetargeting retargeting) {
        return retargeting == null ? 0 : retargeting.getCode();
    }

    @Mapping(target = "value", source = "propertyValue")
    @Mapping(target = "name", source = "propertyName")
    LinkExtensionResponse map(LinkProperty linkProperty);

    @Mapping(target = "retargeting", expression = "java(map(request.retargeting(), this::map))")
    @Mapping(target = "tracking", expression = "java(map(request.tracking(), this::map))")
    @Mapping(target = "extensions", expression = "java(map(request.extensions(), this::mapExtensions))")
    PatchLinkCommand toCommand(PatchLinkRequest request);

    default LinkProperty toProperty(CreateLinkExtensionRequest extension) {
        return new LinkProperty(LinkProperty.PropertyType.LinkExtension,
                StringUtils.defaultString(extension.name()),
                StringUtils.defaultString(extension.value()));
    }

    List<LinkProperty> mapExtensions(List<CreateLinkExtensionRequest> linkProperty);
}
