<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.EmmActionAction" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<c:set var="agnNavigationKey" 		value="Action" 						scope="request" />
<c:set var="agnTitleKey" 			value="Actions"						scope="request" />
<c:set var="agnSubtitleKey" 		value="Actions" 					scope="request" />
<c:set var="sidemenu_active" 		value="TriggerManagement"			scope="request" />
<c:set var="sidemenu_sub_active"	value="Actions" 					scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 			scope="request" />
<c:set var="agnHelpKey" 			value="actionList" 					scope="request" />
