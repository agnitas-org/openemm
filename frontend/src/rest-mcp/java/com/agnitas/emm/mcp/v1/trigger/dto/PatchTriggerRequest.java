/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger.dto;

import com.agnitas.emm.core.action.bean.EmmActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchTriggerRequest(
        @McpToolParam(description = "Name of the trigger", required = false)
        JsonNullable<@NotBlank String> name,

        @McpToolParam(description = "Description of the trigger", required = false)
        JsonNullable<String> description,

        @McpToolParam(description = "Type of the trigger: Link (usable on a mailing link/open/click action), "
                + "Form (usable as a form's startTriggerId/endTriggerId), or All (both). See "
                + "docs://triggers/operation-types for details.", required = false)
        JsonNullable<@NotNull EmmActionType> type,

        @McpToolParam(description = "Whether the trigger is active", required = false)
        JsonNullable<Boolean> isActive,

        @McpToolParam(description = "Whether the trigger is an advertising action", required = false)
        JsonNullable<Boolean> isAdvertising,

        @McpToolParam(
                description = "Operations to execute when the trigger fires, replacing all existing operations: "
                        + "a JSON array, encoded as a string (this parameter's type is string, not array), e.g. "
                        + "\"[{\\\"type\\\":\\\"SendMailing\\\",\\\"mailingID\\\":123}]\". Read the "
                        + "docs://triggers/operation-types resource for the full list of operation types and "
                        + "their fields.",
                required = false
        )
        JsonNullable<String> operations
) {
    public PatchTriggerRequest {
        operations.ifPresent(TriggerOperationsJsonValidation::validate);
    }
}
