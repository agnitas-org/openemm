/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.workflow;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.workflow.service.WorkflowMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class WorkflowTools {

    private final WorkflowMcpService workflowMcpService;
    private final McpAdminProvider adminProvider;

    public WorkflowTools(WorkflowMcpService workflowMcpService, McpAdminProvider adminProvider) {
        this.workflowMcpService = workflowMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_workflows",
            description = "Returns a paginated list of workflows (campaigns)"
    )
    public PageResponse<WorkflowLightResponse> getList(
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.WORKFLOW_SHOW);
        return workflowMcpService.list(pageable, admin);
    }

    @McpTool(
            name = "activate_workflow",
            description = "Activates a workflow (campaign)."
    )
    public WorkflowStatusResponse activate(
            @McpToolParam(description = "Workflow ID") @Positive int workflowId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.WORKFLOW_ACTIVATE);
        return workflowMcpService.activate(workflowId, admin.getCompanyID());
    }

    @McpTool(
            name = "deactivate_workflow",
            description = "Deactivates a workflow (campaign)."
    )
    public WorkflowStatusResponse deactivate(
            @McpToolParam(description = "Workflow ID") @Positive int workflowId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.WORKFLOW_ACTIVATE);
        return workflowMcpService.deactivate(workflowId, admin.getCompanyID());
    }

    @McpTool(
            name = "pause_workflow",
            description = "Pauses an active workflow (campaign)."
    )
    public WorkflowPauseResponse pause(
            @McpToolParam(description = "Workflow ID") @Positive int workflowId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.WORKFLOW_ACTIVATE);
        return workflowMcpService.pause(workflowId, admin.getCompanyID());
    }

}
