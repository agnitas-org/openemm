/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

import org.apache.commons.lang3.math.NumberUtils;

public enum PeriodType {
    DATE_RANGE_WEEK(6),
    DATE_RANGE_30DAYS(7),
    DATE_RANGE_CUSTOM(8),
    DATE_RANGE_DAY(9),
    DATE_RANGE_THREE_MONTH(10),
    DATE_RANGE_LAST_MONTH(11);
    
    
    private final int key;
    
    PeriodType(int key) {
        this.key = key;
    }
    
    public static PeriodType getTypeByStringKey(String param) {
        int key = NumberUtils.toInt(param, -1);
        for(PeriodType type : values()) {
            if (type.getKey() == key) {
                return type;
            }
        }
		
		return null;
    }
    
    public static PeriodType getTypeByKey(int typeKey) {
        for(PeriodType type : values()) {
            if (type.getKey() == typeKey) {
                return type;
            }
        }
		
		return null;
    }
    
    public int getKey() {
        return key;
    }
}
