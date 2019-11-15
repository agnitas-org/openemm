<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://ajaxtags.org/tags/ajax" prefix="ajax" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>

<c:set var="agnNavigationKey" 		value="none" 												scope="request" />
<c:set var="agnTitleKey" 			value="PushNotifications" 									scope="request" />
<c:set var="agnSubtitleKey" 		value="PushNotifications" 									scope="request" />
<c:set var="sidemenu_active" 		value="PushNotifications" 									scope="request" />
<c:set var="sidemenu_sub_active" 	value="default.Overview" 									scope="request" />
<c:set var="agnHighlightKey" 		value="default.Overview" 									scope="request" />
<c:set var="isBreadcrumbsShown" 	value="true" 												scope="request" />
<c:set var="agnBreadcrumbsRootKey" 	value="PushNotifications" 									scope="request" />
<c:set var="agnHelpKey" 			value="Manage_create_send_and_evaluate_push_notifications" 	scope="request" />
