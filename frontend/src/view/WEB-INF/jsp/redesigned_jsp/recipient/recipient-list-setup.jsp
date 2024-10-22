<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey" 		value="recipient_list"         scope="request" />
<c:set var="agnTitleKey" 			value="Recipients" 		       scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 		       scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 	   scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	   scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 		       scope="request" />
<c:set var="agnHelpKey" 			value="recipientList" 	       scope="request" />
<c:set var="agnEditViewKey" 	    value="recipients-overview"    scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="default.Overview"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${newResourceSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="url"><c:url value="/recipient/create.action"/></c:set>
    </emm:instantiate>
</emm:instantiate>
