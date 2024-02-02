<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export"/>

<c:url var="listUrl" value="/export/list.action"/>

<c:set var="agnTitleKey" 			value="export" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="export" 				scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport"	 	scope="request" />
<c:set var="sidemenu_sub_active" 	value="export" 				scope="request" />
<c:set var="agnHighlightKey" 		value="export"				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 		scope="request" />
<c:set var="agnHelpKey" 			value="export" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="export"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${listUrl}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="button.Finish"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element0}" property="iconBefore" value="icon-reply"/>
        <c:set target="${element0}" property="type" value="href"/>
        <c:set target="${element0}" property="url">
            <c:url value="/export/${id}/view.action"/>
        </c:set>
        <c:set target="${element0}" property="name">
            <mvc:message code="button.Back"/>
        </c:set>
    </jsp:useBean>
</jsp:useBean>
