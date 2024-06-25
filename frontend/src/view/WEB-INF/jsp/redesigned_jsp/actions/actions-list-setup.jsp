<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.action.form.EmmActionsForm"--%>

<c:set var="agnNavigationKey" 		value="ActionOverview" 		  scope="request"/>
<c:set var="agnTitleKey" 			value="Actions" 			  scope="request"/>
<c:set var="agnSubtitleKey" 		value="Actions" 			  scope="request"/>
<c:set var="sidemenu_active" 		value="TriggerManagement" 	  scope="request"/>
<c:set var="sidemenu_sub_active" 	value="Actions" 			  scope="request"/>
<c:set var="agnHighlightKey" 		value="default.Overview"	  scope="request"/>
<c:set var="isBreadcrumbsShown" 	value="true" 				  scope="request"/>
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

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">

    <%-- Actions dropdown --%>
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <emm:ShowByPermission token="actions.delete">
            <emm:instantiate var="element" type="java.util.LinkedHashMap">
                <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

                <c:set target="${element}" property="btnCls" value="btn dropdown-toggle"/>
                <c:set target="${element}" property="cls" value="mobile-hidden"/>
                <c:set target="${element}" property="extraAttributes" value="data-bs-toggle='dropdown'"/>
                <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
                <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

                <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                    <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
                </emm:instantiate>

                <%-- Items for dropdown --%>
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:url var="confirmDeleteUrl" value="/action/deleteRedesigned.action"/>
                    <c:set target="${optionList}" property="0" value="${option}"/>
                    <c:set target="${option}" property="url" value="#"/>
                    <c:set target="${option}" property="cls" value="js-data-table-bulk-delete"/>
                    <c:set target="${option}" property="extraAttributes" value="data-table-body='.js-data-table-body' data-bulk-url='${confirmDeleteUrl}'"/>
                    <c:set target="${option}" property="name"><mvc:message code="bulkAction.delete.action"/></c:set>
                </emm:instantiate>
            </emm:instantiate>
        </emm:ShowByPermission>
    </emm:instantiate>
</emm:instantiate>
