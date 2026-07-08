<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnTitleKey" 			value="settings.Mailloop" 	              scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		              scope="request" />
<c:set var="sidemenu_sub_active" 	value="settings.Mailloop" 	              scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 	              scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		              scope="request" />
<c:set var="agnHelpKey" 			value="bounceFilter" 		              scope="request" />
<c:set var="agnEditViewKey" 	    value="response-processing-overview" 	  scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}"   property="0"        value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}"    property="textKey"  value="settings.Mailloop"/>
    </emm:instantiate>
</emm:instantiate>

<emm:ShowByPermission token="mailloop.change">
    <emm:instantiate var="newResourceSettings" type="java.util.LinkedHashMap" scope="request">
        <emm:instantiate var="element" type="java.util.LinkedHashMap">
            <c:set target="${newResourceSettings}" property="0" value="${element}"/>
            <c:set target="${element}" property="url"><c:url value="/administration/bounce/new.action"/></c:set>
            <c:set target="${element}" property="extraAttributes" value="data-confirm=''"/>
        </emm:instantiate>
    </emm:instantiate>
</emm:ShowByPermission>
