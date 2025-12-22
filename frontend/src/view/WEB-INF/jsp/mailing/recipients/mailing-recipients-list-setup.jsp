<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="shortname" type="java.lang.String"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<c:url var="mailingViewLink" value="/mailing/${mailingId}/settings.action">
    <c:param name="keepForward" value="true"/>
</c:url>

<c:set var="agnTitleKey" 			value="Mailing" 					 scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 					 scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 			 scope="request" />
<c:set var="agnHighlightKey" 		value="Recipients" 					 scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 					 scope="request" />
<c:url var="agnBreadcrumbsRootUrl" 	value="/mailing/list.action" 	     scope="request" />
<c:set var="agnHelpKey"			    value="Recipients"	                 scope="request" />
<c:set var="agnEditViewKey"         value="mailing-recipients-overview"  scope="request" />

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="agnNavigationKey" value="GridMailingView" scope="request" />

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
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

<emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavConditionsParams}" property="isActiveMailing" value="true" />
    <c:set target="${agnNavConditionsParams}" property="isPostMailing" value="${not empty isPostMailing and isPostMailing}" />
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingId}"/>
        <jsp:param name="isTemplate" value="false"/>
        <jsp:param name="workflowId" value="${workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
    </jsp:include>

    <c:url var="exportUrl" value="/mailing/${mailingId}/recipients/export.action" />
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:set target="${element}" property="cls" value="mobile-hidden" />
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#table-tile' data-form-submit-static data-prevent-load data-form-url='${exportUrl}'"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name">
            <mvc:message code="Export"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
