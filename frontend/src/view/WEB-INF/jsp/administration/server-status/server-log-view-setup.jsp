<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>

<c:set var="agnNavigationKey"	    value="serverStatusTabs"        scope="request" />
<c:set var="agnTitleKey"            value="settings.server.status"  scope="request" />
<c:set var="sidemenu_active"        value="Administration"          scope="request" />
<c:set var="sidemenu_sub_active"    value="settings.server.status"  scope="request" />
<c:set var="agnHighlightKey"        value="settings.logfile.show"   scope="request" />
<c:set var="agnBreadcrumbsRootKey"  value="Administration"          scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="1" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="settings.server.status"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:url var="downloadUrl" value="/serverstatus/logfile/download.action" />

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="type" value="href"/>
        <c:set target="${element}" property="url" value="${downloadUrl}"/>
        <c:set target="${element}" property="extraAttributes" value="data-prevent-load"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name">
            <mvc:message code="button.Download" />
        </c:set>
    </emm:instantiate>

</emm:instantiate>
