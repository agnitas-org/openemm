/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import java.util.Set;

import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ListBindingsRequest(
        Set<@Positive Integer> recipientIds,
        Set<@NotBlank @Email String> emails,
        Set<@Positive Integer> mailinglistIds,
        Set<@NotNull MediaTypes> mediatypes,
        Set<@NotNull UserStatus> userStatuses,

        @Pattern(
                regexp = "^[\\d_]{4}-[\\d_]{2}-[\\d_]{2}$",
                message = "Date must match pattern YYYY-MM-DD with optional '_' wildcards"
        )
        String date
) {
}
