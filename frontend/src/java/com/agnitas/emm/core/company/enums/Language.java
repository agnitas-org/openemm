/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.enums;

public enum Language {
    NONE("none", "default.none"),
    DE("de_DE", "settings.German"),
    US("en_US", "settings.English"),
    FR("fr_FR", "settings.French"),
    ES("es_ES", "settings.Spanish"),
    PT("pt_PT", "settings.Portuguese"),
    NL("nl_NL", "settings.Dutch"),
    IT("it_IT", "settings.Italian");

    private final String key;
    private final String messageKey;

    Language(String key, String messageKey) {
        this.key = key;
        this.messageKey = messageKey;
    }

    public String getKey() {
        return key;
    }

    public String getMessageKey() {
        return messageKey;
    }
    
}
