/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.blocklist;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.blocklist.dto.BlocklistEntryResponse;
import com.agnitas.emm.mcp.v1.blocklist.dto.CreateBlocklistEntryRequest;
import com.agnitas.emm.mcp.v1.blocklist.dto.PatchBlocklistEntryRequest;
import com.agnitas.emm.mcp.v1.blocklist.service.BlocklistMcpService;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class BlocklistTools {

    private final BlocklistMcpService blocklistMcpService;
    private final McpAdminProvider adminProvider;

    public BlocklistTools(
            BlocklistMcpService blocklistMcpService,
            McpAdminProvider adminProvider
    ) {
        this.blocklistMcpService = blocklistMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_blocklist_entries",
            description = "Returns a paginated list of blocklisted email addresses"
    )
    public PageResponse<BlocklistEntryResponse> getList(
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.BLACKLIST);
        return blocklistMcpService.list(pageable, admin.getCompanyID());
    }

    @McpTool(
            name = "get_blocklist_entry_by_email",
            description = "Returns a blocklist entry by email address"
    )
    public BlocklistEntryResponse getByEmail(
            @McpToolParam(description = "Email address") @NotBlank String email,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.BLACKLIST);
        return blocklistMcpService.getByEmail(email, admin.getCompanyID());
    }

    @McpTool(
            name = "create_blocklist_entry",
            description = "Adds an email address to the blocklist, preventing it from receiving mailings"
    )
    public BlocklistEntryResponse create(
            @Valid CreateBlocklistEntryRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.BLACKLIST);
        return blocklistMcpService.create(request, admin);
    }

    @McpTool(
            name = "update_blocklist_entry",
            description = "Updates the reason of an existing blocklist entry"
    )
    public BlocklistEntryResponse patch(
            @McpToolParam(description = "Email address") @NotBlank String email,
            @Valid PatchBlocklistEntryRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.BLACKLIST);
        return blocklistMcpService.patch(email, request, admin.getCompanyID());
    }

    @McpTool(
            name = "delete_blocklist_entry",
            description = "Removes an email address from the blocklist"
    )
    public void delete(
            @McpToolParam(description = "Email address") @NotBlank String email,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_DELETE);
        blocklistMcpService.delete(email, admin.getCompanyID());
    }

}
