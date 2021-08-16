<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ExportWizardAction" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export" />

<c:set var="ACTION_START_WIZARD" value="<%= ExportWizardAction.ACTION_LIST %>" scope="request" />

<c:url var="exportWizardStartLink" value="/exportwizard.do">
	<c:param name="action" value="${ACTION_START_WIZARD}" />
</c:url>

<c:set var="agnNavigationKey" 		value="subscriber_export" scope="request" />
<c:set var="agnTitleKey" 			value="export" scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" scope="request" />
<c:set var="sidemenu_sub_active" 	value="export" scope="request" />
<c:set var="agnHighlightKey" 		value="export.Wizard" scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" scope="request" />
<c:set var="agnHelpKey" 			value="export" scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}" />
		<c:set target="${agnBreadcrumb}" property="textKey" value="export.Wizard" />
		<c:set target="${agnBreadcrumb}" property="url" value="${exportWizardStartLink}" />
	</emm:instantiate>

	<emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
		<c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}" />
		<c:set target="${agnBreadcrumb}" property="textKey" value="statistics.progress" />
	</emm:instantiate>
</emm:instantiate>
