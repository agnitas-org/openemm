/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.profilefield;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.profilefield.dto.ProfileFieldResponse;
import com.agnitas.emm.mcp.v1.profilefield.service.ProfileFieldMcpService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

@Component
public class ProfileFieldTools {

    private final ProfileFieldMcpService profileFieldMcpService;
    private final McpAdminProvider adminProvider;

    public ProfileFieldTools(ProfileFieldMcpService profileFieldMcpService, McpAdminProvider adminProvider) {
        this.profileFieldMcpService = profileFieldMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_profile_fields",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description = """
                    Returns the recipient profile fields visible to the current user with their data types. \
                    Use "column" as the key in recipient data and "eqlName" to reference the field in EQL expressions."""
    )
    public List<ProfileFieldResponse> getList(@SuppressWarnings("unused") McpSyncRequestContext requestContext) {
        Admin admin = adminProvider.requireAdmin(Permission.RECIPIENT_SHOW);
        return profileFieldMcpService.list(admin);
    }
}
