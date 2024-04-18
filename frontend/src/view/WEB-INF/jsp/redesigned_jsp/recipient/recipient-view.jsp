<%@page import="com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField"%>
<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="org.agnitas.util.importvalues.MailType" %>
<%@ page import="com.agnitas.beans.ProfileField" %>
<%@ page import="org.agnitas.util.DbColumnType" %>
<%@ page import="org.agnitas.emm.core.recipient.RecipientUtils" %>
<%@page import="org.agnitas.dao.UserStatus"%>
<%@ page import="com.agnitas.emm.core.mediatypes.common.MediaTypes" %>
<%@ page import="org.agnitas.beans.BindingEntry" %>
<%@ page import="org.agnitas.util.importvalues.Gender" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="isRecipientEmailInUseWarningEnabled" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglists" type="java.util.List"--%>
<%--@elvariable id="adminDateTimeFormatWithSeconds" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientForm"--%>
<%--@elvariable id="allowedEmptyEmail" type="java.lang.Boolean"--%>
<%--@elvariable id="disableTrackingVeto" type="java.lang.Boolean"--%>
<%--@elvariable id="columnDefinitions" type="java.util.List<com.agnitas.emm.core.recipient.dto.RecipientColumnDefinition>"--%>

<c:set var="GENERIC_TYPE_DATE" value="<%= DbColumnType.SimpleDataType.Date %>"/>
<c:set var="GENERIC_TYPE_DATETIME" value="<%= DbColumnType.SimpleDataType.DateTime %>"/>
<c:set var="GENERIC_TYPE_VARCHAR" value="<%= DbColumnType.SimpleDataType.Characters %>"/>
<c:set var="COLUMN_CUSTOMER_ID" value="<%= RecipientStandardField.CustomerID.getColumnName() %>"/>
<c:set var="COLUMN_DATASOURCE_ID" value="<%= RecipientStandardField.DatasourceID.getColumnName() %>"/>
<c:set var="COLUMN_LATEST_DATASOURCE_ID" value="<%= RecipientStandardField.LatestDatasourceID.getColumnName() %>"/>
<c:set var="COLUMN_CHANGE_DATE" value="<%= RecipientStandardField.ChangeDate.getColumnName() %>"/>
<c:set var="COLUMN_CREATION_DATE" value="<%= RecipientStandardField.CreationDate.getColumnName() %>"/>
<c:set var="COLUMN_CLEANED_DATE" value="<%= RecipientStandardField.CleanedDate.getColumnName() %>"/>
<c:set var="COLUMN_LASTCLICK_DATE" value="<%= RecipientStandardField.LastClickDate.getColumnName() %>"/>
<c:set var="COLUMN_LASTOPEN_DATE" value="<%= RecipientStandardField.LastOpenDate.getColumnName() %>"/>
<c:set var="COLUMN_LASTSEND_DATE" value="<%= RecipientStandardField.LastSendDate.getColumnName() %>"/>

<c:set var="MAILTYPE_TEXT" value="<%= MailType.TEXT %>" scope="page"/>
<c:set var="MAILTYPE_HTML" value="<%= MailType.HTML %>" scope="page"/>
<c:set var="MAILTYPE_HTML_OFFLINE" value="<%= MailType.HTML_OFFLINE %>" scope="page"/>

<c:set var="USER_STATUS_ACTIVE" value="<%= UserStatus.Active %>"/>
<c:set var="USER_STATUS_ADMIN_OUT" value="<%= UserStatus.AdminOut %>"/>
<c:set var="USER_STATUS_USER_OUT" value="<%= UserStatus.UserOut %>"/>

<c:set var="USER_TYPE_ADMIN" value="<%= BindingEntry.UserType.Admin.getTypeCode() %>"/>
<c:set var="USER_TYPE_TEST" value="<%= BindingEntry.UserType.TestUser.getTypeCode() %>"/>
<c:set var="USER_TYPE_NORMAL" value="<%= BindingEntry.UserType.World.getTypeCode() %>"/>
<c:set var="USER_TYPE_TEST_VIP" value="<%= BindingEntry.UserType.TestVIP.getTypeCode() %>"/>
<c:set var="USER_TYPE_NORMAL_VIP" value="<%= BindingEntry.UserType.WorldVIP.getTypeCode() %>"/>

<c:set var="GENDER_MALE" value="<%= Gender.MALE %>"/>
<c:set var="GENDER_FEMALE" value="<%= Gender.FEMALE %>"/>
<c:set var="GENDER_UNKNOWN" value="<%= Gender.UNKNOWN %>"/>
<c:set var="GENDER_PRAXIS" value="<%= Gender.PRAXIS %>"/>
<c:set var="GENDER_COMPANY" value="<%= Gender.COMPANY %>"/>

<c:set var="MEDIATYPE_EMAIL" value="<%= MediaTypes.EMAIL %>" />
<c:set var="MEDIATYPE_SMS" value="<%= MediaTypes.SMS %>" />
<c:set var="MEDIATYPE_POST" value="<%= MediaTypes.POST %>" />

<c:set var="POSSIBLE_MEDIATYPES" value="${[MEDIATYPE_EMAIL, MEDIATYPE_SMS, MEDIATYPE_POST]}"/>

<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>

<mvc:form id="recipient-detail-view" servletRelativeAction="/recipient/save.action" cssClass="tiles-container hidden" modelAttribute="form"
          data-form="resource" data-controller="recipient-view" data-action="recipient-save" data-editable-view="${agnEditViewKey}">

    <c:if test="${isRecipientEmailInUseWarningEnabled}">
        <script data-initializer="recipient-view" type="application/json">
            {
              "urls": {
                "CHECK_ADDRESS": "<c:url value='/recipient/${form.id}/checkAddress.action'/>",
                "CHECK_MATCH_ALTG": "<c:url value='/recipient/checkAltgMatch.action'/>",
                "SAVE_AND_BACK_TO_LIST": "<c:url value='/recipient/saveAndBackToList.action'/>",
                "EXISTING_USER_URL_PATTERN": "<c:url value='/recipient/:recipientID:/view.action'/>"
              }
            }
        </script>
    </c:if>

    <div id="settings-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="Settings" /></h1>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <div class="col-12">
                    <label class="form-label" for="recipient-salutation"><mvc:message code="recipient.Salutation" /></label>

                    <mvc:select path="gender" id="recipient-salutation" cssClass="form-control" multiple="false">
                        <mvc:option value="${GENDER_MALE}"><mvc:message code="recipient.gender.0.short"/></mvc:option>
                        <mvc:option value="${GENDER_FEMALE}"><mvc:message code="recipient.gender.1.short"/></mvc:option>
                        <emm:ShowByPermission token="recipient.gender.extended">
                            <mvc:option value="${GENDER_PRAXIS}"><mvc:message code="recipient.gender.4.short"/></mvc:option>
                            <mvc:option value="${GENDER_COMPANY}"><mvc:message code="recipient.gender.5.short"/></mvc:option>
                        </emm:ShowByPermission>
                        <mvc:option value="${GENDER_UNKNOWN}"><mvc:message code="recipient.gender.2.short"/></mvc:option>
                    </mvc:select>
                </div>

                <div class="col-12">
                    <label class="form-label" for="recipient-title"><mvc:message code="Title" /></label>
                    <mvc:text path="title" id="recipient-title" cssClass="form-control"
                              data-field-validator="length" data-validator-options="required: false, max: 100"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="recipient-firstname"><mvc:message code="Firstname" /></label>
                    <mvc:text path="firstname" id="recipient-firstname" cssClass="form-control"
                              data-field-validator="length" data-validator-options="required: false, max: 100"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="recipient-lastname"><mvc:message code="Lastname" /></label>
                    <mvc:text path="lastname" id="recipient-lastname" cssClass="form-control"
                              data-field-validator="length" data-validator-options="required: false, max: 100"/>
                </div>

                <div class="col-12">
                    <label class="form-label" for="recipient-email">
                        <mvc:message code="mailing.MediaType.0"/> *
                        <emm:ShowByPermission token="mailing.encrypted.send">
                            <mvc:message var="encryptedSendTooltip" code="${form.encryptedSend ? 'recipient.encrypted.possible' : 'recipient.encrypted.notpossible'}"/>
                            <i class="icon icon-lock${form.encryptedSend ? ' text-success' : '-open text-danger'} fs-3" data-tooltip="${encryptedSendTooltip}"></i>
                        </emm:ShowByPermission>
                    </label>
                    <c:if test="${allowedEmptyEmail}">
                        <mvc:text path="email" id="recipient-email" cssClass="form-control"/>
                    </c:if>
                    <c:if test="${not allowedEmptyEmail}">
                        <mvc:text path="email" id="recipient-email" cssClass="form-control" data-field="required"/>
                    </c:if>
                </div>

                <div class="col-12">
                    <div class="form-check form-switch">
                        <mvc:checkbox path="trackingVeto" id="trackingVeto" cssClass="form-check-input" role="switch" disabled="${disableTrackingVeto}" />
                        <label class="form-label form-check-label text-capitalize" for="trackingVeto"><mvc:message code="recipient.trackingVeto"/></label>
                    </div>
                </div>

                <div class="col-12">
                    <label class="form-label" for="recipient-mailtype"><mvc:message code="Mailtype"/></label>

                    <mvc:select path="mailtype" id="recipient-mailtype" cssClass="form-control js-select" multiple="false">
                        <mvc:option value="${MAILTYPE_TEXT}"><mvc:message code="${MAILTYPE_TEXT.messageKey}"/></mvc:option>
                        <mvc:option value="${MAILTYPE_HTML}"><mvc:message code="${MAILTYPE_HTML.messageKey}"/></mvc:option>
                        <mvc:option value="${MAILTYPE_HTML_OFFLINE}"><mvc:message code="${MAILTYPE_HTML_OFFLINE.messageKey}"/></mvc:option>
                    </mvc:select>
                </div>

                <%@include file="fragments/recipient-freq-counter.jspf" %>
            </div>
        </div>
    </div>

    <div id="profile-fields-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="recipient.fields" /></h1>
        </div>

        <div class="tile-body js-scrollable">
            <div class="d-flex flex-column gap-3">
                <c:forEach items="${columnDefinitions}" var="definition">
                    <c:if test="${definition.readable}">
                        <c:set var="hasFixedValues" value="${not empty definition.fixedValues}"/>
                        <c:set var="propName" value="additionalColumns[${definition.columnName}]"/>

                        <c:set var="fieldBlockClass" value="${definition.lineAfter ? 'tile-body__block pt-0' : ''}" />

                        <c:choose>
                            <c:when test="${definition.mainColumn}">
                                <c:choose>
                                    <c:when test="${definition.shortname eq COLUMN_CUSTOMER_ID}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-id">${COLUMN_CUSTOMER_ID}</label>
                                            <mvc:text path="id" id="recipient-id" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_DATASOURCE_ID}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-datasource-id">${COLUMN_DATASOURCE_ID}</label>
                                            <mvc:text path="dataSourceId" id="recipient-datasource-id" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_LATEST_DATASOURCE_ID}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-latest-datasource-id">${COLUMN_LATEST_DATASOURCE_ID}</label>
                                            <mvc:text path="latestDataSourceId" id="recipient-latest-datasource-id" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_CHANGE_DATE}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-timestamp">${COLUMN_CHANGE_DATE}</label>
                                            <mvc:text path="${propName}" id="recipient-timestamp" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_CREATION_DATE}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-timestamp">${COLUMN_CREATION_DATE}</label>
                                            <mvc:text path="${propName}" id="recipient-timestamp" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_CLEANED_DATE}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-timestamp">${COLUMN_CLEANED_DATE}</label>
                                            <mvc:text path="${propName}" id="recipient-timestamp" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_LASTCLICK_DATE}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-timestamp">${COLUMN_LASTCLICK_DATE}</label>
                                            <mvc:text path="${propName}" id="recipient-timestamp" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_LASTOPEN_DATE}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-timestamp">${COLUMN_LASTOPEN_DATE}</label>
                                            <mvc:text path="${propName}" id="recipient-timestamp" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                    <c:when test="${definition.shortname eq COLUMN_LASTSEND_DATE}">
                                        <div class="${fieldBlockClass}">
                                            <label class="form-label text-truncate" for="recipient-timestamp">${COLUMN_LASTSEND_DATE}</label>
                                            <mvc:text path="${propName}" id="recipient-timestamp" cssClass="form-control" readonly="true"/>
                                        </div>
                                    </c:when>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <div class="${fieldBlockClass}">
                                    <label class="form-label text-truncate">${definition.shortname}</label>

                                    <%-- display field with fixed values list--%>
                                    <c:if test="${definition.writable and hasFixedValues}">
                                        <mvc:select path="${propName}" cssClass="form-control" readonly="${not definition.writable}">
                                            <c:if test="${definition.nullable}">
                                                <mvc:option value="">NULL</mvc:option>
                                            </c:if>
                                            <c:if test="${not empty definition.defaultValue}">
                                                <mvc:option value="${definition.defaultValue}">${definition.defaultValue}</mvc:option>
                                            </c:if>
                                            <c:forEach items="${definition.fixedValues}" var="fixedValue">
                                                <mvc:option value="${fixedValue}">${fixedValue}</mvc:option>
                                            </c:forEach>
                                        </mvc:select>
                                    </c:if>

                                    <c:if test="${not hasFixedValues}">
                                        <c:choose>
                                            <c:when test="${definition.dataType eq GENERIC_TYPE_DATETIME and definition.writable}">
                                                <mvc:hidden path="${propName}"/>
                                                <div data-field="datetime" data-property="${propName}"
                                                     data-field-options="value: '${form.additionalColumns[definition.columnName]}', defaultValue: '${definition.defaultValue}'">
                                                </div>
                                            </c:when>

                                            <c:when test="${definition.dataType eq GENERIC_TYPE_DATE and definition.writable}">
                                                <div class="date-picker-container">
                                                    <mvc:text id="recipient-${propName}" path="${propName}" cssClass="form-control js-datepicker" readonly="${not definition.writable}"/>
                                                </div>
                                            </c:when>
                                            <c:when test="${definition.dataType eq GENERIC_TYPE_VARCHAR}">
                                                <mvc:text path="${propName}" cssClass="form-control" maxlength="${definition.maxSize}" readonly="${not definition.writable}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <mvc:text path="${propName}" cssClass="form-control" readonly="${not definition.writable}"/>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:if>
                                </div>
                            </c:otherwise>
                        </c:choose>

                    </c:if>
                </c:forEach>
            </div>
        </div>
    </div>

    <div id="mailinglists-tile" class="tile" data-editable-tile>
        <div class="tile-header">
            <h1 class="tile-title"><mvc:message code="Mailinglists" /></h1>
        </div>

        <div class="tile-body js-scrollable">
            <div class="row g-3">
                <c:set var="editable" value="false"/>
                <emm:ShowByPermission token="recipient.change">
                    <c:set var="editable" value="true"/>
                </emm:ShowByPermission>

                <%--@elvariable id="bindingsListForm" type="com.agnitas.emm.core.recipient.forms.RecipientBindingListForm"--%>
                <c:set var="bindingsListForm" value="${form.bindingsListForm}"/>

                <c:forEach items="${mailinglists}" var="mlist">
                    <%--@elvariable id="mailinglistBindings" type="com.agnitas.emm.core.recipient.forms.RecipientBindingForm"--%>
                    <c:set var="mailinglistBindings" value="${bindingsListForm.getListBinding(mlist.id)}"/>
                    <c:set var="mailinglistTileState" value="close"/>

                    <c:forEach items="${POSSIBLE_MEDIATYPES}" var="mediaType">
                        <emm:ShowByPermission token="${mediaType.requiredPermission.tokenString}">
                            <%--@elvariable id="binding" type="com.agnitas.emm.core.recipient.dto.RecipientBindingDto"--%>
                            <c:set var="binding" value="${mailinglistBindings.getBinding(mediaType)}"/>

                            <c:if test="${not empty binding.status and binding.status eq USER_STATUS_ACTIVE}">
                                <c:set var="mailinglistTileState" value="open"/>
                            </c:if>
                        </emm:ShowByPermission>
                    </c:forEach>

                    <div class="col-12">
                        <div id="tile-recipient-mailinglist-${mlist.id}" class="tile tile--md" data-toggle-tile data-toggle-tile-default-state="${mailinglistTileState}" data-mailinglist-tile>
                            <div class="tile-header">
                                <h3 class="tile-title text-color-default" role="button">
                                    <i class="icon icon-caret-up"></i>
                                    ${mlist.shortname}
                                </h3>

                                <div class="d-flex gap-3">
                                    <c:forEach items="${POSSIBLE_MEDIATYPES}" var="mediaType">
                                        <emm:ShowByPermission token="${mediaType.requiredPermission.tokenString}">
                                            <c:set var="binding" value="${mailinglistBindings.getBinding(mediaType)}"/>
                                            <c:set var="isBindingActive" value="${not empty binding.status and binding.status eq USER_STATUS_ACTIVE}" />

                                            <i class="icon icon-mediatype-${mediaType.mediaCode} fs-1 ${isBindingActive ? 'text-primary' : ''}"></i>
                                        </emm:ShowByPermission>
                                    </c:forEach>
                                </div>
                            </div>

                            <div class="tile-body">
                                <div class="row g-3">
                                    <c:forEach items="${POSSIBLE_MEDIATYPES}" var="mediaType">
                                        <emm:ShowByPermission token="${mediaType.requiredPermission.tokenString}">
                                            <c:set var="binding" value="${mailinglistBindings.getBinding(mediaType)}"/>
                                            <c:set var="isBindingActive" value="${not empty binding.status and binding.status eq USER_STATUS_ACTIVE}" />

                                            <c:set var="propertyName" value="bindingsListForm.mailinglistBindings[${mlist.id}].mediatypeBindings[${mediaType}]"/>
                                            <mvc:hidden path="${propertyName}.mediaType" value="${mediaType}"/>
                                            <mvc:hidden path="${propertyName}.mailinglistId" value="${mlist.id}"/>
                                            <mvc:hidden path="${propertyName}.referrer"/>
                                            <mvc:hidden path="${propertyName}.exitMailingId"/>

                                            <div class="col-12">
                                                <div class="tile tile--sm">
                                                    <div class="tile-header">
                                                        <p class="tile-title">
                                                            <i class="icon icon-mediatype-${mediaType.mediaCode} fs-1 ${isBindingActive ? 'text-primary' : ''}"></i>
                                                            <mvc:message code="mailing.MediaType.${mediaType.mediaCode}"/>
                                                        </p>

                                                        <div class="form-check form-switch">
                                                            <mvc:checkbox path="${propertyName}.status" value="${USER_STATUS_ACTIVE}" data-action="activate-mediatype" data-mediatype="${mediaType.mediaCode}"
                                                                          disabled="${not editable}" cssClass="form-check-input" role="switch"/>
                                                        </div>
                                                    </div>
                                                    <div class="tile-body">
                                                        <div class="row g-2">
                                                            <div class="col-12">
                                                                <label class="form-label"><mvc:message code="recipient.RecipientType" /></label>
                                                                <mvc:select path="${propertyName}.userType" cssClass="form-control" disabled="${not editable or not isBindingActive}" multiple="false" data-binding-usertype="">
                                                                    <mvc:option value="${USER_TYPE_ADMIN}"><mvc:message code="recipient.Administrator"/></mvc:option>
                                                                    <mvc:option value="${USER_TYPE_TEST}"><mvc:message code="TestSubscriber"/></mvc:option>
                                                                    <%@include file="fragments/recipient-test-vip-user-type.jspf" %>
                                                                    <mvc:option value="${USER_TYPE_NORMAL}"><mvc:message code="NormalSubscriber"/></mvc:option>
                                                                    <%@include file="fragments/recipient-normal-vip-user-type.jspf" %>
                                                                </mvc:select>
                                                            </div>

                                                            <div class="col-12">
                                                                <c:set var="statusRemark" value="${binding.userRemark}"/>
                                                                <c:if test="${not empty binding.referrer}">
                                                                    <c:set var="statusRemark" value="${binding.userRemark} Ref: ${emm:abbreviate(binding.referrer, 15)}"/>
                                                                </c:if>
                                                                <c:if test="${binding.status eq USER_STATUS_USER_OUT}">
                                                                    <c:set var="statusRemark" value="${statusRemark}<br>Opt-Out-Mailing: ${binding.exitMailingId}"/>
                                                                </c:if>

                                                                <label class="form-label"><mvc:message code="recipient.Remark" /></label>
                                                                <div class="row g-2">
                                                                    <div class="col-8">
                                                                        <input type="text" class="form-control" value="${statusRemark}" readonly>
                                                                    </div>
                                                                    <div class="col-4">
                                                                        <c:set var="bindingChangeDate" value="" />
                                                                        <c:if test="${not empty binding.changeDate}">
                                                                            <fmt:formatDate var="bindingChangeDate" value="${binding.changeDate}" pattern="${adminDateTimeFormatWithSeconds}"/>
                                                                        </c:if>
                                                                        <input type="text" class="form-control" value="${bindingChangeDate}" readonly>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </emm:ShowByPermission>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>
</mvc:form>

<script id="recipient-confirmation-modal" type="text/x-mustache-template">
    <div class="modal" tabindex="-1">
        <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title"><mvc:message code="warning"/></h1>
                    <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body">
                    <p>{{- question }}</p>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-danger js-confirm-negative flex-grow-1" data-bs-dismiss="modal">
                        <i class="icon icon-times"></i>
                        <span class="text">
                            <mvc:message code="button.Cancel"/>
                        </span>
                    </button>

                    <button type="button" class="btn btn-primary js-confirm-positive flex-grow-1" data-bs-dismiss="modal">
                        <i class="icon icon-check"></i>
                        <span class="text">
                            <mvc:message code="button.Proceed"/>
                        </span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</script>
