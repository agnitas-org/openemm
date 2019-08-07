<%@ page import="org.agnitas.web.CampaignAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= CampaignAction.ACTION_LIST %>"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= CampaignAction.ACTION_CONFIRM_DELETE %>"/>

<emm:CheckLogon/>
<emm:Permission token="campaign.show"/>

<c:set var="agnNavHrefAppend" 		value="&campaignID=${campaignForm.campaignID}"	scope="request" />
<c:set var="agnTitleKey" 			value="mailing.archive" 						scope="request" />
<c:set var="sidemenu_active"	 	value="Mailings" 								scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 						scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 									scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 								scope="request" />

<c:choose>
	<c:when test="${campaignForm.campaignID != 0}">
	    <c:set var="agnNavigationKey"	value="Campaign" 		scope="request" />
		<c:set var="agnSubtitleKey" 	value="mailing.archive"	scope="request" />
		<c:set var="agnHighlightKey" 	value="campaign.Edit" 	scope="request" />
		<c:set var="agnHelpKey" 		value="archiveView" 	scope="request" />

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
            </emm:instantiate>
        </emm:instantiate>
	</c:when>
	<c:otherwise>
	    <c:set var="agnNavigationKey"	value="CampaignsOverview" 		scope="request" />
 		<c:set var="agnSubtitleKey" 	value="campaign.NewCampaign"	scope="request" />
		<c:set var="agnHighlightKey" 	value="campaign.NewCampaign" 	scope="request" />
		<c:set var="agnHelpKey" 		value="newArchive"	 			scope="request" />

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
                <c:set target="${agnBreadcrumb}" property="textKey" value="campaign.NewCampaign"/>
            </emm:instantiate>
        </emm:instantiate>
	</c:otherwise>
</c:choose>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty workflowForwardParams}">
        <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
            <c:set target="${element0}" property="btnCls" value="btn btn-secondary btn-regular"/>
            <c:set target="${element0}" property="iconBefore" value="icon-angle-left"/>
            <c:set target="${element0}" property="type" value="href"/>
            <c:set target="${element0}" property="url">
                <html:rewrite page="/workflow.do?method=view&forwardParams=${workflowForwardParams};elementValue=${campaignForm.campaignID}&workflowId=${workflowId}"/>
            </c:set>
            <c:set target="${element0}" property="name">
                <bean:message key="button.Back"/>
            </c:set>
        </jsp:useBean>
    </c:if>

    <logic:notEqual name="campaignForm" property="campaignID" value="0">
        <emm:ShowByPermission token="campaign.delete">
            <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
                <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-alert"/>
                <c:set target="${element1}" property="extraAttributes" value="data-form-confirm='${ACTION_CONFIRM_DELETE}' data-form-target='#campaignForm'"/>
                <c:set target="${element1}" property="iconBefore" value="icon-trash-o"/>
                <c:set target="${element1}" property="name">
                    <bean:message key="button.Delete"/>
                </c:set>
            </jsp:useBean>
        </emm:ShowByPermission>
    </logic:notEqual>

    <emm:ShowByPermission token="campaign.change">
        <jsp:useBean id="element2" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="2" value="${element2}"/>
            <c:set target="${element2}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element2}" property="extraAttributes" value="data-form-submit-static data-form-target='#campaignForm'"/>
            <c:set target="${element2}" property="iconBefore" value="icon-save"/>
            <c:set target="${element2}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </jsp:useBean>
    </emm:ShowByPermission>
</jsp:useBean>
