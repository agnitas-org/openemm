<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="mailing.import|forms.import"/>

<c:set var="agnNavigationKey" 		value="subscriber_import" 			scope="request" />
<c:set var="agnTitleKey" 			value="ImportExport"				scope="request" />
<c:set var="agnSubtitleKey" 		value="import.templates.div" 		scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.templates.div" 		scope="request" />
<c:set var="agnHighlightKey" 		value="import.templates.div" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="import" 						scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="manage.tables.newImport"/>
    </emm:instantiate>
</emm:instantiate>
