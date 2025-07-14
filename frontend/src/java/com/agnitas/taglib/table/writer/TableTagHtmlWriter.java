/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.table.writer;

import static com.agnitas.taglib.table.util.TableTagUtils.buildUrl;
import static com.agnitas.taglib.table.util.TableTagUtils.getPageNumbersToDisplay;
import static com.agnitas.taglib.table.util.TableTagUtils.translateMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TableTagHtmlWriter {

    private static final Logger logger = LogManager.getLogger(TableTagHtmlWriter.class);

    private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(20, 50, 100, 200);
    private static final int MAX_PAGES_COUNT = 10;

    private final PageContext pageContext;
    private final String url;
    private final int fullListSize;
    private final int listSize;
    private final int currentPage;
    private final int totalPages;

    private final StringBuilder builder = new StringBuilder();

    public TableTagHtmlWriter(PageContext pageContext, String url, int fullListSize, int listSize, int currentPage, int totalPages) {
        this.pageContext = pageContext;
        this.url = url;
        this.fullListSize = fullListSize;
        this.listSize = listSize;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
    }

    public void closeRow() {
        closeTag("tr");
    }

    public void openHead() {
        openTag("thead");
    }

    public void closeHead() {
        closeTag("thead");
    }

    public void closeBody() {
        closeTag("tbody");
    }

    public void openTable(Map<String, String> attrs) {
        openTag("table", attrs);
    }

    public void closeTable() {
        closeTag("table");
    }

    public void openBody() {
        openTag("tbody");
    }

    public void openRow() {
        openRow(Collections.emptyMap());
    }

    public void openRow(Map<String, String> attrs) {
        openTag("tr", attrs);
    }

    public void openDataCell(Map<String, String> attrs) {
        openTag("td", attrs);
    }

    public void closeDataCell() {
        closeTag("td");
    }

    public void openHeaderCell(Map<String, String> attrs) {
        openTag("th", attrs);
    }

    public void closeHeaderCell() {
        closeTag("th");
    }

    public void write(String tag, String content) {
        openTag(tag);
        write(content);
        closeTag(tag);
    }

    public void write(String content) {
        appendContent(content);
    }

    public void openTag(String tag) {
        write("<" + tag + ">");
    }

    public void openTag(String tag, Map<String, String> attrs) {
        appendContent("<" + tag);
        attrs.forEach((k, v) -> appendContent(" " + k + "=\"" + v + "\""));
        appendContent(">");
    }

    public void closeTag(String tag) {
        appendContent("</" + tag + ">");
    }

    public void appendEmptyRow(String message) {
        if (StringUtils.isNotBlank(message)) {
            appendContent("<tr class=\"empty\"><td><div class=\"notification-simple\"><i class=\"icon icon-info-circle\"></i>");
            write("span", message);
            appendContent("</div></td></tr>");
        }
    }

    public void appendRowsCountSelect(int selectedNumberOfRows) {
        appendContent("<select name=\"numberOfRows\" class=\"form-control js-select compact\" data-form-submit data-form-change data-select-options=\"width: 'auto', dropdownAutoWidth: true\">");

        PAGE_SIZE_OPTIONS.forEach(numberOfRows -> {
            appendContent("<option value=\"" + numberOfRows + "\"");

            if (selectedNumberOfRows == numberOfRows) {
                appendContent(" selected");
            }

            appendContent(">" + numberOfRows);
            closeTag("option");
        });

        closeTag("select");
    }

    public void appendHiddenRowsCount(int numberOfRows) {
        appendContent("<input type=\"hidden\" name=\"numberOfRows\" value=\"" + numberOfRows + "\" />");
    }

    public void appendPagination(int currentPage, int totalPages, List<Integer> pageNumbers) {
        openTag("ul", Map.of("class", "pagination"));

        appendPaginationArrow(1, "icon-angle-double-left", currentPage == 1);
        appendPaginationArrow(currentPage - 1, "icon-angle-left", currentPage == 1);
        pageNumbers.forEach(pageNumber -> appendPageButton(pageNumber, pageNumber == currentPage));
        appendPaginationArrow(currentPage + 1, "icon-angle-right", currentPage == totalPages);
        appendPaginationArrow(totalPages, "icon-angle-double-right", currentPage == totalPages);

        closeTag("ul");
    }

    private void appendPaginationArrow(int forPage, String iconClass, boolean disabled) {
        appendContent("<li class=\"" + (disabled ? "disabled" : "") + "\">");

        if (!disabled) {
            appendContent("<a href=\"");
            appendContent(buildUrl(url, Map.of("page", String.valueOf(forPage))));
            appendContent("\" data-paginate>");
        }
        appendContent("<i class=\"icon " + iconClass + "\"></i>");
        if (!disabled) {
            closeTag("a");
        }
        closeTag("li");
    }

    private void appendPageButton(int forPage, boolean isActive) {
        appendContent("<li");

        if (isActive) {
            appendContent(" class=\"active\">");
            write("span", String.valueOf(forPage));
        } else {
            appendContent("><a href=\"");
            appendContent(buildUrl(url, Map.of("page", String.valueOf(forPage))));
            appendContent("\" data-paginate>" + forPage + "</a>");
        }

        closeTag("li");
    }

    public void appendFooter(int numberOfRows) {
        appendContent("</div><div class=\"table-wrapper__footer\">");

        if (fullListSize == 0) {
            appendRowsSelectionBlock("default.NoEntries", () -> appendHiddenRowsCount(numberOfRows));
            return;
        }

        appendRowsSelectionBlock("default.list.display.rows", () -> appendRowsCountSelect(numberOfRows));

        if (fullListSize != listSize) {
            appendPagination(currentPage, totalPages, getPageNumbersToDisplay(currentPage, totalPages, MAX_PAGES_COUNT));
        }
    }

    private void appendRowsSelectionBlock(String messageKey, Runnable rowsCountAppendFunction) {
        appendContent("<div class=\"table-wrapper__rows-selection\">");
        rowsCountAppendFunction.run();
        write("span", translateMessage(messageKey, (HttpServletRequest) pageContext.getRequest()));
        closeTag("div");
    }

    private void appendContent(String content) {
        this.builder.append(content);
    }

    public void flush() {
        try {
            pageContext.getOut().write(builder.toString());
        } catch (IOException e) {
            logger.error("Can't append tag content: '{}'!", builder);
            throw new RuntimeException(e);
        }
    }
}
