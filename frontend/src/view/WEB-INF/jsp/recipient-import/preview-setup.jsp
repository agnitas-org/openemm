<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.imports.form.RecipientImportForm"--%>

<c:set var="agnNavigationKey" 		value="none"		 				scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="importStep2" 				scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:url var="importViewLink" value="/recipient/import/view.action"/>

        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.csv_upload"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${importViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Preview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-regular btn-secondary"/>
        <c:set target="${element}" property="iconBefore" value="icon-reply"/>
        <c:set target="${element}" property="type" value="href"/>
        <c:set target="${element}" property="url">
            <c:url value="/recipient/import/view.action">
                <c:param name="profileId" value="${form.profileId}"/>
                <c:param name="attachmentCsvFileID" value="${form.attachmentCsvFileID}"/>
            </c:url>
        </c:set>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Back"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="element1" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element1}" property="extraAttributes" value="data-form-target='#import-form' data-form-submit"/>
        <c:set target="${element1}" property="iconBefore" value="icon-share"/>
        <c:set target="${element1}" property="name">
            <mvc:message code="button.Import_Start"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
