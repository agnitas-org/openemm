<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="recipient.show"/>


<c:set var="agnTitleKey" 			value="recipient.Blacklist" scope="request" />
<c:set var="agnSubtitleKey" 		value="recipient.Blacklist" scope="request" />
<c:set var="sidemenu_active" 		value="Recipients" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="recipient.Blacklist" scope="request" />
<c:set var="agnHighlightKey" 		value="recipient.Blacklist" scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Recipients" 			scope="request" />
<c:set var="agnHelpKey" 			value="blacklist" 			scope="request" />
<c:set var="agnNavigationKey"       value="blacklist"           scope="request"/>

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="recipient.Blacklist"/>
    </emm:instantiate>
</emm:instantiate>
