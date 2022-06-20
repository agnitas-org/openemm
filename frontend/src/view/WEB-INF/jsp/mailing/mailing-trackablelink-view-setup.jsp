<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.StrutsActionBase" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="workflowForwardParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, trackableLinkForm.workflowId).workflowForwardParams}"/>

<c:set var="ACTION_LIST" value="<%= StrutsActionBase.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>

<emm:CheckLogon/>
<emm:Permission token="mailing.content.show"/>

<%--@elvariable id="trackableLinkForm" type="com.agnitas.web.ComTrackableLinkForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

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
    <c:if test="${isMailingGrid}">
        <c:param name="templateId" value="${templateId}"/>
    </c:if>
</c:url>

<c:url var="trackableLinksOverviewLink" value="/tracklink.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="mailingID" value="${trackableLinkForm.mailingID}"/>
    <c:param name="init" value="true"/>
</c:url>

<c:set var="agnTitleKey" 			value="mailing.Trackable_Link" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="mailing.Trackable_Link" 	scope="request" />
<c:set var="sidemenu_active"	 	value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Trackable_Links" scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnHelpKey" 			value="trackableLinkView" 		scope="request" />

<c:choose>
    <c:when test="${trackableLinkForm.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 										scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 											scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingBaseForm.mailingID}"/>
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
                <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${trackableLinksOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
                <c:choose>
                    <c:when test="${not empty trackableLinkForm.linkName}">
                        <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.linkName}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set target="${agnBreadcrumb}" property="textKey" value="Unknown"/>
                    </c:otherwise>
                </c:choose>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="sidemenu_sub_active" value="none" scope="request"/>
        <c:choose>
            <c:when test="${isMailingGrid}">
                <c:set var="isTabsMenuShown" value="false" scope="request"/>

                <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${templateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${trackableLinkForm.mailingID}"/>
                </emm:instantiate>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${limitedRecipientOverview}">
                        <c:set var="agnNavigationKey" value="mailingView_DisabledMailinglist" scope="request"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="agnNavigationKey" value="mailingView" scope="request"/>
                    </c:otherwise>
                </c:choose>
                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${trackableLinkForm.mailingID}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${trackableLinksOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
                <c:choose>
                    <c:when test="${not empty trackableLinkForm.linkName}">
                        <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.linkName}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set target="${agnBreadcrumb}" property="textKey" value="Unknown"/>
                    </c:otherwise>
                </c:choose>
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

    <%-- Cancel/Save buttons for grid mailing are shown at tile footer --%>

    <c:if test="${not trackableLinkForm.isMailingGrid}">
        <emm:instantiate var="element0" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element0}"/>
            <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-secondary"/>
            <c:set target="${element0}" property="iconBefore" value="icon-reply"/>
            <c:set target="${element0}" property="type" value="href"/>
            <c:set target="${element0}" property="url">
                <c:url value="/tracklink.do">
                    <c:param name="action" value="${ACTION_LIST}"/>
                    <c:param name="mailingID" value="${trackableLinkForm.mailingID}"/>
                    <c:param name="forwardParams" value="${workflowForwardParams}"/>
                </c:url>
            </c:set>
            <c:set target="${element0}" property="name">
                <bean:message key="button.Cancel"/>
            </c:set>
        </emm:instantiate>

        <emm:instantiate var="element1" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-target='#trackableLinkForm' data-form-submit"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
