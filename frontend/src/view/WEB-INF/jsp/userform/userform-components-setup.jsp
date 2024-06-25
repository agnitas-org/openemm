<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="formId" type="java.lang.Integer"--%>
<%--@elvariable id="userFormInfo" type="java.lang.String"--%>

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 								scope="request" />
<c:set var="agnTitleKey" 			value="workflow.panel.forms" 							scope="request" />
<c:set var="agnSubtitleKey" 		value="workflow.panel.forms" 							scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 											scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 							scope="request" />
<c:set var="agnHighlightKey" 		value="mailing.Graphics_Components" 					scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 											scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 											scope="request" />
<c:set var="agnHelpKey" 			value="formImages" 								        scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${formId}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/webform/list.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${formId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userFormInfo}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/webform/${formId}/components/list.action"/>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="mailing.Graphics_Components"/>
    </emm:instantiate>
</emm:instantiate>
