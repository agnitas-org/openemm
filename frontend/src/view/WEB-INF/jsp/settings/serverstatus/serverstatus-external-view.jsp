<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<%--@elvariable id="sendDiagnosisResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="jobStartResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="sendTestmailResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="serverStatus" type="com.agnitas.emm.core.serverstatus.bean.ServerStatus"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="layoutdir" type="java.lang.String"--%>
<c:if test="${not empty layoutdir and layoutdir != 'assets/core'}">
    <s:message var="title" code="serverStatus.statusExternal.header" text="${title}"/>
</c:if>

<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<sec:csrfMetaTags />

		<title>${title}</title>
	
		<link rel="shortcut icon" href="<c:url value="/favicon.ico"/>">
	
		<tiles:insertTemplate template="/WEB-INF/jsp/assets.jsp" />
	</head>
	<body class="sysstat__container">
		<mvc:form servletRelativeAction="/serverstatus/externalView.action" method="post" modelAttribute="serverStatusForm" data-form="resource">
			<div class="sysstat">
				<div class="sysstat__header">
					<div class="sysstat__header__content">
						<div class="sysstat__header__logo">
							<a href="/logonOld.action"><img class="sysstat__header__logo_img" src="/assets/core/images/serverstatus/logo_combined.png" alt="AGNITAS AG"/></a>
						</div>
						<div class="sysstat__header__label">
							<mvc:message code="settings.server.status" />
						</div>
					</div>
				</div>
				<div class="sysstat__body">
				<c:set var="overallStatus" value="${serverStatus.overallStatus}"/>
					<div class="sysstat__body__overall sysstat__body__overall--${overallStatus ? 'success' : 'warning'}">
						<div class="sysstat__body__overall__icon">
							<i class="icon-fa5 icon-fa5-${overallStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
						</div>
						<div class="sysstat__body__overall__label">
							<mvc:message code="serverStatus.statusExternal.overall.${overallStatus ? 'success' : 'warning'}" />
						</div>
					</div>
					<!-- Jobqueue -->
					<c:set var="jobQueueStatus" value="${serverStatus.jobQueueStatus}"/>
					<div class="sysstat__body__statitem sysstat__body__statitem--${jobQueueStatus ? 'success' : 'warning'}">
						<div class="sysstat__body__statitem__label__container">
							<div class="sysstat__body__statitem__icon">
								<i class="icon-fa5 icon-fa5-${jobQueueStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
							</div>
							<div class="sysstat__body__statitem__label sysstat__body__statitem__label--${jobQueueStatus ? 'success' : 'warning'}">
								<mvc:message code="serverStatus.statusExternal.jobqueue" />
							</div>
						</div>
						<div class="sysstat__body__statitem__desc">
							<div class="sysstat__body__statitem__desc__text">
								<mvc:message code="serverStatus.statusExternal.jobqueue.${jobQueueStatus ? 'success' : 'error'}" />
							</div>
						</div>
					</div>
					<!-- Reports -->
					<c:set var="reportStatus" value="${serverStatus.reportStatus}"/>
					<div class="sysstat__body__statitem ${reportStatus ? 'sysstat__body__statitem--success' : 'sysstat__body__statitem--warning'}">
						<div class="sysstat__body__statitem__label__container">
							<div class="sysstat__body__statitem__icon">
								<i class="icon-fa5 icon-fa5-${reportStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
							</div>
							<div class="sysstat__body__statitem__label sysstat__body__statitem__label--${reportStatus ? 'success' : 'warning'}">
								<mvc:message code="serverStatus.statusExternal.reports" />
							</div>
						</div>
						<div class="sysstat__body__statitem__desc">
							<div class="sysstat__body__statitem__desc__text">
								<mvc:message code="serverStatus.statusExternal.reports.${reportStatus ? 'success' : 'error'}" />
							</div>
						</div>
					</div>
					<!-- Imports -->
					<c:set var="importStatus" value="${serverStatus.importStatus}"/>
					<div class="sysstat__body__statitem sysstat__body__statitem--${importStatus ? 'success' : 'warning'}">
						<div class="sysstat__body__statitem__label__container">
							<div class="sysstat__body__statitem__icon">
								<i class="icon-fa5 icon-fa5-${importStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
							</div>
							<div class="sysstat__body__statitem__label sysstat__body__statitem__label--${importStatus ? 'success' : 'warning'}">
								<mvc:message code="serverStatus.statusExternal.imports" />
							</div>
						</div>
						<div class="sysstat__body__statitem__desc">
							<div class="sysstat__body__statitem__desc__text">
								<mvc:message code="serverStatus.statusExternal.imports.${importStatus ? 'success' : 'error'}" />
							</div>
						</div>
					</div>
					<!-- DB Status -->
					<c:set var="dbStatus" value="${serverStatus.dbStatus}"/>
					<div class="sysstat__body__statitem sysstat__body__statitem--${dbStatus ? 'success' : 'warning'}">
						<div class="sysstat__body__statitem__label__container">
							<div class="sysstat__body__statitem__icon">
								<i class="icon-fa5 icon-fa5-${dbStatus ? 'check-circle' : 'exclamation-triangle'}"></i>
							</div>
							<div class="sysstat__body__statitem__label sysstat__body__statitem__label--${dbStatus ? 'success' : 'warning'}">
								<mvc:message code="serverStatus.statusExternal.dbstatus" />
							</div>
						</div>
						<div class="sysstat__body__statitem__desc">
							<div class="sysstat__body__statitem__desc__text">
								<mvc:message code="serverStatus.statusExternal.dbstatus.${dbStatus ? 'success' : 'error'}" />
							</div>
						</div>
					</div>
					<!-- DB Connection -->
					<c:set var="dbConnectStatus" value="${serverStatus.dbConnectStatus}"/>
					<div class="sysstat__body__statitem sysstat__body__statitem--${dbConnectStatus ? 'success' : 'error'}">
						<div class="sysstat__body__statitem__label__container">
							<div class="sysstat__body__statitem__icon">
								<i class="icon-fa5 icon-fa5-${dbConnectStatus ? 'check-circle' : 'ban'}"></i>
							</div>
							<div class="sysstat__body__statitem__label sysstat__body__statitem__label--${dbConnectStatus ? 'success' : 'error'}">
								<mvc:message code="serverStatus.statusExternal.dbconnection" />
							</div>
						</div>
						<div class="sysstat__body__statitem__desc">
							<div class="sysstat__body__statitem__desc__text">
								<mvc:message code="serverStatus.statusExternal.dbconnection.${dbConnectStatus ? 'success' : 'error'}" />
							</div>
						</div>
					</div>
					<%@ include file="fragments/serverstatus-external-footer.jspf" %>
				</div>
				<div class="sysstat__page-footer"><p><mvc:message code="serverStatus.general.version"/>: ${appVersion}</p></div>
			</div>
		</mvc:form>
	</body>
</html>
