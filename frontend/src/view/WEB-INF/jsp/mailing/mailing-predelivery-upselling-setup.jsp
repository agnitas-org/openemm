<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="upsellingForm" type="com.agnitas.emm.core.upselling.form.UpsellingForm"--%>
<%--@elvariable id="navigationKey" type="java.lang.String"--%>
<%--@elvariable id="predeliveryForm" type="org.agnitas.web.forms.ComPredeliveryForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<%--TODO: GWUA-4803: migrate upselling page after migration code test successfully --%>

<c:set var="mailingID" value="${upsellingForm.extraParamsMap.mailingID}"/>
<c:if test="${empty mailingID}">
    <c:set var="mailingID" value="${upsellingForm.mailingIdFromSpringUrl}"/>
</c:if>
<c:set var="templateId" value="${upsellingForm.extraParamsMap.templateId}"/>

<c:if test="${not empty predeliveryForm}">
    <c:set var="mailingID" value="${predeliveryForm.mailingID}"/>
    <c:set var="templateId" value="${predeliveryForm.templateId}"/>

    <c:choose>
        <c:when test="${predeliveryForm.isMailingGrid}">
            <c:set var="isTabsMenuShown" value="false" 	scope="request" />
            <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>
            <c:set var="sidemenu_sub_active"	value="mailing.New_Mailing" scope="request" />
            <c:set var="CONTEXT" value="${pageContext.request.contextPath}" scope="request"/>
        </c:when>
        <c:otherwise>
            <c:choose>
                <c:when test="${limitedRecipientOverview}">
                    <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist" scope="request" />
                </c:when>
                <c:otherwise>
                    <c:set var="agnNavigationKey" 		value="mailingView"                     scope="request" />
                </c:otherwise>
            </c:choose>
            <c:set var="sidemenu_sub_active"	        value="none"                            scope="request" />
        </c:otherwise>
    </c:choose>
</c:if>

<c:if test="${empty predeliveryForm}">
    <c:choose>
        <c:when test="${navigationKey eq 'GridMailingView_DisabledMailinglist' or navigationKey eq 'GridMailingView'}">
            <c:set var="isTabsMenuShown"        value="false" 	            scope="request" />
            <c:set var="sidemenu_sub_active"	value="mailing.New_Mailing" scope="request" />
        </c:when>

        <c:otherwise>
            <c:set var="sidemenu_sub_active"	value="none"    scope="request" />
        </c:otherwise>
    </c:choose>
    <c:set var="agnNavigationKey" value="${navigationKey}"      scope="request"/>
</c:if>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="templateID" value="${templateId}"/>
    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingID}"/>
    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
</emm:instantiate>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="mailingViewLink" value="/mailing/${mailingID}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 				    scope="request" />
<c:set var="agnTitleKey" 			value="Mailing" 				    scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 				    scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.provider.preview" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					    scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				    scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	    scope="request" />
<c:set var="agnHelpKey" 			value="inboxPreview" 		        scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${not empty predeliveryForm}">
        <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
            <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
            <c:set target="${agnBreadcrumb}" property="text" value="${predeliveryForm.shortName}"/>
            <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
        </emm:instantiate>
    </c:if>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.provider.preview"/>
    </emm:instantiate>
</emm:instantiate>
