<!DOCTYPE html>
<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ page import="com.agnitas.beans.Recipient" %>
<%@ page import="com.agnitas.emm.core.target.beans.ChainOperator" %>
<%@ page import="com.agnitas.emm.core.target.beans.ConditionalOperator" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowRecipient" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl" %>
<%@ page import="com.agnitas.emm.core.workflow.web.forms.WorkflowForm.WorkflowStatus" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowForward" %>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"  %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"    uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
<%--@elvariable id="accessLimitTargetId" type="java.lang.Integer"--%>
<%--@elvariable id="statisticUrl" type="java.lang.String"--%>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<c:set var="STATUS_OPEN" value="<%= WorkflowStatus.STATUS_OPEN %>" scope="page"/>
<c:set var="STATUS_ACTIVE" value="<%= WorkflowStatus.STATUS_ACTIVE %>" scope="page"/>
<c:set var="STATUS_INACTIVE" value="<%= WorkflowStatus.STATUS_INACTIVE %>" scope="page"/>
<c:set var="STATUS_TESTING" value="<%= WorkflowStatus.STATUS_TESTING %>" scope="page"/>
<c:set var="STATUS_PAUSED" value="<%= WorkflowStatus.STATUS_PAUSED %>" scope="page"/>
<c:set var="CHAIN_OPERATOR_AND" value="<%= ChainOperator.AND.getOperatorCode() %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= ChainOperator.OR.getOperatorCode() %>"/>
<c:set var="OPERATOR_IS" value="<%= ConditionalOperator.IS.getOperatorCode() %>"/>

<c:url var="iconSpriteLocation" value="/assets/core/images/campaignManager/campaign-icon-sprite.svg"/>

<script id="workflow-node" type="text/x-mustache-template">
    <%-- Toggle 'active' class to toggle active/inactive node images --%>
    <div class="node" rel="popover">
        <div class="node-comment-image">
            <svg><use href="${iconSpriteLocation}#comment"></use></svg>
        </div>
        <svg class="node-image">
            <use href="${iconSpriteLocation.concat('#')}{{- type }}"></use>
        </svg>
        <div class="icon-overlay-title"></div>
        <div class="node-connect-button">
            <svg><use href="${iconSpriteLocation}#arrow"></use></svg>
        </div>
    </div>
</script>
    
<script id="workflow-icon-title" type="text/x-mustache-template">
    <div class="icon-title" style="display: none;">
        <span class="icon-title-span" style="white-space: pre-line;"></span>
        <br>
    </div>
</script>

<tiles:insertAttribute name="page-setup"/>

<html>

<tiles:insertAttribute name="head-tag"/>
<body style="background-color: #fff">

<div data-controller="workflow-view">
    <script type="application/json" data-initializer="workflow-view">
        {
            "isEditable": false,
            "isContextMenuEnabled": false,
            "isMinimapEnabled": false,
            "fitPdfPage": true,
            "isFootnotesEnabled": true,
            "initializerFinishStatus": "initializerPdfFinished",
            "icons": ${workflowForm.workflowSchema},
            "workflowId": ${workflowForm.workflowId},
            "shortname": ${emm:toJson(workflowForm.shortname)},
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
            "accessLimitTargetId": ${accessLimitTargetId}
        }
    </script>

    <div id="pageCampaignEditorContainer">
        <div id="viewPort">
            <div id="canvas">
                <div id="icon-titles-container"></div>
            </div>
        </div>
        <div id="footnotes-container" class="footnotes"><ol></ol></div>
    </div>

    <script type="application/javascript">
      jQuery(window).on('load', function() {
        function loading() {
          if (window.waitStatus === 'initializerPdfFinished') {
            var images = jQuery('.node .node-image');
            if (images.length > 0) {
              images
                .imagesLoaded()
                .always(function(){window.waitStatus = "wmLoadFinished";});
            } else {
              window.waitStatus = "wmLoadFinished";
            }
          } else {
            window.setTimeout(loading, 100);
          }
        }
        loading();
        return false;
      }).on('error', function (e) {
        window.waitStatus = "wmLoadFinished";
        return false;
      });
    </script>
</div>
</body>
</html>
