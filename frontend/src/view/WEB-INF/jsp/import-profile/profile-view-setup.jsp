<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.import_profile.form.ImportProfileForm"--%>
<%--@elvariable id="isUserHasPermissionForSelectedMode" type="java.lang.Boolean"--%>

<c:set var="profileId" value="${form.id}" />
<c:set var="profileExists" value="${profileId != 0}" />

<c:set var="sidemenu_active" 		value="ImportExport" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="ProfileAdministration"	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey"	value="ImportExport" 			scope="request" />

<c:choose>
    <c:when test="${profileExists}">
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${agnNavHrefParams}" property="profileId" value="${profileId}"/>
        </emm:instantiate>

		<c:set var="agnNavigationKey" 	value="ImportProfile" 		        scope="request" />
        <c:set var="agnTitleKey" 		value="import.ImportProfile" 		scope="request" />
        <c:set var="agnSubtitleKey" 	value="import.ImportProfile" 		scope="request" />
        <c:set var="agnHighlightKey" 	value="import.EditImportProfile"	scope="request" />
        <c:set var="agnHelpKey" 		value="newImportProfile" 		    scope="request" />
    </c:when>
    <c:otherwise>
		<c:set var="agnNavigationKey"	value="none" 			         scope="request" />
		<c:set var="agnTitleKey"		value="import.NewImportProfile"	 scope="request" />
		<c:set var="agnHelpKey"			value="newImportProfile" 		 scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:url var="profilesOverviewLink" value="/import-profile/list.action" />

        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="import.ProfileAdministration"/>
        <c:set target="${agnBreadcrumb}" property="url" value="${profilesOverviewLink}"/>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${profileExists}">
                <c:set target="${agnBreadcrumb}" property="text" value="${form.name}" />
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="textKey" value="import.NewImportProfile"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:ShowByPermission token="import.delete">
    	<c:if test="${profileExists}">
            <emm:instantiate var="element0" type="java.util.LinkedHashMap" scope="request">
                <c:set target="${itemActionsSettings}" property="1" value="${element0}"/>
                <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-alert"/>
                <c:set target="${element0}" property="extraAttributes" value="data-confirm=''"/>
                <c:set target="${element0}" property="type" value="href"/>
                <c:set target="${element0}" property="url">
                    <c:url value="/import-profile/${form.id}/confirmDelete.action"/>
                </c:set>
                <c:set target="${element0}" property="iconBefore" value="icon-trash-o"/>
                <c:set target="${element0}" property="name">
                    <mvc:message code="button.Delete"/>
                </c:set>
            </emm:instantiate>
    	</c:if>
    </emm:ShowByPermission>

    <emm:ShowByPermission token="import.change">
        <emm:instantiate var="element1" type="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="2" value="${element1}"/>
            <c:set target="${element1}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element1}" property="extraAttributes" value="data-form-set='save: save' data-form-target='#importProfileForm' data-action='save'"/>
            <c:set target="${element1}" property="iconBefore" value="icon-save"/>
            <c:set target="${element1}" property="name">
                <mvc:message code="button.Save"/>
            </c:set>
        </emm:instantiate>
    </emm:ShowByPermission>
</emm:instantiate>
