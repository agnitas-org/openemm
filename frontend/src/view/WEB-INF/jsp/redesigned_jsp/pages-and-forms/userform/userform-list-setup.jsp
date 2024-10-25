<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.imports.web.ImportController" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="USER_FORM" value="<%= ImportController.ImportType.USER_FORM %>" />

<c:set var="agnTitleKey" 			value="workflow.panel.forms" 	scope="request" />
<c:set var="sidemenu_active" 		value="Forms"			 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="Forms" 	                scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 		scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 					scope="request" />
<c:set var="agnHelpKey" 			value="formList" 				scope="request" />
<c:set var="agnEditViewKey" 	    value="userforms-overview"      scope="request" />

<emm:ShowByPermission token="forms.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/webform/new.action"/></c:set>
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
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

            <c:set target="${element}" property="cls" value="mobile-hidden"/>
            <c:set target="${element}" property="iconBefore" value="icon-wrench"/>
            <c:set target="${element}" property="name"><mvc:message code="action.Action"/></c:set>

            <emm:instantiate var="optionList" type="java.util.LinkedHashMap">
                <c:set target="${element}" property="dropDownItems" value="${optionList}"/>
            </emm:instantiate>

            <%-- Items for dropdown --%>
            <emm:ShowByPermission token="forms.import">
                <emm:instantiate var="option" type="java.util.LinkedHashMap">
                    <c:set target="${optionList}" property="1" value="${option}"/>
                    <c:set target="${option}" property="url"><c:url value="/import/file.action?type=${USER_FORM}"/></c:set>
                    <c:set target="${option}" property="extraAttributes" value="data-confirm"/>
                    <c:set target="${option}" property="name"><mvc:message code="forms.import"/></c:set>
                </emm:instantiate>
            </emm:ShowByPermission>
        </emm:instantiate>
    </emm:instantiate>
</emm:instantiate>
