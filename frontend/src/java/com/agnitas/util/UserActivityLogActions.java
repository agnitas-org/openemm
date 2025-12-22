/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

public enum UserActivityLogActions {

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
    GRANT("UserActivitylog.Action.Grant", "grant"),
    APPROVAL("default.enabling", "approval"),
    AI_CHAT("GWUA.aiSupportChat", "AI chat");

    private final String messageKey;
    // action type for log file
    private final String[] localValues;

    // first parameter of constant is publicValue, second is localValue
    UserActivityLogActions(String messageKey, String... localValues) {
        this.messageKey = messageKey;
        if (localValues == null || localValues.length < 1){
            throw new IllegalArgumentException("UserActivityLogActions should have at least one localValue");
        }
        this.localValues = localValues;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getLocalValue() {
        return localValues[0];
    }

    public String[] getLocalValues() {
        return localValues;
    }

}
