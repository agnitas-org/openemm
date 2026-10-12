/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.images.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record UploadMailingImageRequest(
        @NotBlank
        @McpToolParam(description = "Public URL of the image file to upload (e.g. https://example.com/logo.png). "
                + "The filename used for the image is taken from this URL")
        String url,

        @McpToolParam(description = "Description of the image", required = false)
        String description,

        @McpToolParam(description = "URL the image links to when clicked in the mailing. Must not contain spaces "
                + "or quote characters", required = false)
        String linkTarget,

        @McpToolParam(description = "Filename of an existing desktop (non-mobile) image in this mailing that this "
                + "image is the mobile version of", required = false)
        String mobileVersionOf
) {
}
