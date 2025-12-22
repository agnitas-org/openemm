<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="agnNavigationKey" 		value="userlogs" 			     scope="request" />
<c:set var="agnTitleKey" 			value="Userlogs"		 		 scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		     scope="request" />
<c:set var="sidemenu_sub_active" 	value="Userlogs"		 		 scope="request" />
<c:set var="agnHighlightKey" 		value="settings.webservice.user" scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		     scope="request" />
<c:set var="agnHelpKey" 			value="userlog" 			     scope="request" />
<c:set var="agnEditViewKey" 	    value="soap-ual-overview"        scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Userlogs"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:url var="csvDownloadUrl" value="/administration/soap-user/activitylog/download.action" />

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="extraAttributes" value="data-form-url='${csvDownloadUrl}' data-prevent-load data-form-submit-static data-form-target='#filter-tile'"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name">
            <mvc:message code="export.message.csv" />
        </c:set>
    </emm:instantiate>
</emm:instantiate>
