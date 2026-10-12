/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.content.dto;

import java.util.List;

import jakarta.validation.Valid;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchMailingContentRequest(
        @McpToolParam(description = "Whether to disable link extension for this content block", required = false)
        JsonNullable<Boolean> disableLinkExtension,

        @McpToolParam(description = "Target-specific content variants. All existing variants are replaced with the provided list.", required = false)
        JsonNullable<List<@Valid UpdateTargetContentRequest>> content
) {}
