/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.table;

import static com.agnitas.taglib.table.util.TableTagUtils.translateMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.taglib.table.util.TableTagUtils;
import com.agnitas.taglib.table.writer.TableTagHtmlWriter;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import jakarta.servlet.jsp.tagext.DynamicAttributes;
import org.apache.commons.lang3.StringUtils;
import org.apache.taglibs.standard.functions.Functions;

public class ColumnTag extends BodyTagSupport implements DynamicAttributes {

    private String title;
    private String cssClass;
    private String headerClass;
    private String property;
    private String sortProperty;
    private String titleKey;
    private boolean sortable;

    private TableTag tableTag;
    private Map<String, String> dynamicAttributes;

    @Override
    public void setDynamicAttribute(String uri, String localName, Object value) {
        if (this.dynamicAttributes == null) {
            this.dynamicAttributes = new HashMap<>();
        }

        this.dynamicAttributes.put(localName, value.toString());
    }

    @Override
    public void release() {
        super.release();

        title = null;
        cssClass = null;
        headerClass = null;
        property = null;
        sortProperty = null;
        titleKey = null;
        sortable = false;
        tableTag = null;
    }

    @Override
    public int doStartTag() throws JspException {
        tableTag = (TableTag) findAncestorWithClass(this, TableTag.class);
        if (tableTag == null) {
            throw new IllegalStateException("Column tag can't be used without a TableTag!");
        }

        if (!tableTag.isHeaderAdded()) {
            return SKIP_BODY;
        }

        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() {
        if (!tableTag.isHeaderAdded()) {
            appendHeaderCell();
            return EVAL_PAGE;
        }

        TableTagHtmlWriter writer = getWriter();
        writer.openDataCell(Map.of("class", StringUtils.defaultString(cssClass)));

        if (this.bodyContent == null) {
            if (StringUtils.isNotBlank(property)) {
                String propertyValue = getPropertyValue();

                if (StringUtils.isNotBlank(propertyValue)) {
                    writer.write("span", Functions.escapeXml(propertyValue));
                }
            }
        } else {
            writer.write(this.bodyContent.getString());
        }

        writer.closeDataCell();

        return EVAL_PAGE;
    }

    private String getPropertyValue() {
        Object simpleProperty = TableTagUtils.getPropertyValue(pageContext.getAttribute(tableTag.getVar()), property);
        if (simpleProperty instanceof Date date) {
            return getFormattedDate(date);
        }

        return simpleProperty == null ? "" : simpleProperty.toString();
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat dateFormat = Optional.ofNullable(AgnUtils.getAdmin(pageContext))
                .map(Admin::getDateTimeFormat)
                .orElse(new SimpleDateFormat("yyyy-MM-dd hh:mm"));

        return dateFormat.format(date);
    }

    private void appendHeaderCell() {
        String headerCssClass = StringUtils.defaultString(headerClass);
        if (sortable) {
            headerCssClass += " sortable";
        }

        String propertyToSort = StringUtils.defaultIfEmpty(sortProperty, property);
        if (sortable && tableTag.isSortedBy(propertyToSort)) {
            headerCssClass += " sorted " + tableTag.getSortOrderClass();
        }

        Map<String, String> attrs = new HashMap<>(Map.of("class", headerCssClass));
        if (dynamicAttributes != null) {
            attrs.putAll(dynamicAttributes);
        }

        TableTagHtmlWriter writer = getWriter();
        writer.openHeaderCell(attrs);

        if (sortable) {
            String href = TableTagUtils.buildUrl(tableTag.getRequestUri(), Map.of(
                    "sort", propertyToSort,
                    "dir", tableTag.getNextSortDir(propertyToSort).getId()
            ));
            writer.openTag("a", Map.of("data-table-sort", "", "href", href));
        }
        if (title != null) {
            writer.write(title);
        } else if (StringUtils.isNotBlank(titleKey)) {
            writer.write(translateMessage(titleKey, (HttpServletRequest) pageContext.getRequest()));
        }

        if (sortable) {
            writer.closeTag("a");
        }

        writer.closeHeaderCell();
    }

    private TableTagHtmlWriter getWriter() {
        return tableTag.getHtmlWriter();
    }

    // region Getters & Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getHeaderClass() {
        return headerClass;
    }

    public void setHeaderClass(String headerClass) {
        this.headerClass = headerClass;
    }

    public String getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(String sortProperty) {
        this.sortProperty = sortProperty;
    }

    public String getTitleKey() {
        return titleKey;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    // endregion
}
