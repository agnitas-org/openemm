<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingWizardAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingWizardForm" type="org.agnitas.web.MailingWizardForm"--%>

<c:choose>
    <c:when test="${not empty mailingWizardForm.mailing}">
        <c:set var="shortname" value="${mailingWizardForm.mailing.shortname}"/>
    </c:when>
    <c:otherwise>
        <c:set var="shortname" value=""/>
    </c:otherwise>
</c:choose>

<c:set var="agnNavigationKey" 		value="MailingNew" 			scope="request" />
<c:set var="agnTitleKey" 			value="Mailing" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 			scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailing.New_Mailing" scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.New_Mailing"	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request" />
<c:set var="agnHelpKey" 			value="newMailingWizard" 	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.New_Mailing"/>
        <c:url var="newMailingLink" value="/mwStart.do">
            <c:param name="action" value="init"/>
        </c:url>
        <c:set target="${agnBreadcrumb}" property="url" value="${newMailingLink}"/>
    </emm:instantiate>

    <c:if test="${not empty shortname}">
        <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
            <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
            <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
