<%@ page contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<div id="mailings-comparison-view" class="tiles-container" data-controller="mailings-comparison-stat-view">
    <mvc:form id="stat-form" cssClass="tile" servletRelativeAction="/statistics/mailing/comparison/compare.action"
              modelAttribute="form" data-form="resource">

        <script data-initializer="mailings-comparison-stat-view" type="application/json">
            {
                "ids": ${emm:toJson(form.bulkIds)},
                "barCharts": [
                    {
                        "label": "<mvc:message code="statistic.mails.sent" />",
                        "property": "sentEmails"
                    },
                    {
                        "label": "<mvc:message code="statistic.mails.delivered" />",
                        "property": "acceptedEmails"
                    },
                    {
                        "label": "<mvc:message code="statistic.Opt_Outs" />",
                        "property": "optOuts"
                    },
                    {
                        "label": "<mvc:message code="statistic.bounces.hardbounce" />",
                        "property": "hardBounces"
                    },
                    {
                        "label": "<mvc:message code="report.openers.measured" />",
                        "property": "measuredOpeners"
                    },
                    {
                        "label": "<mvc:message code="report.opens.invisibleWithoutNote" />",
                        "property": "invisibleOpeners"
                    },
                    {
                        "label": "<mvc:message code="statistic.opener.total" />",
                        "property": "totalOpeners"
                    },
                    {
                        "label": "<mvc:message code="statistic.openers.anonym" />",
                        "property": "anonymousOpenersCount"
                    },
                    {
                        "label": "<mvc:message code="statistic.clicker" />",
                        "property": "clickers"
                    },
                    {
                        "label": "<mvc:message code="statistic.clicks.anonym" />",
                        "property": "anonymousClickersCount"
                    },
                    {
                        "label": "<mvc:message code="report.softbounces.undeliverable" />",
                        "property": "withoutReceiptConfirmation"
                    },
                    {
                        "label": "<mvc:message code="Revenue" />",
                        "property": "revenue"
                    }
                ]
            }
        </script>

        <mvc:hidden path="bulkIds"/>

        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="default.Overview"/></h1>
        </div>
        <div class="tile-body d-flex flex-column gap-3 h-100 js-scrollable">
            <div id="stat-loader" class="tile tile--notification tile--processing hidden">
                <div class="tile-body">
                    <h1>
                        <i class="icon icon-sync icon-pulse"></i>
                        <mvc:message code="statistic.viewer.progressbar.prompt" />
                    </h1>
                </div>
            </div>

            <div id="mailings-data" class="hidden">
                <div class="d-flex flex-column gap-inherit">
                    <div class="table-wrapper w-100" data-table-cfg="#base-info-table"></div>
                    <div class="table-wrapper w-100" data-table-cfg="#send-info-table"></div>
                    <div class="table-wrapper w-100" data-table-cfg="#openers-table"></div>
                    <div class="table-wrapper w-100" data-table-cfg="#clickers-table"></div>
                </div>
                <div class="tile">
                    <div class="tile-body">
                        <div id="charts-container">
                            <%-- Loads by JS --%>
                        </div>
                    </div>
                </div>
            </div>

            <div id="statistics-footer-info" class="notification-simple notification-simple--lg hidden">
                <i class="icon icon-info-circle"></i>
                <div class="d-flex flex-column gap-2">
                    <span>
                        <mvc:message var="explanationMsg" code="report.opens.measured.explanation" />
                        ${fn:replace(explanationMsg, '[2]', '[1]')}
                    </span>
                    <span>
                        <mvc:message var="explanationMsg" code="report.opens.invisible.explanation" />
                        ${fn:replace(explanationMsg, '[3]', '[2]')}
                    </span>
                    <span>
                        <mvc:message var="explanationMsg" code="statistic.withoutConfirmation.explanation" />
                        ${fn:replace(explanationMsg, '[7]', '[3]')}
                    </span>
                </div>
            </div>
        </div>
    </mvc:form>
</div>

<script id="base-info-table" type="application/json">
    {
        "columns": [
            {
                "headerName": "<mvc:message code='Mailing'/>",
                "type": "colorBadge",
                "field": "name",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='Description'/>",
                "cellRenderer": "NotEscapedStringCellRenderer",
                "field": "description",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='mailing.senddate'/>",
                "field": "sendDate",
                "type": "dateColumn",
                "sortable": false
            }
        ],
        "options": {
            "pagination": false,
            "showRecordsCount": "simple",
            "domLayout": "autoHeight"
        }
    }
</script>

<script id="send-info-table" type="application/json">
    {
        "columns": [
            {
                "headerName": "<mvc:message code='Mailing'/>",
                "type": "colorBadge",
                "field": "name",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.mails.sent'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "sentEmails",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.mails.delivered'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "acceptedEmails",
                "type": "textCaseInsensitiveColumn",
                "autoHideIfAllNull": true,
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.Opt_Outs'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "optOuts",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.bounces.hardbounce'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "hardBounces",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            }
        ],
        "options": {
            "pagination": false,
            "showRecordsCount": "simple",
            "domLayout": "autoHeight"
        }
    }
</script>

<script id="openers-table" type="application/json">
    {
        "columns": [
            {
                "headerName": "<mvc:message code='Mailing'/>",
                "type": "colorBadge",
                "field": "name",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='report.openers.measured'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "measuredOpeners",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='report.opens.invisibleWithoutNote'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "invisibleOpeners",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.opener.total'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "totalOpeners",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.openers.anonym'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "statistics-number-cell"},
                "field": "anonymousOpenersCount",
                "type": "numberColumn",
                "sortable": false
            }
        ],
        "options": {
            "pagination": false,
            "showRecordsCount": "simple",
            "domLayout": "autoHeight"
        }
    }
</script>

<script id="clickers-table" type="application/json">
    {
        "columns": [
            {
                "headerName": "<mvc:message code='Mailing'/>",
                "type": "colorBadge",
                "field": "name",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.clicker'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "clickers",
                "type": "textCaseInsensitiveColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='statistic.clicks.anonym'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "statistics-number-cell"},
                "field": "anonymousClickersCount",
                "type": "numberColumn",
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='report.softbounces.undeliverable'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "stat-metric-cell"},
                "field": "withoutReceiptConfirmation",
                "type": "textCaseInsensitiveColumn",
                "autoHideIfAllNull": true,
                "sortable": false
            },
            {
                "headerName": "<mvc:message code='Revenue'/>",
                "cellRenderer": "MustacheTemplateCellRender",
                "cellRendererParams": {"templateName": "revenue-cell"},
                "field": "revenue",
                "type": "numberColumn",
                "autoHideIfAllNull": true,
                "sortable": false
            }
        ],
        "options": {
            "pagination": false,
            "showRecordsCount": "simple",
            "domLayout": "autoHeight"
        }
    }
</script>

<template id="stat-metric-cell">
    {{ if (value != null) { }}
        <span>{{= AGN.formatNumber(value.value) }} ({{= value.rate.toFixed(1) }}%)</span>
    {{ } }}
</template>

<template id="revenue-cell">
    <span>{{= AGN.formatNumber(value) }}</span>
</template>

<template id="bar-chart">
    <div class="comparison-bar-chart-container">
        <p>{{= label }}</p>
        <div style="height: {{= height }};">
            <canvas class="h-100"></canvas>
        </div>
    </div>
</template>
