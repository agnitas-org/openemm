<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="parsedContentJson" type="org.json.JSONArray"--%>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/verify.action" modelAttribute="importWizardSteps" enctype="multipart/form-data" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-notification tile-notification-info">
            <mvc:message code="import.csv_analysis"/>
            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_4/CsvAnalysis.xml"></button>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="table-wrapper">
                <table class="table table-bordered table-striped">
                    <thead>
                        <tr>
                            <th><mvc:message code="csv_used_column"/></th>
                            <th><mvc:message code="csv_unused_column_csv"/></th>
                            <th><mvc:message code="csv_unused_column_db"/></th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td class="align-top">
                                <c:forEach var="column" items="${importWizardSteps.helper.csvAllColumns}">
                                    <c:if test="${column.active}">
                                        ${column.name}<br>
                                    </c:if>
                                </c:forEach>
                            </td>
                            <td class="align-top">
                                <c:forEach var="column" items="${importWizardSteps.helper.csvAllColumns}">
                                    <c:if test="${!column.active}">
                                        ${column.name}<br>
                                    </c:if>
                                </c:forEach>
                            </td>
                            <td class="align-top">
                                <c:forEach var="column" items="${importWizardSteps.helper.dbAllColumns}">
                                    <c:if test="${!column.value.active}">
                                        ${column.value.name}<br>
                                    </c:if>
                                </c:forEach>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <div class="tile-separator"></div>
            <div class="inline-tile">
                <div class="inline-tile-header">
                    <h2><mvc:message code="default.Preview"/></h2>
                </div>
                <div class="inline-tile-content">
                    <div class="js-data-table" data-table="csv-preview">
                        <div class="js-data-table-body"></div>
                        <c:set var="csvPreviewJson">
                        {
                            "columns": [
                            <c:forEach var="col" items="${importWizardSteps.helper.csvAllColumns}" varStatus="status">
                                <c:if test="${col.active}">
                                    {
                                        "headerName": "${col.name}",
                                        "editable": false,
                                        "suppressMenu": true,
                                        "width": 60,
                                        "field": "${col.name}",
                                        "cellRenderer": "NotEscapedStringCellRenderer"
                                    },
                                </c:if>
                            </c:forEach>
                            ],
                            "options": {
                                "paginationPageSize": 5
                            },
                            "data": ${parsedContentJson}
                        }
                        </c:set>
                        <script id="csv-preview" type="application/json">
                            ${csvPreviewJson.replaceAll("\\,(?=\\s*?[\\}\\]])", "")}
                        </script>
                    </div>
                </div>
            </div>
        </div>
    </c:set>
    <c:set var="step" value="5"/>
    <c:url var="backUrl" value="/recipient/import/wizard/step/${importWizardSteps.missingFieldsStepNeeded ? 'verifyMissingFields.action' : 'mapping.action?back=true'}"/>
    
    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
