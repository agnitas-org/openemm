<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="parsedContentJson" type="net.sf.json.JSONArray"--%>

<c:set var="step" value="5"/>
<c:url var="backUrl" value="/recipient/import/wizard/step/${importWizardSteps.missingFieldsStepNeeded ? 'verifyMissingFields.action' : 'mapping.action?back=true'}"/>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/verify.action" modelAttribute="importWizardSteps" enctype="multipart/form-data" cssClass="tiles-container" data-form="resource">
    <div class="tile">
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.Wizard" /></h1>
            <div class="tile-controls">
                <%@ include file="fragments/import-wizard-steps-navigation.jspf" %>
            </div>
        </div>
        <div class="tile-body d-flex flex-column gap-3">
            <div class="notification-simple notification-simple--lg notification-simple--info">
                <span>
                    <mvc:message code="import.csv_analysis"/>
                    <a href="#" type="button" class="icon icon-question-circle" data-help="help_${helplanguage}/importwizard/step_4/CsvAnalysis.xml"></a>
                </span>
            </div>

            <div class="table-wrapper">
                <div class="table-wrapper__header"></div>
                <div class="table-wrapper__body">
                    <table class="table table--borderless h-100">
                        <thead>
                        <tr>
                            <th><mvc:message code="csv_used_column"/></th>
                            <th><mvc:message code="csv_unused_column_csv"/></th>
                            <th><mvc:message code="csv_unused_column_db"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>
                                <div class="d-flex flex-column h-100">
                                    <c:forEach var="column" items="${importWizardSteps.helper.csvAllColumns}">
                                        <c:if test="${column.active}">
                                            <p class="py-2">${column.name}</p>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </td>
                            <td>
                                <div class="d-flex flex-column h-100">
                                    <c:forEach var="column" items="${importWizardSteps.helper.csvAllColumns}">
                                        <c:if test="${!column.active}">
                                            <p class="py-2">${column.name}</p>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </td>
                            <td>
                                <div class="d-flex flex-column h-100">
                                    <c:forEach var="column" items="${importWizardSteps.helper.dbAllColumns}">
                                        <c:if test="${!column.value.active}">
                                            <p class="py-2">${column.value.name}</p>
                                        </c:if>
                                    </c:forEach>
                                </div>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div class="table-wrapper__footer"></div>
            </div>

            <div class="table-wrapper" data-js-table="csv-preview">
                <div class="table-wrapper__header">
                    <h1 class="table-wrapper__title"><mvc:message code="default.Preview" /></h1>
                    <div class="table-wrapper__controls">
                        <%@include file="../../../common/table/toggle-truncation-btn.jspf" %>
                        <jsp:include page="../../../common/table/entries-label.jsp" />
                    </div>
                </div>

                <c:set var="csvPreviewJson">
                    {
                        "columns": [
                            <c:forEach var="col" items="${importWizardSteps.helper.csvAllColumns}" varStatus="status">
                                <c:if test="${col.active}">
                                    {
                                        "headerName": "${col.name}",
                                        "editable": false,
                                        "suppressMenu": true,
                                        "field": "${col.name}",
                                        "cellRenderer": "NotEscapedStringCellRenderer"
                                    },
                                </c:if>
                            </c:forEach>
                        ],
                        "data": ${emm:toJson(parsedContentJson)}
                    }
                </c:set>
                <script id="csv-preview" type="application/json">
                    ${csvPreviewJson.replaceAll("\\,(?=\\s*?[\\}\\]])", "")}
                </script>
            </div>
        </div>
    </div>
</mvc:form>