<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ExportWizardAction" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export"/>

<c:set var="ACTION_SAVE" value="<%= ExportWizardAction.ACTION_SAVE %>" scope="request"/>
<c:set var="ACTION_LIST" value="<%= ExportWizardAction.ACTION_LIST %>" scope="request"/>
<c:set var="ACTION_COLLECT_DATA" value="<%= ExportWizardAction.ACTION_COLLECT_DATA %>" scope="request"/>

<c:url var="linkToExportsList" value="/exportwizard.do?action=${ACTION_LIST}"/>

<%--@elvariable id="exportWizardForm" type="org.agnitas.web.forms.ExportWizardForm"--%>
<c:set var="exportPredefId" value="${exportWizardForm.exportPredefID}"/>

<c:set var="agnNavigationKey" 		value="subscriber_export" 	scope="request"/>
<c:set var="agnTitleKey" 			value="export" 				scope="request"/>
<c:set var="agnSubtitleKey" 		value="export" 				scope="request"/>
<c:set var="sidemenu_active" 		value="ImportExport" 		scope="request"/>
<c:set var="sidemenu_sub_active" 	value="export" 				scope="request"/>
<c:set var="agnHighlightKey" 		value="export" 				scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 		scope="request"/>
<c:set var="agnHelpKey" 			value="export" 				scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="export"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${linkToExportsList}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${exportPredefId > 0}">
                <c:set target="${agnBreadcrumb}" property="text" value="${exportWizardForm.shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="export.new_export_profile"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
        <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-inverse"/>
        <c:set target="${element0}" property="url"/>
        <emm:ShowByPermission token="export.ownColumns">
            <c:set target="${element0}" property="extraAttributes" value="data-form-target='#exportWizardForm' data-form-set='action: ${ACTION_COLLECT_DATA}' data-action='collect-data'"/>
        </emm:ShowByPermission>
        <emm:HideByPermission token="export.ownColumns">
            <c:set target="${element0}" property="extraAttributes" value="data-form-target='#exportWizardForm' data-form-set='action: ${ACTION_COLLECT_DATA}' data-form-submit"/>
        </emm:HideByPermission>
        <c:set target="${element0}" property="iconBefore" value="icon-eye"/>
        <c:set target="${element0}" property="name">
            <bean:message key="Evaluate"/>
        </c:set>
    </jsp:useBean>
    <emm:ShowByPermission token="export.change">
	    <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
	        <c:set target="${itemActionsSettings}" property="1" value="${element1}"/>
	        <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-secondary"/>
	        <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <emm:ShowByPermission token="export.ownColumns">
                <c:set target="${element1}" property="extraAttributes" value="data-form-target='#exportWizardForm' data-form-set='action: ${ACTION_SAVE}' data-action='save'"/>
            </emm:ShowByPermission>
            <emm:HideByPermission token="export.ownColumns">
	            <c:set target="${element1}" property="extraAttributes" value="data-form-target='#exportWizardForm' data-form-set='action: ${ACTION_SAVE}' data-form-submit"/>
            </emm:HideByPermission>
            <c:set target="${element1}" property="name">
	            <bean:message key="button.Save"/>
	        </c:set>
	    </jsp:useBean>
	</emm:ShowByPermission>
</jsp:useBean>
