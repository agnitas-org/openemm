/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.mailing.dto;

import java.util.Arrays;

import com.agnitas.util.importvalues.MailType;

public enum MailFormat {
    HTML_OFFLINE(MailType.HTML_OFFLINE),
    HTML(MailType.HTML),
    TEXT(MailType.TEXT);

    private final MailType coreConst;

    MailFormat(MailType coreConst) {
        this.coreConst = coreConst;
    }

    public static MailFormat from(int code) {
        return Arrays.stream(MailFormat.values())
                .filter(e -> e.coreConst == MailType.getFromInt(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown mailing type with code: " + code));
    }
}
