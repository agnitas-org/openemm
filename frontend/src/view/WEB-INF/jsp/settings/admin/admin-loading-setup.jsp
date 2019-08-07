<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="com.agnitas.web.ComAdminAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" 			value="<%= ComAdminAction.ACTION_LIST %>" 			scope="request" />
<c:set var="ACTION_VIEW" 			value="<%= ComAdminAction.ACTION_VIEW %>" 			scope="request" />
<c:set var="ACTION_CONFIRM_DELETE" 	value="<%= ComAdminAction.ACTION_CONFIRM_DELETE %>" scope="request" />

<emm:CheckLogon/>
<emm:Permission token="admin.show"/>

<c:set var="agnNavigationKey" 		value="admins" 				scope="request" />
<c:set var="agnTitleKey" 			value="settings.Admin" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.Admin" 		scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 		scope="request" />
<c:set var="agnHighlightKey" 		value="settings.Admin" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		scope="request" />
<c:set var="agnHelpKey" 			value="newUser" 			scope="request" />

<emm:ShowByPermission token="admin.new">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/admin.do?action=2&adminID=0'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="settings.New_Admin"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.Admin"/>
    </emm:instantiate>
</emm:instantiate>
