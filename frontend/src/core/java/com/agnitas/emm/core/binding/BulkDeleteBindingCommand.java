/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.binding;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.validator.ValidationException;
import org.apache.commons.collections4.CollectionUtils;

public record BulkDeleteBindingCommand(
        int companyId,
        List<Integer> recipientIds,
        Set<Integer> mailinglistIds,
        Set<MediaTypes> mediatypes
) {
    public BulkDeleteBindingCommand {
        recipientIds = List.copyOf(emptyIfNull(recipientIds));
        mailinglistIds = Set.copyOf(emptyIfNull(mailinglistIds));
        mediatypes = Set.copyOf(emptyIfNull(mediatypes));

        if (companyId <= 0) {
            throw ValidationException.invalidPath("companyId");
        }
        if (CollectionUtils.isEmpty(mailinglistIds)) {
            throwMailinglistIdsValidationException("At least one ID must be provided.");
        }
        if (CollectionUtils.isEmpty(recipientIds)) {
            throwRecipientIdsValidationException("At least one ID must be provided.");
        }
        if (recipientIds.size() != new HashSet<>(recipientIds).size()) {
            throwRecipientIdsValidationException("Duplicate IDs are not allowed.");
        }
        if (mailinglistIds.stream().anyMatch(id -> id <= 0)) {
            throwMailinglistIdsValidationException("IDs must be positive.");
        }
        if (recipientIds.stream().anyMatch(id -> id <= 0)) {
            throwRecipientIdsValidationException("IDs must be positive.");
        }
    }

    private static void throwRecipientIdsValidationException(String msg) {
        throw ValidationException.exact("recipientIds. " + msg);
    }

    private static void throwMailinglistIdsValidationException(String msg) {
        throw ValidationException.exact("mailinglistIds. " + msg);
    }
}
