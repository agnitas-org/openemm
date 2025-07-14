<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.calendar.web.CalendarController" %>
<%@ page import="com.agnitas.emm.core.push.bean.PushNotificationStatus" %>
<%@ page import="com.agnitas.util.AgnUtils" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="dashboardForm" type="com.agnitas.emm.core.dashboard.form.DashboardForm"--%>
<%--@elvariable id="dashboardCalendarForm" type="com.agnitas.emm.core.calendar.form.DashboardCalendarForm"--%>
<%--@elvariable id="mailinglist" type="com.agnitas.beans.impl.PaginatedListImpl<java.util.Map<java.lang.String, java.lang.Object>"--%>
<%--@elvariable id="randomInactiveFeaturePackage" type="com.agnitas.emm.premium.bean.FeaturePackage"--%>
<%--@elvariable id="layout" type="java.lang.String"--%>
<%--@elvariable id="showALlCalendarEntries" type="java.lang.Boolean"--%>
<%--@elvariable id="companyAdmins" type="java.util.Map<java.lang.String, java.lang.String>"--%>
<%--@elvariable id="unplannedMailings" type="java.util.List<com.agnitas.emm.core.calendar.beans.CalendarUnsentMailing>"--%>
<%--@elvariable id="plannedMailings" type="java.util.Map<com.agnitas.emm.core.calendar.beans.CalendarUnsentMailing>"--%>

<c:set var="MONTH_LIST" value="<%= AgnUtils.getMonthList() %>"/>
<c:set var="YEAR_LIST" value="<%= AgnUtils.getCalendarYearList(CalendarController.SELECTOR_START_YEAR_NUM, 5) %>"/>

<c:set var="adminId" value="${sessionScope[AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN].adminID}"/>
<c:set var="firstDayOfWeek" value="1"/> <%--monday--%>

<c:set var="statisticsViewAllowed" value="${false}"/>
<emm:ShowByPermission token="stats.mailing">
    <c:set var="statisticsViewAllowed" value="${true}"/>
</emm:ShowByPermission>

<div class="tiles-container" data-controller="dashboard-calendar" data-initializer="dashboard-calendar">

    <script id="config:dashboard-calendar" type="application/json">
        {
            "firstDayOfWeek": ${firstDayOfWeek},
            "showALlMailingsPerDay": ${showALlCalendarEntries},
            "statisticsViewAllowed": ${statisticsViewAllowed},
            "pushStatusSent": "${PushNotificationStatus.SENT.name()}",
            "pushStatusScheduled": "${PushNotificationStatus.SCHEDULED.name()}",
            "unplannedMailings": ${emm:toJson(unplannedMailings)},
            "plannedMailings": ${emm:toJson(plannedMailings)},
            "minYear": ${CalendarController.SELECTOR_START_YEAR_NUM}
        }
    </script>

    <div id="dashboard-calendar" class="tile">
        <div class="tile-header px-3 gap-3">
            <div id="period-picker" class="dropdown dropdown--period-picker">
                <button class="btn dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                    <h1 class="tile-title"><span class="text-truncate"></span></h1>
                </button>

                <div class="dropdown-menu tile--period-picker">
                    <div class="tile-header">
                        <div class="tile-title">
                            <button type="button" class="btn period-picker__title-btn"></button>
                        </div>
                        <div class="tile-title-controls">
                            <i class="icon icon-caret-down"></i>
                            <i class="icon icon-caret-up"></i>
                        </div>
                    </div>
                    <div class="tile-body"><%-- content added with js DashboardCalendarPeriodPicker --%>
                        <div class="period-picker__years"></div>
                        <div class="period-picker__months"></div>
                        <div class="period-picker__weeks"></div>
                    </div>
                </div>
            </div>

            <div class="tile-title-controls gap-3">
                <label class="switch" style="display: none"><%--initially hidden to hide the slider movements which are done with js--%>
                    <input data-action="switch-mode" type="checkbox">
                    <span><i class="icon icon-calendar-alt"></i><mvc:message code='Month'/></span>
                    <span><i class="icon icon-calendar-week"></i><mvc:message code='Week'/></span>
                </label>

                <a href="#" class="btn btn-primary btn-sm-horizontal" data-action="jump-to-today">
                    <i class="icon icon-calendar-day"></i>
                    <mvc:message code="calendar.today.button"/>
                </a>
            </div>

            <div class="tile-controls">
                <div class="dropdown">
                    <button class="btn btn-secondary btn-sm-horizontal dropdown-toggle" type="button" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false">
                        <i class="icon icon-wrench"></i>
                        <mvc:message code="Settings"/>
                    </button>

                    <ul class="dropdown-menu p-2">
                        <li>
                            <div class="form-check form-switch">
                                <input id="toggle-calendar-comments" type="checkbox" class="form-check-input" role="switch" data-action="toggle-comments"/>
                                <label class="form-label form-check-label" for="toggle-calendar-comments"><mvc:message code="calendar.ShowHideComments"/></label>
                            </div>
                        </li>
                        <li>
                            <mvc:form id="unsent-mailings-form" servletRelativeAction="/calendar/unsent-mailings.action" method="GET" modelAttribute="dashboardCalendarForm"
                                      data-form="resource"
                                      data-resource-selector="#dashboard-calendar-unsent-wrapper">

                                <script type="application/json" data-initializer="web-storage-persist">
                                    {
                                      "dashboard-calendar": {
                                        "showUnsentList": "${dashboardCalendarForm.showUnsentList}",
                                        "showUnsentPlanned": "${dashboardCalendarForm.showUnsentPlanned}"
                                      }
                                    }
                                </script>
                                <div class="form-check form-switch mt-2">
                                    <mvc:checkbox id="toggle-unsent-list" path="showUnsentList" cssClass="form-check-input" role="switch" data-form-submit=""/>
                                    <label class="form-label form-check-label" for="toggle-unsent-list">Toggle unsent list</label>
                                </div>
                            </mvc:form>
                        </li>
                        <li>
                            <div class="form-check form-switch mt-2">
                                <input id="toggle-calendar-scroll" type="checkbox" class="form-check-input" role="switch" data-action="toggle-scroll"/>
                                <label class="form-label form-check-label" for="toggle-calendar-scroll">Toggle scroll</label>
                            </div>
                        </li>
                    </ul>
                </div>
                <div class="search-container">
                    <input type="text" class="form-control" data-action="search" placeholder="Search">
                </div>
            </div>
        </div>

        <div class="tile-body">
            <div id="dashboard-calendar-table">
                <div id="dashboard-calendar-weekdays">
                    <c:forEach var="dow" begin="${1 + firstDayOfWeek}" end="7" step="1">
                        <div><mvc:message code="calendar.dayOfWeek.${dow}"/></div>
                    </c:forEach>
                    <c:forEach var="dow" begin="1" end="${firstDayOfWeek}" step="1">
                        <div><mvc:message code="calendar.dayOfWeek.${dow}"/></div>
                    </c:forEach>
                </div>
                <div id="dashboard-calendar-grid-wrapper" class="js-scrollable"><%--helps provide 'tile scroll' mode--%>
                    <div id="dashboard-calendar-grid"><%--js--%></div>
                </div>
            </div>

            <%@ include file="dashboard-calendar-unsent-list.jsp" %>
        </div>
    </div>
</div>

<script id="calendar-comment-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <mvc:form id="calendar-comment-form" servletRelativeAction="/calendar/saveComment.action" cssClass="modal-content" data-validator="calendar-comment" data-initializer="dashboard-calendar-comment-modal">
                <input type="hidden" name="commentId" value="{{- commentId }}"/>
                <input type="hidden" name="date" value="{{- date }}"/>

                <div class="modal-header">
                    <h1 class="modal-title">
                        {{- t(commentId > 0 ? 'calendar.common.edit_comment' : 'calendar.common.new_comment') }}
                    </h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="modal-body form-column">
                    <div data-field="validator">
                        <label class="form-label" for="comment-text"><mvc:message code="calendar.Comment"/></label>
                        <textarea rows="1" class="form-control" id="comment-text" name="comment" maxlength="1000">{{- comment }}</textarea>
                    </div>

                    <div class="form-check form-switch" data-field="toggle-vis" data-field-vis-scope="#calendar-comment-form">
                        <input id="scheduleReminder" type="checkbox" {{- sendReminder ? 'checked' : ''}} class="form-check-input" role="switch"
                        data-field-vis="" data-field-vis-show="#reminderDetails" value="{{- deadline }}" />
                        <div class="hidden" data-field-vis-default="" data-field-vis-hide="#reminderDetails"></div>
                        <label class="form-label form-check-label" for="scheduleReminder"><mvc:message code="calendar.reminder.send"/></label>
                    </div>

                    <div id="reminderDetails" class="form-column" data-field="toggle-vis">
                        <select name="isCustomRecipients" class="form-control js-select" data-field-vis="">
                            <option value="false" data-field-vis-hide="#custom-recipients" data-field-vis-show="#notify-users" {{- isCustomRecipients ? '' : 'selected' }}><mvc:message code="workflow.start.emmUsers"/></option>
                            <option value="true" data-field-vis-hide="#notify-users" data-field-vis-show="#custom-recipients" {{- isCustomRecipients ? 'selected' : '' }}><mvc:message code="workflow.start.customRecipients"/></option>
                        </select>

                        <div id="notify-users">
                            {{ var notifyAdminId = notifyAdminId ? notifyAdminId : ${adminId}; }}
                            <select name="notifyAdminId" class="form-control js-select">
                                <c:forEach var="adminEntry" items="${companyAdmins}">
                                    <option value="${adminEntry.key}" {{- ${adminEntry.key} == notifyAdminId ? 'selected' : '' }}>${adminEntry.value}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div id="custom-recipients">
                            <select name="recipients" class="form-control dynamic-tags" multiple="" data-placeholder="<mvc:message code='enterEmailAddresses'/>">
                                {{ _.each(recipients, function(recipient) { }}
                                <option value="{{- recipient }}" selected>{{- recipient }}</option>
                                {{ }); }}
                            </select>
                        </div>

                        <div class="form-check form-switch">
                            <input id="isSendNow" type="checkbox" name="isSendNow" class="form-check-input" role="switch"/>
                            <label class="form-label form-check-label" for="isSendNow"><mvc:message code="calendar.sendRemiderNow"/></label>
                        </div>

                        <div class="form-check form-switch">
                            <input id="isDeadline" type="checkbox" name="deadline" {{- deadline ? 'checked' : '' }} class="form-check-input" role="switch"
                            data-field-vis="" data-field-vis-show="#reminder-send-time"/>
                            <div class="hidden" data-field-vis-default="" data-field-vis-hide="#reminder-send-time"></div>
                            <label class="form-label form-check-label" for="isDeadline"><mvc:message code="workflow.start.scheduleReminder"/></label>
                        </div>
                    </div>

                    <div data-show-by-checkbox="#scheduleReminder">
                        <div id="reminder-send-time" data-show-by-checkbox="#isDeadline">
                            <label class="form-label"><mvc:message code="mailing.SendingTime"/></label>
                            <div id="remind-date" data-field="datetime" data-property="plannedSendDate"
                                 data-field-options="value: '{{- plannedSendDate }}', timeMask:'h:00'"
                                 data-datepicker-options="minDate: 0">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    {{ if (commentId > 0) { }}
                    <button type="button" class="btn btn-danger" data-comment-id="{{- commentId }}" data-action="delete-comment" data-bs-dismiss="modal">
                        <i class="icon icon-trash-alt"></i>
                        <mvc:message code="Delete"/>
                    </button>
                    {{ } }}
                    <button type="button" class="btn btn-primary" data-comment-id="{{- commentId }}" data-action="save-comment">
                        <i class="icon icon-save"></i>
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </mvc:form>
        </div>
    </div>
</script>

<script id="dashboard-calendar-mailing-label" type="text/x-mustache-template">
    <a href="{{- link }}" id="{{- labelId }}" class="dashboard-calendar__label dashboard-calendar__label--{{- status }}">
        <span class="status-badge" data-tooltip="{{- t(status) }}"></span>
        <span><span class="text-truncate-lines" style="--text-truncate-lines: 2;">{{- shortname }}</span></span>
        {{ if (mediatype && mediatype !== 'mailing.mediatype.email') { }}
            <span class="status-badge {{- mediatype }}" data-tooltip="{{- t(mediatype) }}"></span>
        {{ } }}
    </a>
</script>

<script id="dashboard-calendar-auto-opt-label" type="text/x-mustache-template">
    <a href="{{- link }}" id="{{- labelId }}" class="dashboard-calendar__label dashboard-calendar__label--{{- status }}">
        <span class="status-badge" data-tooltip="{{- t(tooltip) }}"></span>
        <span><span class="text-truncate-lines" style="--text-truncate-lines: 2;">{{- shortname }}</span></span>
        <span class="status-badge status.auto-opt"></span>
    </a>
</script>

<%@include file="fragments/dashboard-calendar-push-label.jspf"%>

<script id="dashboard-calendar-comment-label" type="text/x-mustache-template">
    <a href="#" id="{{- labelId }}" class="dashboard-calendar__label dashboard-calendar__label--comment" data-action="show-comment">
        <span class="status-badge status.comment" data-tooltip="<mvc:message code='calendar.Comment'/>"></span>
        <span><span class="text-truncate-lines" style="--text-truncate-lines: 2;">{{- comment }}</span></span>
    </a>
</script>

<script id="dashboard-calendar-show-more-btn" type="text/x-mustache-template">
    <a href="#" class="btn btn-primary" data-action="show-more-mailings">
        <i class="icon icon-plus"></i>
        <mvc:message code="default.more"/>
    </a>
</script>

<script id="dashboard-calendar-day" type="text/x-mustache-template">
    <div class="dashboard-calendar-cell"><%-- the wrapper is needed because week numbers are added to Mondays and .overflow-hidden prevents them from being displayed--%>
        <div data-date="{{- date }}" class="dashboard-calendar-day overflow-hidden">
            <div class="dashboard-calendar-day__header">
                {{- day }}
                <button type="button" class="icon-btn"
                        data-date="{{- date }}"
                        data-action="create-comment"
                        data-tooltip="<mvc:message code='calendar.NewComment'/>">
                    <i class="icon icon-comment-medical"></i>
                </button>
            </div>
            <div class="dashboard-calendar-day__body"></div>
        </div>
    </div>
</script>

<%@ include file="fragments/mailing-popover.jspf"%>

<%@ include file="fragments/news-fragments.jspf"%>
