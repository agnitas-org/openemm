<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="dashboardForm" type="com.agnitas.emm.core.dashboard.form.DashboardForm"--%>
<%--@elvariable id="mailinglist" type="com.agnitas.beans.PaginatedList<java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="randomInactiveFeaturePackage" type="com.agnitas.emm.premium.bean.FeaturePackage"--%>
<%--@elvariable id="layout" type="java.lang.String"--%>

<div id="dashboard-tiles" class="tiles-container" data-controller="dashboard-grid" data-initializer="dashboard-grid">

    <script id="config:dashboard-grid" type="application/json">
        {
            "layout" : ${emm:toJson(layout)}
        }
    </script>

    <%-- Loads by JS --%>
</div>

<emm:ShowByPermission token="stats.mailing">
    <%@ include file="fragments/tiles/statistics-tile.jspf" %>
    <%@ include file="fragments/tiles/clickers-tile.jspf" %>
    <%@ include file="fragments/tiles/openers-tile.jspf" %>
</emm:ShowByPermission>
<%@ include file="fragments/tiles/add-ons-tile.jspf" %>
<%@ include file="fragments/tiles/planning-tile.jspf" %>
<%@ include file="fragments/tiles/news-tile.jspf" %>
<%@ include file="fragments/tiles/imports-exports-tile.jspf" %>
<%@ include file="fragments/tiles/mailings-tile.jspf" %>
<%@ include file="fragments/tiles/workflows-tile.jspf" %>
<%@ include file="fragments/tiles/empty-tile.jspf" %>
<%@ include file="fragments/tiles/analysis-tile.jspf" %>
<jsp:include page="fragments/tiles/calendar-tile.jsp" />

<script id="dashboard-tiles-selection-modal" type="text/x-mustache-template">
    <div id="choose-tile-modal" class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="dashboard.tile.add"/></h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body js-scrollable">
                    <div class="row row-cols-4">
                        {{ _.each(tiles, tile => { }}
                        <div class="col" data-field="toggle-vis">
                            {{ _.each(tile.variants, variant => { }}
                            {{var checkboxControl = tile.variants.length > 1 ? 'data-' + (variant.name === 'tall' ? 'show' : 'hide') + '-by-checkbox=#switch-' + tile.id + '-tile' : '';}}
                            <div class="tile-thumbnail {{- variant.disabled ? 'disabled' : '' }}"
                                 data-action="{{= variant.disabled ? '' : 'add-tile'}}"
                                 data-type="{{- variant.name }}"
                                 data-tile-id="{{- tile.id }}"
                                 {{- checkboxControl }}>
                                <svg>
                                    <use href="{{- tile.thumbnail(variant) }}"></use>
                                </svg>
                                <div class="overlay rounded">
                                    {{ if (variant.disabled) { }}
                                    <mvc:message code="error.dashboard.tile.space"/>
                                    {{ } else { }}
                                    <i class="icon icon-plus absolute-center"></i>
                                    {{ } }}
                                </div>
                            </div>
                            {{ }) }}
                            {{ if (tile.variants.length > 1) { }}
                            <div class="tile-name d-flex justify-content-between">
                                <b>{{- tile.name }}</b>
                                <label class="switch switch--sm">
                                    <input type="checkbox" id="switch-{{- tile.id }}-tile">
                                    <i class="colon-icon align-items-center"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                    <i class="colon-icon" style="transform: rotate(90deg)"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                </label>
                            </div>
                            {{ } else { }}
                            <div class="tile-name">
                                <b>{{- tile.name }}</b>
                                {{ const variant = tile.variants[0]?.name; }}
                                {{ if (variant === 'regular') { }}
                                <i class="icon icon-square regular-tile-icon"></i>
                                {{ } else if (variant === 'xl') { }}
                                <div class="d-flex flex-column ms-1" style="gap:1px">
                                    <i class="colon-icon wide-tile-icon"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                    <i class="colon-icon wide-tile-icon"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                </div>
                                {{ } else if (variant === 'x-wide') { }}
                                <i class="colon-icon wide-tile-icon ms-1"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                {{ } else { }}
                                <i class="colon-icon wide-tile-icon" style="transform: rotate({{- variant === 'tall' ? '90deg' : '0'}})"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                {{ } }}
                            </div>
                            {{ } }}
                        </div>
                        {{ }) }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="dashboard-layout-selection-modal" type="text/x-mustache-template">
    <div id="choose-layout-modal" class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="dashboard.layout.select"/></h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body row row-cols-3">
                    <div class="col">
                        {{ var cols = 2; }}
                        <div class="grid border rounded p-2 h-100" data-cols-count="{{- cols }}" data-action="change-layout" data-bs-dismiss="modal" style="--bs-columns: {{- cols}}">
                            {{ for (let i = 0; i < cols * 2; i++) { }}
                                <div class="rounded-1"></div>
                            {{ } }}
                        </div>
                    </div>
                    <div class="col">
                        {{ var cols = 3; }}
                        <div class="grid border rounded p-2" data-cols-count="{{- cols }}" data-action="change-layout" data-bs-dismiss="modal" style="--bs-columns: {{- cols}}">
                            {{ for (let i = 0; i < cols * 2; i++) { }}
                                <div class="rounded-1 ratio ratio-1x1"></div>
                            {{ } }}
                        </div>
                    </div>
                    <div class="col">
                        {{ cols = 4; }}
                        <div class="grid border rounded p-2 h-100" data-cols-count="{{- cols }}" data-action="change-layout" data-bs-dismiss="modal" style="--bs-columns: {{- cols}}">
                            {{ for (let i = 0; i < cols * 2; i++) { }}
                                <div class="rounded-1"></div>
                            {{ } }}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="dashboard-replace-tiles-question-modal" type="text/x-mustache-template">
    <div class="modal modal-warning" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="warning"/></h1>
                    <button type="button" class="btn-close" data-confirm-negative>
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body">
                    <p><mvc:message code="warning.tile.replace" arguments="${['{{= tileNames }}']}" /></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-danger" data-confirm-negative>
                        <i class="icon icon-times"></i>
                        <span class="text"><mvc:message code="button.replace.tile.no"/></span>
                    </button>

                    <button type="button" class="btn btn-success" data-confirm-positive>
                        <i class="icon icon-check"></i>
                        <span class="text"><mvc:message code="button.replace.tile.yes"/></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>

<script id="dashboard-schedule-day" type="text/x-mustache-template">
    <div class="schedule__day grid" style="--bs-columns: 1;">
        <h2 class="schedule__day-date text-truncate">{{- dayOfWeek }}, {{- dateStr }}</h2>
        <div class="schedule__day-mailings grid">
            {{ if (_.isEmpty(dayMailings)) { }}
                <div class="notification-simple">
                    <i class="icon icon-info-circle"></i>
                    <span><mvc:message code='dashboard.mailing.noMailings'/></span>
                </div>
            {{ } else { }}
                {{ _.each(dayMailings, function(mailing) { }}
                    <a class="schedule__day-mailing overflow-hidden" href="{{- mailing.link }}" data-mailing-id="{{- mailing.id }}">
                        <span class="schedule__send-time">{{- mailing.sendTime }}</span>
                        <div class="schedule__mailing-info">
                            <div class="schedule__mailing-name">
                                <span class="status-badge {{- mailing.status }}" data-tooltip="{{- t(mailing.status) }}"></span>
                                <span class="text-truncate">{{- mailing.shortname }}</span>
                            </div>
                            <div class="schedule__mailing-mailinglist">
                                <i class="icon icon-list"></i>
                                <span class="text-truncate">{{- mailing.mailinglistName }}</span>
                            </div>
                        </div>
                    </a>
                {{ }) }}
            {{ } }}
        </div>
    </div>
</script>

<script id="dashboard-tile-overlay" type="text/x-mustache-template">
    <div class="tile-overlay tile-overlay--visible">
        <div class="d-inline-flex flex-column gap-2">
            <button type="button" class="btn btn-lg btn-sm-horizontal tile-overlay__btn" data-action="delete-tile">
                <i class="icon icon-trash-alt"></i>
                <span class="text"><mvc:message code="default.tile.remove" /></span>
            </button>

            <button type="button" class="btn btn-lg btn-primary btn-sm-horizontal" data-action="replace-tile">
                <i class="icon icon-sync-alt"></i>
                <span class="text"><mvc:message code="default.tile.replace" /></span>
            </button>
        </div>
    </div>
</script>

<%@ include file="fragments/mailing-popover.jspf"%>

<%@ include file="fragments/news-fragments.jspf"%>
