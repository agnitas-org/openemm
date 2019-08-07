<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineType" %>
<%@ page import="com.agnitas.emm.core.workflow.beans.WorkflowDeadline.WorkflowDeadlineTimeUnit" %>

<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic"   uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

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

    <form action="" id="deadlineForm" name="deadlineForm">
        <input name="id" type="hidden">
        <div class="form-group">
            <div class="col-sm-8 col-sm-push-4">
                <label class="radio-inline">
                    <input type="radio" name="deadlineType" id="typeDelay" data-action="deadline-editor-update" checked="checked" value="${TYPE_DELAY}">
                    <bean:message key="Delay"/>
                </label>
                <label class="radio-inline">
                    <input type="radio" name="deadlineType" id="typeFixedDeadline" data-action="deadline-editor-update" class="decision-type-radio" value="${TYPE_FIXED_DEADLINE}">
                    <bean:message key="workflow.deadline.FixedDeadline"/>
                </label>
            </div>
        </div>

        <div id="fixedDeadlinePanel" >
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <bean:message key="settings.fieldType.DATE"/>
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
                        <bean:message key="workflow.deadline.TimeUnit"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <select name="timeUnit" class="form-control" data-action="deadline-editor-update">
                        <option value="${TIME_UNIT_MINUTE}"><bean:message key="workflow.deadline.Minutes"/></option>
                        <option value="${TIME_UNIT_HOUR}"><bean:message key="Hours"/></option>
                        <option value="${TIME_UNIT_DAY}"><bean:message key="Days"/></option>
                        <%-- TODO: remove beta state when GWUA-3941 will be tested --%>
                        <option value="${TIME_UNIT_WEEK}"><bean:message key="default.weeks"/></option>
                        <option value="${TIME_UNIT_MONTH}"><bean:message key="default.months"/></option>
                    </select>
                </div>
            </div>

            <div id="deadlineTimeUnitMinute">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="minutesPeriod">
                            <bean:message key="workflow.deadline.Minutes"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                    <select class="form-control" data-action="deadline-editor-update" id="minutesPeriod">
                            <c:forEach var="minute" begin="5" end="55" step="5">
                                <option value="${minute}">
                                    ${minute} <bean:message key="workflow.deadline.Minutes"/>
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
                            <bean:message key="Hours"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <select class="form-control" data-action="deadline-editor-update" id="hoursPeriod">
                            <option value="1">
                                1 <bean:message key="Hour"/>
                            </option>
                            <c:forEach var="hour" begin="2" end="24" step="1">
                                <option value="${hour}">
                                    ${hour} <bean:message key="Hours"/>
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
                            <bean:message key="Days"/>
                        </label>
                    </div>
                    <div class="col-sm-8">
                        <select class="form-control" data-action="deadline-editor-update" id="daysPeriod">
                            <option value="1">
                                1 <bean:message key="Day"/>
                            </option>
                            <c:forEach var="day" begin="2" end="30" step="1">
                                <option value="${day}">
                                    ${day} <bean:message key="Days"/>
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
                                <bean:message key="default.weeks"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <select class="form-control" data-action="deadline-editor-update" id="weeksPeriod">
                                <option value="1">
                                    1 <bean:message key="Week"/>
                                </option>
                                <c:forEach var="week" begin="2" end="6" step="1">
                                    <option value="${week}">
                                            ${week} <bean:message key="default.weeks"/>
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
                                <bean:message key="default.months"/>
                            </label>
                        </div>
                        <div class="col-sm-8">
                            <select class="form-control" data-action="deadline-editor-update" id="monthsPeriod">
                                <option value="1">
                                    1 <bean:message key="Month"/>
                                </option>
                                <c:forEach var="month" begin="2" end="12" step="1">
                                    <option value="${month}">
                                            ${month} <bean:message key="default.months"/>
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
                        <bean:message key="Time"/>
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
                        <bean:message key="button.Cancel"/>
                    </a>
                    <a href="#" class="btn btn-regular btn-primary hide-for-active" data-action="deadline-editor-save">
                        <bean:message key="button.Save"/>
                    </a>
                </div>
            </div>
        </div>
    </form>
</div>
