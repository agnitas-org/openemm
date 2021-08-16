<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionsForm"--%>

<c:set var="agnNavigationKey" 		value="ActionOverview" 				scope="request"/>
<c:set var="agnTitleKey" 			value="Actions" 			scope="request"/>
<c:set var="agnSubtitleKey" 		value="Actions" 			scope="request"/>
<c:set var="sidemenu_active" 		value="TriggerManagement" 	scope="request"/>
<c:set var="sidemenu_sub_active" 	value="Actions" 			scope="request"/>
<c:set var="agnHighlightKey" 		value="default.Overview"	scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request"/>
<c:set var="agnBreadcrumbsRootKey" 	value="TriggerManagement" 	scope="request"/>
<c:set var="agnHelpKey" 			value="actionList" 			scope="request"/>

<c:set var="agnSubtitleValue" scope="request">
    <i class="icon-fa5 icon-fa5-list-alt far"></i> <mvc:message code="Actions"/>
</c:set>

<emm:ShowByPermission token="actions.change">
    <c:url var="createNewItemUrl" value="/action/new.action" scope="request"/>
    <mvc:message var="createNewItemLabel" code="action.New_Action" scope="request"/>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>
