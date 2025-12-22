<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action"%>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="adminRightsForm" type="com.agnitas.emm.core.admin.form.AdminRightsForm"--%>
<%--@elvariable id="isRestfulUser" type="java.lang.Boolean"--%>

<c:set var="path" value="${isRestfulUser ? 'restfulUser' : 'admin'}"/>

<c:url var="overviewLink" value="/${path}/list.action" />

<c:set var="agnNavigationKey" 		 value="${isRestfulUser ? 'restfulUser' : 'admin'}"            scope="request" />
<c:set var="agnTitleKey" 			 value="settings.${isRestfulUser ? 'RestfulUser' : 'Admin'}"   scope="request" />
<c:set var="sidemenu_active" 		 value="Administration" 					                   scope="request" />
<c:set var="sidemenu_sub_active" 	 value="UserActivitylog.Users" 					               scope="request" />
<c:set var="agnHighlightKey" 		 value="UserRights" 						                   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	 value="UserActivitylog.Users" 					               scope="request" />
<c:set var="agnBreadcrumbsRootUrl" 	 value="${overviewLink}"	                                   scope="request" />
<c:set var="agnHelpKey" 			 value="userRights" 						                   scope="request" />
<c:set var="agnEditViewKey" 	     value="user-permissions" 	                                   scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="adminID" value="${adminRightsForm.adminID}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${adminRightsForm.username}"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/${path}/${adminRightsForm.adminID}/view.action"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="admin.setpermission">
        <c:url var="saveUrl" value="/${path}/${adminRightsForm.adminID}/rights/save.action"/>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-url='${saveUrl}' data-form-target='#PermissionForm' data-form-submit"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
