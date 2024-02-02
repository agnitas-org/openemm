<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<emm:CheckLogon/>

<emm:Permission token="workflow.show"/>

<c:set var="agnNavigationKey"		value="Workflow" 			scope="request" />
<c:set var="agnTitleKey" 			value="Workflow" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="default.Overview" 	scope="request" />
<c:set var="sidemenu_active" 		value="Workflow" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Workflow" 			scope="request" />
<c:set var="agnHelpKey" 			value="workflow" 			scope="request" />

<emm:ShowByPermission token="workflow.change">
    <c:url var="createNewItemUrl" value="/workflow/create.action" scope="request"/>
    <mvc:message code="workflow.new" var="createNewItemLabel" scope="request"/>
</emm:ShowByPermission>
