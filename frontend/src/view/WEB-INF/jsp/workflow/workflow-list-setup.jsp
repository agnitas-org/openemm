<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

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

<emm:ShowByPermission token="workflow.edit">
    <c:set var="createNewItemUrl" scope="request">
        <html:rewrite page='/workflow/create.action'/>
    </c:set>
    <c:set var="createNewItemLabel" scope="request">
        <bean:message key="workflow.new"/>
    </c:set>
</emm:ShowByPermission>
