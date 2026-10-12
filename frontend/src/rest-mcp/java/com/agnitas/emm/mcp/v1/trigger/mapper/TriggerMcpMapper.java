/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger.mapper;

import java.sql.Timestamp;
import java.time.Instant;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.action.bean.EmmAction;
import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.emm.core.action.dto.CreateTriggerCommand;
import com.agnitas.emm.core.action.dto.PatchTriggerCommand;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.trigger.dto.CreateTriggerRequest;
import com.agnitas.emm.mcp.v1.trigger.dto.TriggerLightResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.TriggerResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.PatchTriggerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = { ActionOperationParametersParser.class, TriggerOperationsPatchFieldConverter.class },
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface TriggerMcpMapper extends McpMapper {

    @Mapping(target = "operations", source = "actionOperations")
    @Mapping(target = "isAdvertising", source = "advertising")
    @Mapping(target = "name", source = "shortname")
    TriggerResponse toResponse(EmmAction trigger);

    @Mapping(target = "isAdvertising", source = "advertising")
    @Mapping(target = "name", source = "shortname")
    TriggerLightResponse toLightResponse(EmmAction trigger);

    @Mapping(target = "isActive", expression = "java(Boolean.TRUE.equals(request.isActive()))")
    @Mapping(target = "isAdvertising", expression = "java(Boolean.TRUE.equals(request.isAdvertising()))")
    CreateTriggerCommand toCommand(CreateTriggerRequest request);

    PatchTriggerCommand toCommand(int id, PatchTriggerRequest request);

    default PageResponse<TriggerLightResponse> toPageResponse(PaginatedList<EmmAction> source) {
        return toPageResponse(source, this::toLightResponse);
    }

    default Instant map(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    default EmmActionType map(int type) {
        return EmmActionType.getEmmActionTypeByID(type);
    }

}
