<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do"%>
<%@ page import="com.agnitas.web.ComMailingBaseAction" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_NEW" 			value="<%= ComMailingBaseAction.ACTION_NEW %>"				scope="request" />
<c:set var="ACTION_MAILING_IMPORT" 	value="<%= ComMailingBaseAction.ACTION_MAILING_IMPORT %>"	scope="request" />

<emm:CheckLogon/>

<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 				scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	value="${mailingsOverviewLink}"	scope="request" />
<c:set var="isNewItemAvailable" 	value="false"					scope="request" />

<c:choose>
    <c:when test="${mailingBaseForm.isTemplate}">
        <c:set var="agnNavigationKey" 		value="templates" 		scope="request" />
        <c:set var="agnTitleKey" 			value="Templates" 		scope="request" />
        <c:set var="agnSubtitleKey" 		value="Templates" 		scope="request" />
        <c:set var="sidemenu_sub_active"	value="Templates" 		scope="request" />
        <c:set var="agnHelpKey" 			value="templateList"	scope="request" />

        <emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
            <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
                <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
                <c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
            </emm:instantiate>
        </emm:instantiate>

		<emm:ShowByPermission token="mailing.import">
			<c:set var="importItemUrl" value="/mailingbase.do?action=${ACTION_MAILING_IMPORT}&isTemplate=true" scope="request"/>
			<c:set var="importItemLabelKey" value="template.import" scope="request"/>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="template.change">
            <c:set var="isNewItemAvailable" value="true"/>
            <c:set var="newItemUrl" value="/mailingbase.do?action=${ACTION_NEW}&mailingID=0&isTemplate=true" scope="request"/>
            <c:set var="newItemLabelKey" value="mailing.New_Template" scope="request"/>
        </emm:ShowByPermission>
    </c:when>
    <c:otherwise>
       
        <c:set var="agnNavigationKey" 		value="MailingsOverview"	scope="request" />
        <c:set var="agnTitleKey" 			value="Mailings" 			scope="request" />
        <c:set var="agnSubtitleKey" 		value="Mailings" 			scope="request" />
        <c:set var="sidemenu_sub_active"	value="default.Overview" 	scope="request" />
        <c:set var="agnHelpKey" 			value="mailingList" 		scope="request" />

		 <emm:ShowByPermission token="mailing.import">
			<c:set var="importItemUrl" value="/mailingbase.do?action=${ACTION_MAILING_IMPORT}" scope="request"/>
			<c:set var="importItemLabelKey" value="mailing.import" scope="request"/>
        </emm:ShowByPermission>
        <emm:ShowByPermission token="mailing.change">
            <c:set var="isNewItemAvailable" value="true"/>
            <c:set var="newItemUrl" value="/mwStart.do?action=init" scope="request"/>
            <c:set var="newItemLabelKey" value="mailing.New_Mailing" scope="request"/>
        </emm:ShowByPermission>
    </c:otherwise>
</c:choose>

<emm:ShowByPermission token="mailing.import">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page="${importItemUrl}"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="${importItemLabelKey}"/>
    </c:set>
</emm:ShowByPermission>

<c:if test="${isNewItemAvailable}">
    <c:set var="createNewItemUrl2" scope="request">
        <html:rewrite page="${newItemUrl}"/>
    </c:set>
    <c:set var="createNewItemLabel2" scope="request">
        <bean:message key="${newItemLabelKey}"/>
    </c:set>
</c:if>

<c:set var="CONTEXT" value="${pageContext.request.contextPath}" scope="request"/>
