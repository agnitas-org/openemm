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

    private static final String ID_OR_NAME_REQUIRED_MESSAGE = "Either contentId or contentBlockName must be provided";

    private final MailingContentMcpService contentService;
    private final McpAdminProvider adminProvider;

    public MailingContentTools(MailingContentMcpService contentService, McpAdminProvider adminProvider) {
        this.contentService = contentService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailing_contents",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description = "Returns all content blocks for a mailing. There is no tool to create a content block: add an "
                    + "[agnDYN name=\"...\" /] reference to a mailing template via update_mailing, then fill it with update_mailing_content."
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
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false),
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
        throw ValidationException.exact(ID_OR_NAME_REQUIRED_MESSAGE);
    }

    @McpTool(
            name = "update_mailing_content",
            annotations = @McpTool.McpAnnotations(destructiveHint = false, idempotentHint = true, openWorldHint = false),
            description = "Updates an existing mailing content block by its ID or name. Provide either contentId or contentBlockName. "
                    + "Fields not passed (null) are left unchanged. Content blocks cannot be created directly: to create a new one, "
                    + "first add a reference such as [agnDYN name=\"my_block\" /] to htmlTemplate/textTemplate/smsTemplate/rcsTemplate "
                    + "via create_mailing or update_mailing (the block is created when the mailing is saved), then call this tool "
                    + "with contentBlockName=\"my_block\" to set its content per target group. To change an image, reference "
                    + "an uploaded image (create_mailing_image / get_mailing_images) via agnIMAGE and keep the surrounding HTML."
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
        throw ValidationException.exact(ID_OR_NAME_REQUIRED_MESSAGE);
    }

    @McpTool(
            name = "delete_mailing_content",
            annotations = @McpTool.McpAnnotations(idempotentHint = true, openWorldHint = false),
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
        throw ValidationException.exact(ID_OR_NAME_REQUIRED_MESSAGE);
    }

    @SuppressWarnings("unused")
    public boolean isExcludedFromXssCheck(String path, Admin admin) {
        return path.matches("request\\.content\\[\\d+]\\.text");
    }
}
