<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="CampaignsOverview"	scope="request"/>
<c:set var="agnTitleKey" 			value="mailing.archive" 	scope="request"/>
<c:set var="agnSubtitleKey" 		value="mailing.archive" 	scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request"/>
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 	scope="request"/>
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request"/>
<c:set var="agnHelpKey" 			value="mailingArchive" 		scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="campaign.change">
    <c:url var="createNewItemUrl" value="/campaign/create.action" scope="request"/>
    <mvc:message var="createNewItemLabel" code="campaign.NewCampaign" scope="request"/>
</emm:ShowByPermission>
