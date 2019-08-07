<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.ComEmmActionAction" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= ComEmmActionAction.ACTION_LIST %>"/>

<%--@elvariable id="emmActionForm" type="com.agnitas.web.forms.ComEmmActionForm"--%>

<emm:CheckLogon/>
<emm:Permission token="actions.show"/>

<c:set var="isTabsMenuShown" 		value="true" 								scope="request" />
<c:set var="agnNavigationKey" 		value="ActionEdit" 							scope="request" />
<c:set var="agnNavHrefAppend" 		value="&actionID=${emmActionForm.actionID}"	scope="request" />
<c:set var="agnTitleKey" 			value="Actions" 							scope="request" />
<c:set var="agnSubtitleKey" 		value="Actions" 							scope="request" />
<c:set var="sidemenu_active" 		value="SiteActions" 						scope="request" />
<c:set var="sidemenu_sub_active" 	value="Actions" 							scope="request" />
<c:set var="agnHighlightKey" 		value="default.usedIn" 						scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 								scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="SiteActions" 						scope="request" />
<c:set var="agnHelpKey" 			value="formView" 							scope="request" />
<c:set var="agnHelpKey" 			value="newAction" 							scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Actions"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/action.do">
                <c:param name="action" value="${ACTION_LIST}"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${emmActionForm.shortname}"/>
    </emm:instantiate>
</emm:instantiate>
