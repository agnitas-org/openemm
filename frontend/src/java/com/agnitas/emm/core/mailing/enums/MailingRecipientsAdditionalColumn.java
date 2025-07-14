/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.enums;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MailingRecipientsAdditionalColumn {

    BOUNCE_TIME("recipient.MailingState2"),
    OPT_OUT_TIME("recipient.status.optout");

    private final String messageKey;

    MailingRecipientsAdditionalColumn(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static Set<String> getColumns() {
        return Stream.of(values())
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}
