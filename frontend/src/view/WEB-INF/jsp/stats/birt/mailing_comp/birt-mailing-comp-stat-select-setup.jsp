<%@ page import="com.agnitas.reporting.birt.web.ComMailingBIRTStatAction"  errorPage="/error.do" %>
<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_MAILINGSTAT" value="<%= ComMailingBIRTStatAction.ACTION_MAILINGSTAT %>" 	scope="request" />
<c:set var="ACTION_LIST" 		value="<%= ComMailingBIRTStatAction.ACTION_LIST %>" 		scope="request" />

<emm:CheckLogon/>
<emm:Permission token="stats.mailing"/>

<c:set var="agnNavigationKey" 		value="statsCompare" 			scope="request" />
<c:set var="agnTitleKey" 			value="statistic.comparison" 	scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 				scope="request" />
<c:set var="sidemenu_sub_active" 	value="statistic.comparison" 	scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.comparison" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 					scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 				scope="request" />
<c:set var="agnHelpKey" 			value="compareMailings" 		scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="statistic.comparison"/>
    </emm:instantiate>
</emm:instantiate>
