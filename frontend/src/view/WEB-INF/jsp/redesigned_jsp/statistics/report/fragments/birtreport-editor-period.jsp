<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
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

<label class="form-label"><mvc:message code="report.recipient.period"/></label>

<div class="row g-1">
    <div class="col-12">
        <mvc:select path="${periodPath}" cssClass="form-control js-select" data-field-vis="">
            <mvc:option value="${DATE_RANGE_DAY}" data-field-vis-hide="#period-date-range-block">
                <mvc:message code="report.recipient.previous.day"/>
            </mvc:option>

            <mvc:option value="${DATE_RANGE_WEEK}" data-field-vis-hide="#period-date-range-block">
                <mvc:message code="report.recipient.last.week"/>
            </mvc:option>

            <mvc:option value="${DATE_RANGE_30DAYS}" data-field-vis-hide="#period-date-range-block">
                <mvc:message code="report.recipient.last.week"/>
            </mvc:option>

            <mvc:option value="${DATE_RANGE_LAST_MONTH}" data-field-vis-hide="#period-date-range-block">
                <mvc:message code="report.last.month"/>
            </mvc:option>

            <mvc:option value="${DATE_RANGE_CUSTOM}" data-field-vis-show="#period-date-range-block">
                <mvc:message code="statistics.dateRange"/>
            </mvc:option>
        </mvc:select>
    </div>

    <div id="period-date-range-block" class="col-12" data-date-range>
        <div class="row g-1">
            <div class="col">
                <div class="date-picker-container">
                    <mvc:message var="fromMsg" code="From"/>
                    <mvc:text id="customStartDate_${periodPath}" path="${startDatePath}" cssClass="form-control js-datepicker" placeholder="${fromMsg}"
                              data-datepicker-options="formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'"/>
                </div>
            </div>
            <div class="col">
                <div class="date-picker-container">
                    <mvc:message var="toMsg" code="default.to"/>
                    <mvc:text id="customStopDate_${periodPath}" path="${stopDatePath}" cssClass="form-control js-datepicker" placeholder="${toMsg}"
                              data-datepicker-options="formatSubmit: '${fn:toLowerCase(reportDateFormatPattern)}'"/>
                </div>
            </div>
        </div>
    </div>
</div>
