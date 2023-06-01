<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ page import="org.agnitas.target.ConditionalOperator" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
<%--delete after GWUA-4957 has been successfully tested--%>
<%--@elvariable id="accessLimitTargetId" type="java.lang.Integer"--%>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>
<c:url var="iconArrowSrc" value="/assets/core/images/campaignManager/icon_arrow_rapid.png"/>

<c:set var="STATUS_NONE" value="<%= WorkflowStatus.STATUS_NONE %>" scope="page"/>
<c:set var="STATUS_OPEN" value="<%= WorkflowStatus.STATUS_OPEN %>" scope="page"/>
<c:set var="STATUS_ACTIVE" value="<%= WorkflowStatus.STATUS_ACTIVE %>" scope="page"/>
<c:set var="STATUS_INACTIVE" value="<%= WorkflowStatus.STATUS_INACTIVE %>" scope="page"/>
<c:set var="STATUS_COMPLETE" value="<%= WorkflowStatus.STATUS_COMPLETE %>" scope="page"/>
<c:set var="STATUS_TESTING" value="<%= WorkflowStatus.STATUS_TESTING %>" scope="page"/>
<c:set var="STATUS_TESTED" value="<%= WorkflowStatus.STATUS_TESTED %>" scope="page"/>
<c:set var="quote" value="'" />
<c:set var="quoteReplace" value="\\'" />
<c:set var="CHAIN_OPERATOR_AND" value="<%= ChainOperator.AND.getOperatorCode() %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= ChainOperator.OR.getOperatorCode() %>"/>
<c:set var="OPERATOR_IS" value="<%= ConditionalOperator.IS.getOperatorCode() %>"/>

<c:set var="FORWARD_TARGETGROUP_CREATE" value="<%= WorkflowController.FORWARD_TARGETGROUP_CREATE_QB%>"/>
<c:set var="FORWARD_TARGETGROUP_EDIT" value="<%= WorkflowController.FORWARD_TARGETGROUP_EDIT_QB%>"/>

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

<script>
    (function() {
      window.addEventListener('wheel', function(e) { if (e.ctrlKey == true) {e.preventDefault();}}, { passive: false });
      window.addEventListener('mousewheel', function(e) { if (e.ctrlKey == true) {e.preventDefault();}}, { passive: false });
      window.addEventListener('DOMMouseScroll', function(e) { if (e.ctrlKey == true) {e.preventDefault();}}, { passive: false });
    })();
</script>

<div data-controller="workflow-view">

    <emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>

    <script type="application/json" data-initializer="workflow-view">
        {
            "icons": ${workflowForm.workflowSchema},
            "workflowId": ${workflowForm.workflowId},
            "shortname": "${workflowForm.shortname}",
            "constants": {
	            "startTypeOpen": "<%= WorkflowStart.WorkflowStartType.OPEN %>",
	            "startTypeDate": "<%= WorkflowStart.WorkflowStartType.DATE %>",
	            "startTypeEvent": "<%= WorkflowStart.WorkflowStartType.EVENT %>",
	            "startEventReaction": "<%= WorkflowStart.WorkflowStartEventType.EVENT_REACTION %>",
	            "startEventDate": "<%= WorkflowStart.WorkflowStartEventType.EVENT_DATE %>",
	            "endTypeAutomatic": "<%= WorkflowStop.WorkflowEndType.AUTOMATIC %>",
	            "endTypeDate": "<%= WorkflowStop.WorkflowEndType.DATE %>",
	            "deadlineTypeDelay": "<%= WorkflowDeadline.WorkflowDeadlineType.TYPE_DELAY %>",
	            "deadlineTypeFixedDeadline": "<%= WorkflowDeadline.WorkflowDeadlineType.TYPE_FIXED_DEADLINE %>",
	            "deadlineTimeUnitMinute": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE %>",
	            "deadlineTimeUnitHour": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR %>",
	            "deadlineTimeUnitDay": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_DAY %>",
	            "deadlineTimeUnitWeek": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_WEEK %>",
	            "deadlineTimeUnitMonth": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MONTH %>",
	            "defaultImportDelayLimit" : "<%= WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT %>",
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
	            "decisionTypeAutoOptimization": "<%= WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION %>",
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
	            "forwardMailingCreate": "<%= WorkflowController.FORWARD_MAILING_CREATE %>",
	            "forwardMailingEdit": "<%= WorkflowController.FORWARD_MAILING_EDIT %>",
	            "forwardMailingCopy": "<%= WorkflowController.FORWARD_MAILING_COPY %>",
	            "forwardUserFormCreate": "<%= WorkflowController.FORWARD_USERFORM_CREATE %>",
	            "forwardUserFormEdit": "<%= WorkflowController.FORWARD_USERFORM_EDIT %>",
	            "forwardReportCreate": "<%= WorkflowController.FORWARD_REPORT_CREATE %>",
	            "forwardReportEdit": "<%= WorkflowController.FORWARD_REPORT_EDIT %>",
	            "forwardAutoExportCreate": "<%= WorkflowController.FORWARD_AUTOEXPORT_CREATE %>",
	            "forwardAutoExportEdit": "<%= WorkflowController.FORWARD_AUTOEXPORT_EDIT %>",
	            "forwardAutoImportCreate": "<%= WorkflowController.FORWARD_AUTOIMPORT_CREATE %>",
	            "forwardAutoImportEdit": "<%= WorkflowController.FORWARD_AUTOIMPORT_EDIT %>",
	            "forwardArchiveCreate": "<%= WorkflowController.FORWARD_ARCHIVE_CREATE %>",
	            "statusInactive":"${STATUS_INACTIVE}",
	            "statusActive": "${STATUS_ACTIVE}",
	            "statusTesting": "${STATUS_TESTING}",
	            "statusOpen": "${STATUS_OPEN}",
	            "genderOptions": {
	                "<%= Recipient.GENDER_MALE %>": "Male",
	                "<%= Recipient.GENDER_FEMALE %>": "Female",
	                "<%= Recipient.GENDER_UNKNOWN %>": "Unknown"
	            },
	            "targetOptions": {
	                "<%= WorkflowRecipient.WorkflowTargetOption.ALL_TARGETS_REQUIRED %>": "∩",
	                "<%= WorkflowRecipient.WorkflowTargetOption.NOT_IN_TARGETS %>": "≠",
	                "<%= WorkflowRecipient.WorkflowTargetOption.ONE_TARGET_REQUIRED %>": "∪"
	            },
	            "chainOperatorOptions": {
	                "<%= ChainOperator.AND.getOperatorCode() %>": "<bean:message key="default.and"/>",
	                "<%= ChainOperator.OR.getOperatorCode() %>": "<bean:message key="default.or"/>"
	            },
	            "operators": [
	                 <c:forEach items="${operators}"  var="operator" varStatus="index">
	                    <c:set var="types" value="${operatorsTypeSupportMap[operator]}"/>
	                {
	                    "id": "${operator.operatorCode}",
	                    "text": "${operator.eqlSymbol}",
	                    "data": {
	                        "types": "${empty types ? '' : types}"
	                    }
	                }${!index.last ? ',':''}
	                </c:forEach>
	            ],
	            "operatorsMap": {
	                <c:forEach items="${operators}"  var="operator" varStatus="index">
	                  "${operator.operatorCode}": "${operator.eqlSymbol}"${!index.last ? ',':''}
	                </c:forEach>
	            },
                "imagePath": "${absoluteImagePath}/campaignManager/",
                "initialWorkflowStatus": "${workflowForm.status}",
                "localeDateTimePattern": "${localeDateTimePattern}",
                "isAltgExtended" : ${isExtendedAltgEnabled}
	        },
            "accessLimitTargetId": ${accessLimitTargetId}
        }
    </script>

    <script id="workflow-node" type="text/x-mustache-template">
        <%-- Toggle 'active' class to toggle active/inactive node images --%>
        <div class="node" rel="popover">
            <img class="node-image inactive-node-image" src="${absoluteImagePath}/campaignManager/{{- icons.inactive }}"/>
            <img class="node-image active-node-image" src="${absoluteImagePath}/campaignManager/{{- icons.active }}"/>

            <div class="icon-overlay-title"></div>
            <div class="icon-overlay-image"><img/></div>

            <div class="node-connect-button">
                <img src="${iconArrowSrc}" alt="arrow">
            </div>
        </div>
    </script>

    <script id="workflow-draggable-node" type="text/x-mustache-template">
        <div class="draggable-node" data-type="{{- type }}">
            <img class="node-image" src="${absoluteImagePath}/campaignManager/{{- icons.inactive }}"/>
        </div>
    </script>

    <script id="workflow-icon-title" type="text/x-mustache-template">
        <div class="icon-title" style="display: none;">
            <span class="icon-title-span" style="white-space: pre-line;"></span>
            <br>
            <span class="icon-statistic-span" style="white-space: pre-line;"></span>
        </div>
    </script>

    <div id="activating-campaign-dialog" style="visibility: hidden; display: none;">
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
        <jsp:include page="editors/workflow-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-action-based-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-date-based-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-followup-based-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-import-editor.jsp"/>
        <jsp:include page="editors/workflow-export-editor.jsp"/>
        <jsp:include page="editors/workflow-icon-comment-editor.jsp"/>
        <jsp:include page="editors/mailing-data-transfer-modal.jsp"/>
        <jsp:include page="editors/own-workflow-expanding-modal.jsp"/>
        <jsp:include page="workflow-save-before-pdf-dialog.jsp"/>
        <jsp:include page="workflow-copy-dialog-new.jsp"/>
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
            <jsp:param name="dialogName" value="NoStatistics"/>
        </jsp:include>

        <c:if test="${workflowToggleTestingButtonEnabled}">
            <jsp:include page="workflow-testing-dialog-new.jsp"/>
        </c:if>

        <jsp:include page="editors/workflow-sms-mailing-editor.jsp"/>
        <jsp:include page="editors/workflow-post-mailing-editor.jsp"/>
    </div>

    <mvc:form servletRelativeAction="/workflow/save.action" cssClass="form-vertical" id="workflowForm"  modelAttribute="workflowForm" data-form="resource">
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
                            <mvc:message var="nameMsg" code="default.Name"/>
                            ${nameMsg} *
                        </label>
                        <mvc:text path="shortname" cssClass="form-control" id="name" placeholder="${nameMsg}"/>
                    </div>
                </div>
                <div class="col-sm-5 col-lg-4">
                    <div class="form-group">
                        <label for="workflow_description" class="form-label">
                            <mvc:message var="descriptionMsg" code="default.description"/>
                            ${descriptionMsg}
                        </label>
                        <mvc:text path="description" cssClass="form-control" id="workflow_description" placeholder="${descriptionMsg}"/>
                    </div>
                </div>

                <div class="col-sm-2">
                    <div class="form-group">
                        <label class="form-label block">
                            <bean:message key="workflow.status"/>
                        </label>
                        <b class="form-badge campaign.status.background.${workflowForm.status.name}">
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
                                    <input id="workflow_active" data-action="workflow-view-change-status" ${workflowForm.status == STATUS_ACTIVE.name() ? 'checked="checked"':''}  type="checkbox"/>
                                    <div class="toggle-control"></div>
                                </label>
                            </c:if>
                            <input id="workflow-status" type="hidden" name="status" value="${workflowForm.status}"/>
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
                    <a data-action="create-workflow-enlarged-editor-modal"
                       data-tooltip="<bean:message key='editor.enlargeEditor'/>" class="dropdown-toggle">
                        <i class="icon icon-arrows-alt"></i>
                    </a>
                </li>
            </ul>
        </div>

        <div class="tile-content" id="pageCampaignEditorContainer">
            <div class="editor-content-body" id="campaignEditorBody">
                <div class="editor-content-body-top unselectable" id="toolbarTop">
                    <div id="toolbarCross">
                        <div id="toolbarTopName"><bean:message key="workflow.panel.icons"/>:</div>
                        <div id="toolbarBottomName"><bean:message key="Templates"/>:</div>
                    </div>

                    <div class="iconPanel">
                        <div class="iconPanelTitle">
                            <bean:message key="workflow.process"/>
                        </div>

                        <div class="iconPanelRow">
                            <div class="toolbarButton js-draggable-button" data-type="start"
                                 title="<bean:message key="workflow.icon.start"/>"></div>
                            <div class="toolbarButton js-draggable-button" data-type="decision"
                                 title="<bean:message key="workflow.decision"/>"></div>
                            <div class="toolbarButton js-draggable-button" data-type="parameter"
                                 title="<bean:message key="workflow.icon.parameter"/>"></div>
                        </div>

                        <div class="iconPanelRow">
                            <div id="arrowButton" class="toolbarButton" title="<bean:message key="workflow.icon.chaining"/>" data-action="chain-mode"></div>
                            <div class="toolbarButton js-draggable-button" data-type="deadline" title="<bean:message key="workflow.icon.deadline"/>"></div>
                        </div>
                    </div>

                    <div class="iconPanel">
                        <div class="iconPanelTitle">
                            <bean:message key="Recipient"/>
                        </div>

                        <div class="iconPanelRow">
                            <div class="toolbarButton js-draggable-button" data-type="recipient" title="<bean:message key="Recipient"/>"></div>
                        </div>

                        <div class="iconPanelRow">
                            <%@include file="fragments/workflow-view-auto-import-icon.jspf" %>
                            <%@include file="fragments/workflow-view-auto-export-icon.jspf" %>
                        </div>
                    </div>

                    <div class="iconPanel">
                        <div class="iconPanelTitle">
                            <bean:message key="workflow.panel.mailings"/>
                        </div>

                        <div class="iconPanelRow">
                            <div class="toolbarButton js-draggable-button" data-type="mailing" title="<bean:message key="workflow.icon.mailing"/>"></div>
                            <div class="toolbarButton js-draggable-button" data-type="actionbased_mailing" title="<bean:message key="mailing.action.based.mailing"/>"></div>
                            <div class="toolbarButton js-draggable-button" data-type="archive" title="<bean:message key="mailing.archive"/>"></div>
                        </div>

                        <div class="iconPanelRow">
                            <div class="toolbarButton js-draggable-button" data-type="datebased_mailing" title="<bean:message key="mailing.Rulebased_Mailing"/>"></div>

                            <%@include file="fragments/workflow-view-followup-button.jspf" %>
                            <%@include file="fragments/workflow-view-sms-mailing-icon.jspf" %>
                            <%@include file="fragments/workflow-view-post-mailing-icon.jspf" %>
                        </div>
                    </div>

                    <div class="actionPanel actionPanel-md">
                        <div class="actionPanelTool">
                            <div class="actionPanelTitle">
                                <bean:message key="workflow.autoLayout"/>:
                            </div>
                            <div id="autoLayout" class="toolbarButton toolbarButtonLeft" title="<bean:message key='workflow.doAutoLayout'/>" data-action="align-all"></div>
                        </div>

                        <div id="zoomTool" class="actionPanelTool">
                            <div class="actionPanelTitle">
                                <bean:message key="workflow.panel.zoom"/>
                            </div>
                            <div id="zoomToolContent">
                                <div id="zoomMin" class="toolbarButton unselectable-text js-zoom-scale-down" data-action="zoom-out">-</div>
                                <div id="zoomMiddle" class="toolbarButton unselectable-text js-zoom-reset-level" data-action="reset-zoom">0</div>
                                <div id="zoomMax" class="toolbarButton unselectable-text js-zoom-scale-up" data-action="zoom-in">+</div>
                                <div id="sliderContainer">
                                    <div id="slider" class="js-zoom-slider full-width"></div>
                                </div>
                            </div>
                        </div>

                        <div class="actionPanelTool">
                            <div class="actionPanelTitle"><bean:message key="workflow.panel.undo"/></div>
                            <div id="undoButton" class="toolbarButton disabled" data-action="undo"></div>
                        </div>

                        <emm:ShowByPermission token="workflow.change">
                            <div class="actionPanelTool">
                                <div class="actionPanelTitle"><bean:message key="button.Delete"/></div>
                                <div id="deleteButton" class="toolbarButton disabled" data-action="delete-selected"></div>
                            </div>
                        </emm:ShowByPermission>
                    </div>

                    <div class="actionPanel actionPanel-sm">
                        <div class="dropdown">
                            <div class="toolbarButton actionPanelButton" data-toggle="dropdown" title="<bean:message key="MoreComponents"/>">
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
                                        <button type="button" class="btn btn-regular js-zoom-scale-down" id="zoomMinItem">
                                            <i class="icon icon-search-minus"></i>
                                        </button>
                                        <button type="button" class="btn btn-regular js-zoom-reset-level" id="zoomMiddleItem">
                                            <strong>0</strong>
                                        </button>
                                        <button type="button" class="btn btn-regular js-zoom-scale-up" id="zoomMaxItem">
                                            <i class="icon icon-search-plus"></i>
                                        </button>
                                    </p>
                                </li>

                                <li>
                                    <%-- Disabled by default — initially no change is made --%>
                                    <button type="button" id="undoItem" disabled="disabled">
                                        <i class="icon icon-reply"></i>
                                        <bean:message key="workflow.panel.undo"/>
                                    </button>
                                </li>

                                <emm:ShowByPermission token="workflow.change">
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
                <div class="editor-content-body-bottom">
                    <div class="editor-content-body-left-toolbar unselectable" id="toolbarLeft">
                        <div class="toolbarLeftTitle">
                            <bean:message key="workflow.sampleCampaign"/>:
                        </div>
                        <emm:ShowByPermission token="campaign.change" ignoreException="true">
                            <div class="toolbarButtonLeft js-draggable-button" data-type="scABTest" title="<bean:message key='mailing.autooptimization'/>"></div>
                            <div class="leftMenuLabel"><bean:message key="workflow.sampleCampaign.ABTest"/></div>
                        </emm:ShowByPermission>

                        <div class="toolbarButtonLeft js-draggable-button" data-type="scDOI" title="<bean:message key='workflow.icon.DOI'/>"></div>
                        <div id="scDOIButtonLabel" class="leftMenuLabel"><bean:message key="recipient.DOI"/></div>

                        <div class="toolbarButtonLeft js-draggable-button" data-type="scBirthday" title="<bean:message key='workflow.icon.birthday'/>"></div>
                        <div class="leftMenuLabel"><bean:message key="workflow.sampleCampaign.birthday"/></div>

                        <div class="leftMenuSeparator"></div>

                        <div class="toolbarLeftTitle">
                            <bean:message key="workflow.ownCampaign"/>:
                        </div>
                        <div class="toolbarButtonLeft js-draggable-button" data-type="ownWorkflow" title="<bean:message key='workflow.ownCampaign'/>"></div>
                    </div>
                    <div id="viewPort">
                        <div id="canvas">
                            <div id="icon-titles-container"></div>
                        </div>
                        <div id="minimap" class="minimap">
                            <div class="minimap-canvas"></div>
                            <div class="minimap-panner"></div>
                            <div class="minimap-collapse"></div>
                        </div>
                    </div>
                    <div id="navigator" class="js-navigation hidden"></div>
                </div>
            </div>
        </div>
        <!-- Tile Content END -->

        <div id="selection-backdrop">
            <!-- Required to disable all the hover-related effects while selection lasso is visible -->
        </div>

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

<%@include file="fragments/workflow-enlarged-editor-template.jspf" %>
