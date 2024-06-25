<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>
<c:url var="mailingViewLink" value="/mailing/${mailing.id}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="isBreadcrumbsShown"    value="true"                      scope="request"/>
<c:set var="agnBreadcrumbsRootKey" value="Mailings"                  scope="request"/>
<c:set var="agnBreadcrumbsRootUrl" value="${mailingsOverviewLink}"   scope="request"/>
<c:set var="agnEditViewKey" 	   value="mailing-attachments" 	     scope="request" />

<c:choose>
    <c:when test="${isMailingGrid}">
        <%@ include file="../fragments/mailing-grid-navigation.jspf" %>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
        </emm:instantiate>

        <c:set var="agnTitleKey" 			value="Mailing" 				scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 					scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 	scope="request" />
        <c:set var="agnHelpKey" 			value="mailingAttachments" 		scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>

    <c:when test="${not isMailingGrid and mailing.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 			scope="request" />
        <c:set var="agnTitleKey" 			value="Template" 				scope="request" />
        <c:set var="agnSubtitleKey" 		value="Template" 				scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 				scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 	scope="request" />
        <c:set var="agnHelpKey"             value="mailingAttachments"      scope="request" />

        <c:url var="templateViewLink" value="/mailing/${mailing.id}/settings.action"/>
        <c:url var="templatesOverviewLink" value="/mailing/list.action">
            <c:param name="forTemplates" value="true"/>
        </c:url>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
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
                <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>

    <c:otherwise>
        <c:choose>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey" value="mailingView_DisabledMailinglist" scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" value="mailingView" scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
        <c:set var="agnTitleKey" 			value="Mailing" 				scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 					scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 	scope="request" />
        <c:set var="agnHelpKey"             value="mailingAttachments"      scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailing.id}"/>
        <jsp:param name="isTemplate" value="${mailing.isTemplate}"/>
        <jsp:param name="workflowId" value="${workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${isMailingEditable}">
        <c:url var="saveUrl" value="/mailing/${mailing.id}/attachment/save.action" />
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn"/>
            <c:set target="${element}" property="cls" value="mobile-hidden" />
            <c:set target="${element}" property="type" value="button"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#table-tile' data-form-submit data-form-url='${saveUrl}'"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
