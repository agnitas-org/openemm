<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ page import="com.agnitas.emm.core.push.bean.PushNotificationStatus" %>
<%@ page import="com.agnitas.emm.core.calendar.web.CalendarController" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="SESSION_CONTEXT_KEYNAME_ADMIN" value="<%= AgnUtils.SESSION_CONTEXT_KEYNAME_ADMIN %>" />

<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="firstDayOfWeek" type="java.lang.Integer"--%>

<%--@elvariable id="admin" type="com.agnitas.beans.Admin"--%>

<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<%--@elvariable id="companyAdmins" type="java.util.Map<java.lang.String, java.lang.String>"--%>

<c:set var="admin" value="${sessionScope[SESSION_CONTEXT_KEYNAME_ADMIN]}"/>

<%--JS dependencies--%>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/calendar/localeDate.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/calendar/weekOfYear.js"></script>

<c:set var="SECONDS_BEFORE_WAIT_MESSAGE" value="<%= CalendarController.SECONDS_BEFORE_WAIT_MESSAGE %>"/>

<c:set var="MONTH_LIST" value="<%= AgnUtils.getMonthList() %>"/>
<c:set var="YEAR_LIST" value="<%= AgnUtils.getCalendarYearList(CalendarController.SELECTOR_START_YEAR_NUM) %>"/>

<c:set var="RECIPIENT_EMM" value="1"/>
<c:set var="RECIPIENT_CUSTOM" value="2"/>

<c:set var="PUSH_STATUS_SENT" value="<%=PushNotificationStatus.SENT.name()%>"/>
<c:set var="PUSH_STATUS_SCHEDULED" value="<%=PushNotificationStatus.SCHEDULED.name()%>"/>

<fmt:formatDate var="currentServerTime" value="${emm:now()}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}"/>

<c:set var="isStatisticsViewPermitted" value="${false}"/>

<emm:ShowByPermission token="stats.mailing">
    <c:set var="isStatisticsViewPermitted" value="${true}"/>
</emm:ShowByPermission>

<div class="calendar" data-initializer="calendar-table">
    <div class="tile">
        <c:url var="mailingLink" value="/mailing/{mailingId}/settings.action"/>

        <c:url var="mailingStatisticsViewUrl" value="/statistics/mailing/{mailingId}/view.action">
            <c:param name="init" value="true"/>
        </c:url>

        <script id="config:calendar-table" type="application/json">
            {
                "localeDatePattern": "${localeDatePattern}",
                "currentServerTime": "${currentServerTime}",
                "firstDayOfWeek": ${firstDayOfWeek},
                "adminId": ${admin.adminID},
                "adminName": "${admin.username}",
                "recipientEmm":  ${RECIPIENT_EMM},
                "recipientCustom": ${RECIPIENT_CUSTOM},
                "pushStatusSent": "${PUSH_STATUS_SENT}",
                "pushStatusScheduled": "${PUSH_STATUS_SCHEDULED}",
                "secondsBeforeWaitMessage": ${SECONDS_BEFORE_WAIT_MESSAGE},
                "isStatisticsViewPermitted": ${isStatisticsViewPermitted},
                "urls": {
                    "CALENDAR_UNSENT_MAILINGS": "<c:url value="/calendar/getUnsentMailings.action"/>",
                    "CALENDAR_PLANNED_MAILINGS": "<c:url value="/calendar/getPlannedMailings.action"/>",
                    "CALENDAR_MAILINGS_LIST": "<c:url value="/calendar/mailings.action"/>",
                    "CALENDAR_MAILINGS_MOVE": "<c:url value="/calendar/moveMailing.action"/>",
                    "CALENDAR_PUSHES_LIST": "<c:url value="/calendar/pushes.action"/>",
                    "CALENDAR_PUSHES_MOVE": "<c:url value="/calendar/movePushNotification.action"/>",
                    "CALENDAR_AUTO_OPTIMIZATION": "<c:url value="/calendar/autoOptimization.action"/>",
                    "CALENDAR_COMMENT_SAVE": "<c:url value="/calendar/saveComment.action"/>",
                    "CALENDAR_COMMENT_REMOVE": "<c:url value="/calendar/removeComment.action"/>",
                    "CALENDAR_COMMENT_LIST": "<c:url value="/calendar/comments.action"/>",
                    "MAILING_VIEW": "${mailingLink}",
                    "MAILING_STATISTICS_VIEW": "${mailingStatisticsViewUrl}"
                }
            }
        </script>

        <div class="tile-header">
            <h2 class="headline">
                <div id="calendar-wait-notification" style="display: none;">
                    <img class="calendar-wait-spinner" src="<c:url value="/assets/core/images/facelift//loading.gif"/>" alt="loading..." />
                    <span class="calendar-wait-message"><mvc:message code="calendar.loading"/></span>
                </div>
                <div id="calendar-days"></div>
            </h2>
            <ul class="tile-header-nav">
                <li id="linkSelectByMonth" class="active">
                    <a href="#" class="js-calendar-view-month">
                        <mvc:message code="calendar.month.view"/>
                    </a>
                </li>
                <li id="linkSelectByWeek">
                    <a href="#" class="js-calendar-view-week">
                        <mvc:message code="calendar.week.view"/>
                    </a>
                </li>
            </ul>

            <ul class="tile-header-actions">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <i class="icon icon-pencil"></i>
                        <span class="text"><mvc:message code="button.Show"/></span>
                        <i class="icon icon-caret-down"></i>
                    </a>

                    <ul class="dropdown-menu">
                        <li class="dropdown-header">
                            <mvc:message code="default.View"/>
                        </li>
                        <li>
                            <label class="label js-dropdown-close">
                                <input type="checkbox" class="js-calendar-toggle-all-comments"/>
                                <span class="label-text"><mvc:message code="calendar.ShowAllComments"/></span>
                            </label>
                        </li>
                        <li class="dropdown-header">
                            <mvc:message code="default.sidebar"/>
                        </li>
                        <li>
                            <a href="#" class="js-calendar-list-unsent">
                                <mvc:message code="calendar.unscheduledMailings"/>
                            </a>
                        </li>
                        <li>
                            <a href="#" class="js-calendar-list-planned">
                                <mvc:message code="calendar.planned.mailings"/>
                            </a>
                        </li>
                    </ul>

                </li>
            </ul>

        </div>

        <div class="tile-content">
            <div class="calendar-controls">
                <ul class="pagination">
                    <li>
                        <a href="#" class="js-calendar-navigate-previous">
                            <i class="icon icon-angle-left"></i>
                        </a>
                    </li>
                    <li>
                        <a href="#" class="js-calendar-navigate-today">
                            <mvc:message code="calendar.today.button"/>
                        </a>
                    </li>
                    <li>
                        <a href="#" class="js-calendar-navigate-next">
                            <i class="icon icon-angle-right"></i>
                        </a>
                    </li>
                </ul>

                <ul class="calendar-date-select col-sm-6">
                    <li id="dateSelectByMonth" style='display:inline;'>
                        <div class="row">
                            <div class="col-sm-6">
                                <select id="month_list" size="1" class="form-control">
                                    <c:forEach var="mon" items="${MONTH_LIST}">
                                        <option value="${mon[0]}"><mvc:message code="${mon[1]}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div class="col-sm-6">
                                <select id="month_list_year" size="1" class="form-control">
                                    <c:forEach var="yea" items="${YEAR_LIST}">
                                        <option value="${yea}"><c:out value="${yea}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </li>
                    <li id="dateSelectByWeek" style='display:none;'>
                        <div class="row">
                            <div class="col-sm-6">
                                <select id="weeks_list" size="1" class="form-control">
                                </select>
                            </div>
                            <div class="col-sm-6">
                                <select id="weeks_list_year" size="1" class="form-control">
                                    <c:forEach var="yea" items="${YEAR_LIST}">
                                        <option value="${yea}"><c:out value="${yea}"/></option>
                                    </c:forEach>
                                </select>
                            </div>
                        </div>
                    </li>
                </ul>

            </div>
            <div id="calendar-wrapper" class="calendar-wrapper calendar-with-sidebar">
                <div>
                    <table id="calendar-table" cellpadding="0" cellspacing="0" class="calendar-table">
                        <thead>
                        <tr>
                            <th><mvc:message code="calendar.WeekNumber"/></th>
                            <c:forEach var="dow" begin="${1 + firstDayOfWeek}" end="7" step="1">
                                <th><mvc:message code="calendar.dayOfWeek.${dow}"/></th>
                            </c:forEach>
                            <c:forEach var="dow" begin="1" end="${firstDayOfWeek}" step="1">
                                <th><mvc:message code="calendar.dayOfWeek.${dow}"/></th>
                            </c:forEach>
                        </tr>
                        </thead>
                        <tbody id="calendar-container"></tbody>
                    </table>
                </div>

                <div>
                    <div class="calendar-sidebar js-scrollable">
                        <div class="calendar-sidebar-content"></div>
                    </div>
                </div>

            </div>
            <div id="calendar-popup-holder"></div>

            <script id="calendar-dialog-modal-template" type="text/html">
                <div id="calendar-dialog-modal" class="editor-admin-reminder-row modal modal-wide">
                    <div class="modal-dialog">
                        <div class="modal-content">
                            <div class="modal-header">
                                <button type="button" class="close-icon close" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only">
                                    <mvc:message code="button.Cancel"/>
                                </span></button>
                                <h4 class="modal-title">
                                    <mvc:message code="calendar.Comment"/><br>
                                </h4>
                            </div>

                            <div class="modal-body">
                                <div class="form-group">
                                    <div class="col-sm-8 col-sm-push-4">
                                        <p id="calendar-comment-error" class="calendar-comment-error" style="color: #cc071d;"></p>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <div class="col-sm-4">
                                        <label class="control-label" for="comment-text">
                                            <mvc:message code="calendar.Comment"/>
                                        </label>
                                    </div>
                                    <div class="col-sm-8">
                                        <textarea rows="6" class="calendar-textarea form-control" id="comment-text" maxlength="1000" style="resize: none;"></textarea>
                                    </div>

                                </div>

                                <div class="form-group">
                                    <div class="col-sm-8 col-sm-push-4">
                                        <label class="checkbox-inline">
                                            <input type="checkbox" name="scheduleReminder" id="scheduleReminder" value="true"/>
                                            <mvc:message code="calendar.Notify"/>
                                        </label>
                                    </div>
                                </div>



                                <div id="reminderDetails" class="reminder-container" data-field="toggle-vis">
                                    <div class="form-group">
                                        <div class="col-sm-8 col-sm-push-4">
                                            <label class="radio-inline">
                                                <input type="radio" value="${RECIPIENT_EMM}" id="isEmmUserType" name="userType" checked="checked"
                                                       data-field-vis="" data-field-vis-hide="#customRecipients" data-field-vis-show="#emmUsers"/>
                                                <mvc:message code="workflow.start.emmUsers"/>
                                            </label>
                                            <label class="radio-inline">
                                                <input type="radio" value="${RECIPIENT_CUSTOM}" name="userType"
                                                       data-field-vis="" data-field-vis-hide="#emmUsers" data-field-vis-show="#customRecipients"/>
                                                <mvc:message code="workflow.start.customRecipients"/>
                                            </label>
                                        </div>
                                    </div>

                                    <div class="form-group" id="emmUsers">
                                        <div class="col-sm-8 col-sm-push-4">
                                            <select name="adminForNotify" id="admin-for-notify" class="form-control js-select">
                                                <c:forEach var="adminEntry" items="${companyAdmins}">
                                                    <option value="${adminEntry.key}">${adminEntry.value}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="form-group" id="customRecipients">
                                        <div class="col-sm-8 col-sm-push-4">
                                            <input type="hidden" name="adminTimezone" value="${param.adminTimezone}"/>
                                            <mvc:message var="enterEmailAddresses" code="enterEmailAddresses"/>
                                            <textarea name="recipients" id="recipients-text" cols="32" rows="3" class="form-control" placeholder="${enterEmailAddresses}" style="resize: none;"></textarea>
                                        </div>
                                    </div>


                                    <div class="modal-separator"></div>

                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label class="control-label"><mvc:message code="calendar.reminder.send"/></label>
                                        </div>
                                        <div class="col-sm-8">
                                            <ul class="list-group">
                                                <li class="list-group-item">
                                                    <label class="checkbox-inline">
                                                        <input type="checkbox" name="isSendNow" id="isSendNow"/>
                                                        <mvc:message code="calendar.sendRemiderNow"/>
                                                    </label>
                                                </li>
                                                <li class="list-group-item">
                                                    <label class="checkbox-inline">
                                                        <input type="checkbox" id="isDeadline" name="isDeadline" value="true" />
                                                        <mvc:message code="workflow.start.scheduleReminder"/>
                                                    </label>
                                                </li>
                                            </ul>
                                        </div>
                                    </div>
                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label class="control-label"><mvc:message code="mailing.SendingTime"/></label>
                                        </div>
                                        <div class="col-sm-5">
                                            <div class="input-group">
                                                <div class="input-group-controls">
                                                    <input type="text" id="remindDate" class="form-control datepicker-input js-datepicker" />
                                                </div>

                                                <div class="input-group-btn">
                                                    <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                        <i class="icon icon-calendar-o"></i>
                                                    </button>
                                                </div>

                                            </div>
                                        </div>
                                        <div class="col-sm-3">
                                            <div class="input-group">
                                                <div class="input-group-controls">
                                                    <input type="text" id="remindTime" class="form-control js-timepicker" data-timepicker-options="mask: 'h:00'" />
                                                </div>

                                                <div class="input-group-addon">
                                                    <span class="addon">
                                                        <i class="icon icon-clock-o"></i>
                                                    </span>
                                                </div>
                                            </div>
                                            <p class="help-block"><mvc:message code="default.interval"/>: <mvc:message code="default.minutes.60"/></p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-regular" data-dismiss="modal">
                                        <i class="icon icon-times"></i>
                                        <span><mvc:message code="button.Cancel"/></span>
                                    </button>
                                    <button type="button" id="comment-save-button" class="btn btn-primary btn-regular">
                                        <i class="icon icon-check"></i>
                                        <span id="comment-save-button-message"><mvc:message code="button.Save"/></span>
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </script>

        </div>
    </div>

    <script id="calendar-planned-push-notification" type="text/x-mustache-template">
        <div class="calendar-mail-label ui-draggable" data-push-id="{{- id }}" id="push-badge-{{- id }}" data-status="{{- status }}" data-tooltip="{{- title }}">
            <a href="<c:url value="/push/{{- id }}/view.action"/>">
                <span class="calendar-badge push.status.{{- status.toLowerCase() }}">{{- name }}</span>
            </a>
        </div>
    </script>

    <script id="calendar-auto-optimization" type="text/x-mustache-template">
        <div class="calendar-mail-label" optimization-id="{{= optimizationId }}" data-status="{{= status }}" data-tooltip="{{= shortname }}">
            <a href="<c:url value='/{{- linkUrl }}'/>">
                <span class="calendar-badge calendar-optimization-{{= status.toLowerCase().replace('_', '') }}">{{= shortname }}</span>
            </a>
        </div>
    </script>

    <script id="calendar-mail-link" type="text/x-mustache-template">
        <div class="calendar-mail-label {{- sent ? 'calendar-sent-mail' : 'calendar-scheduled-mail'}} ui-draggable" data-action="mailing-popup"
             id="{{- mailingId }}" data-mailing-id="{{- mailingId }}" data-time="{{- sendTime }}" sent="{{- sent }}" planned="{{- planned }}" workstatus="{{- workstatus }}">
            <a class="calendar-name-link {{- statusClass }}" href="{{- mailingLink }}">
                <div id="shortname-{{- mailingId }}">
                        <span class="calendar-badge {{- workstatus }}">
                            {{ if (sent === true || planned === true) { }}
                                {{- shortname }}
                            {{ } else { }}
                                <b class="calendar-send-time-label">({{= sendTime }})</b> {{- shortname }}
                            {{ } }}
                        </span>
                </div>
            </a>
        </div>
    </script>

    <script id="calendar-mail-popup" type="text/x-mustache-template">
        <div id="popup-{{- mailingId }}" class="calendar-mail-popup" style="display: none;">
            <div class="arrow"></div>
            <div class="calendar-mail-popup-header">
                <i class="icon {{- workstatus }}"></i> {{- shortname }}
            </div>
            <div class="calendar-mail-popup-preview {{- popupClass }}">
                <img src="{{= previewImage }}" alt="" border="0"/>
            </div>
            <div class="calendar-mail-popup-content">
                <p><b><mvc:message code="mailing.Subject"/></b></p>
                <p>{{= subject }}</p>
                <p><b><mvc:message code="Status"/></b></p>
                {{ if (emptyWorkstatus === false) { }}
                <p>{{- workstatusIn }}</p>
                {{ } }}

                {{ if (sent === true) { }}
                <p><b><mvc:message code="report.numberRecipients"/></b></p>
                <p>{{- mailsSent }}</p>
                <p><b><mvc:message code="calendar.Openers"/></b></p>
                <p>{{- openers }}</p>
                <p><b><mvc:message code="statistic.clicker"/></b></p>
                <p>{{- clickers }}</p>
                {{ } }}
            </div>
        </div>
    </script>
</div>
