<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="ACTION_START" value="<%= ProfileImportAction.ACTION_START %>"/>

<c:url var="importWizardLink" value="/newimportwizard.do">
    <c:param name="action" value="${ACTION_START}"/>
</c:url>

<c:set var="size" value="${fn:length(newImportWizardForm.allMailingLists)}" scope="request"/>

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
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.Wizard"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${importWizardLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Preview"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-secondary"/>
        <c:set target="${element0}" property="extraAttributes" value="data-form-set='preview_back: back' data-form-target='#newimportwizard' data-form-submit"/>
        <c:set target="${element0}" property="iconBefore" value="icon-reply"/>
        <c:set target="${element0}" property="name">
            <bean:message key="button.Back"/>
        </c:set>
    </jsp:useBean>
    <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
        <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element1}" property="extraAttributes" value="data-form-set='preview_proceed: proceed' data-form-target='#newimportwizard' data-form-submit"/>
        <c:set target="${element1}" property="iconBefore" value="icon-share"/>
        <c:set target="${element1}" property="name">
            <bean:message key="button.Import_Start"/>
        </c:set>
    </jsp:useBean>
</jsp:useBean>
