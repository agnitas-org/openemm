<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="userFormId" type="java.lang.Integer"--%>
<%--@elvariable id="userFormName" type="java.lang.String"--%>

<c:url var="switchDesignUrl" value="/webform/statistic.action?formId=${userFormId}"         scope="request" />

<c:set var="agnNavigationKey" 		value="formViewWithLinks" 							    scope="request" />
<c:set var="agnTitleKey" 			value="Form" 											scope="request" />
<c:set var="agnSubtitleKey" 		value="Form" 											scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 											scope="request" />
<c:set var="sidemenu_sub_active" 	value="workflow.panel.forms" 							scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 										scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 											scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Forms" 											scope="request" />
<c:set var="agnHelpKey" 			value="formStatistic" 									scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="user-form-id" value="${userFormId}"/>
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
            <c:when test="${userFormId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="New_Form"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${userFormName}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/webform/${userFormId}/view.action"/>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Statistics"/>
    </emm:instantiate>
</emm:instantiate>
