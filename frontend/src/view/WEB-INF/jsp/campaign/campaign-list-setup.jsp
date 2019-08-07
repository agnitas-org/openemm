<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.CampaignAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= CampaignAction.ACTION_LIST %>" scope="request"/>
<c:set var="ACTION_VIEW" value="<%= CampaignAction.ACTION_VIEW %>" scope="request"/>
<c:set var="ACTION_NEW" value="<%= CampaignAction.ACTION_NEW %>" scope="request"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= CampaignAction.ACTION_CONFIRM_DELETE %>" scope="request"/>

<emm:CheckLogon/>
<emm:Permission token="campaign.show"/>

<c:set var="agnNavigationKey" 		value="CampaignsOverview"	scope="request"/>
<c:set var="agnTitleKey" 			value="mailing.archive" 	scope="request"/>
<c:set var="agnSubtitleKey" 		value="mailing.archive" 	scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 			scope="request"/>
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 	scope="request"/>
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request"/>
<c:set var="agnHelpKey" 			value="archiveView" 		scope="request"/>

<emm:ShowByPermission token="campaign.change">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/campaign.do?action=${ACTION_NEW}'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="campaign.NewCampaign"/>
    </c:set>
</emm:ShowByPermission>


<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>
    </emm:instantiate>
</emm:instantiate>
