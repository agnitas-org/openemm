<%@ page language="java" import="org.agnitas.web.*, com.agnitas.reporting.birt.web.*" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_LIST" 		value="<%= ComMailingBIRTStatAction.ACTION_LIST %>" 		scope="request" />
<c:set var="ACTION_MAILINGSTAT" value="<%= ComMailingBIRTStatAction.ACTION_MAILINGSTAT %>" 	scope="request" />

<emm:CheckLogon/>
<emm:Permission token="stats.mailing"/>

<c:set var="agnNavigationKey" 		value="statsMailing" 		scope="request" />
<c:set var="agnTitleKey" 			value="Statistics" 			scope="request" />
<c:set var="agnSubtitleKey" 		value="Statistics" 			scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 			scope="request" />
<c:set var="sidemenu_sub_active" 	value="MailStat" 			scope="request" />
<c:set var="agnHighlightKey" 		value="statistic.MailStat" 	scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 				scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="Statistics" 			scope="request" />
<c:set var="agnHelpKey" 			value="Mailing_statistic2" 	scope="request" />

<emm:instantiate var="agnBreadcrumbs" type="java.util.LinkedHashMap" scope="request">
    <emm:instantiate var="agnBreadcrumb" type="java.util.LinkedHashMap">
        <c:set target="${agnBreadcrumbs}" property="0" value="${agnBreadcrumb}"/>
        <c:set target="${agnBreadcrumb}" property="textKey" value="MailStat"/>
    </emm:instantiate>
</emm:instantiate>
