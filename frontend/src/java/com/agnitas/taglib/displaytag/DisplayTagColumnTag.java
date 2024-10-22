/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.displaytag;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import jakarta.servlet.jsp.tagext.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.displaytag.model.HeaderCell;
import org.displaytag.tags.ColumnTag;
import org.displaytag.tags.TableTag;
import org.displaytag.util.HtmlAttributeMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayTagColumnTag extends ColumnTag implements DynamicAttributes {
    private static final long serialVersionUID = 7366291919931480794L;
    
	private Map<String, Object> dynamicAttributes;

    @Override
    public void setDynamicAttribute(String uri, String localName, Object value) {
        if (this.dynamicAttributes == null) {
            this.dynamicAttributes = new HashMap<>();
        }

        this.dynamicAttributes.put(localName, value);
    }

    @Override
    public int doEndTag() throws JspException {
        int resultCode = super.doEndTag();

        if (MapUtils.isNotEmpty(dynamicAttributes)) {
            Tag tag = findAncestorWithClass(this, TableTag.class);

            if (tag instanceof DisplayTagTableTag) {
                DisplayTagTableTag tableTag = (DisplayTagTableTag) tag;
                if (tableTag.isFirstIteration()) {
                    List<?> headerCellList = tableTag.getTableModel().getHeaderCellList();

                    if (CollectionUtils.isNotEmpty(headerCellList)) {
                        Object cell = headerCellList.get(headerCellList.size() - 1);

                        if (cell instanceof HeaderCell) {
                            HtmlAttributeMap headerAttributes = ((HeaderCell) cell).getHeaderAttributes();
                            dynamicAttributes.forEach(headerAttributes::putIfAbsent);
                        }
                    }
                }
            }
        }

        return resultCode;
    }
}
