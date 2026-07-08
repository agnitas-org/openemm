<%@ page contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.birtstatistics.monthly.MonthlyStatType" %>
<%@ page import="com.agnitas.util.importvalues.MailType" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="years" type="java.util.List<java.lang.Integer>"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticForm"--%>

<mvc:form id="stat-form" cssClass="tiles-container" servletRelativeAction="/statistics/monthly/csv.action"
          modelAttribute="form" data-controller="monthly-overview-stat" data-initializer="monthly-overview-stat">

    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="default.Overview" /></h1>
            <div class="tile-title-controls">
                <div class="input-group w-auto">
                    <span class="input-group-text input-group-text--disabled"><mvc:message code="Month"/></span>

                    <mvc:select path="month" cssClass="form-control" data-select-options="dropdownAutoWidth: true">
                        <c:forEach var="month" begin="1" end="12">
                            <mvc:option value="${month}"><mvc:message code="calendar.month.${month}" /></mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="input-group w-auto">
                    <span class="input-group-text input-group-text--disabled"><mvc:message code="Year"/></span>

                    <mvc:select path="year" cssClass="form-control" data-select-options="dropdownAutoWidth: true">
                        <mvc:options items="${years}" />
                    </mvc:select>
                </div>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <mvc:hidden id="stat-type" path="type" />

            <div id="stat-loader" class="tile tile--notification tile--processing hidden">
                <div class="tile-body">
                    <h1>
                        <i class="icon icon-sync icon-pulse"></i>
                        <mvc:message code="statistic.viewer.progressbar.prompt" />
                    </h1>
                </div>
            </div>

            <div id="stat-content" class="d-flex flex-column gap-3 h-100 hidden">
                <%-- Loads with JS--%>
            </div>
        </div>
    </div>
</mvc:form>

<template id="monthly-overview-content">
    <div class="input-group">
        <span class="input-group-text input-group-text--disabled">
            <mvc:message code="AverageMailSize"/>
        </span>
        <span class="input-group-text input-group-text--disabled flex-grow-1">
            {{= AGN.formatNumber(averageMailSizeKb) }} kB
        </span>
    </div>

    <div>
        <div class="table-wrapper" data-js-table="mailing-type-table"></div>
    </div>

    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="statistic.MonthlyStat.top10.metrics" /></h1>
            <div class="tile-title-controls">
                <div class="input-group w-auto">
                    <span class="input-group-text input-group-text--disabled">
                        <mvc:message code="statistic.base" />
                    </span>

                    <select class="form-control" data-select-options="dropdownAutoWidth: true" data-sync-to="#stat-type">
                        <c:forEach var="statType" items="${MonthlyStatType.values()}">
                            {{ if (type == '${statType}') { }}
                                <option value="${statType}" selected>
                                    <mvc:message code="${statType.messageKey}" />
                                </option>
                            {{ } else { }}
                                <option value="${statType}">
                                    <mvc:message code="${statType.messageKey}" />
                                </option>
                            {{ } }}
                        </c:forEach>
                    </select>
                </div>
            </div>
        </div>
        <div class="tile-body d-flex gap-3">
            <div class="min-w-0" style="flex: 1.8">
                <div class="table-wrapper" data-js-table="mailings-details-table"></div>
            </div>

            {{ if (amountRows.length) { }}
                <div class="flex-1 min-w-0">
                    <canvas id="amount-chart"></canvas>
                </div>
            {{ } }}
        </div>
    </div>

    <script id="mailing-type-table" type="application/json">
        {
            "columns": [
                {
                    "headerName": "<mvc:message code='mailing.Mailing_Type' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "mailing-type-cell"},
                    "type": "textCaseInsensitiveColumn",
                    "field": "mailingType",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='NumberOfMailings' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "mailing-type-table-cell"},
                    "field": "mailingsCount",
                    "type": "textCaseInsensitiveColumn",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='NumberOfMails' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "mailing-type-table-cell"},
                    "field": "emailsCount",
                    "type": "textCaseInsensitiveColumn",
                    "sortable": false
                }
            ],
            "data": {{= JSON.stringify(mailingTypesData) }},
            "options": {
                "pagination": false,
                "showRecordsCount": "simple",
                "domLayout": "autoHeight"
            }
        }
    </script>

    <script id="mailings-details-table" type="application/json">
        {
            "columns": [
                {
                    "headerName": "<mvc:message code='Mailing' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "mailing-name-cell"},
                    "type": "textCaseInsensitiveColumn",
                    "field": "shortname",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='Description' />",
                    "field": "description",
                    "type": "textCaseInsensitiveColumn",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='mailing.senddate' />",
                    "field": "date",
                    "type": "dateColumn",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='Mailtype' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "mail-type-cell"},
                    "type": "textCaseInsensitiveColumn",
                    "field": "mailType",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='default.Size' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "mail-size-cell"},
                    "type": "textCaseInsensitiveColumn",
                    "field": "sizeKb",
                    "sortable": false
                },
                {
                    "headerName": "<mvc:message code='statistic.Amount' />",
                    "cellRenderer": "MustacheTemplateCellRender",
                    "cellRendererParams": {"templateName": "statistics-number-cell"},
                    "field": "amount",
                    "type": "textCaseInsensitiveColumn",
                    "sortable": false
                }
            ],
            "data": {{= JSON.stringify(detailRows) }},
            "options": {
                "pagination": false,
                "showRecordsCount": "simple"
            }
        }
    </script>
</template>

<template id="mailing-type-cell">
    <span>
        {{ if (entry.isTotal) { }}
            <b class="text-secondary"><mvc:message code="report.total" /></b>
        {{ } else { }}
            {{= t('statistics.monthly.mailing_type.' + value) }}
        {{ } }}
    </span>
</template>

<template id="mailing-type-table-cell">
    <span>
        {{ if (entry.isTotal) { }}
            <b class="text-secondary">{{= AGN.formatNumber(value) }}</b>
        {{ } else { }}
            {{= AGN.formatNumber(value) }}
        {{ } }}
    </span>
</template>

<template id="mailing-name-cell">
    <div class="d-flex gap-2">
        <span class="square-badge" style="background: {{= entry.colorHex }}"></span>
        <span class="text-truncate-table">{{= value }}</span>
    </div>
</template>

<template id="mail-type-cell">
    <c:forEach var="mailType" items="${MailType.values()}">
        {{ if ('${mailType}' === value) { }}
            <span><mvc:message code="${mailType.messageKey}" /></span>
        {{ } }}
    </c:forEach>
</template>

<template id="mail-size-cell">
    <span>{{= AGN.formatNumber(value) }} kB</span>
</template>
