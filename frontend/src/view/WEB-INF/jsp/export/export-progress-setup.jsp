<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export" />

<c:url var="exportListUrl" value="/export/list.action"/>

<c:set var="agnTitleKey" 			value="export" 				scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="export" 				scope="request" />
<c:set var="agnHighlightKey" 		value="export" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 		scope="request" />
<c:set var="agnHelpKey" 			value="export" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}" />
		<c:set target="${agnBreadcrumb}" property="textKey" value="export" />
		<c:set target="${agnBreadcrumb}" property="url" value="${exportListUrl}" />
	</emm:instantiate>

	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}" />
		<c:set target="${agnBreadcrumb}" property="textKey" value="statistics.progress" />
	</emm:instantiate>
</emm:instantiate>
