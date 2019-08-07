<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="isTabsMenuShown" 		value="false" 					scope="request" />
<c:set var="agnNavigationKey" 		value="pluginmanagerView" 		scope="request" />
<c:set var="agnTitleKey" 			value="settings.pluginmanager" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.pluginmanager" 	scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.pluginmanager" 	scope="request" />
<c:set var="agnHighlightKey" 		value="pluginmanager.install" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Administration" 			scope="request" />
<c:set var="agnHelpKey" 			value="pluginmanagerList" 		scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.pluginmanager"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/administration/pluginmanager/plugins.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="pluginmanager.install"/>
    </emm:instantiate>
</emm:instantiate>
