<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<%--@elvariable id="sendDiagnosisResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="jobStartResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="sendTestmailResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="serverStatus" type="com.agnitas.emm.core.serverstatus.bean.ServerStatus"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>
<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
    <mvc:message var="title" code="serverStatus.statusExternal.header" text="${title}"/>
</c:if>

<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">

		<title>${title}</title>
	
		<link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">

		<tiles:insertTemplate template="/WEB-INF/jsp/redesigned_jsp/page-template/assets.jsp"/>
	</head>
	<body>
		<div id="sys-status-external-view">
			<header>
				<div class="header__content">
					<a href="<c:url value="/logon.action"/>">
						<img class="header__logo" src="<c:url value="/assets/core/images/serverstatus/logo_combined.png" />" alt="AGNITAS AG"/>
					</a>
					<h1><mvc:message code="settings.server.status" /></h1>
				</div>
			</header>
			<main>
				<c:set var="overallStatus" value="${serverStatus.overallStatus}" />
				<div class="panel panel--${overallStatus ? 'success' : 'warning'}">
					<i class="icon icon-${overallStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
					<p><mvc:message code="serverStatus.statusExternal.overall.${overallStatus ? 'success' : 'warning'}" /></p>
				</div>

				<!-- Jobqueue -->
				<c:set var="jobQueueStatus" value="${serverStatus.jobQueueStatus}"/>
				<div class="panel panel--status panel--${jobQueueStatus ? 'success' : 'warning'}">
					<div class="panel-head">
						<i class="icon icon-${jobQueueStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
						<div class="panel-title">
							<p><mvc:message code="serverStatus.statusExternal.jobqueue" /></p>
						</div>
					</div>
					<div class="panel-body">
						<mvc:message code="serverStatus.statusExternal.jobqueue.${jobQueueStatus ? 'success' : 'error'}" />
					</div>
				</div>

				<!-- Reports -->
				<c:set var="reportStatus" value="${serverStatus.reportStatus}"/>
				<div class="panel panel--status panel--${reportStatus ? 'success' : 'warning'}">
					<div class="panel-head">
						<i class="icon icon-${reportStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
						<div class="panel-title">
							<p><mvc:message code="serverStatus.statusExternal.reports" /></p>
						</div>
					</div>
					<div class="panel-body">
						<mvc:message code="serverStatus.statusExternal.reports.${reportStatus ? 'success' : 'error'}" />
					</div>
				</div>
				<!-- Imports -->
				<c:set var="importStatus" value="${serverStatus.importStatus}"/>
				<div class="panel panel--status panel--${importStatus ? 'success' : 'warning'}">
					<div class="panel-head">
						<i class="icon icon-${importStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
						<div class="panel-title">
							<p><mvc:message code="serverStatus.statusExternal.imports" /></p>
						</div>
					</div>
					<div class="panel-body">
						<mvc:message code="serverStatus.statusExternal.imports.${importStatus ? 'success' : 'error'}" />
					</div>
				</div>
				<!-- DB Status -->
				<c:set var="dbStatus" value="${serverStatus.dbStatus}"/>
				<div class="panel panel--status panel--${dbStatus ? 'success' : 'warning'}">
					<div class="panel-head">
						<i class="icon icon-${dbStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
						<div class="panel-title">
							<p><mvc:message code="serverStatus.statusExternal.dbstatus" /></p>
						</div>
					</div>
					<div class="panel-body">
						<mvc:message code="serverStatus.statusExternal.dbstatus.${dbStatus ? 'success' : 'error'}" />
					</div>
				</div>
				<!-- DB Connection -->
				<c:set var="dbConnectStatus" value="${serverStatus.dbConnectStatus}"/>
				<div class="panel panel--status panel--${dbConnectStatus ? 'success' : 'alert'}">
					<div class="panel-head">
						<i class="icon icon-${dbConnectStatus ? 'check-circle' : 'ban'}"></i>
						<div class="panel-title">
							<p><mvc:message code="serverStatus.statusExternal.dbconnection" /></p>
						</div>
					</div>
					<div class="panel-body">
						<mvc:message code="serverStatus.statusExternal.dbconnection.${dbConnectStatus ? 'success' : 'error'}" />
					</div>
				</div>

				<%@ include file="fragments/serverstatus-external-footer.jspf" %>
			</main>
			<footer><p><mvc:message code="serverStatus.general.version"/>: ${appVersion}</p></footer>
		</div>
	</body>
</html>
