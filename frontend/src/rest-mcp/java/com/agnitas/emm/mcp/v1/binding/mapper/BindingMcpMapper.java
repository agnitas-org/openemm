/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding.mapper;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.binding.BulkDeleteBindingCommand;
import com.agnitas.emm.core.binding.BulkUpsertBindingCommand;
import com.agnitas.emm.core.binding.CreateBindingCommand;
import com.agnitas.emm.core.binding.GetBindingQuery;
import com.agnitas.emm.core.binding.ListBindingsQuery;
import com.agnitas.emm.core.binding.PatchBindingCommand;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.mcp.v1.binding.dto.BindingResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.CreateBindingRequest;
import com.agnitas.emm.mcp.v1.binding.dto.ListBindingsFilter;
import com.agnitas.emm.mcp.v1.binding.dto.PatchBindingRequest;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        imports = {MediaTypes.class, UserStatus.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface BindingMcpMapper extends McpMapper {

    @Mapping(source = "filter.recipientIds", target = "recipientIds")
    @Mapping(source = "filter.mailinglistIds", target = "mailinglistIds")
    @Mapping(source = "filter.mediatypes", target = "mediatypes")
    @Mapping(source = "filter.userStatuses", target = "userStatuses")
    @Mapping(source = "filter.date", target = "date")
    @Mapping(source = "pageable.page", target = "page")
    @Mapping(source = "pageable.size", target = "size")
    ListBindingsQuery toQuery(ListBindingsFilter filter, McpPageable pageable);

    CreateBindingCommand toCommand(CreateBindingRequest request);

    BulkDeleteBindingCommand toCommand(BulkDeleteBindingsRequest request, int companyId);

    BulkUpsertBindingCommand toCommand(BulkUpsertBindingsRequest request, String remark);

    @Mapping(source = "query", target = "getQuery")
    PatchBindingCommand toCommand(GetBindingQuery query, PatchBindingRequest request);

    @Mapping(source = "binding.customerID", target = "recipientId")
    @Mapping(source = "binding.mailinglistID", target = "mailinglistId")
    @Mapping(target = "mediatype", expression = "java(MediaTypes.getMediaTypeForCode(binding.getMediaType()))")
    @Mapping(target = "userStatus", expression = "java(UserStatus.getByCode(binding.getUserStatus()))")
    @Mapping(target = "userType", expression = "java(BindingEntry.UserType.getUserTypeByString(binding.getUserType()))")
    @Mapping(source = "binding.userRemark", target = "userRemark")
    @Mapping(source = "binding.referrer", target = "referrer")
    @Mapping(source = "binding.entryMailingID", target = "entryMailingId")
    @Mapping(source = "binding.exitMailingID", target = "exitMailingId")
    @Mapping(source = "binding.creationDate", target = "creationDate")
    @Mapping(source = "binding.changeDate", target = "changeDate")
    @Mapping(source = "actionExecutionInfo", target = "actionExecutionInfo")
    BindingResponse toResponse(BindingEntry binding, String actionExecutionInfo);

    default BindingResponse toResponse(BindingEntry binding) {
        return toResponse(binding, null);
    }

    default PageResponse<BindingResponse> toPageResponse(PaginatedList<BindingEntry> source) {
        return toPageResponse(source, this::toResponse);
    }

}
