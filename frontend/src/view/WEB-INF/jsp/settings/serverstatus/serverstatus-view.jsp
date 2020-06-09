<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do"%>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>

<%--@elvariable id="sendDiagnosisResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="jobStartResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="sendTestmailResult" type="com.agnitas.service.SimpleServiceResult"--%>
<%--@elvariable id="serverStatus" type="com.agnitas.emm.core.serverstatus.bean.ServerStatus"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<style>
    .status {color: #FFFFFF !important;}
    .status-success {background-color: green !important;}
    .status-error {background-color: red !important;}
    .import_status_ok {display: none;}
    .import_status_error {}
</style>

<mvc:form servletRelativeAction="/serverstatus/view.action" method="post" modelAttribute="serverStatusForm" data-form="resource">
    <div style="line-height:1.8">
        <div class="col-sm-6">
<!-- ----------General Information------------------------------------------------------------- -->
            <div class="tile">
                <div class="tile-header">
                    <div class="headline">
                        <mvc:message code="settings.general.information" />
                    </div>
                </div>
                <div class="table-responsive">
                    <table id="serverStatus_generalInfo" class="table table-bordered table-hover js-table" style="display: table;">
                        <thead>
                            <tr>
                                <th><mvc:message code="Name" /></th>
                                <th><mvc:message code="Value" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>License</td>
                                <td>${serverStatus.licenseName}</td>
                            </tr>
                            <tr>
                                <td>Hostname</td>
                                <td>${serverStatus.hostName}</td>
                            </tr>
                            <tr>
                                <td>Java Version</td>
                                <td>${serverStatus.javaVersion}</td>
                            </tr>
                            <tr>
                                <td>EMM Version</td>
                                <td>${serverStatus.version}</td>
                            </tr>
                            <tr>
                                <td>Temp Directory</td>
                                <td style="white-space: normal;">${serverStatus.tempDir}</td>
                            </tr>
                            <tr>
                                <td>Install Path</td>
                                <td style="white-space: normal;">${serverStatus.installPath}</td>
                            </tr>
                            <tr>
                                <td>System Time</td>
                                <td>${serverStatus.sysTime}</td>
                            </tr>
                            <tr>
                                <td>Build Time</td>
                                <td>${serverStatus.buildTime}</td>
                            </tr>
                            <tr>
                                <td>Start Up Time</td>
                                <td>${serverStatus.startupTime}</td>
                            </tr>
                            <tr>
                                <td>Up Time</td>
                                <td>${serverStatus.uptime}</td>
                            </tr>
                            <tr>
                                <td>Configuration<br>Expiration Time</td>
                                <td>${serverStatus.configExpirationTime}</td>
                            </tr>
                            <tr>
                                <td>DB Type</td>
                                <td>${serverStatus.dbType}</td>
                            </tr>
                            <tr>
                                <td>DB URL</td>
                                <td style="white-space: normal;">${serverStatus.dbUrl}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="tile">
                <div class="tile-content tile-content-forms">
<!-- ----------Updatecheck------------------------------------------------------------- -->
                    <div class="form-group" style="padding:5px; margin-left:-12px !important; margin-right:-12px !important;">
                        <div class="col-sm-12">
                            <c:url var="updatecheckUrl" value="/serverstatus/updatecheck.action"/>
                            <button type="button" class="btn btn-primary btn-regular col-sm-12" data-form-url="${updatecheckUrl}" data-form-submit="" style="white-space: normal; padding:5px">
                            	<mvc:message code="checkForUpdate" />
                            </button>
                        </div>
                    </div>
<!-- ----------Jobstart------------------------------------------------------------- -->
                    <div class="form-group" style="padding:5px; margin-left:-12px !important; margin-right:-12px !important;">
                        <div class="col-sm-7">
                            <mvc:text path="jobStart" id="jobStart" cssClass="form-control" placeholder=""/>
                        </div>
                        <div class="col-sm-5 pull-right">
                            <c:url var="jobStartUrl" value="/serverstatus/job/start.action"/>
                            <button type="button" class="btn btn-primary btn-regular col-sm-12" data-form-url="${jobStartUrl}" data-form-submit="" style="white-space: normal; padding:5px">
                                <mvc:message code="settings.job.start" />
                            </button>
                        </div>
                    </div>
<!-- ----------Send Testmails------------------------------------------------------------- -->
                    <div class="form-group" style="padding:5px; margin-left:-12px !important; margin-right:-12px !important;">
                        <div class="col-sm-7">
                            <mvc:text path="sendTestEmail" id="testMail" cssClass="form-control" placeholder="e-mail"/>
                        </div>
                        <div class="col-sm-5 pull-right">
                            <c:url var="testMailSendUrl" value="/serverstatus/testemail/send.action"/>
                            <button type="button" class="btn btn-primary btn-regular col-sm-12" data-form-url="${testMailSendUrl}" data-form-submit="" style="white-space: normal; padding:5px">
                                <mvc:message code="settings.testmail.send" />
                            </button>
                        </div>
                    </div>
<!-- ----------Diagnostic Information------------------------------------------------------------- -->
                    <div class="form-group" style="padding:5px; margin-left:-12px !important; margin-right:-12px !important;">
                        <div class="col-sm-7">
                            <mvc:text path="sendDiagnosis" id="diagnosisSend" cssClass="form-control" placeholder="e-mail"/>
                        </div>
                        <div class="col-sm-5 pull-right">
                            <c:url var="testDiagnosisSend" value="/serverstatus/diagnosis/show.action"/>
                            <button type="button" class="btn btn-primary btn-regular col-sm-12" data-form-url="${testDiagnosisSend}" data-form-submit="" style="white-space: normal; padding:5px">
                                <mvc:message code="settings.diagnosis.send" />
                            </button>
                        </div>
                    </div>
<!-- ----------Button Group------------------------------------------------------------- -->
                    <div class="form-group vspace-top-20">
                        <div class="row">
                            <div class="col-sm-11">
                                <c:url var="viewJobQueue" value="/serverstatus/jobqueue/view.action"/>
                                <a href="${viewJobQueue}" class="btn btn-regular full-width">
                                    <mvc:message code="settings.jobqueue.show" />
                                </a>
                            </div>
                            <div class="col-sm-1">
                                <button class="icon icon-help" data-help="help_${helplanguage}/settings/serverstatus/ShowJobQueue.xml" type="button" tabindex="-1"></button>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-sm-11">
                                <c:url var="logfileDownloadUrl" value="/serverstatus/logfile/download.action"/>
                                <a href="${logfileDownloadUrl}" data-prevent-load="" class="btn btn-regular full-width"><mvc:message code="settings.logfile.download" /></a>
                            </div>

                            <div class="col-sm-1">
                                <button class="icon icon-help" data-help="help_${helplanguage}/settings/serverstatus/DownloadLog.xml" tabindex="-1" type="button"></button>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-sm-11">
                                <c:url var="logfileViewUrl" value="/serverstatus/logfile/view.action"/>
                                <a href="${logfileViewUrl}" class="btn btn-regular full-width">
                                    <mvc:message code="settings.logfile.show" />
                                </a>
                            </div>
                            <div class="col-sm-1">
                                <button class="icon icon-help" data-help="help_${helplanguage}/settings/serverstatus/ShowLog.xml" tabindex="-1" type="button"></button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-sm-6">
<!-- ----------Server Status------------------------------------------------------------- -->
            <div class="tile">
                <div class="tile-header">
                    <div class="headline">
                        <mvc:message code="settings.server.status" />
                    </div>
                </div>
                <div class="table-responsive">
                    <table id="serverStatus_status" class="table table-bordered table-hover js-table">
                        <thead>
                            <tr>
                                <th><mvc:message code="Name" /></th>
                                <th><mvc:message code="Status" /></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Overall Status</td>
                                <c:set var="isOkOverallStatus" value="${serverStatus.overallStatus}"/>
                                <td class="status ${isOkOverallStatus ? 'status-success' : 'status-error'}">
                                		${isOkOverallStatus ? 'OK' : 'ERROR'}
                                </td>
                            </tr>
                            <tr>
                                <td>Jobqueue Status</td>
                                <c:set var="isOkJobQueuStatus" value="${serverStatus.jobQueueStatus}"/>
                                <td class="status ${isOkJobQueuStatus ? 'status-success' : 'status-error'}">
                                		${isOkJobQueuStatus ? 'OK' : 'ERROR'}
                                </td>
                            </tr>
                            <tr>
                                <td>Import Status</td>
                                <c:set var="isOkImportStatus" value="${serverStatus.importStatus}"/>
                                <td class="status ${isOkImportStatus ? 'status-success' : 'status-error'}">
                                		${isOkImportStatus ? 'OK' : 'STALLING'}
                                </td>
                            </tr>
                            <tr>
                                <td>DB Status</td>
                                <c:set var="isOkDbStatus" value="${serverStatus.dbStatus}"/>
                                <td class="status ${isOkDbStatus ? 'status-success' : 'status-error'}">
                                		${isOkDbStatus ? 'OK' : 'ERROR'}
                                </td>
                            </tr>
                            <tr>
                                <td>DB Connection Status</td>
                                <c:set var="isOkDbConnectionStatus" value="${serverStatus.dbConnectStatus}"/>
                                <td class="status ${isOkDbConnectionStatus ? 'status-success' : 'status-error'}">
                                		${isOkDbConnectionStatus ? 'OK' : 'Cannot connect to DB'}
                                </td>
                            </tr>
                            <tr>
                                <td>Report Status</td>
                                <c:set var="isOkReportStatus" value="${serverStatus.reportStatus}"/>
                                <td class="status ${isOkReportStatus ? 'status-success' : 'status-error'}">
                                		${isOkReportStatus ? 'OK' : 'ERROR'}
                                </td>
                            </tr>
                            <tr class="${isOkImportStatus ? 'import_status_ok' : 'import_status_error'}">
                                <td colspan="2">
				                            <c:url var="killRunningImportsUrl" value="/serverstatus/killRunningImports.action"/>
				                            <button type="button" class="btn btn-primary btn-regular col-sm-12 kill-import-btn" style="white-space: normal; padding:5px">
				                            		<mvc:message code="serverStatus.killImports" />
				                            </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
<!-- ----------DB Column------------------------------------------------------------- -->
            <div class="tile">
                <div class="tile-header">
                    <div class="headline">
                        <mvc:message code="settings.DB" />
                    </div>
                </div>
                <div class="table-responsive">
                    <table id="serverStatus_dbColumn" class="table table-bordered table-striped table-hover js-table">
                        <thead>
                            <tr>
                                <th><mvc:message code="pluginmanager.plugin.version" />
                                </th>
                                <th><mvc:message code="Status" />
                                </th>
                            </tr>
                        </thead>
                        <tbody style="line-height:8px">
                            <c:forEach var="dbVersion" items="${serverStatus.dbVersionStatuses}">
                                <tr>
                                    <td>${dbVersion.version}</td>
                                    <c:set var="isOkVersionStatus" value="${dbVersion.status}"/>
                                    <td class="status ${isOkVersionStatus ? 'status-success' : 'status-error'}">
                                            ${isOkVersionStatus ? 'OK' : 'is Missing'}
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
<!-- ----------Config Values------------------------------------------------------------- -->
            <div class="tile">
                <div class="tile-header">
                    <div class="headline">
                        <mvc:message code="settings.Config" />
                    </div>
                </div>
                <div class="tile-content tile-content-forms">
                    <div class="form-group" >
                        <div class="row">
                        <div class="col-sm-3">
                            <mvc:text path="configForm.companyIdString" cssClass="form-control" placeholder="CompanyID"/>
                        </div>
                        <div class="col-sm-3">
                            <mvc:text path="configForm.name" cssClass="form-control" placeholder="ConfigName"/>
                        </div>
                        <div class="col-sm-3">
                            <mvc:text path="configForm.value" cssClass="form-control" placeholder="Value"/>
                        </div>
                        <div class="col-sm-3">
                            <mvc:text path="configForm.description" cssClass="form-control" placeholder="Description"/>
                        </div>
                        </div>
                        <div class="row" style="padding-top: 20px;">
                        </div>
                        <div class="row">
                            <div class="col-sm-4 pull-right">
                            <c:url var="configSaveAction" value="/serverstatus/config/view.action"/>
                            <button type="button" class="btn btn-primary btn-regular col-sm-12" data-form-url="${configSaveAction}" data-form-submit="" style="white-space: normal; padding:5px">
                                <mvc:message code="button.Show" />
                            </button>
                        </div>
                        <div class="col-sm-4 pull-right">
                            <c:url var="configViewAction" value="/serverstatus/config/save.action"/>
                            <button type="button" class="btn btn-primary btn-regular col-sm-12" data-form-url="${configViewAction}" data-form-submit="" style="white-space: normal; padding:5px">
                                <mvc:message code="button.Save" />
                            </button>
                        </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
<!-- ----------Kill Running Imports Light Box------------------------------------------- -->
	    <div class="modal-backdrop in kill-import-modal" style="display: none;"></div>
	    <div class="modal modal-wide kill-import-modal" style="display: none;">
				<div class="modal-dialog">
					<div class="modal-content">
						<div class="tile">
							
							<div class="tile-header">
								<div class="headline">
									<mvc:message code="serverStatus.killImports" />
								</div>
							</div>
						
							<div class="tile-content">
								<div class="col-sm-12" style="padding: 10px;">
									<p style="padding-bottom: 5px; color: #A91B1B;"><strong><mvc:message code="serverStatus.killImports.Xplain1" /></strong></p>
									<p><mvc:message code="serverStatus.killImports.Xplain2" /></p>
								</div>
								<div class="row">
									<div class="col-sm-12" style="padding: 10px;">
										<div class="btn-group">
											<div class="col-sm-4 pull-left">
												<button type="button" class="btn btn-large pull-left kill-import-btn">
													<i class="icon icon-times"></i> <span class="text"><bean:message key="button.Cancel" /></span>
												</button>
											</div>
											<div class="col-sm-4 pull-right">
												<button type="button" class="btn btn-large btn-primary pull-right kill-import-btn" data-form-url="${killRunningImportsUrl}" data-form-submit="">
													<i class="icon icon-check"></i> <span class="text"><bean:message key="button.OK" /></span>
												</button>
											</div>
										</div>
									</div>
								</div>
							</div>
							
						</div>
					</div>
				</div>
			</div>
		<script>
			jQuery('.kill-import-btn').on('click', function() {
				if (jQuery('.kill-import-modal').is(':visible')) {
						jQuery('.kill-import-modal').hide();
			    } else {
			    	jQuery('.kill-import-modal').show();
			    }
			});
		</script>
</mvc:form>

<%@ include file="serverstatus-license-upload.jspf" %>
