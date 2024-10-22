<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="step" value="6"/>
<c:url var="backUrl" value="/recipient/import/wizard/step/verify.action" />
<c:set var="status" value="${importWizardSteps.helper.status}"/>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/preScan.action" modelAttribute="importWizardSteps.preScanStep" enctype="multipart/form-data" cssClass="tiles-container" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
            <div class="tile-controls">
                <%@ include file="fragments/import-wizard-steps-navigation.jspf" %>
            </div>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <div class="notification-simple notification-simple--lg notification-simple--info">
                        <span>
                            <mvc:message code="info.import.analyse"/>
                            <a href="#" type="button" class="icon icon-question-circle" data-help="help_${helplanguage}/importwizard/step_5/CsvErrors.xml"></a>
                        </span>
                    </div>
                </div>

                <div class="col-12">
                    <div class="input-groups input-groups--md">
                        <c:set var="labelCode" value="import.csv_errors_email"/>
                        <c:set var="count" value="${status.getError('email')}"/>
                        <c:set var="downloadName" value="error_email"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_errors_blacklist"/>
                        <c:set var="count" value="${status.getError('blacklist')}"/>
                        <c:set var="downloadName" value="error_blacklist"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_errors_double"/>
                        <c:set var="count" value="${status.getError('keyDouble')}"/>
                        <c:set var="downloadName" value="double_email"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_errors_numeric"/>
                        <c:set var="count" value="${status.getError('numeric')}"/>
                        <c:set var="downloadName" value="error_numeric"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_errors_mailtype"/>
                        <c:set var="count" value="${status.getError('mailtype')}"/>
                        <c:set var="downloadName" value="error_mailtype"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_errors_gender"/>
                        <c:set var="count" value="${status.getError('gender')}"/>
                        <c:set var="downloadName" value="error_gender"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_errors_date"/>
                        <c:set var="count" value="${status.getError('date')}"/>
                        <c:set var="downloadName" value="error_date"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="csv_errors_linestructure"/>
                        <c:set var="count" value="${status.getError('structure')}"/>
                        <c:set var="downloadName" value="error_structure"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>

                        <c:set var="labelCode" value="import.csv_summary"/>
                        <c:set var="count" value="${importWizardSteps.helper.linesOK}"/>
                        <c:set var="downloadName" value="import_ok"/>
                        <%@ include file="fragments/import-wizard-prescan-download-row.jspf" %>
                    </div>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
