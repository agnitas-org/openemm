/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.binding.dto;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.common.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchBindingRequest(
        @McpToolParam(description = "Subscription status to set for the binding", required = false)
        JsonNullable<@NotNull UserStatus> userStatus,

        @McpToolParam(description = "Type of the recipient within the mailinglist", required = false)
        JsonNullable<UserType> userType,

        @McpToolParam(description = "Free-text remark explaining the subscription status", required = false)
        JsonNullable<String> userRemark,

        @McpToolParam(description = "Referrer that caused the binding to be created", required = false)
        JsonNullable<String> referrer,

        @McpToolParam(description = "ID of the mailing the recipient entered the mailinglist with", required = false)
        JsonNullable<@Positive Integer> entryMailingId,

        @McpToolParam(description = "ID of the mailing the recipient exited the mailinglist with", required = false)
        JsonNullable<@Positive Integer> exitMailingId
) {
}
