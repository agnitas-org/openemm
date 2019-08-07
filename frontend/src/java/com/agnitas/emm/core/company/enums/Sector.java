/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.enums;

import java.util.Arrays;

public enum Sector {

    NONE(0, "none"),
    AGENCIES(1, "company.sector.1"),
    COMMUNITIES(2, "company.sector.2"),
    TOURISM(3, "company.sector.3"),
    FINANCE(4, "company.sector.4"),
    IT(5, "company.sector.5"),
    RETAIL(6, "company.sector.6"),
    MANUFACTURING_INDUSTRY(7, "company.sector.7"),
    CONSUMER_GOODS(8, "company.sector.8"),
    PUBLISHER(9, "company.sector.9"),
    NON_PROFIT(10, "company.sector.10"),
    EDUCATION(11, "company.sector.11");

    private int id;
    private String messageKey;

    Sector(int id, String messageKey) {
        this.id = id;
        this.messageKey = messageKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public static Sector getById(int id) {
        return Arrays.stream(values()).filter(sector -> sector.id == id)
                .findFirst()
                .orElse(null);
    }
}
