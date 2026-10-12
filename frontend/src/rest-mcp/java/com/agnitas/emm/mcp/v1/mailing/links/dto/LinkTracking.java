/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.links.dto;

import java.util.stream.Stream;

public enum LinkTracking {

    NOT_TRACKABLE(0),
    TEXT(1),
    HTML(2),
    TEXT_AND_HTML(3);

    private final int code;

    LinkTracking(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static LinkTracking from(int code) {
        return Stream.of(values())
                .filter(tracking -> tracking.code == code)
                .findFirst()
                .orElse(TEXT_AND_HTML);
    }

}
