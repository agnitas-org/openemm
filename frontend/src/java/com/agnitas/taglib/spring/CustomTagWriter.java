/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.spring;

import java.io.Writer;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.springframework.web.servlet.tags.form.TagWriter;

public class CustomTagWriter extends TagWriter {
    public CustomTagWriter(PageContext pageContext) {
        super(pageContext);
    }

    public CustomTagWriter(Writer writer) {
        super(writer);
    }

    @Override
    public void writeOptionalAttributeValue(String attributeName, String attributeValue) throws JspException {
        // Preserve custom attributes anyway.
        this.writeAttribute(attributeName, attributeValue);
    }
}
