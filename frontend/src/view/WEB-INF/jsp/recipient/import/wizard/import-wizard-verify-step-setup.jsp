<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.importclassic"/>

<c:set var="agnNavigationKey" 		value="subscriber_import" 			scope="request" />
<c:set var="agnTitleKey" 			value="import.Wizard" 				scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers"	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard"			 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport"		 		scope="request" />
<c:set var="agnHelpKey" 			value="classicImport" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.Wizard"/>
    </emm:instantiate>
</emm:instantiate>
