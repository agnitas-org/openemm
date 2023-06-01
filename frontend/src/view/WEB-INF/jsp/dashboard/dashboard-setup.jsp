<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnTitleKey" 			value="default.A_EMM" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="Dashboard" 			scope="request" />
<c:set var="sidemenu_active" 		value="Dashboard" 			scope="request" />
<c:set var="agnHelpKey" 			value="dashboard" 			scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-desktop"></i>
    <mvc:message code="Dashboard" />
</c:set>
