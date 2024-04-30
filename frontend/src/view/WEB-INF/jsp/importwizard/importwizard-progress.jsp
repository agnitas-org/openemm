<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.web.ProfileImportAction" %>
<%@ page import="com.agnitas.emm.core.imports.beans.ImportProgressSteps" %>
<%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--@elvariable id="newImportWizardForm" type="com.agnitas.web.forms.ComNewImportWizardForm"--%>

<c:choose>
    <c:when test="${newImportWizardForm.error}">
        <c:set var="ACTION_PROCEED" value="<%= ProfileImportAction.ACTION_PROCEED %>"/>
        <c:set var="ACTION_PREVIEW" value="<%= ProfileImportAction.ACTION_PREVIEW %>"/>

        <agn:agnForm action="/newimportwizard" data-form="resource">
            <div class="msg-tile msg-tile-alert">
                <div class="msg-tile-header">
                    <img alt="" src="assets/core/images/facelift/msgs_msg-uploading-error.svg" onerror="this.onerror=null; this.src='assets/core/images/facelift/msgs_msg-uploading-error.png'">
                </div>
                <div class="msg-tile-content">
                    <h3><bean:message key="default.loading.stopped"/></h3>
                    <p><bean:message key="error.importErrorsOccured"/></p>
                    <div class="btn-block">
                        <div class="btn-group">
                            <button class="btn btn-primary btn-regular" type="button" data-form-set="action:${ACTION_PREVIEW}" data-form-change="" data-form-submit="">
                                <i class="icon icon-angle-left"></i><span class="text"><bean:message key="button.Back"/></span>
                            </button>
                            <button class="btn btn-regular" type="button" data-form-change data-form-set="action:${ACTION_PROCEED}" data-form-submit="">
                                <i class="icon icon-refresh"></i><span class="text"><bean:message key="button.TryAgain"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </agn:agnForm>
    </c:when>
    <c:otherwise>
        <c:set var="refreshMillis">${newImportWizardForm.refreshMillis}</c:set>
        <c:set var="stepsNum" value="<%= ImportProgressSteps.values().length %>"/>

        <agn:agnForm action="/newimportwizard" data-form="loading" data-polling-interval="${refreshMillis}">
            <html:hidden property="action"/>
            <html:hidden property="error"/>

            <div class="msg-tile msg-tile-primary">
                <div class="msg-tile-header">
                    <img alt="" src="assets/core/images/facelift/msgs_msg-uploading.svg" onerror="this.onerror=null; this.src='assets/core/images/facelift/msgs_msg-uploading.png'">
                </div>
                <div class="msg-tile-content">
                    <h3><bean:message key="upload.data"/></h3>
                    <div class="progress thin-progress">
                        <div class="progress-bar" role="progressbar" aria-valuenow="${newImportWizardForm.currentProgressStatus.parentStep.ordinal()}" aria-valuemin="0" aria-valuemax="${stepsNum}" style="width: ${(newImportWizardForm.currentProgressStatus.parentStep.ordinal() + 1) / stepsNum * 100}%">
                        </div>
                    </div>

                    <p class="progress-bottom-desc">
                        <bean:message key="import.steps" arg0="${newImportWizardForm.currentProgressStatus.parentStep.ordinal() + 1}" arg1="${stepsNum}"/>
                        : <bean:message key="import.progress.step.${newImportWizardForm.currentProgressStatus.parentStep}" />
                    </p>


                    <c:choose>
                        <c:when test="${newImportWizardForm.currentProgressStatus eq 'IMPORTING_DATA_TO_TMP_TABLE'}">
                            <div class="progress">
                                <div class="progress-bar" role="progressbar" aria-valuenow="${newImportWizardForm.completedPercent}" aria-valuemin="0" aria-valuemax="100" style="width: ${newImportWizardForm.completedPercent}%">
                                    <bean:message key="import.csv_importing_data"/> ${newImportWizardForm.completedPercent}%
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <p>Details: <bean:message key="import.progress.detailedItem.${newImportWizardForm.currentProgressStatus}"/></p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </agn:agnForm>
    </c:otherwise>
</c:choose>
