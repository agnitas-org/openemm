/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.attachments;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.AttachmentLightResponse;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.AttachmentResponse;
import com.agnitas.emm.mcp.v1.mailing.attachments.dto.UploadAttachmentRequest;
import com.agnitas.emm.mcp.v1.mailing.attachments.service.MailingAttachmentMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingAttachmentTools {

    private static final String ID_DESCRIPTION = "Attachment ID";
    private static final String FILENAME_DESCRIPTION = "Attachment filename";

    private final MailingAttachmentMcpService mailingAttachmentMcpService;
    private final McpAdminProvider adminProvider;

    public MailingAttachmentTools(
            MailingAttachmentMcpService mailingAttachmentMcpService,
            McpAdminProvider adminProvider
    ) {
        this.mailingAttachmentMcpService = mailingAttachmentMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailing_attachments",
            description = "Returns a paginated list of mailing attachments"
    )
    public PageResponse<AttachmentLightResponse> getList(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_ATTACHMENTS_SHOW);
        return mailingAttachmentMcpService.list(mailingId, pageable, admin);
    }

    @McpTool(
            name = "get_mailing_attachment",
            description = "Returns an attachment of a mailing by its ID or filename, including a signed URL to "
                    + "download its binary content. Provide either attachmentId or filename."
    )
    public AttachmentResponse get(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = ID_DESCRIPTION, required = false) Integer attachmentId,
            @McpToolParam(description = FILENAME_DESCRIPTION, required = false) String filename,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_ATTACHMENTS_SHOW);
        if (attachmentId != null) {
            return mailingAttachmentMcpService.getById(attachmentId, mailingId, admin);
        }
        if (filename != null) {
            return mailingAttachmentMcpService.getByFilename(filename, mailingId, admin);
        }
        throw ValidationException.exact("Either attachmentId or filename must be provided");
    }

    @McpTool(
            name = "create_mailing_attachment",
            description = "Creates a new attachment"
    )
    public AttachmentResponse create(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @Valid UploadAttachmentRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_ATTACHMENTS_SHOW);
        return mailingAttachmentMcpService.create(request, mailingId, admin);
    }

    @McpTool(
            name = "delete_mailing_attachment",
            description = "Deletes an attachment by its ID or filename. Provide either attachmentId or filename."
    )
    public void delete(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = ID_DESCRIPTION, required = false) Integer attachmentId,
            @McpToolParam(description = FILENAME_DESCRIPTION, required = false) String filename,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_ATTACHMENTS_SHOW);
        if (attachmentId != null) {
            mailingAttachmentMcpService.deleteById(attachmentId, mailingId, admin);
            return;
        }
        if (filename != null) {
            mailingAttachmentMcpService.deleteByFilename(filename, mailingId, admin);
            return;
        }
        throw ValidationException.exact("Either attachmentId or filename must be provided");
    }

}
