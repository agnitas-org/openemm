/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.blacklist.service.impl;

import com.agnitas.emm.core.blacklist.BlocklistConstraints;
import com.agnitas.emm.core.globalblacklist.forms.validation.BlacklistEmailPatternValidator;
import com.agnitas.emm.core.validator.ValidationException;
import org.apache.commons.lang3.StringUtils;

public record AddBlacklistEntryCommand(
        int adminId,
        int companyId,
        String email,
        String reason
) {
    public AddBlacklistEntryCommand {
        if (adminId <= 0) {
            throw ValidationException.invalidPath("adminId");
        }
        if (companyId <= 0) {
            throw ValidationException.invalidPath("companyId");
        }
        if (StringUtils.isBlank(email) || !BlacklistEmailPatternValidator.validateEmailPattern(email)) {
            throw new ValidationException("error.invalid.email");
        }
        if (StringUtils.length(reason) > BlocklistConstraints.REASON_MAX_LENGTH) {
            throw new ValidationException("error.reason.tooLong", BlocklistConstraints.REASON_MAX_LENGTH);
        }
    }
}
