<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.birtstatistics.domain.form.DomainStatisticForm"--%>
<%--@elvariable id="targetList" type="java.util.List"--%>
<%--@elvariable id="mailingLists" type="java.util.List"--%>
<%--@elvariable id="statistics" type="java.util.List<com.agnitas.reporting.birt.external.beans.DomainStatRow>"--%>

<mvc:form cssClass="tiles-container flex-column" id="stat-form" servletRelativeAction="/statistics/domain/view.action"
          method="GET" modelAttribute="form" data-form="resource"
          data-editable-view="${agnEditViewKey}" data-controller="domain-statistics-view">

    <div id="filter-tile" class="tile h-auto flex-none" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.filter" /></h1>
        </div>

        <div class="tile-body">
            <div class="row g-3">
                <div class="col">
                    <label class="form-label" for="targetId"><mvc:message code="Target"/></label>

                    <mvc:select path="targetId" id="targetId" size="1" cssClass="form-control">
                        <mvc:option value=""><mvc:message code="statistic.all_subscribers"/></mvc:option>
                        <mvc:options items="${targetList}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>

                <div class="col">
                    <label class="form-label" for="mailinglistId"><mvc:message code="Mailinglist"/></label>
                    <mvc:select path="mailinglistId" id="mailinglistId" size="1" cssClass="form-control">
                        <mvc:option value=""><mvc:message code="statistic.All_Mailinglists"/></mvc:option>
                        <mvc:options items="${mailingLists}" itemValue="id" itemLabel="shortname"/>
                    </mvc:select>
                </div>

                <div class="col">
                    <label class="form-label" for="maxDomainNum"><mvc:message code="domains.max"/></label>
                    <mvc:select path="maxDomainNum" id="maxDomainNum" size="1" cssClass="form-control">
                        <mvc:option value="5" label="5"/>
                        <mvc:option value="10" label="10"/>
                        <mvc:option value="15" label="15"/>
                        <mvc:option value="20" label="20"/>
                        <mvc:option value="25" label="25"/>
                        <mvc:option value="50" label="50"/>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div id="overview-tile" class="tile" data-editable-tile="main">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="report.mailing.statistics.select"/></h1>
            <div class="tile-title-controls">
                <mvc:select path="topLevelDomain" cssClass="form-control" data-form-submit="" data-select-options="dropdownAutoWidth: true, width: 'auto'">
                    <mvc:option value="false"><mvc:message code="Domains"/></mvc:option>
                    <mvc:option value="true"><mvc:message code="ToplevelDomains"/></mvc:option>
                </mvc:select>
            </div>
        </div>
        <div class="tile-body d-grid gap-3 js-scrollable" style="grid-template-columns: repeat(2, 1fr);">
            <script type="application/json" data-initializer="domain-statistics-view">
                {
                    "statistics": ${emm:toJson(statistics)}
                }
            </script>

            <div class="table-wrapper" data-js-table="domain-stat-table">
                <script id="domain-stat-table" type="application/json">
                    {
                        "columns": [
                            {
                                "headerName": "<mvc:message code='statistic.domain' />",
                                "field": "domainName",
                                "type": "colorBadge"
                            },
                            {
                                "headerName": "<mvc:message code='statistic.amount.percent' />",
                                "cellRenderer": "MustacheTemplateCellRender",
                                "cellRendererParams": {"templateName": "amount-cell"},
                                "field": "count",
                                "type": "textCaseInsensitiveColumn",
                                "suppressSizeToFit": true
                            }
                        ],
                        "options": {
                            "pagination": false,
                            "showRecordsCount": "simple"
                        },
                        "data": ${emm:toJson(statistics)}
                    }
                </script>
            </div>

            <div class="tile">
                <div class="tile-body">
                    <c:if test="${empty statistics}">
                        <div class="notification-simple">
                            <i class="icon icon-info-circle"></i>
                            <span><mvc:message code="noResultsFound" /></span>
                        </div>
                    </c:if>
                    <canvas id="domain-stat-chart" class="h-100">
                        <%-- Loads by JS --%>
                    </canvas>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<template id="amount-cell">
    <span>{{= AGN.formatNumber(value) }} ({{= entry.rate.toFixed(1) }}%)</span>
</template>
