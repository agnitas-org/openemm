<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core"%>

<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="isActiveMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>

<c:set var="agnTitleKey"         value="${isTemplate ? 'Template' : 'Mailing'}"           scope="request" />
<c:set var="sidemenu_active" 	 value="Mailings" 			                              scope="request" />
<c:set var="sidemenu_sub_active" value="${isTemplate ? 'Templates' : 'default.Overview'}" scope="request" />
<c:set var="agnHelpKey" 		 value="mailingLinks" 			                          scope="request" />
<c:set var="agnEditViewKey"      value="mailing-links-overview"                           scope="request" />

<%-- Breadcrumbs --%>
<c:set var="agnBreadcrumbsRootKey" value="${isTemplate ? 'Templates' : 'Mailings'}" scope="request" />
<c:url var="agnBreadcrumbsRootUrl" value="/mailing/list.action" scope="request">
    <c:param name="forTemplates" value="${isTemplate}"/>
</c:url>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${mailingShortname}"/>
    </emm:instantiate>
</emm:instantiate>

<%-- Header tabs --%>
<c:choose>
    <c:when test="${isTemplate}">
        <c:set var="agnNavigationKey" value="templateView" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="${isMailingGrid ? 'GridMailingView' : 'mailingView'}" scope="request" />
        <emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavConditionsParams}" property="isActiveMailing"     value="${isActiveMailing}" />
            <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
        </emm:instantiate>
    </c:otherwise>
</c:choose>
<c:set var="agnHighlightKey" value="mailing.Trackable_Links" scope="request" />
<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
    <c:choose>
        <c:when test="${isMailingGrid}">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
        </c:when>
        <c:otherwise>
            <c:set target="${agnNavHrefParams}" property="init" value="true"/>
        </c:otherwise>
    </c:choose>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingId}"/>
        <jsp:param name="isTemplate" value="${isTemplate}"/>
        <jsp:param name="workflowId" value="${workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
    </jsp:include>

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="2" value="${element}"/>
        <c:choose>
            <c:when test="${isSettingsReadonly}">
                <c:set target="${element}" property="extraAttributes" value="disabled"/>
            </c:when>
            <c:otherwise>
                <c:set target="${element}" property="extraAttributes" value="data-form-target='#trackableLinksForm' data-form-set='everyPositionLink: false' data-form-submit-event"/>
            </c:otherwise>
        </c:choose>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name"><mvc:message code="button.Save"/></c:set>
    </emm:instantiate>
</emm:instantiate>
