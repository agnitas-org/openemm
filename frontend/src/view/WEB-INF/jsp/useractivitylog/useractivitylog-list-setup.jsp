<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnNavigationKey" 		value="userlogs" 			scope="request" />
<c:set var="agnTitleKey" 			value="Userlogs" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Userlogs" 			scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="Userlogs" 			scope="request" />
<c:set var="agnHighlightKey" 		value="settings.Admin" 	    scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		scope="request" />
<c:set var="agnHelpKey" 			value="userlog" 			scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Userlogs"/>
    </emm:instantiate>
</emm:instantiate>
