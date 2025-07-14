<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action"%>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="sendDiagnosisResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="jobStartResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="sendTestmailResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="serverStatus" type="com.agnitas.emm.core.serverstatus.bean.ServerStatus"--%>
<%--@elvariable id="checkDbSchemaAllowed" type="java.lang.Boolean"--%>

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
		<div class="tile-body vstack gap-3 js-scrollable">
			<mvc:form data-form="resource" servletRelativeAction="/serverstatus/updatecheck.action">
				<button type="button" class="btn btn-primary w-100" data-form-submit>
					<mvc:message code="GWUA.checkForNewVersion" />
				</button>
			</mvc:form>

			<c:if test="${checkDbSchemaAllowed}">
				<a href="<c:url value="/serverstatus/schema/check.action" />" type="button" class="btn btn-primary" data-confirm>
					<mvc:message code="GWUA.checkDbSchema" />
				</a>
			</c:if>

			<emm:ShowByPermission token="master.dbschema.snapshot.create">
				<a href="<c:url value="/serverstatus/db-schema/download.action" />" class="btn btn-primary" data-prevent-load>
					<mvc:message code="GWUA.dbSchema.download" />
				</a>
			</emm:ShowByPermission>

			<a href="<c:url value="/serverstatus/config/download.action" />" class="btn btn-primary">
				<mvc:message code="settings.download.config" />
			</a>

			<mvc:form cssClass="grid" cssStyle="--bs-columns: 2; grid-template-columns: 1fr min-content; --bs-gap: 5px; row-gap: 15px" modelAttribute="serverStatusForm" data-form="resource">
				<div>
					<mvc:text path="jobStart" cssClass="form-control" />
				</div>
				<div>
					<button type="button" class="btn btn-primary w-100" data-form-url="<c:url value="/serverstatus/job/start.action" />" data-form-submit>
						<span class="text-truncate"><mvc:message code="settings.job.start" /></span>
					</button>
				</div>

				<div>
					<mvc:text path="sendTestEmail" cssClass="form-control" placeholder="${emailPlaceholder}" />
				</div>
				<div>
					<button type="button" class="btn btn-primary w-100" data-form-url="<c:url value="/serverstatus/testemail/send.action" />" data-form-submit>
						<span class="text-truncate"><mvc:message code="settings.testmail.send" /></span>
					</button>
				</div>

				<div>
					<mvc:text path="sendDiagnosis" cssClass="form-control" placeholder="${emailPlaceholder}" />
				</div>
				<div>
					<button type="button" class="btn btn-primary w-100" data-form-url="<c:url value="/serverstatus/diagnosis/show.action" />" data-form-submit>
						<span class="text-truncate"><mvc:message code="settings.diagnosis.send" /></span>
					</button>
				</div>
			</mvc:form>

			<mvc:form servletRelativeAction="/serverstatus/licensedata/licenseupload.action" modelAttribute="licenceDataUploadForm" data-form="resource" enctype="multipart/form-data">
				<div class="tile tile--sm tile--highlighted">
					<div class="tile-header">
						<h3 class="tile-title"><mvc:message code="serverStatus.general.licenseDataUpload" /></h3>
					</div>
					<div class="tile-body vstack gap-3">
						<div class="d-flex gap-1">
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
			</mvc:form>

			<mvc:form id="dbSchema-upload-form" servletRelativeAction="/serverstatus/db-schema/upload.action" data-form="resource" enctype="multipart/form-data" method="POST">
				<div class="tile tile--sm tile--highlighted">
					<div class="tile-header">
						<h3 class="tile-title"><mvc:message code="GWUA.dbSchema.upload" /></h3>
					</div>
					<div class="tile-body d-flex gap-1">
						<div class="flex-grow-1">
							<input type="file" name="file" class="form-control" accept=".json" data-field="required">
						</div>
						<button type="button" class="btn btn-icon btn-primary" data-form-confirm>
							<i class="icon icon-cloud-upload-alt"></i>
						</button>
					</div>
				</div>
			</mvc:form>

			<mvc:form data-form="resource" modelAttribute="serverStatusForm">
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
								<button type="button" class="btn btn-primary w-100" data-form-url="<c:url value="/serverstatus/config/view.action" />" data-form-submit>
									<mvc:message code="button.Show" />
								</button>
							</div>

							<div class="col">
								<button type="button" class="btn btn-primary w-100" data-form-url="<c:url value="/serverstatus/config/save.action"/>" data-form-submit>
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
