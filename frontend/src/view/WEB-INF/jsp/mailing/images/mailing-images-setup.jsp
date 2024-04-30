<%@ page language="java" import="org.agnitas.util.*, org.agnitas.beans.*, com.agnitas.beans.*" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>

<c:set var="MAILING_COMPONENT_IMAGE_TYPE"		 value="<%= MailingComponentType.Image %>"		scope="request" />
<c:set var="MAILING_COMPONENT_HOSTED_IMAGE_TYPE" value="<%= MailingComponentType.HostedImage %>" scope="request" />

<emm:CheckLogon/>
<emm:Permission token="mailing.components.show"/>

<c:url var="templatesOverviewLink" value="/mailing/list.action">
    <c:param name="forTemplates" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailing/${mailingId}/settings.action"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 						scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Graphics_Components"		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings"	 					scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"			scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinks" 			        scope="request" />

<c:choose>
    <c:when test="${isTemplate}">
        <!-- Template navigation -->
        <c:set var="agnNavigationKey" 	 value="templateView" 			 scope="request" />
        <c:set var="agnTitleKey" 		 value="Template" 				 scope="request" />
        <c:set var="agnSubtitleKey"      value="Template" 				 scope="request" />
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

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Graphics_Components"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <!-- Mailing navigation -->
        <c:choose>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey" value="mailingView_DisabledMailinglist" scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" value="mailingView" scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
        <c:set var="agnTitleKey" 		 value="Mailing" scope="request" />
        <c:set var="agnSubtitleKey" 	 value="Mailing" scope="request" />
        <c:set var="sidemenu_sub_active" value="none"    scope="request" />
        
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingShortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Graphics_Components"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<jsp:include page="../mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingId}"/>
    <jsp:param name="isTemplate" value="${isTemplate}"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
</jsp:include>
