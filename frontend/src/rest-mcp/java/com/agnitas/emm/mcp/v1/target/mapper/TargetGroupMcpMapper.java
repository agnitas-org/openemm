/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target.mapper;

import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.beans.TargetLight;
import com.agnitas.emm.core.target.dto.CreateTargetCommand;
import com.agnitas.emm.core.target.dto.PatchTargetCommand;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.target.dto.CreateTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupLightResponse;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupResponse;
import com.agnitas.emm.mcp.v1.target.dto.PatchTargetGroupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TargetGroupMcpMapper extends McpMapper {

    @Mapping(target = "name", source = "targetName")
    @Mapping(target = "description", source = "targetDescription")
    @Mapping(target = "eql", source = "EQL")
    @Mapping(target = "sql", source = "EQL")
    TargetGroupResponse toResponse(Target target);

    CreateTargetCommand toCommand(CreateTargetGroupRequest request, int companyId);

    PatchTargetCommand toCommand(PatchTargetGroupRequest request, int id, int companyId);

    @Mapping(target = "name", source = "targetName")
    @Mapping(target = "description", source = "targetDescription")
    TargetGroupLightResponse toLightResponse(TargetLight target);

    default PageResponse<TargetGroupLightResponse> toPageResponse(PaginatedList<TargetLight> source) {
        return toPageResponse(source, this::toLightResponse);
    }

}
