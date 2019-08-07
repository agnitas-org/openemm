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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.DynamicAttributes;

import org.apache.struts.taglib.TagUtils;
import org.apache.struts.taglib.html.Constants;
import org.apache.struts.taglib.html.OptionTag;
import org.apache.struts.taglib.html.SelectTag;

public class AgnOptionTag extends OptionTag implements DynamicAttributes {
	private static final long serialVersionUID = -1498392870162934785L;
	
	private Map<String, Object> tagattr = new HashMap<>();

    @Override
    public void setDynamicAttribute(String uri,String name,Object value) {
        tagattr.put(name,value);
    }

    protected void prepareOtherAttributes(StringBuffer results) {
    	for (Entry<String, Object> entry : tagattr.entrySet()) {
            prepareAttribute(results, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Generate an HTML %lt;option&gt; element.
     *
     * @throws javax.servlet.jsp.JspException
     * @since Struts 1.1
     */
    @Override
    protected String renderOptionElement()
            throws JspException {
        StringBuffer results = new StringBuffer("<option value=\"");

        if (filter) {
            results.append(TagUtils.getInstance().filter(this.value));
        }
        else {
            results.append(this.value);
        }
        results.append("\"");

        if (disabled) {
            results.append(" disabled=\"disabled\"");
        }

        if (this.selectTag().isMatched(this.value)) {
            results.append(" selected=\"selected\"");
        }

        if (getStyleClass() != null) {
            results.append(" style=\"");
            results.append(getStyleClass());
            results.append("\"");
        }

        if (styleId != null) {
            results.append(" id=\"");
            results.append(styleId);
            results.append("\"");
        }

        if (getStyleClass() != null) {
            results.append(" class=\"");
            results.append(getStyleClass());
            results.append("\"");
        }

        if (getDir() != null) {
            results.append(" dir=\"");
            results.append(getDir());
            results.append("\"");
        }

        if (getLang() != null) {
            results.append(" lang=\"");
            results.append(getLang());
            results.append("\"");
        }

        prepareOtherAttributes(results);

        results.append(">");

        results.append(text());

        results.append("</option>");

        return results.toString();
    }

    /**
     * Acquire the select tag we are associated with.
     *
     * @throws JspException
     */
    private SelectTag selectTag()
            throws JspException {
        SelectTag selectTag =
                (SelectTag) pageContext.getAttribute(Constants.SELECT_KEY);

        if (selectTag == null) {
            JspException e =
                    new JspException(messages.getMessage("optionTag.select"));

            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        return selectTag;
    }

    /**
     * Prepares an attribute if the value is not null, appending it to the the
     * given StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void  prepareAttribute(StringBuffer handlers, String name,
                                    Object value) {
        if (value != null) {
            handlers.append(" ");
            handlers.append(name);
            handlers.append("=\"");
            handlers.append(value);
            handlers.append("\"");
        }
    }
}
