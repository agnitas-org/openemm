<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>

<c:url var="importLink" value="/recipient/import/view.action">
    <c:param name="profileId" value="${form.profileId}" />
</c:url>

<c:set var="agnNavigationKey" 		value="none" 						scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers"	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="importStep4" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.csv_upload"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${importLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="importing"/>
    </emm:instantiate>
</emm:instantiate>
