<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey"		value="" 			            scope="request" />
<c:set var="isTabsMenuShown"		value="false" 			        scope="request" />
<c:set var="agnTitleKey" 			value="workflow.panel.forms"	scope="request" />
<c:set var="agnSubtitleKey" 		value="workflow.panel.forms" 	scope="request" />
<c:set var="sidemenu_active" 		value="Forms"		 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="Forms" 	                scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Forms" 					scope="request" />
<c:set var="agnHelpKey" 			value="formsImport" 			scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import"/>
    </emm:instantiate>
</emm:instantiate>
