<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey"		value="FormsOverview" 		scope="request" />
<c:set var="agnTitleKey" 			value="workflow.panel.forms" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="workflow.panel.forms" 	scope="request" />
<c:set var="sidemenu_active" 		value="Forms"			 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 					scope="request" />
<c:set var="agnHelpKey" 			value="formList" 				scope="request" />

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-list-alt far"></i> <mvc:message code="workflow.panel.forms"/>
</c:set>

<emm:ShowByPermission token="forms.import">
    <c:url var="createNewItemUrl" value="/webform/import.action" scope="request"/>
    <mvc:message var="createNewItemLabel" code="forms.import" scope="request"/>
</emm:ShowByPermission>

<emm:ShowByPermission token="forms.change">
    <c:url var="createNewItemUrl2" value="/webform/new.action" scope="request"/>
    <mvc:message var="createNewItemLabel2" code="New_Form" scope="request"/>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>
