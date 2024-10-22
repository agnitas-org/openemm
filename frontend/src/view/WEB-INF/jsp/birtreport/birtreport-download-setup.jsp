<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="reportId" type="java.lang.Integer"--%>
<%--@elvariable id="reportShortname" type="java.lang.String"--%>
<%--@elvariable id="backUrl" type="java.lang.String"--%>

<c:set var="isTabsMenuShown" 		value="false" 									scope="request" />
<c:set var="agnNavigationKey" 		value="none" 								    scope="request" />
<c:set var="agnTitleKey" 			value="Reports" 								scope="request" />
<c:set var="agnSubtitleKey" 		value="Reports" 								scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 								scope="request" />
<c:set var="sidemenu_sub_active"	value="Reports" 								scope="request" />
<c:set var="agnHighlightKey" 		value="report.edit" 							scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 									scope="request"/>
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 								scope="request"/>
<c:set var="agnHelpKey" 			value="reports" 								scope="request" />

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="reportId" value="${reportId}"/>
</emm:instantiate>

<c:if test="${reportId ne 0}">
    <jsp:useBean id="itemActionsSettings" class="java.util.LinkedHashMap" scope="request">
        <jsp:useBean id="element0" class="java.util.LinkedHashMap" scope="request">
            <c:set target="${itemActionsSettings}" property="0" value="${element0}"/>
            <c:set target="${element0}" property="btnCls" value="btn btn-regular btn-inverse"/>
            <c:set target="${element0}" property="iconBefore" value="icon-reply"/>
            <c:set target="${element0}" property="type" value="href"/>
            <c:set target="${element0}" property="url">
                <c:if test="${backUrl eq null}">
                    <c:set var="backUrl" value="/statistics/report/${reportId}/view.action" />
                </c:if>
                <c:url value="${backUrl}"/>
            </c:set>
            <c:set target="${element0}" property="name">
                <mvc:message code="button.Back"/>
            </c:set>
        </jsp:useBean>
    </jsp:useBean>
</c:if>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Reports"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/statistics/reports.action?restoreSort=true"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${reportId eq 0}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="report.new"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${reportShortname}"/>
                <c:set target="${agnBreadcrumb}" property="url">
                    <c:url value="/statistics/report/${reportId}/view.action"/>
                </c:set>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="2" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="button.Finish"/>
    </emm:instantiate>
</emm:instantiate>
