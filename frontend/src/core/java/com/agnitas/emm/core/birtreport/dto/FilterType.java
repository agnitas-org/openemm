/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

import org.apache.commons.lang3.math.NumberUtils;

public enum FilterType {
    FILTER_NO_FILTER(0),
    FILTER_ARCHIVE(1),
    FILTER_MAILINGLIST(2),
    FILTER_MAILING(3),
    FILTER_TARGET(4);
    
    private final int key;
    
    FilterType(int key) {
        this.key = key;
    }
    
    public int getKey() {
        return key;
    }
    
    public static FilterType getFilterTypeByKey(int param) {
        for(FilterType type : values()) {
            if (type.getKey() == param) {
                return type;
            }
        }
		
		return FILTER_NO_FILTER;
    }
    
    public static FilterType getFilterTypeByStringKey(String param) {
        int key = NumberUtils.toInt(param);
        return getFilterTypeByKey(key);
    }
}
