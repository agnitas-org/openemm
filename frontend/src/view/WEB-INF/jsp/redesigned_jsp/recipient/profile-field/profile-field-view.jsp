<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.util.DbColumnType" %>
<%@ page import="com.agnitas.emm.core.profilefields.bean.ProfileFieldDependentType" %>
<%@ page import="com.agnitas.beans.ProfileFieldMode" %>

<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="profileForm" type="com.agnitas.emm.core.profilefields.form.ProfileFieldForm"--%>
<%--@elvariable id="HISTORY_FEATURE_ENABLED" type="java.lang.Boolean"--%>
<%--@elvariable id="isNewField" type="java.lang.Boolean"--%>

<c:set var="GENERIC_TYPE_INTEGER" value="<%= DbColumnType.GENERIC_TYPE_INTEGER %>"/>
<c:set var="GENERIC_TYPE_FLOAT" value="<%= DbColumnType.GENERIC_TYPE_FLOAT %>"/>
<c:set var="GENERIC_TYPE_VARCHAR" value="<%= DbColumnType.GENERIC_TYPE_VARCHAR %>"/>
<c:set var="GENERIC_TYPE_DATE" value="<%= DbColumnType.GENERIC_TYPE_DATE %>"/>
<c:set var="GENERIC_TYPE_DATETIME" value="<%= DbColumnType.GENERIC_TYPE_DATETIME %>"/>

<c:set var="DEPENDENT_TYPE_WORKFLOW" value="<%= ProfileFieldDependentType.WORKFLOW %>"/>
<c:set var="DEPENDENT_TYPE_TARGET_GROUP" value="<%= ProfileFieldDependentType.TARGET_GROUP %>"/>
<c:set var="DEPENDENT_TYPE_IMPORT_PROFILE" value="<%= ProfileFieldDependentType.IMPORT_PROFILE %>"/>
<c:set var="DEPENDENT_TYPE_EXPORT_PROFILE" value="<%= ProfileFieldDependentType.EXPORT_PROFILE %>"/>
<c:set var="DEPENDENT_TYPE_TRIGGER" value="<%= ProfileFieldDependentType.TRIGGER %>"/>

<div class="tiles-container" data-editable-view="${agnEditViewKey}">
    <mvc:form id="settings-tile" servletRelativeAction="/profiledb/save.action" method="POST" modelAttribute="profileForm" cssClass="tile"
              data-form="resource" data-controller="profile-field-view" data-action="save" data-editable-tile="main">

        <c:if test="${targetUrl ne null}">
            <input type="hidden" name="targetUrl" value="${targetUrl}">
        </c:if>

        <script data-initializer="profile-field-view" type="application/json">
            {
                "fixedValues": ${emm:toJson(profileForm.allowedValues)},
                "fieldType": ${emm:toJson(profileForm.fieldType)}
            }
        </script>

        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="Settings" /></h1>
        </div>
        <div class="tile-body js-scrollable">
            <div class="row g-3" data-field="toggle-vis">
                <div class="col-6">
                    <label for="fieldShortname" class="form-label"><mvc:message code="settings.FieldName"/> *</label>
                    <mvc:text path="shortname" id="fieldShortname" cssClass="form-control" maxlength="99" size="32" data-field="required"/>
                </div>

                <div class="col-6">
                    <label for="fieldDbName" class="form-label"><mvc:message code="settings.FieldNameDB"/> *</label>
                    <mvc:text id="fieldDbName" path="fieldname" cssClass="form-control" size="32" readonly="${not isNewField}" data-field="required"/>
                </div>

                <div class="col-12">
                    <label for="fieldDescription" class="form-label"><mvc:message code="Description"/> *</label>
                    <mvc:text path="description" id="fieldDescription" cssClass="form-control"/>
                </div>

                <div class="col-12">
                    <div class="row g-3">
                        <div class="col">
                            <label for="fieldType" class="form-label"><mvc:message code="default.Type"/></label>
                            <mvc:select path="fieldType" size="1" id="fieldType" cssClass="form-control" data-field-vis="" disabled="${not isNewField}" data-action="change-field-type">
                                <mvc:option value="INTEGER" data-field-vis-hide="#fieldLengthBlock" data-field-vis-show="#fieldInterestBlock, #defaultFieldBlock, #nullAllowedBlock">
                                    <mvc:message code="settings.fieldType.INTEGER"/>
                                </mvc:option>
                                <c:if test="${not isNewField}">
                                    <mvc:option value="FLOAT" data-field-vis-hide="#fieldLengthBlock" data-field-vis-show="#fieldInterestBlock, #defaultFieldBlock, #nullAllowedBlock">
                                        <mvc:message code="settings.fieldType.DOUBLE"/>
                                    </mvc:option>
                                </c:if>
                                <mvc:option value="DOUBLE" data-field-vis-hide="#fieldLengthBlock" data-field-vis-show="#fieldInterestBlock, #defaultFieldBlock, #nullAllowedBlock">
                                    <mvc:message code="settings.fieldType.DOUBLE"/>
                                </mvc:option>
                                <mvc:option value="VARCHAR" data-field-vis-hide="#fieldInterestBlock" data-field-vis-show="#fieldLengthBlock, #defaultFieldBlock, #nullAllowedBlock">
                                    <mvc:message code="settings.fieldType.VARCHAR"/>
                                </mvc:option>
                                <mvc:option value="DATE" data-field-vis-hide="#fieldLengthBlock, #fieldInterestBlock, #defaultFieldBlock, #nullAllowedBlock">
                                    <mvc:message code="settings.fieldType.DATE"/>
                                </mvc:option>
                                <mvc:option value="DATETIME" data-field-vis-hide="#fieldLengthBlock, #fieldInterestBlock, #defaultFieldBlock, #nullAllowedBlock">
                                    <mvc:message code="settings.fieldType.DATETIME"/>
                                </mvc:option>
                            </mvc:select>

                            <c:if test="${not isNewField}">
                                <mvc:hidden path="fieldType"/>
                            </c:if>
                        </div>

                        <c:choose>
                            <c:when test="${isNewField}">
                                <div id="fieldLengthBlock" class="col" data-field="validator">
                                    <label for="fieldLength" class="form-label"><mvc:message code="settings.Length"/></label>
                                    <mvc:text path="fieldLength" id="fieldLength" cssClass="form-control"
                                              data-field-validator="number" data-validator-options="min: 1, max: 4000, required: true, strict: true"/>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:if test="${profileForm.fieldType == GENERIC_TYPE_VARCHAR}">
                                    <div id="fieldLengthBlock" class="col">
                                        <label for="fieldLength" class="form-label"><mvc:message code="settings.Length"/></label>
                                        <mvc:text path="fieldLength" id="fieldLength" cssClass="form-control" disabled="true"/>
                                        <mvc:hidden path="fieldLength"/>
                                    </div>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <c:if test="${profileForm.fieldType != GENERIC_TYPE_DATE && profileForm.fieldType != GENERIC_TYPE_DATETIME}">
                    <div id="defaultFieldBlock" class="col-12">
                        <label for="fieldDefault" class="form-label"><mvc:message code="settings.Default_Value"/></label>
                        <mvc:text path="fieldDefault" id="fieldDefault" cssClass="form-control" readonly="${!isNewField}" size="32"  maxlength="199"/>
                    </div>
                </c:if>

                <emm:ShowByPermission token="profileField.visible">
                    <div class="col-12">
                        <label for="field-mode" class="form-label"><mvc:message code="visibility"/></label>
                        <mvc:select id="field-mode" path="fieldMode" cssClass="form-control js-select">
                            <c:forEach var="fieldMode" items="${ProfileFieldMode.values()}">
                                <mvc:option value="${fieldMode}"><mvc:message code="${fieldMode.messageKey}" /></mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </emm:ShowByPermission>

                <div id="nullAllowedBlock" class="col-6">
                    <div class="form-check form-switch">
                        <mvc:checkbox path="fieldNull" id="fieldNull" cssClass="form-check-input" role="switch" disabled="${not isNewField}"/>
                        <label class="form-label form-check-label" for="fieldNull"><mvc:message code="settings.NullAllowed"/></label>
                    </div>
                </div>

                <div class="col-6">
                    <div class="form-check form-switch">
                        <mvc:checkbox path="line" id="lineAfter" cssClass="form-check-input" role="switch"/>
                        <label class="form-label form-check-label" for="lineAfter"><mvc:message code="line_after"/></label>
                    </div>
                </div>

                <c:if test="${profileForm.fieldType == GENERIC_TYPE_INTEGER || profileForm.fieldType == GENERIC_TYPE_FLOAT}">
                    <div id="fieldInterestBlock" class="col-6">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="interest" id="interest" cssClass="form-check-input" role="switch"/>
                            <label class="form-label form-check-label" for="interest"><mvc:message code="FieldIsInterest"/></label>
                        </div>
                    </div>
                </c:if>

                <c:if test="${HISTORY_FEATURE_ENABLED}">
                    <div class="col-6">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="includeInHistory" id="includeInHistory" cssClass="form-check-input" role="switch"/>
                            <label class="form-label form-check-label" for="includeInHistory">
                                <mvc:message code="profileHistory.includeField"/>
                                <a href="#" class="icon icon-question-circle" data-help="recipient/profileField/HistorisationAdd.xml"></a>
                            </label>
                        </div>
                    </div>
                </c:if>

                <div class="col-12">
                    <label for="fieldSort" class="form-label"><mvc:message code="FieldSort"/></label>
                    <select class="form-control" name="fieldSort" id="fieldSort">
                        <option value="1000"<c:if test="${profileForm.fieldSort == 1000}"> selected</c:if>>
                            <mvc:message code="noSort"/></option>
                        <option value="1"<c:if test="${profileForm.fieldSort == 1}"> selected</c:if>><mvc:message code="first"/></option>

                        <c:forEach var="field" items="${fieldsWithIndividualSortOrder}">
                            <option value='${field.sortOrder + 1}' <c:if
                                    test="${profileForm.fieldSort == field.sortOrder + 1}"> selected</c:if>>
                                <mvc:message code="after"/> ${field.shortName}</option>
                        </c:forEach>
                    </select>
                </div>

                <c:if test="${not empty creationDate}">
                    <div class="col">
                        <label class="form-label"><mvc:message code="default.creationDate"/></label>
                        <input type="text" class="form-control" readonly value="${creationDate}"/>
                    </div>
                </c:if>

                <c:if test="${not empty changeDate}">
                    <div class="col">
                        <label class="form-label"><mvc:message code="default.changeDate"/></label>
                        <input type="text" class="form-control" readonly value="${changeDate}"/>
                    </div>
                </c:if>

                <div class="col-12">
                    <div class="hidden" data-field-vis-default="" data-field-vis-hide="#allowed-values-block"></div>

                    <div class="row g-1">
                        <div class="col-12">
                            <div class="form-check form-switch">
                                <mvc:checkbox path="useAllowedValues" id="useAllowedValues" cssClass="form-check-input" role="switch"
                                              data-field-vis="" data-field-vis-show="#allowed-values-block" />
                                <label class="form-label form-check-label" for="useAllowedValues">
                                    <mvc:message code="settings.FieldFixedValue"/>
                                    <a href="#" class="icon icon-question-circle" data-help="recipient/profileField/FixedValue.xml"></a>
                                </label>
                            </div>
                        </div>

                        <div id="allowed-values-block" class="col-12">
                            <div id="fixed-values-container" class="row g-1">
                                <%-- Loads by JS --%>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </mvc:form>

    <c:if test="${not isNewField}">
        <div id="usages-tile" class="tile" data-editable-tile>
            <div class="tile-body">
                <div class="table-wrapper" data-web-storage="profile-fields-dependents-overview" data-js-table="profile-field-dependents-table">
                    <div class="table-wrapper__header">
                        <h1 class="table-wrapper__title"><mvc:message code="default.usedIn" /></h1>
                        <div class="table-wrapper__controls">
                            <%@include file="../../common/table/toggle-truncation-btn.jspf" %>
                            <jsp:include page="../../common/table/entries-label.jsp" />
                        </div>
                    </div>
                </div>

                <script id="profile-field-dependents-table" type="application/json">
                    {
                        "columns": [
                            {
                                "headerName": "<mvc:message code='default.Type'/>",
                                "editable": false,
                                "cellRenderer": "MustacheTemplateCellRender",
                                "cellRendererParams": {"templateName": "profile-field-dependency-type"},
                                "field": "type"
                            },
                            {
                                "headerName": "<mvc:message code='Name'/>",
                                "editable": false,
                                "cellRenderer": "StringCellRenderer",
                                "field": "shortname"
                            }
                        ],
                        "data": ${emm:toJson(dependents)},
                        "options": {"viewLinkTemplate": "profile-field-dependency-view-link"}
                    }
                </script>
            </div>
        </div>
    </c:if>
</div>

<script id="fixed-value-row" type="text/x-mustache-template">
    <div class="col-12" data-fixed-value-row>
        <div class="row g-1">
            <div class="col">
                {{ if (fieldType === 'DATE') { }}
                    <input type="text" class="form-control js-datepicker" name="allowedValues" value="{{- value }}" />
                {{ } else if (fieldType === 'DATETIME') { }}
                    <div data-field="datetime" data-property="allowedValues" data-field-options="value: '{{- value }}'"></div>
                {{ } else { }}
                    <input type="text" class="form-control" name="allowedValues" value="{{- value }}" />
                {{ } }}
            </div>

            <div class="col-auto">
                {{ if (isLastRow) { }}
                    <button type="button" class="btn btn-icon btn-primary" data-action="add-fixed-value">
                        <i class="icon icon-plus"></i>
                    </button>
                {{ } else { }}
                    <button type="button" class="btn btn-icon btn-danger" data-action="delete-fixed-value">
                        <i class="icon icon-trash-alt"></i>
                    </button>
                {{ } }}
            </div>
        </div>
    </div>
</script>

<script id="profile-field-dependency-type" type="text/x-mustache-template">
    <span class="text-truncate-table">
        {{ if ('${DEPENDENT_TYPE_WORKFLOW.name()}' === value) { }}
            <mvc:message code="workflow.single"/>
        {{ } else if ('${DEPENDENT_TYPE_TARGET_GROUP.name()}' === value) { }}
            <mvc:message code="Target"/>
        {{ } else if ('${DEPENDENT_TYPE_IMPORT_PROFILE.name()}' === value) { }}
            <mvc:message code="import.ImportProfile"/>
        {{ } else if ('${DEPENDENT_TYPE_EXPORT_PROFILE.name()}' === value) { }}
            <mvc:message code="export"/>
        {{ } else if ('${DEPENDENT_TYPE_TRIGGER.name()}' === value) { }}
            <mvc:message code="userform.usesActions"/>
        {{ } }}
    </span>
</script>

<script id="profile-field-dependency-view-link" type="text/x-mustache-template">
    {{ if ('${DEPENDENT_TYPE_WORKFLOW.name()}' === type) { }}
    /workflow/{{- id }}/view.action
    {{ } else if ('${DEPENDENT_TYPE_TARGET_GROUP.name()}' === type) { }}
    /target/{{- id }}/view.action
    {{ } else if ('${DEPENDENT_TYPE_IMPORT_PROFILE.name()}' === type) { }}
    /import-profile/{{- id }}/view.action
    {{ } else if ('${DEPENDENT_TYPE_EXPORT_PROFILE.name()}' === type) { }}
    /export/{{- id }}/view.action
    {{ } else if ('${DEPENDENT_TYPE_TRIGGER.name()}' === type) { }}
    /action/{{- id }}/view.action
    {{ } }}
</script>
