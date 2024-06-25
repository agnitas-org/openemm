<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.mailingcontent.form.MailingContentForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>

<c:set var="isMailingEditable" value="${isMailingEditable and isMailingExclusiveLockingAcquired}" scope="request"/>
<c:set var="mailingId" value="${form.mailingID}"/>

<c:url var="templatesOverviewLink" value="/mailing/list.action">
    <c:param name="forTemplates" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailing/${mailingId}/settings.action"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnEditViewKey" 	    value="mailing-content-blocks"  scope="request" />

<c:choose>
    <c:when test="${form.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 								scope="request" />
        <c:set var="agnTitleKey"	 		value="Template" 									scope="request" />
        <c:set var="agnSubtitleKey"	 		value="Template" 									scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 									scope="request" />
        <c:set var="agnHighlightKey" 		value="default.Content" 							scope="request" />
        <c:set var="agnHelpKey" 			value="default.Content" 							scope="request" />

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
                <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="sidemenu_sub_active" value="none" scope="request"/>
        <c:set var="agnTitleKey" value="Mailing" scope="request"/>
        <c:set var="agnSubtitleKey" value="Mailing" scope="request"/>

        <c:choose>
            <c:when test="${form.gridTemplateId > 0}">
                <%@ include file="../fragments/mailing-grid-navigation.jspf" %>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${form.gridTemplateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                </emm:instantiate>

                <c:set var="agnHighlightKey" 	value="default.Content" 	scope="request" />
                <c:set var="agnHelpKey" 		value="mailingGridTextContent" 	scope="request" />
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${isPostMailing}">
                        <c:set var="agnNavigationKey" value="mailingView_post" scope="request" />
                    </c:when>
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
                <c:set var="agnHighlightKey" 	value="default.Content" scope="request" />
                <c:set var="agnHelpKey" 		value="contentView" 	scope="request" />
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingId}"/>
        <jsp:param name="isTemplate" value="${form.isTemplate}"/>
        <jsp:param name="workflowId" value="${form.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${isMailingEditable}">
        <c:url var="saveUrl" value="/mailing/content/${mailingId}/save.action"/>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn"/>
            <c:set target="${element}" property="extraAttributes" value="data-url='${saveUrl}' data-action='save'"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name"><mvc:message code="button.Save"/></c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
