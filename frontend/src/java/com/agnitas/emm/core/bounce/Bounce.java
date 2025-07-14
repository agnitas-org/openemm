/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce;

import com.agnitas.beans.IntEnum;
import com.agnitas.messages.I18nString;

public enum Bounce implements IntEnum {
    // soft bounces
    UNDELIVERABLE(33),
    OTHER_SOFT_BOUNCE(400),
    RECIPIENT(410),
    MAILBOX(420),
    MAIL_SERVER(430),
    IRREGULAR(500),

    // hard bounces
    OTHER_HARD_BOUNCE(510),
    UNKNOWN_ADDRESS(511),
    UNKNOWN_DOMAIN_NAME(512);

    private final int code;

    Bounce(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public int getId() {
        return getCode();
    }

    public String getDetailMsg(String lang) {
        if (this == UNDELIVERABLE) {
            return I18nString.getLocaleString("report.softbounces.undeliverable", lang);
        }
        return I18nString.getLocaleString("bounces.detail.%s".formatted(code), lang);
    }

    public static Bounce from(int code) {
        return IntEnum.fromId(Bounce.class, code);
    }
}
