<%@ page language="java" pageEncoding="UTF-8" errorPage="/errorRedesigned.action"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>

<div class="d-flex flex-column gap-3">
	<div class="tile tile--sm w-100 h-auto">
		<div class="tile-header">
			<h2 class="tile-title"><mvc:message code="totp.setup.info.headline.twoFactor" /></h2>
		</div>
		<div class="tile-body">
			<p><mvc:message code="GWUA.twoFactorAuthentication.description" /></p>
		</div>
	</div>
	<div class="tile tile--sm w-100 h-auto">
		<div class="tile-header">
			<h2 class="tile-title"><mvc:message code="totp.info.headline.availableApps" /></h2>
		</div>
		<div class="tile-body">
			<p><mvc:message code="totp.info.content.availableApps" /></p>
			<br/>
			<p><mvc:message code="totp.info.content.availableApps.listHeadline" /></p>
			<br/>
			<ul class="text-circle-list">
				<li><mvc:message code="totp.info.content.availableApps.listContent.1" /></li>
				<li><mvc:message code="totp.info.content.availableApps.listContent.2" /></li>
				<li><mvc:message code="totp.info.content.availableApps.listContent.3" /></li>
			</ul>
		</div>
	</div>
	<div class="tile tile--sm w-100 h-auto">
		<div class="tile-header">
			<h2 class="tile-title"><mvc:message code="totp.info.headline.loginProblems" /></h2>
		</div>
		<div class="tile-body">
			<p><b><mvc:message code="totp.info.content.loginProblems.1" /></b></p>
			<br/>
			<p><b><mvc:message code="totp.info.content.loginProblems.listHeadline.1" /></b></p>
			<ul class="text-circle-list">
				<li><mvc:message code="totp.info.content.loginProblems.listContent.1" /></li>
			</ul>
			<br/>
			<p><b><mvc:message code="totp.info.content.loginProblems.listHeadline.2" /></b></p>
			<ul class="text-circle-list">
				<li><mvc:message code="totp.info.content.loginProblems.listContent.2" /></li>
			</ul>
			<br/>
			<p><b><mvc:message code="totp.info.content.loginProblems.listHeadline.3" /></b></p>
			<ul class="text-circle-list">
				<li><mvc:message code="totp.info.content.loginProblems.listContent.3" /></li>
			</ul>
			<br/>
			<p><b><mvc:message code="totp.info.content.loginProblems.listHeadline.4" /></b></p>
			<ul class="text-circle-list">
				<li><mvc:message code="totp.info.content.loginProblems.listContent.4" /></li>
			</ul>
			<br/>
			<p><b><mvc:message code="totp.info.content.loginProblems.2" /></b></p>
		</div>
	</div>
</div>
