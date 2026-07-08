<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			value="mailloop.inbox" 	                  scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		              scope="request" />
<c:set var="sidemenu_sub_active" 	value="mailloop.inbox" 	                  scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	              scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		              scope="request" />
<c:set var="agnHelpKey" 			value="bounceFilter" 		              scope="request" />
<c:set var="agnEditViewKey" 	    value="response-inbox-overview" 	      scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}"   property="0"        value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}"    property="textKey"  value="mailloop.inbox"/>
    </emm:instantiate>
</emm:instantiate>
