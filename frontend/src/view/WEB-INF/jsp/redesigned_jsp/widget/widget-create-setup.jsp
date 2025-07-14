<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnTitleKey" 			value="messenger.widget"    scope="request" />
<c:set var="sidemenu_active" 		value="Forms" 		        scope="request" />
<c:set var="sidemenu_sub_active" 	value="messenger.widget" 	scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="messenger.widget"    scope="request" />
<c:set var="agnHelpKey" 			value="js_widget" 	        scope="request" />
<c:set var="agnEditViewKey" 	    value="widget-config" 	    scope="request" />

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>
        <c:set target="${element}" property="extraAttributes" value="data-action='generate-widget'"/>
        <c:set target="${element}" property="iconBefore" value="icon-magic"/>
        <c:set target="${element}" property="name">
            <mvc:message code="generate"/>
        </c:set>
    </emm:instantiate>
</emm:instantiate>
