<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="agnNavigationKey" 		value="MailingNew" 			scope="request" />
<c:set var="agnTitleKey" 			value="Mailing" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 			scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailing.New_Mailing" scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.New_Mailing" scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request" />
<c:set var="agnHelpKey" 			value="newMailingNormal"	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.New_Mailing"/>
            <c:url var="newMailingLink" value="/mailing/create.action"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${newMailingLink}"/>
    </emm:instantiate>
</emm:instantiate>
