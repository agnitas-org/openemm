<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey"	    value="serverStatusTabs"        scope="request" />
<c:set var="agnTitleKey"            value="settings.server.status"  scope="request" />
<c:set var="agnHelpKey" 			value="Server_status" 			scope="request" />
<c:set var="agnHighlightKey"        value="settings.server.status"  scope="request" />
<c:set var="sidemenu_active"        value="Administration"          scope="request" />
<c:set var="sidemenu_sub_active"    value="settings.server.status"  scope="request" />
<c:set var="agnBreadcrumbsRootKey"  value="Administration"          scope="request" />
<c:set var="agnEditViewKey" 	    value="system-status" 	        scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.server.status"/>
    </emm:instantiate>
</emm:instantiate>
