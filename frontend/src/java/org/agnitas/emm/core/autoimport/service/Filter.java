/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.service;

import org.apache.commons.lang3.StringUtils;

public enum Filter {

    IS_CAMPAIGN_DRIVEN("isCampaignManager"), IS_EMC("isEmc");
    
    private String name;
    
    Filter(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public static Filter getFilter(String name) {
        for(Filter filter: Filter.values()) {
            if(StringUtils.equalsIgnoreCase(filter.getName(), name)) {
                return filter;
            }
        }
        return null;
    }
}
