<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="dashboardForm" type="com.agnitas.emm.core.dashboard.form.DashboardForm"--%>
<%--@elvariable id="mailinglist" type="org.agnitas.beans.impl.PaginatedListImpl<java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="randomInactiveFeaturePackage" type="com.agnitas.emm.premium.bean.FeaturePackage"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="layout" type="java.lang.String"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<div id="dashboard-tiles" class="tiles-container" data-controller="dashboard" data-initializer="dashboard-view">

    <script id="config:dashboard-view" type="application/json">
        {
            "layout" : ${emm:toJson(layout)}
        }
    </script>

    <%-- loads by JS --%>
</div>

<%@ include file="fragments/tiles/add-ons-tile.jspf" %>
<%@ include file="fragments/tiles/statistics-tile.jspf" %>
<%@ include file="fragments/tiles/planning-tile.jspf" %>
<%@ include file="fragments/tiles/news-tile.jspf" %>
<%@ include file="fragments/tiles/imports-exports-tile.jspf" %>
<%@ include file="fragments/tiles/mailings-tile.jspf" %>
<%@ include file="fragments/tiles/workflows-tile.jspf" %>
<%@ include file="fragments/tiles/empty-tile.jspf" %>
<jsp:include page="fragments/tiles/calendar-tile.jsp" />

<script id="dashboard-tiles-selection-modal" type="text/x-mustache-template">
    <div id="choose-tile-modal" class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <mvc:message code="dashboard.tile.add"/>
                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body">
                    <div class="row row-cols-4">
                        {{ _.each(tiles, function(tile) { }}
                            <div class="col" data-field="toggle-vis">
                                {{ _.each(tile.variants, function(variant) { }}
                                    {{var checkboxControl = tile.variants.length > 1 ? 'data-' + (variant.type === 'tall' ? 'show' : 'hide') + '-by-checkbox=#switch-' + tile.id + '-tile' : '';}}
                                    <div class="tile-thumbnail border rounded {{- variant.disabled ? 'disabled' : '' }}"
                                         data-action="add-tile"
                                         data-type="{{- variant.type }}"
                                         data-tile-id="{{- tile.id }}"
                                         {{- checkboxControl }}
                                         data-bs-dismiss="modal">
                                        <img class="img-fluid" src="{{- tile.thumbnail(variant.type) }}" alt="">
                                        <div class="overlay rounded">
                                            {{ if (variant.disabled) { }}
                                                <mvc:message code="error.dashboard.tile.space"/>
                                            {{ } else { }}
                                                <i class="icon icon-plus"></i>
                                            {{ } }}
                                        </div>
                                    </div>
                                {{ }) }}
                                {{ if (tile.variants.length > 1) { }}
                                    <div class="tile-name d-flex justify-content-between">
                                        <b>{{- tile.name }}</b>
                                        <input type="checkbox" id="switch-{{- tile.id }}-tile" class="icon-switch">
                                        <label for="switch-{{- tile.id }}-tile" class="icon-switch__label">
                                            <i class="colon-icon"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                            <i class="colon-icon" style="transform: rotate(90deg)"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                        </label>
                                    </div>
                                    {{ } else { }}
                                        <div class="tile-name">
                                            <b>{{- tile.name }}</b>
                                            {{ const variant = tile.variants[0].type; }}
                                            {{ if (variant !== 'regular') { }}
                                                <i class="colon-icon wide-tile-icon" style="transform: rotate({{- variant === 'tall' ? '90deg' : '0'}})"><span class="colon-icon__dot"></span><span class="colon-icon__dot"></span></i>
                                            {{ } else { }}
                                                <i class="icon icon-square regular-tile-icon"></i>
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
        <div class="modal-dialog modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <mvc:message code="dashboard.layout.select"/>
                    <button type="button" class="btn-close shadow-none" data-bs-dismiss="modal">
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

<script id="dashboard-schedule-day" type="text/x-mustache-template">
    <div class="schedule__day grid" style="--bs-columns: 1;">
        <span class="schedule__day-date text-truncate">{{- dayOfWeek }}, {{- dateStr }}</span>
        <div class="schedule__day-mailings grid">
            {{ if (_.isEmpty(dayMailings)) { }}
                <div class="notification-simple">
                    <i class="icon icon-info-circle"></i>
                    <span><mvc:message code='dashboard.mailing.noMailings'/></span>
                </div>
            {{ } else { }}
                {{ _.each(dayMailings, function(mailing) { }}
                    <a class="schedule__day-mailing overflow-hidden" href="{{- mailing.link }}">
                        <span class="schedule__send-time">{{- mailing.sendTime }}</span>
                        <div class="schedule__mailing-info">
                            <div class="schedule__mailing-name">
                                <span class="mailing-badge {{- mailing.workstatus }}" data-tooltip="{{- mailing.workstatusIn }}"></span>
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
