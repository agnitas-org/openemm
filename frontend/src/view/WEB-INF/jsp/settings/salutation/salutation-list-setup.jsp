<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.*" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" 			value="<%= SalutationAction.ACTION_LIST %>"/>
<c:set var="ACTION_CONFIRM_DELETE" 	value="<%= SalutationAction.ACTION_CONFIRM_DELETE %>"/>
<c:set var="ACTION_VIEW" 			value="<%= SalutationAction.ACTION_VIEW %>"/>

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
		<html:rewrite page='/salutation.do?action=2&salutationID=0'/>
	</c:set>

	<c:set var="createNewItemLabel" scope="request">
		<bean:message key="default.salutation.shortname"/>
	</c:set>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
		<c:set target="${agnBreadcrumb}" property="textKey" value="settings.FormsOfAddress"/>
	</emm:instantiate>
</emm:instantiate>
