<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="plannedMails" type="java.util.List"--%>

<c:set var="ACTION_VIEW" value="<%=  MailingBaseAction.ACTION_VIEW %>" scope="page"/>
<c:set var="ACTION_USED_ACTIONS" value="<%=  MailingBaseAction.ACTION_USED_ACTIONS %>" scope="page"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%=  MailingBaseAction.ACTION_CONFIRM_DELETE %>" scope="page"/>

<div class="calendar-sidebar-header">
    <mvc:message code="calendar.planned.mailings"/>
</div>
<ul class="link-list">
    <c:forEach var="plannedMail" items="${plannedMails}">
        <li>
            <c:url var="mailingBaseUrl" value="/mailingbase.do">
                <c:param name="action" value="${ACTION_VIEW}"/>
                <c:param name="mailingID" value="${plannedMail.mailingid}"/>
            </c:url>
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
