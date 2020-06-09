<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnNavigationKey" 		value="statsRecipients" 	scope="request" />
<c:set var="agnTitleKey" 			value="Recipient" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 			scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="Recipient" 			scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.Recipient" scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 			scope="request" />
<c:set var="agnHelpKey" 			value="recipientStatistic" 	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Recipients"/>
    </emm:instantiate>
</emm:instantiate>
