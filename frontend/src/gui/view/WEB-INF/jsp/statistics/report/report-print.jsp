<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action"%>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="emm"   uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="darkMode" type="java.lang.Boolean"--%>
<%--@elvariable id="windowWaitStatus" type="java.lang.String"--%>
<%--@elvariable id="figures" type="java.util.List<com.agnitas.reporting.birt.external.utils.BirtReportUtils.BirtReportFigure>"--%>
<%--@elvariable id="mailingIds" type="java.util.List<java.lang.Integer>"--%>
<%--@elvariable id="reportLocale" type="java.lang.String"--%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><mvc:message code="Report" /></title>

    <link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

    <tiles:insertTemplate template="/WEB-INF/jsp/page-template/assets.jsp"/>

    <script>
        window.adminLocale = ${emm:toJson(reportLocale)};
    </script>
</head>
<body id="statistics-report-page" data-controller="mailing-statistics-report" data-bs-theme="${darkMode ? 'dark' : 'light'}">
<script id="config:statistics-report" type="application/json">
    {
        "windowWaitStatus": ${emm:toJson(windowWaitStatus)},
        "mailingIds": ${emm:toJson(mailingIds)},
        "mailingsData": ${emm:toJson(mailingsData)},
        "figures": ${emm:toJson(figures)}
    }
</script>

<div data-initializer="statistics-report"></div>

<script id="mailing-report-section" type="text/x-mustache-template">
    <h3 class="fw-medium text-secondary mb-2 {{= forceNewPage ? 'force-new-page' : '' }}">
        <mvc:message code="birt.report.mailingStatisticsIndividually" />
    </h3>

    <div class="d-flex gap-3 align-items-start">
        {{ if (generalInfoRows) { }}
        <div class="mailing-info-column">
            <table class="table table--borderless table--xs table-layout-fixed">
                <thead>
                <tr>
                    <th colspan="2">{{- t('statistics.report.general') }}</th>
                </tr>
                </thead>
                <tbody>
                {{ generalInfoRows.forEach(row => { }}
                <tr>
                    <td>{{- row.label }}</td>
                    <td>
                        {{ if (row.rawHtml) { }}
                            {{= row.value }}
                        {{ } else { }}
                            {{- row.value }}
                        {{ } }}
                    </td>
                </tr>
                {{ }); }}
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="2"></td>
                </tr>
                </tfoot>
            </table>
        </div>
        {{ } }}

        <div class="mailing-preview-column">
            <div class="bordered-box-xs flex-center mailing-thumbnail">
                <img src="{{- thumbnailUrl }}" alt="Mailing thumbnail" class="img-fluid mailing-thumbnail-img">
            </div>

            {{ if (mailFormatColumns) { }}
            <h5 class="text-secondary fw-medium mt-2 mb-1">
                <mvc:message code="report.recipient.statistics.bymailingtype" />
            </h5>

            <table class="table table--borderless table--xs table-layout-fixed">
                <thead>
                <tr>
                    {{ mailFormatColumns.forEach(col => { }}
                    <th>{{- col.label }}</th>
                    {{ }); }}
                </tr>
                </thead>
                <tbody>
                <tr>
                    {{ mailFormatColumns.forEach(col => { }}
                    <td>{{- col.value }} ({{- col.percent }}%)</td>
                    {{ }); }}
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <td colspan="{{= mailFormatColumns.length }}"></td>
                </tr>
                </tfoot>
            </table>
            {{ } }}
        </div>
    </div>

    {{ if (hasSummary) { }}
    <div class="tile tile--xs mt-2 summary-chart-tile">
        <div class="tile-header tile-header--compact">
            <h4 class="tile-title text-secondary">{{- t('statistics.default.summary') }}</h4>
        </div>
        <div class="tile-body border-top p-2">
            <canvas data-summary-chart="{{= mailingId }}"></canvas>
        </div>
    </div>
    <div class="summary-table-page force-new-page">
        <h3 class="text-secondary fw-medium mt-2 mb-1">{{- t('statistics.default.summary') }}</h3>
        <div class="summary-table-container" data-summary-tables="{{= mailingId }}"></div>
    </div>
    {{ } }}

    <div data-device-stats="{{= mailingId }}"></div>
    <div data-bounces-and-links="{{= mailingId }}"></div>
    <div data-link-clicks-detail="{{= mailingId }}"></div>
    <div data-domain-stats="{{= mailingId }}"></div>
</script>

<script id="mailing-summary-table" type="text/x-mustache-template">
    <table class="table table--borderless table--xs table--stretch-rows {{= idx > 0 ? 'mt-2' : '' }}">
        <thead>
        <tr>
            <th>{{- t('statistics.default.summary') }}</th>
            {{ columns.forEach(col => { }}
            <th>{{- col.targetName }}</th>
            {{ }); }}
        </tr>
        </thead>
        <tbody style="--table-row-count: {{= rowCount }}">
        {{ rows.forEach(row => { }}
        <tr>
            <td{{= row.subLabel ? ' class="border-bottom-0"' : '' }}>
                <span class="d-flex align-items-center gap-2">
                    <span class="square-badge square-badge--sm" style="background:{{= row.color }}"></span>
                    {{- row.label }}
                </span>
            </td>
            {{ row.cells.forEach(cell => { }}
            <td{{= row.subLabel ? ' class="border-bottom-0"' : '' }}>
                {{= cell }}
            </td>
            {{ }); }}
        </tr>
        {{ if (row.subLabel) { }}
        <tr>
            <td class="ps-3">
                <span class="d-flex align-items-center gap-2">
                    <span class="square-badge square-badge--sm" style="background:{{= row.subColor }}"></span>
                    {{- row.subLabel }}
                </span>
            </td>
            {{ row.subCells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        {{ } }}
        {{ }); }}
        </tbody>
        <tfoot>
        <tr>
            <td colspan="{{= 1 + columns.length }}"></td>
        </tr>
        </tfoot>
    </table>
</script>

<script id="section-heading" type="text/x-mustache-template">
    <h3 class="fw-medium text-secondary mt-4 mb-2 {{= forceNewPage ? 'force-new-page' : '' }}">{{- title }}</h3>
</script>

<script id="link-clicks-chart-page" type="text/x-mustache-template">
    <div class="link-clicks-chart-page force-new-page">
        <h3 class="fw-medium text-secondary mt-4 mb-2">{{- title }}</h3>
        <div class="tile tile--xs mb-3 link-clicks-chart-tile">
            <div class="tile-header tile-header--compact">
                <h4 class="tile-title text-secondary">{{- title }}</h4>
            </div>
            <div class="tile-body p-2 border-top">
                <canvas></canvas>
            </div>
        </div>
        <div class="bordered-box-sm link-clicks-chart-targets">
            {{ legends.forEach(legend => { }}
                <div class="d-flex align-items-center gap-1 w-100">
                    <span class="square-badge" style="background: {{- legend.color }}"></span>
                    <span class="text-secondary fw-medium fs-5">{{- legend.name }}</span>
                </div>
            {{ }); }}
        </div>
    </div>
</script>

<script id="stat-section" type="text/x-mustache-template">
    <div>
        <h3 class="fw-medium text-secondary mt-4 mb-2 {{= forceNewPage ? 'force-new-page' : '' }}">{{- title }}</h3>
        <div class="stat-section-charts" data-charts></div>
        <div data-table></div>
    </div>
</script>

<script id="stat-donut-tile" type="text/x-mustache-template">
    <div class="tile tile--xs stat-donut-tile">
        <div class="tile-header tile-header--compact">
            <h4 class="tile-title text-secondary">{{- targetName }}</h4>
        </div>
        <div class="tile-body p-2 border-top">
            {{ if (hasData) { }}
                <canvas></canvas>
            {{ } else { }}
                <div class="notification-simple">
                    <i class="icon icon-info-circle"></i>
                    <span class="fs-4"><mvc:message code="noResultsFound" /></span>
                </div>
            {{ } }}
        </div>
    </div>
</script>

<script id="stat-legend-table" type="text/x-mustache-template">
    <table class="table table--borderless table--xs table-layout-fixed {{= idx > 0 ? 'mt-2' : '' }}">
        <thead>
        <tr>
            <th>{{- title }}</th>
            {{ targets.forEach(target => { }}
            <th>{{- target.name }}</th>
            {{ }); }}
        </tr>
        </thead>
        <tbody>
        {{ rows.forEach(row => { }}
        <tr{{= row.bold ? ' class="fw-medium"' : '' }}>
            <td>
                <span class="d-flex align-items-center gap-2">
                    {{ if (row.color) { }}
                        <span class="square-badge square-badge--sm" style="background:{{= row.color }}"></span>
                    {{ } }}
                    {{- row.label }}
                </span>
            </td>
            {{ row.cells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        {{ }); }}
        </tbody>
        <tfoot>
        <tr>
            <td colspan="{{= 1 + targets.length }}"></td>
        </tr>
        </tfoot>
    </table>
</script>

<script id="bounce-section" type="text/x-mustache-template">
    <div class="d-flex gap-3 bounce-row">
        <div class="bounce-chart-column" data-chart></div>
        <div class="bounce-legend-column" data-table></div>
    </div>
</script>

<script id="link-clicks-summary-table" type="text/x-mustache-template">
    <table class="table table--borderless table--link-summary-groups table--xs table-layout-fixed {{= idx > 0 ? 'mt-2' : '' }}">
        <thead>
        <tr>
            <th colspan="2">{{- title }}</th>
            {{ targets.forEach(target => { }}
            <th>{{- target.name }}</th>
            {{ }); }}
        </tr>
        </thead>
        {{ groups.forEach(group => { }}
        <tbody class="link-group">
        <tr>
            <td class="fw-medium table-row-divider" rowspan="{{= group.rows.length }}">{{- group.label }}</td>
            <td>{{- group.rows[0].label }}</td>
            {{ group.rows[0].cells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        {{ group.rows.slice(1).forEach((row, idx) => { }}
        <tr{{= idx === group.rows.length - 2 ? ' class="table-row-divider"' : '' }}>
            <td>{{- row.label }}</td>
            {{ row.cells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        {{ }); }}
        </tbody>
        {{ }); }}
        <tbody>
        <tr class="fw-medium">
            <td colspan="2">{{- t('statistics.default.total') }}</td>
            {{ totalCells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        </tbody>
        <tfoot>
        <tr>
            <td colspan="{{= 2 + targets.length }}"></td>
        </tr>
        </tfoot>
    </table>
</script>

<script id="link-category-table" type="text/x-mustache-template">
    <table class="table table--borderless table--link-groups table--xs table-layout-fixed mb-2 {{= forceNewPage ? 'force-new-page' : '' }} {{= idx > 0 ? 'mt-2' : '' }}">
        <thead>
        <tr>
            <th>{{- title }}</th>
            {{ targets.forEach(target => { }}
            <th>{{- target.name }}</th>
            {{ }); }}
        </tr>
        </thead>
        {{ links.forEach(link => { }}
        <tbody class="link-group">
        <tr class="fw-medium">
            <td colspan="{{= 1 + targets.length }}">{{= link.number }}: {{- link.url }}</td>
        </tr>
        {{ link.rows.forEach(row => { }}
        <tr class="{{= row.rowClass }}">
            <td>{{- row.label }}</td>
            {{ row.cells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        {{ }); }}
        </tbody>
        {{ }); }}
        <tfoot>
        <tr>
            <td colspan="{{= 1 + targets.length }}"></td>
        </tr>
        </tfoot>
    </table>
</script>

<script id="domain-table" type="text/x-mustache-template">
    <table class="table table--borderless table--xs table-layout-fixed mb-3">
        <thead>
        <tr>
            <th>{{- title }}</th>
            {{ targets.forEach(target => { }}
            <th>{{- target.name }}</th>
            {{ }); }}
        </tr>
        </thead>
        <tbody>
        {{ rows.forEach(row => { }}
        <tr>
            <td>{{- row.domain }}</td>
            {{ row.cells.forEach(cell => { }}
            <td>{{= cell }}</td>
            {{ }); }}
        </tr>
        {{ }); }}
        </tbody>
        <tfoot>
        <tr>
            <td colspan="{{= 1 + targets.length }}"></td>
        </tr>
        </tfoot>
    </table>
</script>

</body>
</html>
