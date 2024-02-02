<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowDecisionCriteria" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision.WorkflowAutoOptimizationCriteria" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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
<%@include file="../fragments/workflow-decision-editor-revenue-settigs.jspf" %>

<div id="decision-editor" data-initializer="decision-editor-initializer">
    <mvc:form action="" id="decisionForm" name="decisionForm">

        <div class="status_error editor-error-messages well" style="display: none;">
        </div>

        <input name="id" type="hidden">
        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <c:choose>
                    <c:when test="${anonymizeAllRecipients}">
                        <label class="radio-inline" data-tooltip="<mvc:message code='hint.workflow.followup.trackingveto'/>">
                            <input type="radio" name="decisionType" id="typeDecision" data-action="decision-editor-type-change" value="${TYPE_DECISION}" data-tooltip="<mvc:message code='hint.workflow.followup.trackingveto'/>" disabled>
                            <mvc:message code="workflow.decision"/>
                        </label>
                    </c:when>
                    <c:otherwise>
                        <label class="radio-inline">
                            <input type="radio" name="decisionType" id="typeDecision" data-action="decision-editor-type-change" checked="checked" value="${TYPE_DECISION}">
                            <mvc:message code="workflow.decision"/>
                        </label>
                    </c:otherwise>
                </c:choose>
                <label class="radio-inline">
                    <input type="radio" name="decisionType" id="typeAutoOptimization" data-action="decision-editor-type-change" class="decision-type-radio" ${anonymizeAllRecipients ? "checked='checked'" : ""} value="${TYPE_AUTO_OPTIMIZATION}">
                    <mvc:message code="mailing.autooptimization"/>
                </label>
            </div>
        </div>

        <div class="form-group" id="ruleMailingReceivedWrapper">
            <div class="col-sm-8 col-sm-push-4">
                <p class="form-control-static"><mvc:message code="workflow.decision.ruleMailingReceived"/></p>
            </div>
        </div>

        <div id="decisionPanel" >
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="decisionCriteria">
                        <mvc:message code="campaign.autoopt.evaltype"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <select id="decisionCriteria" name="decisionCriteria" class="form-control" data-action="decision-editor-criteria-change">
                        <option value="${DECISION_REACTION}"><mvc:message code="workflow.Reaction"/></option>
                        <option value="${DECISION_PROFILE_FIELD}"><mvc:message code="workflow.start.ProfileField"/></option>
                    </select>
                </div>
            </div>

            <div id="decisionReactionPanel">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="decisionReaction">
                            <mvc:message code="workflow.Reaction"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <select id="decisionReaction" name="reaction" class="form-control" data-action="decision-editor-reaction-change">
                            <option value="${REACTION_OPENED}"><mvc:message code="statistic.opened"/></option>
                            <option value="${REACTION_CLICKED}"><mvc:message code="default.clicked"/></option>
                            <option value="${REACTION_CLICKED_LINK}"><mvc:message code="workflow.reaction.ClickedOnLink"/></option>
                            <option value="${REACTION_OPENED_AND_CLICKED}"><mvc:message code="workflow.reaction.OpenedAndClicked"/></option>
                            <option value="${REACTION_OPENED_OR_CLICKED}"><mvc:message code="workflow.reaction.OpenedOrClicked"/></option>
                            <c:if test="${hasDeepTrackingTables}">
                                <option value="${REACTION_BOUGHT}"><mvc:message code="workflow.reaction.Bought"/></option>
                            </c:if>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="includeVetoed" class="control-label checkbox-control-label">
                            <mvc:message code="recipient.trackingVeto"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/workflow/decision/TrackingVeto.xml" tabindex="-1" type="button"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <label class="toggle">
                            <input id="includeVetoed" name="includeVetoed" value="true" type="checkbox">
                            <div class="toggle-control"></div>
                            <span class="text">
                               <mvc:message code="recipient.trackingVeto.include"/>
                           </span>
                        </label>
                    </div>
                </div>

                <jsp:include page="workflow-mailing-selector.jsp">
                    <jsp:param name="mailingSelectorSorter" value="decision-editor-mailing-sort"/>
                    <jsp:param name="mailingSelectorEventHandler" value="decision-editor-mailing-select"/>
                    <jsp:param name="selectName" value="mailingId"/>
                    <jsp:param name="mailingLabelClass" value="decision-editor-label"/>
                    <jsp:param name="mailingFieldClass" value="decision-editor-field"/>
                </jsp:include>

                <div id="reactionLinkPanel">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <mvc:message code="workflow.decision.ChooseLink"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <select name="linkId" class="form-control">
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            <div id="decisionProfileFieldPanel">
                <c:if test="${not isMailtrackingActive}">
                    <div class="form-group">
                        <div class="col-sm-4"></div>
                        <div class="col-sm-8">
                            <div class="well"><mvc:message code="mailtrackingRequired"/></div>
                        </div>
                    </div>

                    <div style="display: none;">
                </c:if>

                        <div class="form-group">
                            <div class="col-sm-4">
                                <label for="decisionProfileField" class="control-label">
                                    <mvc:message code="workflow.start.ProfileField"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <select id="decisionProfileField" name="profileField" class="form-control js-select" data-action="decision-editor-profile-field-change">
                                    <option value="">--</option>
                                    <c:forEach var="profileField" items="${profileFields}">
                                        <option value="${profileField.column}">${profileField.shortname}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>

                        <div id="decisionDateFormat">
                            <div class="form-group">
                                <div class="col-sm-4">
                                    <label for="decisionProfileFieldDateFormat" class="control-label">
                                        <mvc:message code="import.dateFormat"/>
                                    </label>
                                </div>
                                <div class="col-sm-8">
                                    <select id="decisionProfileFieldDateFormat" name="dateFormat" class="form-control">
                                        <option value="yyyymmdd"><mvc:message code="default.date.format.YYYYMMDD" /></option>
                                        <option value="mmdd"><mvc:message code="default.date.format.MMDD" /></option>
                                        <option value="yyyymm"><mvc:message code="default.date.format.YYYYMM" /></option>
                                        <option value="dd"><mvc:message code="default.date.format.DD" /></option>
                                        <option value="mm"><mvc:message code="default.date.format.MM" /></option>
                                        <option value="yyyy"><mvc:message code="default.date.format.YYYY" /></option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div id="decisionProfileFieldRules">
                            <div class="form-group">
                                <div class="col-sm-8 col-sm-push-4">
                                    <div class="row">
                                        <table class="table table-bordered table-form">
                                            <tbody id="decisionProfileFieldAddedRules"></tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>

                            <div id="decisionProfileFieldRuleAdd">
                                <div class="form-group">
                                    <div class="col-sm-8 col-sm-push-4">
                                        <div class="row">
                                            <table class="table table-bordered table-form">
                                                <tbody>
                                                    <tr>
                                                        <td>
                                                            <select id="decision_newRule_chainOperator">
                                                                <option value="${CHAIN_OPERATOR_AND}"><mvc:message code="default.and"/></option>
                                                                <option value="${CHAIN_OPERATOR_OR}"><mvc:message code="default.or"/></option>
                                                            </select>
                                                        </td>
                                                        <td>
                                                            <select id="decision_newRule_parenthesisOpened" class="parentheses-opened">
                                                                <option value="0">&nbsp;</option>
                                                                <option value="1">(</option>
                                                            </select>
                                                        </td>
                                                        <td>
                                                            <select id="decision_newRule_primaryOperator" class="decision-rule-operator" data-action="decision-rule-operator-change">
                                                                <c:forEach var="operator" items="${operators}">
                                                                    <c:set var="types" value="${operatorsTypeSupportMap[operator]}"/>
                                                                    <option data-types="${types}" value="${operator.operatorCode}">${operator.eqlSymbol}</option>
                                                                </c:forEach>
                                                            </select>
                                                        </td>
                                                        <td>
                                                            <input id="decision_newRule_primaryValue" type="text" class="form-control primary-value"/>
                                                        </td>
                                                        <td>
                                                            <select id="decision_newRule_parenthesisClosed" class="parenthesis-closed">
                                                                <option value="0">&nbsp;</option>
                                                                <option value="1">)</option>
                                                            </select>
                                                        </td>
                                                        <td>
                                                            <a class="btn btn-regular btn-secondary advanced_search_add add-rule disable-for-active" data-action="decision-editor-rule-add" href="#" data-tooltip="<mvc:message code="button.Add"/>">
                                                                <i class="icon icon-plus-circle"></i>
                                                            </a>
                                                        </td>
                                                    </tr>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                <c:if test="${not isMailtrackingActive}">
                    </div>
                </c:if>
            </div>
        </div>

        <div id="autoOptimizationPanel">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="aoDecisionCriteria">
                        <mvc:message code="campaign.autoopt.evaltype"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <select id="aoDecisionCriteria" name="aoDecisionCriteria" class="form-control" data-action="decision-editor-criteria-change">
                        <option value="${AO_CRITERIA_OPENRATE}"><mvc:message code="workflow.decision.OpeningRate"/></option>
                        <option value="${AO_CRITERIA_CLICKRATE}"><mvc:message code="Clickrate"/></option>
                        <c:if test="${isRevenueCrteriaEnabled}">
                             <option value="${AO_CRITERIA_REVENUE}"><mvc:message code="statistic.revenue"/></option>
                        </c:if>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <label for="threshold"><mvc:message code="mailing.autooptimization.threshold"/></label>
                        <button class="icon icon-help" data-help="help_${helplanguage}/workflow/decision/Threshold.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <input type="text" id="threshold" name="threshold" class="form-control"/>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <label for="decisionDate"><mvc:message code="settings.fieldType.DATE"/></label>
                        <button class="icon icon-help" data-help="help_${helplanguage}/workflow/decision/Date.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <input type="text" id="decisionDate" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}', min: true"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="decisionTime">
                        <mvc:message code="Time"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <input type="text" id="decisionTime" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'"/>
                        </div>
                        <div class="input-group-addon">
                            <span class="addon">
                                <i class="icon icon-clock-o"></i>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <hr>

        <div class="col-xs-12">
            <div class="form-group">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <mvc:message code="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="decision-editor-save">
                        <mvc:message code="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </mvc:form>

    <emm:instantiate var="decisionProfileFieldsTypes" type="java.util.HashMap">
        <c:forEach var="profileField" items="${profileFields}">
            <c:set target="${decisionProfileFieldsTypes}" property="${profileField.column}" value="${profileField.dataType}"/>
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
