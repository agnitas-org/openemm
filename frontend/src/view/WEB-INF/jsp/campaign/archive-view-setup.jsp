<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnTitleKey" 			value="mailing.archive" 						scope="request" />
<c:set var="sidemenu_active"	 	value="Mailings" 								scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailing.archive" 						scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 									scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 								scope="request" />

<c:choose>
    <c:when test="${form.id gt 0}">
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="campaignID" value="${form.id}"/>
            <c:set target="${agnNavHrefParams}" property="campaignName" value="${form.shortname}"/>
        </emm:instantiate>
        <c:set var="agnNavigationKey"	value="Archive" 		scope="request" />
        <c:set var="agnSubtitleKey" 	value="mailing.archive"	scope="request" />
        <c:set var="agnHighlightKey" 	value="campaign.Edit" 	scope="request" />
        <c:set var="agnHelpKey" 		value="mailingArchive" 	scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>
                <c:url var="campaignOverviewLink" value="/mailing/archive/list.action"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${campaignOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnSubtitleKey" 	value="campaign.NewCampaign"	scope="request" />
        <c:set var="agnHighlightKey" 	value="campaign.NewCampaign" 	scope="request" />
        <c:set var="agnHelpKey" 		value="mailingArchive"	 		scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.archive"/>
                <c:url var="campaignOverviewLink" value="/mailing/archive/list.action"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${campaignOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="campaign.NewCampaign"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty workflowForwardParams}">
        <emm:instantiate var="option" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="0" value="${option}"/>
            <c:set target="${option}" property="btnCls" value="btn btn-secondary btn-regular"/>
            <c:set target="${option}" property="iconBefore" value="icon-angle-left"/>
            <c:set target="${option}" property="type" value="href"/>
            <c:set target="${option}" property="url">
                <c:url value="/workflow/${workflowId}/view.action">
                    <c:param name="forwardParams" value="${workflowForwardParams};elementValue=${form.id}"/>
                </c:url>
            </c:set>
            <c:set target="${option}" property="name">
                <mvc:message code="button.Back"/>
            </c:set>
        </emm:instantiate>
    </c:if>

    <c:if test="${form.id gt 0}">
        <emm:ShowByPermission token="campaign.delete">
            <emm:instantiate type="java.util.LinkedHashMap" var="option">
                <c:set target="${itemActionsSettings}" property="0" value="${option}"/>
                <c:set target="${option}" property="btnCls" value="btn btn-regular btn-alert js-confirm"/>
                <c:set target="${option}" property="url">
                    <c:url value="/mailing/archive/${form.id}/confirmDelete.action"/>
                </c:set>
                <c:set target="${option}" property="iconBefore" value="icon-trash-o"/>
                <c:set target="${option}" property="type" value="href"/>
                <c:set target="${option}" property="name">
                    <mvc:message code="button.Delete"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:if>

    <emm:ShowByPermission token="campaign.change">
        <c:url var="saveUrl" value="/mailing/archive/save.action"/>
        <emm:instantiate type="java.util.LinkedHashMap" var="option">
            <c:set target="${itemActionsSettings}" property="1" value="${option}"/>
            <c:set target="${option}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${option}" property="extraAttributes" value="data-form-url='${saveUrl}' data-form-target='#archiveForm' data-form-submit"/>
            <c:set target="${option}" property="iconBefore" value="icon-save"/>
            <c:set target="${option}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
