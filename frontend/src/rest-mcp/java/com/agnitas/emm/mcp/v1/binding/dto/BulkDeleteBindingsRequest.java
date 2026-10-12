/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding.dto;

import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record BulkDeleteBindingsRequest(
        @Size(min = 1, max = 1000)
        @McpToolParam(description = "Recipient IDs to delete bindings for")
        List<@Positive Integer> recipientIds,

        @Size(min = 1, max = 1000)
        @McpToolParam(description = "Mailinglist IDs to delete the recipients' bindings from")
        Set<@Positive Integer> mailinglistIds,

        @McpToolParam(description = "Restrict deletion to these media types; if omitted, bindings of all media types are deleted", required = false)
        Set<MediaTypes> mediatypes
) {
}
