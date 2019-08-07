<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComMailingContentAction" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingContentForm" type="com.agnitas.web.ComMailingContentForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="ACTION_LIST" 					value="<%= MailingBaseAction.ACTION_LIST %>"							scope="request" />
<c:set var="ACTION_VIEW" 					value="<%= MailingBaseAction.ACTION_VIEW %>"							scope="request" />
<c:set var="ACTION_ADD_TEXTBLOCK" 			value="<%= ComMailingContentAction.ACTION_ADD_TEXTBLOCK %>" 			scope="request" />
<c:set var="ACTION_GENERATE_TEXT_FROM_HTML"	value="<%= ComMailingContentAction.ACTION_GENERATE_TEXT_FROM_HTML %>"	scope="request" />
<c:set var="ACTION_VIEW_CONTENT" 			value="<%= ComMailingContentAction.ACTION_VIEW_CONTENT %>" 				scope="request" />
<c:set var="ACTION_ADD_TEXTBLOCK_AND_BACK" 	value="<%= ComMailingContentAction.ACTION_ADD_TEXTBLOCK_AND_BACK %>" 	scope="request" />
<c:set var="ACTION_CONFIRM_DELETE" 			value="<%= MailingBaseAction.ACTION_CONFIRM_DELETE %>"					scope="request" />
<c:set var="ACTION_CLONE_AS_MAILING" 		value="<%= MailingBaseAction.ACTION_CLONE_AS_MAILING %>" 				scope="request" />
<c:set var="ACTION_SAVE_TEXTBLOCK" 			value="<%= ComMailingContentAction.ACTION_SAVE_TEXTBLOCK %>"			scope="request" />
<c:set var="ACTION_SAVE_TEXTBLOCK_AND_BACK" value="<%= ComMailingContentAction.ACTION_SAVE_TEXTBLOCK_AND_BACK %>"	scope="request" />

<emm:CheckLogon/>
<emm:Permission token="mailing.content.show"/>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:set var="mailingExists" value="${mailingContentForm.mailingID ne 0}"/>
<c:set var="isTemplate" value="${mailingContentForm.isTemplate}"/>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<c:url var="contentOverviewLink" value="/mailingcontent.do">
    <c:param name="action" value="${ACTION_VIEW_CONTENT}"/>
    <c:param name="mailingID" value="${mailingContentForm.mailingID}"/>
</c:url>

<c:if test="${mailingContentForm.gridTemplateId > 0}">
    <c:set var="isTabsMenuShown" value="false" scope="request"/>
</c:if>

<jsp:include page="/${emm:ckEditorPath(pageContext.request)}/ckeditor-emm-helper.jsp">
    <jsp:param name="toolbarType" value="${emm:isCKEditorTrimmed(pageContext.request) ? 'Classic' : 'EMM'}"/>
</jsp:include>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}"	scope="request" />

<c:choose>
    <c:when test="${isTemplate}">
        <c:set var="agnNavigationKey" 		value="templateView" 								scope="request" />
        <c:set var="agnNavHrefAppend" 		value="&mailingID=${mailingContentForm.mailingID}"	scope="request" />
        <c:set var="agnTitleKey" 			value="Template" 									scope="request" />
        <c:set var="agnSubtitleKey" 		value="Template" 									scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 									scope="request" />
        <c:set var="agnHighlightKey" 		value="default.Content" 							scope="request" />
        <c:set var="agnHelpKey" 			value="contentView" 								scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingContentForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="default.Content"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${contentOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingContentForm.dynName}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnTitleKey" 			value="Mailing"	scope="request"	/>
        <c:set var="agnSubtitleKey" 		value="Mailing" scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 	scope="request" />
        <c:set var="agnHighlightKey" 		value="default.Content" scope="request" />
      
        <c:choose>
            <c:when test="${mailingContentForm.gridTemplateId > 0}">
                <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

                <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
                    <c:set target="${agnNavHrefParams}" property="templateID" value="${mailingContentForm.gridTemplateId}"/>
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingContentForm.mailingID}"/>
                </emm:instantiate>

                <c:set var="agnHighlightKey" 	value="mailing.TextModules" 																		scope="request" />
                <c:set var="agnHelpKey" 		value="Content_blocks" 																				scope="request" />
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${limitedRecipientOverview}">
                        <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
                    </c:otherwise>
                </c:choose>
                <c:set var="agnNavHrefAppend" 	value="&mailingID=${mailingContentForm.mailingID}"	scope="request" />
                <c:set var="agnHighlightKey" 	value="default.Content" 									scope="request" />
                <c:set var="agnHelpKey" 		value="contentView" 								scope="request" />
            </c:otherwise>
        </c:choose>

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingContentForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="default.Content"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${contentOverviewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="3" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingContentForm.dynName}"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:otherwise>
</c:choose>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <%-- Actions dropdown --%>

    <jsp:include page="actions-dropdown.jsp">
        <jsp:param name="elementIndex" value="0"/>
        <jsp:param name="mailingId" value="${mailingContentForm.mailingID}"/>
        <jsp:param name="isTemplate" value="${mailingContentForm.isTemplate}"/>
        <jsp:param name="workflowId" value="${mailingContentForm.workflowId}"/>
        <jsp:param name="isMailingUndoAvailable" value="${mailingContentForm.isMailingUndoAvailable}"/>
    </jsp:include>

    <c:if test="${mailingExists}">
        <%-- View dropdown --%>

        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="3" value="${element}"/>

            <c:set target="${element}" property="btnCls" value="btn btn-secondary btn-regular dropdown-toggle"/>
            <c:set target="${element}" property="extraAttributes" value="data-toggle='dropdown'"/>
            <c:set target="${element}" property="iconBefore" value="icon-eye"/>
            <c:set target="${element}" property="name">
                <bean:message key="default.View"/>
            </c:set>
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
                <c:set target="${dropDownItem}" property="name">
                    <bean:message key="mailing.content.blockview"/>
                </c:set>
            </emm:instantiate>

            <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="1" value="${dropDownItem}"/>

                <c:set target="${dropDownItem}" property="type" value="radio"/>
                <c:set target="${dropDownItem}" property="radioName" value="view-state"/>
                <c:set target="${dropDownItem}" property="radioValue" value="split"/>
                <c:set target="${dropDownItem}" property="extraAttributes" value="checked data-view='mailingViewBase'"/>
                <c:set target="${dropDownItem}" property="name">
                    <bean:message key="mailing.content.splitview"/>
                </c:set>
            </emm:instantiate>

            <emm:instantiate var="dropDownItem" type="java.util.LinkedHashMap">
                <c:set target="${dropDownItems}" property="2" value="${dropDownItem}"/>

                <c:set target="${dropDownItem}" property="type" value="radio"/>
                <c:set target="${dropDownItem}" property="radioName" value="view-state"/>
                <c:set target="${dropDownItem}" property="radioValue" value="hidden"/>
                <c:set target="${dropDownItem}" property="extraAttributes" value="data-view='mailingViewBase'"/>
                <c:set target="${dropDownItem}" property="name">
                    <bean:message key="mailing.content.hidepreview"/>
                </c:set>
            </emm:instantiate>
        </emm:instantiate>
    </c:if>

    <c:if test="${mailingContentForm.gridTemplateId <= 0}">
    	<%@include file="mailing-content-view-setup-readonly.jspf" %>

        <emm:instantiate var="element1" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="2" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-inverse btn-regular"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-target='#mailingContentForm' data-form-submit='' data-controls-group='save' data-form-set='action:${ACTION_SAVE_TEXTBLOCK}'"/>

            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </emm:instantiate>
    </c:if>
</emm:instantiate>
