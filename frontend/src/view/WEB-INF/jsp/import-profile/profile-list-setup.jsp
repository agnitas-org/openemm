<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="none"		 			scope="request" />
<c:set var="agnTitleKey" 			value="import.ImportProfile"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.ImportProfile" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="ProfileAdministration" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 			scope="request" />
<c:set var="agnHelpKey" 			value="manageProfile" 			scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.ProfileAdministration"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="import.change">
	<c:url var="createNewItemUrl" value="/import-profile/create.action" scope="request"/>
	<mvc:message var="createNewItemLabel" code="import.NewImportProfile" scope="request"/>
</emm:ShowByPermission>
