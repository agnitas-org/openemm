/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util;

public enum UserActivityLogActions {
    ANY("UserActivitylog.Any_Action", "any"),
    CREATE("UserActivitylog.Action.Create", "create"),
    EDIT("UserActivitylog.Action.Edit", "edit"),
    DELETE("UserActivitylog.Action.Delete", "delete"),
    VIEW("default.View", "view"),
    DO("UserActivitylog.Action.Do", "do"),
    UPLOAD_DOWNLOAD("UserActivitylog.Action.UploadDownload", "download", "upload"),
    SEND("UserActivitylog.Action.Send", "send"),
    BLACKLIST("UserActivitylog.Action.Blacklist", "blacklist"),
    LOGIN_LOGOUT("UserActivitylog.login.logout", "login_logout"),
    ANY_WITHOUT_LOGIN("UserActivitylog.Any_Action_without_login", "all"),
    SERVER_CHANGE("UserActivitylog.Server_change", "server_change"),
    IMPORT("import.csv_upload", "import"),
    EXPORT("Export", "export"),
    GRANT("UserActivitylog.Action.Grant", "grant");

    // message key in resource bundle to display value on pages
    private String publicValue;
    
    // action type for log file
    private String localValues[];

    // first parameter of constant is publicValue, second is localValue
    UserActivityLogActions(String publicValue, String... localValues) {
        this.publicValue = publicValue;
        if(localValues == null || localValues.length < 1){
            throw new RuntimeException("UserActivityLogActions should have at least one localValue");
        }
        this.localValues=localValues;
    }

    public String getPublicValue() {
        return publicValue;
    }

    public String getLocalValue() {
        return localValues[0];
    }

    public String[] getLocalValues() {
        return localValues;
    }

    // Position of constant in Enum
    public int getIntValue() {
        return ordinal();
    }

    public static String getPublicValue(int intValue) {
        if (intValue < UserActivityLogActions.values().length) {
            return UserActivityLogActions.values()[intValue].getPublicValue();
        } else {
            return null;
        }
    }

    public static String getLocalValue(int intValue) {
        if (intValue < UserActivityLogActions.values().length) {
            return UserActivityLogActions.values()[intValue].getLocalValue();
        } else {
            return null;
        }
    }

    public static String[] getLocalValues(int intValue) {
        if (intValue < UserActivityLogActions.values().length) {
            return UserActivityLogActions.values()[intValue].getLocalValues();
        } else {
            return null;
        }
    }
}
