/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.images;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageLightResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.ImageResponse;
import com.agnitas.emm.mcp.v1.mailing.images.dto.UploadMailingImageRequest;
import com.agnitas.emm.mcp.v1.mailing.images.service.MailingImageMcpService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class MailingImageTools {

    private static final String ID_DESCRIPTION = "Image ID";
    private static final String FILENAME_DESCRIPTION = "Image filename";

    private final MailingImageMcpService mailingImageMcpService;
    private final McpAdminProvider adminProvider;

    public MailingImageTools(
            MailingImageMcpService mailingImageMcpService,
            McpAdminProvider adminProvider
    ) {
        this.mailingImageMcpService = mailingImageMcpService;
        this.adminProvider = adminProvider;
    }

    @McpTool(
            name = "get_mailing_images",
            description = "Returns a paginated list of mailing images"
    )
    public PageResponse<ImageLightResponse> getList(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @Valid McpPageable pageable,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        return mailingImageMcpService.list(mailingId, pageable, admin);
    }

    @McpTool(
            name = "get_mailing_image",
            description = "Returns an image of a mailing by its ID or filename, including a signed URL to "
                    + "download its binary content. Provide either imageId or filename."
    )
    public ImageResponse get(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = ID_DESCRIPTION, required = false) Integer imageId,
            @McpToolParam(description = FILENAME_DESCRIPTION, required = false) String filename,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_CONTENT_SHOW);
        if (imageId != null) {
            return mailingImageMcpService.getById(imageId, mailingId, admin);
        }
        if (filename != null) {
            return mailingImageMcpService.getByFilename(filename, mailingId, admin);
        }
        throw ValidationException.exact("Either imageId or filename must be provided");
    }

    @McpTool(
            name = "create_mailing_image",
            description = "Creates a new image"
    )
    public ImageResponse create(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @Valid UploadMailingImageRequest request,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_COMPONENTS_CHANGE);
        return mailingImageMcpService.create(request, mailingId, admin);
    }

    @McpTool(
            name = "delete_mailing_image",
            description = "Deletes an image by its ID or filename. Provide either imageId or filename."
    )
    public void delete(
            @McpToolParam(description = "Mailing ID") @Positive int mailingId,
            @McpToolParam(description = ID_DESCRIPTION, required = false) Integer imageId,
            @McpToolParam(description = FILENAME_DESCRIPTION, required = false) String filename,
            @SuppressWarnings("unused") McpSyncRequestContext requestContext
    ) {
        Admin admin = adminProvider.requireAdmin(Permission.MAILING_COMPONENTS_CHANGE);
        if (imageId != null) {
            mailingImageMcpService.deleteById(imageId, mailingId, admin);
            return;
        }
        if (filename != null) {
            mailingImageMcpService.deleteByFilename(filename, mailingId, admin);
            return;
        }
        throw ValidationException.exact("Either imageId or filename must be provided");
    }

}
