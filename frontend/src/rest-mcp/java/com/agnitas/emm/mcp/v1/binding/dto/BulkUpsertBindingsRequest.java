/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding.dto;

import java.util.SequencedSet;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record BulkUpsertBindingsRequest(
        @NotNull
        @Size(min = 1, max = 1000)
        @McpToolParam(description = "Recipient IDs to create or update bindings for")
        SequencedSet<@Positive Integer> recipientIds,

        @NotNull
        @Size(min = 1, max = 1000)
        @McpToolParam(description = "Mailinglist IDs to bind the recipients to")
        SequencedSet<@Positive Integer> mailinglistIds,

        @NotNull
        @Size(min = 1, max = 1000)
        @McpToolParam(description = "Media types to create or update bindings for")
        SequencedSet<MediaTypes> mediatypes,

        @NotNull
        @McpToolParam(description = "Subscription status to set for every combination of recipient, mailinglist and media type")
        UserStatus status
) {
}
