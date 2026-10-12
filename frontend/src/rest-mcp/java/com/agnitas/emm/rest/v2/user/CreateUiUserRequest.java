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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUiUserRequest(

        Integer clientId,

        @NotBlank
        @Size(min = UiUserConstraints.USERNAME_MIN_LENGTH, max = UiUserConstraints.USERNAME_MAX_LENGTH)
        String username,

        @NotBlank
        String password,

        @NotBlank
        @Size(min = UiUserConstraints.COMPANY_MIN_LENGTH, max = UiUserConstraints.COMPANY_MAX_LENGTH)
        String company,

        Gender gender,

        String title,

        @NotBlank
        @Size(max = UiUserConstraints.FIRSTNAME_MAX_LENGTH)
        String firstName,

        @NotBlank
        @Size(max = UiUserConstraints.LASTNAME_MAX_LENGTH)
        String lastName,

        @NotBlank
        @Email
        String email,

        @Email
        String statisticsEmail,

        String phone,

        @NotBlank
        @Pattern(regexp = UiUserConstraints.LANGUAGE_REGEX, message = UiUserConstraints.LANGUAGE_MESSAGE)
        String language,

        String timezone,

        Integer employeeId,

        @NotEmpty
        Set<Integer> groupIds,

        Set<String> permissions,

        Set<Integer> accessLimitingTargetGroupIds,

        Integer accessLimitingTargetGroupId,

        Set<Integer> disabledMailingListIds
) {

    @SuppressWarnings("unused")
    @AssertTrue(message = UiUserConstraints.ALTG_MESSAGE)
    private boolean isAccessLimitingTargetGroup() {
        return accessLimitingTargetGroupIds == null
               || accessLimitingTargetGroupId == null;
    }
}
