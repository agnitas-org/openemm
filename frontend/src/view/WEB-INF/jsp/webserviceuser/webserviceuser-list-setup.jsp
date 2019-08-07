<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey" 		    value="admins" 						scope="request" />
<c:set var="agnTitleKey" 			    value="settings.webservice.user" 	scope="request" />
<c:set var="agnSubtitleKey" 		    value="settings.webservice.user" 	scope="request" />
<c:set var="sidemenu_active" 		    value="Administration" 				scope="request" />
<c:set var="sidemenu_sub_active" 	    value="settings.Admin" 				scope="request" />
<c:set var="agnHighlightKey" 		    value="settings.webservice.user" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	    value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	    value="Administration" 				scope="request" />
<c:set var="formFieldErrorDontShow"     value="true"                	    scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.webservice.user"/>
    </emm:instantiate>
</emm:instantiate>
