/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.workflow.mapper;

import java.time.Instant;

import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.workflow.WorkflowLightResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WorkflowMcpMapper extends McpMapper {

    @Mapping(target = "name", source = "workflow.shortname")
    @Mapping(target = "id", source = "workflow.workflowId")
    WorkflowLightResponse toLightResponse(Workflow workflow, Instant pauseTime, int pauseExpirationHours);

}
