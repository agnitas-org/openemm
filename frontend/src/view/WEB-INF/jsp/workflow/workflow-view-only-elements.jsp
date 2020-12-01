<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.impl.WorkflowDeadlineImpl" %>
<%@ page import="org.agnitas.target.ChainOperator" %>

<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

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

<tiles:insert attribute="page-setup"/>

<html>

<tiles:insert attribute="head-tag"/>
<body style="background-color: #fff">
<div class="emm-container" data-controller="workflow-view">
    <script data-initializer="workflow-view-constants" type="application/json">
        {
            "startTypeOpen": "<%= WorkflowStart.WorkflowStartType.OPEN %>",
            "startTypeDate": "<%= WorkflowStart.WorkflowStartType.DATE %>",
            "startTypeEvent": "<%= WorkflowStart.WorkflowStartType.EVENT %>",
            "endTypeAutomatic": "<%= WorkflowStop.WorkflowEndType.AUTOMATIC %>",
            "endTypeDate": "<%= WorkflowStop.WorkflowEndType.DATE %>",
            "startEventReaction": "<%= WorkflowStart.WorkflowStartEventType.EVENT_REACTION %>",
            "startEventDate": "<%= WorkflowStart.WorkflowStartEventType.EVENT_DATE %>",
            "deadlineTypeDelay": "<%= WorkflowDeadline.WorkflowDeadlineType.TYPE_DELAY %>",
            "deadlineTypeFixedDeadline": "<%= WorkflowDeadline.WorkflowDeadlineType.TYPE_FIXED_DEADLINE %>",
            "deadlineTimeUnitMinute": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE %>",
            "deadlineTimeUnitHour": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR %>",
            "deadlineTimeUnitDay": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_DAY %>",
            "deadlineTimeUnitWeek": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_WEEK %>",
            "deadlineTimeUnitMonth": "<%= WorkflowDeadline.WorkflowDeadlineTimeUnit.TIME_UNIT_MONTH %>",
            "defaultImportDelayLimit" : "<%=WorkflowDeadlineImpl.DEFAULT_AUTOIMPORT_DELAY_LIMIT%>",
            "reactionOpened": "<%=  WorkflowReactionType.OPENED %>",
            "reactionNotOpened": "<%=  WorkflowReactionType.NOT_OPENED %>",
            "reactionClicked": "<%=  WorkflowReactionType.CLICKED %>",
            "reactionNotClicked": "<%=  WorkflowReactionType.NOT_CLICKED %>",
            "reactionBought": "<%=  WorkflowReactionType.BOUGHT %>",
            "reactionNotBought": "<%=  WorkflowReactionType.NOT_BOUGHT %>",
            "reactionDownload": "<%=  WorkflowReactionType.DOWNLOAD %>",
            "reactionChangeOfProfile": "<%=  WorkflowReactionType.CHANGE_OF_PROFILE %>",
            "reactionWaitingForConfirm": "<%=  WorkflowReactionType.WAITING_FOR_CONFIRM %>",
            "reactionOptIn": "<%=  WorkflowReactionType.OPT_IN %>",
            "reactionOptOut": "<%=  WorkflowReactionType.OPT_OUT %>",
            "reactionClickedLink": "<%=  WorkflowReactionType.CLICKED_LINK %>",
            "reactionOpenedAndClicked": "<%=  WorkflowReactionType.OPENED_AND_CLICKED %>",
            "reactionOpenedOrClicked": "<%=  WorkflowReactionType.OPENED_OR_CLICKED %>",
            "reactionConfirmedOptIn": "<%=  WorkflowReactionType.CONFIRMED_OPT_IN %>",
            "decisionTypeDecision": "<%= WorkflowDecision.WorkflowDecisionType.TYPE_DECISION %>",
            "decisionTypeAutoOptimization": "<%= WorkflowDecision.WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION %>",
            "decisionReaction": "<%= WorkflowDecision.WorkflowDecisionCriteria.DECISION_REACTION %>",
            "decisionProfileField": "<%= WorkflowDecision.WorkflowDecisionCriteria.DECISION_PROFILE_FIELD %>",
            "decisionAOCriteriaClickRate": "<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE %>",
            "decisionAOCriteriaOpenrate": "<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE %>",
            "decisionAOCriteriaTurnover": "<%= WorkflowDecision.WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE %>",
            "genderOptions": {
                "<%= Recipient.GENDER_MALE %>": "Male",
                "<%= Recipient.GENDER_FEMALE %>": "Female",
                "<%= Recipient.GENDER_UNKNOWN %>": "Unknown"
            },
            "chainOperatorOptions": {
                "<%= ChainOperator.AND.getOperatorCode() %>": "<bean:message key="default.and"/>",
                "<%= ChainOperator.OR.getOperatorCode() %>": "<bean:message key="default.or"/>"
            },
            "operators": [
                <c:forEach items="${operators}" var="operator" varStatus="index">
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
                <c:forEach  items="${operators}" var="operator" varStatus="index">
                  "${operator.operatorCode}": "${operator.eqlSymbol}"${!index.last ? ',':''}
                </c:forEach>
            },
            "mailingThumbnailURL" : "<c:url value='/workflow/getMailingThumbnail.action'/>",
            "componentURL" : "<c:url value='/sc?compID={component-id}'/>"
        }
    </script>

    <emm:setAbsolutePath var="absoluteImagePath" path="${emmLayoutBase.imagesURL}"/>

    <script type="application/json" data-initializer="workflow-pdf-initialize">
        {
            "sessionId": "${pageContext.session.id}",
            "imageUrl": "${absoluteImagePath}",
            "locale": "<bean:write name="emm.admin" property="adminLang" scope="session"/>",
            "icons": ${workflowForm.workflowSchema},
            "editorPositionLeft": "${workflowForm.editorPositionLeft}",
            "editorPositionTop": "${workflowForm.editorPositionTop}",
            "localeDateNTimePattern": "${localeDateNTimePattern}",
            "noContextMenu": true,
            "workflowId": "${workflowForm.workflowId}",
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
    <div id="viewPort"></div>
    <footer id="footnotes-container">
        <table id="comment-footnotes-list"></table>
    </footer>
    <script type="application/javascript">
        jQuery(window).on('load', function() {
         function loading() {
           if (window.status == 'initializerFinished') {
             jQuery(".iconNode .node-image")
               .imagesLoaded()
               .always(function(){window.status = "wmLoadFinished";});
           } else {
             window.setTimeout(loading, 100);
           }
         }
         loading();
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
        <c:if test="${not isWkhtmltopdfUsage}">
            <jsp:include page="editors/workflow-start-editor.jsp"/>
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
            <jsp:include page="editors/workflow-ownworkflow-usecopy-dialog.jsp"/>
            <jsp:include page="workflow-copy-dialog.jsp">
                <jsp:param name="workflowId" value="${workflowForm.workflowId}"/>
            </jsp:include>
            <jsp:include page="workflow-undoHistoryIsEmpty-dialog.jsp"/>
        </c:if>
    </div>
</div>

</body>
</html>
