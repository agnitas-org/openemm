<%@ page language="java" contentType="text/html; charset=utf-8" import="com.agnitas.web.ComEmmActionAction, org.agnitas.web.EmmActionAction" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= EmmActionAction.ACTION_LIST %>" scope="request"/>
<c:set var="ACTION_VIEW" value="<%= EmmActionAction.ACTION_VIEW %>" scope="request"/>
<c:set var="ACTION_NEW" value="<%= EmmActionAction.ACTION_NEW %>" scope="request"/>
<c:set var="ACTION_CONFIRM_DELETE" value="<%= EmmActionAction.ACTION_CONFIRM_DELETE %>" scope="request"/>
<c:set var="ACTION_BULK_CONFIRM_DELETE" value="<%= ComEmmActionAction.ACTION_BULK_CONFIRM_DELETE %>" scope="request"/>

<emm:CheckLogon/>
<emm:Permission token="actions.show"/>
<c:set var="agnNavigationKey" 		value="Action" 				scope="request"/>
<c:set var="agnTitleKey" 			value="Actions" 			scope="request"/>
<c:set var="agnSubtitleKey" 		value="Actions" 			scope="request"/>
<c:set var="sidemenu_active" 		value="TriggerManagement" 	scope="request"/>
<c:set var="sidemenu_sub_active" 	value="Actions" 			scope="request"/>
<c:set var="agnHighlightKey" 		value="default.Overview"	scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey" 	value="TriggerManagement" 	scope="request"/>
<c:set var="agnHelpKey" 			value="actionList" 			scope="request"/>

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-list-alt far"></i> <bean:message key="Actions"/>
</c:set>

<emm:ShowByPermission token="actions.change">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/action.do?action=${ACTION_NEW}'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="action.New_Action"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Actions"/>
    </emm:instantiate>
</emm:instantiate>
