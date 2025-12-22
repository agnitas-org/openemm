<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c"    uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm"  uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnTitleKey" 			value="recipient.DatasourceId"      scope="request" />
<c:set var="sidemenu_active" 		value="ImportExport" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.DatasourceId" 	    scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 			scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="ImportExport" 				scope="request" />
<c:set var="agnHelpKey" 			value="dataSourceList" 				scope="request" />
<c:set var="agnEditViewKey" 	    value="datasource-overview"         scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.DatasourceId"/>
    </emm:instantiate>
</emm:instantiate>
