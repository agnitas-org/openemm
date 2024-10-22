<%@ page language="java" contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/error.action"%>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<%--@elvariable id="adminRightsForm" type="com.agnitas.emm.core.admin.form.AdminRightsForm"--%>

<c:set var="RESTORE_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESTORE_PARAM_NAME%>"/>

<emm:CheckLogon/>
<emm:Permission token="admin.setpermission"/>

<c:set var="agnNavigationKey" 		value="admin" scope="request"/>
<c:set var="agnTitleKey" 			value="settings.Admin" 					scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.Admin" 					scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 					scope="request" />
<c:set var="agnHighlightKey" 		value="UserRights" 						scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 					scope="request" />
<c:set var="agnHelpKey" 			value="userRights" 						scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="adminID" value="${adminRightsForm.adminID}"/>
</emm:instantiate>

<emm:sideMenuAdditionalParam name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.Admin"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/admin/list.action">
                <c:param name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>
                <c:param name="restoreSort" value="true"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${adminRightsForm.username}"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/admin/${adminRightsForm.adminID}/view.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="UserRights"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="admin.setpermission">
        <c:url var="saveUrl" value="/admin/${adminRightsForm.adminID}/rights/save.action"/>
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element}" property="extraAttributes" value="data-form-url='${saveUrl}' data-form-target='#PermissionForm' data-form-submit"/>
            <c:set target="${element}" property="iconBefore" value="icon-save"/>
            <c:set target="${element}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
