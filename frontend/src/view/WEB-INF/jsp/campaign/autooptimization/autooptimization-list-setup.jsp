<%@ page import="org.agnitas.web.StrutsActionBase" %>
<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= StrutsActionBase.ACTION_LIST %>" scope="request"/>
<c:set var="ACTION_VIEW" value="<%= StrutsActionBase.ACTION_VIEW %>" scope="request"/>

<emm:CheckLogon/>
<emm:Permission token="campaign.change"/>

<c:set var="agnNavigationKey" 		value="Campaign" 									scope="request"/>
<c:set var="agnNavHrefAppend" 		value="&campaignID=${optimizationForm.campaignID}"	scope="request"/>
<c:set var="agnTitleKey" 			value="mailing.autooptimization" 					scope="request"/>
<c:set var="agnSubtitleKey" 		value="mailing.archive" 							scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 									scope="request"/>
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 							scope="request"/>
<c:set var="agnHighlightKey" 		value="mailing.autooptimization" 					scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 										scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 									scope="request"/>
<c:set var="agnHelpKey" 			value="autooptimization" 							scope="request"/>


<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>
        <c:url var="campaignOverviewLink" value="/campaign.do">
            <c:param name="action" value="${ACTION_LIST}"/>
        </c:url>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${campaignForm.shortname}"/>
        <c:url var="campaignLink" value="/campaign.do">
            <c:param name="action" value="${ACTION_VIEW}"/>
            <c:param name="campaignID" value="${optimizationForm.campaignID}"/>
        </c:url>
        <c:set target="${agnBreadcrumb}" property="url" value="${campaignLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.autooptimization"/>
    </emm:instantiate>
</emm:instantiate>
