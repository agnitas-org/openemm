<%@ page language="java" import="com.agnitas.reporting.birt.web.forms.*" contentType="text/html; charset=utf-8" buffer="32kb"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="birtStatForm" type="com.agnitas.web.forms.ComBirtStatForm"--%>
<%--@elvariable id="limitedRecipientOverview" type="java.lang.Boolean"--%>

<emm:CheckLogon/>
<emm:Permission token="stats.mailing"/>

<c:choose>
    <c:when test="${limitedRecipientOverview}">
        <c:set var="agnNavigationKey" 		value="mailingView_DisabledMailinglist"     scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="agnNavigationKey" 		value="mailingView"                         scope="request" />
    </c:otherwise>
</c:choose>
<c:set var="agnNavHrefAppend" 		value="&mailingID=${birtStatForm.mailingID}&init=true" 	scope="request" />
<c:set var="agnTitleKey" 			value="Mailing"				 							scope="request" />
<c:set var="agnSubtitleKey" 		value="Mailing" 										scope="request" />
<c:set var="sidemenu_active" 		value="Statistics" 										scope="request" />
<c:set var="sidemenu_sub_active" 	value="MailStat" 										scope="request" />
<c:set var="agnHighlightKey" 		value="Statistics" 										scope="request" />
<c:set var="agnHelpKey" 			value="mailingStatistic" 								scope="request" />
