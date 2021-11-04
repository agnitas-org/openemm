
<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ImportProfileAction" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ page import="com.agnitas.web.ImportProfileColumnsAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>

<emm:CheckLogon/>
<emm:Permission token="mailinglist.show"/>

<c:set var="ACTION_START"	value="<%= ProfileImportAction.ACTION_START %>"	scope="request" />
<c:set var="ACTION_LIST" 	value="<%= ImportProfileAction.ACTION_LIST %>"	scope="request" />
<c:set var="ACTION_VIEW" 	value="<%= ImportProfileAction.ACTION_VIEW %>"	scope="request" />
<c:set var="ACTION_SAVE" value="<%= ImportProfileColumnsAction.ACTION_SAVE %>" scope="request" />
<c:set var="ACTION_SAVE_AND_START" value="<%= ImportProfileColumnsAction.ACTION_SAVE_AND_START %>" scope="request" />

<c:set var="profileId" value="${importProfileColumnsForm.profileId}"/>
<c:set var="shortname" value="${importProfileColumnsForm.profile.name}"/>

<c:url var="importWizardLink" value="/newimportwizard.do">
    <c:param name="action" value="${ACTION_START}"/>
</c:url>

<c:url var="profilesOverviewLink" value="/importprofile.do">
    <c:param name="action" value="${ACTION_LIST}"/>
</c:url>

<c:url var="profileViewLink" value="/importprofile.do">
    <c:param name="action" value="${ACTION_VIEW}"/>
    <c:param name="profileId" value="${profileId}"/>
</c:url>

<c:set var="agnNavigationKey" 		value="ImportProfile" 			scope="request" />
<c:set var="agnNavHrefAppend" 		value="&profileId=${profileId}"	scope="request" />
<c:set var="agnTitleKey" 			value="import.ManageColumns" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="import.ManageColumns" 	scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="ProfileAdministration" 	scope="request" />
<c:set var="agnHighlightKey" 		value="import.ManageColumns" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 			scope="request" />
<c:set var="agnHelpKey" 			value="manageFields" 			scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.ProfileAdministration"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${profilesOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${profileViewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.ManageColumns"/>
    </emm:instantiate>
</emm:instantiate>

<c:if test="${not isReadonly}">
	<emm:ShowByPermission token="import.change">
	    <emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
	        <emm:instantiate var="element" type="java.util.LinkedHashMap">
	            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
	            <c:set target="${element}" property="btnCls" value="btn btn-inverse btn-regular"/>
	            <c:set target="${element}" property="iconBefore" value="icon icon-save"/>
	            <c:set target="${element}" property="extraAttributes" value="data-form-target='#importProfileColumnsForm' data-form-persist='action:${ACTION_SAVE}'  data-form-submit"/>
	            <c:set target="${element}" property="name"><bean:message key="button.Save"/></c:set>
	        </emm:instantiate>
	        
	        <emm:instantiate var="element" type="java.util.LinkedHashMap">
	            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
	            <c:set target="${element}" property="btnCls" value="btn btn-inverse btn-regular"/>
	            <c:set target="${element}" property="iconBefore" value="icon icon-save"/>
	            <c:set target="${element}" property="extraAttributes" value="data-form-target='#importProfileColumnsForm' data-form-persist='action:${ACTION_SAVE_AND_START}' data-form-submit" />
	            <c:set target="${element}" property="name"><bean:message key="button.SaveAndStartImport"/></c:set>
	        </emm:instantiate>
	    </emm:instantiate>
    </emm:ShowByPermission>
</c:if>
