<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="plannedMails" type="java.util.List"--%>

<div class="calendar-sidebar-header">
    <mvc:message code="calendar.planned.mailings"/>
</div>
<ul class="link-list">
    <c:forEach var="plannedMail" items="${plannedMails}">
        <li>
            <c:url var="mailingBaseUrl" value="/mailing/${plannedMail.mailingid}/settings.action"/>
            <a href="${mailingBaseUrl}" class="link-list-item">
                <p class="headline">
                    <span class="mailing-badge ${plannedMail.workstatus}" data-tooltip="<mvc:message code="${plannedMail.workstatus}"/>"></span>
                    ${plannedMail.shortname}
                </p>
                <p class="description">
                    <i class="icon icon-list-ul"></i>
                    <span class="text">${plannedMail.mailinglist}</span>
                </p>
            </a>
        </li>
    </c:forEach>
</ul>
