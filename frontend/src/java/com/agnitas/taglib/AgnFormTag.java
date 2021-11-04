/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.jsp.tagext.DynamicAttributes;

import org.apache.struts.taglib.html.FormTag;

public class AgnFormTag extends FormTag implements DynamicAttributes {
	private static final long serialVersionUID = 530481172972908744L;
	
	private Map<String, Object> tagattr = new HashMap<>();

	@Override
    public void setDynamicAttribute(String uri, String name, Object value) {
        tagattr.put(name, value);
    }

    @Override
    protected void renderOtherAttributes(StringBuffer results) {
    	for (Entry<String, Object> entry : tagattr.entrySet()) {
            renderAttribute(results, entry.getKey(), (String) entry.getValue());
        }
    }

    @Override
    protected String renderFocusJavascript() {
        return  lineEnd +
                "<script type=\"text/javascript\">" + lineEnd +
                "  (function() {" + lineEnd +
                "     var focusControl = document.forms[\"" + this.beanName + "\"].elements[\"" + this.focus + "\"];" + lineEnd + lineEnd +
                "     if (focusControl.type != \"hidden\" && !focusControl.disabled && focusControl.style.display != \"none\") {" + lineEnd +
                "        focusControl" + (this.focusIndex == null ? "" : "[" + this.focusIndex + "]") + ".focus();" + lineEnd +
                "     }" + lineEnd +
                "  })();" + lineEnd +
                "</script>" + lineEnd;
    }
}
