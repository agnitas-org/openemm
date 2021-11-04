<%@ page language="java" contentType="text/html; charset=utf-8"  buffer="32kb"  errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="subscriber_list" scope="request" />
<c:set var="agnTitleKey" 			value="Recipients" 		scope="request" />
<c:set var="agnSubtitleKey" 		value="Recipients" 		scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.search" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.search" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 		scope="request" />
<c:set var="agnHelpKey" 			value="recipientList" 	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.search"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="recipient.create">
    <c:url var="createNewItemUrl" value="/recipient/create.action" scope="request"/>
    <mvc:message var="createNewItemLabel" code="recipient.NewRecipient" scope="request"/>
</emm:ShowByPermission>
