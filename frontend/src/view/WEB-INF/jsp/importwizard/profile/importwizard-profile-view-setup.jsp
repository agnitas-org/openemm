<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ImportProfileAction" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="importProfileForm" type="org.agnitas.web.forms.ImportProfileForm"--%>
<%--@elvariable id="isUserHasPermissionForSelectedMode" type="java.lang.Boolean"--%>

<emm:CheckLogon/>
<emm:Permission token="mailinglist.show"/>

<c:set var="ACTION_START" value="<%= ProfileImportAction.ACTION_START %>" scope="request" />
<c:set var="ACTION_LIST" value="<%= ImportProfileAction.ACTION_LIST %>" scope="request" />
<c:set var="ACTION_CONFIRM_DELETE" value="<%= ImportProfileAction.ACTION_CONFIRM_DELETE %>" scope="request" />

<c:set var="profileId" value="${importProfileForm.profileId}"/>
<c:set var="profileExists" value="${profileId != 0}"/>
<c:set var="shortname" value="${importProfileForm.profile.name}"/>
<c:set var="isJsonImportAllowed" value="false"/>
<%@include file="json-import-allowed-property.jspf" %>
<c:set var="isPreprocessingAllowed" value="false"/>
<emm:ShowByPermission token="import.preprocessing">
    <c:set var="isPreprocessingAllowed" value="true"/>
</emm:ShowByPermission>

<c:url var="importWizardLink" value="/newimportwizard.do">
    <c:param name="action" value="${ACTION_START}"/>
</c:url>

<c:url var="profilesOverviewLink" value="/importprofile.do">
    <c:param name="action" value="${ACTION_LIST}"/>
</c:url>


<c:set var="sidemenu_active" 		value="ImportExport" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="ProfileAdministration"	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 			scope="request" />

<c:choose>
    <c:when test="${profileExists}">
		<c:set var="agnNavigationKey" 	value="ImportProfile" 		scope="request" />
        <c:set var="agnNavHrefAppend"	value="&profileId=${profileId}" 	scope="request" />
        <c:set var="agnTitleKey" 		value="import.ImportProfile" 		scope="request" />
        <c:set var="agnSubtitleKey" 	value="import.ImportProfile" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="import.EditImportProfile"	scope="request" />
        <c:set var="agnHelpKey" 		value="newImportProfile" 				scope="request" />
    </c:when>
    <c:otherwise>
		<c:set var="agnNavigationKey"	value="none" 			scope="request" />
		<c:set var="agnTitleKey"		value="import.NewImportProfile"	scope="request" />
		<c:set var="agnHelpKey"			value="newImportProfile" 		scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.ProfileAdministration"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${profilesOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${profileExists}">
                <c:set target="${agnBreadcrumb}" property="text" value="${shortname}"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="import.NewImportProfile"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="import.delete">
    	<c:if test="${profileExists}">	
        	<jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
            	<c:set target="${itemActionsSettings}" property="1" value="${element0}"/>
            	<c:set target="${element0}" property="btnCls" value="btn btn-regular btn-alert"/>
            	<c:set target="${element0}" property="extraAttributes" value="data-form-confirm='${ACTION_CONFIRM_DELETE}' data-form-target='#importProfileForm'"/>
            	<c:set target="${element0}" property="iconBefore" value="icon-trash-o"/>
            	<c:set target="${element0}" property="name">
                	<bean:message key="button.Delete"/>
            	</c:set>
        	</jsp:useBean>
    	</c:if>
    </emm:ShowByPermission>

    <c:if test="${(isUserHasPermissionForSelectedMode 
        && (importProfileForm.profile.importProcessActionID == 0 || isPreprocessingAllowed) 
        && (importProfileForm.profile.datatype ne 'JSON' || isJsonImportAllowed)) 
        || importProfileForm.profileId eq 0}">
    <emm:ShowByPermission token="import.change">
        <jsp:useBean id="element1" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="2" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-set='save: save' data-form-target='#importProfileForm' data-action='save'"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <bean:message key="button.Save"/>
            </c:set>
        </jsp:useBean>
    </emm:ShowByPermission>
    </c:if>
</jsp:useBean>
