<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="templateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>

<c:set var="isMailingGrid" value="${not empty templateId and templateId gt 0}" scope="request"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="mailingViewLink" value="/mailing/${mailing.id}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="agnTitleKey" 			value="Mailing" 				scope="request"/>
<c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 				scope="request"/>
<c:set var="agnHighlightKey" 		value="ecs.Heatmap"	 			scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request"/>
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}"	scope="request"/>
<c:set var="agnHelpKey" 			value="heatmap" 				scope="request" />

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request"/>

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${templateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
    </c:otherwise>
</c:choose>


<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">у
      <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="ecs.Heatmap"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:include page="/WEB-INF/jsp/mailing/mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailing.id}"/>
    <jsp:param name="isTemplate" value="false"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
</jsp:include>
