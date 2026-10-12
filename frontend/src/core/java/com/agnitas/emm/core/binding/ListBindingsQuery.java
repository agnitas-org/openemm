/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.util.AgnUtils;

public record ListBindingsQuery(
        Set<Integer> recipientIds,
        Set<String> emails,
        Set<Integer> mailinglistIds,
        Set<MediaTypes> mediatypes,
        Set<UserStatus> userStatuses,
        String date,
        int page,
        int size
) {
    private static final Pattern DATE_WITH_WILDCARDS = Pattern.compile("^[\\d_]{4}-[\\d_]{2}-[\\d_]{2}$");

    public ListBindingsQuery {
        recipientIds = Set.copyOf(emptyIfNull(recipientIds));
        emails = emptyIfNull(emails).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
        mailinglistIds = Set.copyOf(emptyIfNull(mailinglistIds));
        mediatypes = Set.copyOf(emptyIfNull(mediatypes));
        userStatuses = Set.copyOf(emptyIfNull(userStatuses));

        if (recipientIds.stream().anyMatch(id -> id <= 0)) {
            throw ValidationException.exact("recipientIds must be positive.");
        }
        String invalidEmails = emails.stream()
                .filter(email -> !AgnUtils.isEmailValid(email))
                .collect(Collectors.joining(","));
        if (!invalidEmails.isEmpty()) {
            throw ValidationException.exact("Invalid emails: [%s]".formatted(invalidEmails));
        }
        if (mailinglistIds.stream().anyMatch(id -> id <= 0)) {
            throw ValidationException.exact("mailinglistIds must be positive.");
        }
        if (date != null && !DATE_WITH_WILDCARDS.matcher(date).matches()) {
            throw ValidationException.exact("Invalid wildcard in date.");
        }
    }
}
