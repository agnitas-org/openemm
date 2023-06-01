/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.FormTag;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AgnFormTag extends FormTag implements DynamicAttributes {

	private static final long serialVersionUID = 530481172972908744L;
    private static final String DISABLE_CSRF_TOKEN_ATTR = "DISABLE_CSRF_TOKEN_ATTR";

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
    public int doStartTag() throws JspException {
        int result = super.doStartTag();
        appendCsrfDataIfNeeded();

        return result;
    }

    private void appendCsrfDataIfNeeded() throws JspException {
        ServletRequest request = pageContext.getRequest();
        if (Boolean.TRUE.equals(request.getAttribute(DISABLE_CSRF_TOKEN_ATTR))) {
            request.removeAttribute(DISABLE_CSRF_TOKEN_ATTR);
        } else {
            CsrfToken token = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                String csrfToken = "<div>\n" +
                        "<input type=\"hidden\" " + "name=\"" + token.getParameterName() + "\" value=\"" + token.getToken() + "\" " +
                        "/>\n" +
                        "</div>";

                TagUtils.getInstance().write(this.pageContext, csrfToken);
            }
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
