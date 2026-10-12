/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.user.dto;

import java.util.Set;

import com.agnitas.emm.core.admin.validation.UiUserConstraints;
import com.agnitas.util.importvalues.Gender;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record CreateUiUserRequest(

        @McpToolParam(description = "Client (company) ID this user belongs to; defaults to the caller's own company", required = false)
        Integer clientId,

        @NotBlank
        @Size(min = UiUserConstraints.USERNAME_MIN_LENGTH, max = UiUserConstraints.USERNAME_MAX_LENGTH)
        @McpToolParam(description = "Login username")
        String username,

        @NotBlank
        @McpToolParam(description = "Login password")
        String password,

        @NotBlank
        @Size(min = UiUserConstraints.COMPANY_MIN_LENGTH, max = UiUserConstraints.COMPANY_MAX_LENGTH)
        @McpToolParam(description = "Company name shown for this user")
        String company,

        @McpToolParam(description = "Gender (MALE, FEMALE, UNKNOWN, PRAXIS, COMPANY)", required = false)
        Gender gender,

        @McpToolParam(description = "Title (e.g. \"Dr.\")", required = false)
        String title,

        @NotBlank
        @Size(max = UiUserConstraints.FIRSTNAME_MAX_LENGTH)
        @McpToolParam(description = "First name")
        String firstName,

        @NotBlank
        @Size(max = UiUserConstraints.LASTNAME_MAX_LENGTH)
        @McpToolParam(description = "Last name")
        String lastName,

        @NotBlank
        @Email
        @McpToolParam(description = "Email address")
        String email,

        @Email
        @McpToolParam(description = "Email address statistics reports are sent to", required = false)
        String statisticsEmail,

        @McpToolParam(description = "Phone number", required = false)
        String phone,

        @NotBlank
        @Pattern(regexp = UiUserConstraints.LANGUAGE_REGEX, message = UiUserConstraints.LANGUAGE_MESSAGE)
        @McpToolParam(description = "UI language, format 'lang_COUNTRY' (e.g. 'en_US')")
        String language,

        @McpToolParam(description = "Time zone (e.g. 'Europe/Berlin')", required = false)
        String timezone,

        @McpToolParam(description = "External employee ID", required = false)
        Integer employeeId,

        @NotEmpty
        @McpToolParam(description = "IDs of user groups this user belongs to; determines base permissions")
        Set<Integer> groupIds,

        @McpToolParam(description = "Additional permission codes to grant on top of the user groups", required = false)
        Set<String> permissions,

        @McpToolParam(description = "Target group IDs limiting which recipients this user can access. Provide at most one of "
                + "accessLimitingTargetGroupIds or accessLimitingTargetGroupId.", required = false)
        Set<Integer> accessLimitingTargetGroupIds,

        @McpToolParam(description = "Single target group ID limiting which recipients this user can access. Provide at most one of "
                + "accessLimitingTargetGroupIds or accessLimitingTargetGroupId.", required = false)
        Integer accessLimitingTargetGroupId,

        @McpToolParam(description = "Mailinglist IDs this user is not allowed to send to", required = false)
        Set<Integer> disabledMailingListIds
) {

    @SuppressWarnings("unused")
    @AssertTrue(message = UiUserConstraints.ALTG_MESSAGE)
    private boolean isAccessLimitingTargetGroup() {
        return accessLimitingTargetGroupIds == null
               || accessLimitingTargetGroupId == null;
    }
}
