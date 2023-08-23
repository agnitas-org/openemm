<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="isMailingGrid" value="${form.isMailingGrid}" scope="page"/>

<c:set var="shortname" value="${form.shortname}" scope="page"/>
<c:set var="mailingId" value="${form.mailingID}" scope="page"/>
<c:set var="mailingExists" value="${mailingId ne 0}" scope="page"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="mailingViewLink" value="/mailing/${form.mailingID}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:url var="mailingSendViewLink" value="/mailing/send/${form.mailingID}/view.action" />

<c:set var="agnTitleKey" 			value="Mailing" 	            scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 	            scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 	            scope="request" />
<c:set var="sidemenu_sub_active" 	value="none" 		            scope="request" />
<c:set var="agnHighlightKey" 		value="Send_Mailing"            scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 		            scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 	            scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnHelpKey" 			value="sendMailing"	            scope="request" />

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request" />

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${form.templateId}"/>
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
        <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
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

<jsp:include page="/WEB-INF/jsp/mailing/mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${form.mailingID}"/>
    <jsp:param name="isTemplate" value="${form.isTemplate}"/>
    <jsp:param name="workflowId" value="${form.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
</jsp:include>
