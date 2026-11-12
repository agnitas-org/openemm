/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.recipient.mapper;

import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.recipient.dto.RecipientSearchParamsDto;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.recipient.dto.ListRecipientsRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RecipientsMcpMapper extends McpMapper {

    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "altgId", ignore = true)
    @Mapping(target = "targetGroupId", source = "targetId")
    @Mapping(target = "mailingListId", source = "mailinglistId")
    RecipientSearchParamsDto toDto(ListRecipientsRequest request);

    @Mapping(target = "sort", source = "sortBy")
    PaginatedFilter<String> toPagination(McpPageable pageable);

    default int toUserStatusCode(UserStatus status) {
        return status == null ? 0 : status.getStatusCode();
    }

    default String toUserStatusCode(BindingEntry.UserType type) {
        return type == null ? null : type.getTypeCode();
    }
}
