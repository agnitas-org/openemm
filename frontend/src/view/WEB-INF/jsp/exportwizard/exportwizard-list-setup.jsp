<%@ page import="org.agnitas.web.ExportWizardAction"  errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="wizard.export"/>

<c:set var="ACTION_VIEW" value="<%= ExportWizardAction.ACTION_VIEW %>"/>

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
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
	<emm:ShowByPermission token="export.change">
    	<jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
        	<c:set target="${itemActionsSettings}" property="1" value="${element0}"/>
        	<c:set target="${element0}" property="url">
            	<html:rewrite page="/exportwizard.do?action=${ACTION_VIEW}&exportPredefID=0"/>
        	</c:set>
        	<c:set target="${element0}" property="type" value="href"/>
        	<c:set target="${element0}" property="btnCls" value="btn btn-regular btn-inverse"/>
        	<c:set target="${element0}" property="iconBefore" value="icon-plus"/>
        	<c:set target="${element0}" property="name"><bean:message key="button.New"/></c:set>
    	</jsp:useBean>
    </emm:ShowByPermission>
</jsp:useBean>
