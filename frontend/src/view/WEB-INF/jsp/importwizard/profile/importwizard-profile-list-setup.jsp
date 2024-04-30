<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.ImportProfileAction"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="ACTION_START" 			value="<%= ProfileImportAction.ACTION_START %>"				scope="request" />
<c:set var="ACTION_VIEW" 			value="<%= ImportProfileAction.ACTION_VIEW %>" 				scope="request" />
<c:set var="ACTION_NEW" 			value="<%= ImportProfileAction.ACTION_NEW %>"				scope="request" />
<c:set var="ACTION_CONFIRM_DELETE"	value="<%= ImportProfileAction.ACTION_CONFIRM_DELETE %>"	scope="request" />

<c:url var="importWizardLink" value="/newimportwizard.do">
    <c:param name="action" value="${ACTION_START}"/>
</c:url>

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

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
	<emm:ShowByPermission token="import.change">
    	<emm:instantiate var="element" type="java.util.LinkedHashMap">
	        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
	        <c:set target="${element}" property="btnCls" value="btn btn-inverse btn-regular"/>
	        <c:set target="${element}" property="iconBefore" value="icon-plus"/>
	        <c:set target="${element}" property="type" value="href"/>
	        <c:set target="${element}" property="name">
	            <bean:message key="import.NewImportProfile"/>
	        </c:set>
	        <c:set target="${element}" property="url">
	            <c:url value="/importprofile.do">
	                <c:param name="action" value="${ACTION_NEW}"/>
	            </c:url>
	        </c:set>
	    </emm:instantiate>
	</emm:ShowByPermission>
</emm:instantiate>
