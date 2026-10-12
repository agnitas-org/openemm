/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.content;

import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.infrastructure.security.xss.McpXssExclude;
import com.agnitas.emm.mcp.v1.mailing.content.dto.PatchMailingContentRequest;
import com.agnitas.emm.mcp.v1.mailing.content.service.MailingContentMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingContentTools {

    private final MailingContentMcpService contentService;
    private final McpAdminProvider adminProvider;

    public MailingContentTools(MailingContentMcpService contentService, McpAdminProvider adminProvider) {
        this.contentService = contentService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailing_contents",
            description = "Returns all content blocks for a mailing"
    )
    public List<MailingContentResponse> getList(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        return contentService.list(mailingId, admin.getCompanyID());
    }

    @McpTool(
            name = "get_mailing_content",
            description = "Returns a mailing content block by its ID or name. Provide either contentId or contentBlockName."
    )
    public MailingContentResponse get(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = "Content block ID", required = false) Integer contentId,
            @McpToolParam(description = "Content block name", required = false) String contentBlockName,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        if (contentId != null) {
            return contentService.getById(contentId, mailingId, admin.getCompanyID());
        }
        if (contentBlockName != null) {
            return contentService.getByName(contentBlockName, mailingId, admin.getCompanyID());
        }
        throw ValidationException.exact("Either contentId or contentBlockName must be provided");
    }

    @McpTool(
            name = "update_mailing_content",
            description = "Updates an existing mailing content block by its ID or name. Provide either contentId or contentBlockName. "
                    + "Fields not passed (null) are left unchanged."
    )
    @McpXssExclude(checkMethod = "isExcludedFromXssCheck")
    public MailingContentResponse patch(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = "Content block ID", required = false) Integer contentId,
            @McpToolParam(description = "Content block name", required = false) String contentBlockName,
            @Valid PatchMailingContentRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CHANGE);
        if (contentId != null) {
            return contentService.patch(contentId, mailingId, request, admin);
        }
        if (contentBlockName != null) {
            return contentService.patch(contentBlockName, mailingId, request, admin);
        }
        throw ValidationException.exact("Either contentId or contentBlockName must be provided");
    }

    @McpTool(
            name = "delete_mailing_content",
            description = "Deletes a mailing content block by its ID or name. Provide either contentId or contentBlockName."
    )
    public void delete(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = "Content block ID", required = false) Integer contentId,
            @McpToolParam(description = "Content block name", required = false) String contentBlockName,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CHANGE);
        if (contentId != null) {
            contentService.delete(contentId, mailingId, admin.getCompanyID());
            return;
        }
        if (contentBlockName != null) {
            contentService.delete(contentBlockName, mailingId, admin.getCompanyID());
            return;
        }
        throw ValidationException.exact("Either contentId or contentBlockName must be provided");
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        return path.matches("request\\.content\\[\\d+]\\.text");
    }
}
