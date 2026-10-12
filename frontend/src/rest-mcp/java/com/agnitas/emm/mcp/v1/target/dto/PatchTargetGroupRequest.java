/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.target.dto;

import jakarta.validation.constraints.NotBlank;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchTargetGroupRequest(
        @McpToolParam(description = "Name of the target group", required = false)
        JsonNullable<@NotBlank String> name,

        @McpToolParam(description = "Description of the target group", required = false)
        JsonNullable<String> description,

        @McpToolParam(description = "EQL expression defining the target group, e.g. \"gender = 1 AND firstname IS NOT EMPTY\". "
                + "See the \"docs://target-groups/eql-syntax\" resource for full syntax.", required = false)
        JsonNullable<@NotBlank String> eql
) {
}
