<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ExportWizardAction" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export"/>

<c:set var="ACTION_START_WIZARD" value="<%= ExportWizardAction.ACTION_LIST %>" scope="request"/>
<c:set var="ACTION_VIEW" value="<%= ExportWizardAction.ACTION_VIEW %>" scope="request"/>

<c:url var="exportWizardStartLink" value="/exportwizard.do">
    <c:param name="action" value="${ACTION_START_WIZARD}"/>
</c:url>

<c:set var="agnNavigationKey" 		value="subscriber_export"	scope="request"/>
<c:set var="agnTitleKey" 			value="export" 				scope="request"/>
<c:set var="agnSubtitleKey" 		value="export" 				scope="request"/>
<c:set var="sidemenu_active" 		value="ImportExport"	 	scope="request"/>
<c:set var="sidemenu_sub_active" 	value="export" 				scope="request"/>
<c:set var="agnHighlightKey" 		value="export.Wizard"		scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 		scope="request"/>
<c:set var="agnHelpKey" 			value="export" 				scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="export.Wizard"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${exportWizardStartLink}"/>
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
        <c:set target="${element0}" property="iconBefore" value="icon-reply"/>
        <c:set target="${element0}" property="extraAttributes" value="data-form-action='${ACTION_VIEW}' data-form-target='#exportWizardForm'"/>
        <c:set target="${element0}" property="name">
            <bean:message key="button.Back"/>
        </c:set>
    </jsp:useBean>
</jsp:useBean>
