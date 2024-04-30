<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="recipient" type="org.agnitas.emm.core.recipient.dto.RecipientLightDto"--%>
<%--@elvariable id="isMailTrackingEnabled" type="java.lang.Boolean"--%>

<c:set var="RESTORE_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESTORE_PARAM_NAME%>"/>

<c:set var="agnTitleKey" 			value="Recipient" 									scope="request" />
<c:set var="agnSubtitleKey" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.search" 								scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.RecipientMailings" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 										scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 									scope="request" />
<c:set var="agnHelpKey" 			value="recipientMailingHistory" 					scope="request" />

<emm:sideMenuAdditionalParam name="${RESTORE_SEARCH_PARAM_NAME}" value="true" forSubmenuOnly="false"/>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="recipientID" value="${recipient.customerId}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${isMailTrackingEnabled}">
		<c:set var="agnNavigationKey" value="subscriber_editor_mailtracking" scope="request" />
    </c:when>
    <c:otherwise>
		<c:set var="agnNavigationKey" value="subscriber_editor_no_mailtracking" scope="request" />
    </c:otherwise>
</c:choose>

<c:url var="recipientOverviewUrl" value="/recipient/list.action">
    <c:param name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>
</c:url>
<c:url var="recipientViewLink" value="/recipient/${recipient.customerId}/view.action"/>


<c:choose>
    <c:when test="${not empty recipient.firstname and not empty recipient.lastname}">
        <c:set var="recipientMention" value="${recipient.firstname} ${recipient.lastname}"/>
    </c:when>
    <c:when test="${not empty recipient.firstname}">
        <c:set var="recipientMention" value="${recipient.firstname}"/>
    </c:when>
    <c:when test="${not empty recipient.lastname}">
        <c:set var="recipientMention" value="${recipient.lastname}"/>
    </c:when>
    <c:otherwise>
        <c:set var="recipientMention" value="${recipient.email}"/>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.search"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${recipientOverviewUrl}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${recipientMention}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${recipientViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.RecipientMailings"/>
    </emm:instantiate>
</emm:instantiate>
