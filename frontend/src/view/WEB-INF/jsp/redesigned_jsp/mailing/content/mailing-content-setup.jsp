<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.mailingcontent.form.MailingContentForm"--%>
<%--@elvariable id="isWorldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>
<%--@elvariable id="isMailingEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>

<c:set var="isMailingEditable" value="${isMailingEditable and isMailingExclusiveLockingAcquired}" scope="request"/>
<c:set var="mailingId" value="${form.mailingID}"/>

<c:set var="agnTitleKey"         value="${form.isTemplate ? 'Template' : 'Mailing'}"           scope="request" />
<c:set var="sidemenu_sub_active" value="${form.isTemplate ? 'Templates' : 'default.Overview'}" scope="request" />
<c:set var="sidemenu_active"     value="Mailings" 				                               scope="request" />
<c:set var="agnEditViewKey"      value="mailing-content-blocks"                                scope="request" />

<%-- Breadcrumbs --%>
<c:set var="agnBreadcrumbsRootKey" value="${form.isTemplate ? 'Templates' : 'Mailings'}" scope="request" />
<c:url var="agnBreadcrumbsRootUrl" value="/mailing/list.action" scope="request">
    <c:param name="forTemplates" value="${form.isTemplate}"/>
</c:url>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${form.shortname}"/>
    </emm:instantiate>
</emm:instantiate>

<%-- Header tabs --%>
<c:choose>
    <c:when test="${form.isTemplate}">
        <c:set var="agnNavigationKey" value="templateView"    scope="request" />
        <c:set var="agnHelpKey"       value="default.Content" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" value="${form.gridTemplateId gt 0 ? 'GridMailingView' : 'mailingView'}"        scope="request" />
        <c:set var="agnHelpKey"       value="${form.gridTemplateId gt 0 ? 'mailingGridTextContent' : 'contentView'}" scope="request" />

        <emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavConditionsParams}" property="isActiveMailing"     value="${isWorldMailingSend}" />
            <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
            <c:set target="${agnNavConditionsParams}" property="isPostMailing"       value="${not empty isPostMailing and isPostMailing}" />
        </emm:instantiate>
    </c:otherwise>
</c:choose>
<c:set var="agnHighlightKey" value="default.Content" scope="request" />
<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
    <c:choose>
        <c:when test="${form.gridTemplateId gt 0}">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${form.gridTemplateId}"/>
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
        <jsp:param name="isTemplate" value="${form.isTemplate}"/>
        <jsp:param name="workflowId" value="${form.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${form.isMailingUndoAvailable}"/>
    </jsp:include>

    <%-- Save btn --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:choose>
            <c:when test="${isMailingEditable}">
                <c:url var="saveUrl" value="/mailing/content/${mailingId}/save.action"/> <%-- data url different for EMC template text modules--%>
                <c:set target="${element}" property="extraAttributes" value="data-url='${saveUrl}' data-action='save'"/>
            </c:when>
            <c:otherwise>
                <c:set target="${element}" property="extraAttributes" value="disabled"/>
            </c:otherwise>
        </c:choose>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name"><mvc:message code="button.Save"/></c:set>
    </emm:instantiate>
</emm:instantiate>
