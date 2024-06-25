<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="isTabsMenuShown" 		value="false"				scope="request" />
<c:set var="agnNavigationKey" 		value="none"				scope="request" />
<c:set var="agnTitleKey" 			value="Reports" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="default.Overview"	scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="Reports" 			scope="request" />
<c:set var="agnHighlightKey" 		value="Reports" 			scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Statistics" 			scope="request" />
<c:set var="agnHelpKey" 			value="reports" 			scope="request" />

<emm:ShowByPermission token="report.birt.change">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/statistics/report/new.action"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="report.new"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Reports"/>
    </emm:instantiate>
</emm:instantiate>
