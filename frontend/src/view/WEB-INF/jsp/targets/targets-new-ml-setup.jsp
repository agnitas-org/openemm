<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetForm" type="com.agnitas.web.forms.ComTargetForm"--%>

<emm:CheckLogon/>

<emm:Permission token="targets.show"/>

<c:set var="tmpTargetID" value="${targetForm.targetID}" scope="request"/>

<c:set var="agnNavigationKey" 		value="MailinglistNew" 						scope="request" />
<c:set var="agnNavHrefAppend" 		value="&targetID=${targetForm.targetID}" 	scope="request" />
<c:set var="agnTitleKey" 			value="Mailinglists" 						scope="request" />
<c:set var="agnSubtitleKey" 		value="NewMailinglist" 						scope="request" />
<c:set var="agnSubtitleValue" 		value="${targetForm.shortname} test" 		scope="request" />
<c:set var="sidemenu_active" 		value="Mailinglists" 						scope="request" />
<c:set var="sidemenu_sub_active" 	value="NewMailinglist" 						scope="request" />
<c:set var="agnHighlightKey" 		value="settings.NewMailinglist" 			scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupView" 					scope="request" />
