/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding;

import java.util.Objects;

import com.agnitas.beans.BindingEntry;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.validator.BindingValidationField;

public record PatchBindingCommand(
        GetBindingQuery getQuery,
        PatchField<UserStatus> userStatus,
        PatchField<BindingEntry.UserType> userType,
        PatchField<String> userRemark,
        PatchField<String> referrer,
        PatchField<Integer> entryMailingId,
        PatchField<Integer> exitMailingId
) {
    public PatchBindingCommand {
        userStatus.check(Objects::nonNull, BindingValidationField.USER_STATUS);
        entryMailingId.check(id -> id == null || id > 0, BindingValidationField.ENTRY_MAILING_ID);
        exitMailingId.check(id -> id == null || id > 0, BindingValidationField.EXIT_MAILING_ID);
    }
}
