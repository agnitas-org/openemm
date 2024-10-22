<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.importvalues.ImportMode" %>
<%@ page import="org.agnitas.beans.Recipient" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILTYPE_TEXT" 			value="<%= Recipient.MAILTYPE_TEXT %>" />
<c:set var="MAILTYPE_HTML"          value="<%= Recipient.MAILTYPE_HTML %>" />
<c:set var="MAILTYPE_HTML_OFFLINE"	value="<%= Recipient.MAILTYPE_HTML_OFFLINE %>" />
<c:set var="UPDATE_ONLY_MODE_CODE"  value="<%= ImportMode.UPDATE.getIntValue() %>" />

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>

<c:set var="step" value="4"/>
<c:url var="backUrl" value="/recipient/import/wizard/step/mapping.action?back=true"/>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/verifyMissingFields.action" modelAttribute="importWizardSteps" cssClass="tiles-container" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
            <div class="tile-controls">
                <%@ include file="fragments/import-wizard-steps-navigation.jspf" %>
            </div>
        </div>
        <div class="tile-body">
            <div class="table-wrapper">
                <div class="table-wrapper__header"></div>
                <div class="table-wrapper__body">
                    <table class="table table--borderless">
                        <thead>
                        <tr>
                            <th>
                                <span class="${importWizardSteps.helper.genderMissing ? '' : 'opacity-0'}"><mvc:message code="error.import.column.gender.required"/></span>
                            </th>
                            <th>
                                <span class="${importWizardSteps.helper.genderMissing ? '' : 'opacity-0'}"><mvc:message code="import.gender.as.unknown"/></span>
                            </th>
                        <tr>
                        </thead>
                        <tbody>
                            <c:if test="${importWizardSteps.helper.mailingTypeMissing and importWizardSteps.helper.mode != UPDATE_ONLY_MODE_CODE}">
                                <tr>
                                    <td><mvc:message code="recipient.mailingtype"/>&nbsp;&nbsp;</td>
                                    <td>
                                        <mvc:select path="helper.manualAssignedMailingType" cssClass="form-control js-select">
                                            <mvc:option value="${MAILTYPE_TEXT}">
                                                <mvc:message code="recipient.mailingtype.text"/>
                                            </mvc:option>
                                            <mvc:option value="${MAILTYPE_HTML}">
                                                <mvc:message code="HTML"/>
                                            </mvc:option>
                                            <mvc:option value="${MAILTYPE_HTML_OFFLINE}">
                                                <mvc:message code="recipient.mailingtype.htmloffline"/>
                                            </mvc:option>
                                        </mvc:select>
                                    </td>
                                <tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
                <div class="table-wrapper__footer"></div>
            </div>
        </div>
    </div>
</mvc:form>
