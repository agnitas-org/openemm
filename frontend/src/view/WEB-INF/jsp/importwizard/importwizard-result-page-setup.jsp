<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.import"/>

<c:set var="ACTION_START" 		value="<%= ProfileImportAction.ACTION_START %>" 						scope="request" />
<c:set var="DOWNLOAD_ACTION"	value="<%= ProfileImportAction.ACTION_DOWNLOAD_CSV_FILE %>" 			scope="request" />
<c:set var="RESULT" 			value="<%= ProfileImportAction.RESULT_TYPE %>" 							scope="request" />
<c:set var="VALID" 				value="<%= ProfileImportAction.RECIPIENT_TYPE_VALID %>" 				scope="request"	/>
<c:set var="INVALID" 			value="<%= ProfileImportAction.RECIPIENT_TYPE_INVALID %>" 				scope="request" />
<c:set var="FIXED" 				value="<%= ProfileImportAction.RECIPIENT_TYPE_FIXED_BY_HAND %>" 		scope="request" />
<c:set var="DUPLICATE" 			value="<%= ProfileImportAction.RECIPIENT_TYPE_DUPLICATE_RECIPIENT %>"	scope="request" />

<c:url var="importWizardLink" value="/newimportwizard.do">
    <c:param name="action" value="${ACTION_START}"/>
</c:url>

<c:set var="agnNavigationKey" 		value="none"		 				scope="request" />
<c:set var="agnTitleKey" 			value="import.UploadSubscribers"	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.UploadSubscribers" 	scope="request" />
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
        <c:set target="${agnBreadcrumb}" property="url" value="${importWizardLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="button.Finish"/>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element0}" property="iconBefore" value="icon-share"/>
        <c:set target="${element0}" property="type" value="href"/>
        <c:set target="${element0}" property="url">
            <c:url value="/recipient/list.action">
                <c:param name="latestDataSourceId" value="${newImportWizardForm.datasourceId}"/>
            </c:url>
        </c:set>
        <c:set target="${element0}" property="name">
            <bean:message key="button.Finish"/>
        </c:set>
    </jsp:useBean>
</jsp:useBean>
