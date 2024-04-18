<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:set var="sidemenu_active" 		value="Mailings" 						scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Graphics_Components"		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings"	 					scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"			scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinks" 			        scope="request" />
<c:set var="agnEditViewKey" 	    value="mailing-images" 	                scope="request" />

<c:choose>
    <c:when test="${isTemplate}">
        <c:url var="templatesOverviewLink" value="/mailing/list.action">
            <c:param name="forTemplates" value="true"/>
        </c:url>

        <c:url var="templateViewLink" value="/mailing/${mailingId}/settings.action"/>

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
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
            <c:param name="keepForward" value="true"/>
        </c:url>

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
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
        <c:set target="${element}" property="cls" value="mobile-hidden"/>
        <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
        <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
        <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

        <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
            <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
        </emm:instantiate>

        <c:url var="bulkDownloadUrl" value="/mailing/${mailingId}/images/bulkDownload.action"/>

        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="0" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-form-url='${bulkDownloadUrl}' data-prevent-load data-form-submit-static data-form-target='#table-tile'"/>
            <c:set target="${option}" property="url">#</c:set>
            <c:set target="${option}" property="name">
                <mvc:message code="bulkAction.download.image.selected" />
            </c:set>
        </emm:instantiate>

        <c:url var="bulkDeleteUrl" value="/mailing/${mailingId}/images/deleteRedesigned.action"/>

        <emm:instantiate var="option" type="java.util.LinkedHashMap">
            <c:set target="${optionList}" property="2" value="${option}"/>
            <c:set target="${option}" property="extraAttributes" value="data-form-url='${bulkDeleteUrl}' data-form-confirm='' data-form-target='#table-tile'"/>
            <c:set target="${option}" property="url">#</c:set>
            <c:set target="${option}" property="name">
                <mvc:message code="bulkAction.delete.image" />
            </c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
