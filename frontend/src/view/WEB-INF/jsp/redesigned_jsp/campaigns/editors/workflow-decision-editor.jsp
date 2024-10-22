<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionCriteria" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="profileFields" type="java.util.List<com.agnitas.beans.ComProfileField>"--%>
<%--@elvariable id="anonymizeAllRecipients" type="java.lang.Boolean"--%>

<c:set var="TYPE_DECISION" value="<%= WorkflowDecisionType.TYPE_DECISION %>"/>
<c:set var="TYPE_AUTO_OPTIMIZATION" value="<%= WorkflowDecisionType.TYPE_AUTO_OPTIMIZATION %>"/>

<c:set var="DECISION_REACTION" value="<%= WorkflowDecisionCriteria.DECISION_REACTION %>"/>
<c:set var="DECISION_PROFILE_FIELD" value="<%= WorkflowDecisionCriteria.DECISION_PROFILE_FIELD %>"/>

<c:set var="AO_CRITERIA_CLICKRATE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_CLICKRATE %>"/>
<c:set var="AO_CRITERIA_OPENRATE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_OPENRATE %>"/>
<c:set var="AO_CRITERIA_REVENUE" value="<%= WorkflowAutoOptimizationCriteria.AO_CRITERIA_REVENUE %>"/>

<c:set var="REACTION_OPENED" value="<%= WorkflowReactionType.OPENED %>"/>
<c:set var="REACTION_CLICKED" value="<%= WorkflowReactionType.CLICKED %>"/>
<c:set var="REACTION_CLICKED_LINK" value="<%= WorkflowReactionType.CLICKED_LINK %>"/>
<c:set var="REACTION_OPENED_AND_CLICKED" value="<%= WorkflowReactionType.OPENED_AND_CLICKED %>"/>
<c:set var="REACTION_OPENED_OR_CLICKED" value="<%= WorkflowReactionType.OPENED_OR_CLICKED %>"/>
<c:set var="REACTION_BOUGHT" value="<%= WorkflowReactionType.BOUGHT %>"/>
<c:set var="REACTION_CONFIRMED_OPT_IN" value="<%= WorkflowReactionType.CONFIRMED_OPT_IN %>"/>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<c:set var="CHAIN_OPERATOR_AND" value="<%= ChainOperator.AND.getOperatorCode() %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= ChainOperator.OR.getOperatorCode() %>"/>

<c:set var="isRevenueCrteriaEnabled" value="false"/>
<%@include file="fragments/workflow-decision-editor-revenue-settigs.jspf" %>

<div id="decision-editor" data-initializer="decision-editor-initializer">
    <mvc:form action="" id="decisionForm" name="decisionForm" cssClass="form-column">
        <input name="id" type="hidden">

        <select name="decisionType" id="" data-action="decision-editor-type-change" class="form-control js-select">
            <c:choose>
                <c:when test="${anonymizeAllRecipients}">
                    <option id="typeDecision" value="${TYPE_DECISION}"
                            data-tooltip="<mvc:message code='hint.workflow.followup.trackingveto'/>" disabled>
                        <mvc:message code="workflow.decision"/>
                    </option>
                </c:when>
                <c:otherwise>
                    <option id="typeDecision" value="${TYPE_DECISION}" selected><mvc:message
                            code="workflow.decision"/></option>
                </c:otherwise>
            </c:choose>
            <option id="typeAutoOptimization" value="${TYPE_AUTO_OPTIMIZATION}"
                ${anonymizeAllRecipients ? "selected" : ""}><mvc:message code="mailing.autooptimization"/></option>
        </select>

        <div id="ruleMailingReceivedWrapper">
            <p><mvc:message code="workflow.decision.ruleMailingReceived"/></p>
        </div>

        <div id="decisionPanel" class="form-column">
            <div>
                <label class="form-label" for="decisionCriteria"><mvc:message code="campaign.autoopt.evaltype"/></label>
                <select id="decisionCriteria" name="decisionCriteria" class="form-control js-select"
                        data-action="decision-editor-criteria-change">
                    <option value="${DECISION_REACTION}"><mvc:message code="workflow.Reaction"/></option>
                    <option value="${DECISION_PROFILE_FIELD}"><mvc:message code="workflow.start.ProfileField"/></option>
                </select>
            </div>

            <div id="decisionReactionPanel" class="form-column">
                <div>
                    <label class="form-label" for="decisionReaction"><mvc:message code="workflow.Reaction"/></label>
                    <select id="decisionReaction" name="reaction" class="form-control js-select"
                            data-action="decision-editor-reaction-change">
                        <option value="${REACTION_OPENED}"><mvc:message code="statistic.opened"/></option>
                        <option value="${REACTION_CLICKED}"><mvc:message code="default.clicked"/></option>
                        <option value="${REACTION_CLICKED_LINK}"><mvc:message
                                code="workflow.reaction.ClickedOnLink"/></option>
                        <option value="${REACTION_OPENED_AND_CLICKED}"><mvc:message
                                code="workflow.reaction.OpenedAndClicked"/></option>
                        <option value="${REACTION_OPENED_OR_CLICKED}"><mvc:message
                                code="workflow.reaction.OpenedOrClicked"/></option>
                        <c:if test="${hasDeepTrackingTables}">
                            <option value="${REACTION_BOUGHT}"><mvc:message code="workflow.reaction.Bought"/></option>
                        </c:if>
                    </select>
                </div>

                <div class="form-check form-switch">
                    <input id="includeVetoed" name="includeVetoed" value="true" class="form-check-input" type="checkbox"
                           role="switch">
                    <label class="form-label form-check-label" for="includeVetoed">
                        <mvc:message code="recipient.trackingVeto"/>
                        <a href="#" class="icon icon-question-circle"
                           data-help="help_${helplanguage}/workflow/decision/TrackingVeto.xml"></a>
                    </label>
                </div>

                <div>
                    <label class="form-label"><mvc:message code="Mailing"/></label>
                    <select name="mailingId" data-action="decision-editor-mailing-select" class="form-control js-select"></select>
                </div>

                <div id="reactionLinkPanel">
                    <label class="form-label"><mvc:message code="workflow.decision.ChooseLink"/></label>
                    <select name="linkId" class="form-control js-select"></select>
                </div>
            </div>

            <div id="decisionProfileFieldPanel" class="form-column">
                <c:if test="${not isMailtrackingActive}">
                <div class="notification-simple notification-info">
                    <mvc:message code="mailtrackingRequired"/>
                </div>
                <div style="display: none;">
                    </c:if>
                    <div>
                        <label for="decisionProfileField" class="form-label"><mvc:message code="workflow.start.ProfileField"/></label>
                        <select id="decisionProfileField" name="profileField" class="form-control js-select"
                                data-action="decision-editor-profile-field-change">
                            <option value="">--</option>
                            <c:forEach var="profileField" items="${profileFields}">
                                <option value="${profileField.column}">${profileField.shortname}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <div id="decisionDateFormat">
                        <label for="decisionProfileFieldDateFormat" class="form-label"><mvc:message code="import.dateFormat"/></label>
                        <select id="decisionProfileFieldDateFormat" name="dateFormat" class="form-control js-select">
                            <option value="yyyymmdd"><mvc:message code="default.date.format.YYYYMMDD"/></option>
                            <option value="mmdd"><mvc:message code="default.date.format.MMDD"/></option>
                            <option value="yyyymm"><mvc:message code="default.date.format.YYYYMM"/></option>
                            <option value="dd"><mvc:message code="default.date.format.DD"/></option>
                            <option value="mm"><mvc:message code="default.date.format.MM"/></option>
                            <option value="yyyy"><mvc:message code="default.date.format.YYYY"/></option>
                        </select>
                    </div>

                    <div id="decisionProfileFieldRules">
                        <%-- populated with js. see class FieldRulesTable--%>
                    </div>

                    <c:if test="${not isMailtrackingActive}">
                </div>
                </c:if>
            </div>
        </div>

        <div id="autoOptimizationPanel" class="form-column">
            <div>
                <label class="form-label" for="aoDecisionCriteria"><mvc:message code="campaign.autoopt.evaltype"/></label>
                <select id="aoDecisionCriteria" name="aoDecisionCriteria" class="form-control js-select"
                        data-action="decision-editor-criteria-change">
                    <option value="${AO_CRITERIA_OPENRATE}"><mvc:message code="workflow.decision.OpeningRate"/></option>
                    <option value="${AO_CRITERIA_CLICKRATE}"><mvc:message code="Clickrate"/></option>
                    <c:if test="${isRevenueCrteriaEnabled}">
                        <option value="${AO_CRITERIA_REVENUE}"><mvc:message code="statistic.revenue"/></option>
                    </c:if>
                </select>
            </div>

            <div>
                <label class="form-label">
                    <label for="threshold"><mvc:message code="mailing.autooptimization.threshold"/></label>
                    <a href="#" class="icon icon-question-circle"
                       data-help="help_${helplanguage}/workflow/decision/Threshold.xml"></a>
                </label>
                <input type="text" id="threshold" name="threshold" class="form-control"/>
            </div>

            <div id="decision-date-time-inputs">
                <label class="form-label">
                    <label for="decisionDate"><mvc:message code="default.date.time"/></label>
                    <a href="#" class="icon icon-question-circle"
                       data-help="help_${helplanguage}/workflow/decision/Date.xml"></a>
                </label>
                <div class="date-time-container">
                    <div class="date-picker-container">
                        <input type="text" id="decisionDate" class="form-control js-datepicker"
                               data-datepicker-options="minDate: 0"/>
                    </div>
                    <div class="time-picker-container">
                        <input type="text" id="decisionTime" class="form-control js-timepicker"
                               data-timepicker-options="mask: 'h:s'"/>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>

    <emm:instantiate var="decisionProfileFieldsTypes" type="java.util.HashMap">
        <c:forEach var="profileField" items="${profileFields}">
            <c:set target="${decisionProfileFieldsTypes}" property="${profileField.column}"
                   value="${profileField.dataType}"/>
        </c:forEach>
    </emm:instantiate>

    <script id="config:decision-editor-initializer" type="application/json">
        {
            "form": "decisionForm",
            "container": "#decisionReactionPanel",
            "sessionId": "${pageContext.session.id}",
            "selectName": "mailingId",
            "noMailingOption": "true",
            "isMailtrackingActive": "${isMailtrackingActive}",
            "profileFields": ${emm:toJson(decisionProfileFieldsTypes)}
        }
    </script>
</div>
