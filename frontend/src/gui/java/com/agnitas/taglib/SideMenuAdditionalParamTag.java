/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.jsp.tagext.TagSupport;

public class SideMenuAdditionalParamTag extends TagSupport {

    private static final long serialVersionUID = -8570540008917904498L;

    private static final String SIDE_MENU_ATTRIBUTE_NAME = "sidemenu_active_additional_params";
    private String value;
    private String name;
    private boolean forSubmenuOnly = true;

    @Override
    public int doStartTag() {
        getMenuAdditionalParams().add(new MenuAdditionalParams(getName(), getValue(), isForSubmenuOnly()));
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        return EVAL_PAGE;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isForSubmenuOnly() {
        return forSubmenuOnly;
    }

    public void setForSubmenuOnly(boolean forSubmenuOnly) {
        this.forSubmenuOnly = forSubmenuOnly;
    }

    private List<MenuAdditionalParams> getMenuAdditionalParams(){
        @SuppressWarnings("unchecked")
		List<MenuAdditionalParams> additionalParams = (List<MenuAdditionalParams>) pageContext.getRequest().getAttribute(SIDE_MENU_ATTRIBUTE_NAME); // suppress warning for this cast
        if(additionalParams == null) {
            additionalParams = new ArrayList<>();
            pageContext.getRequest().setAttribute(SIDE_MENU_ATTRIBUTE_NAME, additionalParams);
        }
        return additionalParams;
    }

    public static class MenuAdditionalParams {
        private final String paramName;
        private final String paramValue;
        private final boolean forSubmenuOnly;

        private MenuAdditionalParams(String paramName, String paramValue, boolean forSubmenuOnly) {
            this.paramName = paramName;
            this.paramValue = paramValue;
            this.forSubmenuOnly = forSubmenuOnly;
        }

        public String getParamName() {
            return paramName;
        }

        public String getParamValue() {
            return paramValue;
        }

        public boolean isForSubmenuOnly() {
            return forSubmenuOnly;
        }
    }
}
