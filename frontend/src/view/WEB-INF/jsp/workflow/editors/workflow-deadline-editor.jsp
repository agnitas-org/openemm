<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
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

    <mvc:form action="" id="deadlineForm" name="deadlineForm">
        <input name="id" type="hidden">
        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <label class="radio-inline">
                    <input type="radio" name="deadlineType" id="typeDelay" data-action="deadline-editor-update" checked="checked" value="${TYPE_DELAY}">
                    <mvc:message code="Delay"/>
                </label>
                <label class="radio-inline">
                    <input type="radio" name="deadlineType" id="typeFixedDeadline" data-action="deadline-editor-update" class="decision-type-radio" value="${TYPE_FIXED_DEADLINE}">
                    <mvc:message code="workflow.deadline.FixedDeadline"/>
                </label>
            </div>
        </div>

        <div id="fixedDeadlinePanel" >
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="settings.fieldType.DATE"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <input type="text" id="deadlineDate" class="form-control datepicker-input js-datepicker" data-datepicker-options="format: '${fn:toLowerCase(localeDatePattern)}', min: true"/>
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

        <div id="delayDeadlinePanel">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <mvc:message code="workflow.deadline.TimeUnit"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <select name="timeUnit" class="form-control" data-action="deadline-editor-update">
                        <option value="${TIME_UNIT_MINUTE}"><mvc:message code="workflow.deadline.Minutes"/></option>
                        <option value="${TIME_UNIT_HOUR}"><mvc:message code="Hours"/></option>
                        <option value="${TIME_UNIT_DAY}"><mvc:message code="Days"/></option>
                        <option value="${TIME_UNIT_WEEK}"><mvc:message code="default.weeks"/></option>
                        <option value="${TIME_UNIT_MONTH}"><mvc:message code="default.months"/></option>
                    </select>
                </div>
            </div>

            <div id="deadlineTimeUnitMinute">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="minutesPeriod">
                            <mvc:message code="workflow.deadline.Minutes"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                    <select class="form-control" data-action="deadline-editor-update" id="minutesPeriod">
                            <c:forEach var="minute" begin="5" end="55" step="5">
                                <option value="${minute}">
                                    ${minute} <mvc:message code="workflow.deadline.Minutes"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </div>

            <div id="deadlineTimeUnitHour">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="hoursPeriod">
                            <mvc:message code="Hours"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
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
                </div>
            </div>

            <div id="deadlineTimeUnitDay">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="daysPeriod">
                            <mvc:message code="Days"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
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
                </div>
            </div>

                <div id="deadlineTimeUnitWeek">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="weeksPeriod">
                                <mvc:message code="default.weeks"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
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
                    </div>
                </div>

                <div id="deadlineTimeUnitMonth">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <label class="control-label" for="monthsPeriod">
                                <mvc:message code="default.months"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
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
                </div>
        </div>

        <div id="deadlineTimeContainer">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <input type="checkbox" name="useTime" value="true"/>
                        <mvc:message code="Time"/>
                        <button id="deadlineTimeHelp" type="button" class="icon icon-help" tabindex="-1" data-help="help_${helplanguage}/workflow/DeadlineTime.xml"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <input type="text" data-action="deadline-editor-time-change" id="time" class="form-control js-timepicker" data-timepicker-options="mask: 'h:s'"/>
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

        <div class="form-group">
            <div class="col-xs-12">
                <div class="btn-group">
                    <a href="#" class="btn btn-regular" data-action="editor-cancel">
                        <mvc:message code="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="deadline-editor-save">
                        <mvc:message code="button.Apply"/>
                    </a>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
