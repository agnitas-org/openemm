<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.mailingcontent.form.MailingContentForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>

<c:url var="templatesOverviewLink" value="/mailing/list.action">
    <c:param name="forTemplates" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailing/${form.mailingID}/settings.action"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="mailingViewLink" value="/mailing/${form.mailingID}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}"	scope="request" />

<c:if test="${form.gridTemplateId > 0}">
    <c:set var="isTabsMenuShown" value="false" scope="request"/>
</c:if>

<c:choose>
    <c:when test="${form.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 								scope="request" />
        <c:set var="agnTitleKey"	 		value="Template" 									scope="request" />
        <c:set var="agnSubtitleKey"	 		value="Template" 									scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 									scope="request" />
        <c:set var="agnHighlightKey" 		value="default.Content" 							scope="request" />
        <c:set var="agnHelpKey" 			value="default.Content" 							scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${form.mailingID}"/>
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

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="default.Content"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="sidemenu_sub_active" value="none" scope="request"/>
        <c:set var="agnTitleKey" value="Mailing" scope="request"/>
        <c:set var="agnSubtitleKey" value="Mailing" scope="request"/>

        <c:choose>
            <c:when test="${form.gridTemplateId > 0}">
                <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${form.gridTemplateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${form.mailingID}"/>
                </emm:instantiate>

                <c:set var="agnHighlightKey" 	value="mailing.TextModules" 	scope="request" />
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
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${form.mailingID}"/>
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

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="default.Content"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${form.mailingID}"/>
        <jsp:param name="isTemplate" value="${form.isTemplate}"/>
        <jsp:param name="workflowId" value="${form.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${form.mailingID ne 0 and not isPostMailing}">
        <%-- View dropdown --%>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-eye"/>
            <c:set target="${element}" property="name"><mvc:message code='default.View' /></c:set>
            <c:set target="${element}" property="iconAfter" value="icon-caret-down"/>

            <emm:instantiate var="dropDownItems" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${dropDownItems}"/>
            </emm:instantiate>

            <%-- Add dropdown items (view modes) --%>
            <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="0" value="${dropDownItem}"/>

                <c:set target="${dropDownItem}" property="type" value="radio"/>
                <c:set target="${dropDownItem}" property="radioName" value="view-state"/>
                <c:set target="${dropDownItem}" property="radioValue" value="block"/>
                <c:set target="${dropDownItem}" property="extraAttributes" value="data-view='mailingViewBase'"/>
                <c:set target="${dropDownItem}" property="name"><mvc:message code='mailing.content.blockview' /></c:set>
            </emm:instantiate>

            <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="1" value="${dropDownItem}"/>

                <c:set target="${dropDownItem}" property="type" value="radio"/>
                <c:set target="${dropDownItem}" property="radioName" value="view-state"/>
                <c:set target="${dropDownItem}" property="radioValue" value="split"/>
                <c:set target="${dropDownItem}" property="extraAttributes" value="checked data-view='mailingViewBase'"/>
                <c:set target="${dropDownItem}" property="name"><mvc:message code="mailing.content.splitview"/></c:set>
            </emm:instantiate>

            <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="2" value="${dropDownItem}"/>

                <c:set target="${dropDownItem}" property="type" value="radio"/>
                <c:set target="${dropDownItem}" property="radioName" value="view-state"/>
                <c:set target="${dropDownItem}" property="radioValue" value="hidden"/>
                <c:set target="${dropDownItem}" property="extraAttributes" value="data-view='mailingViewBase'"/>
                <c:set target="${dropDownItem}" property="name"><mvc:message code="mailing.content.hidepreview"/></c:set>
            </emm:instantiate>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
