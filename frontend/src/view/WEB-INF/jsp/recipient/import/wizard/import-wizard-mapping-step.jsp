<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@page import="com.agnitas.util.importvalues.ImportMode"%>
<%@page import="com.agnitas.beans.ProfileFieldMode"%>
<%@ page import="com.agnitas.util.AgnUtils" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="importWizardSteps" type="com.agnitas.emm.core.recipient.imports.wizard.form.ImportWizardSteps"--%>
<%--@elvariable id="profileFields" type="java.util.List<com.agnitas.beans.ProfileField>"--%>
<%--@elvariable id="_agnTbl_editable" type="com.agnitas.beans.ProfileFieldMode"--%>
<%--@elvariable id="_agnTbl_column_name" type="java.lang.String"--%>
<%--@elvariable id="_agnTbl_shortname" type="java.lang.String"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>

<c:set var="customerIdAllowed" value="false"/>
<emm:ShowByPermission token="import.customerid">
    <c:set var="customerIdAllowed" value="true"/>
</emm:ShowByPermission>

<mvc:form servletRelativeAction="/recipient/import/wizard/step/mapping.action" modelAttribute="importWizardSteps.mappingStep" enctype="multipart/form-data" data-form="resource">
    <c:set var="tileContent">
        <div class="tile-notification tile-notification-info">
            <mvc:message code="export.CsvMappingMsg"/>
            <button type="button" class="icon icon-help" data-help="help_${helplanguage}/importwizard/step_3/Csvmapping.xml"></button>
        </div>
        <div class="tile-content tile-content-forms">
            <div class="table-wrapper">
                <table class="table table-bordered table-striped">
                    <thead>
                        <th><mvc:message code="import.CsvColumn"/></th>
                        <th><mvc:message code="import.DbColumn"/></th>
                    </thead>
                    <tbody>
                    <emm:instantiate var="dbColumns" type="java.util.LinkedHashMap">
                        <emm:ShowColumnInfo id="agnTbl" table="<%= AgnUtils.getCompanyID(request) %>"
                                            hide="timestamp, change_date, creation_date, bounceload, datasource_id, lastopen_date, lastclick_date, lastsend_date, latest_datasource_id, cleaned_date, facebook_status, foursquare_status, google_status, twitter_status, xing_status, sys_encrypted_sending">
                            <c:if test="${_agnTbl_editable == ProfileFieldMode.Editable ||(_agnTbl_editable == ProfileFieldMode.ReadOnly && importWizardSteps.helper.status.keycolumn.equalsIgnoreCase(_agnTbl_column_name))}">
                                <c:set target="${dbColumns}" property="${fn:toLowerCase(_agnTbl_column_name)}" value="${_agnTbl_shortname}"/>
                            </c:if>
                        </emm:ShowColumnInfo>
                    </emm:instantiate>
                    
                    <c:forEach var="csvColumn" items="${importWizardSteps.helper.csvAllColumns}" varStatus="rowCounter">
                    <tr>
                        <td class="import_classic_columns_csv">
                            ${csvColumn.name}
                        </td>
                        <td class="import_classic_columns_db">
                            <c:set var="columnMapping" value="${importWizardSteps.helper.columnMapping}"/>
                            <mvc:select path="columnMapping[map_${rowCounter.index + 1}]" cssClass='form-control js-select'>
                                <option value="NOOP"><mvc:message code="import.column.skip"/></option>
                                <c:forEach var="dbColumn" items="${importWizardSteps.helper.dbAllColumns}">
                                    <c:set var="dbColumnName" value="${dbColumn.key}" />
                                    <c:set var="dbColumnAlias" value="${dbColumn.value.name}" />
                                    <c:set var="mode" value="${importWizardSteps.helper.mode}" />
                                    <c:choose>
                                        <c:when test="${customerIdAllowed and (not dbColumnName.equalsIgnoreCase('CUSTOMER_ID') or (dbColumnName.equalsIgnoreCase('CUSTOMER_ID') and mode != ImportMode.ADD.intValue && mode != ImportMode.ADD_AND_UPDATE.intValue))}">
                                            <option value="${dbColumnName}" ${((columnMapping != null && columnMapping.containsKey(csvColumn.getName().trim())
                                                    && dbColumnName.trim().equalsIgnoreCase((columnMapping.get(csvColumn.getName().trim())).getName().trim()))
                                                    || (columnMapping == null && dbColumnName.trim().replace("-", "").replace("_", "").equalsIgnoreCase(csvColumn.getName().replace("-", "").replace("_", "").trim())))
                                                    ? 'selected' : ''}>
                                                    ${dbColumnAlias}
                                            </option>
                                        </c:when>
                                        <c:when test="${!dbColumnName.equalsIgnoreCase('CUSTOMER_ID')}">
                                            <option value="${dbColumnName}" ${((columnMapping != null && columnMapping.containsKey(csvColumn.getName().trim())
                                                    && dbColumnName.trim().equalsIgnoreCase((columnMapping.get(csvColumn.getName().trim())).getName().trim()))
                                                    || (columnMapping == null && dbColumnName.trim().replace("-", "").replace("_", "").equalsIgnoreCase(csvColumn.getName().replace("-", "").replace("_", "").trim())))
                                                    ? 'selected' : ''}>
                                                    ${dbColumnAlias}
                                            </option>
                                        </c:when>
                                    </c:choose>
                                </c:forEach>
                            </mvc:select>
                        </td>
                    </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:set>
    <c:set var="step" value="3"/>
    <c:url var="backUrl" value="/recipient/import/wizard/step/mode.action"/>

    <%@ include file="fragments/import-wizard-step-template.jspf" %>
</mvc:form>
