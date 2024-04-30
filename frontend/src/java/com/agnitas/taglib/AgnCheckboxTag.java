/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.DynamicAttributes;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.CheckboxTag;

public class AgnCheckboxTag extends CheckboxTag implements DynamicAttributes {
	private static final long serialVersionUID = 4106499321410709065L;
	
	private Map<String,Object> tagattr = new HashMap<>();

    @Override
    public void setDynamicAttribute(String uri,String name,Object value) {
        tagattr.put(name,value);
    }

    @Override
    protected void prepareOtherAttributes(StringBuffer results) {
        for(Map.Entry<String, Object> entry : tagattr.entrySet()) {
            prepareAttribute(results, entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected boolean isChecked() throws JspException {
        Object result = TagUtils.getInstance().lookup(this.pageContext, this.name, this.property, null);

        if (result instanceof String[]) {
            return ArrayUtils.contains((String[]) result, this.value);
        }

        if (result == null) {
            result = "";
        }

        String checked = result.toString();
        return checked.equalsIgnoreCase(this.value) || checked.equalsIgnoreCase("true") || checked.equalsIgnoreCase("yes") || checked.equalsIgnoreCase("on");
    }
}
