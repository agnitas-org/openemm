/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.send;

import java.util.Collection;

import com.agnitas.messages.Message;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MailingStartResponse(
        Status status,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        Collection<Message> warnings
) {
    public enum Status {
        STARTED, ACTIVATED,
    }

    public static MailingStartResponse activated(Collection<Message> warnings) {
        return new MailingStartResponse(
                Status.ACTIVATED,
                "Mailing activated",
                warnings
        );
    }

    public static MailingStartResponse started(Collection<Message> warnings) {
        return new MailingStartResponse(
                Status.STARTED,
                "Mailing started",
                warnings
        );
    }
}
