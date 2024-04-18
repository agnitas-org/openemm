<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ page import="com.agnitas.emm.core.birtreport.dto.PeriodType" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="dateFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="datePickerFormatPattern" type="java.lang.String"--%>
<%--@elvariable id="reportDateFormatPattern" type="java.lang.String"--%>

<c:set var="DATE_RANGE_WEEK" value="<%=PeriodType.DATE_RANGE_WEEK.getKey()%>" scope="request"/>
<c:set var="DATE_RANGE_30DAYS" value="<%=PeriodType.DATE_RANGE_30DAYS.getKey()%>" scope="request"/>
<c:set var="DATE_RANGE_LAST_MONTH" value="<%=PeriodType.DATE_RANGE_LAST_MONTH.getKey()%>" scope="request"/>
<c:set var="DATE_RANGE_CUSTOM" value="<%=PeriodType.DATE_RANGE_CUSTOM.getKey()%>" scope="request"/>
<c:set var="DATE_RANGE_DAY" value="<%=PeriodType.DATE_RANGE_DAY.getKey()%>" scope="request"/>

<c:set var="periodPath" value="${param.periodPath}"/>
<c:set var="startDatePath" value="${param.startDatePath}"/>
<c:set var="stopDatePath" value="${param.stopDatePath}"/>
<c:set var="startDateValue" value="${param.startDateValue}"/>
<c:set var="stopDateValue" value="${param.stopDateValue}"/>

<div id="mailing-statistic-period" class="form-group">
    <div class="col-sm-4">
        <label class="control-label"><mvc:message code="report.recipient.period"/></label>
    </div>
    <div class="col-sm-8">
        <label class="radio-inline">
            <mvc:radiobutton path="${periodPath}"
                          value="${DATE_RANGE_DAY}"
                          data-field-vis=""
                          data-field-vis-hide="#custom-date-range-from, #custom-date-range-to"/>
            <mvc:message code="report.recipient.previous.day"/>
        </label>
        <label class="radio-inline">
            <mvc:radiobutton path="${periodPath}"
                          value="${DATE_RANGE_WEEK}"
                          data-field-vis=""
                          data-field-vis-hide="#custom-date-range-from, #custom-date-range-to"/>
            <mvc:message code="report.recipient.last.week"/>
        </label>
        <label class="radio-inline">
            <mvc:radiobutton path="${periodPath}"
                          value="${DATE_RANGE_30DAYS}"
                          data-field-vis=""
                          data-field-vis-hide="#custom-date-range-from, #custom-date-range-to"/>
            <mvc:message code="report.last.30days"/>
        </label>
        <label class="radio-inline">
            <mvc:radiobutton path="${periodPath}"
                          value="${DATE_RANGE_LAST_MONTH}"
                          data-field-vis=""
                          data-field-vis-hide="#custom-date-range-from, #custom-date-range-to"/>
            <mvc:message code="report.last.month"/>
        </label>
        <label class="radio-inline">
            <mvc:radiobutton path="${periodPath}"
                          value="${DATE_RANGE_CUSTOM}"
                          data-field-vis=""
                          data-field-vis-show="#custom-date-range-from, #custom-date-range-to"/>
            <mvc:message code="statistics.dateRange"/>
        </label>
    </div>
</div>

<div id="custom-date-range-from" class="form-group">
    <div class="col-sm-4">
        <label class="control-label"><mvc:message code="report.recipient.from"/></label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <mvc:text path="${startDatePath}" id="customStartDate" data-value="${startDateValue}"
                           cssClass="form-control datepicker-input js-datepicker"
                           data-datepicker-options="format: '${fn:toLowerCase(datePickerFormatPattern)}', formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'"/>
            </div>
            <div class="input-group-btn">
                <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" >
                    <i class="icon icon-calendar-o"></i>
                </button>
            </div>
        </div>

    </div>
</div>

<div id="custom-date-range-to" class="form-group">
    <div class="col-sm-4">
        <label class="control-label"><mvc:message code="default.to"/></label>
    </div>
    <div class="col-sm-8">
        <div class="input-group">
            <div class="input-group-controls">
                <mvc:text path="${stopDatePath}" id="customStopDate" data-value="${stopDateValue}"
                           cssClass="form-control datepicker-input js-datepicker"
                           data-datepicker-options="format: '${fn:toLowerCase(datePickerFormatPattern)}', formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'"/>
            </div>
            <div class="input-group-btn">
                <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" >
                    <i class="icon icon-calendar-o"></i>
                </button>
            </div>
        </div>

    </div>
</div>
