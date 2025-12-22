<%@ page import="com.agnitas.beans.Recipient" %>
<%@ page import="com.agnitas.util.importvalues.ImportMode" %>
<%@ page import="com.agnitas.beans.ColumnMapping" %>
<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.import_profile.form.ImportProfileForm"--%>
<%--@elvariable id="genderMappingJoined" type="java.lang.String"--%>
<%--@elvariable id="isGenderSectionFocused" type="java.lang.Boolean"--%>

<%--@elvariable id="charsets" type="com.agnitas.util.importvalues.Charset[]"--%>
<%--@elvariable id="delimiters" type="com.agnitas.util.importvalues.TextRecognitionChar[]"--%>
<%--@elvariable id="dateFormats" type="com.agnitas.util.importvalues.DateFormat[]"--%>

<%--@elvariable id="isUserHasPermissionForSelectedMode" type="java.lang.Boolean"--%>
<%--@elvariable id="importModes" type="com.agnitas.util.importvalues.ImportMode[]"--%>
<%--@elvariable id="availableTimeZones" type="java.lang.String[]"--%>
<%--@elvariable id="availableImportProfileFields" type="java.util.List<com.agnitas.emm.core.service.RecipientFieldDescription>"--%>
<%--@elvariable id="checkForDuplicatesValues" type="com.agnitas.util.importvalues.CheckForDuplicates[]"--%>
<%--@elvariable id="importProcessActions" type="java.util.List<com.agnitas.beans.ImportProcessAction>"--%>

<%--@elvariable id="profileFields" type="java.util.Map<java.lang.String, com.agnitas.beans.ProfileField>"--%>
<%--@elvariable id="columnMappings" type="java.util.List<com.agnitas.beans.ColumnMapping>"--%>
<%--@elvariable id="isClientForceSendingActive" type="java.lang.Boolean"--%>
<%--@elvariable id="allowedModesForAllMailinglists" type="java.util.List<java.lang.Integer>"--%>

<c:set var="MAILTYPE_TEXT" value="<%= Recipient.MAILTYPE_TEXT %>"/>
<c:set var="MAILTYPE_HTML" value="<%= Recipient.MAILTYPE_HTML %>"/>
<c:set var="MAILTYPE_HTML_OFFLINE" value="<%= Recipient.MAILTYPE_HTML_OFFLINE %>"/>

<c:set var="DO_NOT_IMPORT" value="<%= ColumnMapping.DO_NOT_IMPORT %>"/>
<c:set var="isNewProfile" value="${form.id == 0}" />

<mvc:form id="import-profile-view" cssClass="tiles-container" servletRelativeAction="/import-profile/save.action" modelAttribute="form" enctype="multipart/form-data"
          data-form="resource" data-controller="import-profile" data-initializer="import-profile-view" data-editable-view="${agnEditViewKey}">

    <script id="config:import-profile-view" type="application/json">
        {
            "genderMappings": ${emm:toJson(genderMappingJoined)},
            "availableGenderIntValues": ${emm:toJson(availableGenderIntValues)},
            "isClientForceSendingActive": ${isClientForceSendingActive}
        }
    </script>

    <div class="tiles-block flex-column">
        <div id="general-settings-tile" class="tile" data-editable-tile style="flex: 1">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="mailing.generalSettings" /></h1>
            </div>

            <div class="tile-body vstack gap-3 js-scrollable">
                <mvc:hidden path="id" />

                <div>
                    <label for="profileName" class="form-label">
                        <mvc:message code="default.Name"/> *
                    </label>
                    <mvc:text path="name" cssClass="form-control" id="profileName" maxlength="99" />
                </div>

                <div data-hide-by-select="#import_mode_select" data-hide-by-select-values="${ImportMode.TO_BLACKLIST.intValue}">
                    <div class="row g-1">
                        <div class="col">
                            <label for="recipient-mailinglists" class="form-label">
                                <mvc:message code="recipient.Mailinglists"/>
                            </label>
                        </div>

                        <c:if test="${isAllMailinglistsAllowed}">
                            <div class="col-auto" data-show-by-select="#import_mode_select" data-show-by-select-values="${allowedModesForAllMailinglists}">
                                <div class="form-check form-switch">
                                    <mvc:checkbox id="allMalinglistsCheckbox" path="mailinglistsAll" data-action="allMailinglists-toggle" role="switch" cssClass="form-check-input" disabled="${!isAllowedToShowMailinglists}" />
                                    <label class="form-label form-check-label text-truncate fw-normal" for="allMalinglistsCheckbox">
                                        <mvc:message code="import.mailinglists.all"/>
                                    </label>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <mvc:select id="recipient-mailinglists" path="selectedMailinglists" cssClass="form-control" multiple="true" disabled="${!isAllowedToShowMailinglists}">
                        <c:forEach var="mailinglist" items="${availableMailinglists}">
                            <mvc:option value="${mailinglist.id}">${fn:escapeXml(mailinglist.shortname)}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <%@ include file="fragments/mediatype_settings.jspf" %>

                <div>
                    <label for="import_email" class="form-label text-truncate">
                        <mvc:message code="import.profile.report.email"/>
                        <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/ReportEmail.xml"></a>
                    </label>

                    <mvc:select id="import_email" path="mailForReport" dynamicTags="true" cssClass="form-control" placeholder="${emailPlaceholder}" />
                </div>

                <div>
                    <label for="import_error_email" class="form-label text-truncate">
                        <mvc:message code="error.import.profile.email"/>
                        <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/ErrorEmail.xml"></a>
                    </label>

                    <mvc:select id="import_error_email" path="mailForError" dynamicTags="true" cssClass="form-control" placeholder="${emailPlaceholder}" />
                </div>
            </div>
        </div>

        <div id="file-settings-tile" class="tile" data-editable-tile style="flex: 1">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="import.profile.file.settings" /></h1>
            </div>

            <div class="tile-body js-scrollable">
                <div class="row g-3" data-field="toggle-vis">
                    <%@ include file="fragments/datatype_settings.jspf" %>

                    <div id="separator-block" class="col">
                        <label for="recipient-import-format-separator" class="form-label">
                            <mvc:message code="import.Separator"/>
                            <a href="#" class="icon icon-question-circle" data-help="importwizard/step_1/Separator.xml"></a>
                        </label>

                        <mvc:select id="recipient-import-format-separator" path="separator" cssClass="form-control">
                            <mvc:option value="0">;</mvc:option>
                            <mvc:option value="1">,</mvc:option>
                            <mvc:option value="2">|</mvc:option>
                            <mvc:option value="3">Tab</mvc:option>
                            <mvc:option value="4">^</mvc:option>
                        </mvc:select>
                    </div>

                    <div class="col">
                        <label for="import_decimalseparator" class="form-label text-truncate">
                            <mvc:message code="csv.DecimalSeparator" />
                            <a href="#" class="icon icon-question-circle" data-help="importwizard/step_1/DecimalSeparator.xml"></a>
                        </label>
                        <mvc:select id="import_decimalseparator" path="decimalSeparator" cssClass="form-control">
                            <mvc:option value=".">.</mvc:option>
                            <mvc:option value=",">,</mvc:option>
                        </mvc:select>
                    </div>

                    <div class="col-6">
                        <label for="recipient-import-format-charset" class="form-label text-truncate">
                            <mvc:message code="mailing.Charset"/>
                            <a href="#" class="icon icon-question-circle" data-help="importwizard/step_1/Charset.xml"></a>
                        </label>

                        <mvc:select id="recipient-import-format-charset" path="charset" cssClass="form-control">
                            <mvc:options items="${charsets}" itemValue="intValue" itemLabel="charsetName"/>
                        </mvc:select>
                    </div>

                    <div id="text-recognition-block" class="col-6">
                        <label for="recipient-import-format-delimiter" class="form-label text-truncate">
                            <mvc:message code="import.Delimiter"/>
                            <a href="#" class="icon icon-question-circle" data-help="importwizard/step_1/Delimiter.xml"></a>
                        </label>

                        <mvc:select id="recipient-import-format-delimiter" path="textRecognitionChar" cssClass="form-control">
                            <c:forEach var="delimiter" items="${delimiters}">
                                <mvc:option value="${delimiter.intValue}">
                                    <mvc:message code="${delimiter.publicValue}"/>
                                </mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>

                    <div class="col-6">
                        <label for="recipient-import-format-dateformat" class="form-label text-truncate">
                            <mvc:message code="import.dateFormat"/>
                            <a href="#" class="icon icon-question-circle" data-help="importwizard/step_1/DateFormat.xml"></a>
                        </label>

                        <mvc:select id="recipient-import-format-dateformat" path="dateFormat" cssClass="form-control">
                            <mvc:options items="${dateFormats}" itemValue="intValue" itemLabel="publicValue"/>
                        </mvc:select>
                    </div>

                    <div class="col-6">
                        <label for="zipPassword" class="form-label text-truncate">
                            <mvc:message code="import.zipPassword"/>
                            <a href="#" class="icon icon-question-circle" data-help="importwizard/step_1/ImportZipped.xml"></a>
                        </label>

                        <mvc:text id="zipPassword" path="zipPassword" cssClass="form-control" maxlength="99" />
                    </div>

                    <%@include file="fragments/file_settings_nocsvheaders.jspf" %>
                </div>
            </div>
        </div>
    </div>

    <div id="import-settings-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title text-truncate"><mvc:message code="import.profile.process.settings" /></h1>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3" data-field="toggle-vis">
                <div class="col-12">
                    <label for="import_mode_select" class="form-label">
                        <mvc:message code="settings.Mode"/>
                        <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/Mode.xml"></a>
                    </label>

                    <mvc:select id="import_mode_select" path="importMode" cssClass="form-control" data-action="change-mode"
                                data-field-vis="" disabled="${!isUserHasPermissionForSelectedMode && !isNewProfile}">
                        <c:forEach var="importMode" items="${importModes}">
                            <c:choose>
                                <c:when test="${[ImportMode.ADD, ImportMode.ADD_AND_UPDATE, ImportMode.ADD_AND_UPDATE_FORCED, ImportMode.ADD_AND_UPDATE_EXCLUSIVE].contains(importMode)}">
                                    <c:set var="hideAttr" value=""/>
                                    <c:set var="showAttr" value="#mailtype-block,#new-recipients-doi-action"/>
                                </c:when>
                                <c:when test="${importMode == ImportMode.UPDATE}">
                                    <c:set var="hideAttr" value="#mailtype-block"/>
                                    <c:set var="showAttr" value="#new-recipients-doi-action"/>
                                </c:when>
                                <c:otherwise>
                                    <c:set var="hideAttr" value="#new-recipients-doi-action"/>
                                    <c:set var="showAttr" value="#mailtype-block"/>
                                </c:otherwise>
                            </c:choose>

                            <mvc:option value="${importMode.intValue}" data-field-vis-hide="${hideAttr}" data-field-vis-show="${showAttr}">
                                <mvc:message code="${importMode.messageKey}"/>
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <%@ include file="fragments/action_settings-extended_nullvalue.jspf" %>

                <div class="col-12">
                    <label for="import_key_column" class="form-label">
                        <mvc:message code="import.keycolumn"/>
                        <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/KeyColumn.xml"></a>
                    </label>

                    <c:choose>
                        <c:when test="${form.firstKeyColumn eq 'customer_id' and not isCustomerIdImportAllowed}">
                            <input type="text" class="form-control" disabled value="${form.firstKeyColumn}">
                        </c:when>
                        <c:otherwise>
                            <mvc:select id="import_key_column" path="firstKeyColumn" cssClass="form-control">
                                <mvc:options items="${availableImportProfileFields}" itemLabel="shortName" itemValue="columnName" />
                            </mvc:select>
                        </c:otherwise>
                    </c:choose>
                </div>

                <c:if test="${isCheckForDuplicatesAllowed}">
                    <div class="col-12">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="shouldCheckForDuplicates" id="checkForDuplicates" role="switch" cssClass="form-check-input"/>
                            <label class="form-label form-check-label text-truncate" for="checkForDuplicates">
                                <mvc:message code="import.doublechecking"/>
                                <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/Doublechecking.xml"></a>
                            </label>
                        </div>
                    </div>
                </c:if>

                <c:if test="${isUpdateDuplicatesChangeAllowed}">
                    <div class="col-12">
                        <div class="form-check form-switch">
                            <mvc:checkbox path="updateAllDuplicates" id="import_duplicates" role="switch" cssClass="form-check-input"/>
                            <label class="form-label form-check-label text-truncate" for="import_duplicates">
                                <mvc:message code="import.profile.updateAllDuplicates"/>
                                <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/UpdateAllDuplicates.xml"></a>
                            </label>
                        </div>
                    </div>
                </c:if>

                <div id="mailtype-block" class="col-12">
                    <label for="import_mailingtype" class="form-label">
                        <mvc:message code="recipient.mailingtype"/>
                        <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/MailType.xml"></a>
                    </label>

                    <mvc:select id="import_mailingtype" path="defaultMailType" cssClass="form-control">
                        <mvc:option value="${MAILTYPE_TEXT}"><mvc:message code="recipient.mailingtype.text"/></mvc:option>
                        <mvc:option value="${MAILTYPE_HTML}"><mvc:message code="HTML"/></mvc:option>
                        <mvc:option value="${MAILTYPE_HTML_OFFLINE}"><mvc:message code="recipient.mailingtype.htmloffline"/></mvc:option>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label for="import_processaction" class="form-label">
                        <mvc:message code="import.processPreImportAction"/>
                        <a href="#" class="icon icon-question-circle" data-help="importwizard/step_2/ProcessImportAction.xml"></a>
                    </label>

                    <mvc:select id="import_processaction" path="importProcessActionID" cssClass="form-control" disabled="${!isPreprocessingAllowed}">
                        <mvc:option value="0"><mvc:message code="none"/></mvc:option>
                        <mvc:options items="${importProcessActions}" itemValue="importactionID" itemLabel="name" />
                    </mvc:select>
                </div>

                <div class="col-6">
                    <label for="reportLocale" class="form-label text-truncate">
                        <mvc:message code="import.report.locale" />
                    </label>

                    <mvc:select id="reportLocale" path="reportLocale" cssClass="form-control">
                        <mvc:option value="de_DE"><mvc:message code="settings.German" /></mvc:option>
                        <mvc:option value="en_US"><mvc:message code="settings.English" /></mvc:option>
                        <mvc:option value="fr_FR"><mvc:message code="settings.French" /></mvc:option>
                        <mvc:option value="es_ES"><mvc:message code="settings.Spanish" /></mvc:option>
                        <mvc:option value="pt_PT"><mvc:message code="settings.Portuguese" /></mvc:option>
                        <mvc:option value="nl_NL"><mvc:message code="settings.Dutch" /></mvc:option>
                        <mvc:option value="it_IT"><mvc:message code="settings.Italian" /></mvc:option>
                    </mvc:select>
                </div>

                <div class="col-6">
                    <label for="reportTimezone" class="form-label text-truncate">
                        <mvc:message code="import.report.timezone" />
                    </label>

                    <mvc:select id="reportTimezone" path="reportTimezone" cssClass="form-control">
                        <c:forEach var="timeZone" items="${availableTimeZones}">
                            <mvc:option value="${timeZone}">${timeZone}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <%@ include file="fragments/action_settings-extended_actions.jspf" %>
            </div>
        </div>
    </div>

    <div class="tiles-block flex-column">
        <div id="gender-settings-tile" class="tile" data-editable-tile style="flex: 1">
            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="import.profile.gender.settings" /></h1>
            </div>

            <div class="tile-body js-scrollable">
                <div id="gender-mappings-block" class="row g-1">
                    <%-- Loads by JS --%>
                </div>
            </div>
        </div>

        <div id="manage-fields-tile" class="tile" data-initializer="import-profile-mappings" data-editable-tile style="flex: 1">
            <c:if test="${not isNewProfile}">
                <c:set var="profileFieldsAsJson" value="${emm:toJson(profileFields)}" />
                <script type="application/json" id="config:import-profile-mappings">
                    {
                        "columns" : ${profileFieldsAsJson},
                        "columnMappings": ${emm:toJson(columnMappings)},
                        "doNotImportValue": ${emm:toJson(DO_NOT_IMPORT)}
                    }
                </script>
            </c:if>

            <div class="tile-header">
                <h1 class="tile-title text-truncate"><mvc:message code="import.ManageColumns" /></h1>

                <c:if test="${not isNewProfile}">
                    <div class="tile-controls">
                        <button type="button" class="btn btn-secondary btn-icon" data-action="show-column-mappings">
                            <i class="icon icon-external-link-alt"></i>
                        </button>
                    </div>
                </c:if>
            </div>

            <div class="tile-body js-scrollable">
                <div class="row g-3">
                    <c:if test="${not isNewProfile}">
                        <div class="col-12 d-flex gap-1">
                            <input id="uploadFile" type="file" name="uploadFile" class="form-control">
                            <button id="upload-mappings-btn" type="button" class="btn btn-icon btn-primary" data-action="upload-column-mappings" data-tooltip="<mvc:message code="button.Upload" />">
                                <i class="icon icon-cloud-upload-alt"></i>
                            </button>
                        </div>
                    </c:if>

                    <%@ include file="fragments/profile-automatic-mapping-switch.jspf" %>

                    <div class="col-12">
                        <div class="notification-simple notification-simple--lg notification-simple--info">
                            <span><mvc:message code="${isNewProfile ? 'hint.import.manage.fields.save' : 'export.CsvMappingMsg'}" /></span>
                        </div>
                    </div>
                    <c:if test="${not isNewProfile}">
                        <div class="col-12">
                            <div id="column-mappings-block" class="row g-1">
                                <%-- Loads by JS --%>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</mvc:form>

<c:if test="${not isNewProfile}">
    <script type="application/json" data-initializer="import-profile-mappings-validator">
        {
            "columns": ${profileFieldsAsJson},
            "doNotImportValue": ${emm:toJson(DO_NOT_IMPORT)}
        }
   </script>
</c:if>

<%@ include file="fragments/import-profile-mappings-templates.jspf" %>

