<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export"/>

<%--@elvariable id="id" type="java.lang.Integer"--%>
<%--@elvariable id="isManageAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="isOwnColumnsExportAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>

<c:url var="listUrl" value="/export/list.action?restoreSort=true"/>
<c:url var="evaluateUrl" value="/export/${id}/evaluate.action"/>

<c:set var="agnTitleKey" 			value="export" 				scope="request"/>
<c:set var="agnSubtitleKey" 		value="export" 				scope="request"/>
<c:set var="sidemenu_active" 		value="ImportExport" 		scope="request"/>
<c:set var="sidemenu_sub_active" 	value="export" 				scope="request"/>
<c:set var="agnHighlightKey" 		value="export" 				scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 		scope="request"/>
<c:set var="agnHelpKey" 			value="export" 				scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="export"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${listUrl}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${id > 0}">
                <c:set target="${agnBreadcrumb}" property="text" value="${exportForm.shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="export.new_export_profile"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<c:if test="${isManageAllowed}">
    <jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
        <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
            <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:choose>
                <c:when test="${isOwnColumnsExportAllowed}">
                    <c:set target="${element0}" property="extraAttributes" value="data-form-target='#exportForm' data-form-url='${evaluateUrl}' data-action='evaluate'"/>
                </c:when>
                <c:otherwise>
                    <c:set target="${element0}" property="extraAttributes" value="data-form-target='#exportForm' data-form-url='${evaluateUrl}' data-form-submit"/>
                </c:otherwise>
            </c:choose>
            <c:set target="${element0}" property="iconBefore" value="icon-eye"/>
            <c:set target="${element0}" property="name">
                <mvc:message code="Evaluate"/>
            </c:set>
        </jsp:useBean>
        <emm:ShowByPermission token="export.change">
            <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
                <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-secondary"/>
                <c:set target="${element1}" property="iconBefore" value="icon-save"/>
                <c:choose>
                    <c:when test="${isOwnColumnsExportAllowed}">
                        <c:set target="${element1}" property="extraAttributes" value="data-form-target='#exportForm' data-action='save'"/>
                    </c:when>
                    <c:otherwise>
                        <c:set target="${element1}" property="extraAttributes" value="data-form-target='#exportForm' data-form-submit"/>
                    </c:otherwise>
                </c:choose>
                <c:set target="${element1}" property="name">
                    <mvc:message code="button.Save"/>
                </c:set>
            </jsp:useBean>
        </emm:ShowByPermission>
    </jsp:useBean>
</c:if>
