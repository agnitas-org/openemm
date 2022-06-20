<%@ page language="java" contentType="text/html; charset=utf-8" %>

<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="html"    uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="bean"    uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnTitleKey" 			value="recipient.DatasourceId"      scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.DatasourceId"      scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.DatasourceId" 	    scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 			scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 						scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="dataSourceList" 				scope="request" />
<c:set var="agnNavigationKey" 		value="datasource" 		   		    scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.DatasourceId"/>
    </emm:instantiate>
</emm:instantiate>
