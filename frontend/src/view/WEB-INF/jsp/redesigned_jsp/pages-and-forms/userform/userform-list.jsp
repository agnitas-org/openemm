<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.commons.ActivenessStatus" %>
<%@ page import="org.agnitas.dao.MailingStatus" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="userFormURLPattern" type="java.lang.String"--%>
<%--@elvariable id="webformListJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="companyToken" type="java.lang.String"--%>

<c:set var="ACTIVE" value="<%= ActivenessStatus.ACTIVE %>"/>
<c:set var="INACTIVE" value="<%= ActivenessStatus.INACTIVE %>"/>

<c:set var="isDeletionAllowed" value="false"/>
<emm:ShowByPermission token="forms.delete">
    <c:set var="isDeletionAllowed" value="true"/>
</emm:ShowByPermission>

<div class="filter-overview hidden" data-controller="emm-activeness" data-editable-view="${agnEditViewKey}">

    <script data-initializer="emm-activeness" type="application/json">
        {
            "urls": {
                "SAVE": "/webform/saveActiveness.action"
            }
        }
    </script>

    <div id="table-tile" class="tile js-data-table" data-table="webform-lists" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview" /></h1>

            <c:if test="${not empty companyToken}">
                <div class="tile-controls">
                    <div class="notification-simple rounded-5">
                        <span>CTOKEN: </span>
                        <span class="d-flex align-items-center gap-3 text-dark" data-copyable="" data-copyable-value="${companyToken}">
                            ${companyToken}
                            <i class="icon icon-copy"></i>
                        </span>
                    </div>
                </div>
            </c:if>
        </div>

        <div class="tile-body">
            <div class="js-data-table-body" data-web-storage="userform-overview"></div>

            <c:forEach var="entry" items="${webformListJson}">
                <c:url var="viewLink" value="/webform/${entry['id']}/view.action"/>
                <c:set target="${entry}" property="show" value="${viewLink}"/>

                <c:url var="deleteLink" value="/webform/deleteRedesigned.action?bulkIds=${entry['id']}"/>
                <c:set target="${entry}" property="delete" value="${deleteLink}"/>
            </c:forEach>

            <script id="webform-lists" type="application/json">
                {
                    "columns": [
                        {
                            "field": "select",
                            "type": "bulkSelectColumn",
                            "headerCheckboxSelectionFilteredOnly": true,
                            "hide": ${not isDeletionAllowed}
                        },
                        {
                            "headerName": "<mvc:message code='Form'/>",
                            "editable": false,
                            "cellRenderer": "StringCellRenderer",
                            "field": "name",
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
                            "headerName": "<mvc:message code='userform.usesActions'/>",
                            "editable": false,
                            "suppressMenu": true,
                            "field": "actionNames",
                            "cellStyle": {"textAlign": "center"},
                            "suppressSizeToFit": true,
                            "cellRenderer": "MustacheTemplateCellRender",
                            "cellRendererParams": {"templateName": "webform-usage-badge"},
                            "width": 85
                        },
                        {
                            "headerName": "<mvc:message code='default.url'/>",
                            "editable": false,
                            "suppressMenu": true,
                            "cellRenderer": "StringCellRenderer",
                            "field": "webformUrl"
                        },
                        {
                            "headerName": "<mvc:message code='default.creationDate'/>",
                            "editable": false,
                            "field": "creationDate",
                            "type": "dateColumn",
                            "width": 150
                        },
                        {
                            "headerName": "<mvc:message code='default.changeDate'/>",
                            "editable": false,
                            "field": "changeDate",
                            "type": "dateColumn",
                            "width": 150
                        },
                        {
                            "headerName": "<mvc:message code='workflow.view.status.active'/>",
                            "editable": false,
                            "field": "activeStatus",
                            "type": "select",
                            "cellRenderer": "MustacheTemplateCellRender",
                            "cellRendererParams": {"templateName": "webform-activeness"},
                            "suppressSizeToFit": true,
                            "noViewLink": true,
                            "cellStyle": {"display": "flex", "align-items": "center"}
                        },
                        {
                            "field": "delete",
                            "type": "deleteColumn",
                            "hide": ${not isDeletionAllowed}
                        }
                    ],
                    "data": ${webformListJson}
                }
            </script>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up desktop-hidden"></i><mvc:message code="report.mailing.filter"/>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-icon-sm btn-inverse" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-sync"></i></a>
                <a class="btn btn-icon btn-icon-sm btn-primary" id="apply-filter" data-tooltip="<mvc:message code="button.filter.apply"/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="name-filter"><mvc:message code="Name" /></label>
                    <input type="text" id="name-filter" class="form-control"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="description-filter"><mvc:message code="Description" /></label>
                    <input type="text" id="description-filter" class="form-control"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="creationDate-from-filter"><mvc:message code="default.creationDate" /></label>
                    <div class="inline-input-range" data-date-range>
                        <div class="date-picker-container">
                            <input type="text" id="creationDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container">
                            <input type="text" id="creationDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                        </div>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="changeDate-from-filter"><mvc:message code="default.changeDate" /></label>
                    <div class="inline-input-range" data-date-range>
                        <div class="date-picker-container">
                            <input type="text" id="changeDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container">
                            <input type="text" id="changeDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                        </div>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="activeStatus-filter"><mvc:message code="Status" /></label>
                    <select id="activeStatus-filter" class="form-control">
                        <option value=""><mvc:message code="default.All" /></option>
                        <option value="${ACTIVE}"><mvc:message code="workflow.view.status.active" /></option>
                        <option value="${INACTIVE}"><mvc:message code="workflow.view.status.inActive" /></option>
                    </select>
                </div>
            </div>
        </div>
    </div>
</div>

<script id="webform-usage-badge" type="text/x-mustache-template">
    {{ if (value.length > 0) { }}
        <span class="pill-badge" data-tooltip="<mvc:message code="default.Name"/>: {{- value.join(', ') }}">
            <mvc:message code="default.Yes"/>
        </span>
    {{ } else { }}
        <span class="pill-badge"><mvc:message code="No"/></span>
    {{ } }}
</script>

<script id="webform-activeness" type="text/x-mustache-template">
    {{ var checked = value == '${ACTIVE}' ? 'checked' : '';}}
    <div class="form-check form-switch">
        <input type="checkbox" {{- checked }} class="form-check-input" role="switch" data-action="toggle-active" data-item-id="{{- entry.id}}">
    </div>
</script>
