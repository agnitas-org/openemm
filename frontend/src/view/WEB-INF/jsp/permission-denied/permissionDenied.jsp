<%@ page isErrorPage="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<div class="tile tile--notification tile--alert">
	<div class="tile-header">
		<h1 class="tile-title">
			<i class="icon icon-state-alert"></i>
			<span class="text-truncate"><mvc:message code="permission.denied.title"/></span>
		</h1>
	</div>

	<div class="tile-body vstack gap-3">
		<h2><mvc:message code="permission.denied.message"/></h2>

		<p><mvc:message code="permission.denied.message.extended"/> (${firstName} ${fullName}, <a href="mailto:${email}" class="text-link">${email}</a>)</p>

		<button class="btn btn-primary" onclick="window.history.back(); return false;">
			<i class="icon icon-reply"></i>
			<span class="text"><mvc:message code="button.Back"/></span>
		</button>
	</div>
</div>
