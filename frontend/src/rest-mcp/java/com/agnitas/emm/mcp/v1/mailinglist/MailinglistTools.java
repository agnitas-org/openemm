/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailinglist;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailinglist.dto.CreateMailinglistRequest;
import com.agnitas.emm.mcp.v1.mailinglist.dto.MailinglistResponse;
import com.agnitas.emm.mcp.v1.mailinglist.dto.PatchMailinglistRequest;
import com.agnitas.emm.mcp.v1.mailinglist.service.MailinglistMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailinglistTools {

    private final MailinglistMcpService mailinglistMcpService;
    private final McpAdminProvider adminProvider;

    public MailinglistTools(
            MailinglistMcpService mailinglistMcpService,
            McpAdminProvider adminProvider
    ) {
        this.mailinglistMcpService = mailinglistMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailinglists",
            description = "Returns a paginated list of mailinglists"
    )
    public PageResponse<MailinglistResponse> getList(
            @Valid McpPageable request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILINGLIST_SHOW);
        return mailinglistMcpService.list(request, admin.getCompanyID());
    }

    @McpTool(
            name = "get_mailinglist_by_id",
            description = "Returns a mailinglist by its ID"
    )
    public MailinglistResponse getById(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILINGLIST_SHOW);
        return mailinglistMcpService.getById(id, admin.getCompanyID());
    }

    @McpTool(
            name = "create_mailinglist",
            description = "Creates a new mailinglist"
    )
    public MailinglistResponse create(
            @Valid CreateMailinglistRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILINGLIST_CHANGE);
        return mailinglistMcpService.create(request, admin.getCompanyID());
    }

    @McpTool(
            name = "update_mailinglist",
            description = "Updates an existing mailinglist. Fields not passed (null) are left unchanged."
    )
    public MailinglistResponse patch(
            @McpToolParam(description = "Mailinglist ID") @Positive int id,
            PatchMailinglistRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILINGLIST_CHANGE);
        return mailinglistMcpService.patch(id, request, admin.getCompanyID());
    }

    @McpTool(
            name = "delete_mailinglist",
            description = "Deletes a mailinglist by its ID"
    )
    public void delete(
            @Positive int id,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILINGLIST_DELETE);
        mailinglistMcpService.delete(id, admin);
    }

}
