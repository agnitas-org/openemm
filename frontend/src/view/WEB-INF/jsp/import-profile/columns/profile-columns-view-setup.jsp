<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.import_profile.form.ImportProfileColumnsForm"--%>
<%--@elvariable id="isReadonly" type="java.lang.Boolean"--%>

<c:set var="profileId" value="${form.profileId}"/>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="profileId" value="${profileId}"/>
</emm:instantiate>

<c:set var="agnNavigationKey" 		value="ImportProfile" 			scope="request" />
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
        <c:url var="profilesOverviewLink" value="/import-profile/list.action" />

        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.ProfileAdministration"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${profilesOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:url var="profileViewLink" value="/import-profile/${profileId}/view.action" />

        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="text" value="${form.profileName}"/>
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
	            <c:set target="${element}" property="extraAttributes" value="data-form-target='#importProfileColumnsForm' data-form-submit-event"/>
	            <c:set target="${element}" property="name"><mvc:message code="button.Save"/></c:set>
	        </emm:instantiate>
	        
	        <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:url var="saveAndStartUrl" value="/import-profile/columns/save.action">
                    <c:param name="start" value="true" />
                </c:url>

	            <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
	            <c:set target="${element}" property="btnCls" value="btn btn-inverse btn-regular"/>
	            <c:set target="${element}" property="iconBefore" value="icon icon-save"/>
	            <c:set target="${element}" property="extraAttributes" value="data-form-target='#importProfileColumnsForm' data-form-url='${saveAndStartUrl}' data-form-submit-event" />
	            <c:set target="${element}" property="name"><mvc:message code="button.SaveAndStartImport"/></c:set>
	        </emm:instantiate>
	    </emm:instantiate>
    </emm:ShowByPermission>
</c:if>
