/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBindingRequest(
        @NotNull @Positive Integer mailinglistId,
        @NotNull @Positive Integer recipientId,
        @NotNull UserStatus userStatus,
        @NotNull MediaTypes mediatype,

        UserType userType,
        String userRemark,
        String referrer,
        @Positive Integer entryMailingId,
        @Positive Integer exitMailingId,
        Integer triggerId,
        Boolean runActionAsynchronous
) {
}
