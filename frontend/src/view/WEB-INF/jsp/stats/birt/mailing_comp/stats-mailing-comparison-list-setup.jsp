<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey" 		value="statsCompare" 			scope="request" />
<c:set var="isTabsMenuShown" 		value="false" 			        scope="request" />
<c:set var="agnTitleKey" 			value="statistic.comparison" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="statistic.comparison" 	scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.comparison" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 				scope="request" />
<c:set var="agnHelpKey" 			value="compareMailings" 		scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="statistic.comparison"/>
    </emm:instantiate>
</emm:instantiate>
