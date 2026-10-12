/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.bean;

import java.util.stream.Stream;

public enum ReportFormat {

    PDF(0),
    CSV(1);

    private final int code;

    ReportFormat(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ReportFormat getByCode(int code) {
        return Stream.of(values())
                .filter(f -> f.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown report format: " + code));
    }

}
