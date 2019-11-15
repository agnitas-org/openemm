<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do"%>
<%@ page import="com.agnitas.emm.core.workflow.beans.Workflow.WorkflowStatus" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop.WorkflowEndType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.ComWorkflowForm"--%>
<%--@elvariable id="workflowsJson" type="net.sf.json.JSONArray"--%>
<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>

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

<div class="tile js-data-table" data-sizing="container" data-table="workflow-list-table" data-controller="workflow-list">
    <script type="application/json" data-initializer="workflow-list">
        {
            "urls": {
                "WORKFLOW_BULK_DELETE": "<c:url value="/workflow/confirmBulkDelete.action"/>",
                "WORKFLOW_BULK_DEACTIVATE": "<c:url value="/workflow/confirmBulkDeactivate.action"/>"
            }
        }
    </script>

    <div class="tile-header" data-sizing="top">
        <h2 class="headline"><mvc:message code="default.Overview"/></h2>

        <ul class="tile-header-actions">
            <emm:ShowByPermission token="workflow.delete">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-pencil"></i>
                        <span class="text"><mvc:message code="bulkAction"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="#" data-action="bulk-delete"><mvc:message code="bulkAction.delete.workflow"/></a>
                        </li>
                        <li>
                            <a href="#" data-action="bulk-deactivate"><mvc:message code="bulkAction.deactivate.workflow"/></a>
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
                        <label class="label js-data-table-paginate" data-page-size="20" data-table-body=".js-data-table-body" data-web-storage="workflow-overview">
                            <span class="label-text">20</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="50" data-table-body=".js-data-table-body" data-web-storage="workflow-overview">
                            <span class="label-text">50</span>
                        </label>
                        <label class="label js-data-table-paginate" data-page-size="100" data-table-body=".js-data-table-body" data-web-storage="workflow-overview">
                            <span class="label-text">100</span>
                        </label>
                    </li>
                </ul>
            </li>
        </ul>
    </div>

    <div class="tile-content" data-sizing="scroll">
        <div class="js-data-table-body" data-web-storage="workflow-overview" style="height: 100%;"></div>
    </div>

    <c:forEach var="entry" items="${workflowsJson}">
        <c:url var="viewLink" value="/workflow/${entry['id']}/view.action"/>
        <c:set target="${entry}" property="show" value="${viewLink}"/>

        <c:url var="deleteLink" value="/workflow/${entry['id']}/confirmDelete.action"/>
        <c:set target="${entry}" property="delete" value="${deleteLink}"/>
    </c:forEach>

    <script id="workflow-list-table" type="application/json">
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
                    "width": 20,
                    "cellAction": "select"
                },
                {
                    "headerName": "<mvc:message code='Status'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "status",
                    "cellRenderer": "WorkflowStatusCellRenderer"
                },
                {
                    "headerName": "<mvc:message code='default.Name'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "shortname"
                },
                {
                    "headerName": "<mvc:message code='Description'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "description"
                },
                {
                    "headerName": "<mvc:message code='Start'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "startDate",
                    "cellRenderer": "WorkflowStartCellRenderer",
                    "cellRendererParams": { "adminDateTimeFormat": "${fn:toUpperCase(adminDateTimeFormat)}" }
                },
                {
                    "headerName": "<mvc:message code='report.stopDate'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "stopDate",
                    "cellRenderer": "WorkflowEndCellRenderer",
                    "cellRendererParams": { "adminDateTimeFormat": "${fn:toUpperCase(adminDateTimeFormat)}" }
                },
                {
                    "headerName": "<mvc:message code='workflow.Reaction'/>",
                    "editable": false,
                    "cellAction": "goTo",
                    "field": "reaction",
                    "cellRenderer": "WorkflowReactionCellRenderer"
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
            "data": ${workflowsJson}
        }
    </script>
</div>

<style type="text/css">
    .badge-campaigntype-actionbased {
        background-color: #0071b9;
    }
    .badge-campaigntype-datebased {
        background-color: #33b0b8;
    }
</style>
