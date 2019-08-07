<%@page import="com.agnitas.motd.web.MOTDAction"%>
<%@ page language="java" import="org.agnitas.util.*" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
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
		var userMessageContainer = document.getElementById("header_usermessage");
		var reminderRequest = new XMLHttpRequest();
		var requestUrl = "motd.do;jsessionid=0?method=showUserReminderMessage&company_id=<%= Integer.toString(AgnUtils.getCompanyID(request)) %>&admin_id=<%= Integer.toString(AgnUtils.getAdmin(request).getAdminID()) %>&host=<%= AgnUtils.getHostName() %>&token=<%= MOTDAction.calculateSignatureToken(AgnUtils.getCompanyID(request), AgnUtils.getAdmin(request).getAdminID()) %>";
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
		setTimeout(function() {
			reloadTime = 60000; // following calls: 1 minute delay
			self.scheduleNextRequestUserMessages();
			
			loadAndShowReminderMessages();
			
			var popupRequest = new XMLHttpRequest();
			var requestUrl = "motd.do;jsessionid=0?method=showUserPopupMessage&company_id=<%= Integer.toString(AgnUtils.getCompanyID(request)) %>&admin_id=<%= Integer.toString(AgnUtils.getAdmin(request).getAdminID()) %>&host=<%= AgnUtils.getHostName() %>&token=<%= MOTDAction.calculateSignatureToken(AgnUtils.getCompanyID(request), AgnUtils.getAdmin(request).getAdminID()) %>";
			popupRequest.open("POST", requestUrl, false);
			popupRequest.send();
			if (popupRequest.status == 200 && !isBlank(popupRequest.responseText)) {
				alert(popupRequest.responseText);
			}
		}, reloadTime);
	}
</script>
