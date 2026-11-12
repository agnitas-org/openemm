/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record McpPageable(
        @Min(1)
        @McpToolParam(description = "One-based page index (1 = first page). Default: " + McpPageable.DEFAULT_PAGE, required = false)
        Integer page,

        @Positive
        @Max(McpPageable.MAX_SIZE)
        @McpToolParam(description = "Number of items per page. Default: " + McpPageable.DEFAULT_SIZE + ", maximum: " + McpPageable.MAX_SIZE, required = false)
        Integer size,

        @McpToolParam(description = "Field name to sort by", required = false)
        String sortBy,

        @McpToolParam(description = "Sort direction: true for ascending, false for descending", required = false)
        Boolean ascending
) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public static final McpPageable DEFAULT = new McpPageable(DEFAULT_PAGE, DEFAULT_SIZE, null, null);

    public static final String PARAM_DESCRIPTION = "Pagination and sorting. Optional; defaults to the first page with "
            + DEFAULT_SIZE + " items.";

    public McpPageable {
        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (size == null) {
            size = DEFAULT_SIZE;
        }
    }

    /**
     * MCP passes {@code null} when the client omits the whole {@code pageable} argument.
     */
    public static McpPageable orDefault(McpPageable pageable) {
        return pageable != null ? pageable : DEFAULT;
    }
}
