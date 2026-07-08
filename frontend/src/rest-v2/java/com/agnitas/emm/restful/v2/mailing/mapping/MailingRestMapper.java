/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.mailing.mapping;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.LinkProperty;
import com.agnitas.beans.MailingsListProperties;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.impl.TrackableLinkImpl;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mailing.bean.LightweightMailing;
import com.agnitas.emm.core.mailing.bean.MailingParameter;
import com.agnitas.emm.restful.v2.mailing.dto.LinkPropertyDto;
import com.agnitas.emm.restful.v2.mailing.dto.MailingLightResponse;
import com.agnitas.emm.restful.v2.mailing.dto.MailingParameterDto;
import com.agnitas.emm.restful.v2.mailing.dto.MailingsPage;
import com.agnitas.emm.restful.v2.mailing.dto.TrackableLinkDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MailingRestMapper {

    @Mapping(source = "mailingID", target = "id")
    @Mapping(source = "shortname", target = "name")
    @Mapping(source = "mailingType", target = "type")
    @Mapping(source = "mailingDescription", target = "description")
    MailingLightResponse toLightResponse(LightweightMailing mailing);

    @Mapping(target = "types", source = "types")
    @Mapping(target = "rownums", source = "pageSize")
    @Mapping(target = "searchNameStr", source = "name")
    MailingsListProperties toListProperties(MailingsPage form);

    MailingParameter toParameter(MailingParameterDto dto);
    List<MailingParameter> toParameters(List<MailingParameterDto> dto);

    @Mapping(target = "deepTracking", source = "deep_tracking")
    @Mapping(target = "fullUrl", source = "url")
    TrackableLinkImpl toTrackableLinks(TrackableLinkDto dto);
    List<TrackableLinkImpl> toTrackableLinks(List<TrackableLinkDto> dto);

    @Mapping(target = "propertyName", source = "name")
    @Mapping(target = "propertyValue", source = "value")
    @Mapping(target = "propertyType", source = "type")
    LinkProperty toLinkProperty(LinkPropertyDto dto);

    default String typesToCsv(Set<MailingType> types) {
        if (isEmpty(types)) {
            return null;
        }
        return types.stream()
                .map(t -> String.valueOf(t.getCode()))
                .collect(Collectors.joining(","));
    }

    default MailingLightResponse toMailingLightResponse(Map<String, Object> source) {
        return new MailingLightResponse(
                (Integer) source.get("mailingid"),
                MailingType.getByCode((Integer) source.get("mailing_type")).name(),
                (String) source.get("shortname"),
                (String) source.get("description")
        );
    }

    PaginatedList<MailingLightResponse> toMailingLightResponsePage(PaginatedList<Map<String, Object>> source);
}
