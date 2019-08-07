<%@ page language="java" contentType="text/html; charset=utf-8"
         buffer="64kb"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>
<emm:Permission token="campaign.autoopt"/>

<c:set var="agnNavigationKey" 		value="Campaign" 									scope="request"/>
<c:set var="agnNavHrefAppend"		value="&campaignID=${optimizationForm.campaignID}"	scope="request"/>
<c:set var="agnTitleKey" 			value="mailing.archive" 							scope="request"/>
<c:set var="sidemenu_active" 		value="Mailings" 									scope="request"/>
<c:set var="sidemenu_sub_active"	value="mailing.archive" 							scope="request"/>
<c:set var="agnHighlightKey" 		value="mailing.autooptimization" 					scope="request"/>
<c:set var="agnHelpKey" 			value="autooptimization" 							scope="request"/>

<c:choose>
    <c:when test="${optimizationForm.optimizationID != 0}">
        <c:set var="agnSubtitleKey" 		value="mailing.autooptimization" 		scope="request"/>
        <c:set var="agnSubtitleValue" 		value="${optimizationForm.shortname}"	scope="request"/>
    </c:when>
    <c:otherwise>
        <c:set var="agnSubtitleKey" 		value="optimization.new" scope="request"/>
    </c:otherwise>

</c:choose>
