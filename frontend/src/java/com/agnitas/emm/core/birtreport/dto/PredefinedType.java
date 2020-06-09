/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

import org.apache.commons.lang3.StringUtils;

public enum PredefinedType {
    PREDEFINED_LAST_ONE("last_1", 1),
    PREDEFINED_LAST_THREE("last_3", 3),
    PREDEFINED_LAST_FIVE("last_5", 5);
    
    private final String value;
    private final int number;
    
    PredefinedType(String value, int number) {
        this.value = value;
        this.number = number;
    }
    
    public String getValue() {
        return value;
    }
    public int getNumber() {
        return number;
    }
    
    public static int getLastNumberValue(String value) {
        for(PredefinedType period: values()) {
            if (StringUtils.equals(period.getValue(), value)) {
                return period.getNumber();
            }
        }
        
        return 0;
    }
}
