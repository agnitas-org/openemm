/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.binding.dto.BindingResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkDeleteBindingsResponse;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsRequest;
import com.agnitas.emm.mcp.v1.binding.dto.BulkUpsertBindingsResponse;
import com.agnitas.emm.mcp.v1.binding.dto.CreateBindingRequest;
import com.agnitas.emm.mcp.v1.binding.dto.ListBindingsFilter;
import com.agnitas.emm.mcp.v1.binding.dto.PatchBindingRequest;
import com.agnitas.emm.mcp.v1.binding.service.BindingMcpService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class BindingTools {

    private final BindingMcpService bindingMcpService;
    private final McpAdminProvider adminProvider;

    public BindingTools(
            BindingMcpService bindingMcpService,
            McpAdminProvider adminProvider
    ) {
        this.bindingMcpService = bindingMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_bindings",
            description = "Returns a paginated list of mailinglist bindings (recipient subscriptions), optionally filtered"
    )
    public PageResponse<BindingResponse> getList(
            @Valid ListBindingsFilter filter,
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return bindingMcpService.list(filter, pageable, admin.getCompanyID());
    }

    @McpTool(
            name = "get_binding",
            description = "Returns a mailinglist binding identified by recipient ID, mailinglist ID and media type"
    )
    public BindingResponse getById(
            @McpToolParam(description = "Recipient ID") @Positive int recipientId,
            @McpToolParam(description = "Mailinglist ID") @Positive int mailinglistId,
            @McpToolParam(description = "Media type of the binding, e.g. EMAIL") MediaTypes mediatype,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return bindingMcpService.getById(recipientId, mailinglistId, mediatype, admin.getCompanyID());
    }

    @McpTool(
            name = "create_binding",
            description = "Creates a new mailinglist binding (subscribes a recipient to a mailinglist)"
    )
    public BindingResponse create(
            @Valid CreateBindingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        return bindingMcpService.create(request, admin.getCompanyID());
    }

    @McpTool(
            name = "update_binding",
            description = "Updates an existing mailinglist binding. Fields not passed (absent) are left unchanged."
    )
    public BindingResponse patch(
            @McpToolParam(description = "Recipient ID") @Positive int recipientId,
            @McpToolParam(description = "Mailinglist ID") @Positive int mailinglistId,
            @McpToolParam(description = "Media type of the binding, e.g. EMAIL") MediaTypes mediatype,
            @Valid PatchBindingRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        return bindingMcpService.patch(recipientId, mailinglistId, mediatype, request, admin.getCompanyID());
    }

    @McpTool(
            name = "delete_binding",
            description = "Deletes a mailinglist binding identified by recipient ID, mailinglist ID and media type"
    )
    public void delete(
            @McpToolParam(description = "Recipient ID") @Positive int recipientId,
            @McpToolParam(description = "Mailinglist ID") @Positive int mailinglistId,
            @McpToolParam(description = "Media type of the binding, e.g. EMAIL") MediaTypes mediatype,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        bindingMcpService.delete(recipientId, mailinglistId, mediatype, admin.getCompanyID());
    }

    @McpTool(
            name = "delete_bindings",
            description = "Deletes mailinglist bindings in bulk for combinations of the given recipients and mailinglists"
    )
    public BulkDeleteBindingsResponse deleteBulk(
            @Valid BulkDeleteBindingsRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        return bindingMcpService.deleteBulk(request, admin.getCompanyID());
    }

    @McpTool(
            name = "upsert_bindings",
            description = "Creates or updates mailinglist bindings in bulk for every combination of the given recipients, "
                    + "mailinglists and media types, setting the given subscription status"
    )
    public BulkUpsertBindingsResponse upsertBulk(
            @Valid BulkUpsertBindingsRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_CHANGE);
        return bindingMcpService.upsertBulk(request, admin);
    }

}
