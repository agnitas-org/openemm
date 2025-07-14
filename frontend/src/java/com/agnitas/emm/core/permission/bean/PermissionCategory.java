/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.permission.bean;

public enum PermissionCategory {

    GENERAL("General", "home"),
    MAILING("Mailing", "envelope"),
    RECIPIENT("Subscriber-Editor", "user"),
    IMPORT_EXPORT("ImportExport", "exchange-alt"),
    TARGET_GROUPS("Target-Groups", "users"),
    STATISTICS("Statistics", "chart-bar"),
    FORMS("Forms", "list-alt"),
    ACTIONS("Actions", "arrow-alt-circle-right"),
    ADMINISTRATION("Administration", "cog"),
    SYSTEM("System", "tools"),
    PREMIUM("Premium", "plus-square"),
    NEGATIVE_PERMISSIONS("NegativePermissions", "minus-square");

    private String dbName;
    private String iconCode;

    PermissionCategory(String dbName, String iconCode) {
        this.dbName = dbName;
        this.iconCode = iconCode;
    }

    public String getDbName() {
        return dbName;
    }

    public String getIconCode() {
        return iconCode;
    }

    public static PermissionCategory from(String dbName) {
        for (PermissionCategory enumValue : PermissionCategory.values()) {
            if (enumValue.getDbName().equals(dbName)) {
                return enumValue;
            }
        }
        throw new IllegalArgumentException("No PermissionCategory enum constant with dbName: " + dbName);
    }
}
