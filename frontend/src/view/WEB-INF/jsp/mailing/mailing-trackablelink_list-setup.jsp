<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComTrackableLinkAction" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="trackableLinkForm" type="com.agnitas.web.ComTrackableLinkForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="ACTION_LIST" 		value="<%= MailingBaseAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" 		value="<%= MailingBaseAction.ACTION_VIEW %>"/>
<c:set var="ACTION_SAVE_ALL" 	value="<%= ComTrackableLinkAction.ACTION_SAVE_ALL %>"/>

<emm:CheckLogon/>
<emm:Permission token="mailing.content.show"/>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${trackableLinkForm.mailingID}"/>
</c:url>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${trackableLinkForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
    <c:if test="${trackableLinkForm.isMailingGrid}">
        <c:param name="templateId" value="${templateId}"/>
    </c:if>
</c:url>

<c:set var="agnHighlightKey" 		value="mailing.Trackable_Links" scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnHelpKey" 			value="mailingLinks" 			scope="request" />

<c:choose>
    <c:when test="${trackableLinkForm.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 										scope="request" />
        <c:set var="agnNavHrefAppend" 		value="&mailingID=${trackableLinkForm.mailingID}&init=true"	scope="request" />
		<c:set var="agnTitleKey" 			value="Template" 											scope="request" />
        <c:set var="agnSubtitleKey" 		value="Template" 											scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 											scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 											scope="request" />
        
        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
	    <c:set var="agnTitleKey" 			value="Mailing" 				scope="request" />
    	<c:set var="agnSubtitleKey" 		value="Mailing" 				scope="request" />
    	<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 					scope="request" />
                
        <c:choose>
            <c:when test="${trackableLinkForm.isMailingGrid}">
                <c:set var="isTabsMenuShown" 	value="false" 																			scope="request" />

                <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${templateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${trackableLinkForm.mailingID}"/>
                </emm:instantiate>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${limitedRecipientOverview}">
                        <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
                    </c:otherwise>
                </c:choose>
                <c:set var="agnNavHrefAppend" 	value="&mailingID=${trackableLinkForm.mailingID}&init=true"	scope="request" />
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>

    <jsp:include page="actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${trackableLinkForm.mailingID}"/>
        <jsp:param name="isTemplate" value="${trackableLinkForm.isTemplate}"/>
        <jsp:param name="workflowId" value="${trackableLinkForm.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${trackableLinkForm.isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${not trackableLinkForm.isMailingGrid}">
        <%-- Save button for grid mailing is located at tile footer --%>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-target='#trackableLinkForm' data-form-set='everyPositionLink: false' data-form-action='${ACTION_SAVE_ALL}'"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
