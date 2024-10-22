<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<c:set var="agnNavigationKey" 		value="userlogs" 			    scope="request" />
<c:set var="agnTitleKey" 			value="Userlogs"		 		scope="request" />
<c:set var="sidemenu_active" 		value="Administration" 		    scope="request" />
<c:set var="sidemenu_sub_active" 	value="Userlogs"		 		scope="request" />
<c:set var="agnHighlightKey" 		value="settings.RestfulUser" 	scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Administration" 		    scope="request" />
<c:set var="agnHelpKey" 			value="userlog" 			    scope="request" />
<c:set var="agnEditViewKey" 	    value="restful-ual-overview"    scope="request" />

<c:url var="switchDesignUrl" value="/administration/restful-user/activitylog/list.action" scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="Userlogs"/>
    </emm:instantiate>
</emm:instantiate>

<emm:instantiate var="itemActionsSettings" type="java.util.LinkedHashMap" scope="request">
    <c:url var="csvDownloadUrl" value="/administration/restful-user/activitylog/downloadRedesigned.action" />

    <emm:instantiate var="element" type="java.util.LinkedHashMap">
        <c:set target="${itemActionsSettings}" property="0" value="${element}"/>

        <c:set target="${element}" property="extraAttributes" value="data-form-url='${csvDownloadUrl}' data-prevent-load data-form-submit-static data-form-target='#filter-tile'"/>
        <c:set target="${element}" property="iconBefore" value="icon-cloud-download-alt"/>
        <c:set target="${element}" property="name">
            <mvc:message code="export.message.csv" />
        </c:set>
    </emm:instantiate>

</emm:instantiate>
