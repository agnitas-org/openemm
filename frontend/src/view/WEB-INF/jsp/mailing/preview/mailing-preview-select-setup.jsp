<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.preview.form.PreviewForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>

<c:set var="mailingId" value="${form.mailingId}"/>
<c:set var="shortname" value="${form.mailingShortname}"/>
<c:set var="gridTemplateId" value="${form.mailingTemplateId}"/>

<c:url var="mailingsOverviewLink" value="/mailing/list.action"/>

<c:url var="templatesOverviewLink" value="/mailing/list.action">
    <c:param name="forTemplates" value="true"/>
</c:url>

<c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailing/${mailingId}/settings.action"/>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Preview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnHelpKey" 			value="mailingPreview" 		    scope="request" />


<c:choose>
    <c:when test="${form.isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request"/>

        <c:set var="CONTEXT" value="${pageContext.request.contextPath}" scope="request"/>

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
        </emm:instantiate>

        <c:set var="agnTitleKey" 			value="Mailing" 		scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 			scope="request" />
    </c:when>

    <c:otherwise>
        <c:choose>
            <c:when test="${form.isTemplate}">
                <c:choose>
                    <c:when test="${isPostMailing}">
                        <c:set var="agnNavigationKey" value="templateView_post" scope="request" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="agnNavigationKey" value="templateView" scope="request" />
                    </c:otherwise>
                </c:choose>
                <c:set var="agnTitleKey" 			value="Template" 				scope="request" />
                <c:set var="agnSubtitleKey" 		value="Template" 				scope="request" />
                <c:set var="sidemenu_sub_active"	value="Templates" 				scope="request" />

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${form.mailingId}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
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
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
                <c:set var="agnTitleKey" 			value="Mailing" 							scope="request" />
                <c:set var="agnSubtitleKey" 		value="Mailing" 							scope="request" />
                <c:set var="sidemenu_sub_active"	value="Mailings"	 						scope="request" />
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <c:choose>
        <c:when test="${form.isTemplate}">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Preview"/>
            </emm:instantiate>
        </c:when>
        <c:otherwise>
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Preview"/>
            </emm:instantiate>
        </c:otherwise>
    </c:choose>
</emm:instantiate>

<jsp:include page="/WEB-INF/jsp/mailing/mailing-actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${form.mailingId}"/>
    <jsp:param name="isTemplate" value="${form.isTemplate}"/>
    <jsp:param name="workflowId" value="${form.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
</jsp:include>
