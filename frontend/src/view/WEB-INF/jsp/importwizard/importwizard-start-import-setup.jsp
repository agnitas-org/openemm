<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="agnNavigationKey" 		value="none" 						scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="import.csv_upload" 			scope="request" />
<c:set var="agnHighlightKey" 		value="import.Wizard" 				scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="importStep1" 				scope="request" />


<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.csv_upload"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="btnCls" value="btn btn-inverse btn-regular"/>
        <c:set target="${element}" property="iconAfter" value="icon icon-share"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#newImportWizardForm' data-form-persist='start_proceed: proceed' data-form-submit-static=''" />
        <c:set target="${element}" property="name">
            <bean:message key="button.Proceed"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
