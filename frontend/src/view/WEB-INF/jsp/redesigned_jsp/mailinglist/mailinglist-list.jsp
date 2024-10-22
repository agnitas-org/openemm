<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailinglistForm" type="com.agnitas.web.forms.ComMailinglistForm"--%>
<%--@elvariable id="mailingListsJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

<c:set var="deleteAllowed" value="${emm:permissionAllowed('mailinglist.delete', pageContext.request)}" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}" data-controller="mailinglist-list">
    <div id="table-tile" class="tile" data-editable-tile="main">
        <div class="tile-body">
            <div class="table-wrapper" data-web-storage="mailinglist-overview" data-js-table="mailing-lists">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${deleteAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <a href="#" class="icon-btn text-danger" data-tooltip="<mvc:message code="bulkAction.delete.mailinglist" />" data-action="bulk-delete">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </div>
                            </div>
                        </c:if>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp" />
                    </div>
                </div>
            </div>
        </div>
        <c:set value="true" var="hideFrequencyCounterInfo"/>
        <%@include file="fragments/diactivate-hide-frequency-counter-property.jspf" %>

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
                        "headerName": "<mvc:message code='Name'/>",
                        "editable": false,
                        "cellRenderer": "MustacheTemplateCellRender",
                        "cellRendererParams": {"templateName": "mailinglist-name"},
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
                        "type": "tableActionsColumn",
                        "buttons": [{"name": "delete", "template": "mailinglist-delete-btn"}],
                        "hide": ${not deleteAllowed}
                    }
                ],
                "data": ${mailingListsJson},
                "options": {"viewLinkTemplate": "/mailinglist/{{- id }}/view.action"}
            }
        </script>

        <script id="frequency-counter-badge" type="text/x-mustache-template">
            <span class="table-badge">{{- t(value === true ? 'defaults.yes' : 'defaults.no') }}</span>
        </script>

        <script id="mailinglist-name" type="text/x-mustache-template">
            <div class="d-flex align-items-center gap-2 overflow-wrap-anywhere">
                {{ if (entry.restrictedForSomeAdmins) { }}
                    <span class="icon-badge text-bg-danger-dark" data-tooltip="<mvc:message code="mailinglist.limit.access" />">
                     <i class="icon icon-user-lock"></i>
                    </span>
                {{ } }}
                <span class="text-truncate-table">{{- value }}</span>
            </div>
        </script>

        <script id="mailinglist-delete-btn" type="text/x-mustache-template">
            <a href="{{= AGN.url('/mailinglist/' + id + '/confirmDelete.action') }}" type="button" class="icon-btn text-danger js-data-table-delete" data-tooltip="<mvc:message code="Delete" />">
                <i class="icon icon-trash-alt"></i>
            </a>
        </script>
    </div>

    <div id="filter-tile" class="tile" data-toggle-tile="mobile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-inverse" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" id="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
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
