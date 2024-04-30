<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.mailing.autooptimization.form.OptimizationForm"--%>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="campaignID" value="${form.campaignID}"/>
    <c:set target="${agnNavHrefParams}" property="campaignName" value="${form.campaignName}"/>
</emm:instantiate>

<c:set var="agnNavigationKey" 		value="Archive" 					scope="request"/>
<c:set var="agnTitleKey" 			value="mailing.autooptimization" 	scope="request"/>
<c:set var="agnSubtitleKey" 		value="mailing.archive" 			scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 					scope="request"/>
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 			scope="request"/>
<c:set var="agnHighlightKey" 		value="mailing.autooptimization" 	scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 					scope="request"/>
<c:set var="agnHelpKey" 			value="autooptimization" 			scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>

        <c:url var="campaignOverviewLink" value="/mailing/archive/list.action"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${form.campaignName}"/>

        <c:url var="campaignLink" value="/mailing/archive/${form.campaignID}/view.action"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.autooptimization"/>
    </emm:instantiate>
</emm:instantiate>
