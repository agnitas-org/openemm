<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="profiledb" 				scope="request" />
<c:set var="agnTitleKey" 			value="recipient.fields" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.fields" 		scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.fields" 		scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 				scope="request" />
<c:set var="agnHelpKey" 			value="Managing profile fields" scope="request" />

<emm:ShowByPermission token="profileField.show">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/profiledbold/new.action" />
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="settings.NewProfileDB_Field"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.fields"/>
    </emm:instantiate>
</emm:instantiate>
