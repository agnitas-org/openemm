/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.recipient;

import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.recipient.dto.RecipientSearchParamsDto;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface RecipientsRestMapper extends RestMapper {

    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "altgId", ignore = true)
    @Mapping(target = "targetGroupId", source = "targetId")
    @Mapping(target = "mailingListId", source = "mailinglistId")
    RecipientSearchParamsDto toDto(ListRecipientsRequest request);

    @Mapping(target = "ascending", expression = "java(toDirection(pageable.getSort()))")
    @Mapping(target = "sort", expression = "java(toSortProperty(pageable.getSort()))")
    @Mapping(target = "size", source = "pageSize")
    @Mapping(target = "page", source = "pageNumber")
    PaginatedFilter<String> toPagination(Pageable pageable);

    default int toUserStatusCode(UserStatus status) {
        return status == null ? 0 : status.getStatusCode();
    }

    default String toUserStatusCode(BindingEntry.UserType type) {
        return type == null ? null : type.getTypeCode();
    }
}
