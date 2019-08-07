<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="none" 	        scope="request" />
<c:set var="agnTitleKey" 			value="Mailinglists" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailinglists" 	scope="request" />
<c:set var="sidemenu_active" 		value="Mailinglists" 	scope="request" />
<c:set var="agnHighlightKey" 		value="Mailinglists" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 			scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="Mailinglists" 	scope="request" />
<c:set var="agnHelpKey" 			value="mailinglists"	scope="request" />

<emm:ShowByPermission token="mailinglist.change">
    <c:set var="createNewItemUrl" scope="request">
        <c:url value="/mailinglist/create.action"/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <mvc:message code="settings.NewMailinglist"/>
    </c:set>
</emm:ShowByPermission>
