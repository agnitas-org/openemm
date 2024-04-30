<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="usergroup" 	        scope="request" />
<c:set var="agnTitleKey" 			value="settings.Usergroup" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.Usergroups" scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Usergroups" scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		scope="request" />
<c:set var="agnHelpKey" 			value="managingUserGroups"	scope="request" />

<emm:ShowByPermission token="role.change">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/administration/usergroup/create.action"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="settings.NewUsergroup"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.Usergroups"/>
    </emm:instantiate>
</emm:instantiate>
