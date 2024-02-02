<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/preScan.action" modelAttribute="importWizardSteps.preScanStep" enctype="multipart/form-data" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-notification tile-notification-info">
            <mvc:message code="ResultMsg"/>
            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_5/CsvErrors.xml"></button>
        </div>
        <c:set var="status" value="${importWizardSteps.helper.status}"/>
        <div class="tile-content tile-content-forms">
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
    </c:set>
    <c:set var="step" value="6"/>
    <c:url var="backUrl" value="/recipient/import/wizard/step/verify.action"/>

    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
