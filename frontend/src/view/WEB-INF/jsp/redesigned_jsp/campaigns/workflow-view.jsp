<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ page import="org.agnitas.target.ConditionalOperator" %>
<%@ page import="org.agnitas.emm.core.commons.util.ConfigValue" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl" %>
<%@ page import="com.agnitas.emm.core.workflow.web.WorkflowController" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
<%--@elvariable id="isMailtrackingActive" type="java.lang.Boolean"--%>
<%--@elvariable id="accessLimitTargetId" type="java.lang.Integer"--%>
<%--@elvariable id="statisticUrl" type="java.lang.String"--%>
<%--@elvariable id="showStatisticsImmediately" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowToggleTestingButtonEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="pauseTime" type="java.lang.Long"--%>
<%--@elvariable id="pauseExpirationHours" type="java.lang.Integer"--%>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<c:set var="STATUS_NONE" value="<%= WorkflowStatus.STATUS_NONE %>" scope="page"/>
<c:set var="STATUS_OPEN" value="<%= WorkflowStatus.STATUS_OPEN %>" scope="page"/>
<c:set var="STATUS_ACTIVE" value="<%= WorkflowStatus.STATUS_ACTIVE %>" scope="page"/>
<c:set var="STATUS_INACTIVE" value="<%= WorkflowStatus.STATUS_INACTIVE %>" scope="page"/>
<c:set var="STATUS_COMPLETE" value="<%= WorkflowStatus.STATUS_COMPLETE %>" scope="page"/>
<c:set var="STATUS_TESTING" value="<%= WorkflowStatus.STATUS_TESTING %>" scope="page"/>
<c:set var="STATUS_TESTED" value="<%= WorkflowStatus.STATUS_TESTED %>" scope="page"/>
<c:set var="STATUS_PAUSED" value="<%= WorkflowStatus.STATUS_PAUSED %>" scope="page"/>
<%--<c:set var="quote" value="'" />--%>
<%--<c:set var="quoteReplace" value="\\'" />--%>
<c:set var="CHAIN_OPERATOR_AND" value="<%= ChainOperator.AND.getOperatorCode() %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= ChainOperator.OR.getOperatorCode() %>"/>
<c:set var="OPERATOR_IS" value="<%= ConditionalOperator.IS.getOperatorCode() %>"/>

<c:set var="FORWARD_TARGETGROUP_CREATE" value="<%= WorkflowController.FORWARD_TARGETGROUP_CREATE_QB%>"/>
<c:set var="FORWARD_TARGETGROUP_EDIT" value="<%= WorkflowController.FORWARD_TARGETGROUP_EDIT_QB%>"/>

<c:set var="DEFAULT_PAUSE_EXPIRATION_HOURS" value="<%= ConfigValue.WorkflowPauseExpirationHours.getDefaultValue() %>"/>

<c:set var="isActive" value="${workflowForm.status == STATUS_ACTIVE.name()}"/>
<c:set var="isComplete" value="${workflowForm.status == STATUS_COMPLETE.name()}"/>
<c:set var="isTesting" value="${workflowForm.status eq STATUS_TESTING}"/>
<c:set var="isPause" value="${workflowForm.status eq STATUS_PAUSED}"/>

<%--<emm:instantiate var="mailingLists" type="java.util.LinkedHashMap">--%>
<%--    <c:forEach var="mailingList" items="${allMailinglists}">--%>
<%--        <c:set target="${mailingLists}" property="${mailingList.id}" value="${mailingList.shortname}"/>--%>
<%--    </c:forEach>--%>
<%--</emm:instantiate>--%>

<%--<emm:instantiate var="targets" type="java.util.LinkedHashMap">--%>
<%--    <c:forEach var="targer" items="${allTargets}">--%>
<%--        <c:set target="${targets}" property="${targer.id}" value="${targer.targetName}"/>--%>
<%--    </c:forEach>--%>
<%--</emm:instantiate>--%>

<%--<emm:instantiate var="mailings" type="java.util.LinkedHashMap">--%>
<%--    <c:forEach var="mailing" items="${allMailings}">--%>
<%--        <c:set target="${mailings}" property="${mailing.mailingID}" value="${fn:replace(mailing.shortname, quote, quoteReplace)}"/>--%>
<%--    </c:forEach>--%>
<%--</emm:instantiate>--%>

<%--<emm:instantiate var="allForms" type="java.util.LinkedHashMap">--%>
<%--    <c:set target="${allForms}" property="0" value=""/>--%>
<%--    <c:forEach var="userForm" items="${allUserForms}">--%>
<%--        <c:set target="${allForms}" property="${userForm.id}" value="${userForm.formName}"/>--%>
<%--    </c:forEach>--%>
<%--</emm:instantiate>--%>

<%--<script>--%>
<%--    (function() {--%>
<%--      window.addEventListener('wheel', function(e) { if (e.ctrlKey == true) {e.preventDefault();}}, { passive: false });--%>
<%--      window.addEventListener('mousewheel', function(e) { if (e.ctrlKey == true) {e.preventDefault();}}, { passive: false });--%>
<%--      window.addEventListener('DOMMouseScroll', function(e) { if (e.ctrlKey == true) {e.preventDefault();}}, { passive: false });--%>
<%--    })();--%>
<%--</script>--%>

<c:url var="iconSpriteLocation" value="/assets/core/images/campaignManager/campaign-icon-sprite.svg"/>

<%--todo check if any images still required--%>
<emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/> 

<%--todo to separate file for fragments ?--%>

<script id="workflow-node" type="text/x-mustache-template">
    <%-- Toggle 'active' class to toggle active/inactive node images --%>
    <div class="node" rel="popover">
        <svg class="node-image">
            <use href="${iconSpriteLocation.concat('#')}{{- type }}"></use>
        </svg>
        <div class="icon-overlay-title"></div>
        <div class="icon-overlay-image"><img/></div>
        <div class="node-connect-button">
            <svg><use href="${iconSpriteLocation}#arrow"></use></svg>
        </div>
        <div class="node-comment-button">
            <svg><use href="${iconSpriteLocation}#comment"></use></svg>
        </div>
    </div>
</script>
    
<script id="workflow-icon-title" type="text/x-mustache-template">
    <div class="icon-title" style="display: none;">
        <span class="icon-title-span" style="white-space: pre-line;"></span>
        <br>
    </div>
</script>

<script id="workflow-draggable-node" type="text/x-mustache-template">
    <div class="draggable-node" data-type="{{- type }}">
        <svg class="node-image">
            <use href="${iconSpriteLocation.concat('#')}{{- type }}"></use>
        </svg>
    </div>
</script>


<%--    <div id="activating-campaign-dialog" style="visibility: hidden; display: none;">--%>
<%--        <div class="form-group">--%>
<%--            <div class="col-sm-12">--%>
<%--                <div class="well"><mvc:message code="${isPause ? 'workflow.continue.question' : 'workflow.activating.question'}"/></div>--%>
<%--            </div>--%>
<%--        </div>--%>

<%--        <c:if test="${!isPause}">--%>
<%--            <div class="form-group">--%>
<%--                <div class="col-sm-4">--%>
<%--                    <label class="control-label">--%>
<%--                        <mvc:message code="workflow.activating.mailings"/>--%>
<%--                    </label>--%>
<%--                </div>--%>
<%--                <div class="col-sm-8">--%>
<%--                    <label id="activating-campaign-mailings"></label>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </c:if>--%>

<%--        <hr>--%>

<%--        <div class="col-xs-12">--%>
<%--            <div class="form-group">--%>
<%--                <div class="btn-group">--%>
<%--                    <a href="#" class="btn btn-regular"--%>
<%--                       onclick="jQuery('#activating-campaign-dialog').dialog('close'); return false;">--%>
<%--                        <mvc:message code="button.Cancel"/>--%>
<%--                    </a>--%>

<%--                    <a href="#" class="btn btn-regular btn-primary" id="activating-campaign-activate-button">--%>
<%--                        <mvc:message code="${isPause ? 'button.continue.workflow' : 'workflow.activating.title'}"/>--%>
<%--                    </a>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>

<%--    <script id="mailing-types-replace-modal" type="text/x-mustache-template">--%>
<%--        <div class="modal">--%>
<%--            <div class="modal-dialog">--%>
<%--                <div class="modal-content">--%>
<%--                    <div class="modal-header">--%>
<%--                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>--%>
<%--                        <h4 class="modal-title">--%>
<%--                            <mvc:message code="warning"/>--%>
<%--                        </h4>--%>
<%--                    </div>--%>

<%--                    <div class="modal-body">--%>
<%--                        <p><mvc:message code="workflow.mailingTypesFix.question"/></p>--%>
<%--                    </div>--%>

<%--                    <div class="modal-footer">--%>
<%--                        <div class="btn-group">--%>
<%--                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">--%>
<%--                                <i class="icon icon-times"></i>--%>
<%--                                <span class="text">--%>
<%--                                    <mvc:message code="button.Cancel"/>--%>
<%--                                </span>--%>
<%--                            </button>--%>

<%--                            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">--%>
<%--                                <i class="icon icon-check"></i>--%>
<%--                                <span class="text">--%>
<%--                                    <mvc:message code="button.Proceed"/>--%>
<%--                                </span>--%>
<%--                            </button>--%>
<%--                        </div>--%>
<%--                    </div>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </script>--%>

<%--    <div id="inactivating-campaign-dialog" style=" visibility: hidden; display: none;">--%>
<%--        <div class="form-group">--%>
<%--            <div class="col-sm-12">--%>
<%--                <div class="well"><mvc:message code="workflow.inactivating.question"/></div>--%>
<%--            </div>--%>
<%--        </div>--%>

<%--        <hr>--%>

<%--        <div class="col-xs-12">--%>
<%--            <div class="form-group">--%>
<%--                <div class="btn-group">--%>
<%--                    <a href="#" class="btn btn-regular"--%>
<%--                       onclick="jQuery('#inactivating-campaign-dialog').dialog('close'); return false;">--%>
<%--                        <span><mvc:message code="button.Cancel"/></span>--%>
<%--                    </a>--%>

<%--                    <a href="#" class="btn btn-regular btn-primary" id="inactivating-campaign-inactivate-button">--%>
<%--                        <mvc:message code="workflow.inactivating.title"/>--%>
<%--                    </a>--%>
<%--                </div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>

<mvc:form cssClass="tiles-container d-flex flex-column hidden" servletRelativeAction="/workflow/save.action" id="workflowForm" modelAttribute="workflowForm"
          data-controller="workflow-view"
          data-form="resource"
          data-editable-view="${agnEditViewKey}">
    <input type="hidden" name="workflowId" value="${workflowForm.workflowId}"/>
    <input type="hidden" name="schema" id="schema" value=""/>
<%--    <input type="hidden" name="editorPositionTop" id="editorPositionTop" value=""/>--%>
<%--    <input type="hidden" name="editorPositionLeft" id="editorPositionLeft" value=""/>--%>
<%--    <input type="hidden" name="forwardName" id="forwardName" value=""/>--%>
<%--    <input type="hidden" name="forwardParams" id="forwardParams" value=""/>--%>
<%--    <input type="hidden" name="forwardTargetItemId" id="forwardTargetItemId" value=""/>--%>
<%--    <mvc:hidden path="workflowUndoHistoryData" />--%>
<%--    <mvc:hidden path="usingActivatedWorkflow"/>--%>
<%--    <mvc:hidden path="usingActivatedWorkflowName"/>--%>
<%--    <mvc:hidden path="partOfActivatedWorkflow"/>--%>
<%--    <mvc:hidden path="partOfActivatedWorkflowName"/>--%>

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
                "forwardAutoExportCreate": "<%= WorkflowController.FORWARD_AUTOEXPORT_CREATE %>",
                "forwardAutoExportEdit": "<%= WorkflowController.FORWARD_AUTOEXPORT_EDIT %>",
                "forwardAutoImportCreate": "<%= WorkflowController.FORWARD_AUTOIMPORT_CREATE %>",
                "forwardAutoImportEdit": "<%= WorkflowController.FORWARD_AUTOIMPORT_EDIT %>",
                "forwardArchiveCreate": "<%= WorkflowController.FORWARD_ARCHIVE_CREATE %>",
                "statusInactive":"${STATUS_INACTIVE}",
                "statusActive": "${STATUS_ACTIVE}",
                "statusTesting": "${STATUS_TESTING}",
                "statusOpen": "${STATUS_OPEN}",
                "statusPaused": "${STATUS_PAUSED}",
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
                    "<%= ChainOperator.AND.getOperatorCode() %>": "<mvc:message code="default.and"/>",
                    "<%= ChainOperator.OR.getOperatorCode() %>": "<mvc:message code="default.or"/>"
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
            "accessLimitTargetId": ${accessLimitTargetId},
            "statisticUrl": "${statisticUrl}"
        }
    </script>
    
    <div id="settings-tile" class="tile" data-editable-tile style="height: auto">
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="campaignInformation"/></h1>
            <div class="tile-controls">
                <c:if test="${isPause}">
                    <script data-initializer="workflow-pause-timer" type="application/json">
                        {
                            "defaultExpirationHours": ${DEFAULT_PAUSE_EXPIRATION_HOURS},
                            "expirationHours": ${pauseExpirationHours},
                            "workflowId": ${workflowForm.workflowId},
                            "pauseTime": ${pauseTime}
                        }
                    </script>
                    <div data-tooltip="<mvc:message code="workflow.pause.timer"/>">
                        <div id="workflow-pause-timer-text" class="badge text-bg-primary">--:--:--</div>
                    </div>
                </c:if>
                <span class="badge campaign.status.${workflowForm.status.name}"></span>
                <span class="status-badge campaign.status.${workflowForm.status.name}" data-tooltip="<mvc:message code="${workflowForm.status.messageKey}"/>"></span>
            </div>
            <mvc:hidden id="workflow-status" path="status"/>
        </div>
        <div class="tile-body grid" data-field="toggle-vis" style="--bs-columns: 3; grid-template-columns: 1fr">
            <div class="row">
                <div class="col">
                    <label for="name" class="form-label">
                        <mvc:message var="nameMsg" code="default.Name"/>
                        ${nameMsg} *
                    </label>
                    <mvc:text path="shortname" cssClass="form-control" id="name" placeholder="${nameMsg}"/>
                </div>
                <div class="col">
                    <label for="workflow_description" class="form-label">
                        <mvc:message var="descriptionMsg" code="Description"/>
                        ${descriptionMsg}
                    </label>
                    <mvc:text path="description" cssClass="form-control" id="workflow_description" placeholder="${descriptionMsg}"/>
                </div>

                <div class="col-auto">
                    <emm:ShowByPermission token="workflow.activate">
                        <c:if test="${workflowForm.statusMaybeChangedTo ne STATUS_NONE}">
                            <label class="form-label"><mvc:message code="workflow.status.change"/></label>
                            <div class="d-flex gap-1">
                                <emm:ShowByPermission token="workflow.change">
                                    <mvc:message var="dryRunHelpTitle" code="${workflowToggleTestingButtonState ? 'button.workflow.testrun.start' : 'button.workflow.testrun.stop'}"/>
                                    <mvc:message var="dryRunHelpText" code="button.workflow.testrun.help"/>
                                    <c:if test="${workflowToggleTestingButtonEnabled}">
                                        <a href="#" class="btn btn-icon-sm bg-primary text-white"
                                           data-tooltip-help='{"title": "${dryRunHelpTitle}", "content": "${dryRunHelpText}", "placement": "bottom-end"}'
                                           data-action="workflow-dry-run">
                                            <i class="icon icon-flask"></i>
                                        </a>
                                    </c:if>
                                </emm:ShowByPermission>
                                
                                <c:if test="${workflowForm.status ne STATUS_ACTIVE}">
                                    <a href="#" class="btn btn-icon-sm bg-success text-white" data-tooltip="<mvc:message code='${isPause ? "button.continue.workflow" : "button.Activate"}'/>" data-action="${isPause ? 'workflow-unpause' : 'workflow-activate'}">
                                        <i class="icon icon-play"></i>
                                    </a>
                                </c:if>
                                <c:if test="${isActive}">
                                    <a href="#" class="btn btn-icon-sm bg-warning text-white" data-tooltip="<mvc:message code='button.Pause'/>" data-action="workflow-pause">
                                        <i class="icon icon-pause"></i>
                                    </a>
                                </c:if>
                                <c:if test="${isActive or isTesting or isPause}">
                                    <a href="#" class="btn btn-icon-sm bg-danger text-white" data-tooltip="<mvc:message code='stop'/>" data-action="workflow-deactivate">
                                        <i class="icon icon-stop"></i>
                                    </a>
                                </c:if>
                            </div>
                        </c:if>
                    </emm:ShowByPermission>
                    <emm:HideByPermission token="workflow.activate">
                        <label class="form-label">&nbsp;</label>
                        <div class="notification-simple notification-simple--info">
                            <mvc:message code="workflow.activate.info"/>
                        </div>
                    </emm:HideByPermission>
                </div>
            </div>
        </div>
    </div>

    <div class="tiles-block flex-grow-1">
        <div id="editor-tile" class="tile" style="flex: 3" data-editable-tile="main">
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="workflow.editor"/></h1>
            </div>
    
            <div class="tile-body p-0" id="pageCampaignEditorContainer">
    <%--            todo containers--%>
                <div class="editor-content-body" id="campaignEditorBody">
                    <div class="toolbar unselectable">
                        <div class="toolbar__icons">
                            <div>
                                <div class="form-label"><mvc:message code="workflow.process"/></div>
                                <div class="toolbar__icon-set">
                                    <svg class="toolbar__icon js-draggable-button" data-type="start" data-tooltip="<mvc:message code="workflow.icon.start"/>">
                                        <use href="${iconSpriteLocation}#start-stop"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button w-30" data-type="decision" data-tooltip="<mvc:message code="workflow.decision"/>">
                                        <use href="${iconSpriteLocation}#decision"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button w-30" data-type="parameter" data-tooltip="<mvc:message code="workflow.icon.parameter"/>">
                                        <use href="${iconSpriteLocation}#parameter"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button w-26" data-type="deadline" data-tooltip="<mvc:message code="workflow.icon.deadline"/>">
                                        <use href="${iconSpriteLocation}#deadline"></use>
                                    </svg>
                                    <svg class="toolbar__icon w-30" id="arrowButton" data-tooltip="<mvc:message code="workflow.icon.chaining"/>" data-action="chain-mode">
                                        <use href="${iconSpriteLocation}#arrow"></use>
                                    </svg>
                                </div>
                            </div>
        
                            <div>
                                <div class="form-label"><mvc:message code="Recipient"/></div>
                                <div class="toolbar__icon-set">
                                    <svg class="toolbar__icon js-draggable-button w-30" data-type="recipient" data-tooltip="<mvc:message code="Recipient"/>">
                                        <use href="${iconSpriteLocation}#recipient"></use>
                                    </svg>
                                    <%@include file="fragments/workflow-data-icons-extended.jspf" %>
                                </div>
                            </div>
        
                            <div>
                                <div class="form-label">
                                    <mvc:message code="workflow.panel.mailings"/>
                                </div>
                                <div class="toolbar__icon-set">
                                    <svg class="toolbar__icon js-draggable-button" data-type="mailing" data-tooltip="<mvc:message code="workflow.icon.mailing"/>">
                                        <use href="${iconSpriteLocation}#mailing"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button w-35" data-type="archive" data-tooltip="<mvc:message code="mailing.archive"/>">
                                        <use href="${iconSpriteLocation}#archive"></use>
                                    </svg>
                                    <%@include file="fragments/workflow-senidng-icons-extended.jspf" %>
                                </div>
                            </div>
        
                            <div>
                                <div class="form-label"><mvc:message code="Templates"/></div>
                                <div class="toolbar__icon-set">
                                    <emm:ShowByPermission token="campaign.change" ignoreException="true">
                                        <svg class="toolbar__icon js-draggable-button w-35" data-type="scABTest" data-tooltip="<mvc:message code='mailing.autooptimization'/>">
                                            <use href="${iconSpriteLocation}#scABTest"></use>
                                        </svg>
                                    </emm:ShowByPermission>
                                    <svg class="toolbar__icon js-draggable-button" data-type="scDOI" data-tooltip="<mvc:message code='workflow.icon.DOI'/>">
                                        <use href="${iconSpriteLocation}#scDOI"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button" data-type="scBirthday" data-tooltip="<mvc:message code='workflow.icon.birthday'/>">
                                        <use href="${iconSpriteLocation}#scBirthday"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button" data-type="ownWorkflow" data-tooltip="<mvc:message code='workflow.ownCampaign'/>">
                                        <use href="${iconSpriteLocation}#ownWorkflow"></use>
                                    </svg>
                                </div>
                            </div>
                        </div>
    
                        <div>
                            <div class="form-label"><mvc:message code="action.Action"/></div>
                            <div class="d-flex gap-1">
                                <a id="autoLayout" class="btn btn-icon-sm btn-inverse" data-tooltip="<mvc:message code='workflow.doAutoLayout'/>" data-action="align-all">
                                    <i class="icon icon-vector-square"></i>
                                </a>
                                <a id="autoLayout" class="btn btn-icon-sm btn-inverse" data-tooltip="<mvc:message code='campaign.grid.show'/>" data-action="show-grid">
                                    <i class="icon icon-border-all"></i>
                                </a>
                                <a href="#" id="undoButton" class="btn btn-icon-sm btn-primary disabled" data-action="undo" data-tooltip='<mvc:message code="workflow.panel.undo"/>'>
                                    <i class="icon icon-undo"></i>
                                </a>
                                <emm:ShowByPermission token="workflow.change">
                                    <a href="#" id="deleteButton" class="btn btn-icon-sm btn-danger disabled" data-action="delete-selected" data-tooltip='<mvc:message code="button.Delete"/>'>
                                        <i class="icon icon-trash-alt"></i>
                                    </a>
                                </emm:ShowByPermission>
                            </div>
                        </div>
                    </div>
                    <div class="editor-content-body-bottom">
                        <div id="viewPort">
                            <div id="grid-background" class="hidden"></div>
                            <div id="canvas">
                                <div id="icon-titles-container"></div>
                            </div>
                            <div id="navigator">
                                <div id="minimap" class="minimap" style="display: none">
                                    <div class="minimap-canvas"></div>
                                    <div class="minimap-arrow" data-arrow="up">
                                        <i class="icon icon-angle-up"></i>
                                    </div>
                                    <div class="minimap-arrow" data-arrow="right">
                                        <i class="icon icon-angle-right"></i>
                                    </div>
                                    <div class="minimap-arrow" data-arrow="down">
                                        <i class="icon icon-angle-down"></i>
                                    </div>
                                    <div class="minimap-arrow" data-arrow="left">
                                        <i class="icon icon-angle-left"></i>
                                    </div>
                                    <div class="minimap-panner"></div>
<%--                                    <div class="minimap-collapse"></div>--%>
                                </div>
                                <div id="zoom">
                                    <button type="button" class="zoom-btn unselectable js-zoom-scale-down" data-action="zoom-out">
                                        <i class="icon icon-minus"></i>
                                    </button>
                                    <div id="slider" class="js-zoom-slider full-width"></div>
                                    <button type="button" class="zoom-btn unselectable js-zoom-scale-up" data-action="zoom-in">
                                        <i class="icon icon-plus"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                        <c:if test="${not isMailtrackingActive}">
                            <div class="p-2">
                                <div class="notification-simple notification-simple--info">
                                    <mvc:message code="workflow.info.noMailtracking"/>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
                <div id="selection-backdrop">
                    <!-- Required to disable all the hover-related effects while selection lasso is visible -->
                </div>
            </div>
        </div>
        <div id="edit-node-tile" class="tile" data-editable-tile style="flex: 1">
            <div class="tile-header">
                <h1 class="tile-tit"><mvc:message code="GWUA.edit.node"/></h1>
            </div>
            <div class="tile-body" id="node-editor">
                <div class="notification-simple">
                    <i class="icon icon-info-circle"></i>
                    <mvc:message code="GWUA.edit.node.select"/>
                </div>
            </div>
        </div>
    </div>
    
<%--        <script data-initializer="open-edit-icon-initializer" type="application/json">--%>
<%--            {--%>
<%--                "nodeId": "${nodeId}",--%>
<%--                "elementValue": "${elementValue}"--%>
<%--            }--%>
<%--        </script>--%>
<%--    </div>--%>
<%--    <!-- Tile END -->--%>

<%--        <div class="tile">--%>
<%--            <div class="tile-header">--%>
<%--                <a class="headline" href="#" data-toggle-tile="#tile-statistic">--%>
<%--                    <i class="icon tile-toggle icon-angle-up"></i>--%>
<%--                    <mvc:message code="statistic.workflow" />--%>
<%--                </a>--%>
<%--                <ul class="tile-header-actions">--%>
<%--                    <li>--%>
<%--                        <c:choose>--%>
<%--                            <c:when test="${isActive or isComplete}">--%>
<%--                                <button type="button" class="btn btn-regular btn-primary" data-action="evaluate-statistic">--%>
<%--                                    <i class="icon icon-refresh"></i>--%>
<%--                                    <span class="text"><mvc:message code="Evaluate" /></span>--%>
<%--                                </button>--%>
<%--                            </c:when>--%>
<%--                            <c:otherwise>--%>
<%--                                <button type="button" class="btn btn-regular btn-primary" data-form-target="#workflowForm" data-action="workflow-save" data-form-set="showStatistic: true">--%>
<%--                                    <i class="icon icon-refresh"></i>--%>
<%--                                    <span class="text"><mvc:message code="button.save.evaluate" /></span>--%>
<%--                                </button>--%>
<%--                            </c:otherwise>--%>
<%--                        </c:choose>--%>
<%--                    </li>--%>
<%--                </ul>--%>
<%--            </div>--%>
<%--            <div id="tile-statistic" class="tile-content">--%>
<%--                <c:choose>--%>
<%--                    <c:when test="${isActive or isComplete}">--%>
<%--                        <iframe id="statistic-iframe" class="hidden" border="0" scrolling="auto" width="100%" height="600px" frameborder="0">--%>
<%--                            Your Browser does not support IFRAMEs, please update!--%>
<%--                        </iframe>--%>
<%--                    </c:when>--%>
<%--                    <c:otherwise>--%>
<%--                        <c:if test="${showStatisticsImmediately}">--%>
<%--                            <iframe src="${statisticUrl}" border="0" scrolling="auto" width="100%" height="600px" frameborder="0">--%>
<%--                                Your Browser does not support IFRAMEs, please update!--%>
<%--                            </iframe>--%>
<%--                        </c:if>--%>
<%--                    </c:otherwise>--%>
<%--                </c:choose>--%>
<%--            </div>--%>
<%--        </div>--%>

    <script type="text/javascript">
        //fix problem with not expected behavior of JSON.stringify() for arrays
        if (window.Prototype) {
            delete Array.prototype.toJSON;
        }
    </script>
</mvc:form>

<div style="display: none">
    <%--        <jsp:include page="editors/workflow-start-editor.jsp">--%>
    <%--            <jsp:param name="adminTimezone" value="${adminTimezone}"/>--%>
    <%--        </jsp:include>--%>
    <%--        <jsp:include page="editors/workflow-decision-editor.jsp"/>--%>
    <jsp:include page="editors/workflow-deadline-editor.jsp"/>
    <jsp:include page="editors/workflow-parameter-editor.jsp"/>
    <jsp:include page="editors/workflow-recipient-editor.jsp"/>
    <jsp:include page="editors/workflow-archive-editor.jsp"/>
    <jsp:include page="editors/workflow-mailing-editor.jsp"/>
    <%--        <jsp:include page="editors/workflow-action-based-mailing-editor.jsp"/>--%>
    <%--        <jsp:include page="editors/workflow-date-based-mailing-editor.jsp"/>--%>
    <%--        <jsp:include page="editors/workflow-followup-based-mailing-editor.jsp"/>--%>
    <jsp:include page="editors/workflow-import-editor.jsp"/>
    <jsp:include page="editors/workflow-export-editor.jsp"/>
    <%--        <jsp:include page="editors/workflow-icon-comment-editor.jsp"/>--%>
    <%--        <jsp:include page="workflow-simple-dialog.jsp">--%>
    <%--            <jsp:param name="messageKey" value="error.workflow.connection.notAllowed"/>--%>
    <%--            <jsp:param name="titleKey" value="error.workflow.connection.deactivated"/>--%>
    <%--            <jsp:param name="dialogName" value="NotAllowedConnection"/>-Target-%>
    <%--        </jsp:include>--%>
    <%--        <jsp:include page="workflow-simple-dialog.jsp">--%>
    <%--            <jsp:param name="messageKey" value="error.workflow.notAllowedSeveralConnections.description"/>--%>
    <%--            <jsp:param name="titleKey" value="error.workflow.connection.deactivated"/>--%>
    <%--            <jsp:param name="dialogName" value="NotAllowedSeveralConnections"/>--%>
    <%--        </jsp:include>--%>
    <%--        <jsp:include page="workflow-simple-dialog.jsp">--%>
    <%--            <jsp:param name="messageKey" value="error.workflow.notAllowedEditing.description"/>--%>
    <%--            <jsp:param name="titleKey" value="error.workflow.notAllowedEditing.title"/>--%>
    <%--            <jsp:param name="dialogName" value="NotAllowedEditing"/>--%>
    <%--        </jsp:include>--%>
    <%--        <jsp:include page="editors/workflow-sms-mailing-editor.jsp"/>--%>
        <jsp:include page="editors/workflow-post-mailing-editor.jsp"/>
</div>

<%--<jsp:include page="editors/mailing-data-transfer-modal.jsp"/>--%>
<%@include file="fragments/modal/workflow-copy-modal.jspf" %>
<%@include file="fragments/modal/own-workflow-expanding-modal.jspf" %>
<%@include file="fragments/modal/workflow-save-before-pdf-modal.jspf" %>
<c:if test="${workflowToggleTestingButtonEnabled}">
    <%@include file="fragments/modal/workflow-testing-modal.jspf" %>
</c:if>
