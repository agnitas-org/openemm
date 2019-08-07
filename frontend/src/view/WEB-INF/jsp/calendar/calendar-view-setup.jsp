<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingBaseAction" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="ACTION_MAILINGS_LIST" value="<%=ComMailingBaseAction.ACTION_LIST %>" scope="request" />

<c:set var="agnNavigationKey" 		value="Calendar" 			scope="request" />
<c:set var="agnTitleKey" 			value="calendar.Calendar"	scope="request" />
<c:set var="agnSubtitleKey" 		value="calendar.Calendar" 	scope="request" />
<c:set var="sidemenu_active" 		value="Dashboard" 			scope="request" />
<c:set var="sidemenu_sub_active"	value="calendar.Calendar" 	scope="request" />
<c:set var="agnHighlightKey" 		value="calendar.Calendar" 	scope="request" />
<c:set var="agnHelpKey" 			value="calendar" 			scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon icon-desktop"></i>
    <mvc:message code="calendar.Calendar" />
</c:set>
