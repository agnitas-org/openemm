/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.target.dto;

public enum TargetInclude {
    RECIPIENT_COUNT("recipientCount");

    private final String value;

    TargetInclude(String value) { this.value = value; }

    public String value() { return value; }

    public static TargetInclude parse(String s) {
        for (TargetInclude t : values()) {
            if (t.value.equals(s)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown include: " + s);
    }
}
