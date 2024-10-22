<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>

<c:url var="backUrl" value="/recipient/import/view.action" scope="request">
    <c:param name="profileId" value="${form.profileId}"/>
    <c:param name="attachmentCsvFileID" value="${form.attachmentCsvFileID}"/>
</c:url>

<c:set var="agnTitleKey" 			       value="import.UploadSubscribers"	                 scope="request" />
<c:set var="sidemenu_active" 		       value="ImportExport" 				             scope="request" />
<c:set var="sidemenu_sub_active" 	       value="import.csv_upload" 			             scope="request" />
<c:set var="agnHighlightKey" 		       value="import.Wizard" 				             scope="request" />
<c:set var="agnBreadcrumbsRootKey"	       value="import" 				                     scope="request" />
<c:set var="agnHelpKey" 			       value="importStep2" 				                 scope="request" />
<c:set var="agnBreadcrumbsRootUrl"         value="${backUrl}"                                scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.standard"/>
    </emm:instantiate>
</emm:instantiate>
