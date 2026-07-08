/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly;

public enum MonthlyStatType {

    RECIPIENT_NUM(0, "report.numberRecipients"),
    OPENERS(1, "statistic.opener"),
    ANONYMOUS_OPENINGS(4, "statistic.openings.anonym"),
    CLICKERS(2, "statistic.clicker"),
    ANONYMOUS_CLICKS(3, "statistic.clicks.anonym");

    // TODO: GWUA-6883: remove when deleting Monthly.rptdesign
    private final int code;
    private final String messageKey;

    MonthlyStatType(int code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    public int getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static MonthlyStatType getDefault() {
        return RECIPIENT_NUM;
    }

    // TODO: GWUA-6883: remove when deleting Monthly.rptdesign
    public static MonthlyStatType get(int statusCode) {
        for (MonthlyStatType type : values()) {
            if (type.code == statusCode) {
                return type;
            }
        }
        return null;
    }
}
