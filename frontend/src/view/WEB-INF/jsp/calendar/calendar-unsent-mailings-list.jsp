<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="unsentMails" type="java.util.List"--%>

<div class="calendar-sidebar-header">
    <mvc:message code="calendar.unscheduledMailings"/>
</div>
<ul class="link-list">
    <c:forEach var="unsentMail" items="${unsentMails}">
        <li>
            <c:url var="mailingUrl" value="/mailing/${unsentMail.mailingid}/settings.action"/>
            <a href="${mailingUrl}" class="link-list-item">
                <p class="headline">
                    <span class="mailing-badge ${unsentMail.workstatus}"
                          data-tooltip="<mvc:message code="${unsentMail.workstatus}"/>"></span>
                        ${unsentMail.shortname}
                </p>
                <p class="description">
                    <span data-tooltip="<mvc:message code='birt.mailinglist'/>">
                        <i class="icon icon-list-ul"></i>
                        <span class="text">${unsentMail.mailinglist}</span>
                    </span>
                </p>
            </a>
        </li>
    </c:forEach>
</ul>
