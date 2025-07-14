/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.spring;

import java.util.Collections;
import java.util.Map;

import jakarta.servlet.jsp.JspException;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.tags.form.SelectTag;
import org.springframework.web.servlet.tags.form.TagWriter;

public class SpringSelectTag extends SelectTag {
    private static final long serialVersionUID = 1859170298435917355L;

    private boolean dynamicTags;

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

    @Override
    protected String getCssClass() {
        String cssClass = super.getCssClass();
        if (!dynamicTags) {
            return cssClass;
        }

        return StringUtils.defaultIfBlank(cssClass, "") + " dynamic-tags";
    }

    @Override
    protected Object getItems() {
        Object items = super.getItems();
        if (!dynamicTags || items != null) {
            return items;
        }

        try {
            BindStatus bindStatus = getBindStatus();
            Object actualValue = bindStatus.getActualValue();

            if (String.class.equals(bindStatus.getValueType())) {
                if (actualValue instanceof String str) {
                    return AgnUtils.splitAndTrimList(str).stream()
                            .filter(StringUtils::isNotBlank)
                            .toList();
                } else {
                    return Collections.emptyList();
                }
            }

            return actualValue;
        } catch (JspException e) {
            logger.error("Can't get actual value of dynamic tag select!", e);
            return null;
        }
    }

    public boolean isDynamicTags() {
        return dynamicTags;
    }

    public void setDynamicTags(boolean dynamicTags) {
        this.dynamicTags = dynamicTags;

        if (dynamicTags) {
            setMultiple(true);
        }
    }
}
