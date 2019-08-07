<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.RecipientAction"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.delete"/>

<c:set var="ACTION_LIST" 		value="<%= RecipientAction.ACTION_LIST %>" 		scope="request" />
<c:set var="ACTION_BULK_DELETE" value="<%= RecipientAction.ACTION_DELETE %>" 	scope="request" />

<c:set var="agnTitleKey" 			value="Recipients" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="Recipients" 				scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.search" 			scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.RecipientEdit" scope="request" />

<c:choose>
    <c:when test="${mailtracking}">
		<c:set var="agnNavigationKey" value="subscriber_editor_mailtracking" scope="request" />
    </c:when>
    <c:otherwise>
		<c:set var="agnNavigationKey" value="subscriber_editor_no_mailtracking" scope="request" />
    </c:otherwise>
</c:choose>
