/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly;

public enum MonthlyStatType {
    RECIPIENT_NUM(0),
    OPENERS(1),
    CLICKERS(2),
    ANONYMOUS_CLICKS(3),
    ANONYMOUS_OPENINGS(4);

    private int code;

    MonthlyStatType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MonthlyStatType get(int statusCode) {
        for (MonthlyStatType type : values()) {
            if (type.code == statusCode) {
                return type;
            }
        }
        return null;
    }
}
