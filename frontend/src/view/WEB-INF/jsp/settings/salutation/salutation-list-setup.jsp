<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.*" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="Salutations" 			scope="request" />
<c:set var="agnTitleKey" 			value="settings.FormsOfAddress" scope="request" />
<c:set var="agnSubtitleKey" 		value="settings.FormsOfAddress" scope="request" />
<c:set var="sidemenu_active" 		value="Mailings" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.FormsOfAddress" scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Mailings" 				scope="request" />
<c:set var="agnHelpKey" 			value="salutationForms" 		scope="request" />

<emm:ShowByPermission token="salutation.change">
	<c:set var="createNewItemUrl" scope="request">
        <c:url value='/salutation/create.action'/>
	</c:set>

	<c:set var="createNewItemLabel" scope="request">
		<mvc:message code="default.salutation.shortname"/>
	</c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
		<c:set target="${agnBreadcrumb}" property="textKey" value="settings.FormsOfAddress"/>
	</emm:instantiate>
</emm:instantiate>
