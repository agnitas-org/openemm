/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.CreateTriggerRequest;
import com.agnitas.emm.mcp.v1.trigger.dto.TriggerLightResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.TriggerResponse;
import com.agnitas.emm.mcp.v1.trigger.dto.PatchTriggerRequest;
import com.agnitas.emm.mcp.v1.trigger.service.TriggerMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class TriggerTools {

    private final TriggerMcpService triggerMcpService;
    private final McpAdminProvider adminProvider;

    public TriggerTools(
            TriggerMcpService triggerMcpService,
            McpAdminProvider adminProvider
    ) {
        this.triggerMcpService = triggerMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_triggers",
            description = "Returns a paginated list of triggers"
    )
    public PageResponse<TriggerLightResponse> getList(
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ACTIONS_SHOW);
        return triggerMcpService.list(pageable, admin.getCompanyID());
    }

    @McpTool(
            name = "get_trigger_by_id",
            description = "Returns a trigger by its ID, including its operations"
    )
    public TriggerResponse getById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ACTIONS_SHOW);
        return triggerMcpService.getById(id, admin);
    }

    @McpTool(
            name = "create_trigger",
            description = "Creates a new trigger"
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck", jsonFields = "request.operations")
    public TriggerResponse create(
            @Valid CreateTriggerRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ACTIONS_CHANGE);
        return triggerMcpService.create(request, admin);
    }

    @McpTool(
            name = "update_trigger",
            description = "Updates an existing trigger. Fields not passed (absent) are left unchanged."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck", jsonFields = "request.operations")
    public TriggerResponse patch(
            @McpToolParam(description = "Trigger ID") @Positive int id,
            @Valid PatchTriggerRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ACTIONS_CHANGE);
        return triggerMcpService.patch(id, request, admin);
    }

    @McpTool(
            name = "execute_trigger",
            description = "Executes a trigger's operations, optionally with additional request-like field values"
    )
    public void execute(
            @McpToolParam(description = "Trigger ID") @Positive int id,
            @McpToolParam(description = "Additional field values passed to the trigger's operations", required = false)
            Map<String, Object> fields,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ACTIONS_SHOW);
        triggerMcpService.execute(id, admin.getCompanyID(), fields);
    }

    @McpTool(
            name = "delete_trigger",
            description = "Deletes a trigger by its ID"
    )
    public void delete(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ACTIONS_DELETE);
        triggerMcpService.delete(id, admin.getCompanyID());
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        return path.matches("request\\.operations\\[\\d+]\\.(script|htmlMail)");
    }
}
