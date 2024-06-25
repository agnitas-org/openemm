<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineTimeUnit" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>

<c:set var="TYPE_DELAY" value="<%= WorkflowDeadlineType.TYPE_DELAY %>"/>
<c:set var="TYPE_FIXED_DEADLINE" value="<%= WorkflowDeadlineType.TYPE_FIXED_DEADLINE %>"/>

<c:set var="TIME_UNIT_MINUTE" value="<%= WorkflowDeadlineTimeUnit.TIME_UNIT_MINUTE %>"/>
<c:set var="TIME_UNIT_HOUR" value="<%= WorkflowDeadlineTimeUnit.TIME_UNIT_HOUR %>"/>
<c:set var="TIME_UNIT_DAY" value="<%= WorkflowDeadlineTimeUnit.TIME_UNIT_DAY %>"/>
<c:set var="TIME_UNIT_WEEK" value="<%= WorkflowDeadlineTimeUnit.TIME_UNIT_WEEK %>"/>
<c:set var="TIME_UNIT_MONTH" value="<%= WorkflowDeadlineTimeUnit.TIME_UNIT_MONTH %>"/>

<div id="deadline-editor" data-initializer="deadline-editor-initializer">

    <div class="status_error editor-error-messages well" style="margin-bottom: 10px; display: none;"></div>

    <mvc:form action="" id="deadlineForm" name="deadlineForm" cssClass="grid gap-3" cssStyle="--bs-columns: 1">
        <input name="id" type="hidden">
<%--        todo fix js--%>
        <div>
            <label for="deadline-type" class="form-label"><mvc:message code="default.Type"/></label>
            <select id="deadline-type" name="deadlineType" class="form-control">
                <option value="${TYPE_DELAY}" id="typeDelay" data-action="deadline-editor-update"><mvc:message code="Delay"/></option>
                <option value="${TYPE_FIXED_DEADLINE}" id="typeFixedDeadline" data-action="deadline-editor-update"><mvc:message code="workflow.deadline.FixedDeadline"/></option>
            </select>             
        </div>

        <div id="fixedDeadlinePanel">
<%--                todo--%>
            <label class="form-label"><mvc:message code="Date"/></label>
            <div class="date-picker-container">
                <input type="text" id="deadlineDate" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}', min: true"/>
            </div>
        </div>

        <div id="delayDeadlinePanel" class="grid gap-3" style="--bs-columns: 1">
            <div>
                <label class="form-label"><mvc:message code="workflow.deadline.TimeUnit"/></label>
                <select name="timeUnit" class="form-control" data-action="deadline-editor-update">
                    <option value="${TIME_UNIT_MINUTE}"><mvc:message code="workflow.deadline.Minutes"/></option>
                    <option value="${TIME_UNIT_HOUR}"><mvc:message code="Hours"/></option>
                    <option value="${TIME_UNIT_DAY}"><mvc:message code="Days"/></option>
                    <option value="${TIME_UNIT_WEEK}"><mvc:message code="default.weeks"/></option>
                    <option value="${TIME_UNIT_MONTH}"><mvc:message code="default.months"/></option>
                </select>
            </div>

            <div id="deadlineTimeUnitMinute">
                <label class="form-label" for="minutesPeriod"><mvc:message code="workflow.deadline.Minutes"/></label>
                <select class="form-control" data-action="deadline-editor-update" id="minutesPeriod">
                    <c:forEach var="minute" begin="5" end="55" step="5">
                        <option value="${minute}">
                            ${minute} <mvc:message code="workflow.deadline.Minutes"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div id="deadlineTimeUnitHour">
                <label class="form-label" for="hoursPeriod"><mvc:message code="Hours"/></label>
                <select class="form-control" data-action="deadline-editor-update" id="hoursPeriod">
                    <option value="1">
                        1 <mvc:message code="Hour"/>
                    </option>
                    <c:forEach var="hour" begin="2" end="24" step="1">
                        <option value="${hour}">
                            ${hour} <mvc:message code="Hours"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div id="deadlineTimeUnitDay">
                <label class="form-label" for="daysPeriod"><mvc:message code="Days"/></label>
                <select class="form-control" data-action="deadline-editor-update" id="daysPeriod">
                    <option value="1">
                        1 <mvc:message code="Day"/>
                    </option>
                    <c:forEach var="day" begin="2" end="30" step="1">
                        <option value="${day}">
                            ${day} <mvc:message code="Days"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div id="deadlineTimeUnitWeek">
                <label class="form-label" for="weeksPeriod"><mvc:message code="default.weeks"/></label>
                <select class="form-control" data-action="deadline-editor-update" id="weeksPeriod">
                    <option value="1">
                        1 <mvc:message code="Week"/>
                    </option>
                    <c:forEach var="week" begin="2" end="6" step="1">
                        <option value="${week}">
                                ${week} <mvc:message code="default.weeks"/>
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div id="deadlineTimeUnitMonth">
                <label class="form-label" for="monthsPeriod"><mvc:message code="default.months"/></label>
                <select class="form-control" data-action="deadline-editor-update" id="monthsPeriod">
                    <option value="1">
                        1 <mvc:message code="Month"/> (30 <mvc:message code="days"/> )
                    </option>
                    <c:forEach var="month" begin="2" end="12" step="1">
                        <option value="${month}">
                                ${month} <mvc:message code="default.months"/> (${month * 30} <mvc:message code="days"/>)
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div id="deadlineTimeContainer">
            <label class="form-label">
                <div class="form-check form-switch mt-3">
                    <input id="use-time" type="checkbox" name="useTime" value="true" class="form-check-input" role="switch"/>
                    <label class="form-label form-check-label" for="use-time"><mvc:message code="Time"/></label>
                    <a href="#" id="deadlineTimeHelp" class="icon icon-question-circle" data-help="help_${helplanguage}/workflow/DeadlineTime.xml"></a>
                </div>
            </label>
<%--            todo--%>
            <div id="importTime" class="time-picker-container">
                <input type="text" data-action="deadline-editor-time-change" id="time" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'"/>
            </div>
        </div>

<%--                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="deadline-editor-save">--%>
<%--                        <mvc:message code="button.Apply"/>--%>
<%--                    </a>--%>
    </mvc:form>
</div>
