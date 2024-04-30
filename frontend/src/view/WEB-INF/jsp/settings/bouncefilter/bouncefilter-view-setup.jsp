<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean"%>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="bounceFilterForm" type="com.agnitas.emm.core.bounce.form.BounceFilterForm"--%>

<c:set var="isBounceFilterExist" value="${bounceFilterForm.id > 0}"/>

<c:set var="isTabsMenuShown" 		value="false" 				scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Mailloop" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		scope="request" />
<c:set var="agnHelpKey" 			value="bounceFilter" 		scope="request" />

<c:choose>
    <c:when test="${isBounceFilterExist}">
        <c:set var="agnNavHrefAppend" 	value="&id=${bounceFilterForm.id}" 	scope="request" />
        <c:set var="agnTitleKey" 		value="settings.Mailloop" 			scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.Mailloop" 			scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.EditMailloop" 		scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnTitleKey" 		value="settings.NewMailloop" 	scope="request" />
        <c:set var="agnSubtitleKey" 	value="settings.NewMailloop" 	scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.NewMailloop" 	scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}"   property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}"    property="textKey" value="settings.Mailloop"/>
        <c:set target="${agnBreadcrumb}"    property="url">
            <c:url value="/administration/bounce/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${isBounceFilterExist}">
                <c:set target="${agnBreadcrumb}" property="text" value="${bounceFilterForm.shortName}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.NewMailloop"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:if test="${isBounceFilterExist}">
        <emm:ShowByPermission token="mailloop.delete">
            <emm:instantiate var="itemSetting" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="0" value="${itemSetting}"/>
                <c:set target="${itemSetting}" property="btnCls" value="btn btn-regular btn-alert"/>
                <c:set target="${itemSetting}" property="extraAttributes"
                       value="data-form-confirm='/administration/${bounceFilterForm.id}/confirmDelete.action' data-form-target='#bounceFilterForm'"/>
                <c:set target="${itemSetting}" property="iconBefore" value="icon-trash-o"/>
                <c:set target="${itemSetting}" property="type" value="button"/>
                <c:set target="${itemSetting}" property="name">
                    <bean:message key="button.Delete"/>
                </c:set>
            </emm:instantiate>
        </emm:ShowByPermission>
    </c:if>

    <emm:ShowByPermission token="mailloop.change">
        <emm:instantiate var="itemSetting" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${itemSetting}"/>
            <c:set target="${itemSetting}" property="btnCls" value="btn btn btn-regular btn-inverse"/>
            <c:set target="${itemSetting}" property="extraAttributes" value="data-form-target='#bounceFilterForm' data-form-submit"/>
            <c:set target="${itemSetting}" property="iconBefore" value="icon-save"/>
            <c:set target="${itemSetting}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
