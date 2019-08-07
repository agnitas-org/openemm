<%@ page language="java" import="com.agnitas.web.*" contentType="text/html; charset=utf-8"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="ACTION_VIEW" value="<%= ComTargetAction.ACTION_VIEW %>" scope="request" />

<emm:CheckLogon/>

<emm:Permission token="targets.show"/>

<c:set var="agnNavigationKey" 		value="targets" 							scope="request" />
<c:set var="agnNavHrefAppend" 		value="&targetID=${targetForm.targetID}" 	scope="request" />
<c:set var="agnTitleKey" 			value="target.Target" 						scope="request" />
<c:set var="agnSubtitleKey" 		value="target.Target" 						scope="request" />
<c:set var="agnSubtitleValue" 		value="${targetForm.shortname}" 			scope="request" />
<c:set var="sidemenu_active" 		value="Targetgroups" 						scope="request" />
<c:set var="sidemenu_sub_active" 	value="none" 								scope="request" />
<c:set var="agnHighlightKey" 		value="target.NewTarget" 					scope="request" />
<c:set var="agnHelpKey" 			value="targetGroupList" 					scope="request" />
