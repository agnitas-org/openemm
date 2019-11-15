<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl" %>
<%@ page import="com.agnitas.emm.core.workflow.web.ComWorkflowAction" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ page import="org.agnitas.target.TargetNode" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<c:set var="STATUS_NONE" value="<%= WorkflowStatus.STATUS_NONE %>" scope="page"/>
<c:set var="STATUS_OPEN" value="<%= WorkflowStatus.STATUS_OPEN %>" scope="page"/>
<c:set var="STATUS_ACTIVE" value="<%= WorkflowStatus.STATUS_ACTIVE %>" scope="page"/>
<c:set var="STATUS_INACTIVE" value="<%= WorkflowStatus.STATUS_INACTIVE %>" scope="page"/>
<c:set var="STATUS_COMPLETE" value="<%= WorkflowStatus.STATUS_COMPLETE %>" scope="page"/>
<c:set var="STATUS_TESTING" value="<%= WorkflowStatus.STATUS_TESTING %>" scope="page"/>
<c:set var="STATUS_TESTED" value="<%= WorkflowStatus.STATUS_TESTED %>" scope="page"/>
<c:set var="quote" value="'" />
<c:set var="quoteReplace" value="\\'" />
<c:set var="CHAIN_OPERATOR_AND" value="<%= TargetNode.CHAIN_OPERATOR_AND %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= TargetNode.CHAIN_OPERATOR_OR %>"/>
<c:set var="OPERATOR_IS" value="<%= TargetNode.OPERATOR_IS.getOperatorCode() %>"/>

<c:set var="FORWARD_TARGETGROUP_CREATE" value="<%=ComWorkflowAction.FORWARD_TARGETGROUP_CREATE_QB%>"/>
<c:set var="FORWARD_TARGETGROUP_EDIT" value="<%=ComWorkflowAction.FORWARD_TARGETGROUP_EDIT_QB%>"/>

<emm:instantiate var="mailingLists" type="java.util.LinkedHashMap">
    <c:forEach var="mailingList" items="${allMailinglists}">
        <c:set target="${mailingLists}" property="${mailingList.id}" value="${mailingList.shortname}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="targets" type="java.util.LinkedHashMap">
    <c:forEach var="targer" items="${allTargets}">
        <c:set target="${targets}" property="${targer.id}" value="${targer.targetName}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="mailings" type="java.util.LinkedHashMap">
    <c:forEach var="mailing" items="${allMailings}">
        <c:set target="${mailings}" property="${mailing.mailingID}" value="${fn:replace(mailing.shortname, quote, quoteReplace)}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="reports" type="java.util.LinkedHashMap">
    <c:forEach var="report" items="${allReports}">
        <c:set target="${reports}" property="${report.key}" value="${fn:escapeXml(report.value)}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="allForms" type="java.util.LinkedHashMap">
    <c:set target="${allForms}" property="0" value=""/>
    <c:forEach var="userForm" items="${allUserForms}">
        <c:set target="${allForms}" property="${userForm.id}" value="${userForm.formName}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="autoExports" type="java.util.LinkedHashMap">
    <c:set target="${autoExports}" property="0" value=""/>
    <c:forEach var="autoExport" items="${allAutoExports}" varStatus="index">
        <c:set target="${autoExports}" property="${autoExport.autoExportId}" value="${autoExport.shortname}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="allImports" type="java.util.LinkedHashMap">
    <c:set target="${allImports}" property="0" value=""/>
    <c:forEach var="autoImport" items="${allAutoImports}">
        <c:set target="${allImports}" property="${autoImport.autoImportId}" value="${autoImport.shortname}"/>
    </c:forEach>
</emm:instantiate>

<emm:instantiate var="allCampaigns" type="java.util.LinkedHashMap">
    <c:set target="${allCampaigns}" property="0" value=""/>
    <c:forEach var="archive" items="${campaigns}" >
        <c:set target="${allCampaigns}" property="${archive.id}" value="${archive.shortname}"/>
    </c:forEach>
</emm:instantiate>

<div data-controller="workflow-view-new">

    <script data-initializer="workflow-view-constants" type="application/json">
        {
            "startTypeOpen": "<%=WorkflowStart.WorkflowStartType.OPEN%>",
            "startTypeDate": "<%=WorkflowStart.WorkflowStartType.DATE%>",
            "startTypeEvent": "<%=WorkflowStart.WorkflowStartType.EVENT%>",
            "startEventReaction": "<%=WorkflowStart.WorkflowStartEventType.EVENT_REACTION%>",
            "startEventDate": "<%=WorkflowStart.WorkflowStartEventType.EVENT_DATE%>",
            "endTypeAutomatic": "<%=WorkflowStop.WorkflowEndType.AUTOMATIC %>",
            "endTypeDate": "<%=WorkflowStop.WorkflowEndType.DATE %>",
            "deadlineTypeDelay": "<%=WorkflowDeadline.WorkflowDeadlineType.TYPE_DELAY%>",
            "deadlineTypeFixedDeadline": "<%=WorkflowDeadline.WorkflowDeadlineType.TYPE_FIXED_DEADLINE%>",
            "deadlineTimeUnitMinute": "<%=WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE%>",
            "deadlineTimeUnitHour": "<%=WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR%>",
            "deadlineTimeUnitDay": "<%=WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_DAY%>",
            "deadlineTimeUnitWeek": "<%=WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_WEEK%>",
            "deadlineTimeUnitMonth": "<%=WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MONTH%>",
            "defaultImportDelayLimit" : "<%=WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT%>",
            "reactionOpened": "<%= WorkflowReactionType.OPENED %>",
            "reactionNotOpened": "<%= WorkflowReactionType.NOT_OPENED %>",
            "reactionClicked": "<%= WorkflowReactionType.CLICKED %>",
            "reactionNotClicked": "<%= WorkflowReactionType.NOT_CLICKED %>",
            "reactionBought": "<%= WorkflowReactionType.BOUGHT %>",
            "reactionNotBought": "<%= WorkflowReactionType.NOT_BOUGHT %>",
            "reactionDownload": "<%= WorkflowReactionType.DOWNLOAD %>",
            "reactionChangeOfProfile": "<%= WorkflowReactionType.CHANGE_OF_PROFILE %>",
            "reactionWaitingForConfirm": "<%= WorkflowReactionType.WAITING_FOR_CONFIRM %>",
            "reactionOptIn": "<%= WorkflowReactionType.OPT_IN %>",
            "reactionOptOut": "<%= WorkflowReactionType.OPT_OUT %>",
            "reactionClickedLink": "<%= WorkflowReactionType.CLICKED_LINK %>",
            "reactionOpenedAndClicked": "<%= WorkflowReactionType.OPENED_AND_CLICKED %>",
            "reactionOpenedOrClicked": "<%= WorkflowReactionType.OPENED_OR_CLICKED %>",
            "reactionConfirmedOptIn": "<%= WorkflowReactionType.CONFIRMED_OPT_IN %>",
            "decisionTypeDecision": "<%=WorkflowDecision.WorkflowDecisionType.TYPE_DECISION %>",
            "decisionTypeAutoOptimization": "<%=WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION %>",
            "decisionReaction": "<%= WorkflowDecision.WorkflowDecisionCriteria.DECISION_REACTION %>",
            "decisionProfileField": "<%= WorkflowDecision.WorkflowDecisionCriteria.DECISION_PROFILE_FIELD %>",
            "decisionAOCriteriaClickRate": "<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE %>",
            "decisionAOCriteriaOpenrate": "<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE %>",
            "decisionAOCriteriaTurnover": "<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE %>",
            "chainOperatorAnd": "${CHAIN_OPERATOR_AND}",
            "chainOperatorOr": "${CHAIN_OPERATOR_OR}",
            "operatorIs": "${OPERATOR_IS}",
            "forwardTargetGroupCreate": "${FORWARD_TARGETGROUP_CREATE}",
            "forwardTargetGroupEdit": "${FORWARD_TARGETGROUP_EDIT}",
            "forwardMailingCreate": "<%=ComWorkflowAction.FORWARD_MAILING_CREATE%>",
            "forwardMailingEdit": "<%=ComWorkflowAction.FORWARD_MAILING_EDIT%>",
            "forwardMailingCopy": "<%=ComWorkflowAction.FORWARD_MAILING_COPY%>",
            "forwardUserFormCreate": "<%=ComWorkflowAction.FORWARD_USERFORM_CREATE%>",
            "forwardUserFormEdit": "<%=ComWorkflowAction.FORWARD_USERFORM_EDIT%>",
            "forwardReportCreate": "<%=ComWorkflowAction.FORWARD_REPORT_CREATE%>",
            "forwardReportEdit": "<%=ComWorkflowAction.FORWARD_REPORT_EDIT%>",
            "forwardAutoExportCreate": "<%=ComWorkflowAction.FORWARD_AUTOEXPORT_CREATE%>",
            "forwardAutoExportEdit": "<%=ComWorkflowAction.FORWARD_AUTOEXPORT_EDIT%>",
            "forwardAutoImportCreate": "<%=ComWorkflowAction.FORWARD_AUTOIMPORT_CREATE%>",
            "forwardAutoImportEdit": "<%=ComWorkflowAction.FORWARD_AUTOIMPORT_EDIT%>",
            "forwardArchiveCreate": "<%=ComWorkflowAction.FORWARD_ARCHIVE_CREATE%>",
            "statusInactive":"${STATUS_INACTIVE}",
            "statusActive": "${STATUS_ACTIVE}",
            "genderOptions": {
                "<%= Recipient.GENDER_MALE %>": "Male",
                "<%= Recipient.GENDER_FEMALE %>": "Female",
                "<%= Recipient.GENDER_UNKNOWN %>": "Unknown"
            },
            "chainOperatorOptions": {
                "<%= TargetNode.CHAIN_OPERATOR_AND %>": "<bean:message key="default.and"/>",
                "<%= TargetNode.CHAIN_OPERATOR_OR %>": "<bean:message key="default.or"/>"
            },
            "operators": [
                 <c:forEach items="${operators}"  var="operator" varStatus="index">
                    <c:set var="types" value="${operatorsTypeSupportMap[operator]}"/>
                {
                    "id": "${operator.operatorCode}",
                    "text": "${operator.operatorSymbol}",
                    "data": {
                        "types": "${empty types ? '' : types}"
                    }
                }${!index.last ? ',':''}
                </c:forEach>
            ],
            "operatorsMap": {
                <c:forEach items="${operators}"  var="operator" varStatus="index">
                  "${operator.operatorCode}": "${operator.operatorSymbol}"${!index.last ? ',':''}
                </c:forEach>
            },
            "componentURL" : "<c:url value='/sc?compID={component-id}'/>"
        }
    </script>


    <c:set var="contextPath" value="${emm:absUrlPrefix(pageContext.request.contextPath)}" scope="page"/>
    <c:choose>
        <c:when test="${fn:startsWith(emmLayoutBase.imagesURL, contextPath)}">
            <c:set var="imagesUrl" value="${emm:absUrlPrefix(emmLayoutBase.imagesURL)}" scope="page"/>
            <c:set var="ifUsed" value="-- 1 --" scope="page"/>
        </c:when>
        <c:otherwise>
            <c:set var="imagesUrl" value="${pageContext.request.contextPath}/${emmLayoutBase.imagesURL}" scope="page"/>
            <c:set var="ifUsed" value="-- 2 --" scope="page"/>
        </c:otherwise>
    </c:choose>

    <script data-initializer="campaign-manager-init" type="application/json">
        {
            "icons":${workflowForm.workflowSchema},
            "editorPositionLeft": "${workflowForm.editorPositionLeft}",
            "editorPositionTop": "${workflowForm.editorPositionTop}",
            "resizeTimeoutId": "false",
            "localeDateNTimePattern": "${localeDateNTimePattern}",
            "pageContextSessionId": "${pageContext.session.id}",
            "newStatus": "${workflowForm.statusMaybeChangedTo}",
            "workflowId": "${workflowForm.workflowId}",
            "workflowUndoHistoryData":${workflowForm.workflowUndoHistoryData},
            "imageUrl": "${imagesUrl}",
            "enabledToggleButton": "${workflowToggleTestingButtonEnabled}",
            "isStatusOpen": "${workflowForm.status == STATUS_OPEN}",
            "shortName": "${workflowForm.shortname}",
            "emmLocal": "<bean:write name="emm.admin" property="adminLang" scope="session"/>",
            "isActivated": ${workflowForm.status == STATUS_ACTIVE || workflowForm.status == STATUS_TESTING},
            "workflowStatus": "${workflowForm.status}",
            "pdfGenerationUrl": "<html:rewrite page="/workflow/generatePDF.action"/>",
            "allMailings":${emm:toJson(mailings)},
            "allMailingLists":${emm:toJson(mailingLists)},
            "allTargets":${emm:toJson(targets)},
            "allReports":${emm:toJson(reports)},
            "allUserForms":${emm:toJson(allForms)},
            "allAutoExports" :${emm:toJson(autoExports)},
            "allAutoImports" :${emm:toJson(allImports)},
            "allCampaigns":${emm:toJson(allCampaigns)}
        }
    </script>


    <div id="activating-campaign-dialog" style=" visibility: hidden; display: none;">
        <div class="form-group">
            <div class="col-sm-12">
                <div class="well"><bean:message key="workflow.activating.question"/></div>
            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label">
                    <bean:message key="workflow.activating.mailings"/>
                </label>
            </div>
            <div class="col-sm-8">
                <label id="activating-campaign-mailings"></label>
            </div>
        </div>

        <hr>

        <div class="col-xs-12">
            <div class="form-group">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular"
                       onclick="jQuery('#activating-campaign-dialog').dialog('close'); return false;">
                        <bean:message key="button.Cancel"/>
                    </a>

                    <a href="#" class="btn btn-regular btn-primary" id="activating-campaign-activate-button">
                        <bean:message key="workflow.activating.title"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <script id="mailing-types-replace-modal" type="text/x-mustache-template">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                        <h4 class="modal-title">
                            <bean:message key="warning"/>
                        </h4>
                    </div>

                    <div class="modal-body">
                        <p><bean:message key="workflow.mailingTypesFix.question"/></p>
                    </div>

                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text">
                                    <bean:message key="button.Cancel"/>
                                </span>
                            </button>

                            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <span class="text">
                                    <bean:message key="button.Proceed"/>
                                </span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>

    <div id="inactivating-campaign-dialog" style=" visibility: hidden; display: none;">
        <div class="form-group">
            <div class="col-sm-12">
                <div class="well"><bean:message key="workflow.inactivating.question"/></div>
            </div>
        </div>

        <hr>

        <div class="col-xs-12">
            <div class="form-group">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular"
                       onclick="jQuery('#inactivating-campaign-dialog').dialog('close'); return false;">
                        <span><bean:message key="button.Cancel"/></span>
                    </a>

                    <a href="#" class="btn btn-regular btn-primary" id="inactivating-campaign-inactivate-button">
                        <bean:message key="workflow.inactivating.title"/>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div id="invisible">
        <div id="connectRapidButton">
            <c:url var="icon_arrow_rapid" value="/assets/core/images/campaignManager/icon_arrow_rapid.png"/>
            <img src="${icon_arrow_rapid}" alt="arrow">
        </div>
        <jsp:include page="editors/workflow-start-editor.jsp">
            <jsp:param name="adminTimezone" value="${adminTimezone}"/>
        </jsp:include>
        <jsp:include page="editors/workflow-decision-editor.jsp"/>
        <jsp:include page="editors/workflow-deadline-editor.jsp"/>
        <jsp:include page="editors/workflow-parameter-editor.jsp"/>
        <jsp:include page="editors/workflow-report-editor.jsp"/>
        <jsp:include page="editors/workflow-recipient-editor.jsp"/>
        <jsp:include page="editors/workflow-archive-editor.jsp"/>
        <jsp:include page="editors/workflow-form-editor.jsp"/>
        <jsp:include page="editors/workflow-ownworkflow-editor.jsp"/>
        <jsp:include page="editors/workflow-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-action-based-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-date-based-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-followup-based-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-import-editor.jsp"/>
        <jsp:include page="editors/workflow-export-editor.jsp"/>
        <jsp:include page="editors/workflow-icon-comment-editor.jsp"/>
        <jsp:include page="editors/workflow-ownworkflow-usecopy-dialog.jsp"/>
        <jsp:include page="workflow-save-before-pdf-dialog.jsp"/>
        <jsp:include page="workflow-copy-dialog-new.jsp">
            <jsp:param name="workflowId" value="${workflowForm.workflowId}"/>
        </jsp:include>
        <jsp:include page="workflow-undoHistoryIsEmpty-dialog.jsp"/>
        <jsp:include page="workflow-simple-dialog.jsp">
            <jsp:param name="messageKey" value="error.workflow.connection.notAllowed"/>
            <jsp:param name="titleKey" value="error.workflow.connection.deactivated"/>
            <jsp:param name="dialogName" value="NotAllowedConnection"/>
        </jsp:include>
        <jsp:include page="workflow-simple-dialog.jsp">
            <jsp:param name="messageKey" value="error.workflow.notAllowedSeveralConnections.description"/>
            <jsp:param name="titleKey" value="error.workflow.connection.deactivated"/>
            <jsp:param name="dialogName" value="NotAllowedSeveralConnections"/>
        </jsp:include>
        <jsp:include page="workflow-simple-dialog.jsp">
            <jsp:param name="messageKey" value="error.workflow.notAllowedEditing.description"/>
            <jsp:param name="titleKey" value="error.workflow.notAllowedEditing.title"/>
            <jsp:param name="dialogName" value="NotAllowedEditing"/>
        </jsp:include>
        <jsp:include page="workflow-simple-dialog.jsp">
            <jsp:param name="messageKey" value="error.workflow.noStatistics.description"/>
            <jsp:param name="titleKey" value="error.workflow.noStatistics.title"/>
            <jsp:param name="dialogName" value="noStatistics"/>
        </jsp:include>

        <c:if test="${workflowToggleTestingButtonEnabled}">
            <c:choose>
                <c:when test="${workflowToggleTestingButtonState}">
                    <!-- Start testing dialog -->
                    <jsp:include page="workflow-testing-dialog-new.jsp">
                        <jsp:param name="newStatus" value="${STATUS_TESTING}"/>
                        <jsp:param name="dialogMessage" value="workflow.test.start.question"/>
                        <jsp:param name="positiveButtonName" value="button.Start"/>
                    </jsp:include>
                </c:when>
                <c:otherwise>
                    <!-- Stop testing dialog -->
                    <jsp:include page="workflow-testing-dialog-new.jsp">
                        <jsp:param name="newStatus" value="${STATUS_OPEN}"/>
                        <jsp:param name="dialogMessage" value="workflow.test.stop.question"/>
                        <jsp:param name="positiveButtonName" value="default.Yes"/>
                    </jsp:include>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>

    <mvc:form servletRelativeAction="/workflow/save.action" cssClass="form-vertical" id="workflowForm"  modelAttribute="workflowForm" data-form="resource">
    <input type="hidden" name="method" value="save" id="action_method"/>
    <input type="hidden" name="workflowId" value="${workflowForm.workflowId}"/>
    <input type="hidden" name="schema" id="schema" value=""/>
    <input type="hidden" name="editorPositionTop" id="editorPositionTop" value=""/>
    <input type="hidden" name="editorPositionLeft" id="editorPositionLeft" value=""/>
    <input type="hidden" name="forwardName" id="forwardName" value=""/>
    <input type="hidden" name="forwardParams" id="forwardParams" value=""/>
    <input type="hidden" name="forwardTargetItemId" id="forwardTargetItemId" value=""/>
        <mvc:hidden path="workflowUndoHistoryData" />
        <mvc:hidden path="usingActivatedWorkflow"/>
        <mvc:hidden path="usingActivatedWorkflowName"/>
        <mvc:hidden path="partOfActivatedWorkflow"/>
        <mvc:hidden path="partOfActivatedWorkflowName"/>
    <input type="hidden" name="appName" id="appNameId" value="${pageContext.request.contextPath}"/>

    <div class="tile">
        <div class="tile-header">
            <a href="#" class="headline" data-toggle-tile="#tile-campaignInformation">
                <i class="tile-toggle icon icon-angle-up"></i>
                <bean:message key="campaignInformation"/>
            </a>
        </div>
        <div id="tile-campaignInformation" class="tile-content tile-content-forms" data-field="toggle-vis">
            <div class="row">
                <div class="col-sm-5 col-lg-4">
                    <div class="form-group">
                        <label for="name" class="form-label">
                            <bean:message key="workflow.view.shortname"/>
                        </label>
                        <mvc:text path="shortname" cssClass="form-control" id="name"/>
                    </div>
                </div>
                <div class="col-sm-5 col-lg-4">
                    <div class="form-group">
                        <label for="workflow_description" class="form-label">
                            <bean:message key="default.description"/>
                        </label>
                        <mvc:text path="description" cssClass="form-control" id="workflow_description"/>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group">
                        <label class="form-label block">
                            <bean:message key="workflow.status"/>
                        </label>
                        <b class="form-badge">
                            <bean:message key="${workflowForm.status.messageKey}"/>
                        </b>
                    </div>
                </div>

                <div class="clearfix hidden-lg"></div>

                <emm:ShowByPermission token="workflow.activate">
                    <div class="col-sm-4 col-lg-2">
                        <div class="form-group">
                            <c:if test="${workflowForm.statusMaybeChangedTo ne STATUS_NONE}">
                                <label for="workflow_active" class="form-label block">
                                    <bean:message key="workflow.view.setStatusTo"/>
                                    <b>
                                        <bean:message key="${workflowForm.statusMaybeChangedTo.messageKey}"/>
                                    </b>
                                </label>
                                <label class="toggle">
                                    <input id="workflow_active" data-action="workflow-view-change-status" name="status" value="${workflowForm.status}" ${workflowForm.status == STATUS_ACTIVE.name() ? 'checked="checked"':''}  type="checkbox"/>
                                    <div class="toggle-control"></div>
                                </label>
                            </c:if>
                            <input id="workflow-status" type="hidden" name="__STRUTS_CHECKBOX_workflow.statusString" value="${workflowForm.status}"/>
                        </div>
                    </div>
                </emm:ShowByPermission>


                <emm:HideByPermission token="workflow.activate">
                    <div class="col-sm-12">
                        <div class="form-group">
                            <div class="well block">
                                <bean:message key="workflow.activate.info"/>
                                <!--<b><i class="icon icon-phone"></i> upload.view.phone </b>-->
                            </div>
                        </div>
                    </div>
                </emm:HideByPermission>
            </div>
            <!-- Row END -->

        </div>
        <!-- Tile Content END -->

    </div>
    <!-- Tile END -->


    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <bean:message key="workflow.editor"/>
            </h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><bean:message key="export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li>
                            <a href="#" tabindex="-1" data-action="workflow-generate-pdf">
                                <i class="icon icon-file-pdf-o"></i>
                                <bean:message key="workflow.pdf.tooltip"/>
                            </a>
                        </li>
                    </ul>
                </li>
                <li>
                    <a data-action="campaignEditorEnlarge" href="#" data-modal="modal-editor"
                       data-tooltip="<bean:message key='editor.enlargeEditor'/>" class="dropdown-toggle">
                        <i class="icon icon-arrows-alt"></i>
                    </a>
                </li>
            </ul>
        </div>

        <div class="tile-content" id="pageCampaignEditorContainer">
            <div style="width: 100%;" id="campaignEditorBody">
                <div class="unselectable" id="toolbarTop" style="width: auto">
                    <div id="toolbarCross">
                        <div id="toolbarTopName"><bean:message key="workflow.panel.icons"/>:</div>
                        <div id="toolbarBottomName"><bean:message key="Templates"/>:</div>
                    </div>

                    <div class="iconPanel">
                        <div class="actionPanelTitle">
                            <bean:message key="workflow.process"/>
                        </div>

                        <div id="startButton" class="toolbarButton draggableButton" type="start"
                             title="<bean:message key="workflow.icon.start"/>"></div>
                        <div id="decisionButton" class="toolbarButton draggableButton" type="decision"
                             title="<bean:message key="workflow.decision"/>"></div>
                        <div id="parameterButton" class="toolbarButton draggableButton" type="parameter"
                             title="<bean:message key="workflow.icon.parameter"/>"></div>

                        <div style="clear: left"></div>

                        <div id="arrowButton" class="toolbarButton" title="<bean:message key="workflow.icon.chaining"/>"></div>
                        <div id="deadlineButton" class="toolbarButton draggableButton" type="deadline" title="<bean:message key="workflow.icon.deadline"/>"></div>

                        <%@include file="fragments/workflow-view-birt-report-icon.jspf" %>

                    </div>

                    <div class="iconPanel">

                        <div class="actionPanelTitle">
                            <bean:message key="Recipient"/>
                        </div>

                        <div id="recipientButton" class="toolbarButton draggableButton" type="recipient" title="<bean:message key="Recipient"/>"></div>
                        <div style="clear: left"></div>
                        <%@include file="fragments/workflow-view-auto-import-icon.jspf" %>

                        <%@include file="fragments/workflow-view-auto-export-icon.jspf" %>
                    </div>

                    <div class="iconPanel">
                        <div class="actionPanelTitle">
                            <bean:message key="workflow.panel.mailings"/>
                        </div>

                        <div id="mailingButton" class="toolbarButton draggableButton" type="mailing" title="<bean:message key="workflow.icon.mailing"/>"></div>
                        <div id="actionbasedMailingButton" class="toolbarButton draggableButton" type="actionbased_mailing" title="<bean:message key="mailing.action.based.mailing"/>"></div>
                        <div id="archiveButton" class="toolbarButton draggableButton" type="archive" title="<bean:message key="mailing.archive"/>"></div>

                        <div style="clear: left"></div>

                        <div id="datebasedMailingButton" class="toolbarButton draggableButton" type="datebased_mailing" title="<bean:message key="mailing.Rulebased_Mailing"/>"></div>

                        <%@include file="fragments/workflow-view-followup-button.jspf" %>
                    </div>

                    <div class="actionPanel">
                        <div class="actionPanelTool">
                            <div class="actionPanelTitle">
                                <bean:message key="workflow.autoLayout"/>:
                            </div>
                            <div id="autoLayout" class="toolbarButton toolbarButtonLeft" title="<bean:message key='workflow.doAutoLayout'/>"></div>
                        </div>

                        <div id="zoomTool" class="actionPanelTool">
                            <div class="actionPanelTitle"><bean:message key="workflow.panel.zoom"/></div>
                            <div id="zoomToolContent">
                                <div id="zoomMin" class="toolbarButton unselectable-text">-</div>
                                <div id="zoomMiddle" class="toolbarButton unselectable-text">0</div>
                                <div id="zoomMax" class="toolbarButton unselectable-text">+</div>
                                <div id="sliderContainer">
                                    <div id="slider" class="full-width"></div>
                                </div>
                            </div>
                        </div>

                        <div class="actionPanelTool">
                            <div class="actionPanelTitle"><bean:message key="workflow.panel.undo"/></div>
                            <div id="undoButtonWrapper">
                                <div id="undoButton" class="toolbarButton"></div>
                                <div id="undoButtonFake" class="toolbarButtonWithoutHover"></div>
                            </div>
                        </div>

                        <emm:ShowByPermission token="workflow.edit">
                            <div class="actionPanelTool">
                                <div class="actionPanelTitle"><bean:message key="button.Delete"/></div>
                                <div id="deleteButton" class="toolbarButton"></div>
                            </div>
                        </emm:ShowByPermission>
                    </div>

                    <div class="actionPanelOpenButton">
                        <div class="dropdown">
                            <div id="actionPanelOpenButton" class="toolbarButton" data-toggle="dropdown" title="<bean:message key="MoreComponents"/>">
                                <i class="icon icon-angle-double-right"></i>
                            </div>

                            <ul class="dropdown-menu">
                                <li>
                                    <button type="button" id="autoLayoutItem">
                                        <i class="icon icon-th"></i>
                                        <bean:message key="workflow.autoLayout"/>
                                    </button>
                                </li>

                                <li class="dropdown-header">
                                    <i class="icon icon-search"></i>
                                    <bean:message key="workflow.panel.zoom"/>
                                </li>

                                <li>
                                    <p>
                                        <button type="button" class="btn btn-regular" id="zoomMinItem">
                                            <i class="icon icon-search-minus"></i>
                                        </button>
                                        <button type="button" class="btn btn-regular" id="zoomMiddleItem">
                                            <strong>0</strong>
                                        </button>
                                        <button type="button" class="btn btn-regular" id="zoomMaxItem">
                                            <i class="icon icon-search-plus"></i>
                                        </button>
                                    </p>
                                </li>

                                <li>
                                    <button type="button" id="undoItem">
                                        <i class="icon icon-reply"></i>
                                        <bean:message key="workflow.panel.undo"/>
                                    </button>
                                </li>

                                <emm:ShowByPermission token="workflow.edit">
                                    <li>
                                            <%-- Disabled by default — initially no icon is selected --%>
                                        <button type="button" id="deleteItem" disabled="disabled">
                                            <i class="icon icon-trash-o"></i>
                                            <bean:message key="button.Delete"/>
                                        </button>
                                    </li>
                                </emm:ShowByPermission>
                            </ul>
                        </div>
                    </div>
                </div>
                <div style="width: 100%; display: table; position: relative; table-layout: fixed;">
                    <div class="unselectable" id="toolbarLeft">
                        <div class="actionPanelTitle">
                            <bean:message key="workflow.sampleCampaign"/>:
                        </div>
                        <%@include file="fragments/workflow-view-auto-optimization-icon.jspf" %>

                        <div id="scDOIButton" class="toolbarButton toolbarButtonLeft draggableButton" type="scDOI" title="<bean:message key='workflow.icon.DOI'/>"></div>
                        <div id="scDOIButtonLabel" class="leftMenuLabel"><bean:message key="recipient.DOI"/></div>

                        <div id="scBirthdayButton" class="toolbarButton toolbarButtonLeft draggableButton" type="scBirthday" title="<bean:message key='workflow.icon.birthday'/>"></div>
                        <div class="leftMenuLabel"><bean:message key="workflow.sampleCampaign.birthday"/></div>

                        <div class="leftMenuSeparator"></div>

                        <div class="actionPanelTitle">
                            <bean:message key="workflow.ownCampaign"/>:
                        </div>
                        <div id="ownWorkflowButton" class="toolbarButton toolbarButtonLeft draggableButton" type="ownWorkflow" title="<bean:message key='workflow.ownCampaign'/>"></div>
                    </div>
                    <div id="viewPort"></div>
                </div>
            </div>
        </div>
        <!-- Tile Content END -->

        <script data-initializer="open-edit-icon-initializer" type="application/json">
            {
                "nodeId": "${nodeId}",
                "elementValue": "${elementValue}"
            }
        </script>
    </div>
    <!-- Tile END -->

    <c:if test="${not isMailtrackingActive}">
    <div class="tile">
        <div class="tile-notification tile-notification-info">
            <span><bean:message key="workflow.info.noMailtracking"/></span>
        </div>
    </div>
    </c:if>

    <script type="text/javascript">
        <%--//fix problem with not expected behavior of JSON.stringify() for arrays--%>
        if (window.Prototype) {
            delete Array.prototype.toJSON;
        }

    </script>
    </mvc:form>

    <div id="icon-label-popup-holder"></div>

<%@include file="fragments/workflow-view-modal-editor-template.jspf" %>
