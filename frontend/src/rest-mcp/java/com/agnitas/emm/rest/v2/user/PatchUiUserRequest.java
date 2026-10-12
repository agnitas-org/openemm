/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.user;

import java.util.Set;

import com.agnitas.emm.core.admin.validation.UiUserConstraints;
import com.agnitas.util.importvalues.Gender;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

public record PatchUiUserRequest(

        JsonNullable<
                @NotBlank
                @Size(min = UiUserConstraints.USERNAME_MIN_LENGTH, max = UiUserConstraints.USERNAME_MAX_LENGTH)
                String
        > username,

        JsonNullable<@NotBlank String> password,

        JsonNullable<
                @NotBlank
                @Size(min = UiUserConstraints.COMPANY_MIN_LENGTH, max = UiUserConstraints.COMPANY_MAX_LENGTH)
                String
        > company,

        JsonNullable<@NotNull Gender> gender,

        JsonNullable<String> title,

        JsonNullable<
                @NotBlank
                @Size(max = UiUserConstraints.FIRSTNAME_MAX_LENGTH)
                String
        > firstName,

        JsonNullable<
                @NotBlank
                @Size(max = UiUserConstraints.LASTNAME_MAX_LENGTH)
                String
        > lastName,

        JsonNullable<@NotBlank @Email String> email,

        JsonNullable<@Email String> statisticsEmail,

        JsonNullable<String> phone,

        JsonNullable<
                @NotNull
                @Pattern(regexp = UiUserConstraints.LANGUAGE_REGEX, message = UiUserConstraints.LANGUAGE_MESSAGE)
                String
        > language,

        JsonNullable<String> timezone,

        JsonNullable<String> employeeId,

        JsonNullable<@NotEmpty Set<Integer>> groupIds,

        JsonNullable<Set<String>> permissions,

        JsonNullable<Set<Integer>> accessLimitingTargetGroupIds,

        JsonNullable<Integer> accessLimitingTargetGroupId,

        JsonNullable<Set<Integer>> disabledMailingListIds
) {

    @SuppressWarnings("unused")
    @AssertTrue(message = UiUserConstraints.ALTG_MESSAGE)
    private boolean isAccessLimitingTargetGroupValid() {
        return !accessLimitingTargetGroupIds.isPresent() || !accessLimitingTargetGroupId.isPresent();
    }
}
