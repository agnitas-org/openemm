<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<%--@elvariable id="SHOW_SUPERVISOR_PERMISSION_MANAGEMENT" type="java.lang.Boolean"--%>

<c:choose>
	<c:when test="${SHOW_SUPERVISOR_PERMISSION_MANAGEMENT}">
		<c:set var="agnNavigationKey"		value="userselfservice_with_sv_login"				scope="request" />
	</c:when>
	<c:otherwise>
		<c:set var="agnNavigationKey"		value="userselfservice"				scope="request" />
	</c:otherwise>
</c:choose>
	
<c:set var="agnTitleKey" 			value="settings.Admin" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Welcome" 				scope="request" />
<c:set var="sidemenu_active" 		value="none" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="none"	 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.enabling" 		scope="request" />
<c:set var="agnHelpKey" 			value="supervisor-permissions" 	scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon icon-cog"></i> ${sessionScope['fullName']}
</c:set>
