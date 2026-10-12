/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.admin.service;

import static com.agnitas.emm.core.admin.validation.UiUserConstraints.AVAILABLE_TIMEZONES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.core.admin.validation.UiUserConstraints;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.validator.UserValidationField;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.importvalues.Gender;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public record PatchUiUserCommand(
        PatchField<String> username,
        PatchField<String> companyName,
        PatchField<Gender> gender,
        PatchField<String> title,
        PatchField<String> firstName,
        PatchField<String> lastName,
        PatchField<String> email,
        PatchField<String> statisticsEmail,
        PatchField<String> phone,
        PatchField<String> language,
        PatchField<String> timezone,
        PatchField<String> password,
        PatchField<String> employeeId,
        PatchField<Set<Integer>> groupIds,
        PatchField<Set<String>> permissions,
        PatchField<Set<Integer>> accessLimitingTargetGroupIds,
        PatchField<Integer> accessLimitingTargetGroupId,
        PatchField<Set<Integer>> disabledMailingListIds
) {

    public PatchUiUserCommand {
        gender.check(Objects::nonNull, UserValidationField.GENDER);
        username.check(
                v -> assertLength(v, UiUserConstraints.USERNAME_MIN_LENGTH, UiUserConstraints.USERNAME_MAX_LENGTH),
                UserValidationField.USERNAME
        );
        companyName.check(
                v -> assertLength(v, UiUserConstraints.COMPANY_MIN_LENGTH, UiUserConstraints.COMPANY_MAX_LENGTH),
                UserValidationField.COMPANY_NAME
        );
        firstName.check(v -> assertLength(v, 1, UiUserConstraints.FIRSTNAME_MAX_LENGTH), UserValidationField.FIRST_NAME);
        lastName.check(v -> assertLength(v, 1, UiUserConstraints.LASTNAME_MAX_LENGTH), UserValidationField.LAST_NAME);
        email.check(AgnUtils::isEmailValid, UserValidationField.EMAIL);
        statisticsEmail.check(v -> v == null || AgnUtils.isEmailValid(v), UserValidationField.STATISTICS_EMAIL);
        language.check(v -> v.matches(UiUserConstraints.LANGUAGE_REGEX), UserValidationField.LANGUAGE);
        timezone.check(v -> v == null || AVAILABLE_TIMEZONES.contains(v), UserValidationField.TIMEZONE);
        groupIds.check(CollectionUtils::isNotEmpty, UserValidationField.GROUPS);
        if (accessLimitingTargetGroupIds.present() && accessLimitingTargetGroupId.present()) {
            throw ValidationException.exact(UiUserConstraints.ALTG_MESSAGE);
        }
    }

    private boolean assertLength(String value, int minLength, int maxLength) {
        return isNotBlank(value)
            && StringUtils.length(value.trim()) >= minLength
            && StringUtils.length(value) <= maxLength;
    }
}
