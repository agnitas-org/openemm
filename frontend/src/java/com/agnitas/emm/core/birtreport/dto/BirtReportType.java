/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

public enum BirtReportType {
    
    TYPE_DAILY(0),
    TYPE_WEEKLY(1),
    TYPE_BIWEEKLY(2),
    
    TYPE_MONTHLY_FIRST(3),
    TYPE_MONTHLY_15TH(4),
    TYPE_MONTHLY_LAST(5),
    
    TYPE_AFTER_MAILING_24HOURS(6),
    TYPE_AFTER_MAILING_48HOURS(7),
    TYPE_AFTER_MAILING_WEEK(8);
    
    private final int key;
    
    BirtReportType(int key) {
        this.key = key;
    }
    
    public int getKey() {
        return key;
    }
    
    public static BirtReportType getTypeByCode(int code) {
        for(BirtReportType reportType: BirtReportType.values()) {
            if(reportType.getKey() == code) {
                return reportType;
            }
        }
        return null;
    }
}
