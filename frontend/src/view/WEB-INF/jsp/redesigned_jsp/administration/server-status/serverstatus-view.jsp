<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="sendDiagnosisResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="jobStartResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="sendTestmailResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="serverStatus" type="com.agnitas.emm.core.serverstatus.bean.ServerStatus"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:url var="configViewUrl" value="/serverstatus/config/save.action"/>
<c:url var="configSaveUrl" value="/serverstatus/config/view.action"/>
<c:url var="testDiagnosisSendUrl" value="/serverstatus/diagnosis/show.action"/>
<c:url var="configDownloadUrl" value="/serverstatus/config/download.action"/>
<c:url var="jobStartUrl" value="/serverstatus/job/start.action"/>
<c:url var="testMailSendUrl" value="/serverstatus/testemail/send.action"/>

<div id="system-status-overview" class="tiles-container" data-controller="system-status" data-editable-view="${agnEditViewKey}">
	<div class="tiles-block flex-column" style="flex: 1.743">
		<%@ include file="fragments/general-info-tile.jspf" %>

		<div class="tiles-block flex-grow-1">
			<%@ include file="fragments/system-status-tile.jspf" %>
			<%@ include file="fragments/db-status-tile.jspf" %>
		</div>
	</div>

	<div id="settings-tile" class="tile" data-editable-tile>
		<div class="tile-header">
			<h1 class="tile-title text-truncate"><mvc:message code="Settings" /></h1>
		</div>
		<div class="tile-body js-scrollable">
			<div class="row g-3">
				<mvc:form cssClass="col-12" data-form="resource" servletRelativeAction="/serverstatus/updatecheck.action">
					<button type="button" class="btn btn-primary w-100" data-form-submit>
						<mvc:message code="checkForUpdate" />
					</button>
				</mvc:form>

				<div class="col-12">
					<a href="${configDownloadUrl}" class="btn btn-primary w-100">
						<mvc:message code="settings.download.config" />
					</a>
				</div>

				<mvc:form cssClass="col-12 grid" cssStyle="--bs-columns: 2; grid-template-columns: 1fr min-content; --bs-gap: 5px; row-gap: 15px" modelAttribute="serverStatusForm" data-form="resource">
					<mvc:text path="jobStart" id="jobStart" cssClass="form-control" />
					<button type="button" class="btn btn-primary" data-form-url="${jobStartUrl}" data-form-submit>
						<span class="text-truncate"><mvc:message code="settings.job.start" /></span>
					</button>

					<mvc:text path="sendTestEmail" id="testMail" cssClass="form-control" placeholder="${emailPlaceholder}"/>
					<button type="button" class="btn btn-primary" data-form-url="${testMailSendUrl}" data-form-submit>
						<span class="text-truncate"><mvc:message code="settings.testmail.send" /></span>
					</button>

					<mvc:text path="sendDiagnosis" id="diagnosisSend" cssClass="form-control" placeholder="${emailPlaceholder}"/>
					<button type="button" class="btn btn-primary" data-form-url="${testDiagnosisSendUrl}" data-form-submit>
						<span class="text-truncate"><mvc:message code="settings.diagnosis.send" /></span>
					</button>
				</mvc:form>

				<mvc:form cssClass="col-12" servletRelativeAction="/serverstatus/licensedata/licenseupload.action" modelAttribute="licenceDataUploadForm" data-form="resource" enctype="multipart/form-data">
					<div class="tile tile--sm tile--highlighted">
						<div class="tile-header">
							<h3 class="tile-title"><mvc:message code="serverStatus.general.licenseDataUpload" /></h3>
						</div>
						<div class="tile-body">
							<div class="row g-3">
								<div class="col-12 d-flex gap-1">
									<input type="file" name="file" class="form-control" required>
									<button type="submit" class="btn btn-icon btn-primary">
										<i class="icon icon-cloud-upload-alt"></i>
									</button>
								</div>

								<c:if test="${not empty message}">
									<div class="notification-simple notification-simple--lg notification-simple--info">
										<span><strong>Status: </strong>${message}</span>
									</div>
								</c:if>
							</div>
						</div>
					</div>
				</mvc:form>

				<mvc:form cssClass="col-12" data-form="resource" modelAttribute="serverStatusForm">
					<div class="tile tile--sm tile--highlighted">
						<div class="tile-header">
							<h3 class="tile-title"><mvc:message code="settings.Config"/></h3>
						</div>
						<div class="tile-body">
							<div class="row g-3">
								<div class="col">
									<mvc:text path="configForm.companyIdString" cssClass="form-control" placeholder="CompanyID"/>
								</div>

								<div class="col">
									<mvc:text path="configForm.name" cssClass="form-control" placeholder="ConfigName"/>
								</div>

								<div class="col">
									<mvc:text path="configForm.value" cssClass="form-control" placeholder="Value"/>
								</div>

								<div class="col-12">
									<mvc:text path="configForm.description" cssClass="form-control" placeholder="Description"/>
								</div>

								<div class="col">
									<button type="button" class="btn btn-primary w-100" data-form-url="${configSaveUrl}" data-form-submit>
										<mvc:message code="button.Show" />
									</button>
								</div>

								<div class="col">
									<button type="button" class="btn btn-primary w-100" data-form-url="${configViewUrl}" data-form-submit>
										<mvc:message code="button.Save" />
									</button>
								</div>
							</div>
						</div>
					</div>
				</mvc:form>
			</div>
		</div>
	</div>
</div>
