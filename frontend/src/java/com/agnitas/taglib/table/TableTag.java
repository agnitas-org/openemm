/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.table;

import static com.agnitas.taglib.table.util.TableTagUtils.calculateTotalPages;
import static com.agnitas.taglib.table.util.TableTagUtils.translateMessage;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.web.servlet.support.RequestContextUtils.getLocale;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.agnitas.taglib.spring.SpringFormTag;
import com.agnitas.taglib.table.decorator.TableDecorator;
import com.agnitas.taglib.table.util.TableTagUtils;
import com.agnitas.taglib.table.writer.TableTagHtmlWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.BodyTagSupport;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.displaytag.properties.SortOrderEnum;

public class TableTag extends BodyTagSupport {

    private static final Logger logger = LogManager.getLogger(TableTag.class);

    private String varName;
    private String cssClass;
    private String modelAttribute;
    private String requestUri;
    private String decoratorClassName;
    private int pageSize;

    private List<?> list;
    private PaginatedListImpl<?> paginatedList;
    private Iterator<?> iterator;

    private int rowIndex;
    private boolean headerAdded;
    private int totalPages;
    private int currentPage;
    int fullListSize;

    private SpringFormTag formTag;
    private TableDecorator decorator;
    private TableTagHtmlWriter htmlWriter;

    @Override
    public void release() {
        super.release();

        varName = null;
        decoratorClassName = null;
        cssClass = null;
        modelAttribute = null;
        requestUri = null;

        list = null;
        paginatedList = null;
        iterator = null;
        headerAdded = false;
        formTag = null;
        htmlWriter = null;

        pageSize = 0;
        fullListSize = 0;
        totalPages = 0;
        currentPage = 0;
        rowIndex = 0;
    }

    @Override
    public int doStartTag() throws JspException {
        formTag = ((SpringFormTag) findAncestorWithClass(this, SpringFormTag.class));
        pageSize = 0;
        headerAdded = false;
        rowIndex = 0;
        paginatedList = null;
        totalPages = 1;
        currentPage = 1;
        fullListSize = 0;
        decorator = null;

        if (StringUtils.isNotBlank(decoratorClassName)) {
            initDecorator();
        }

        Object items = this.pageContext.getRequest().getAttribute(modelAttribute);

        if (items == null) {
            list = Collections.emptyList();
        } else if (items instanceof PaginatedListImpl<?> paginatesItems) {
            paginatedList = paginatesItems;
            list = paginatedList.getList();
            fullListSize = paginatedList.getFullListSize();
            pageSize = paginatedList.getPageSize();

            totalPages = calculateTotalPages(fullListSize, pageSize);
            currentPage = AgnUtils.getValidPageNumber(fullListSize, paginatedList.getPageNumber(), pageSize);
        } else if (items instanceof List<?> itemsList) {
            list = itemsList;
            fullListSize = list.size();

            String sortProperty = getSortColumnParamValue();
            if (StringUtils.isNotBlank(sortProperty) && !list.isEmpty()) {
                list.sort(TableTagUtils.getComparator(sortProperty, getSortDirParamValue().equals("desc")));
            }

            if (pageSize > 0) {
                totalPages = calculateTotalPages(fullListSize, pageSize);
                try {
                    int page = Integer.parseInt(pageContext.getRequest().getParameter("page"));
                    currentPage = AgnUtils.getValidPageNumber(list.size(), page, pageSize);
                } catch (Exception e) {
                    currentPage = 1;
                }

                list = list.stream()
                        .skip((long) (currentPage - 1) * pageSize)
                        .limit(pageSize)
                        .toList();
            }
        } else {
            throw new IllegalStateException("Table tag can't work with '" + items.getClass().getName() + "' type!");
        }

        htmlWriter = new TableTagHtmlWriter(pageContext, getRequestUri(), fullListSize, list.size(), currentPage, totalPages);
        iterator = list.iterator();

        htmlWriter.openTable(Map.of(
                "id", StringUtils.defaultIfBlank(getId(), modelAttribute + "-table"),
                "class", defaultString(cssClass)
        ));

        htmlWriter.openHead();
        htmlWriter.openRow();

        if (!list.isEmpty()) {
            setItemAttribute(list.get(0));
        }

        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doAfterBody() {
        htmlWriter.closeRow();

        if (!headerAdded) {
            htmlWriter.closeHead();
            htmlWriter.openBody();
            headerAdded = true;
        }

        if (!iterator.hasNext()) {
            return SKIP_BODY;
        }

        Object item = iterator.next();

        if (decorator != null) {
            Map<String, String> rowAttrs = new HashMap<>(Map.of("class", decorator.getRowCssClass(item)));
            rowAttrs.putAll(decorator.getRowAttributes(item));
            htmlWriter.openRow(rowAttrs);
        } else {
            htmlWriter.openRow();
        }

        setItemAttribute(item);
        this.pageContext.setAttribute(this.getVar() + "_index", rowIndex++);

        return EVAL_BODY_BUFFERED;
    }

    protected boolean isHeaderAdded() {
        return this.headerAdded;
    }

    protected TableTagHtmlWriter getHtmlWriter() {
        return htmlWriter;
    }

    protected String getNextSortDir(String sortProperty) {
        if (!StringUtils.equals(sortProperty, getSortCriterion())) {
            return "asc";
        }

        return SortOrderEnum.ASCENDING.equals(getSortDir()) ? "desc" : "asc";
    }

    protected String getSortOrderClass() {
        return SortOrderEnum.DESCENDING.equals(getSortDir()) ? "order2" : "order1";
    }

    protected boolean isSortedBy(String property) {
        return StringUtils.equals(property, getSortCriterion());
    }

    private SortOrderEnum getSortDir() {
        return Optional.ofNullable(paginatedList)
                .map(PaginatedListImpl::getSortDirection)
                .orElseGet(() -> getSortDirParamValue().equals("desc") ? SortOrderEnum.DESCENDING : SortOrderEnum.ASCENDING);
    }

    private String getSortCriterion() {
        return Optional.ofNullable(paginatedList)
                .map(PaginatedListImpl::getSortCriterion)
                .orElse(getSortColumnParamValue());
    }

    private String getSortDirParamValue() {
        return pageContext.getRequest().getParameter("dir");
    }

    private String getSortColumnParamValue() {
        return pageContext.getRequest().getParameter("sort");
    }

    private void setItemAttribute(Object item) {
        if (item != null) {
            this.pageContext.setAttribute(this.getVar(), item);
        } else {
            this.pageContext.removeAttribute(this.getVar());
        }
    }

    @Override
    public int doEndTag() throws JspException {
        if (list.isEmpty()) {
            String agnTableEmptyListMsg = (String) pageContext.getAttribute("agnTableEmptyListMsg");
            if (agnTableEmptyListMsg == null) {
                agnTableEmptyListMsg = translateMessage("noResultsFound", getReq());
            }

            htmlWriter.appendEmptyRow(agnTableEmptyListMsg);
        }

        htmlWriter.closeBody();
        htmlWriter.closeTable();

        if (paginatedList != null || pageSize > 0) {
            htmlWriter.appendFooter(getSelectedNumberOfRows());
        }

        htmlWriter.flush();
        return super.doEndTag();
    }

    private int getSelectedNumberOfRows() {
        int numberOfRows = 0;
        if (formTag != null && pageContext.getRequest().getAttribute(formTag.getModelAttribute()) instanceof PaginationForm paginationForm) {
            numberOfRows = paginationForm.getNumberOfRows();
        }

        return Math.max(numberOfRows, 20);
    }

    private void initDecorator() {
        try {
            decorator = (TableDecorator) Class.forName(decoratorClassName).getConstructor().newInstance();
            decorator.init(getLocale(getReq()));
        } catch (Exception e) {
            logger.error("Table decorator '%s' can't be instantiated!".formatted(decoratorClassName), e);
            throw new RuntimeException(e);
        }
    }

    private HttpServletRequest getReq() {
        return (HttpServletRequest) pageContext.getRequest();
    }

    // region Getters & Setters

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getModelAttribute() {
        return modelAttribute;
    }

    public void setModelAttribute(String modelAttribute) {
        this.modelAttribute = modelAttribute;
    }

    public String getRequestUri() {
        if (StringUtils.isBlank(requestUri) && formTag != null) {
            requestUri = formTag.getServletRelativeAction();
        }

        if (pageContext.getResponse() instanceof HttpServletResponse resp) {
            requestUri = resp.encodeURL(requestUri);
        }

        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getVar() {
        return varName;
    }

    public void setVar(String varName) {
        this.varName = varName;
    }

    public String getDecorator() {
        return decoratorClassName;
    }

    public void setDecorator(String decoratorClassName) {
        this.decoratorClassName = decoratorClassName;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // endregion
}
