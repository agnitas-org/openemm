<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm"%>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<emm:CheckLogon/>
<emm:Permission token="admin.show"/>

<c:set var="agnNavigationKey" 		value="admins" 				scope="request" />
<c:set var="agnTitleKey" 			value="settings.RestfulUser" 		scope="request" />
<c:set var="agnSubtitleKey"	 		value="settings.RestfulUser" 		scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 		scope="request" />
<c:set var="agnHighlightKey" 		value="settings.RestfulUser" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		scope="request" />
<c:set var="agnHelpKey" 			value="User" 				scope="request" />

<emm:ShowByPermission token="admin.new">
    <c:url var="createNewItemUrl" value="/restfulUser/create.action" scope="request"/>
    <mvc:message var="createNewItemLabel" code="settings.New_RestfulUser" scope="request"/>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.RestfulUser"/>
    </emm:instantiate>
</emm:instantiate>
