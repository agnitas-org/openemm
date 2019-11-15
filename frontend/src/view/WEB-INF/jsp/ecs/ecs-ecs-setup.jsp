<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="ecsForm" type="com.agnitas.ecs.web.forms.EcsForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingsOverviewLink" type="java.lang.String"--%>

<c:set var="BASE_ACTION_LIST" value="<%= MailingBaseAction.ACTION_LIST %>"/>
<c:set var="BASE_ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>

<emm:CheckLogon/>
<emm:Permission token="stats.ecs"/>

<c:set var="agnTitleKey" 			value="Mailing" 				scope="request"/>
<c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 				scope="request"/>
<c:set var="agnHighlightKey" 		value="ecs.Heatmap"	 			scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request"/>
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}"	scope="request"/>
<c:set var="agnHelpKey" 			value="heatmap" 				scope="request" />

<c:choose>
    <c:when test="${ecsForm.isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request"/>

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${ecsForm.templateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${ecsForm.mailingID}"/>
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
        <c:set var="agnNavHrefAppend"	value="&mailingID=${ecsForm.mailingID}&init=true"	scope="request"/>
    </c:otherwise>
</c:choose>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${ecsForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">у
      <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${ecsForm.shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="ecs.Heatmap"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:include page="/WEB-INF/jsp/mailing/actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${ecsForm.mailingID}"/>
    <jsp:param name="isTemplate" value="false"/>
    <jsp:param name="workflowId" value="${ecsForm.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${ecsForm.isMailingUndoAvailable}"/>
</jsp:include>
