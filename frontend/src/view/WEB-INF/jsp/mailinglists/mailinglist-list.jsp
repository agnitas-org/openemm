<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.web.forms.ComMailinglistForm"--%>
<%--@elvariable id="mailingListsJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

<div class="tile js-data-table" data-sizing="container" data-table="mailing-lists" data-controller="mailinglist-list">
    <script type="application/json" data-initializer="mailinglist-list">
        {
            "urls": {
                "MAILINGLIST_BULK_DELETE": "<c:url value="/mailinglist/confirmBulkDelete.action"/>"
            }
        }
    </script>

    <div class="tile-header" data-sizing="top">
        <h2 class="headline">
            <mvc:message code="default.Overview" />
        </h2>
        <ul class="tile-header-actions">
            <emm:ShowByPermission token="mailing.delete">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-pencil"></i>
                        <span class="text"><mvc:message code="bulkAction"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li>
                            <a href="#" data-action="bulk-delete">
                                <mvc:message code="bulkAction.delete.mailinglist"/>
                            </a>
                        </li>
                    </ul>
                </li>
            </emm:ShowByPermission>

            <li class="dropdown">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                    <i class="icon icon-eye"></i>
                    <span class="text"><mvc:message code="button.Show"/></span>
                    <i class="icon icon-caret-down"></i>
                </a>
                <ul class="dropdown-menu">
                    <li class="dropdown-header"><mvc:message code="listSize"/></li>
                    <li>
                        <label class="label js-data-table-paginate" data-page-size="20" data-table-body=".js-data-table-body" data-web-storage="mailinglist-overview">
                            <span class="label-text">20</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="50" data-table-body=".js-data-table-body" data-web-storage="mailinglist-overview">
                            <span class="label-text">50</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="100" data-table-body=".js-data-table-body" data-web-storage="mailinglist-overview">
                            <span class="label-text">100</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div class="tile-content" data-sizing="scroll">
        <div class="js-data-table-body" data-web-storage="mailinglist-overview" style="height: 100%;"></div>
    </div>

    <c:forEach var="entry" items="${mailingListsJson}">
        <c:url var="viewLink" value="/mailinglist/${entry['id']}/view.action"/>
        <c:set target="${entry}" property="show" value="${viewLink}"/>

        <c:url var="deleteLink" value="/mailinglist/${entry['id']}/confirmDelete.action"/>
        <c:set target="${entry}" property="delete" value="${deleteLink}"/>
    </c:forEach>

    <script id="mailing-lists" type="application/json">
        {
            "columns": [
                {
                    "headerName": "",
                    "editable": false,
                    "field": "select",
                    "checkboxSelection": true,
                    "headerCheckboxSelection": true,
                    "suppressResize": true,
                    "suppressMenu": true,
                    "suppressSorting": true,
                    "width": 30,
                    "cellAction": "select"
                },
                {
                    "headerName": "<mvc:message code='MailinglistID'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "id",
                    "type": "numberColumn"
                },
                {
                    "headerName": "<mvc:message code='Mailinglist'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "shortname"
                },
                {
                    "headerName": "<mvc:message code='default.description'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "description"
                },
                {
                    "headerName": "<mvc:message code='CreationDate'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "creationDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:toUpperCase(dateFormatPattern)}" }
                },
                {
                    "headerName": "<mvc:message code='default.changeDate'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "changeDate",
                    "type": "dateColumn",
                    "cellRenderer": "DateCellRenderer",
                    "cellRendererParams": { "optionDateFormat": "${fn:toUpperCase(dateFormatPattern)}" }
                },
                {
                    "headerName": "",
                    "editable": false,
                    "field": "delete",
                    "suppressResize": true,
                    "suppressMenu": true,
                    "suppressSorting": true,
                    "width": 36,
                    "cellAction": null,
                    "cellRenderer": "DeleteCellRenderer"
                }
            ],
            "data": ${mailingListsJson}
        }
    </script>
</div>
