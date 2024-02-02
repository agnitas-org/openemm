<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="actionId" type="java.lang.Integer"--%>
<%--@elvariable id="shortname" type="java.lang.String"--%>

<c:set var="isTabsMenuShown" 		value="true" 								scope="request" />
<c:set var="agnNavigationKey" 		value="ActionEdit" 							scope="request" />
<c:set var="agnTitleKey" 			value="Actions" 							scope="request" />
<c:set var="agnSubtitleKey" 		value="Actions" 							scope="request" />
<c:set var="sidemenu_active" 		value="TriggerManagement" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="Actions" 							scope="request" />
<c:set var="agnHighlightKey" 		value="default.usedIn" 						scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 								scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="TriggerManagement" 					scope="request" />

<c:set var="agnHelpKey" 			value="newAction" 							scope="request" />


<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="action-id" value="${actionId}"/>
</emm:instantiate>


<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/action/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
    </emm:instantiate>
</emm:instantiate>
