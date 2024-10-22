<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionsForm"--%>

<c:set var="agnTitleKey" 			value="Actions" 			  scope="request"/>
<c:set var="sidemenu_active" 		value="TriggerManagement" 	  scope="request"/>
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	  scope="request"/>
<c:set var="agnHighlightKey" 		value="default.Overview"	  scope="request"/>
<c:set var="agnBreadcrumbsRootKey" 	value="TriggerManagement" 	  scope="request"/>
<c:set var="agnHelpKey" 			value="actionList" 			  scope="request"/>
<c:set var="agnEditViewKey" 	    value="emm-actions-overview"  scope="request" />

<emm:ShowByPermission token="actions.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/action/new.action"/></c:set>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>
