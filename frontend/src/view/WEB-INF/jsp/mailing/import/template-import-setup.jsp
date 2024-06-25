<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.ImportForm"--%>

<c:set var="agnNavigationKey" 	value="none"		scope="request" />
<c:set var="agnTitleKey" 		value="Template" 	scope="request" />
<c:set var="agnSubtitleKey"		value="Templates"	scope="request" />

<c:choose>
	<c:when test="${form.grid}">
		<c:set var="sidemenu_active" 		value="grid.layout.builder"	scope="request" />
		<c:set var="sidemenu_sub_active" 	value="Template" 			scope="request" />
		<c:set var="agnHelpKey" 			value="Edit_template" 		scope="request" />
		<c:set var="agnBreadcrumbsRootKey"	value="grid.layout.builder" scope="request" />
	</c:when>
	<c:otherwise>
		<c:set var="sidemenu_active" 		value="Mailings" 			scope="request" />
		<c:set var="sidemenu_sub_active" 	value="Templates" 			scope="request" />
		<c:set var="agnHighlightKey" 		value="default.Overview"	scope="request" />
		<c:set var="agnHelpKey" 			value="templateImport" 		scope="request" />
		<c:set var="agnBreadcrumbsRootKey"	value="Mailings" 			scope="request" />
	</c:otherwise>
</c:choose>

<c:set var="isBreadcrumbsShown" value="true" scope="request"/>
<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
		<c:set target="${agnBreadcrumb}" property="textKey" value="Templates"/>
	</emm:instantiate>
</emm:instantiate>
