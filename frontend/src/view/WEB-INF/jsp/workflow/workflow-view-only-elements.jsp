<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
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

<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<%--@elvariable id="workflowForm" type="com.agnitas.emm.core.workflow.web.forms.WorkflowForm"--%>
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

<tiles:insertAttribute name="page-setup"/>

<html>

<tiles:insertAttribute name="head-tag"/>
<body style="background-color: #fff">

<div class="emm-container" data-controller="workflow-view">

    <emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>

    <script type="application/json" data-initializer="workflow-view">
        {
            "icons": ${workflowForm.workflowSchema},
            "workflowId": ${workflowForm.workflowId},
            "shortname": "${workflowForm.shortname}",
            "accessLimitTargetId": ${accessLimitTargetId},
            "isEditable": false,
            "isContextMenuEnabled": false,
            "isMinimapEnabled": false,
            "fitPdfPage": true,
            "isFootnotesEnabled": true,
            "initializerFinishStatus": "initializerPdfFinished",
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
                "isAltgExtended" : ${not empty adminAltgs}
	        }
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
            </br>
        </div>
    </script>

    <div id="viewPort">
        <div id="canvas">
            <div id="icon-titles-container"></div>
        </div>
    </div>

    <div id="footnotes-container" class="footnotes"><ol></ol></div>

    <script type="application/javascript">
        jQuery(window).on('load', function() {
             function loading() {
               if (window.status == 'initializerPdfFinished') {
                 var images = jQuery('.node .node-image');
                 if (images.length > 0) {
                   images
                     .imagesLoaded()
                     .always(function(){window.status = "wmLoadFinished";});
                 } else {
                   window.status = "wmLoadFinished";
                 }
               } else {
                 window.setTimeout(loading, 100);
               }
             }
             loading();
             return false;
        }).on('error', function (e) {
          window.status = "wmLoadFinished";
          return false;
        });
    </script>

    <div id="invisible">
        <div id="connectRapidButton">
            <c:url var="icon_arrow_rapid" value="/assets/core/images/campaignManager/icon_arrow_rapid.png"/>
            <img src="${icon_arrow_rapid}" alt="arrow">
        </div>
    </div>
</div>

</body>
</html>
