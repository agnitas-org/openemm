/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.send;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MailingStopResponse(
        Status status,
        String message
) {
    public enum Status {
        STOPPED, DEACTIVATED, FAILED
    }

    public static MailingStopResponse stopped() {
        return new MailingStopResponse(
                Status.STOPPED,
                "Mailing stopped"
        );
    }

    public static MailingStopResponse deactivated() {
        return new MailingStopResponse(
                Status.DEACTIVATED,
                "Mailing deactivated"
        );
    }

    public static MailingStopResponse failed(String message) {
        return new MailingStopResponse(
                Status.FAILED,
                message
        );
    }
}
