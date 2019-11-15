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

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.ComWorkflowForm"--%>

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

<html:form action="/workflow">
    <input type="hidden" name="method" value="list"/>

    <html:hidden property="numberOfRowsChanged"/>

    <c:set var="rowsNum" value="${workflowForm.numberOfRows}"/>
    <!-- Tile BEGIN -->
    <div class="tile">

        <!-- Tile Header BEGIN -->
        <div class="tile-header">
            <h2 class="headline"><bean:message key="default.Overview"/></h2>

            <!-- Tile Header Actions BEGIN -->
            <ul class="tile-header-actions">
            	<emm:ShowByPermission token="workflow.delete">
                	<li class="dropdown">
                    	<a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        	<i class="icon icon-pencil"></i>
                        	<span class="text"><bean:message key="bulkAction"/></span>
                        	<i class="icon icon-caret-down"></i>
                    	</a>
                    	<ul class="dropdown-menu">
                        	<li>
                            	<a href="#" data-form-set="method: ${ACTION_BULK_CONFIRM_DELETE}" data-form-confirm><bean:message key="bulkAction.delete.workflow"/></a>
                        	</li>
                        	<li>
                            	<a href="#" data-form-set="method: ${ACTION_BULK_CONFIRM_DEACTIVATE}" data-form-confirm><bean:message key="bulkAction.deactivate.workflow"/></a>
                        	</li>
                    	</ul>
                	</li>
				</emm:ShowByPermission>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-eye"></i>
                        <span class="text">
                            <bean:message key="button.Show"/>
                        </span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><bean:message key="listSize"/></li>
                        <li>
                            <label class="label">
                                <html:radio property="numberOfRows" value="20"/>
                                <span class="label-text">20</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="50"/>
                                <span class="label-text">50</span>
                            </label>
                            <label class="label">
                                <html:radio property="numberOfRows" value="100"/>
                                <span class="label-text">100</span>
                            </label>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <p>
                                <button class="btn btn-block btn-secondary btn-regular" type="button" data-form-change data-form-submit>
                                    <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.Show"/></span>
                                </button>
                            </p>
                            <logic:iterate collection="${workflowForm.columnwidthsList}" indexId="i" id="width">
                                <html:hidden property="columnwidthsList[${i}]"/>
                            </logic:iterate>
                        </li>
                    </ul>
                </li>
            </ul>
            <!-- Tile Header Actions END -->
        </div>
        <!-- Tile Header END -->

        <!-- Tile Content BEGIN -->
        <div class="tile-content" data-form-content="">
            <script type="application/json" data-initializer="web-storage-persist">
                {
                    "workflow-overview": {
                        "rows-count": ${workflowForm.numberOfRows}
                    }
                }
            </script>

            <!-- Table BEGIN -->
            <div class="table-wrapper">

                <display:table
                    class="table table-bordered table-striped table-hover js-table"
                    export="false"
                    id="workflow"
                    name="workflows"
                    pagesize="${workflowForm.numberOfRows}"
                    sort="list"
                    requestURI="/workflow.do?method=list&numberOfRows=${rowsNum}&__fromdisplaytag=true"
                    excludedParams="*">

                    <display:column sortable="false" title="<input type='checkbox' data-form-bulk='bulkID'/>" headerClass="squeeze-column">
                        <html:checkbox property="bulkID[${workflow.workflowId}]"></html:checkbox>
                    </display:column>

                    <display:column titleKey="Status" sortable="true">
                        <c:choose>
                            <c:when test="${workflow.status == STATUS_ACTIVE}">
                                <c:set var="reactionKey" value="default.status.active"/>
                                <c:set var="statusName" value="active"/>
                            </c:when>
                            <c:when test="${workflow.status == STATUS_INACTIVE}">
                                <c:set var="reactionKey" value="workflow.view.status.inActive"/>
                                <c:set var="statusName" value="inactive"/>
                            </c:when>
                            <c:when test="${workflow.status == STATUS_OPEN}">
                                <c:set var="reactionKey" value="workflow.view.status.open"/>
                                <c:set var="statusName" value="open"/>
                            </c:when>
                            <c:when test="${workflow.status == STATUS_COMPLETE}">
                                <c:set var="reactionKey" value="workflow.view.status.complete"/>
                                <c:set var="statusName" value="completed"/>
                            </c:when>
                            <c:when test="${workflow.status == STATUS_TESTING}">
                                <c:set var="reactionKey" value="workflow.view.status.testing"/>
                                <c:set var="statusName" value="testing"/>
                            </c:when>
                            <c:when test="${workflow.status == STATUS_TESTED}">
                                <c:set var="reactionKey" value="workflow.view.status.tested"/>
                                <c:set var="statusName" value="tested"/>
                            </c:when>
                        </c:choose>
                        <span class="status-badge campaign.status.${statusName}"></span>
                        <bean:message key="${reactionKey}"/>
                    </display:column>

                    <display:column sortProperty="shortname" titleKey="default.Name" sortable="true">
                        <span class="multiline-min-sm-150 multiline-min-md-250 multiline-min-lg-350">${workflow.shortname}</span>
                    </display:column>

                    <display:column sortProperty="description" titleKey="Description" sortable="true">
                        <span class="multiline-sm-150 multiline-md-250">${workflow.description}</span>
                    </display:column>

                    <display:column titleKey="Start" sortable="true">
                        <c:choose>
                            <c:when test="${workflow.generalStartEvent == EVENT_REACTION}">
                                <span class="badge badge-campaigntype-actionbased">
                                    <i class="icon icon-gear"></i>
                                    <strong><bean:message key="workflowlist.actionBased"/></strong>
                                </span>
                            </c:when>
                            <c:when test="${workflow.generalStartEvent == EVENT_DATE}">
                                <span class="badge badge-campaigntype-datebased">
                                    <i class="icon icon-calendar-o"></i>
                                    <strong><bean:message key="mailing.date"/></strong>
                                </span>
                            </c:when>
                            <c:otherwise>
                                <fmt:formatDate value="${workflow.generalStartDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" />
                            </c:otherwise>
                        </c:choose>
                    </display:column>

                    <display:column titleKey="report.stopDate" sortable="true">
                        <c:if test="${workflow.generalEndDate == null}">
                            <c:if test="${workflow.endType == STOP_TYPE_AUTOMATIC}">
                                <bean:message key="workflow.stop.AutomaticEnd"/>
                            </c:if>
                            <c:if test="${workflow.endType == STOP_TYPE_DATE}">
                                <bean:message key="workflow.stop.Open"/>
                            </c:if>
                        </c:if>
                        <c:if test="${workflow.generalEndDate != null}">
                            <fmt:formatDate value="${workflow.generalEndDate}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}" />
                        </c:if>
                    </display:column>

                    <display:column titleKey="workflow.Reaction" sortable="true" headerClass="squeeze-column">
                        <c:if test="${not empty workflow.generalStartReaction}">
                            <span class="badge badge-campaign-reactiontype">
                                <c:choose>
                                    <c:when test="${workflow.generalStartReaction == REACTION_OPENED}">
                                        <c:set var="iconClass" value="icon-eye"/>
                                        <c:set var="reactionKey" value="statistic.opened"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_NOT_OPENED}">
                                        <c:set var="iconClass" value="icon-eye-slash"/>
                                        <c:set var="reactionKey" value="workflow.reaction.NotOpened"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_CLICKED}">
                                        <c:set var="iconClass" value="icon-paper-plane"/>
                                        <c:set var="reactionKey" value="default.clicked"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_NOT_CLICKED}">
                                        <c:set var="iconClass" value="icon-times"/>
                                        <c:set var="reactionKey" value="workflow.reaction.NotClicked"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_BOUGHT}">
                                        <c:set var="iconClass" value="icon-shopping-cart"/>
                                        <c:set var="reactionKey" value="workflow.reaction.Bought"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_NOT_BOUGHT}">
                                        <c:set var="iconClass" value="icon-minus"/>
                                        <c:set var="reactionKey" value="workflow.reaction.NotBought"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_DOWNLOAD}">
                                        <c:set var="iconClass" value="icon-download"/>
                                        <c:set var="reactionKey" value="button.Download"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_CHANGE_OF_PROFILE}">
                                        <c:set var="iconClass" value="icon-exchange"/>
                                        <c:set var="reactionKey" value="workflow.reaction.ChangeOfProfile"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_WAITING_FOR_CONFIRM}">
                                        <c:set var="iconClass" value="icon-history"/>
                                        <c:set var="reactionKey" value="workflow.reaction.WaitingForConfirm"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_OPT_IN}">
                                        <c:set var="iconClass" value="icon-sign-in"/>
                                        <c:set var="reactionKey" value="workflow.reaction.OptIn"/>
                                    </c:when>
                                    <c:when test="${workflow.generalStartReaction == REACTION_OPT_OUT}">
                                        <c:set var="iconClass" value="icon-sign-out"/>
                                        <c:set var="reactionKey" value="workflow.reaction.OptOut"/>
                                    </c:when>
                                </c:choose>
                                <i class="icon ${iconClass}"></i>
                                <strong><bean:message key="${reactionKey}"/></strong>
                            </span>
                        </c:if>
                    </display:column>

                    <display:column class="table-actions" headerClass="squeeze-column">
                        <html:link styleClass="js-row-show hidden" titleKey="settings.admin.edit" page="/workflow.do?method=view&workflowId=${workflow.workflowId}">
                            <i class="icon icon-pencil"></i>
                        </html:link>
						<emm:ShowByPermission token="workflow.delete">
                        	<c:set var="campaignDeleteMessage" scope="page">
                            	<bean:message key="button.Delete"/>
                        	</c:set>
                        
                        	<agn:agnLink class="btn btn-regular btn-alert js-row-delete"
                                     data-tooltip="${campaignDeleteMessage}"
                                     page="/workflow.do?method=delete&workflowId=${workflow.workflowId}&fromListPage=true">
                            	<i class="icon icon-trash-o"></i>
                        	</agn:agnLink>
                        </emm:ShowByPermission>
                    </display:column>
                </display:table>

            </div>
            <!-- Table END -->

        </div>
        <!-- Tile Content END -->
    </div>
    <!-- Tile END -->
</html:form>

<style type="text/css">
    .badge-campaigntype-actionbased {
        background-color: #0071b9;
    }
    .badge-campaigntype-datebased {
        background-color: #33b0b8;
    }
</style>
