<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.StrutsActionBase" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--<c:set var="workflowForwardParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, trackableLinkForm.workflowId).workflowForwardParams}"/>--%>

<c:set var="ACTION_LIST" value="<%= StrutsActionBase.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>"/>
<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<emm:CheckLogon/>
<emm:Permission token="mailing.content.show"/>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Boolean"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="trackableLinkForm" type="com.agnitas.emm.core.trackablelinks.form.TrackableLinkForm"--%>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingId}"/>
</c:url>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingId}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
    <c:if test="${isMailingGrid}">
        <c:param name="templateId" value="${gridTemplateId}"/>
    </c:if>
</c:url>

<c:url var="trackableLinksOverviewLink" value="/tracklink.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="mailingID" value="${mailingId}"/>
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
    <c:when test="${isTemplate}">
        <c:set var="agnNavigationKey" 	 value="templateView" scope="request" />
        <c:set var="sidemenu_sub_active" value="Templates"    scope="request" />

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

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Trackable_Links"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${trackableLinksOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
                <c:choose>
                    <c:when test="${not empty trackableLinkForm.shortname}">
                        <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.shortname}"/>
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
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
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
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingShortname}"/>
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
                    <c:when test="${not empty trackableLinkForm.shortname}">
                        <c:set target="${agnBreadcrumb}" property="text" value="${trackableLinkForm.shortname}"/>
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

    <jsp:include page="../actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingId}"/>
        <jsp:param name="isTemplate" value="${isTemplate}"/>
        <jsp:param name="workflowId" value="${workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
    </jsp:include>

    <%-- Cancel/Save buttons for grid mailing are shown at tile footer --%>

    <c:if test="${not isMailingGrid}">
        <emm:instantiate var="element0" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element0}"/>
            <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-secondary"/>
            <c:set target="${element0}" property="iconBefore" value="icon-reply"/>
            <c:set target="${element0}" property="type" value="href"/>
            <c:set target="${element0}" property="url">
                <c:url value="/mailing/${mailingId}/trackablelink/list.action">
<%--                    <c:param name="forwardParams" value="${workflowForwardParams}"/>--%>
                </c:url>
            </c:set>
            <c:set target="${element0}" property="name">
                <mvc:message code="button.Cancel"/>
            </c:set>
        </emm:instantiate>

        <emm:instantiate var="element1" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-target='#trackableLinkForm' data-action='save'"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
