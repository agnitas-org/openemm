/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.entity;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Stream;

public enum AdminTestMarkPlacementOption {

    NONE("none", "default.none"),
    TO_ADDRESS("to", "company.mark.admin.test.to"),
    SUBJECT("subject", "mailing.Subject"),
    BOTH(SUBJECT.storageValue + ", " + TO_ADDRESS.storageValue, "company.mark.admin.test.to.subject");

    private final String storageValue;
    private final String messageKey;

    AdminTestMarkPlacementOption(String storageValue, String messageKey) {
        this.storageValue = storageValue;
        this.messageKey = messageKey;
    }

    public String getStorageValue() {
        return storageValue;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static AdminTestMarkPlacementOption find(String storageValue) {
        return Stream.of(AdminTestMarkPlacementOption.values())
                .filter(o -> o.getStorageValue().equals(storageValue))
                .findAny()
                .orElseGet(() -> smartFind(storageValue));
    }

    private static AdminTestMarkPlacementOption smartFind(String storageValue) {
        List<String> values = AgnUtils.splitAndTrimList(StringUtils.defaultString(storageValue));

        if (values.contains(SUBJECT.getStorageValue()) && values.contains(TO_ADDRESS.getStorageValue())) {
            return BOTH;
        }

        if (values.contains(SUBJECT.getStorageValue())) {
            return SUBJECT;
        }

        if (values.contains(TO_ADDRESS.getStorageValue())) {
            return TO_ADDRESS;
        }

        return NONE;
    }
}
