<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>

<%@ page import="com.agnitas.web.MailingBaseAction" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>

<c:set var="BASE_ACTION_LIST" value="<%= MailingBaseAction.ACTION_LIST %>"/>
<c:set var="BASE_ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
</c:url>

<emm:ShowByPermission token="mailing.settings.migration">
    <c:url var="mailingViewLink" value="/mailing/${mailing.id}/settings.action">
        <c:param name="keepForward" value="true"/>
    </c:url>
</emm:ShowByPermission>
<emm:HideByPermission token="mailing.settings.migration">
    <c:url var="mailingViewLink" value="/mailingbase.do">
        <c:param name="action" value="${BASE_ACTION_VIEW}"/>
        <c:param name="mailingID" value="${mailing.id}"/>
        <c:param name="keepForward" value="true"/>
        <c:param name="init" value="true"/>
    </c:url>
</emm:HideByPermission>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<emm:ShowByPermission token="mailing.settings.migration">
    <c:url var="templateViewLink" value="/mailing/${mailing.id}/settings.action"/>
</emm:ShowByPermission>
<emm:HideByPermission token="mailing.settings.migration">
    <c:url var="templateViewLink" value="/mailingbase.do">
        <c:param name="action" value="${BASE_ACTION_VIEW}"/>
        <c:param name="mailingID" value="${mailing.id}"/>
    </c:url>
</emm:HideByPermission>

<c:set var="isBreadcrumbsShown" value="true" scope="request"/>
<c:set var="agnBreadcrumbsRootKey" value="Mailings" scope="request"/>
<c:set var="agnBreadcrumbsRootUrl" value="${mailingsOverviewLink}" scope="request"/>

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" 		value="false" scope="request" />

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
        </emm:instantiate>

        <c:set var="agnTitleKey" 			value="Mailing" 						scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailing" 						scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 						scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 						    scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 			scope="request" />
        <c:set var="agnHelpKey" 			value="mailingAttachments" 				scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Attachments"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>

    <c:when test="${not isMailingGrid and mailing.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 									scope="request" />
        <c:set var="agnTitleKey" 			value="Template" 										scope="request" />
        <c:set var="agnSubtitleKey" 		value="Template" 										scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 										scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 										scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 							scope="request" />
        <c:set var="agnHelpKey"             value="mailingAttachments" scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <emm:ShowByPermission token="mailing.settings.migration">
                <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
                <c:set target="${agnNavHrefParams}" property="init" value="true"/>
            </emm:ShowByPermission>
            <emm:HideByPermission token="mailing.settings.migration">
                <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingBaseForm.mailingID}"/>
                <c:set target="${agnNavHrefParams}" property="init" value="true"/>
            </emm:HideByPermission>
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

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Attachments"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>

    <c:otherwise>
        <c:choose>
            <c:when test="${limitedRecipientOverview}">
                <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"         scope="request" />
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" 		value="mailingView"                             scope="request" />
            </c:otherwise>
        </c:choose>
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </emm:instantiate>
        <c:set var="agnTitleKey" 			value="Mailing" 													scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailing" 													scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 													scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 														scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 										scope="request" />
        <c:set var="agnHelpKey"             value="mailingAttachments" scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Attachments"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>

</c:choose>

<jsp:include page="actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailing.id}"/>
    <jsp:param name="isTemplate" value="${mailing.isTemplate}"/>
    <jsp:param name="workflowId" value="${workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
</jsp:include>
