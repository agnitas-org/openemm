<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="com.agnitas.web.MailingBaseAction" %>
<%@ page import="org.agnitas.web.MailingSendAction" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="mailingSendForm" type="com.agnitas.web.ComMailingSendForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<emm:CheckLogon/>
<emm:Permission token="mailing.show"/>

<c:set var="ACTION_VIEW_SEND" value="<%= MailingSendAction.ACTION_VIEW_SEND %>"	scope="request"/>
<c:set var="BASE_ACTION_LIST" value="<%= MailingBaseAction.ACTION_LIST %>" 		scope="request"/>
<c:set var="BASE_ACTION_VIEW" value="<%= MailingBaseAction.ACTION_VIEW %>" 		scope="request"/>

<c:set var="mailingId" value="${mailingSendForm.mailingID}"/>
<c:set var="shortname" value="${mailingSendForm.shortname}"/>
<c:set var="gridTemplateId" value="${mailingSendForm.templateId}"/>

<c:url var="mailingsOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="false"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="templatesOverviewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_LIST}"/>
    <c:param name="isTemplate" value="true"/>
    <c:param name="page" value="1"/>
</c:url>

<c:url var="mailingViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingId}"/>
    <c:param name="keepForward" value="true"/>
    <c:param name="init" value="true"/>
</c:url>

<c:url var="templateViewLink" value="/mailingbase.do">
    <c:param name="action" value="${BASE_ACTION_VIEW}"/>
    <c:param name="mailingID" value="${mailingId}"/>
</c:url>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Preview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl"	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="agnHelpKey" 			value="mailingPreview" 		    scope="request" />


<c:choose>
    <c:when test="${mailingSendForm.isMailingGrid}">
        <c:set var="isTabsMenuShown" value="false" scope="request"/>

        <c:set var="CONTEXT" value="${pageContext.request.contextPath}" scope="request"/>

        <emm:include page="/WEB-INF/jsp/mailing/fragments/mailing-grid-navigation.jsp"/>

        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="templateID" value="${gridTemplateId}"/>
            <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingId}"/>
        </emm:instantiate>

        <c:set var="agnTitleKey" 			value="Mailing" 														scope="request" />
        <c:set var="sidemenu_sub_active"	value="none" 															scope="request" />
    </c:when>

    <c:otherwise>
        <c:choose>
            <c:when test="${mailingSendForm.isTemplate}">
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
                    <c:set target="${agnNavHrefParams}" property="mailingID" value="${mailingBaseForm.mailingID}"/>
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
        <c:when test="${mailingSendForm.isTemplate}">
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

<jsp:include page="actions-dropdown.jsp">
    <jsp:param name="elementIndex" value="0"/>
    <jsp:param name="mailingId" value="${mailingSendForm.mailingID}"/>
    <jsp:param name="isTemplate" value="${mailingSendForm.isTemplate}"/>
    <jsp:param name="workflowId" value="${mailingSendForm.workflowId}"/>
    <jsp:param name="isMailingUndoAvailable" value="${mailingSendForm.isMailingUndoAvailable}"/>
</jsp:include>
