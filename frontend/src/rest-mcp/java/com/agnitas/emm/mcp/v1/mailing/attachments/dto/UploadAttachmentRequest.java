/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.attachments.dto;

import com.agnitas.emm.core.components.form.AttachmentType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record UploadAttachmentRequest(
        @McpToolParam(description = "Filename including extension (e.g. logo.png). Defaults to the file name from url", required = false)
        String name,

        @NotBlank
        @McpToolParam(description = "Public URL of the file to upload (e.g. https://example.com/logo.png). For a "
                + "PERSONALIZED attachment, this must be an XSL-FO/XML template used to render the PDF content, "
                + "not the file to attach as-is")
        String url,

        @McpToolParam(description = "Attachment type; defaults to NORMAL if omitted. PERSONALIZED renders a "
                + "per-recipient PDF from url (the XSL-FO/XML template) and pdfBackgroundFileUrl (the PDF "
                + "background), which is then required", required = false)
        AttachmentType type,

        @McpToolParam(description = "Public URL of the PDF background file. Required when type is PERSONALIZED "
                + "(it becomes the attachment's PDF content); ignored for a NORMAL attachment", required = false)
        String pdfBackgroundFileUrl,

        @PositiveOrZero
        @McpToolParam(description = "Target ID", required = false)
        Integer targetId
) {

    public UploadAttachmentRequest {
        if (type == null) {
            type = AttachmentType.NORMAL;
        }
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "pdfBackgroundFileUrl is required when type is PERSONALIZED")
    private boolean isPdfBackgroundFileUrlValid() {
        return type != AttachmentType.PERSONALIZED || StringUtils.isNotBlank(pdfBackgroundFileUrl);
    }

}
