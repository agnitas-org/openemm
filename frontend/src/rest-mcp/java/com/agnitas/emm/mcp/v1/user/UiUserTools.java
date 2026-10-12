/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.user;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.user.dto.CreateUiUserRequest;
import com.agnitas.emm.mcp.v1.user.dto.UiUserLightResponse;
import com.agnitas.emm.mcp.v1.user.dto.UiUserResponse;
import com.agnitas.emm.mcp.v1.user.dto.PatchUiUserRequest;
import com.agnitas.emm.mcp.v1.user.service.UiUserMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class UiUserTools {

    private static final String ID_DESCRIPTION = "UI user ID. Provide exactly one of userId or username.";
    private static final String USERNAME_DESCRIPTION = "UI user login name. Provide exactly one of userId or username.";

    private final UiUserMcpService uiUserMcpService;
    private final McpAdminProvider adminProvider;

    public UiUserTools(UiUserMcpService uiUserMcpService, McpAdminProvider adminProvider) {
        this.uiUserMcpService = uiUserMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_ui_users",
            description = "Returns a paginated list of EMM UI users (administrators/operators of this EMM instance, not recipients)"
    )
    public PageResponse<UiUserLightResponse> getList(
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ADMIN_SHOW);
        return uiUserMcpService.list(pageable, admin);
    }

    @McpTool(
            name = "get_ui_user",
            description = "Returns a single EMM UI user by ID or username"
    )
    public UiUserResponse get(
            @McpToolParam(description = ID_DESCRIPTION, required = false) @Positive Integer userId,
            @McpToolParam(description = USERNAME_DESCRIPTION, required = false) String username,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ADMIN_SHOW);
        return uiUserMcpService.get(userId, username, admin.getCompanyID());
    }

    @McpTool(
            name = "create_ui_user",
            description = "Creates a new EMM UI user"
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public UiUserResponse create(
            @Valid CreateUiUserRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ADMIN_NEW);
        return uiUserMcpService.create(request, admin);
    }

    @McpTool(
            name = "update_ui_user",
            description = "Updates an existing EMM UI user by ID or username. Fields not passed are left unchanged."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public UiUserResponse patch(
            @McpToolParam(description = ID_DESCRIPTION, required = false) @Positive Integer userId,
            @McpToolParam(description = USERNAME_DESCRIPTION, required = false) String username,
            @Valid PatchUiUserRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ADMIN_CHANGE);
        return uiUserMcpService.patch(userId, username, request, admin);
    }

    @McpTool(
            name = "delete_ui_user",
            description = "Deletes an EMM UI user by ID or username"
    )
    public void delete(
            @McpToolParam(description = ID_DESCRIPTION, required = false) @Positive Integer userId,
            @McpToolParam(description = USERNAME_DESCRIPTION, required = false) String username,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.ADMIN_DELETE);
        uiUserMcpService.delete(userId, username, admin.getCompanyID());
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        return "request.password".equals(path);
    }
}
