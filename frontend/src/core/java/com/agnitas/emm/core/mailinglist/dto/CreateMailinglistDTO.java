/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailinglist.dto;

import com.agnitas.emm.core.mailinglist.validation.MailinglistConstraints;
import com.agnitas.emm.core.validator.MailinglistValidationField;
import com.agnitas.emm.core.validator.ValidationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;

public record CreateMailinglistDTO(
        @NotBlank(message = "Name is required")
        @Size(min = MailinglistConstraints.NAME_MIN_LENGTH)
        String name,

        @Size(max = MailinglistConstraints.DESCRIPTION_MAX_LENGTH)
        String description
) {
    public CreateMailinglistDTO {
        if (StringUtils.isBlank(name) || StringUtils.length(name) < MailinglistConstraints.NAME_MIN_LENGTH) {
            throw ValidationException.invalidValue(MailinglistValidationField.NAME);
        }

        if (StringUtils.length(description) > MailinglistConstraints.DESCRIPTION_MAX_LENGTH) {
            throw ValidationException.invalidValue(MailinglistValidationField.DESCRIPTION);
        }
    }
}
