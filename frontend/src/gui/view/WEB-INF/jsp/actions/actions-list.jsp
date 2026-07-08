<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.action.operations.ActionOperationType" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="actionListJson" type="org.json.JSONArray"--%>

<mvc:message var="deactivateMsg" code="btndeactivate" />
<mvc:message var="activateMsg"   code="button.Activate" />

<c:set var="isDeletionAllowed" value="${emm:permissionAllowed('actions.delete', pageContext.request)}"/>
<c:set var="isChangeAllowed"   value="${emm:permissionAllowed('actions.change', pageContext.request)}"/>

<c:url var="confirmDeleteUrl"  value="/action/delete.action"/>
<c:url var="confirmRestoreUrl" value="/action/restore.action"/>

<div class="filter-overview" data-controller="activeness-overview" data-editable-view="${agnEditViewKey}">

    <script type="application/json" data-initializer="activeness-overview">
        {
            "url": "<c:url value="/action/changeActiveness.action" />"
        }
    </script>

    <div id="table-tile" class="tile" data-editable-tile="main">
        <div class="tile-body">
            <div class="table-wrapper" data-web-storage="action-overview" data-js-table="action-list">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <div class="bulk-actions hidden">
                            <p class="bulk-actions__selected">
                                <span><%-- Updates by JS --%></span>
                                <mvc:message code="default.list.entry.select" />
                            </p>
                            <div class="bulk-actions__controls">
                                <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${activateMsg}" data-action="bulk-activate-js" data-bulk-action="activate-action">
                                    <i class="icon icon-check-circle"></i>
                                </a>
                                <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deactivateMsg}" data-action="bulk-deactivate-js" data-bulk-action="deactivate-action">
                                    <i class="icon icon-times-circle"></i>
                                </a>

                                <c:if test="${isDeletionAllowed}">
                                    <a href="#" class="icon-btn icon-btn--danger js-data-table-bulk-delete" data-tooltip="<mvc:message code="bulkAction.delete.action" />"
                                       data-bulk-action="delete-action" data-bulk-url="${confirmDeleteUrl}">
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${deactivateMsg}" data-bulk-action="restore" data-bulk-url="${confirmRestoreUrl}">
                                        <i class="icon icon-redo"></i>
                                    </a>
                                </c:if>
                            </div>
                        </div>
                        <%@include file="../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../common/table/entries-label.jsp" />
                    </div>
                </div>
            </div>

            <script id="action-list" type="application/json">
                {
                    "columns": [
                        {
                            "headerName": "<mvc:message code='Status'/>",
                            "editable": false,
                            "field": "active",
                            "type": "select",
                            "cellRenderer": "MustacheTemplateCellRender",
                            "cellRendererParams": {"templateName": "action-activeness-status"},
                            "suppressSizeToFit": true,
                            "cellStyle": {"display": "flex", "align-items": "center"}
                        },
                        {
                            "headerName": "<mvc:message code='MailinglistID'/>",
                            "editable": false,
                            "suppressSizeToFit": true,
                            "cellRenderer": "StringCellRenderer",
                            "field": "id",
                            "type": "numberColumn"
                        },
                        {
                            "headerName": "<mvc:message code='default.Name'/>",
                            "type": "textCaseInsensitiveColumn",
                            "cellRenderer": "NotEscapedStringCellRenderer",
                            "field": "shortname"
                        },
                        {
                            "headerName": "<mvc:message code='Description'/>",
                            "type": "textCaseInsensitiveColumn",
                            "cellRenderer": "NotEscapedStringCellRenderer",
                            "field": "description"
                        },
                        {
                            "headerName": "<mvc:message code='used'/>",
                            "editable": false,
                            "suppressMenu": true,
                            "field": "formNames",
                            "suppressSizeToFit": true,
                            "cellStyle": {"display": "flex", "align-items": "center"},
                            "cellRenderer": "MustacheTemplateCellRender",
                            "cellRendererParams": {"templateName": "action-usage-badge"}
                        },
                        {
                            "headerName": "<mvc:message code='default.creationDate'/>",
                            "editable": false,
                            "field": "creationDate",
                            "type": "dateColumn"
                        },
                        {
                            "headerName": "<mvc:message code='default.changeDate'/>",
                            "editable": false,
                            "field": "changeDate",
                            "type": "dateColumn"
                        },
                        {
                          "headerName": "<mvc:message code='Steps'/>",
                          "type": "setColumn",
                          "field": "operationTypes",
                          "hide": true
                        },
                        {
                            "field": "deleted",
                            "type": "tableActionsColumn",
                            "buttons": [
                              {"name": "activate-action",   "template": "action-activate-btn",   "hide": ${not isChangeAllowed}},
                              {"name": "deactivate-action", "template": "action-deactivate-btn", "hide": ${not isChangeAllowed}},
                              {"name": "delete-action",     "template": "action-delete-btn",     "hide": ${not isDeletionAllowed}},
                              {"name": "restore",           "template": "action-restore-btn",    "hide": true}
                            ],
                            "hide": ${not isChangeAllowed and not isDeletionAllowed}
                        }
                    ],
                    "data": ${actionListJson},
                    "options": {"viewLinkTemplate": "/action/{{- id }}/view.action"}
                }
            </script>
        </div>
    </div>

    <div id="filter-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" id="apply-filter" data-tooltip="<mvc:message code="button.filter.apply"/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body form-column js-scrollable">
            <div>
                <label class="form-label" for="id-filter"><mvc:message code="MailinglistID" /></label>
                <input type="number" id="id-filter" class="form-control"/>
            </div>

            <div>
                <label class="form-label" for="shortname-filter"><mvc:message code="Name" /></label>
                <input type="text" id="shortname-filter" class="form-control"/>
            </div>

            <div>
                <label class="form-label" for="description-filter"><mvc:message code="Description" /></label>
                <input type="text" id="description-filter" class="form-control"/>
            </div>

            <div>
                <label class="form-label" for="creationDate-from-filter"><mvc:message code="default.creationDate" /></label>
                <div class="inline-input-range" data-date-range>
                    <div class="date-picker-container">
                        <input type="text" id="creationDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker" />
                    </div>
                    <div class="date-picker-container">
                        <input type="text" id="creationDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker" />
                    </div>
                </div>
            </div>

            <div>
                <label class="form-label" for="changeDate-from-filter"><mvc:message code="default.changeDate" /></label>
                <div class="inline-input-range" data-date-range>
                    <div class="date-picker-container">
                        <input type="text" id="changeDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker" />
                    </div>
                    <div class="date-picker-container">
                        <input type="text" id="changeDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker" />
                    </div>
                </div>
            </div>

            <div>
                <label class="form-label" for="active-filter"><mvc:message code="Status" /></label>
                <select id="active-filter" class="form-control" data-result-template="select2-badge-option" data-selection-template="select2-badge-option">
                    <option value=""><mvc:message code="default.All" /></option>
                    <option value="true" data-badge-class="status.success"><mvc:message code="workflow.view.status.active" /></option>
                    <option value="false" data-badge-class="status.error"><mvc:message code="workflow.view.status.inActive" /></option>
                </select>

                <div class="form-check form-switch mt-1">
                    <input type="checkbox" id="deleted-filter" value="true" class="form-check-input" role="switch"/>
                    <label class="form-label form-check-label" for="deleted-filter">
                        <mvc:message code="default.list.deleted.show"/>
                    </label>
                </div>
            </div>

            <div>
                <label for="operationTypes-filter" class="form-label"><mvc:message code="Steps" /></label>
                <select id="operationTypes-filter" class="form-control" multiple data-result-template="action-operation-type" data-selection-template="action-operation-type">
                    <c:forEach var="operationType" items="${ActionOperationType.values()}">
                        <option value="${operationType}">${operationType.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
    </div>
</div>

<script id="action-usage-badge" type="text/x-mustache-template">
    {{ if (value.length > 0) { }}
        <span class="table-badge" data-tooltip="<mvc:message code="default.Name"/>: {{- value.join(', ') }}">
            <mvc:message code="default.Yes"/>
        </span>
    {{ } else { }}
        <span class="table-badge"><mvc:message code="No"/></span>
    {{ } }}
</script>

<script id="action-activeness-status" type="text/x-mustache-template">
    {{ if (value === 'true') { }}
        <span class="status-badge status.success" data-tooltip="<mvc:message code="workflow.view.status.active" />"></span>
    {{ } else { }}
        <span class="status-badge status.error" data-tooltip="<mvc:message code="workflow.view.status.inActive" />"></span>
    {{ } }}
</script>

<script id="action-delete-btn" type="text/x-mustache-template">
    <a href="${confirmDeleteUrl}{{= '?bulkIds=' + id }}" type="button" class="icon-btn icon-btn--danger js-data-table-delete"
       data-bulk-action="delete" data-tooltip="<mvc:message code="Delete" />">
        <i class="icon icon-trash-alt"></i>
    </a>
</script>

<script id="action-restore-btn" type="text/x-mustache-template">
    <a data-bulk-url="${confirmRestoreUrl}" type="button" class="icon-btn icon-btn--primary"
       data-tooltip="<mvc:message code='default.restore' />" data-restore-row>
        <i class="icon icon-redo"></i>
    </a>
</script>

<script id="action-activate-btn" type="text/x-mustache-template">
    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${activateMsg}" data-action="activate-js" data-item-id="{{- id }}">
        <i class="icon icon-check-circle"></i>
    </a>
</script>

<script id="action-deactivate-btn" type="text/x-mustache-template">
    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deactivateMsg}" data-action="deactivate-js" data-item-id="{{- id }}">
        <i class="icon icon-times-circle"></i>
    </a>
</script>

<script id="action-operation-type" type="text/x-mustache-template">
    <span>{{- t('triggerManager.operation.' + text) }}</span>
</script>

<script type="text/javascript">
  AGN.Opt.TableActionsConditions['activate-action'] = data => {
    return data.active === 'false';
  };
  AGN.Opt.TableActionsConditions['deactivate-action'] = data => {
    return data.active === 'true';
  };
</script>
