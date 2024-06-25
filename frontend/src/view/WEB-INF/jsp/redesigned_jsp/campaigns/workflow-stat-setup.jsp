<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%--<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>--%>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>

<emm:Permission token="workflow.show"/>

<c:if test="${showStatisticsTile}">
    <c:set var="agnNavigationKey" 		value="campaign" 		   scope="request" />
</c:if>
<c:set var="agnTitleKey" 			value="Workflow" 			   scope="request" />
<c:set var="agnHighlightKey" 		value="GWUA.edit.campaign" 	   scope="request" />
<c:set var="agnSubtitleKey" 		value="default.Overview" 	   scope="request" />
<c:set var="sidemenu_active" 		value="Workflow" 			   scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	   scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	   scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Workflow" 			   scope="request" />
<c:set var="agnHelpKey" 			value="workflow" 			   scope="request" />
<c:set var="agnEditViewKey" 	    value="workflow-overview"      scope="request" />
