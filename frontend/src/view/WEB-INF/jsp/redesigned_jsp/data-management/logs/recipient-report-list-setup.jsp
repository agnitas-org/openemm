<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			value="statistic.protocol" 	          scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 		          scope="request" />
<c:set var="sidemenu_sub_active" 	value="statistic.protocol" 	          scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.protocol" 	          scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="ImportExport" 		          scope="request" />
<c:set var="agnHelpKey" 			value="Logs" 				          scope="request" />
<c:set var="agnEditViewKey" 	    value="recipient-reports-overview" 	  scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="statistic.protocol"/>
    </emm:instantiate>
</emm:instantiate>
