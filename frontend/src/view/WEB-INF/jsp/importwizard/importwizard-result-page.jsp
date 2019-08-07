<%@ page language="java" contentType="text/html; charset=utf-8" import="org.agnitas.web.RecipientAction"  errorPage="/error.do" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="statusMsg">
    <bean:message key="import.csv_successfully"/>
</c:set>
<c:set var="headlineCls" value="icon-state-success"/>

<c:forEach var="reportEntry" items="${newImportWizardForm.reportEntries}">
	<c:if test="${reportEntry.value.matches('[0-9]+')}">
	    <c:if test="${reportEntry.value gt 0 && fn:contains(reportEntry.key, 'error')}">
	        <c:set var="statusMsg">
	            <bean:message key="import.csv_unsuccessful"/>
	        </c:set>
	        <c:set var="headlineCls" value="icon-state-warning"/>
	    </c:if>
	</c:if>
</c:forEach>

<div class="tile">
    <div class="tile-header">
        <h2 class="headline">
            <i class="icon ${headlineCls}"></i> <bean:message key="import.csv_Finished"/> 
        </h2>
    </div>
    <div class="tile-content tile-content-forms">
        <div class="form-group">
            <div class="col-sm-offset-4 col-sm-8">
                <ul class="list-group">
                    <c:forEach var="reportEntry" items="${newImportWizardForm.reportEntries}">
						<c:if test="${reportEntry.value.matches('.*[A-Za-z]+.*')}">
	                        <li class="list-group-item">
	                            <span class="badge
	                                <c:if test="${fn:contains(reportEntry.key, 'error')}"> badge-alert </c:if>
	                                <c:if test="${fn:contains(reportEntry.key, 'fatal')}"> badge-alert </c:if>">
	                                ${reportEntry.value}
	                            </span>
	                            <bean:message key="${reportEntry.key}"/>
	                        </li>
						</c:if>
						
	                    <c:if test="${reportEntry.value.matches('[0-9]+')}">
	                        <li class="list-group-item">
	                            <span class="badge
	                                <c:if test="${reportEntry.value eq 0 && fn:contains(reportEntry.key, 'error')}"> badge-success </c:if>
	                                <c:if test="${reportEntry.value gt 0 && fn:contains(reportEntry.key, 'error')}"> badge-warning </c:if>">
	                                ${reportEntry.value}
	                            </span>
	                            <bean:message key="${reportEntry.key}"/>
	                        </li>
						</c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
        <c:if test="${newImportWizardForm.assignedMailingLists != null}">
            <div class="form-group">
                <div class="col-sm-offset-4 col-sm-8">
                    <ul class="list-group">
                    <c:forEach var="assignedList" items="${newImportWizardForm.assignedMailingLists}">
                        <li class="list-group-item">
                            <span class="badge">${newImportWizardForm.mailinglistAssignStats[assignedList.id]}</span>
                            <bean:message key="${newImportWizardForm.mailinglistAddMessage}"/> ${assignedList.shortname}
                        </li>
                    </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>
        <c:if test="${newImportWizardForm.resultFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="ResultMsg"/></label>
                </div>
                <div class="col-sm-8">
                    <agn:agnLink styleClass="btn btn-regular btn-primary" data-prevent-load="" page="/newimportwizard.do?action=${DOWNLOAD_ACTION}&downloadFileType=${RESULT}">
                        <i class="icon icon-download"></i>
                        ${newImportWizardForm.resultFile.name}
                    </agn:agnLink>
                </div>
            </div>
        </c:if>

        <c:if test="${newImportWizardForm.validRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="import.recipients.valid"/></label>
                </div>
                <div class="col-sm-8">
                    <agn:agnLink styleClass="btn btn-regular btn-primary" data-prevent-load="" page="/newimportwizard.do?action=${DOWNLOAD_ACTION}&downloadFileType=${VALID}">
                        <i class="icon icon-download"></i>
                        ${newImportWizardForm.validRecipientsFile.name}
                    </agn:agnLink>
                </div>
            </div>
        </c:if>

        <c:if test="${newImportWizardForm.invalidRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="import.recipients.invalid"/></label>
                </div>
                <div class="col-sm-8">
                    <agn:agnLink styleClass="btn btn-regular btn-primary" data-prevent-load="" page="/newimportwizard.do?action=${DOWNLOAD_ACTION}&downloadFileType=${INVALID}">
                        <i class="icon icon-download"></i>
                        ${newImportWizardForm.invalidRecipientsFile.name}
                    </agn:agnLink>
                </div>
            </div>
        </c:if>

        <c:if test="${newImportWizardForm.duplicateRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="import.recipients.duplicate"/></label>
                </div>
                <div class="col-sm-8">
                    <agn:agnLink styleClass="btn btn-regular btn-primary" data-prevent-load="" page="/newimportwizard.do?action=${DOWNLOAD_ACTION}&downloadFileType=${DUPLICATE}">
                        <i class="icon icon-download"></i>
                        ${newImportWizardForm.duplicateRecipientsFile.name}
                    </agn:agnLink>
                </div>
            </div>
        </c:if>

        <c:if test="${newImportWizardForm.fixedRecipientsFile != null}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label"><bean:message key="import.recipients.fixed"/></label>
                </div>
                <div class="col-sm-8">
                    <agn:agnLink styleClass="btn btn-regular btn-primary" data-prevent-load="" page="/newimportwizard.do?action=${DOWNLOAD_ACTION}&downloadFileType=${FIXED}">
                        <i class="icon icon-download"></i>
                        ${newImportWizardForm.fixedRecipientsFile.name}
                    </agn:agnLink>
                </div>
            </div>
        </c:if>
    </div>
</div>
