<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="isActiveMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:set var="sidemenu_active" 		value="Mailings" 						scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Graphics_Components"		scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings"	 					scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"			scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinks" 			        scope="request" />
<c:set var="agnEditViewKey" 	    value="mailing-images" 	                scope="request" />

<c:choose>
    <c:when test="${isTemplate}">
        <c:url var="templatesOverviewLink" value="/mailing/list.action">
            <c:param name="forTemplates" value="true"/>
        </c:url>

        <c:url var="templateViewLink" value="/mailing/${mailingId}/settings.action"/>

        <!-- Template navigation -->
        <c:set var="agnNavigationKey" 	 value="templateView" 			 scope="request" />
        <c:set var="agnTitleKey" 		 value="Template" 				 scope="request" />
        <c:set var="sidemenu_sub_active" value="Templates" 				 scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
        
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingShortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
            <c:param name="keepForward" value="true"/>
        </c:url>

        <!-- Mailing navigation -->
        <c:set var="agnNavigationKey" value="mailingView" scope="request" />

        <emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavConditionsParams}" property="isActiveMailing" value="${isActiveMailing}" />
            <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
        </emm:instantiate>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
        <c:set var="agnTitleKey" 		 value="Mailing"            scope="request" />
        <c:set var="sidemenu_sub_active" value="default.Overview"   scope="request" />
        
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingShortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<jsp:include page="../mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingId}"/>
    <jsp:param name="isTemplate" value="${isTemplate}"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${undoAvailable}"/>
</jsp:include>
