
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@page import="com.agnitas.motd.web.MOTDAction"%>
<%@ page language="java" import="com.agnitas.util.*" contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<div id="header_usermessage" align="left">
</div>
<script type="text/javascript">
	var reloadTime = 5000; // only for the first call: 5 seconds delay

	loadAndShowReminderMessages();
	
	scheduleNextRequestUserMessages();
	
	function isBlank(str) {
	    return (!str || /^\s*$/.test(str));
	}
	
	function loadAndShowReminderMessages() {
		
		<c:url value="motd.do" var="_url">
			<c:param name="method" value="showUserReminderMessage" />
			<c:param name="company_id" value="<%= Integer.toString(AgnUtils.getCompanyID(request)) %>" />
			<c:param name="admin_id" value="<%= Integer.toString(AgnUtils.getAdmin(request).getAdminID()) %>" />
			<c:param name="host" value="<%= AgnUtils.getHostName() %>" />
			<c:param name="token" value="<%= MOTDAction.calculateSignatureToken(AgnUtils.getCompanyID(request), AgnUtils.getAdmin(request).getAdminID()) %>" />
		</c:url>

		var userMessageContainer = document.getElementById("header_usermessage");
		var reminderRequest = new XMLHttpRequest();
		
		var requestUrl = "${_url}";
		reminderRequest.open("POST", requestUrl, false);
		reminderRequest.send();
		while (userMessageContainer.hasChildNodes()) {
			userMessageContainer.removeChild(userMessageContainer.lastChild);
		}
		if (reminderRequest.status == 200 && reminderRequest.responseText.length) {
			userMessageContainer.innerHTML = reminderRequest.responseText;
		}
	}
	
	function scheduleNextRequestUserMessages() {
		
		<c:url value="motd.do" var="_url">
			<c:param name="method" value="showUserPopupMessage" />
			<c:param name="company_id" value="<%= Integer.toString(AgnUtils.getCompanyID(request)) %>" />
			<c:param name="admin_id" value="<%= Integer.toString(AgnUtils.getAdmin(request).getAdminID()) %>" />
			<c:param name="host" value="<%= AgnUtils.getHostName() %>" />
			<c:param name="token" value="<%= MOTDAction.calculateSignatureToken(AgnUtils.getCompanyID(request), AgnUtils.getAdmin(request).getAdminID()) %>" />
		</c:url>
		
		setTimeout(function() {
			reloadTime = 60000; // following calls: 1 minute delay
			self.scheduleNextRequestUserMessages();
			
			loadAndShowReminderMessages();
			
			var popupRequest = new XMLHttpRequest();
			var requestUrl = "${_url}";
			popupRequest.open("POST", requestUrl, false);
			popupRequest.send();
			if (popupRequest.status == 200 && !isBlank(popupRequest.responseText)) {
				alert(popupRequest.responseText);
			}
		}, reloadTime);
	}
</script>
