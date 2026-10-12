/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.bulk;

import com.agnitas.service.ImportMethod;

public enum ImportMode {

    INSERT("insert"),
    UPDATE("update"),
    UPSERT("upsert");

    private final String value;

    ImportMode(String value) {
        this.value = value;
    }

    public ImportMethod toImportMethod() {
        return switch (this) {
            case INSERT -> ImportMethod.InsertOnly;
            case UPDATE -> ImportMethod.UpdateOnly;
            case UPSERT -> ImportMethod.UpdateAndInsert;
        };
    }

    public String getValue() {
        return value;
    }

    @SuppressWarnings("unused") // used by EnumConverterFactory via reflection (finds "parse" method dynamically)
    public static ImportMode parse(String value) {
        for (ImportMode mode : values()) {
            if (mode.value.equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid import mode: " + value + ". Must be one of: insert, update, upsert");
    }
}
