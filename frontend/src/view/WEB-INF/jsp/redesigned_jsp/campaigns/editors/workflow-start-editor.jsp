<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="com.agnitas.emm.core.target.beans.ConditionalOperator" %>
<%@ page import="com.agnitas.emm.core.target.beans.ChainOperator" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="TYPE_DATE" value="<%= WorkflowStartType.DATE %>"/>
<c:set var="TYPE_EVENT" value="<%= WorkflowStartType.EVENT %>"/>

<c:set var="END_TYPE_AUTOMATIC" value="<%= WorkflowStop.WorkflowEndType.AUTOMATIC %>"/>
<c:set var="END_TYPE_DATE" value="<%= WorkflowStop.WorkflowEndType.DATE %>"/>

<c:set var="EVENT_REACTION" value="<%= WorkflowStartEventType.EVENT_REACTION %>"/>
<c:set var="EVENT_DATE" value="<%= WorkflowStartEventType.EVENT_DATE %>"/>

<c:set var="REACTION_OPENED" value="<%= WorkflowReactionType.OPENED %>"/>
<c:set var="REACTION_CLICKED" value="<%= WorkflowReactionType.CLICKED %>"/>
<c:set var="REACTION_CHANGE_OF_PROFILE" value="<%= WorkflowReactionType.CHANGE_OF_PROFILE %>"/>
<c:set var="REACTION_WAITING_FOR_CONFIRM" value="<%= WorkflowReactionType.WAITING_FOR_CONFIRM %>"/>
<c:set var="REACTION_OPT_IN" value="<%= WorkflowReactionType.OPT_IN %>"/>
<c:set var="REACTION_OPT_OUT" value="<%= WorkflowReactionType.OPT_OUT %>"/>
<c:set var="REACTION_CLICKED_LINK" value="<%= WorkflowReactionType.CLICKED_LINK %>"/>

<c:set var="operators" value="<%= WorkflowDecision.DECISION_OPERATORS %>"/>
<c:set var="operatorsTypeSupportMap" value="<%= WorkflowDecision.OPERATOR_TYPE_SUPPORT_MAP %>"/>

<c:set var="equalOperator" value="<%= ConditionalOperator.EQ %>"/>

<c:set var="CHAIN_OPERATOR_AND" value="<%= ChainOperator.AND.getOperatorCode() %>"/>
<c:set var="CHAIN_OPERATOR_OR" value="<%= ChainOperator.OR.getOperatorCode() %>"/>

<c:set var="RECIPIENT_EMM" value="1"/>
<c:set var="RECIPIENT_CUSTOM" value="2"/>

<emm:instantiate var="profiles" type="java.util.LinkedHashMap">
    <c:set target="${profiles}" property="0" value=""/>
    <c:forEach var="field" items="${profileFields}">
        <c:set target="${profiles}" property="${field.column}" value="${field.dataType}"/>
    </c:forEach>
</emm:instantiate>

<c:set var="isBigData" value="false"/>
<%@include file="fragments/workflow-view-bigdata-settings.jspf"%>

<%-- Editor main container should have id="<icon type>-editor" - in that case the campaign manager will be able to find it --%>
<div id="start-editor" title="Define Start of a Campaign" data-initializer="start-editor-initializer">

    <mvc:form action="" id="startForm" name="startForm" cssClass="form-column">
        <input name="id" type="hidden">

        <div id="startStopType">
            <script id="start-types" type="text/x-mustache-template">
                <div>
                    <label for="start-type" class="form-label"><mvc:message code="default.Type"/></label>
                    <select name="startType" id="start-type" class="form-control js-select" data-action="start-editor-type-changed">
                        <option value="${TYPE_DATE}" id="typeDate"><mvc:message code="workflow.start.StartDate"/></option>
                        {{ if(showStartEventTab) { }}
                        <option value="${TYPE_EVENT}" id="typeEvent"><mvc:message code="workflow.start.StartEvent"/></option>
                        {{ } }}
                    </select>
                </div>
            </script>

            <script id="stop-types" type="text/x-mustache-template">
                <select name="endType" class="form-control js-select" data-action="start-editor-type-changed">
                    <option value="${END_TYPE_AUTOMATIC}"><span id="endTypeAutomaticLabel"><mvc:message code="workflow.stop.AutomaticEnd"/></span></option>
                    <option value="${END_TYPE_DATE}" id="end-date-radio"><mvc:message code="workflow.stop.EndDate"/></option>
                </select>
            </script>
        </div>

        <div id="startDatePanel">
            <label for="startDate" class="form-label text-truncate"><mvc:message code="default.date.time" /></label>
            <div class="date-time-container">
                <div class="date-picker-container">
                    <input type="text" id="startDate" class="form-control js-datepicker"/>
                </div>
            </div>
        </div>

        <div id="startEventPanel" class="form-column" style="display: none;">
            <div>
                <label for="startEvent" class="form-label"><mvc:message code="workflow.start.Event"/></label>
                <select data-action="start-editor-event-changed" id="startEvent" name="event" class="form-control js-select">
                    <option value="${EVENT_REACTION}" id="eventReaction"></option>
                    <option value="${EVENT_DATE}" id="eventDate"></option>
                </select>
            </div>

            <div id="reactionStartPanel" class="form-column">
                <div>
                    <label for="startReaction" class="form-label"><mvc:message code="workflow.Reaction"/></label>
                    <select id="startReaction" name="reaction" class="form-control js-select"
                            data-action="start-editor-reaction-changed">
                        <option value="${REACTION_OPENED}"><mvc:message code="statistic.opened"/></option>
                        <option value="${REACTION_CLICKED}"><mvc:message code="default.clicked"/></option>
                        <option value="${REACTION_CLICKED_LINK}"><mvc:message code="workflow.reaction.ClickedOnLink"/></option>
                        <c:if test="${isBigData}">
                            <option value="${REACTION_CHANGE_OF_PROFILE}"><mvc:message code="workflow.reaction.ChangeOfProfile"/></option>
                        </c:if>
                        <option value="${REACTION_WAITING_FOR_CONFIRM}"><mvc:message code="workflow.reaction.WaitingForConfirm"/></option>
                        <option value="${REACTION_OPT_IN}"><mvc:message code="workflow.reaction.OptIn"/></option>
                        <option value="${REACTION_OPT_OUT}"><mvc:message code="workflow.reaction.OptOut"/></option>
                    </select>
                </div>

                <div id="reactionStartMailing">
                    <label class="form-label"><mvc:message code="Mailing"/></label>
                    <div class="d-flex gap-1">
                        <select name="mailingId" data-action="start-editor-mailing-select" class="form-control js-select"></select>
                        <a id="edit-start-editor-mailing-btn" href="#" class="btn btn-icon btn-primary hidden"
                           data-action="mailing-editor-edit" data-tooltip="<mvc:message code="mailing.MailingEdit" />">
                            <i class="icon icon-pen"></i>
                        </a>
                    </div>
                </div>

                <div id="reactionStartMailingLink">
                    <label class="form-label" for="startReactionMailingLink"><mvc:message code="workflow.decision.ChooseLink"/></label>
                    <select id="startReactionMailingLink" name="linkId" class="form-control js-select"></select>
                </div>

                <div id="reactionStartProfile" class="form-column" style="display: none;">
                    <div id="reactionProfileField">
                        <label for="startProfileField" class="form-label"><mvc:message code="workflow.start.ProfileField"/></label>
                        <select id="startProfileField" name="profileField" class="form-control js-select" data-action="start-editor-profile-field-changed">
                            <option value="">--</option>
                            <c:forEach var="profileField" items="${profileFieldsHistorized}">
                                <option value="${profileField.columnName}">${profileField.shortName}</option>
                            </c:forEach>
                        </select>
                    </div>

                    <select name="useRules" class="form-control js-select" data-action="start-editor-rule-changed">
                        <option value="false" id="profileFieldRulesAny"><mvc:message code="workflow.start.rules.any"/></option>
                        <option value="true" id="profileFieldRulesExact"><mvc:message code="workflow.start.rules.exact"/></option>
                    </select>

                    <div id="profileFieldRules">
                        <%-- populated with js. see class FieldRulesTable--%>
                    </div>
                </div>

                <div>
                    <label class="form-label">
                        <mvc:message code="workflow.start.execution"/>
                        <a href="#" class="icon icon-question-circle" data-help="workflow/start/Execution.xml" tabindex="-1" type="button"></a>
                    </label>
                    <select name="executeOnce" class="form-control js-select" data-action="start-editor-execution-changed">
                        <option value="true" id="startExecuteOnce"><mvc:message code="workflow.start.execution.once"/></option>
                        <option value="false" id="startExecutePermanent"><mvc:message code="workflow.start.execution.permanent"/></option>
                    </select>
                </div>
            </div>

            <div id="dateStartPanel" class="form-column" style="display: none">
                <div>
                    <label for="dateProfileField" class="form-label"><mvc:message code="workflow.start.DateField"/></label>
                    <select id="dateProfileField" name="dateProfileField" class="form-control js-select">
                        <option value="">--</option>
                        <c:forEach var="profileField" items="${profileFields}">
                            <c:if test="${profileField.dataType == 'DATE' or profileField.dataType == 'DATETIME'}">
                                <option value="${profileField.column}">${profileField.shortname}</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <div id="startIconDateFieldOperator">
                    <label class="form-label"><mvc:message code="workflow.start.Rule"/></label>
                    <div class="input-group">
                        <span class="input-group-text">
                            ${equalOperator.eqlSymbol}
                            <input id="dateFieldOperator" type="hidden" name="dateFieldOperator" value="${equalOperator.operatorCode}"/>
                        </span>
                        <input id="dateFieldValue" class="form-control" name="dateFieldValue" type="text"/>
                    </div>
                </div>

                <div id="startIconDateFormat">
                    <label for="dateFormat" class="form-label"><mvc:message code="import.dateFormat"/></label>
                    <select id="dateFormat" name="dateFormat" class="form-control js-select">
                        <option value="mmdd"><mvc:message code="default.date.format.MMDD"/></option>
                        <option value="yyyymmdd"><mvc:message code="default.date.format.YYYYMMDD"/></option>
                        <option value="dd"><mvc:message code="default.date.format.DD"/></option>
                    </select>
                </div>
            </div>

            <div id="executionDatePanel">
                <label for="executionDate" id="executionDateLabel" class="form-label"><mvc:message code="workflow.start.execution.schedule"/></label>
                <label for="executionDate" id="firstExecutionDateLabel" class="form-label"><mvc:message code="workflow.start.execution.scheduleFirst"/></label>
                <div class="date-time-container">
                    <div class="date-picker-container">
                        <input type="text" id="executionDate" class="form-control js-datepicker"/>
                    </div>
                </div>
            </div>
        </div>

        <div id="startIconTime" class="time-picker-container">
            <input type="text" id="startTime" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'"/>
        </div>

        <div id="startRemindAdmin" data-field="toggle-vis">
            <div class="form-check form-switch">
                <input type="checkbox" id="sendReminder" name="sendReminder" value="true"
                       class="form-check-input" role="switch" data-action="start-editor-reminder-changed"/>
                <label class="form-label form-check-label" for="sendReminder"><mvc:message code="calendar.Notify"/></label>
            </div>

            <div id="reminderDetails" class="form-column mt-2">
                <select name="userType" id="user-type" class="form-control js-select" data-field-vis="">
                    <option value="${RECIPIENT_EMM}"
                            data-field-vis-hide="#customRecipients"
                            data-field-vis-show="#emmUsers"><mvc:message code="workflow.start.emmUsers"/></option>
                    <option value="${RECIPIENT_CUSTOM}"
                            data-field-vis-hide="#emmUsers"
                            data-field-vis-show="#customRecipients"><mvc:message code="workflow.start.customRecipients"/></option>
                </select>

                <div id="emmUsers">
                    <select id="remindAdminId" name="remindAdminId"  class="form-control js-select">
                        <c:forEach var="admin" items="${admins}">
                            <option value="${admin.id}">${admin.username}</option>
                        </c:forEach>
                    </select>
                </div>

                <div id="customRecipients">
                    <input type="hidden" name="adminTimezone" value="${adminTimezone}"/>
                    <textarea name="recipients" cols="32" rows="3" class="form-control"
                              placeholder="<mvc:message code='enterEmailAddresses'/>" style="resize: none;"></textarea>
                </div>

                <div>
                    <label class="form-label" for="reminderComment"><mvc:message code="calendar.Comment"/></label>
                    <textarea id="reminderComment" name="comment" cols="32" rows="5" class="form-control non-resizable"></textarea>
                </div>

                <div>
                    <label class="form-label" for="remind-specific-date"><mvc:message code="workflow.start.scheduleReminder"/></label>
                    <select name="remindSpecificDate" id="remind-specific-date" class="form-control js-select" data-action="start-editor-schedule-reminder-date-changed">
                        <option value="false" id="remindCalendarDateTitle">?<%-- populated with js--%></option>
                        <option value="true"><mvc:message code="workflow.start.individualDate"/></option>
                    </select>
                </div>

                <div id="dateTimePicker">
                    <label class="form-label" for="remindDate"><mvc:message code="Date"/></label>
                    <div class="date-time-container">
                        <div class="date-picker-container">
                            <input type="text" id="remindDate" class="form-control js-datepicker"/>
                        </div>
                        <div class="time-picker-container">
                            <input type="text" id="remindTime" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>

    <script id="config:start-editor-initializer" type="application/json">
        {
            "isBigData": "${isBigData}",
            "form" : "startForm",
            "container": "#reactionStartMailing",
            "sessionId": "${pageContext.session.id}",
            "noMailingOption": "false",
            "selectedName": "mailingId",
            "adminTimezone": "${adminTimezone}",
            "equelsOperatorCode": "${equalOperator.operatorCode}",
            "profileFields" :${emm:toJson(profiles)}
        }
    </script>
</div>
