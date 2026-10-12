/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.send.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record StartSendMailingRequest(
        @McpToolParam(description = "Mailing ID")
        @Positive
        int mailingId,

        @FutureOrPresent(message = "Send time cannot be in the past")
        @McpToolParam(description = "When to send the mailing. Omit to send immediately.", required = false)
        OffsetDateTime sendDate,

        @McpToolParam(description = "Delay between sending blocks, in mails per hour.", required = false)
        Integer stepping,

        @McpToolParam(description = "Number of recipients per sending block.", required = false)
        Integer blockSize,

        @McpToolParam(description = "Whether to remove admin/test opens, clicks and reactions recorded before this delivery.", required = false)
        Boolean cleanupAdminAndTestActions
) {
}
