<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.web.forms.FormSearchParams" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="recipient" type="org.agnitas.emm.core.recipient.dto.RecipientLightDto"--%>
<%--@elvariable id="isMailTrackingEnabled" type="java.lang.Boolean"--%>

<c:set var="RESTORE_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESTORE_PARAM_NAME%>"/>

<c:set var="agnTitleKey" 			value="Recipient" 									scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 							scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.RecipientMailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 									scope="request" />
<c:set var="agnHelpKey" 			value="recipientMailingHistory" 					scope="request" />
<c:set var="agnEditViewKey" 	    value="contact-history-overview"                    scope="request" />

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
    <c:param name="restoreSort" value="true"/>
</c:url>
<c:url var="recipientViewLink" value="/recipient/${recipient.customerId}/view.action"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${recipient.mention}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${recipientViewLink}"/>
    </emm:instantiate>
</emm:instantiate>
