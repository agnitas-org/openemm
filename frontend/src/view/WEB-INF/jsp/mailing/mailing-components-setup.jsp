<%@ page language="java" import="org.agnitas.util.*, org.agnitas.beans.*, com.agnitas.beans.*" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingComponentsForm" type="com.agnitas.web.forms.ComMailingComponentsForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="ACTION_LIST" 							value="<%= MailingBaseAction.ACTION_LIST %>"		scope="request" />
<c:set var="ACTION_VIEW"							value="<%= MailingBaseAction.ACTION_VIEW %>"		scope="request" />
<c:set var="MAILING_COMPONENT_TYPE_IMAGE"			value="<%= MailingComponentType.Image %>"			scope="request" />
<c:set var="MAILING_COMPONENT_TYPE_HOSTED_IMAGE"	value="<%= MailingComponentType.HostedImage %>"	scope="request" />

<emm:CheckLogon/>
<emm:Permission token="mailing.components.show"/>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingComponentsForm.mailingID}"/>
</c:url>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingComponentsForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 						scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Graphics_Components"		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings"	 					scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"			scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinks" 			        scope="request" />

<c:choose>
    <c:when test="${mailingComponentsForm.isTemplate}">
        <!-- Template navigation -->
       
        <c:set var="agnNavigationKey" 		value="templateView" 									scope="request" />
        <c:set var="agnNavHrefAppend" 		value="&mailingID=${mailingComponentsForm.mailingID}"	scope="request" />
        <c:set var="agnTitleKey" 			value="Template" 										scope="request" />
        <c:set var="agnSubtitleKey" 		value="Template" 										scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 										scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingComponentsForm.shortname}"/>
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
                <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingComponentsForm.mailingID}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
        <c:set var="agnTitleKey" 			value="Mailing" 												scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailing" 												scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 													scope="request" />
        
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingComponentsForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Graphics_Components"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<jsp:include page="actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingComponentsForm.mailingID}"/>
    <jsp:param name="isTemplate" value="${mailingComponentsForm.isTemplate}"/>
    <jsp:param name="workflowId" value="${mailingComponentsForm.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${mailingComponentsForm.isMailingUndoAvailable}"/>
</jsp:include>
