/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.links.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchMailingLinkRequest(
        @McpToolParam(description = "Link URL", required = false)
        JsonNullable<@NotBlank String> url,

        @McpToolParam(description = "Link description", required = false)
        JsonNullable<String> description,

        @McpToolParam(description = "Tracking mode: NOT_TRACKABLE, TEXT, HTML or TEXT_AND_HTML", required = false)
        JsonNullable<LinkTracking> tracking,

        @McpToolParam(description = "Retargeting mode: WITHOUT or SET_COOKIE", required = false)
        JsonNullable<LinkRetargeting> retargeting,

        @McpToolParam(description = "ID of the trigger (action) executed when the link is clicked. Use 0 or null to clear.", required = false)
        JsonNullable<Integer> triggerId,

        @McpToolParam(description = "Whether the link is static (not personalized)", required = false)
        JsonNullable<Boolean> staticLink,

        @McpToolParam(description = "Whether the link is administrative", required = false)
        JsonNullable<Boolean> administrative,

        @McpToolParam(description = "Link extensions (URL parameters). All existing extensions are replaced with the provided list.", required = false)
        JsonNullable<List<@Valid LinkExtensionRequest>> extensions
) {}
