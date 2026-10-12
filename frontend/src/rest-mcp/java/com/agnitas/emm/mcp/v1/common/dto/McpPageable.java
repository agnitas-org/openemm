/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.common.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record McpPageable(
        @Min(1)
        @McpToolParam(description = "One-based page index (1 = first page)")
        int page,

        @Positive
        @McpToolParam(description = "Number of items per page")
        int size,

        @McpToolParam(description = "Field name to sort by", required = false)
        String sortBy,

        @McpToolParam(description = "Sort direction: true for ascending, false for descending", required = false)
        Boolean ascending
) {
}
