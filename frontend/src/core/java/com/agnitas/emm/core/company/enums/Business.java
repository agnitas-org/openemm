/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.enums;

import java.util.Arrays;

public enum Business {

    NONE(0, "none"),
    B2B(1, "company.business.1"),
    B2C(2, "company.business.2");

    private final int id;
    private final String messageKey;

    Business(int id, String messageKey) {
        this.id = id;
        this.messageKey = messageKey;
    }

    public int getId() {
        return id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static Business getById(int id) {
        return Arrays.stream(values()).filter(sector -> sector.id == id)
                .findFirst()
                .orElse(null);
    }
}
