<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.send.world"/>

<c:set var="ACTION_LIST" 				value="<%= MailingBaseAction.ACTION_LIST %>" />
<c:set var="ACTION_VIEW" 				value="<%= MailingBaseAction.ACTION_VIEW %>" />
<c:set var="ACTION_VIEW_SEND" 			value="<%= MailingSendAction.ACTION_VIEW_SEND %>"/>
<c:set var="ACTION_CONFIRM_UNDO" 		value="<%= MailingBaseAction.ACTION_CONFIRM_UNDO %>" scope="page"/>
<c:set var="ACTION_CONFIRM_DELETE" 		value="<%= MailingBaseAction.ACTION_CONFIRM_DELETE %>" />
<c:set var="ACTION_CLONE_AS_MAILING" 	value="<%= MailingBaseAction.ACTION_CLONE_AS_MAILING %>" />
<c:set var="ACTION_CREATE_FOLLOW_UP" 	value="<%= MailingBaseAction.ACTION_CREATE_FOLLOW_UP %>" scope="page" />

<c:set var="isMailingGrid" value="${mailingSendForm.isMailingGrid}" scope="page"/>

<c:set var="shortname" value="${mailingSendForm.shortname}" scope="page"/>
<c:set var="mailingId" value="${mailingSendForm.mailingID}" scope="page"/>
<c:set var="mailingExists" value="${mailingId ne 0}" scope="page"/>

<%-- check if we have a followup --%>
<c:set var="followup" value="false" scope="request"/>
<c:if test="${not empty mailingSendForm.followupFor}">
    <c:set value="followup" var="true" scope="request"/>
</c:if>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<c:url var="mailingSendViewLink" value="/mailingsend.do">
    <c:param name="action" value="${ACTION_VIEW_SEND}"/>
    <c:param name="mailingID" value="${mailingSendForm.mailingID}"/>
    <c:param name="init" value="true"/>
</c:url>

<c:set var="agnTitleKey" 			value="Mailing" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 			scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="none" 				scope="request" />
<c:set var="agnHighlightKey" 		value="Send_Mailing" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request" />
<c:set var="agnHelpKey" 			value="sendMailing"	        scope="request" />

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" 	value="false" 																		scope="request" />

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${mailingSendForm.templateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${isPostMailing}">
                <c:set var="agnNavigationKey" value="mailingView_post" scope="request" />
            </c:when>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${mailingSendForm.shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingSendViewLink}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Send_Mailing"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="workflow.mailing.DeliverySettings"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:include page="actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingSendForm.mailingID}"/>
    <jsp:param name="isTemplate" value="${mailingSendForm.isTemplate}"/>
    <jsp:param name="workflowId" value="${mailingSendForm.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${mailingSendForm.isMailingUndoAvailable}"/>
</jsp:include>
