/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.target.dto.CreateTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupLightResponse;
import com.agnitas.emm.mcp.v1.target.dto.TargetGroupResponse;
import com.agnitas.emm.mcp.v1.target.dto.PatchTargetGroupRequest;
import com.agnitas.emm.mcp.v1.target.service.TargetGroupMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class TargetGroupTools {

    private final TargetGroupMcpService targetGroupMcpService;
    private final McpAdminProvider adminProvider;

    public TargetGroupTools(
            TargetGroupMcpService targetGroupMcpService,
            McpAdminProvider adminProvider
    ) {
        this.targetGroupMcpService = targetGroupMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_target_groups",
            description = "Returns a paginated list of target groups"
    )
    public PageResponse<TargetGroupLightResponse> getList(
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.TARGETS_SHOW);
        return targetGroupMcpService.list(pageable, admin);
    }

    @McpTool(
            name = "get_target_group_by_id",
            description = "Returns a target group by its ID"
    )
    public TargetGroupResponse getById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.TARGETS_SHOW);
        return targetGroupMcpService.getById(id, admin.getCompanyID());
    }

    @McpTool(
            name = "create_target_group",
            description = "Creates a new target group"
    )
    public TargetGroupResponse create(
            @Valid CreateTargetGroupRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.TARGETS_CHANGE);
        return targetGroupMcpService.create(request, admin.getCompanyID());
    }

    @McpTool(
            name = "update_target_group",
            description = "Updates an existing target group. Fields not passed (null) are left unchanged."
    )
    public TargetGroupResponse patch(
            @McpToolParam(description = "Target group ID") @Positive int id,
            @Valid PatchTargetGroupRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.TARGETS_CHANGE);
        return targetGroupMcpService.patch(id, request, admin.getCompanyID());
    }

    @McpTool(
            name = "delete_target_group",
            description = "Deletes a target group by its ID"
    )
    public void deleteById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.TARGETS_DELETE);
        targetGroupMcpService.delete(id, admin);
    }

}
