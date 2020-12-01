<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.MailingBaseAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingAttachmentsForm" type="com.agnitas.web.forms.ComMailingAttachmentsForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<c:set var="BASE_ACTION_LIST" value="<%= MailingBaseAction.ACTION_LIST %>"/>
<c:set var="BASE_ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>" />

<emm:CheckLogon/>
<emm:Permission token="mailing.attachments.show"/>

<c:set var="isMailingGrid" value="${isMailingGrid}" scope="request"/>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingAttachmentsForm.mailingID}"/>
</c:url>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingAttachmentsForm.mailingID}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<c:set var="isBreadcrumbsShown" value="true" scope="request"/>
<c:set var="agnBreadcrumbsRootKey" value="Mailings" scope="request"/>
<c:set var="agnBreadcrumbsRootUrl" value="${mailingsOverviewLink}" scope="request"/>

<c:choose>
    <c:when test="${isMailingGrid}">
        <c:set var="isTabsMenuShown" 		value="false" scope="request" />

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${templateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingAttachmentsForm.mailingID}"/>
        </emm:instantiate>

        <c:set var="agnTitleKey" 			value="Mailing" 						scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailing" 						scope="request" />
        <c:set var="sidemenu_active" 		value="Mailings" 						scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 						    scope="request" />
        <c:set var="agnHighlightKey" 		value="mailing.Attachments" 			scope="request" />
        <c:set var="agnHelpKey" 			value="mailingAttachments" 				scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="text" value="${mailingAttachmentsForm.shortname}"/>
                <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
            </emm:instantiate>

            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Attachments"/>
            </emm:instantiate>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${mailingAttachmentsForm.isTemplate}">
                
                <c:set var="agnNavigationKey" 		value="templateView" 									scope="request" />
                <c:set var="agnNavHrefAppend" 		value="&mailingID=${mailingAttachmentsForm.mailingID}"	scope="request" />
                <c:set var="agnTitleKey" 			value="Template" 										scope="request" />
                <c:set var="agnSubtitleKey" 		value="Template" 										scope="request" />
                <c:set var="sidemenu_active" 		value="Mailings" 										scope="request" />
                <c:set var="sidemenu_sub_active"	value="Templates" 										scope="request" />
                <c:set var="agnHighlightKey" 		value="mailing.Attachments" 							scope="request" />
                
                <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
                    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                        <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
                        <c:set target="${agnBreadcrumb}" property="url" value="${templatesOverviewLink}"/>
                    </emm:instantiate>
                    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                        <c:set target="${agnBreadcrumb}" property="text" value="${mailingAttachmentsForm.shortname}"/>
                        <c:set target="${agnBreadcrumb}" property="url" value="${templateViewLink}"/>
                    </emm:instantiate>

                    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
                        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Attachments"/>
                    </emm:instantiate>
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
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingAttachmentsForm.mailingID}"/>
                    <c:set target="${agnNavHrefParams}" property="init" value="true"/>
                </emm:instantiate>
                <c:set var="agnTitleKey" 			value="Mailing" 													scope="request" />
                <c:set var="agnSubtitleKey" 		value="Mailing" 													scope="request" />
                <c:set var="sidemenu_active" 		value="Mailings" 													scope="request" />
                <c:set var="sidemenu_sub_active"	value="none" 														scope="request" />
                <c:set var="agnHighlightKey" 		value="mailing.Attachments" 										scope="request" />
               
                <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
                    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                        <c:set target="${agnBreadcrumb}" property="text" value="${mailingAttachmentsForm.shortname}"/>
                        <c:set target="${agnBreadcrumb}" property="url" value="${mailingViewLink}"/>
                    </emm:instantiate>

                    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
                        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Attachments"/>
                    </emm:instantiate>
                </emm:instantiate>
            </c:otherwise>
        </c:choose>
        <c:set var="agnHelpKey" value="mailingAttachments" scope="request" />
    </c:otherwise>
</c:choose>

<jsp:include page="actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingAttachmentsForm.mailingID}"/>
    <jsp:param name="isTemplate" value="${mailingAttachmentsForm.isTemplate}"/>
    <jsp:param name="workflowId" value="${mailingAttachmentsForm.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${mailingAttachmentsForm.isMailingUndoAvailable}"/>
</jsp:include>
