/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger.dto;

import com.agnitas.emm.core.action.bean.EmmActionType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record CreateTriggerRequest(
        @NotBlank
        @McpToolParam(description = "Name of the trigger")
        String name,

        @McpToolParam(description = "Description of the trigger", required = false)
        String description,

        @McpToolParam(description = "Type of the trigger: Link (usable on a mailing link/open/click action), "
                + "Form (usable as a form's startTriggerId/endTriggerId), or All (both). See "
                + "docs://triggers/operation-types for details.")
        EmmActionType type,

        @McpToolParam(description = "Whether the trigger is active; defaults to false if omitted", required = false)
        Boolean isActive,

        @McpToolParam(description = "Whether the trigger is an advertising action; defaults to false if omitted", required = false)
        Boolean isAdvertising,

        @McpToolParam(
                description = "Operations to execute when the trigger fires: a JSON array, encoded as a string "
                        + "(this parameter's type is string, not array), e.g. "
                        + "\"[{\\\"type\\\":\\\"SendMailing\\\",\\\"mailingID\\\":123}]\". Read the "
                        + "docs://triggers/operation-types resource for the full list of operation types and "
                        + "their fields.",
                required = false
        )
        String operations
) {
    public CreateTriggerRequest {
        TriggerOperationsJsonValidation.validate(operations);
    }
}
