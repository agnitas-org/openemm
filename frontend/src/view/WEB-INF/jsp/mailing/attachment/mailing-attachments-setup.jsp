<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailing" type="com.agnitas.beans.Mailing"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="isMailingUndoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="isActiveMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>

<c:set var="isMailingGrid" value="${not empty gridTemplateId and gridTemplateId gt 0}" scope="request"/>
<c:set var="isTemplate" value="${mailing.isTemplate}" scope="page" />

<c:set var="agnTitleKey"         value="${isTemplate ? 'Template' : 'Mailing'}"           scope="request" />
<c:set var="sidemenu_active" 	 value="Mailings" 			                              scope="request" />
<c:set var="sidemenu_sub_active" value="${isTemplate ? 'Templates' : 'default.Overview'}" scope="request" />
<c:set var="agnHelpKey"          value="mailingAttachments"                               scope="request" />
<c:set var="agnEditViewKey" 	 value="mailing-attachments" 	                          scope="request" />

<%-- Breadcrumbs --%>
<c:set var="agnBreadcrumbsRootKey" value="${isTemplate ? 'Templates' : 'Mailings'}" scope="request" />
<c:url var="agnBreadcrumbsRootUrl" value="/mailing/list.action" scope="request">
    <c:param name="forTemplates" value="${isTemplate}"/>
</c:url>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${mailing.shortname}"/>
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
<c:set var="agnHighlightKey" value="mailing.Attachments" scope="request" />
<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailing.id}"/>
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
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailing.id}"/>
        <jsp:param name="isTemplate" value="${isTemplate}"/>
        <jsp:param name="workflowId" value="${workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${isMailingUndoAvailable}"/>
    </jsp:include>

    <%-- Save btn --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:set target="${element}" property="cls" value="mobile-hidden" />
        <c:choose>
            <c:when test="${isMailingEditable}">
                <c:url var="saveUrl" value="/mailing/${mailing.id}/attachment/save.action" />
                <c:set target="${element}" property="extraAttributes" value="data-form-target='#table-tile' data-form-submit data-form-url='${saveUrl}'"/>
            </c:when>
            <c:otherwise>
                <c:set target="${element}" property="extraAttributes" value="disabled"/>
            </c:otherwise>
        </c:choose>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Save"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
