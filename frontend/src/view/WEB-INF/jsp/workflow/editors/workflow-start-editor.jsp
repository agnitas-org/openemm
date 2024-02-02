<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowReactionType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartEventType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStart.WorkflowStartType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowStop" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDecision" %>
<%@ page import="org.agnitas.target.ConditionalOperator" %>
<%@ page import="org.agnitas.target.ChainOperator" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="TYPE_OPEN" value="<%= WorkflowStartType.OPEN %>"/>
<c:set var="TYPE_DATE" value="<%= WorkflowStartType.DATE %>"/>
<c:set var="TYPE_EVENT" value="<%= WorkflowStartType.EVENT %>"/>

<c:set var="END_TYPE_AUTOMATIC" value="<%= WorkflowStop.WorkflowEndType.AUTOMATIC %>"/>
<c:set var="END_TYPE_DATE" value="<%= WorkflowStop.WorkflowEndType.DATE %>"/>

<c:set var="EVENT_REACTION" value="<%= WorkflowStartEventType.EVENT_REACTION %>"/>
<c:set var="EVENT_DATE" value="<%= WorkflowStartEventType.EVENT_DATE %>"/>

<c:set var="REACTION_OPENED" value="<%= WorkflowReactionType.OPENED %>"/>
<c:set var="REACTION_NOT_OPENED" value="<%= WorkflowReactionType.NOT_OPENED %>"/>
<c:set var="REACTION_CLICKED" value="<%= WorkflowReactionType.CLICKED %>"/>
<c:set var="REACTION_NOT_CLICKED" value="<%= WorkflowReactionType.NOT_CLICKED %>"/>
<c:set var="REACTION_BOUGHT" value="<%= WorkflowReactionType.BOUGHT %>"/>
<c:set var="REACTION_NOT_BOUGHT" value="<%= WorkflowReactionType.NOT_BOUGHT %>"/>
<c:set var="REACTION_DOWNLOAD" value="<%= WorkflowReactionType.DOWNLOAD %>"/>
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
<%@include file="../fragments/workflow-view-bigdata-settings.jspf"%>

<%-- Editor main container should have id="<icon type>-editor" - in that case the campaign manager will be able to find it --%>
<div id="start-editor" title="Define Start of a Campaign" data-initializer="start-editor-initializer">

    <mvc:form action="" id="startForm" name="startForm">

        <div class="status_error editor-error-messages well" style="display: none;"></div>

        <input name="id" type="hidden">

        <div id="startStopType" class="form-group">
        </div>

        <div id="startDatePanel">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="startDate" class="control-label"><mvc:message code="settings.fieldType.DATE"/></label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <input type="text" id="startDate" class="form-control datepicker-input js-datepicker"
                                   data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}'"/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                <i class="icon icon-calendar-o"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="startEventPanel" style="display: none;">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="startEvent" class="control-label">
                        <mvc:message code="workflow.start.Event"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <select data-action="start-editor-event-changed" id="startEvent" name="event" class="form-control">
                        <option value="${EVENT_REACTION}" id="eventReaction"></option>
                        <option value="${EVENT_DATE}" id="eventDate"></option>
                    </select>
                </div>
            </div>

            <div id="reactionStartPanel">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="startReaction" class="control-label">
                            <mvc:message code="workflow.Reaction"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <select id="startReaction" name="reaction" class="form-control"
                                data-action="start-editor-reaction-changed">
                            <option value="${REACTION_OPENED}"><mvc:message code="statistic.opened"/></option>
                            <%--<option value="${REACTION_NOT_OPENED}"><mvc:message code="workflow.reaction.NotOpened"/></option>--%>
                            <option value="${REACTION_CLICKED}"><mvc:message code="default.clicked"/></option>
                            <option value="${REACTION_CLICKED_LINK}"><mvc:message code="workflow.reaction.ClickedOnLink"/></option>
                            <%--<option value="${REACTION_NOT_CLICKED}"><mvc:message code="workflow.reaction.NotClicked"/></option>--%>
                            <%--<c:if test="${hasDeepTrackingTables}">--%>
                            <%--<option value="${REACTION_BOUGHT}"><mvc:message code="workflow.reaction.Bought"/></option>--%>
                            <%--<option value="${REACTION_NOT_BOUGHT}"><mvc:message code="workflow.reaction.NotBought"/></option>--%>
                            <%--</c:if>--%>
                            <%--<option value="${REACTION_DOWNLOAD}"><mvc:message code="button.Download"/></option>--%>

                            <c:if test="${isBigData}">
                                <option value="${REACTION_CHANGE_OF_PROFILE}"><mvc:message code="workflow.reaction.ChangeOfProfile"/></option>
                            </c:if>
                            <option value="${REACTION_WAITING_FOR_CONFIRM}"><mvc:message code="workflow.reaction.WaitingForConfirm"/></option>
                            <option value="${REACTION_OPT_IN}"><mvc:message code="workflow.reaction.OptIn"/></option>
                            <option value="${REACTION_OPT_OUT}"><mvc:message code="workflow.reaction.OptOut"/></option>
                        </select>
                    </div>
                </div>

                <div id="reactionStartMailing">
                    <jsp:include page="workflow-mailing-selector.jsp">
                        <jsp:param name="mailingSelectorSorter" value="start-editor-mailing-sort"/>
                        <jsp:param name="mailingSelectorEventHandler" value="start-editor-mailing-select"/>
                        <jsp:param name="selectName" value="mailingId"/>
                    </jsp:include>
                </div>

                <div id="reactionStartMailingLink">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="startReactionMailingLink"><mvc:message code="workflow.decision.ChooseLink"/></label>
                        </div>
                        <div class="col-sm-8">
                            <select id="startReactionMailingLink" name="linkId" class="form-control">
                            </select>
                        </div>
                    </div>
                </div>


                <div id="reactionStartProfile" style="display: none;">
                    <div id="reactionProfileField">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label for="startProfileField" class="control-label">
                                    <mvc:message code="workflow.start.ProfileField"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <select id="startProfileField" name="profileField" class="form-control js-select"
                                       data-action="start-editor-profile-field-changed">
                                    <option value="">--</option>
                                    <c:forEach var="profileField" items="${profileFieldsHistorized}">
                                        <option value="${profileField.column}">${profileField.shortname}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-8 col-sm-push-4">
                            <label class="radio-inline">
                                <input type="radio" name="useRules" id="profileFieldRulesAny"
                                       data-action="start-editor-rule-changed" checked="checked" value="false">
                                <mvc:message code="workflow.start.rules.any"/>
                            </label>
                            <label class="radio-inline">
                                <input type="radio" name="useRules" id="profileFieldRulesExact"
                                       data-action="start-editor-rule-changed" class="start-type-event-radio"
                                       value="true">
                                <mvc:message code="workflow.start.rules.exact"/>
                            </label>
                        </div>
                    </div>

                    <div id="profileFieldRules">
                        <div class="form-group">
                            <div class="col-sm-8 col-sm-push-4">
                                <div class="row">
                                    <table class="table table-bordered table-form">
                                        <tbody id="profileFieldAddedRules"></tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div id="profileFieldRuleAdd">
                            <div class="form-group">
                                <div class="col-sm-8 col-sm-push-4">
                                    <div class="row">
                                        <table class="table table-bordered table-form">
                                            <tbody>
                                            <tr>
                                                <td>
                                                    <select id="newRule_chainOperator" class="">
                                                        <option value="${CHAIN_OPERATOR_AND}"><mvc:message code="default.and"/></option>
                                                        <option value="${CHAIN_OPERATOR_OR}"><mvc:message code="default.or"/></option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <select id="newRule_parenthesisOpened" class=" parentheses-opened">
                                                        <option value="0">&nbsp;</option>
                                                        <option value="1">(</option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <select id="newRule_primaryOperator" class="primary-operator" data-action="start-rule-operator-change">
                                                        <c:forEach var="operator" items="${operators}">
                                                            <c:set var="types" value="${operatorsTypeSupportMap[operator]}"/>
                                                            <option data-types="${types}" value="${operator.operatorCode}">${operator.eqlSymbol}</option>
                                                        </c:forEach>
                                                    </select>
                                                </td>
                                                <td>
                                                    <input id="newRule_primaryValue" type="text"
                                                           class="form-control primary-value"/>
                                                </td>
                                                <td>
                                                    <select id="newRule_parenthesisClosed" class="parenthesis-closed">
                                                        <option value="0">&nbsp;</option>
                                                        <option value="1">)</option>
                                                    </select>
                                                </td>
                                                <td>
                                                    <a class="btn btn-regular btn-secondary advanced_search_add add-rule disable-for-active"
                                                       data-action="start-editor-add-rule" href="#"
                                                       data-tooltip="<mvc:message code="button.Add"/>">
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
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <mvc:message code="workflow.start.execution"/>
                            <button class="icon icon-help" data-help="help_${helplanguage}/workflow/start/Execution.xml" tabindex="-1" type="button"></button>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <label class="radio-inline">
                            <input type="radio" name="executeOnce" id="startExecuteOnce" data-action="start-editor-execution-changed"
                                   checked="checked" value="true">
                            <mvc:message code="workflow.start.execution.once"/>
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="executeOnce" data-action="start-editor-execution-changed" id="startExecutePermanent"
                                   class="start-type-event-radio"
                                   value="false">
                            <mvc:message code="workflow.start.execution.permanent"/>
                        </label>
                    </div>
                </div>
            </div>

            <div id="dateStartPanel" style="display: none">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="dateProfileField" class="control-label">
                            <mvc:message code="workflow.start.DateField"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <select id="dateProfileField" name="dateProfileField" class="form-control js-select">
                            <option value="">--</option>
                            <c:forEach var="profileField" items="${profileFields}">
                                <c:if test="${profileField.dataType == 'DATE' or profileField.dataType == 'DATETIME'}">
                                    <option value="${profileField.column}">${profileField.shortname}</option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div id="startIconDateFieldOperator">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label">
                                <mvc:message code="workflow.start.Rule"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-addon">
                                    <span class="addon">${equalOperator.eqlSymbol}</span>
                                    <input id="dateFieldOperator" type="hidden" name="dateFieldOperator"
                                           value="${equalOperator.operatorCode}"/>
                                </div>
                                <div class="input-group-controls">
                                    <input id="dateFieldValue" class="form-control" name="dateFieldValue" type="text"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="startIconDateFormat">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label for="dateFormat" class="control-label">
                                <mvc:message code="import.dateFormat"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <select id="dateFormat" name="dateFormat" class="form-control js-select">
                                <option value="mmdd"><mvc:message code="default.date.format.MMDD"/></option>
                                <option value="yyyymmdd"><mvc:message code="default.date.format.YYYYMMDD"/></option>
                                <option value="dd"><mvc:message code="default.date.format.DD"/></option>
                            </select>
                        </div>
                    </div>
                </div>

            </div>

            <div id="executionDatePanel">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label for="executionDate" id="executionDateLabel" class="control-label">
                            <mvc:message code="workflow.start.execution.schedule"/>
                        </label>
                        <label for="executionDate" id="firstExecutionDateLabel" class="control-label">
                            <mvc:message code="workflow.start.execution.scheduleFirst"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <div class="input-group">
                            <div class="input-group-controls">
                                <input type="text" id="executionDate"
                                       class="form-control datepicker-input js-datepicker"
                                       data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}'"/>
                            </div>
                            <div class="input-group-btn">
                                <button type="button" class="btn btn-regular btn-toggle js-open-datepicker"
                                        tabindex="-1">
                                    <i class="icon icon-calendar-o"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="startIconTime">
            <div class="form-group">
                <div class="col-sm-4">
                    <label for="startTime" class="control-label">
                        <mvc:message code="Time"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <input type="text" id="startTime" class="form-control js-timepicker"
                                   data-timepicker-options="mask: 'h:s'"/>
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

        <div id="startRemindAdmin" data-field="toggle-vis">
            <div class="row">
                <div class="col-sm-4">
                    <label class="checkbox-inline">
                        <input type="checkbox" id="sendReminder" name="sendReminder" value="true"
                               data-action="start-editor-reminder-changed"/>

                        <mvc:message code="calendar.Notify"/>
                    </label>
                </div>
            </div>

            <div id="reminderDetails">
                <div class="form-group">
                    <div class="col-sm-8 col-sm-push-4">
                        <label class="radio-inline">
                            <input type="radio" value="${RECIPIENT_EMM}"
                                   name="userType"
                                   data-field-vis=""
                                   data-field-vis-hide="#customRecipients"
                                   data-field-vis-show="#emmUsers"
                                   checked="checked"
                            />
                            <mvc:message code="workflow.start.emmUsers"/>
                        </label>
                        <label class="radio-inline">
                            <input type="radio" value="${RECIPIENT_CUSTOM}"
                                   name="userType"
                                   data-field-vis=""
                                   data-field-vis-hide="#emmUsers"
                                   data-field-vis-show="#customRecipients"
                            />
                            <mvc:message code="workflow.start.customRecipients"/>
                        </label>
                    </div>
                </div>

                <div class="form-group" id="emmUsers">
                    <div class="col-sm-8 col-sm-push-4">
                        <select id="remindAdminId" name="remindAdminId" class="form-control js-select">
                            <c:forEach var="admin" items="${admins}">
                                <option value="${admin.id}">${admin.username}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group" id="customRecipients">
                    <div class="col-sm-8 col-sm-push-4">
                        <input type="hidden" name="adminTimezone" value="${param.adminTimezone}"/>
                        <c:set var="enterEmailAddresses"><mvc:message code="enterEmailAddresses"/></c:set>
                        <textarea name="recipients" cols="32" rows="3" class="form-control"
                                  placeholder="${enterEmailAddresses}" style="resize: none;"></textarea>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="reminderComment">
                            <mvc:message code="calendar.Comment"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <textarea id="reminderComment" name="comment" cols="32" rows="5" class="form-control non-resizable"></textarea>
                    </div>
                </div>

                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label">
                            <mvc:message code="workflow.start.scheduleReminder"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <label class="radio-inline">
                            <input type="radio" name="remindSpecificDate" value="false" data-action="start-editor-schedule-reminder-date-changed">
                            <span id="remindCalendarDateTitle">?</span>
                        </label>
                        <label class="radio-inline">
                            <input type="radio" name="remindSpecificDate" value="true" data-action="start-editor-schedule-reminder-date-changed">
                            <mvc:message code="workflow.start.individualDate"/>
                        </label>
                    </div>
                </div>

                <div id="dateTimePicker">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="remindDate">
                                <mvc:message code="settings.fieldType.DATE"/>
                            </label>
                        </div>

                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <input type="text" id="remindDate"
                                           class="form-control datepicker-input js-datepicker"
                                           data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}'"/>
                                </div>
                                <div class="input-group-btn">
                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker"
                                            tabindex="-1">
                                        <i class="icon icon-calendar-o"></i>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="remindTime">
                                <mvc:message code="Time"/>
                            </label>
                        </div>

                        <div class="col-sm-8">
                            <div class="input-group">
                                <div class="input-group-controls">
                                    <input type="text" id="remindTime"
                                           class="form-control timepicker-input js-timepicker"
                                           data-timepicker-options="mask: 'h:s'"/>
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
            </div>
        </div>

        <hr>

        <div class="col-xs-12">
            <div class="form-group">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <mvc:message code="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active"
                      data-action="start-editor-validate">
                        <mvc:message code="button.Apply"/>
                    </a>
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
            "adminTimezone": "${param.adminTimezone}",
            "equelsOperatorCode": "${equalOperator.operatorCode}",
            "localeDatePattern": "${localeDatePattern}",

            "profileFields" :${emm:toJson(profiles)}
        }
    </script>
</div>

<script id="start-types" type="text/x-mustache-template">
    <div class="col-sm-8 col-sm-push-4">
        <label class="radio-inline">
            <input type="radio" name="startType" id="typeDate" data-action="start-editor-type-changed"
                   checked="checked" value="${TYPE_DATE}">
            <span><mvc:message code="workflow.start.StartDate"/></span>
        </label>
        {{ if(showStartEventTab) { }}
            <label class="radio-inline">
                <input type="radio" name="startType" id="typeEvent" data-action="start-editor-type-changed"
                       class="start-type-event-radio" value="${TYPE_EVENT}">
                <span><mvc:message code="workflow.start.StartEvent"/></span>
            </label>
        {{ } }}
    </div>
</script>

<script id="stop-types" type="text/x-mustache-template">
    <div class="col-sm-8 col-sm-push-4">
        <label class="radio-inline">
            <input type="radio" name="endType" id="typeOpen" data-action="start-editor-type-changed"
                   value="${END_TYPE_AUTOMATIC}">
            <span id="endTypeActiomaticLabel"><mvc:message code="workflow.stop.AutomaticEnd"/></span>
        </label>
        <label class="radio-inline" id="end-date-radio">
            <input type="radio" name="endType" id="typeDate"  data-action="start-editor-type-changed" checked="checked"
                   value="${END_TYPE_DATE}" class="start-type-event-radio">
            <span><mvc:message code="workflow.stop.EndDate"/></span>
        </label>
    </div>
</script>
