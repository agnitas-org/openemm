<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey" 		value="campaign" 		       scope="request" />
<c:set var="agnTitleKey" 			value="Workflow" 			   scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 	           scope="request" />
<c:set var="sidemenu_active" 		value="Workflow" 			   scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Workflow" 			   scope="request" />
<c:set var="agnHelpKey" 			value="workflow" 			   scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="workflowId" value="${id}"/>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="1" value="${element}"/>
        <c:set target="${element}" property="extraAttributes" value="data-form-target='#stat-form' data-form-submit"/>
        <c:set target="${element}" property="iconBefore" value="icon icon-sync"/>
        <c:set target="${element}" property="name"><mvc:message code="button.Refresh" /></c:set>
    </emm:instantiate>
</emm:instantiate>
