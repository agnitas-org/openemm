/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.taglib;

import jakarta.servlet.jsp.tagext.TagData;
import jakarta.servlet.jsp.tagext.TagExtraInfo;
import jakarta.servlet.jsp.tagext.VariableInfo;

public class ShowNavigationTagExtraInfo extends TagExtraInfo {
    
    /**
     * Getter for property variableInfo.
     *
     * @return Value of property variableInfo.
     */
    @Override
    public VariableInfo[] getVariableInfo(TagData data) {
        String prefix=(String)data.getAttribute("prefix");
        
        if(prefix==null) {
            prefix = "";
        }
        
        return new VariableInfo[]
        {
            new VariableInfo(prefix+"_navigation_switch", "String", true, VariableInfo.NESTED),
            new VariableInfo(prefix+"_navigation_isHighlightKey", "Boolean", true, VariableInfo.NESTED),
            new VariableInfo(prefix+"_navigation_token", "String", true, VariableInfo.NESTED),
            new VariableInfo(prefix+"_navigation_href", "String", true, VariableInfo.NESTED),
            new VariableInfo(prefix+"_navigation_navMsg", "String", true, VariableInfo.NESTED),
            new VariableInfo(prefix+"_navigation_index", "Integer", true, VariableInfo.NESTED),
            new VariableInfo(prefix+"_navigation_conditionSatisfied", "Boolean", true, VariableInfo.NESTED)
        };
    }
}
