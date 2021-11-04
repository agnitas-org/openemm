<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComRecipientAction" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.history"/>

<c:set var="ACTION_VIEW" 			value="<%= ComRecipientAction.ACTION_VIEW %>"			scope="request" />
<c:set var="ACTION_OVERVIEW_START" 	value="<%= ComRecipientAction.ACTION_OVERVIEW_START %>"	scope="request" />
<c:set var="ACTION_HISTORY_VIEW" 	value="<%= ComRecipientAction.ACTION_HISTORY_VIEW %>" 	scope="request" />
<c:set var="RESTORE_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESTORE_PARAM_NAME%>"/>

<c:set var="agnTitleKey" 			value="Recipient" 									scope="request" />
<c:set var="agnSubtitleKey" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 									scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.search" 								scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.RecipientHistory" 					scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 										scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 									scope="request" />
<c:set var="agnHelpKey" 			value="recipientHistory" 							scope="request" />

<emm:sideMenuAdditionalParam name="${RESTORE_SEARCH_PARAM_NAME}" value="true" forSubmenuOnly="false"/>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="recipientID" value="${recipientForm.recipientID}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${mailtracking}">
		<c:set var="agnNavigationKey" value="subscriber_editor_mailtracking" scope="request" />
    </c:when>
    <c:otherwise>
		<c:set var="agnNavigationKey" value="subscriber_editor_no_mailtracking" scope="request" />
    </c:otherwise>
</c:choose>

<emm:HideByPermission token="recipient.rollback">
    <c:url var="recipientOverviewUrl" value="/recipient/list.action">
        <c:param name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>
    </c:url>
    <c:url var="recipientViewLink" value="/recipient/${recipientForm.recipientID}/view.action"/>
</emm:HideByPermission>
<emm:ShowByPermission token="recipient.rollback">
    <c:url var="recipientsOverviewLink" value="/recipient.do">
        <c:param name="action" value="${ACTION_OVERVIEW_START}"/>
        <c:param name="trgt_clear" value="1"/>
        <c:param name="overview" value="true"/>
        <c:param name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>
    </c:url>

    <c:url var="recipientViewLink" value="/recipient.do">
        <c:param name="action" value="${ACTION_VIEW}"/>
        <c:param name="recipientID" value="${recipientForm.recipientID}"/>
    </c:url>
</emm:ShowByPermission>

<c:choose>
    <c:when test="${not empty recipientForm.firstname and not empty recipientForm.lastname}">
        <c:set var="recipientMention" value="${recipientForm.firstname} ${recipientForm.lastname}"/>
    </c:when>
    <c:when test="${not empty recipientForm.firstname}">
        <c:set var="recipientMention" value="${recipientForm.firstname}"/>
    </c:when>
    <c:when test="${not empty recipientForm.lastname}">
        <c:set var="recipientMention" value="${recipientForm.lastname}"/>
    </c:when>
    <c:otherwise>
        <c:set var="recipientMention" value="${recipientForm.email}"/>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.search"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${recipientsOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${recipientMention}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${recipientViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.RecipientHistory"/>
    </emm:instantiate>
</emm:instantiate>
