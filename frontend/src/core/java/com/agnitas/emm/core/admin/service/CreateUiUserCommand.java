/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.admin.service;

import static com.agnitas.emm.core.admin.validation.UiUserConstraints.AVAILABLE_TIMEZONES;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.tomcat.jakartaee.commons.lang3.ObjectUtils.isNotEmpty;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import com.agnitas.emm.core.admin.validation.UiUserConstraints;
import com.agnitas.emm.core.validator.UserValidationField;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.importvalues.Gender;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public record CreateUiUserCommand(
        int clientId,
        String username,
        String companyName,
        Gender gender,
        String title,
        String firstName,
        String lastName,
        String email,
        String statisticsEmail,
        String phone,
        String language,
        String timezone,
        String password,
        String employeeId,
        Set<Integer> groupIds,
        Set<String> permissions,
        Set<Integer> accessLimitingTargetGroupIds,
        int accessLimitingTargetGroupId,
        Set<Integer> disabledMailingListIds
) {
    public CreateUiUserCommand {
        gender = Objects.requireNonNullElse(gender, Gender.UNKNOWN);
        permissions = Objects.requireNonNullElse(permissions, Collections.emptySet());
        accessLimitingTargetGroupIds = Objects.requireNonNullElse(accessLimitingTargetGroupIds, Collections.emptySet());
        disabledMailingListIds = Objects.requireNonNullElse(disabledMailingListIds, Collections.emptySet());
        if (isBlank(language)) {
            language = "de_DE";
        }
        if (isBlank(timezone)) {
            timezone = "Europe/Berlin";
        }
        assertLength(
                UserValidationField.USERNAME,
                username,
                UiUserConstraints.USERNAME_MIN_LENGTH,
                UiUserConstraints.USERNAME_MAX_LENGTH
        );
        assertLength(
                UserValidationField.COMPANY_NAME,
                companyName,
                UiUserConstraints.COMPANY_MIN_LENGTH,
                UiUserConstraints.COMPANY_MAX_LENGTH
        );
        assertLength(UserValidationField.FIRST_NAME, firstName, 1, UiUserConstraints.FIRSTNAME_MAX_LENGTH);
        assertLength(UserValidationField.LAST_NAME, lastName, 1, UiUserConstraints.LASTNAME_MAX_LENGTH);

        if (isNotEmpty(accessLimitingTargetGroupIds) && accessLimitingTargetGroupId > 0) {
            throw ValidationException.exact(UiUserConstraints.ALTG_MESSAGE);
        }
        if (isBlank(email) || !AgnUtils.isEmailValid(email)) {
            throw ValidationException.invalidValue(UserValidationField.EMAIL);
        }
        if (isNotBlank(statisticsEmail) && !AgnUtils.isEmailValid(statisticsEmail)) {
            throw ValidationException.invalidValue(UserValidationField.STATISTICS_EMAIL);
        }
        if (!language.matches(UiUserConstraints.LANGUAGE_REGEX)) {
            throw ValidationException.invalidValue(UserValidationField.LANGUAGE);
        }
        if (!AVAILABLE_TIMEZONES.contains(timezone)) {
            throw ValidationException.invalidValue(UserValidationField.TIMEZONE);
        }
        if (CollectionUtils.isEmpty(groupIds)) {
            throw ValidationException.invalidValue(UserValidationField.GROUPS);
        }
        if (isBlank(password)) {
            throw ValidationException.missing(UserValidationField.PASSWORD);
        }
    }

    private void assertLength(ValidationField field, String value, int minLength, int maxLength) {
        if (isBlank(value)
            || StringUtils.length(value.trim()) < minLength
            || StringUtils.length(value) > maxLength) {
            throw ValidationException.invalidValue(field);
        }
    }
}
