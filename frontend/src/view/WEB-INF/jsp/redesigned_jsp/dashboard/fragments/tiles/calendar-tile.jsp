<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.calendar.web.CalendarController" %>
<%@ page import="org.agnitas.util.AgnUtils" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="firstDayOfWeek" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<emm:ShowByPermission token="calendar.show">
    <c:set var="MONTH_LIST" value="<%= AgnUtils.getMonthList() %>"/>
    <c:set var="YEAR_LIST" value="<%= AgnUtils.getCalendarYearList(CalendarController.SELECTOR_START_YEAR_NUM, 5) %>"/>

    <fmt:formatDate var="currentServerTime" value="${emm:now()}" pattern="${adminDateFormat}" timeZone="${adminTimeZone}"/>

    <c:set var="statisticsViewAllowed" value="${false}"/>
    <emm:ShowByPermission token="stats.mailing">
        <c:set var="statisticsViewAllowed" value="${true}"/>
    </emm:ShowByPermission>

    <script id="config:dashboard-calendar" type="application/json">
        {
            "firstDayOfWeek": ${firstDayOfWeek},
            "statisticsViewAllowed": ${statisticsViewAllowed}
        }
    </script>

    <script id="dashboard-tile-calendar" type="text/x-mustache-template">
        <div id="calendar-tile" class="tile draggable-tile tile-{{- tileType }}" data-initializer="dashboard-calendar">
            <div class="tile-header">
                <h1 class="tile-title">{{- tileName }}</h1>
            </div>
            <div class="tile-body">
                <table id="calendar-table" class="table table-borderless align-middle">
                    <tbody id="calendar-container">
                        <tr id="calendar-header">
                            <td><div class="calendar-cell"><mvc:message code="calendar.WeekNumber"/></div></td>
                        </tr>
                        <%-- else days load by JS--%>
                    </tbody>
                </table>
                <div id="calendar-tile-date-dropdowns">
                    <select id="month_list" size="1" class="form-control has-arrows" data-select-options="minimumResultsForSearch: -1">
                        <c:forEach var="month" items="${MONTH_LIST}">
                            <option value="${month[0]}"><mvc:message code="${month[1]}"/></option>
                        </c:forEach>
                    </select>
                    <select id="month_list_year" size="1" class="form-control has-arrows" data-select-options="minimumResultsForSearch: -1">
                        <c:forEach var="year" items="${YEAR_LIST}">
                            <option value="${year}"><c:out value="${year}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div id="calendar-tile-day-mailings" class="bordered-box">
                    <%-- load by JS--%>
                </div>
            </div>
            {{= overlay }}
        </div>
    </script>
</emm:ShowByPermission>