<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ page import="com.agnitas.emm.core.calendar.web.CalendarController" %>
<%@ page import="com.agnitas.util.AgnUtils" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="adminTimeZone" type="java.lang.String"--%>

<emm:ShowByPermission token="calendar.show">
    <c:set var="MONTH_LIST" value="<%= AgnUtils.getMonthList() %>"/>
    <c:set var="YEAR_LIST" value="<%= AgnUtils.getCalendarYearList(CalendarController.SELECTOR_START_YEAR_NUM, 5) %>"/>

    <c:set var="firstDayOfWeek" value="${1}"/> <%-- MONDAY --%>

    <script id="config:dashboard-calendar-tile" type="application/json">
        {
            "firstDayOfWeek": ${firstDayOfWeek},
            "statisticsViewAllowed": ${emm:permissionAllowed('stats.mailing', pageContext.request)}
        }
    </script>

    <script id="dashboard-tile-calendar" type="text/x-mustache-template">
        <div id="calendar-tile" class="tile draggable-tile tile-{{- tileSize }}" data-initializer="dashboard-calendar-tile">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="calendar.Calendar"/></h1>
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
