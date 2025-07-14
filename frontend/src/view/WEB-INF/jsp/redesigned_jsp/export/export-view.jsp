<%@ page language="java" contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@page import="com.agnitas.emm.common.UserStatus"%>
<%@page import="com.agnitas.service.RecipientExportWorker"%>
<%@ page import="com.agnitas.util.DbColumnType" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="localeDatePattern" type="java.lang.String"--%>
<%--@elvariable id="timeZones" type="java.lang.String[]"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="targetGroups" type="java.util.List<com.agnitas.beans.TargetLight>"--%>
<%--@elvariable id="profileFields" type="java.util.List<com.agnitas.beans.ProfileField>"--%>
<%--@elvariable id="dateFormats" type="com.agnitas.util.importvalues.DateFormat[]"--%>
<%--@elvariable id="dateTimeFormats" type="com.agnitas.util.importvalues.DateFormat[]"--%>
<%--@elvariable id="exportForm" type="com.agnitas.emm.core.export.form.ExportForm"--%>
<%--@elvariable id="id" type="java.lang.Integer"--%>
<%--@elvariable id="isManageAllowed" type="java.lang.Boolean"--%>
<%--@elvariable id="adminHasDisabledMailingLists" type="java.lang.Boolean"--%>
<%--@elvariable id="isOwnColumnsExportAllowed" type="java.lang.Boolean"--%>

<c:set var="NO_MAILINGLIST" value="<%= RecipientExportWorker.NO_MAILINGLIST %>" scope="page" />
<c:set var="ALL_MAILINGLISTS" value="<%= RecipientExportWorker.ALL_MAILINGLISTS %>" scope="page" />
<c:set var="GENERIC_TYPE_VARCHAR" value="<%= DbColumnType.GENERIC_TYPE_VARCHAR %>" scope="page" />
<c:set var="GENERIC_TYPE_INTEGER" value="<%= DbColumnType.GENERIC_TYPE_INTEGER %>" scope="page" />
<c:set var="GENERIC_TYPE_FLOAT" value="<%= DbColumnType.GENERIC_TYPE_FLOAT %>" scope="page" />
<c:set var="GENERIC_TYPE_DATE" value="<%= DbColumnType.GENERIC_TYPE_DATE %>" scope="page" />
<c:set var="GENERIC_TYPE_DATETIME" value="<%= DbColumnType.GENERIC_TYPE_DATETIME %>" scope="page" />
<c:set var="BOUNCE_USER_STATUS_CODE" value="<%= UserStatus.Bounce.getStatusCode() %>" scope="page" />

<c:set var="localeDatePattern" value="${fn:toLowerCase(localeDatePattern)}"/>

<mvc:form cssClass="tiles-container" servletRelativeAction="/export/${id}/save.action" id="exportForm" modelAttribute="exportForm" data-form="resource"
          data-form-focus="shortname"
          data-controller="export-view"
          data-initializer="export-view"
          data-validator="export/form"
          data-editable="${isManageAllowed}"
          data-editable-view="${agnEditViewKey}">
    <script id="config:export-view" type="application/json">
        {
            "bounceUserStatusCode": ${BOUNCE_USER_STATUS_CODE}
        }
    </script>
    
    <div class="tiles-block flex-column">
        <div class="tile" style="flex-shrink: 0; flex-basis: min-content; flex-grow: 1" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="mailing.generalSettings"/></h1>
            </div>
            <div class="tile-body form-column">
                <div>
                    <label for="recipient-export-name" class="form-label">
                        <c:set var="nameMsg"><mvc:message code="default.Name"/></c:set>
                        ${nameMsg} *
                    </label>
                    <mvc:text path="shortname" id="recipient-export-name" cssClass="form-control" maxlength="99" placeholder="${nameMsg}"/>
                </div>
                <div>
                    <label for="recipient-export-description" class="form-label">
                        <c:set var="descriptionMsg"><mvc:message code="default.description"/></c:set>
                        ${descriptionMsg}
                    </label>
                    <mvc:textarea path="description" id="recipient-export-description" cssClass="form-control" rows="1" placeholder="${descriptionMsg}"/>
                </div>
            </div>
        </div>
    
        <div class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="export.settings"/></h1>
            </div>
            <div class="tile-body form-column js-scrollable">
                <c:set var="mailinglistSelect">
                    <div>
                        <label for="recipient-export-mailinglist" class="form-label"><mvc:message code="Mailinglist"/></label>
                        <mvc:select path="mailinglistId" id="recipient-export-mailinglist" cssClass="js-select form-control">
                            <c:if test="${not adminHasDisabledMailingLists}">
                                <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                                <mvc:option value="${NO_MAILINGLIST}"><mvc:message code="No_Mailinglist"/></mvc:option>
                            </c:if>
                            <mvc:options items="${mailinglists}" itemValue="id" itemLabel="shortname"/>
                        </mvc:select>
                    </div>
                </c:set>
                
                <%@ include file="fragments/export-mailinglist-select.jspf" %>

                ${mailinglistSelect}

                <div>
                    <label for="recipient-export-targetgroup" class="form-label"><mvc:message code="target.Target"/></label>
                    <mvc:select path="targetId" id="recipient-export-targetgroup" cssClass="form-control js-select">
                        <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                        <mvc:options items="${targetGroups}" itemValue="id" itemLabel="targetName"/>
                    </mvc:select>
                </div>
                <div>
                    <label for="recipient-export-recipienttype" class="form-label"><mvc:message code="recipient.RecipientType"/></label>
                    <mvc:select path="userType" cssClass="form-control js-select" id="recipient-export-recipienttype">
                        <c:forEach var="userType" items="${availableUserTypeOptions}">
                            <mvc:option value="${userType.typeCode}">
                                <mvc:message code="${userType.messageKey}"/>
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                <div>
                    <label for="recipient-export-recipientstatus" class="form-label"><mvc:message code="recipient.RecipientStatus"/></label>
                    <mvc:select path="userStatus" cssClass="form-control js-select" id="recipient-export-recipientstatus" data-action="add-bounce-col-to-choose">
                        <mvc:option value="0"><mvc:message code="default.All"/></mvc:option>
                        <c:forEach var="userStatus" items="${availableUserStatusOptions}">
                            <mvc:option value="${userStatus.statusCode}">
                                <mvc:message code="${userStatus.messageKey}" />
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                
                <div>
                    <label class="form-label" for="user-columns"><mvc:message code="export.columns"/></label>
                    <c:set var="allColumnsStr"><mvc:message code="export.columns.all"/></c:set>
                    <mvc:select path="userColumns" id="user-columns" multiple="true" cssClass="form-control js-select" data-placeholder="${allColumnsStr}" data-action="user-columns-update">
                        <c:forEach var="profileField" items="${profileFields}">
                            <c:set var="colType">
                                <c:choose>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_VARCHAR}'>
                                        <mvc:message code='statistic.alphanumeric'/>
                                    </c:when>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_INTEGER or profileField.dataType == GENERIC_TYPE_FLOAT}'>
                                        <mvc:message code='statistic.numeric'/>
                                    </c:when>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_DATETIME}'>
                                        <mvc:message code='settings.fieldType.DATETIME'/>
                                    </c:when>
                                    <c:when test='${profileField.dataType == GENERIC_TYPE_DATE}'>
                                        <mvc:message code='settings.fieldType.DATE'/>
                                    </c:when>
                                    <c:otherwise>
                                        ${profileField}
                                    </c:otherwise>
                                </c:choose>
                            </c:set>
                            <mvc:option value="${fn:toLowerCase(profileField.column)}">${fn:escapeXml(profileField.shortname)} (${colType})</mvc:option>
                        </c:forEach>
                        <mvc:option id="mailing-bounce-column-option" value="mailing_bounce"><mvc:message code="report.bounce.reason"/> (<mvc:message code="statistic.alphanumeric"/>)</mvc:option>
                    </mvc:select>
                    <div id="24h-col-info" class="notification-simple notification-simple--info mt-1">
                        <mvc:message code="export.wizard.hint.export.columns"/>
                    </div>
                </div>
                
                <c:if test="${isOwnColumnsExportAllowed}">
                    <div>
                        <label class="form-label" for="custom-column-mappings"><mvc:message code="export.columns.add.own"/></label>
                        <div id="custom-column-mappings">
                            <script data-config type="application/json">
                                {
                                  "data": ${emm:toJson(exportForm.customColumns)},
                                  "readonly": ${not isManageAllowed}
                                }
                            </script>
                            <script data-row-template type="text/x-mustache-template">
                                <tr>
                                    <td><input type="text" class="form-control" data-name="dbColumn" value="{{- dbColumn }}" maxlength="30" placeholder="<mvc:message code='export.Column_Name'/>" ${isManageAllowed ? '' : 'readonly'}/></td>
                                    <td><input type="text" class="form-control" data-name="defaultValue" value="{{- defaultValue }}" placeholder="<mvc:message code='default.value.optional'/>" ${isManageAllowed ? '' : 'readonly'}/></td>
                                </tr>
                            </script>

                            <%-- populated with js. see class CustomMappingsTable . In case of modification recheck export/form validator and controller --%>

                        </div>
                    </div>
                </c:if>

                <div>
                    <label class="form-label">
                        <mvc:message code="export.mailinglist.status"/>
                        <a href="#" class="icon icon-question-circle" data-help="export/Export_Mailinglist_Status.xml"></a>
                    </label>
                    <mvc:select path="mailinglists" id="mailinglists" multiple="true" cssClass="form-control js-select">
                        <c:forEach var="mailinglist" items="${mailinglists}">
                            <mvc:option value='${mailinglist.id}'>${mailinglist.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
            </div>
        </div>
    </div>

    <div class="tiles-block flex-column">
        <div class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="export.file.settings"/></h1>
            </div>
            <div class="tile-body form-column js-scrollable">
                <div class="row">
                    <div class="col">
                        <label for="recipient-export-format-separator" class="form-label"><mvc:message code="import.Separator"/></label>
                        <mvc:select path="separator" cssClass="form-control js-select" id="recipient-export-format-separator">
                            <mvc:option value=";">;</mvc:option>
                            <mvc:option value=",">,</mvc:option>
                            <mvc:option value="|">|</mvc:option>
                            <mvc:option value="t">Tab</mvc:option>
                            <mvc:option value="^">^</mvc:option>
                        </mvc:select>
                    </div>
                    <div class="col">
                        <label for="file-decimal-separator" class="form-label"><mvc:message code="csv.DecimalSeparator"/></label>
                        <mvc:select path="decimalSeparator" cssClass="form-control js-select" id="file-decimal-separator">
                            <mvc:option value=".">.</mvc:option>
                            <mvc:option value=",">,</mvc:option>
                        </mvc:select>
                    </div>
                    <div class="col">
                        <label for="recipient-export-format-delimiter" class="form-label"><mvc:message code="import.Delimiter"/></label>
                        <mvc:select path="delimiter" cssClass="form-control" id="recipient-export-format-delimiter">
                            <mvc:option value='"'><mvc:message code="delimiter.doublequote"/></mvc:option>
                            <mvc:option value="'"><mvc:message code="delimiter.singlequote"/></mvc:option>
                        </mvc:select>
                    </div>
                </div>
                
                <div>
                    <label for="file-text-marking" class="form-label"><mvc:message code="csv.alwaysQuote"/></label>
                    <mvc:select path="alwaysQuote" cssClass="form-control js-select" id="file-text-marking">
                        <mvc:option value="false"><mvc:message code="delimiter.ifneeded"/></mvc:option>
                        <mvc:option value="true"><mvc:message code="delimiter.always"/></mvc:option>
                    </mvc:select>
                </div>
                
                <div>
                    <label for="recipient-export-format-charset" class="form-label"><mvc:message code="mailing.Charset"/></label>
                    <mvc:select path="charset" cssClass="form-control js-select" id="recipient-export-format-charset">
                        <c:forEach var="charset" items="${availableCharsetOptions}">
                            <mvc:option value="${charset.charsetName}">
                                <mvc:message code="${charset.messageKey}" />
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <div class="row">
                    <div class="col">
                        <label for="file-date-format" class="form-label"><mvc:message code="csv.DateFormat"/></label>
                        <mvc:select path="dateFormat" cssClass="form-control js-select" id="file-date-format">
                            <c:forEach var="dateFormat" items="${dateFormats}">
                                <mvc:option value="${dateFormat}">${dateFormat.publicValue}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                    <div class="col">
                        <label for="file-date-time-format" class="form-label"><mvc:message code="csv.DateTimeFormat"/></label>
                        <mvc:select path="dateTimeFormat" cssClass="form-control js-select" id="file-date-time-format">
                            <c:forEach var="dateTimeFormat" items="${dateTimeFormats}">
                                <mvc:option value="${dateTimeFormat}">${dateTimeFormat.publicValue}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
            
                <div class="row">
                    <div class="col">
                        <label class="form-label" for="locale"><mvc:message code="import.report.locale" /></label>
                        <mvc:select path="locale" cssClass="form-control js-select" id="locale">
                            <mvc:option value="de_DE"><mvc:message code="settings.German"/></mvc:option>
                            <mvc:option value="en_US"><mvc:message code="settings.English"/></mvc:option>
                            <mvc:option value="fr_FR"><mvc:message code="settings.French"/></mvc:option>
                            <mvc:option value="es_ES"><mvc:message code="settings.Spanish"/></mvc:option>
                            <mvc:option value="pt_PT"><mvc:message code="settings.Portuguese"/></mvc:option>
                            <mvc:option value="nl_NL"><mvc:message code="settings.Dutch"/></mvc:option>
                            <mvc:option value="it_IT"><mvc:message code="settings.Italian"/></mvc:option>
                        </mvc:select>
                    </div>
                    <div class="col">
                        <label for="file-timezone" class="form-label"><mvc:message code="Timezone"/></label>
                        <mvc:select path="timezone" cssClass="form-control js-select" id="file-timezone">
                            <c:forEach var="availableTimeZone" items="${timeZones}">
                                <mvc:option value="${availableTimeZone}">${availableTimeZone}</mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </div>
    
                <div class="form-check form-switch">
                    <mvc:checkbox path="useDecodedValues" id="export-decoded-values" cssClass="form-check-input" role="switch"/>
                    <label class="form-label form-check-label" for="export-decoded-values">
                        <mvc:message code="export.decode.values"/>
                    </label>
                </div>
            </div>
        </div>

        <div class="tile" data-editable-tile>
            <div class="tile-header">
                <h1 class="tile-title"><mvc:message code="export.dates.limits"/></h1>
                <div class="tile-controls">
                    <label class="switch">
                        <mvc:checkbox path="timeLimitsLinkedByAnd"/>
                        <span>AND</span>
                        <span>OR</span>
                    </label>
                </div>
            </div>
            <div class="tile-body form-column js-scrollable">
                <div data-field="toggle-vis">
                    <label class="form-label" for="change-period-limit-type"><mvc:message code="export.dates.timestamp"/></label>
                    <select id="change-period-limit-type" class="form-control js-select" data-field-vis="">
                        <option data-field-vis-show="#change-period-limit-date-range-inputs"
                                data-field-vis-hide="#change-period-limit-days-inputs"><mvc:message code="birt.report.period.custom"/></option>
                        <option data-field-vis-hide="#change-period-limit-date-range-inputs"
                                data-field-vis-show="#change-period-limit-days-inputs"
                                ${exportForm.timestampLastDays > 0 ? 'selected' : ''}><mvc:message code="lastDays"/></option>
                    </select>
                    <div id="change-period-limit-date-range-inputs" class="inline-input-range mt-1" data-date-range="">
                        <div class="date-picker-container flex-grow-1">
                            <mvc:text id="timestamp-start" path="timestampStart" cssClass="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container flex-grow-1">
                            <mvc:text path="timestampEnd" cssClass="form-control js-datepicker"/>
                        </div>
                    </div>
                    <div id="change-period-limit-days-inputs" class="form-column-2 mt-1">
                        <mvc:text path="timestampLastDaysStr" type="number" cssClass="form-control" value="${exportForm.timestampLastDays == 0 ? '' : exportForm.timestampLastDays}"/>
                        <c:set var="includeCurentDayInputName" value="timestampIncludeCurrentDay"/>
                        <%@ include file="fragments/export-include-current-day-switch.jspf" %>
                    </div>
                </div>
                
                <div data-field="toggle-vis">
                    <label class="form-label" for="creation-period-limit-type"><mvc:message code="export.dates.creation_date"/></label>
                    <select id="creation-period-limit-type" class="form-control js-select" data-field-vis="">
                        <option data-field-vis-show="#creation-period-limit-date-range-inputs"
                                data-field-vis-hide="#creation-period-limit-days-inputs"><mvc:message code="birt.report.period.custom"/></option>
                        <option data-field-vis-hide="#creation-period-limit-date-range-inputs"
                                data-field-vis-show="#creation-period-limit-days-inputs"
                                ${exportForm.creationDateLastDays > 0 ? 'selected' : ''}><mvc:message code="lastDays"/></option>
                    </select>
                    <div id="creation-period-limit-date-range-inputs" class="inline-input-range mt-1" data-date-range="">
                        <div class="date-picker-container flex-grow-1">
                            <mvc:text path="creationDateStart" cssClass="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container flex-grow-1">
                            <mvc:text path="creationDateEnd" cssClass="form-control js-datepicker"/>
                        </div>
                    </div>
                    <div id="creation-period-limit-days-inputs" class="form-column-2 mt-1">
                        <mvc:text path="creationDateLastDaysStr" type="number" cssClass="form-control" value="${exportForm.creationDateLastDays == 0 ? '' : exportForm.creationDateLastDays}"/>
                        <c:set var="includeCurentDayInputName" value="creationDateIncludeCurrentDay"/>
                        <%@ include file="fragments/export-include-current-day-switch.jspf" %>
                    </div>
                </div>
                
                <div data-field="toggle-vis">
                    <label class="form-label" for="mailing-list-period-limit-type"><mvc:message code="export.dates.mailinglists"/></label>
                    <select id="mailing-list-period-limit-type" class="form-control js-select" data-field-vis="">
                        <option data-field-vis-show="#mailing-list-period-limit-date-range-inputs"
                                data-field-vis-hide="#mailing-list-period-limit-days-inputs"><mvc:message code="birt.report.period.custom"/></option>
                        <option data-field-vis-hide="#mailing-list-period-limit-date-range-inputs"
                                data-field-vis-show="#mailing-list-period-limit-days-inputs"
                                ${exportForm.mailinglistBindLastDays > 0 ? 'selected' : ''}><mvc:message code="lastDays"/></option>
                    </select>
                    <div id="mailing-list-period-limit-date-range-inputs" class="inline-input-range mt-1" data-date-range="">
                        <div class="date-picker-container flex-grow-1">
                            <mvc:text path="mailinglistBindStart" cssClass="form-control js-datepicker"/>
                        </div>
                        <div class="date-picker-container flex-grow-1">
                            <mvc:text path="mailinglistBindEnd" cssClass="form-control js-datepicker"/>
                        </div>
                    </div>
                    <div id="mailing-list-period-limit-days-inputs" class="form-column-2 mt-1">
                        <mvc:text path="mailinglistBindLastDaysStr" type="number" cssClass="form-control" value="${exportForm.mailinglistBindLastDays == 0 ? '' : exportForm.mailinglistBindLastDays}"/>
                        <c:set var="includeCurentDayInputName" value="mailinglistBindIncludeCurrentDay"/>
                        <%@ include file="fragments/export-include-current-day-switch.jspf" %>
                    </div>
                </div>
            </div>
        </div>
    </div>
</mvc:form>
