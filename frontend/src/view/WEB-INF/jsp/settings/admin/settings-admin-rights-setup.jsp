<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.do"%>
<%@ page import="com.agnitas.web.ComAdminAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" value="<%= ComAdminAction.ACTION_LIST %>"/>
<c:set var="ACTION_VIEW" value="<%= ComAdminAction.ACTION_VIEW %>"/>

<emm:CheckLogon/>
<emm:Permission token="admin.setpermission"/>

<c:set var="agnNavigationKey" 		value="admin" scope="request"/>
<c:set var="agnNavHrefAppend" 		value="&adminID=${adminForm.adminID}" 	scope="request" />
<c:set var="agnTitleKey" 			value="settings.Admin" 					scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.Admin" 					scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 					scope="request" />
<c:set var="agnHighlightKey" 		value="UserRights" 						scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 					scope="request" />
<c:set var="agnHelpKey" 			value="userRights" 						scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.Admin"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/admin.do">
                <c:param name="action" value="${ACTION_LIST}"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${adminForm.username}"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/admin.do">
                <c:param name="action" value="${ACTION_VIEW}"/>
                <c:param name="adminID" value="${adminForm.adminID}"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="UserRights"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="admin.setpermission">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-set='save: save' data-form-target='#PermissionForm' data-form-submit"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
