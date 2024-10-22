<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.preview.form.PreviewForm"--%>
<%--@elvariable id="isActiveMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>

<c:set var="mailingId" value="${form.mailingId}"/>
<c:set var="shortname" value="${form.mailingShortname}"/>
<c:set var="gridTemplateId" value="${form.mailingTemplateId}"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>
<c:url var="templatesOverviewLink" value="/mailing/list.action">
    <c:param name="forTemplates" value="true"/>
</c:url>
<c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
    <c:if test="${not form.isTemplate}">
        <c:param name="keepForward" value="true"/>
    </c:if>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 				 scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Preview" 		 scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 	             scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	 scope="request" />
<c:set var="agnHelpKey" 			value="mailingPreview" 		     scope="request" />

<c:choose>
    <c:when test="${form.isMailingGrid}">
        <c:set var="agnNavigationKey" value="GridMailingView" scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
        </emm:instantiate>
    </c:when>

    <c:otherwise>
        <c:choose>
            <c:when test="${form.isTemplate}">
                <c:set var="agnNavigationKey"       value="templateView"    scope="request" />
                <c:set var="agnTitleKey" 			value="Template" 		scope="request" />
                <c:set var="sidemenu_sub_active"	value="Templates" 		scope="request" />

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${form.mailingId}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
            </c:when>
            <c:otherwise>
                <c:set var="agnNavigationKey" value="mailingView" scope="request" />

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<c:if test="${not form.isTemplate}">
    <emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
        <c:set target="${agnNavConditionsParams}" property="isActiveMailing" value="${isActiveMailing}" />
        <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
    </emm:instantiate>

    <c:set var="agnTitleKey" 			value="Mailing"            scope="request" />
    <c:set var="sidemenu_sub_active"	value="default.Overview"   scope="request" />
</c:if>

<%-- Breadcrubms --%>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${form.isTemplate}">
        <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
            <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
            <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
            <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
        </emm:instantiate>
    </c:if>
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:include page="../mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${form.mailingId}"/>
    <jsp:param name="isTemplate" value="${form.isTemplate}"/>
    <jsp:param name="workflowId" value="${form.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
</jsp:include>
