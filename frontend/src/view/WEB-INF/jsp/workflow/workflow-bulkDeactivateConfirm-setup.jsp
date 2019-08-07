<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<emm:CheckLogon/>

<emm:Permission token="workflow.delete"/>

<c:set var="agnNavigationKey" 		value="Workflow" 		scope="request" />
<c:set var="agnTitleKey" 			value="workflow.single" scope="request" />
<c:set var="agnSubtitleKey" 		value="workflow.single" scope="request" />
<c:set var="sidemenu_active" 		value="Workflow" 		scope="request" />
<c:set var="sidemenu_sub_active" 	value="none" 			scope="request" />
<c:set var="agnHighlightKey" 		value="workflow.single" scope="request" />
<c:set var="agnHelpKey" 			value="workflow" 		scope="request" />
