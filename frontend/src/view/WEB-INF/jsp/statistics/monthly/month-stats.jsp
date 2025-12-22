<%@ page contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
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

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/statistics/monthly/view.action"
          data-form="resource" modelAttribute="monthlyStatisticForm" data-editable-view="${agnEditViewKey}">

    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter" /></h1>
        </div>

        <div class="tile-body">
            <div class="row g-3">
                <div class="col">
                    <label for="top10MetricsId" class="form-label"><mvc:message code="statistic.MonthlyStat.Top10Metrics"/></label>

                    <mvc:select id="top10MetricsId" path="top10MetricsId" cssClass="form-control">
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

    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate">
                <c:set var="top10MetricsValue" value="${monthlyStatisticForm.top10MetricsId}"/>

                <c:choose>
                    <c:when test="${top10MetricsValue eq MONTHLY_RECIPIENT_NUM}"><mvc:message code="topTenByMailings"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_OPENERS}"><mvc:message code="topTenByOpenings"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_CLICKERS}"><mvc:message code="topTenByClickRecipients"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_ANONYMOUS_OPENINGS}"><mvc:message code="statistic.MonthlyStat.Top10.openings.anonymous"/></c:when>
                    <c:when test="${top10MetricsValue eq MONTHLY_ANONYMOUS_CLICKS}"><mvc:message code="statistic.MonthlyStat.Top10.clicks.anonymous"/></c:when>
                </c:choose>
            </h1>

            <div class="tile-title-controls">
                <mvc:select path="startMonth" cssClass="form-control" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                    <c:forEach items="${monthList}" var="month">
                        <mvc:option value="${month[0]}"><mvc:message code="${month[1]}"/></mvc:option>
                    </c:forEach>
                </mvc:select>

                <mvc:select path="startYear" cssClass="form-control" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                    <mvc:options items="${yearList}" />
                </mvc:select>
            </div>
        </div>

        <div class="tile-body p-2 js-scrollable" style="overflow-y: auto !important;">
            <iframe src="${birtStatisticUrlWithoutFormat}&__format=html" border="0" scrolling="auto" style="width: 100%; height: 100px" frameborder="0">
                Your Browser does not support IFRAMEs, please update!
            </iframe>
        </div>
    </div>
</mvc:form>
