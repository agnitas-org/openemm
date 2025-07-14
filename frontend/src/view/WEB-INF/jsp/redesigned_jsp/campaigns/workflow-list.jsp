<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop.WorkflowEndType" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowsJson" type="org.json.JSONArray"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

<c:set var="STATUSES" value="<%= WorkflowForm.WorkflowStatus.values() %>"/>
<c:set var="STATUS_ACTIVE" value="<%= WorkflowStatus.STATUS_ACTIVE %>" scope="page"/>
<c:set var="STATUS_INACTIVE" value="<%= WorkflowStatus.STATUS_INACTIVE %>" scope="page"/>
<c:set var="STATUS_OPEN" value="<%= WorkflowStatus.STATUS_OPEN %>" scope="page"/>
<c:set var="STATUS_PAUSED" value="<%= WorkflowStatus.STATUS_PAUSED %>" scope="page"/>
<c:set var="STATUS_COMPLETE" value="<%= WorkflowStatus.STATUS_COMPLETE %>" scope="page"/>
<c:set var="STATUS_TESTING" value="<%= WorkflowStatus.STATUS_TESTING %>" scope="page"/>
<c:set var="STATUS_TESTED" value="<%= WorkflowStatus.STATUS_TESTED %>" scope="page"/>

<c:set var="ACTION_BULK_CONFIRM_DELETE" value="bulkDeleteConfirm" scope="page" />
<c:set var="ACTION_BULK_CONFIRM_DEACTIVATE" value="bulkDeactivateConfirm" scope="page" />

<c:set var="REACTION_OPENED" value="<%= WorkflowReactionType.OPENED %>"/>
<c:set var="REACTION_NOT_OPENED" value="<%= WorkflowReactionType.NOT_OPENED %>"/>
<c:set var="REACTION_CLICKED" value="<%= WorkflowReactionType.CLICKED %>"/>
<c:set var="REACTION_NOT_CLICKED" value="<%= WorkflowReactionType.NOT_CLICKED %>"/>
<c:set var="REACTION_BOUGHT" value="<%= WorkflowReactionType.BOUGHT %>"/>
<c:set var="REACTION_NOT_BOUGHT" value="<%= WorkflowReactionType.NOT_BOUGHT %>"/>
<c:set var="REACTION_DOWNLOAD" value="<%= WorkflowReactionType.DOWNLOAD %>"/>
<c:set var="REACTION_CHANGE_OF_PROFILE" value="<%= WorkflowReactionType.CHANGE_OF_PROFILE %>"/>
<c:set var="REACTION_WAITING_FOR_CONFIRM" value="<%= WorkflowReactionType.WAITING_FOR_CONFIRM %>"/>
<c:set var="REACTION_OPT_IN" value="<%= WorkflowReactionType.OPT_IN %>"/>
<c:set var="REACTION_OPT_OUT" value="<%= WorkflowReactionType.OPT_OUT %>"/>

<c:set var="EVENT_REACTION" value="<%= WorkflowStartEventType.EVENT_REACTION %>"/>
<c:set var="EVENT_DATE" value="<%= WorkflowStartEventType.EVENT_DATE %>"/>

<c:set var="STOP_TYPE_AUTOMATIC" value="<%= WorkflowEndType.AUTOMATIC %>"/>
<c:set var="STOP_TYPE_DATE" value="<%= WorkflowEndType.DATE %>"/>

<mvc:message var="deactivateMsg" code="btndeactivate" />
<mvc:message var="activateMsg"   code="button.Activate" />

<c:set var="deleteAllowed" value="${emm:permissionAllowed('workflow.delete', pageContext.request)}" />
<c:set var="changeAllowed" value="${emm:permissionAllowed('workflow.change', pageContext.request)}" />

<div class="filter-overview" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile" data-controller="workflow-list" data-editable-tile="main">

        <script type="application/json" data-initializer="activeness-overview">
            {
                "url": "<c:url value="/workflow/changeActiveness.action" />"
            }
        </script>

        <div class="tile-body">
            <div class="table-wrapper" data-web-storage="workflow-overview" data-js-table="workflow-list-table">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Overview" /></h1>
                    <div class="table-wrapper__controls">
                        <c:if test="${deleteAllowed or changeAllowed}">
                            <div class="bulk-actions hidden">
                                <p class="bulk-actions__selected">
                                    <span><%-- Updates by JS --%></span>
                                    <mvc:message code="default.list.entry.select" />
                                </p>
                                <div class="bulk-actions__controls">
                                    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${activateMsg}" data-action="bulk-activate-js" data-bulk-action="activate-workflow">
                                        <i class="icon icon-check-circle"></i>
                                    </a>
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deactivateMsg}" data-action="bulk-deactivate-js" data-bulk-action="deactivate-workflow">
                                        <i class="icon icon-times-circle"></i>
                                    </a>
                                    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="<mvc:message code="bulkAction.delete.workflow" />" data-bulk-action="delete-workflow" data-action="bulk-delete">
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
    </div>

    <script id="workflow-list-table" type="application/json">
        {
            "columns": [
                {
                    "headerName": "<mvc:message code='Status'/>",
                    "editable": false,
                    "field": "status",
                    "suppressSizeToFit": true,
                    "sortable": false,
                    "cellStyle": {"display": "flex", "align-items": "center"},
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "status-cell"},
                    "type": "setColumn"
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
                    "headerName": "<mvc:message code='Start'/>",
                    "editable": false,
                    "field": "startDate",
                    "sortable": false,
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "start-cell"},
                    "suppressSizeToFit": true,
                    "type": "customColumn",
                    "filter": "WorkflowStartFilter"
                },
                {
                    "headerName": "<mvc:message code='report.stopDate'/>",
                    "editable": false,
                    "field": "stopDate",
                    "sortable": false,
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "end-cell"},
                    "type": "customColumn",
                    "suppressSizeToFit": true,
                    "filter": "WorkflowStopFilter"
                },
                {
                    "headerName": "<mvc:message code='workflow.Reaction'/>",
                    "editable": false,
                    "field": "reaction",
                    "sortable": false,
                    "type": "setColumn",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "reaction-cell"}
                },
                {
                    "type": "tableActionsColumn",
                    "buttons": [
                       {"name": "activate-workflow",   "template": "workflow-activate-btn",   "hide": ${not changeAllowed}},
                       {"name": "deactivate-workflow", "template": "workflow-deactivate-btn", "hide": ${not changeAllowed}},
                       {"name": "delete-workflow",     "template": "workflow-delete-btn",     "hide": ${not deleteAllowed}}
                    ],
                    "hide": ${not changeAllowed and not deleteAllowed}
                }
            ],
            "data": ${workflowsJson},
            "options": {"viewLinkTemplate": "/workflow/{{- id }}/view.action"}
        }
    </script>

    <div id="filter-tile" class="tile" data-toggle-tile data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title">
                <i class="icon icon-caret-up mobile-visible"></i>
                <span class="text-truncate"><mvc:message code="report.mailing.filter"/></span>
            </h1>
            <div class="tile-controls">
                <a class="btn btn-icon btn-secondary" id="reset-filter" data-form-clear="#filter-tile" data-tooltip="<mvc:message code="filter.reset"/>"><i class="icon icon-undo-alt"></i></a>
                <a class="btn btn-icon btn-primary" id="apply-filter" data-tooltip="<mvc:message code='button.filter.apply'/>"><i class="icon icon-search"></i></a>
            </div>
        </div>
        <div class="tile-body vstack gap-3 js-scrollable">
            <div>
                <label class="form-label" for="status-filter"><mvc:message code="Status" /></label>
                <select id="status-filter" multiple class="form-control" data-result-template="select2-badge-option">
                    <c:forEach var="status" items="${STATUSES}">
                        <c:if test="${status.name ne 'NONE'}">
                            <c:choose>
                                <c:when test="${status.name eq 'failed'}">
                                    <option value="${status.name}" data-badge-class="campaign.status.failed">Failed</option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${status.name}" data-badge-class="campaign.status.${status.name}"><mvc:message code="${status.messageKey}"/></option>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </c:forEach>
                </select>
            </div>
            <div>
                <label class="form-label" for="shortname-filter"><mvc:message code="Name"/></label>
                <input type="text" id="shortname-filter" class="form-control"/>
            </div>
            <div>
                <label class="form-label" for="description-filter"><mvc:message code="Description"/></label>
                <input type="text" id="description-filter" class="form-control"/>
            </div>
            <div>
                <label class="form-label" for="startDate-filter"><mvc:message code="Start"/></label>
                <select id="startDate-filter" multiple class="form-control">
                    <option value="startDate"><mvc:message code="Date"/></option>
                    <option value="actionBased"><mvc:message code="workflowlist.actionBased"/></option>
                    <option value="dateBased"><mvc:message code="workflowlist.dateBased"/></option>
                </select>
                <div class="mt-1" data-date-range data-show-by-select="#startDate-filter" data-show-by-select-values="startDate">
                    <div class="date-picker-container mb-1">
                        <input type="text" id="startDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                    </div>
                    <div class="date-picker-container">
                        <input type="text" id="startDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                    </div>
                </div>
            </div>
            <div>
                <label class="form-label" for="stopDate-from-filter"><mvc:message code="report.stopDate"/></label>
                <select id="stopDate-filter" multiple class="form-control">
                    <option value="stopDate"><mvc:message code="Date"/></option>
                    <option value="automaticEnd"><mvc:message code="workflow.stop.AutomaticEnd"/></option>
                </select>
                <div class="mt-1" data-date-range data-show-by-select="#stopDate-filter" data-show-by-select-values="stopDate">
                    <div class="date-picker-container mb-1">
                        <input type="text" id="stopDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                    </div>
                    <div class="date-picker-container">
                        <input type="text" id="stopDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                    </div>
                </div>
            </div>
            <div>
                <label class="form-label" for="reaction-filter"><mvc:message code="workflow.Reaction" /></label>
                <select id="reaction-filter" multiple class="form-control" data-result-template="workflow-reaction-option" data-selection-template="workflow-reaction-option">
                    <c:forEach var="reactionType" items="${WorkflowReactionType.values()}">
                        <option value="${reactionType.name}">${reactionType.name}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
    </div>
</div>

<script id="status-cell" type="text/x-mustache-template">
    <span class="status-badge campaign.status.{{- value }}" data-tooltip="{{- t('workflow.status.' + value) }}"></span>
</script>

<script id="reaction-cell" type="text/x-mustache-template">
    <div class="text-truncate-table">
        {{ if (value) { }}
            {{ if (value.iconClass) { }}
                <i class="icon {{- value.iconClass}}"></i>
            {{ } }}
            {{ if (value.name) { }}
                <strong> {{- t('workflow.reaction.' + value.name)}}</strong>
            {{ } }}
        {{ } }}
    </div>
</script>

<script id="start-cell" type="text/x-mustache-template">
    {{ if (value) { }}
        {{ if (!['${EVENT_REACTION}', '${EVENT_DATE}'].includes(value.type)) { }}
            <span class="text-truncate-table">{{- AGN.Lib.DateFormat.formatAdminDateTime(value.date) }}</span>
        {{ } else { }}
            {{ const type = value.type === 'EVENT_REACTION' ? 'action_based' : 'date_based'; }}
            <span class="badge badge-campaign-start-{{- type }}">
                <i class="icon icon-{{- type === 'action_based' ? 'cog' : 'calendar-alt' }}"></i>
                <strong>{{- t('workflow.start.' + type) }}</strong>
            </span>
        {{ } }}
    {{ } }}
</script>

<script id="end-cell" type="text/x-mustache-template">
    {{ if (value) { }}
        <span class="text-truncate-table">
            {{ if (value.type === '${STOP_TYPE_AUTOMATIC}') { }}
                {{- t('workflow.stop.automatic_end') }}
            {{ } else { }}
                {{- AGN.Lib.DateFormat.formatAdminDateTime(value.date) }}
            {{ } }}
        </span>
    {{ } }}
</script>

<script id="workflow-delete-btn" type="text/x-mustache-template">
    <a href="{{= AGN.url('/workflow/bulkDelete.action?bulkIds=' + id) }}" type="button" class="icon-btn icon-btn--danger js-data-table-delete" data-tooltip="<mvc:message code="Delete" />">
        <i class="icon icon-trash-alt"></i>
    </a>
</script>

<script id="workflow-activate-btn" type="text/x-mustache-template">
    <a href="#" class="icon-btn icon-btn--primary" data-tooltip="${activateMsg}" data-action="activate-js" data-item-id="{{- id }}">
        <i class="icon icon-check-circle"></i>
    </a>
</script>

<script id="workflow-deactivate-btn" type="text/x-mustache-template">
    <a href="#" class="icon-btn icon-btn--danger" data-tooltip="${deactivateMsg}" data-action="deactivate-js" data-item-id="{{- id }}">
        <i class="icon icon-times-circle"></i>
    </a>
</script>

<script id="workflow-reaction-option" type="text/x-mustache-template">
    <span>{{- t('workflow.reaction.' + value) }}</span>
</script>

<script type="text/javascript">
  AGN.Opt.TableActionsConditions['activate-workflow'] = data => {
    return ['${STATUS_INACTIVE.name}', '${STATUS_OPEN.name}', '${STATUS_PAUSED.name}'].includes(data.status);
  };
  AGN.Opt.TableActionsConditions['deactivate-workflow'] = data => {
    return ['${STATUS_ACTIVE.name}', '${STATUS_TESTING.name}', '${STATUS_PAUSED.name}'].includes(data.status);
  };
</script>
