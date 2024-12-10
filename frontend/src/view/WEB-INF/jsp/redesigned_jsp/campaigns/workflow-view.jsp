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
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowForward" %>
<%@ page import="com.agnitas.emm.core.workflow.service.ComSampleWorkflowFactory.SampleWorkflowType" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
<%--@elvariable id="isMailtrackingActive" type="java.lang.Boolean"--%>
<%--@elvariable id="accessLimitTargetId" type="java.lang.Integer"--%>
<%--@elvariable id="statisticUrl" type="java.lang.String"--%>
<%--@elvariable id="showStatisticsImmediately" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowToggleTestingButtonEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="pauseTime" type="java.lang.Long"--%>
<%--@elvariable id="pauseExpirationHours" type="java.lang.Integer"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<c:set var="DEFAULT_PAUSE_EXPIRATION_HOURS" value="<%= ConfigValue.WorkflowPauseExpirationHours.getDefaultValue() %>"/>
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
<c:set var="CHAIN_OPERATOR_AND" value="<%= ChainOperator.AND.getOperatorCode() %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= ChainOperator.OR.getOperatorCode() %>"/>
<c:set var="OPERATOR_IS" value="<%= ConditionalOperator.IS.getOperatorCode() %>"/>

<c:set var="isActive" value="${workflowForm.status == STATUS_ACTIVE.name()}"/>
<c:set var="isComplete" value="${workflowForm.status == STATUS_COMPLETE.name()}"/>
<c:set var="isTesting" value="${workflowForm.status eq STATUS_TESTING}"/>
<c:set var="isPause" value="${workflowForm.status eq STATUS_PAUSED}"/>

<c:url var="iconSpriteLocation" value="/assets/core/images/campaignManager/campaign-icon-sprite.svg"/>

<c:set var="workflowNodeIconTemplate">
    <svg class="node-image">
        <use href="${iconSpriteLocation.concat('#')}{{- type }}"></use>
    </svg>
</c:set>

<script id="workflow-node" type="text/x-mustache-template">
    <%-- Toggle 'active' class to toggle active/inactive node images --%>
    <div class="node" rel="popover">
        ${workflowNodeIconTemplate}
        <div class="icon-overlay-title"></div>
        <div class="node-status-badge" style="display:none;">
            <span class="badge icon-badge text-bg-primary"><i class="icon icon-cogs"></i></span>
        </div>
        <div class="node-connect-button">
            <svg><use href="${iconSpriteLocation}#arrow"></use></svg>
        </div>
    </div>
</script>

<script id="workflow-node-icon" type="text/x-mustache-template">
    ${workflowNodeIconTemplate}
</script>
    
<script id="workflow-icon-title" type="text/x-mustache-template">
    <div class="icon-title" style="display: none;">
        <span class="icon-title-span" style="white-space: pre-line;"></span>
        <br>
    </div>
</script>

<script id="workflow-draggable-node" type="text/x-mustache-template">
    <div class="draggable-node" data-type="{{- type }}">
        ${workflowNodeIconTemplate}
    </div>
</script>

<div class="tiles-container flex-column" data-controller="workflow-view" data-editable-view="${agnEditViewKey}">
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
                "forwards": ${WorkflowForward.asJson()},
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
                "initialWorkflowStatus": "${workflowForm.status}",
                "localeDateTimePattern": "${localeDateTimePattern}",
                "isAltgExtended" : ${isExtendedAltgEnabled}
            },
            "accessLimitTargetId": ${accessLimitTargetId},
            "statisticUrl": "${statisticUrl}",
            "showStatisticWithoutSave": ${isTesting or isActive or isComplete or isPause},
            "sentMailings": ${empty sentMailings ? [] : sentMailings}
        }
    </script>
    
    <mvc:form id="workflowForm" cssClass="tile flex-none" servletRelativeAction="/workflow/save.action" modelAttribute="workflowForm"
              data-form="resource"
              data-editable-tile="" cssStyle="height: auto">
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
        
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="campaignInformation"/></h1>
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
                        <label class="form-label"><mvc:message code="workflow.status.change"/></label>
                        <div class="d-flex gap-1">
                            <emm:ShowByPermission token="workflow.change">
                                <mvc:message var="dryRunHelpTitle" code="${workflowToggleTestingButtonState ? 'button.workflow.testrun.start' : 'button.workflow.testrun.stop'}"/>
                                <mvc:message var="dryRunHelpText" code="button.workflow.testrun.help"/>
                                <c:if test="${workflowToggleTestingButtonEnabled}">
                                    <a href="#" class="btn btn-icon bg-primary text-white" data-popover="${dryRunHelpText}" data-popover-options='{"title": "${dryRunHelpTitle}", "html": true, "popperConfig": {"placement": "bottom-end"}}'
                                       data-action="workflow-dry-run">
                                        <i class="icon icon-flask"></i>
                                    </a>
                                </c:if>
                            </emm:ShowByPermission>
                            
                            <c:if test="${workflowForm.statusMaybeChangedTo ne STATUS_NONE}">
                                <c:if test="${workflowForm.status ne STATUS_ACTIVE}">
                                    <a href="#" class="btn btn-icon bg-success text-white" data-tooltip="<mvc:message code='${isPause ? "button.continue.workflow" : "button.Activate"}'/>" data-action="${isPause ? 'workflow-unpause' : 'workflow-activate'}">
                                        <i class="icon icon-play"></i>
                                    </a>
                                </c:if>
                                <c:if test="${isActive}">
                                    <a href="#" class="btn btn-icon bg-warning text-white" data-tooltip="<mvc:message code='button.Pause'/>" data-action="workflow-pause">
                                        <i class="icon icon-pause"></i>
                                    </a>
                                </c:if>
                                <c:if test="${isActive or isTesting or isPause}">
                                    <a href="#" class="btn btn-icon bg-danger text-white" data-tooltip="<mvc:message code='stop'/>" data-action="workflow-deactivate">
                                        <i class="icon icon-stop"></i>
                                    </a>
                                </c:if>
                            </c:if>
                        </div>
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
    </mvc:form>

    <div class="tiles-block">
        <div id="editor-tile" class="tile" style="flex: 2" data-editable-tile="main">
            <div class="tile-body p-0" id="pageCampaignEditorContainer">
                <div class="editor-content-body" id="campaignEditorBody">
                    <div class="toolbar unselectable gap-6">
                        <div class="toolbar__icons">
                            <div>
                                <div class="form-label text-truncate"><mvc:message code="workflow.process"/></div>
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
                                <div class="form-label text-truncate"><mvc:message code="Recipient"/></div>
                                <div class="toolbar__icon-set">
                                    <svg class="toolbar__icon js-draggable-button w-30" data-type="recipient" data-tooltip="<mvc:message code="Recipient"/>">
                                        <use href="${iconSpriteLocation}#recipient"></use>
                                    </svg>
                                    <%@include file="fragments/workflow-data-icons-extended.jspf" %>
                                </div>
                            </div>
        
                            <div>
                                <div class="form-label text-truncate">
                                    <mvc:message code="workflow.panel.mailings"/>
                                </div>
                                <div class="toolbar__icon-set">
                                    <svg class="toolbar__icon js-draggable-button" data-type="mailing" data-tooltip="<mvc:message code="workflow.icon.mailing"/>">
                                        <use href="${iconSpriteLocation}#mailing"></use>
                                    </svg>
                                    <svg class="toolbar__icon js-draggable-button w-35" data-type="archive" data-tooltip="<mvc:message code="mailing.archive"/>">
                                        <use href="${iconSpriteLocation}#archive"></use>
                                    </svg>
                                    <%@include file="fragments/workflow-sending-icons-extended.jspf" %>
                                </div>
                            </div>
        
                            <div>
                                <div class="form-label text-truncate"><mvc:message code="Templates"/></div>
                                <div class="toolbar__icon-set">
                                    <c:forEach var="workflowSample" items="<%= SampleWorkflowType.values() %>">
                                        <c:if test="${workflowSample eq SampleWorkflowType.AB_TEST}">
                                            <emm:ShowByPermission token="campaign.change" ignoreException="true">
                                                <svg class="toolbar__icon js-draggable-button w-35" data-type="scABTest" data-tooltip="<mvc:message code='mailing.autooptimization'/>">
                                                    <use href="${iconSpriteLocation}#scABTest"></use>
                                                </svg>
                                            </emm:ShowByPermission>
                                        </c:if>

                                        <svg class="toolbar__icon js-draggable-button" data-type="${workflowSample.value}" data-tooltip="<mvc:message code='${workflowSample.message}'/>">
                                            <use href="${iconSpriteLocation}#${workflowSample.value}"></use>
                                        </svg>
                                    </c:forEach>

                                    <svg class="toolbar__icon js-draggable-button" data-type="ownWorkflow" data-tooltip="<mvc:message code='workflow.ownCampaign'/>">
                                        <use href="${iconSpriteLocation}#ownWorkflow"></use>
                                    </svg>
                                </div>
                            </div>
                        </div>
    
                        <div>
                            <div class="form-label text-truncate"><mvc:message code="action.Action"/></div>
                            <div class="d-flex gap-1">
                                <a id="autoLayout" class="btn btn-icon btn-inverse" data-tooltip="<mvc:message code='workflow.doAutoLayout'/>" data-action="align-all">
                                    <i class="icon icon-vector-square"></i>
                                </a>
                                <a id="autoLayout" class="btn btn-icon btn-inverse" data-tooltip="<mvc:message code='campaign.grid.show'/>" data-action="show-grid">
                                    <i class="icon icon-border-all"></i>
                                </a>
                                <a href="#" id="undoButton" class="btn btn-icon btn-primary disabled" data-action="undo" data-tooltip='<mvc:message code="workflow.panel.undo"/>'>
                                    <i class="icon icon-undo"></i>
                                </a>
                                <emm:ShowByPermission token="workflow.change">
                                    <a href="#" id="deleteButton" class="btn btn-icon btn-danger disabled" data-action="delete-selected" data-tooltip='<mvc:message code="button.Delete"/>'>
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
                                    <div class="minimap-collapse"></div>
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
                            <div id="collapsed-navigator" class="btn btn-icon btn-inverse" style="display: none">
                                <i class="icon icon-search-plus"></i>
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
                <h1 class="tile-title text-truncate"><mvc:message code="workflow.element.edit"/></h1>
            </div>
            <div class="tile-body js-scrollable" id="node-editor" data-action="save-node">
                <div id="select-node-notification" class="notification-simple">
                    <i class="icon icon-info-circle"></i>
                    <mvc:message code="workflow.element.select"/>
                </div>
                <jsp:include page="editors/workflow-start-editor.jsp"/>
                <jsp:include page="editors/workflow-decision-editor.jsp"/>
                <jsp:include page="editors/workflow-deadline-editor.jsp"/>
                <jsp:include page="editors/workflow-parameter-editor.jsp"/>
                <jsp:include page="editors/workflow-recipient-editor.jsp"/>
                <jsp:include page="editors/workflow-archive-editor.jsp"/>
                <jsp:include page="editors/workflow-mailing-editor.jsp"/>
                <jsp:include page="editors/workflow-action-based-mailing-editor.jsp"/>
                <jsp:include page="editors/workflow-date-based-mailing-editor.jsp"/>
                <jsp:include page="editors/workflow-followup-mailing-editor.jsp"/>
                <jsp:include page="editors/workflow-import-editor.jsp"/>
                <jsp:include page="editors/workflow-export-editor.jsp"/>
                <jsp:include page="editors/workflow-sms-mailing-editor.jsp"/>
                <jsp:include page="editors/workflow-post-mailing-editor.jsp"/>
                <jsp:include page="editors/workflow-icon-comment-editor.jsp"/>
            </div>
        </div>
    </div>
    
    <%-- Needed when some editor should be opened by default (i.e. if we back to campaign after new mailing creation)--%>
    <script data-initializer="open-edit-icon-initializer" type="application/json">
        {
            "nodeId": "${nodeId}",
            "elementValue": "${elementValue}"
        }
    </script>

    <script type="text/javascript">
        //fix problem with not expected behavior of JSON.stringify() for arrays
        if (window.Prototype) {
            delete Array.prototype.toJSON;
        }
    </script>
</div>

<%@include file="fragments/modal/mailing-data-transfer-modal.jspf" %>
<%@include file="fragments/modal/workflow-activate-modal.jspf" %>
<%@include file="fragments/modal/workflow-simple-dialog-modal.jspf" %>
<%@include file="fragments/modal/workflow-copy-modal.jspf" %>
<%@include file="fragments/modal/own-workflow-expanding-modal.jspf" %>
<%@include file="fragments/modal/workflow-create-auto-opt-modal.jspf" %>
<%@include file="fragments/modal/workflow-save-before-pdf-modal.jspf" %>
<c:if test="${workflowToggleTestingButtonEnabled}">
    <%@include file="fragments/modal/workflow-testing-modal.jspf" %>
</c:if>
