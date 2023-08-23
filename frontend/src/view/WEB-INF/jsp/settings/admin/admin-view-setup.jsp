<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.forms.FormSearchParams" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"      prefix="c" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/common"  prefix="emm" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jsp/spring"  prefix="mvc" %>

<c:set var="RESTORE_SEARCH_PARAM_NAME" value="<%= FormSearchParams.RESTORE_PARAM_NAME%>"/>
<%--@elvariable id="adminForm" type="com.agnitas.emm.core.admin.form.AdminForm"--%>

<emm:CheckLogon/>
<emm:Permission token="admin.show"/>

<c:set var="agnTitleKey" 			value="settings.Admin" 					scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 					scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Admin" 					scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 							scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 					scope="request" />
<c:set var="agnHelpKey" 			value="newUser" 						scope="request" />

<emm:sideMenuAdditionalParam name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="adminID" value="${adminForm.adminID}"/>
</emm:instantiate>

<c:choose>
    <c:when test="${adminForm.adminID ne 0}">
        <c:set var="agnNavigationKey"	value="admin" 				scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.Admin" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.admin.edit" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="admins" 				scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.Admin" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.Admin" 		scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.Admin"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/admin/list.action">
                <c:param name="${RESTORE_SEARCH_PARAM_NAME}" value="true"/>
            </c:url>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${adminForm.adminID eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.New_Admin"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${adminForm.username}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:choose>
        <c:when test="${adminForm.adminID eq 0}">
            <emm:ShowByPermission token="admin.new">
                <c:url var="createUrl" value="/admin/saveNew.action"/>
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                    <c:set target="${element}" property="extraAttributes" value="data-form-url='${createUrl}' data-form-target='#admin-form' data-form-submit"/>
                    <c:set target="${element}" property="iconBefore" value="icon-save"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Create"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:when>
        <c:otherwise>
            <emm:ShowByPermission token="admin.delete">
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:url var="deleteUrl" value="/admin/${adminForm.adminID}/confirmDelete.action"/>
                    <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn btn-regular btn-alert js-confirm"/>
                    <c:set target="${element}" property="url" value="${deleteUrl}"/>
                    <c:set target="${element}" property="iconBefore" value="icon-trash-o"/>
                    <c:set target="${element}" property="type" value="href"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Delete"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>

            <emm:ShowByPermission token="admin.change">
                <c:url var="saveUrl" value="/admin/${adminForm.adminID}/save.action"/>
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                    <c:set target="${element}" property="extraAttributes" value="data-form-url='${saveUrl}' data-form-target='#admin-form' data-form-submit"/>
                    <c:set target="${element}" property="iconBefore" value="icon-save"/>
                    <c:set target="${element}" property="name">
                        <mvc:message code="button.Save"/>
                    </c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </c:otherwise>
    </c:choose>
</emm:instantiate>
