/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.mailinglist.dto;

import static org.apache.commons.lang3.StringUtils.length;

import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.mailinglist.validation.MailinglistConstraints;
import com.agnitas.emm.core.validator.MailinglistValidationField;
import com.agnitas.emm.core.validator.ValidationException;

public record PatchMailinglistCommand(
        int companyId,
        int mailinglistId,
        PatchField<String> name,
        PatchField<String> description
) {
    public PatchMailinglistCommand {
        if (companyId <= 0) {
            throw ValidationException.exact("Invalid companyId");
        }
        if (mailinglistId <= 0) {
            throw ValidationException.exact("Invalid mailinglistId");
        }
        name.check(
                n -> length(n) >= MailinglistConstraints.NAME_MIN_LENGTH,
                MailinglistValidationField.NAME
        );
        description.check(
                n -> length(n) < MailinglistConstraints.DESCRIPTION_MAX_LENGTH,
                MailinglistValidationField.DESCRIPTION
        );
    }
}
