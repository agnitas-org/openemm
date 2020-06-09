<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>

<%--@elvariable id="pluginForm" type="com.agnitas.emm.core.pluginmanager.form.PluginForm"--%>

<c:set var="agnNavigationKey" 		value="pluginmanagerView" 		scope="request" />
<c:set var="agnTitleKey" 			value="settings.pluginmanager" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.pluginmanager" 	scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.pluginmanager" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Administration" 			scope="request" />
<c:set var="agnHelpKey" 			value="pluginManagerGeneral" 		scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.pluginmanager"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/administration/pluginmanager/plugins.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${pluginForm.name}"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:choose>
        <c:when test="${pluginForm.active}">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                <c:set target="${element}" property="iconBefore" value="icon-times-circle-o"/>
                <c:set target="${element}" property="type" value="href"/>
                <c:set target="${element}" property="url">
                    <c:url value="/administration/pluginmanager/plugin/${pluginForm.id}/deactivate.action"/>
                </c:set>
                <c:set target="${element}" property="name">
                    <bean:message key="btndeactivate"/>
                </c:set>
            </emm:instantiate>
        </c:when>

        <c:otherwise>
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
                <c:set target="${element}" property="btnCls" value="btn btn-regular btn-inverse"/>
                <c:set target="${element}" property="iconBefore" value="icon-times-circle-o"/>
                <c:set target="${element}" property="type" value="href"/>
                <c:set target="${element}" property="url">
                    <c:url value="/administration/pluginmanager/plugin/${pluginForm.id}/activate.action"/>
                </c:set>
                <c:set target="${element}" property="name">
                    <bean:message key="button.Activate"/>
                </c:set>
            </emm:instantiate>

            <c:if test="${not pluginForm.system}">
                <emm:instantiate var="element" type="java.util.LinkedHashMap">
                    <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
                    <c:set target="${element}" property="btnCls" value="btn btn-regular btn-alert"/>
                    <c:set target="${element}" property="iconBefore" value="icon-trash-o"/>
                    <c:set target="${element}" property="type" value="href"/>
                    <c:set target="${element}" property="url">
                        <c:url value="/administration/pluginmanager/plugin/${pluginForm.id}/uninstall.action"/>
                    </c:set>
                    <c:set target="${element}" property="name">
                        <bean:message key="pluginmanager.plugin.uninstall"/>
                    </c:set>
                </emm:instantiate>
            </c:if>
        </c:otherwise>
    </c:choose>
</emm:instantiate>
