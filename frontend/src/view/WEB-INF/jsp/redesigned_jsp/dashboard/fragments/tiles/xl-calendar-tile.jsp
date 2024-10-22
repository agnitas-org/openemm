<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.emm.core.push.bean.PushNotificationStatus" %>
<%@ page import="com.agnitas.emm.core.calendar.web.CalendarController" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="showALlCalendarEntries" type="java.lang.Boolean"--%>
<%--@elvariable id="companyAdmins" type="java.util.Map<java.lang.String, java.lang.String>"--%>

<c:set var="MONTH_LIST" value="<%= AgnUtils.getMonthList() %>"/>
<c:set var="YEAR_LIST" value="<%= AgnUtils.getCalendarYearList(CalendarController.SELECTOR_START_YEAR_NUM, 5) %>"/>

<c:set var="adminId" value="${sessionScope[AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN].adminID}"/>
<c:set var="firstDayOfWeek" value="1"/> <%--monday--%>

<c:set var="statisticsViewAllowed" value="${false}"/>
<emm:ShowByPermission token="stats.mailing">
    <c:set var="statisticsViewAllowed" value="${true}"/>
</emm:ShowByPermission>

<script id="config:dashboard-xl-calendar" type="application/json">
    {
        "firstDayOfWeek": ${firstDayOfWeek},
        "showALlMailingsPerDay": ${showALlCalendarEntries},
        "statisticsViewAllowed": ${statisticsViewAllowed},
        "pushStatusSent": "${PushNotificationStatus.SENT.name()}",
        "pushStatusScheduled": "${PushNotificationStatus.SCHEDULED.name()}"
    }
</script>

<script id="dashboard-tile-xl-calendar" type="text/x-mustache-template">
    <div id="xl-calendar-tile" class="tile draggable-tile tile-{{- tileSize }}" data-initializer="dashboard-xl-calendar">
        <div class="tile-header gap-1">
            <h1 class="tile-title d-flex gap-2">
                <span class="text-truncate"><mvc:message code="calendar.Calendar"/></span>
                <label class="icon-switch me-1">
                    <input id="xl-calendar-mode-switch" data-action="toggle-xl-calendar-mode" type="checkbox" {{- isWeekMode ? 'checked' : ''}}>
                    <i class="icon icon-calendar-alt" data-tooltip="<mvc:message code='calendar.month.view'/>"></i>
                    <i class="icon icon-calendar-week" data-tooltip="<mvc:message code='calendar.week.view'/>"></i>
                </label>
            </h1>

            <div class="tile-controls table-wrapper__controls">
                <label class="icon-switch" data-tooltip="<mvc:message code='calendar.ShowHideComments'/>">
                    <input id="xl-calendar-comments-switch" data-action="toggle-xl-calendar-comments" type="checkbox" {{- isCommentsShown ? '' : 'checked'}}>
                    <i class="icon icon-comment"></i>
                    <i class="icon icon-comment-slash"></i>
                </label>

                <a href="#" class="btn btn-primary rounded-2" data-action="xl-calendar-today"><mvc:message code="calendar.today.button"/></a>

                <div id="week-select-container" data-show-by-checkbox="#xl-calendar-mode-switch">
                    <select id="xl-calendar-week" size="1" class="form-control has-arrows"
                            data-select-options="minimumResultsForSearch: -1, dropdownAutoWidth: true, width: 'auto'"
                            data-action="flip-xl-calendar">
                        <%--js--%>
                    </select>
                </div>

                <div id="month-select-container" data-hide-by-checkbox="#xl-calendar-mode-switch">
                    <select id="xl-calendar-month" size="1" class="form-control has-arrows" data-select-options="minimumResultsForSearch: -1" data-action="flip-xl-calendar">
                        <c:forEach var="month" items="${MONTH_LIST}">
                            <option value="${month[0]}"><mvc:message code="${month[1]}"/></option>
                        </c:forEach>
                    </select>
                </div>

                <select id="xl-calendar-year" size="1" class="form-control has-arrows" data-select-options="minimumResultsForSearch: -1" data-action="xl-calendar-change-year">
                    <c:forEach var="year" items="${YEAR_LIST}">
                        <option value="${year}"><c:out value="${year}"/></option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <div class="tile-body">
            <div id="xl-calendar">

                {{ if (tileSize != 'x-wide') { }}
                    <div id="xl-calendar-weeks"><%--js--%></div>
                {{ } }}

                <div id="xl-calendar-weekdays">
                    <c:forEach var="dow" begin="${1 + firstDayOfWeek}" end="7" step="1">
                        <div><mvc:message code="calendar.dayOfWeek.${dow}"/></div>
                    </c:forEach>
                    <c:forEach var="dow" begin="1" end="${firstDayOfWeek}" step="1">
                        <div><mvc:message code="calendar.dayOfWeek.${dow}"/></div>
                    </c:forEach>
                </div>
                <div id="xl-calendar-table"><%--js--%></div>
            </div>

            <div id="xl-calendar-unsent" class="tile">
                <div class="tile-header p-1">
                    <div class="btn-group w-100" role="group">
                        <input type="radio" class="btn-check" name="not-sent-mailings-list-type" value="unplanned" id="unplanned-mailings-btn" autocomplete="off" {{- isUnsentMailingsPlannedType ? '' : 'checked'}}>
                        <label class="btn btn-outline-primary text-truncate text-truncate-alt d-inline-block" for="unplanned-mailings-btn"><mvc:message code="calendar.unscheduledMailings"/></label>

                        <input type="radio" class="btn-check" name="not-sent-mailings-list-type" value="planned" id="planned-mailings-btn" autocomplete="off" {{- isUnsentMailingsPlannedType ? 'checked' : ''}}>
                        <label class="btn btn-outline-primary text-truncate text-truncate-alt d-inline-block" for="planned-mailings-btn"><mvc:message code="calendar.planned.mailings"/></label>
                    </div>
                </div>
                <div class="tile-body d-flex flex-column js-scrollable"><%--js--%></div>
            </div>
        </div>
        {{= overlay }}
    </div>
</script>

<script id="calendar-comment-modal" type="text/x-mustache-template">
    <div class="modal">
        <div class="modal-dialog">
            <mvc:form id="calendar-comment-form" servletRelativeAction="/calendar/saveComment.action" cssClass="modal-content" data-validator="calendar-comment" data-initializer="xl-calendar-comment-modal">
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
                                 data-field-options="value: '{{- plannedSendDate }}', submitDateFormat:'dd-MM-yyyy', timeMask:'h:00'">
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    {{ if (commentId > 0) { }}
                        <button type="button" class="btn btn-danger" data-comment-id="{{- commentId }}" data-action="delete-xl-calendar-comment" data-bs-dismiss="modal">
                            <i class="icon icon-trash-alt"></i>
                            <mvc:message code="Delete"/>
                        </button>
                    {{ } }}
                    <button type="button" class="btn btn-primary" data-comment-id="{{- commentId }}" data-action="save-xl-calendar-comment">
                        <i class="icon icon-save"></i>
                        <mvc:message code="button.Save"/>
                    </button>
                </div>
            </mvc:form>
        </div>
    </div>
</script>

<%--keep inline styles in classes in order to prevent draggable styles overlap--%>
<c:set var="calendarLabelStyle">
    display: flex;
    gap: 5px;
</c:set>
<c:set var="calendarLabelTextClasses" value="xl-calendar-label__text text-truncate bg-disabled border rounded-1 px-1 fs-4 flex-grow-1" />

<script id="xl-calendar-mailing-label" type="text/x-mustache-template">
    <a href="{{- link }}" id="{{- labelId }}" class="xl-calendar-label ui-draggable" style="${calendarLabelStyle}">
        <span class="status-badge {{- mediatype }}" data-tooltip="{{- t(mediatype) }}"></span>
        <span class="status-badge {{- status }}" data-tooltip="{{- t(status) }}"></span>
        <span class="${calendarLabelTextClasses}">{{- shortname }}</span>
    </a>
</script>

<script id="xl-calendar-comment-label" type="text/x-mustache-template">
    <a href="#" id="{{- labelId }}" class="xl-calendar-label xl-calendar-label--comment" data-action="show-xl-calendar-comment" style="${calendarLabelStyle}">
        <span class="status-badge status.comment" data-tooltip="<mvc:message code='calendar.Comment'/>"></span>
        <span class="${calendarLabelTextClasses}">{{- comment }}</span>
    </a>
</script>

<script id="xl-calendar-day" type="text/x-mustache-template">
    <div data-date="{{- date }}" class="xl-calendar-day overflow-hidden">
        <div class="xl-calendar-day__header">
            {{- day }}
            <button type="button" class="icon-btn"
                    data-date="{{- date }}"
                    data-action="create-xl-calendar-comment"
                    data-tooltip="<mvc:message code='calendar.NewComment'/>">
                <i class="icon icon-comment-medical"></i>
            </button>
        </div>
        <div class="xl-calendar-day__body js-scrollable"></div>
    </div>
</script>

<script id="xl-calendar-showMore-btn" type="text/x-mustache-template">
    <a href="#" class="xl-calendar-label" style="${calendarLabelStyle}" data-action="load-more-day-mailings">
        <span class='${calendarLabelTextClasses} bg-primary'>
            <i class="icon icon-plus me-1"></i>
            <mvc:message code="default.more"/>
        </span>
    </a>
</script>
