<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnNavigationKey" 		value="statsMonth" 		scope="request" />
<c:set var="agnTitleKey" 			value="MonthStats" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 		scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="MonthStats" 		scope="request" />
<c:set var="agnHighlightKey" 		value="MonthStats" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 		scope="request" />
<c:set var="agnHelpKey" 			value="monthlyOverview" scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="MonthStats"/>
    </emm:instantiate>
</emm:instantiate>
