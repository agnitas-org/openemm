<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="agnNavigationKey" 		value="admins" 							scope="request" />
<c:set var="agnTitleKey" 			value="settings.Admin" 					scope="request" />                <!-- Titelleiste -->
<c:set var="agnSubtitleKey" 		value="settings.Admin" 					scope="request" />             <!-- ueber rechte Seite -->
<c:set var="agnSubtitleValue" 		value="${adminForm.username}" 			scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 					scope="request" />          <!-- links Button -->
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 					scope="request" />        <!-- links unter Button -->
<c:set var="agnHelpKey" 			value="newUser" 						scope="request" />

<c:choose>
	<c:when test="${adminForm.adminID != 0}">
		<c:set var="agnHighlightKey" value="settings.Admin" scope="request" />
	</c:when>
	<c:otherwise>
		<c:set var="agnHighlightKey" value="settings.New_Admin" scope="request" />
	</c:otherwise>
</c:choose>
