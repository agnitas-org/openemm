/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.spring;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;

import org.apache.taglibs.standard.util.UrlUtil;
import org.springframework.web.servlet.tags.form.FormTag;
import org.springframework.web.servlet.tags.form.TagWriter;

public class SpringFormTag extends FormTag {
    private static final long serialVersionUID = 9211089319170441700L;

    @Override
    protected TagWriter createTagWriter() {
        return new CustomTagWriter(pageContext);
    }

    @Override
    protected String resolveAction() throws JspException {
        String action = super.resolveAction();

        if (UrlUtil.isAbsoluteUrl(action)) {
            return action;
        } else {
            HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
            return response.encodeURL(action);
        }
    }
}
