<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ page import="org.agnitas.util.importvalues.ImportMode" %>
<%@ page import="org.agnitas.beans.Recipient" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="MAILTYPE_TEXT" 			value="<%= Recipient.MAILTYPE_TEXT %>" 		   scope="request"/>
<c:set var="MAILTYPE_HTML"          value="<%= Recipient.MAILTYPE_HTML %>"         scope="request"/>
<c:set var="MAILTYPE_HTML_OFFLINE"	value="<%= Recipient.MAILTYPE_HTML_OFFLINE %>" scope="request"/>
<c:set var="UPDATE_ONLY_MODE_CODE"  value="<%= ImportMode.UPDATE.getIntValue() %>"/>

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/verifyMissingFields.action" modelAttribute="importWizardSteps" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-content tile-content-forms">
            <div class="table-wrapper">
                <table class="table table-bordered table-striped">
                    <thead>
                            <c:if test="${importWizardSteps.helper.genderMissing}">
                            <tr>
                                <th><mvc:message code="error.import.column.gender.required"/></th>
                                <th><mvc:message code="import.gender.as.unknown"/></th>
                            <tr>
                            </c:if>
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
        </div>
    </c:set>
    <c:set var="step" value="4"/>
    <c:url var="backUrl" value="/recipient/import/wizard/step/mapping.action?back=true"/>
    
    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
