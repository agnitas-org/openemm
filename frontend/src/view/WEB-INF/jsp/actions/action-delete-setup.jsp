<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.util.SafeString, org.agnitas.web.forms.EmmActionForm"  errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="agnNavigationKey" 		value="deleteAction" 						scope="request" />
<c:set var="agnNavHrefAppend" 		value="&actionID=${emmActionForm.actionID}"	scope="request" />
<c:set var="agnTitleKey" 			value="action.Action" 						scope="request" />
<c:set var="agnSubtitleKey" 		value="action.Action" 						scope="request" />
<c:set var="agnSubtitleValue" 		value="${emmActionForm.shortname}" 			scope="request" />
<c:set var="sidemenu_active" 		value="TriggerManagement"					scope="request" />
<c:set var="sidemenu_sub_active"	value="Actions" 							scope="request" />
<c:set var="agnHighlightKey" 		value="action.ActionsDelete" 				scope="request" />
<c:set var="agnHelpKey" 			value="actionList" 							scope="request" />
