/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.links;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.links.dto.PatchMailingLinkRequest;
import com.agnitas.emm.mcp.v1.mailing.links.service.MailingLinkMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingLinkTools {

    private final MailingLinkMcpService linkService;
    private final McpAdminProvider adminProvider;

    public MailingLinkTools(MailingLinkMcpService linkService, McpAdminProvider adminProvider) {
        this.linkService = linkService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailing_links",
            description = "Returns a paginated list of trackable links of a mailing, ordered by link ID"
    )
    public PageResponse<MailingLinkResponse> getList(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @Valid McpPageable request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        return linkService.list(mailingId, request, admin.getCompanyID());
    }

    @McpTool(
            name = "get_mailing_link_by_id",
            description = "Returns a trackable link of a mailing by its ID"
    )
    public MailingLinkResponse getById(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = "Link ID") @Positive int linkId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        return linkService.getById(linkId, mailingId, admin.getCompanyID());
    }

    @McpTool(
            name = "update_mailing_link",
            description = "Updates a trackable link of a mailing. Fields not passed (null) are left unchanged."
    )
    public MailingLinkResponse patch(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = "Link ID") @Positive int linkId,
            @Valid PatchMailingLinkRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        return linkService.patch(linkId, mailingId, request, admin);
    }

}
