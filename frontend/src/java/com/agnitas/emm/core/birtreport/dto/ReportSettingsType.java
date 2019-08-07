/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

public enum ReportSettingsType {

    COMPARISON(1, "mailing.comparison"),
    MAILING(2, "mailing.statistics"),
    RECIPIENT(3, "statistic.Recipient");
    
    private final int key;
    private final String typeMsgKey;
    
    ReportSettingsType(int key, String typeMsgKey) {
        this.key = key;
        this.typeMsgKey = typeMsgKey;
    }
    
    public int getKey() {
        return key;
    }
    
    public String getTypeMsgKey() {
        return typeMsgKey;
    }
    
    public static ReportSettingsType getTypeByCode(int code) {
        for(ReportSettingsType settingsType: ReportSettingsType.values()) {
            if(settingsType.getKey() == code) {
                return settingsType;
            }
        }
        return COMPARISON;
    }
}
