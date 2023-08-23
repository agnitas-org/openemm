<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="fieldname" type="java.lang.String"--%>

<c:set var="agnTitleKey" 			value="recipient.fields" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.fields" 	scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.fields" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 			scope="request" />
<c:set var="agnHelpKey" 			value="newProfileField" 	scope="request" />

<c:choose>
    <c:when test="${not empty fieldname}">
        <c:set var="agnNavigationKey" 	value="profiledbEdit" 				    scope="request" />
        <c:set var="agnHighlightKey" 	value="default.usedIn" 	                scope="request" />
        <emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap"  scope="request">
            <c:set target="${agnNavHrefParams}" property="fieldname" value="${fieldname}"/>
        </emm:instantiate>
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 	value="profiledb" 					scope="request" />
        <c:set var="agnHighlightKey" 	value="settings.NewProfileDB_Field" scope="request" />
    </c:otherwise>
</c:choose>

<emm:instantiate var="agnNavHrefParams" type="java.util.LinkedHashMap" scope="request">
    <c:set target="${agnNavHrefParams}" property="fieldname" value="${fieldname}"/>
</emm:instantiate>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.fields"/>
        <c:set target="${agnBreadcrumb}" property="url">
            <c:url value="/profiledbold/profiledb.action"/>
        </c:set>
    </emm:instantiate>

    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:choose>
            <c:when test="${empty fieldname}">
                <c:set target="${agnBreadcrumb}" property="textKey" value="settings.NewProfileDB_Field"/>
            </c:when>
            <c:otherwise>
                <c:set target="${agnBreadcrumb}" property="text" value="${fieldname}"/>
            </c:otherwise>
        </c:choose>
    </emm:instantiate>
</emm:instantiate>
