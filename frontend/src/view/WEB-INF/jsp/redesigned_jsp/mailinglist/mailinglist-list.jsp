<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.web.forms.ComMailinglistForm"--%>
<%--@elvariable id="mailingListsJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>

        <div class="tile-body js-data-table" data-table="mailing-lists" data-controller="mailinglist-list">
            <script type="application/json" data-initializer="mailinglist-list">
                {
                    "urls": {
                        "MAILINGLIST_BULK_DELETE": "<c:url value="/mailinglist/confirmBulkDelete.action"/>"
                    }
                }
            </script>
            <div class="js-data-table-body" data-web-storage="mailinglist-overview"></div>
        </div>
        <c:set value="true" var="hideFrequencyCounterInfo"/>
        <%@include file="fragments/diactivate-hide-frequency-counter-property.jspf" %>

        <c:forEach var="entry" items="${mailingListsJson}">
            <c:url var="viewLink" value="/mailinglist/${entry['id']}/view.action"/>
            <c:set target="${entry}" property="show" value="${viewLink}"/>
            <emm:ShowByPermission token="mailinglist.delete">
                <c:url var="deleteLink" value="/mailinglist/${entry['id']}/confirmDelete.action"/>
                <c:set target="${entry}" property="delete" value="${deleteLink}"/>
            </emm:ShowByPermission>
        </c:forEach>

        <script id="mailing-lists" type="application/json">
            {
                "columns": [
                    {
                        "field": "select",
                        "type": "bulkSelectColumn"
                    },
                    {
                        "headerName": "<mvc:message code='MailinglistID'/>",
                        "editable": false,
                        "cellRenderer": "StringCellRenderer",
                        "field": "id",
                        "type": "numberColumn",
                        "suppressSizeToFit": true
                    },
                    {
                        "headerName": "<mvc:message code='Mailinglist'/>",
                        "editable": false,
                        "cellRenderer": "NotEscapedStringCellRenderer",
                        "field": "shortname",
                        "resizable": true,
                        "type": "textCaseInsensitiveColumn"
                    },
                    {
                        "headerName": "<mvc:message code='Description'/>",
                        "editable": false,
                        "cellRenderer": "NotEscapedStringCellRenderer",
                        "field": "description",
                        "type": "textCaseInsensitiveColumn"
                    },
                    {
                        "headerName": "<mvc:message code='CreationDate'/>",
                        "editable": false,
                        "field": "creationDate",
                        "suppressSizeToFit": true,
                        "type": "dateColumn"
                    },
                    {
                        "headerName": "<mvc:message code='default.changeDate'/>",
                        "editable": false,
                        "field": "changeDate",
                        "suppressSizeToFit": true,
                        "type": "dateColumn"
                    },
                    {
                        "headerName": "<mvc:message code='mailinglist.frequency.counter'/>",
                        "editable": false,
                        "suppressMenu": true,
                        "suppressSizeToFit": true,
                        "field": "isFrequencyCounterEnabled",
                        "hide": ${hideFrequencyCounterInfo},
                        "cellStyle": {"textAlign": "center"},
                        "cellRenderer": "MustacheTemplateCellRender",
                        "cellRendererParams": {"templateName": "frequency-counter-badge"}
                    },
                    {
                        "field": "delete",
                        "type": "deleteColumn"
                    }
                ],
                "data": ${mailingListsJson}
            }
        </script>

        <script id="frequency-counter-badge" type="text/x-mustache-template">
            <span class="pill-badge">{{- t(value === true ? 'defaults.yes' : 'defaults.no') }}</span>
        </script>
    </div>

    <div id="filter-tile" class="tile" data-toggle-tile="mobile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" id="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="id-filter"><mvc:message code="MailinglistID"/></label>
                    <input type="text" id="id-filter" class="form-control"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="shortname-filter"><mvc:message code="Name"/></label>
                    <input type="text" id="shortname-filter" class="form-control"/>
                </div>
                <div class="col-12">
                    <div class="row g-2" data-date-range>
                        <div class="col">
                            <label class="form-label" for="creationDate-from-filter"><mvc:message code="CreationDate"/></label>
                            <div class="date-picker-container mb-1">
                                <input type="text" id="creationDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                            </div>
                        </div>
                        <div class="col">
                            <label class="form-label" for="creationDate-to-filter">&nbsp;</label>
                            <div class="date-picker-container">
                                <input type="text" id="creationDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-12">
                    <div class="row g-2" data-date-range>
                        <div class="col">
                            <label class="form-label" for="changeDate-from-filter"><mvc:message code="default.changeDate"/></label>
                            <div class="date-picker-container mb-1">
                                <input type="text" id="changeDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                            </div>
                        </div>
                        <div class="col">
                            <label class="form-label" for="changeDate-to-filter">&nbsp;</label>
                            <div class="date-picker-container">
                                <input type="text" id="changeDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
