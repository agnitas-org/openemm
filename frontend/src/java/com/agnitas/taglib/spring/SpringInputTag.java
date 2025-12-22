/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.spring;

import java.util.Map;

import jakarta.servlet.jsp.JspException;
import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.TagWriter;

public class SpringInputTag extends InputTag {
    private static final long serialVersionUID = 7945317880344410552L;

    @Override
    protected TagWriter createTagWriter() {
        return new CustomTagWriter(pageContext);
    }

    @Override
    protected void writeOptionalAttributes(TagWriter tagWriter) throws JspException {
        Map<String, Object> dynamicAttributes = getDynamicAttributes();
        if (dynamicAttributes == null) {
            setDynamicAttribute("", "autocomplete", "off");
        } else {
            dynamicAttributes.putIfAbsent("autocomplete", "off");
        }

        super.writeOptionalAttributes(tagWriter);
    }

}
