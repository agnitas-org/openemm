/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.form.dto;

import com.agnitas.emm.core.userform.validation.FormConstraints;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record PatchFormRequest(
        @McpToolParam(description = "Name of the form; letters, numbers, hyphens and underscores only", required = false)
        JsonNullable<
                @NotNull
                @Size(min = FormConstraints.NAME_MIN_LENGTH, max = FormConstraints.NAME_MAX_LENGTH)
                @Pattern(
                        regexp = FormConstraints.NAME_PATTERN,
                        message = "Must contain only letters, numbers, hyphens (-), and underscores (_)"
                )
                String
        > name,

        @McpToolParam(description = "Description of the form", required = false)
        JsonNullable<String> description,

        @McpToolParam(description = "ID of the trigger to execute when the form is opened", required = false)
        JsonNullable<@Positive Integer> startTriggerId,

        @McpToolParam(description = "ID of the trigger to execute when the form is submitted", required = false)
        JsonNullable<@Positive Integer> endTriggerId,

        @McpToolParam(description = "Settings for the response shown after a successful submission", required = false)
        JsonNullable<@NotNull @Valid FormResultSettingsRequest> successSettings,

        @McpToolParam(description = "Settings for the response shown after a failed submission", required = false)
        JsonNullable<@NotNull @Valid FormResultSettingsRequest> errorSettings,

        @McpToolParam(description = "Whether the form is active", required = false)
        JsonNullable<@NotNull Boolean> active
) {
}
