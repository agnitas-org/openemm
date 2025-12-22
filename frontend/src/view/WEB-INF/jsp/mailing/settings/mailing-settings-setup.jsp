<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="isActiveMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistDisabled" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="isPostMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="undoAvailable" type="java.lang.Boolean"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>

<c:set var="isMailingGrid" value="${gridTemplateId > 0}" />
<c:set var="mailingExists" value="${mailingId ne 0}" />

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" scope="request"/>
<c:set var="workflowDriven" value="${workflowParams.workflowId gt 0}"                                    scope="request" />

<c:choose>
    <c:when test="${isTemplate}">
        <c:set var="agnTitleKey" 		 value="Template" 	                                                scope="request" />
        <c:set var="sidemenu_sub_active" value="Templates" 	                                                scope="request" />
        <c:set var="agnHelpKey" 		 value="${mailingExists ? 'mailingGeneralOptions' : 'newTemplate'}" scope="request" />
        <c:set var="agnHighlightKey" 	 value="${mailingExists ? 'Template' : 'mailing.New_Template'}"     scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnTitleKey" 	     value="Mailing" 	                                                   scope="request" />
        <c:set var="agnHelpKey" 	     value="${isMailingGrid ? 'mailingBase' : 'mailingGeneralOptions'}"    scope="request" />
        <c:set var="sidemenu_sub_active" value="default.Overview" 		                                       scope="request" />
        <c:set var="agnHighlightKey"     value="${mailingExists ? 'default.settings' : 'mailing.New_Mailing'}" scope="request" />
    </c:otherwise>
</c:choose>

<c:set var="sidemenu_active" value="Mailings"         scope="request" />
<c:set var="agnEditViewKey"  value="mailing-settings" scope="request" />

<%-- Breadcrumbs --%>
<c:if test="${isTemplate}">
    <c:set var="agnBreadcrumbsRootKey" value="Templates" scope="request" />
</c:if>
<c:url var="agnBreadcrumbsRootUrl" value="/mailing/list.action" scope="request">
    <c:param name="restoreSort" value="true" />
    <c:param name="forTemplates" value="${isTemplate}" />
</c:url>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${mailingExists}">
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingSettingsForm.shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="${isTemplate ? 'mailing.New_Template' : 'mailing.New_Mailing'}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<%-- Header tabs config --%>
<c:if test="${mailingExists}">
    <c:choose>
        <c:when test="${isTemplate}">
            <c:set var="agnNavigationKey" value="${isPostMailing ? 'templateView_post' : 'templateView'}" scope="request" />
        </c:when>
        <c:otherwise>
            <c:set var="agnNavigationKey" value="${isMailingGrid ? 'GridMailingView' : 'mailingView'}" scope="request" />

            <emm:instantiate var="agnNavConditionsParams" type="java.util.LinkedHashMap" scope="request">
                <c:set target="${agnNavConditionsParams}" property="isActiveMailing"     value="${isActiveMailing}" />
                <c:set target="${agnNavConditionsParams}" property="mailinglistDisabled" value="${mailinglistDisabled}" />
                <c:set target="${agnNavConditionsParams}" property="isPostMailing"       value="${not empty isPostMailing and isPostMailing}" />
            </emm:instantiate>
        </c:otherwise>
    </c:choose>

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
</c:if>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>
    <jsp:include page="../mailing-actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingId}"/>
        <jsp:param name="isTemplate" value="${isTemplate}"/>
        <jsp:param name="workflowId" value="${workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${undoAvailable}"/>
    </jsp:include>

    <%-- Save button --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>

        <c:choose>
            <c:when test="${isSettingsReadonly}">
                <c:set target="${element}" property="extraAttributes" value="disabled"/>
            </c:when>
            <c:otherwise>
                <c:set target="${element}" property="extraAttributes" value="data-form-target='#mailingSettingsForm' data-controls-group='save' data-form-submit-event"/>
            </c:otherwise>
        </c:choose>
        <c:set target="${element}" property="iconBefore" value="icon-save"/>
        <c:set target="${element}" property="name">
            <c:choose>
                <c:when test="${mailingExists}">
                    <mvc:message code="button.Save"/>
                </c:when>
                <c:otherwise>
                    <mvc:message code="${isTemplate ? 'button.save.template.create' : 'button.save.mailing.create'}"/>
                </c:otherwise>
            </c:choose>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
