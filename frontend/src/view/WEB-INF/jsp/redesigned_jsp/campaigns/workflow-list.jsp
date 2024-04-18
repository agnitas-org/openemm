<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action"%>
<%@ page import="com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop.WorkflowEndType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.ComWorkflowForm"--%>
<%--@elvariable id="workflowsJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

<c:set var="STATUSES" value="<%= WorkflowForm.WorkflowStatus.values() %>"/>
<c:set var="REACTIONS" value="<%= Arrays.stream(WorkflowReactionType.values()).map(WorkflowReactionType::getName).collect(Collectors.toList()) %>"/>
<c:set var="STATUS_ACTIVE" value="<%= WorkflowStatus.STATUS_ACTIVE %>" scope="page"/>
<c:set var="STATUS_INACTIVE" value="<%= WorkflowStatus.STATUS_INACTIVE %>" scope="page"/>
<c:set var="STATUS_OPEN" value="<%= WorkflowStatus.STATUS_OPEN %>" scope="page"/>
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

<div class="filter-overview hidden" data-editable-view="${agnEditViewKey}">
    <div id="table-tile" class="tile js-data-table" data-table="workflow-list-table" data-controller="workflow-list" data-editable-tile="main">
        <script type="application/json" data-initializer="workflow-list">
            {
                "reactions": ${emm:toJson(REACTIONS)},
                "urls": {
                    "WORKFLOW_BULK_DELETE": "<c:url value="/workflow/bulkDelete.action"/>",
                    "WORKFLOW_BULK_DEACTIVATE": "<c:url value="/workflow/confirmBulkDeactivate.action"/>"
                }
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="default.Overview"/></h1>
        </div>

        <div class="tile-body">
            <div class="js-data-table-body" data-web-storage="workflow-overview"></div>
        </div>

        <c:forEach var="entry" items="${workflowsJson}">
            <c:url var="viewLink" value="/workflow/${entry['id']}/view.action"/>
            <c:set target="${entry}" property="show" value="${viewLink}"/>
            <emm:ShowByPermission token="workflow.delete">
                <c:url var="deleteLink" value="/workflow/bulkDelete.action">
                    <c:param name="bulkIds" value="${entry['id']}"/>
                </c:url>
                <c:set target="${entry}" property="delete" value="${deleteLink}"/>
            </emm:ShowByPermission>
        </c:forEach>

    </div>
    <script id="workflow-list-table" type="application/json">
        {
            "columns": [
                {
                    "field": "select",
                    "type": "bulkSelectColumn"
                },
                {
                    "headerName": "<mvc:message code='Status'/>",
                        "editable": false,
                        "field": "status",
                        "sortable": false,
                        "cellRenderer": "MustacheTemplateCellRender",
                        "cellRendererParams": {"templateName": "status-cell"},
                        "type": "setColumn"
                    },
                    {
                        "headerName": "<mvc:message code='default.Name'/>",
                        "editable": false,
                        "cellRenderer": "NotEscapedStringCellRenderer",
                        "field": "shortname"
                    },
                    {
                        "headerName": "<mvc:message code='Description'/>",
                        "editable": false,
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
                        "field": "delete",
                        "type": "deleteColumn"
                    }
                ],
                "data": ${workflowsJson}
        }
    </script>

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
                    <label class="form-label" for="status-filter"><mvc:message code="Status" /></label>
                    <select id="status-filter" multiple class="form-control">
                        <c:forEach var="status" items="${STATUSES}">
                            <c:if test="${status.name ne 'NONE'}">
                                <c:choose>
                                    <c:when test="${status.name eq 'failed'}">
                                        <option value="${status.name}">Failed</option>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="${status.name}"><mvc:message code="${status.messageKey}"/></option>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-12">
                    <label class="form-label" for="shortname-filter"><mvc:message code="Name"/></label>
                    <input type="text" id="shortname-filter" class="form-control"/>
                </div>
                <div class="col-12">
                    <label class="form-label" for="description-filter"><mvc:message code="Description"/></label>
                    <input type="text" id="description-filter" class="form-control"/>
                </div>
                <div class="col-12">
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
                        <div class="date-picker-container mb-1">
                            <input type="text" id="startDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                        </div>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="stopDate-from-filter"><mvc:message code="report.stopDate"/></label>
                    <select id="stopDate-filter" multiple class="form-control">
                        <option value="stopDate"><mvc:message code="Date"/></option>
                        <option value="automaticEnd"><mvc:message code="workflow.stop.AutomaticEnd"/></option>
                    </select>
                    <div class="mt-1" data-date-range data-show-by-select="#stopDate-filter" data-show-by-select-values="stopDate">
                        <div class="date-picker-container mb-1">
                            <input type="text" id="stopDate-from-filter" placeholder="<mvc:message code='From'/>" class="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container mb-1">
                            <input type="text" id="stopDate-to-filter" placeholder="<mvc:message code='To'/>" class="form-control js-datepicker"/>
                        </div>
                    </div>
                </div>
                <div class="col-12">
                    <label class="form-label" for="reaction-filter"><mvc:message code="workflow.Reaction" /></label>
                    <select id="reaction-filter" multiple class="form-control">
                        <%--options added with js--%>
                    </select>
                </div>
            </div>
        </div>
    </div>
</div>

<script id="status-cell" type="text/x-mustache-template">
    <div class="d-inline-block d-flex align-items-center gap-1">
        <span class="status-badge campaign.status.{{- value.name }}"></span>
        <span class="text-truncate">{{- t('workflow.status.' + value.name) }}</span>
    </div>
</script>

<script id="reaction-cell" type="text/x-mustache-template">
    <div class="text-truncate">
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
            {{- AGN.Lib.DateFormat.formatAdminDateTime(value.date) }}
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
        {{ if (value.type === '${STOP_TYPE_AUTOMATIC}') { }}
            {{- t('workflow.stop.automatic_end') }}
        {{ } else { }}
            {{- AGN.Lib.DateFormat.formatAdminDateTime(value.date) }}
        {{ } }}
    {{ } }}
</script>
