<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.monthly.MonthlyStatType" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="monthlyStatisticForm" type="com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticForm"--%>
<%--@elvariable id="birtStatisticUrlCsvReport" type="java.lang.String"--%>
<%--@elvariable id="birtStatisticUrlWithoutFormat" type="java.lang.String"--%>
<%--@elvariable id="monthList" type="java.util.List"--%>
<%--@elvariable id="yearList" type="java.util.List"--%>

<c:set var="MONTHLY_RECIPIENT_NUM" value="<%= MonthlyStatType.RECIPIENT_NUM.getCode() %>"/>
<c:set var="MONTHLY_OPENERS" value="<%= MonthlyStatType.OPENERS.getCode() %>"/>
<c:set var="MONTHLY_ANONYMOUS_OPENINGS" value="<%= MonthlyStatType.ANONYMOUS_OPENINGS.getCode() %>"/>
<c:set var="MONTHLY_CLICKERS" value="<%= MonthlyStatType.CLICKERS.getCode() %>"/>
<c:set var="MONTHLY_ANONYMOUS_CLICKS" value="<%= MonthlyStatType.ANONYMOUS_CLICKS.getCode() %>"/>

<mvc:form servletRelativeAction="/statistics/monthly/view.action" modelAttribute="monthlyStatisticForm">
    <div class="tile">
        <div class="tile-header">
            <a class="headline" href="#" data-toggle-tile="#tile-statisticsMonthly">
                <i class="tile-toggle icon icon-angle-down"></i>
                <mvc:message code="topTen"/>
            </a>
            <ul class="tile-header-actions">
                <li>
                    <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                        <i class="icon icon-refresh"></i>
                        <span class="text"><mvc:message code="button.Refresh"/></span>
                    </button>
                </li>
            </ul>
        </div>
        <div id="tile-statisticsMonthly" class="tile-content tile-content-forms">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><mvc:message code="statistic.MonthlyStat.Top10Metrics"/></label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="top10MetricsId" cssClass="form-control select2-offscreen">
                        <mvc:option value="${MONTHLY_RECIPIENT_NUM}"><mvc:message code="report.numberRecipients"/></mvc:option>
                        <mvc:option value="${MONTHLY_OPENERS}"><mvc:message code="statistic.opener"/></mvc:option>
                        <mvc:option value="${MONTHLY_ANONYMOUS_OPENINGS}"><mvc:message code="statistic.openings.anonym"/></mvc:option>
                        <mvc:option value="${MONTHLY_CLICKERS}"><mvc:message code="statistic.clicker"/></mvc:option>
                        <mvc:option value="${MONTHLY_ANONYMOUS_CLICKS}"><mvc:message code="statistic.clicks.anonym"/></mvc:option>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h2 class="headline">
                <c:set var="top10MetricsValue" value="${monthlyStatisticForm.top10MetricsId}"/>
                
                <c:choose>
                    <c:when test="${top10MetricsValue eq MONTHLY_RECIPIENT_NUM}"><mvc:message code="topTenByMailings"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_OPENERS}"><mvc:message code="topTenByOpenings"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_CLICKERS}"><mvc:message code="topTenByClickRecipients"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_ANONYMOUS_OPENINGS}"><mvc:message code="statistic.MonthlyStat.Top10.openings.anonymous"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_ANONYMOUS_CLICKS}"><mvc:message code="statistic.MonthlyStat.Top10.clicks.anonymous"/></c:when>
                </c:choose>
            </h2>
            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-cloud-download"></i>
                        <span class="text"><mvc:message code="Export"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header"><mvc:message code="statistics.exportFormat"/></li>
                        <li>
                            <a href="${birtStatisticUrlCsvReport}&__format=csv" tabindex="-1" data-prevent-load="">
                                <i class="icon icon-file-excel-o"></i><mvc:message code='export.message.csv'/>
                            </a>
                        </li>
                    </ul>
                </li>
            </ul>
        </div>

        <div class="tile-content">
            <div class="tile-controls">
                <div class="controls controls-left">
                    <div class="control">
                        <mvc:select path="startMonth" cssClass="form-control select2-offscreen">
                            <c:forEach items="${monthList}" var="month">
                                <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                    <div class="control">
                        <mvc:select path="startYear" cssClass="form-control select2-offscreen">
                                <mvc:options items="${yearList}" />
                        </mvc:select>
                    </div>
                </div>

                <div class="controls controls-right">
                    <div class="control">
                        <button class="btn btn-primary btn-regular" type="button" data-form-submit>
                            <i class="icon icon-refresh"></i>
                            <span class="text"><mvc:message code="button.Refresh"/></span>
                        </button>
                    </div>
                </div>
            </div>

            <iframe id="birt-frame" src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" width="100%" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
