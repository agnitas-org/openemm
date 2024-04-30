/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.jsp.tagext.DynamicAttributes;

import org.apache.struts.taglib.html.BaseFieldTag;

public class AgnCustomInputTag extends BaseFieldTag implements DynamicAttributes {
	private static final long serialVersionUID = 2395842672418585769L;
	
	private Map<String, Object> attributes = new HashMap<>();

    public AgnCustomInputTag(String type) {
        this.type = type;
        this.doReadonly = true;
    }

    @Override
    public void setDynamicAttribute(String uri, String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    protected void prepareOtherAttributes(StringBuffer results) {
    	for (Entry<String, Object> entry : attributes.entrySet()) {
            prepareAttribute(results, entry.getKey(), entry.getValue().toString());
        }
    }
}
