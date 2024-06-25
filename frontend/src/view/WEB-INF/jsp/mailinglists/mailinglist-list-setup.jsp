<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="subscriber_list" scope="request" />
<c:set var="agnTitleKey" 			value="Mailinglists" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailinglists" 	scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="Mailinglists" 	scope="request" />
<c:set var="agnHighlightKey" 		value="Mailinglists" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Recipients" 		scope="request" />
<c:set var="agnHelpKey" 			value="mailinglists"	scope="request" />

<emm:ShowByPermission token="mailinglist.change">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/mailinglist/create.action"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="settings.NewMailinglist"/>
    </c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Mailinglists"/>
    </emm:instantiate>
</emm:instantiate>
