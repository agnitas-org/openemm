<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
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
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormat" type="java.lang.String"--%>
<%--@elvariable id="adminDateTimeFormatWithSeconds" type="java.lang.String"--%>
<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientForm"--%>
<%--@elvariable id="allowedEmptyEmail" type="java.lang.Boolean"--%>

<c:set var="GENERIC_TYPE_DATE" value="<%= DbColumnType.SimpleDataType.Date %>"/>
<c:set var="GENERIC_TYPE_DATETIME" value="<%= DbColumnType.SimpleDataType.DateTime %>"/>
<c:set var="GENERIC_TYPE_VARCHAR" value="<%= DbColumnType.SimpleDataType.Characters %>"/>
<c:set var="COLUMN_CUSTOMER_ID" value="<%= RecipientUtils.COLUMN_CUSTOMER_ID %>"/>
<c:set var="COLUMN_DATASOURCE_ID" value="<%= RecipientUtils.COLUMN_DATASOURCE_ID %>"/>
<c:set var="COLUMN_LATEST_DATASOURCE_ID" value="<%= RecipientUtils.COLUMN_LATEST_DATASOURCE_ID %>"/>
<c:set var="COLUMN_TIMESTAMP" value="<%= RecipientUtils.COLUMN_TIMESTAMP %>"/>

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


<fmt:setLocale value="${sessionScope['emm.admin'].locale}"/>
<mvc:form servletRelativeAction="/recipient/save.action" id="recipientForm" modelAttribute="form" data-form="resource" data-controller="recipient-view" data-action="recipient-save">
    <mvc:hidden path="id"/>

    <c:if test="${isRecipientEmailInUseWarningEnabled}">
        <script data-initializer="recipient-view" type="application/json">
            {
              "urls": {
                "CHECK_ADDRESS": "<c:url value='/recipient/${form.id}/checkAddress.action'/>",
                "CHECK_MATCH_ALTG": "<c:url value='/recipient/checkAltgMatch.action'/>",
                "SAVE_AND_BACK_TO_LIST": "<c:url value='/recipient/saveAndBackToList.action'/>",
                "EXISTING_USER_URL_PATTERN": "<c:url value='/recipient/{recipientID}/view.action'/>"
              }
            }
        </script>
    </c:if>

    <div class="row">
        <div class="col-md-6">
            <%-- Edit main recipient fields --%>
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline">
                        <c:choose>
                            <c:when test="${form.id gt 0}">
                                <mvc:message code="recipient.RecipientEdit"/>
                            </c:when>

                            <c:otherwise>
                                <mvc:message code="recipient.NewRecipient"/>
                            </c:otherwise>
                        </c:choose>
                    </h2>
                </div>
                <div class="tile-content">
                    <div class="tile-content-forms">
                        <div class="form-group">
                            <div class="col-sm-4">
                                <label for="recipient-salutation" class="control-label">
                                    <mvc:message code="recipient.Salutation"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <mvc:select path="gender" id="recipient-salutation" cssClass="js-select form-control">
                                    <mvc:option value="${GENDER_MALE}"><mvc:message code="recipient.gender.0.short"/></mvc:option>
                                    <mvc:option value="${GENDER_FEMALE}"><mvc:message code="recipient.gender.1.short"/></mvc:option>
                                    <emm:ShowByPermission token="recipient.gender.extended">
                                        <mvc:option value="${GENDER_PRAXIS}"><mvc:message code="recipient.gender.4.short"/></mvc:option>
                                        <mvc:option value="${GENDER_COMPANY}"><mvc:message code="recipient.gender.5.short"/></mvc:option>
                                    </emm:ShowByPermission>
                                    <mvc:option value="${GENDER_UNKNOWN}"><mvc:message code="recipient.gender.2.short"/></mvc:option>
                                </mvc:select>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-title" class="control-label">
                                  <mvc:message code="Title"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <mvc:text path="title" id="recipient-title" cssClass="form-control"
                                          data-field-validator="length" data-validator-options="required: false, max: 100"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-firstname" class="control-label">
                                  <mvc:message code="Firstname"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <mvc:text path="firstname" id="recipient-firstname" cssClass="form-control"
                                          data-field-validator="length" data-validator-options="required: false, max: 100"/>
                            </div>
                        </div>

                        <div class="form-group">
                            <div class="col-sm-4">
                              <label for="recipient-lastname" class="control-label">
                                  <mvc:message code="Lastname"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <mvc:text path="lastname" id="recipient-lastname" cssClass="form-control"
                                          data-field-validator="length" data-validator-options="required: false, max: 100"/>
                            </div>
                        </div>

                        <div class="form-group" ${not allowedEmptyEmail ? 'data-field="required"' : ''}>
                            <div class="col-sm-4">
                              <label for="recipient-email" class="control-label">
                                  <emm:ShowByPermission token="mailing.encrypted.send">
                                      <c:choose>
                                          <c:when test="${form.encryptedSend}">
                                              <img data-tooltip="<mvc:message code="recipient.encrypted.possible"/>" class="icon lock-icon" src="<c:url value="/assets/core/images/lock_icon.svg"/>" alt="">
                                          </c:when>
                                          <c:otherwise>
                                              <img data-tooltip="<mvc:message code="recipient.encrypted.notpossible"/>" class="icon unlock-icon" src="<c:url value="/assets/core/images/unlock_icon.svg"/>" alt="">
                                          </c:otherwise>
                                      </c:choose>
                                  </emm:ShowByPermission>
                                  <mvc:message code="mailing.MediaType.0"/> *
                              </label>
                            </div>
                            <div class="col-sm-8">
                                <c:if test="${allowedEmptyEmail}">
                                    <mvc:text path="email" id="recipient-email" cssClass="form-control"/>
                                </c:if>
                                <c:if test="${not allowedEmptyEmail}">
                                    <mvc:text path="email" id="recipient-email" cssClass="form-control" data-field-required=""/>
                                </c:if>
                            </div>
                        </div>

                        <emm:ShowByPermission token="recipient.tracking.veto">
						<div class="form-group">
                            <div class="col-sm-4">
                              <label for="trackingVeto" class="control-label checkbox-control-label">
                                  <mvc:message code="recipient.trackingVeto"/>
                              </label>
                            </div>
                            <div class="col-sm-8">
  								<label class="toggle">
  									<%--@elvariable id="disableTrackingVeto" type="java.lang.Boolean"--%>
  									<mvc:checkbox path="trackingVeto" id="trackingVeto" disabled="${disableTrackingVeto}"/>
                          			<div class="toggle-control"></div>
  								</label>
            				</div>
                        </div>
                        </emm:ShowByPermission>

                        <div class="form-group">
                            <div class="col-sm-4">
                                <label class="control-label">
                                    <mvc:message code="Mailtype"/>
                                </label>
                            </div>
                            <div class="col-sm-8">
                                <ul class="list-group">
                                    <li class="list-group-item">
                                        <label class="radio-inline">
                                            <mvc:radiobutton path="mailtype" value="${MAILTYPE_TEXT}"/>
                                            <mvc:message code="${MAILTYPE_TEXT.messageKey}"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="radio-inline">
                                            <mvc:radiobutton path="mailtype" value="${MAILTYPE_HTML}"/>
                                            <mvc:message code="${MAILTYPE_HTML.messageKey}"/>
                                        </label>
                                    </li>
                                    <li class="list-group-item">
                                        <label class="radio-inline">
                                            <mvc:radiobutton path="mailtype" value="${MAILTYPE_HTML_OFFLINE}"/>
                                            <mvc:message code="${MAILTYPE_HTML_OFFLINE.messageKey}"/>
                                        </label>
                                    </li>
                                </ul>
                            </div>
                        </div>

                        <%@include file="recipient-freq-counter.jspf" %>

                    </div>
                </div>
            </div>

            <%-- Edit additional recipient fields --%>
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline"><mvc:message code="recipient.More_Profile_Data"/></h2>
                </div>
                <div class="tile-content">
                    <div class="tile-content-forms">
                        <%--@elvariable id="columnDefinitions" type="java.util.List<com.agnitas.emm.core.recipient.dto.RecipientColumnDefinition>"--%>
                        <c:forEach items="${columnDefinitions}" var="definition">
                            <c:set var="hasFixedValues" value="${not empty definition.fixedValues}"/>
                            <c:set var="propName" value="additionalColumns[${definition.columnName}]"/>
                            <c:set var="isMainColumn" value="${definition.mainColumn}"/>

                            <c:if test="${definition.readable}">
                                <c:if test="${isMainColumn}">

                                    <c:if test="${definition.shortname eq COLUMN_CUSTOMER_ID}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label multiline-auto">${COLUMN_CUSTOMER_ID}:</label>
                                            </div>
                                            <div class="col-sm-8">
                                                <mvc:text path="id" cssClass="form-control" readonly="true"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${definition.shortname eq COLUMN_DATASOURCE_ID}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label multiline-auto">${COLUMN_DATASOURCE_ID}:</label>
                                            </div>
                                            <div class="col-sm-8">
                                            	<mvc:text path="dataSourceId" cssClass="form-control" readonly="true"/>
                                            </div>
                                        </div>
                                    </c:if>

                                    <c:if test="${definition.shortname eq COLUMN_LATEST_DATASOURCE_ID}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label multiline-auto">${COLUMN_LATEST_DATASOURCE_ID}:</label>
                                            </div>
                                            <div class="col-sm-8">
                                            	<mvc:text path="latestDataSourceId" cssClass="form-control" readonly="true"/>
                                            </div>
                                        </div>
                                    </c:if>
                                    
                                    <c:if test="${definition.shortname eq COLUMN_TIMESTAMP}">
                                        <div class="form-group">
                                            <div class="col-sm-4">
                                                <label class="control-label multiline-auto">${COLUMN_TIMESTAMP}:</label>
                                            </div>
                                            <div class="col-sm-8">
                                                <mvc:text path="${propName}" cssClass="form-control" readonly="true"/>
                                            </div>
                                        </div>
                                    </c:if>

                                </c:if>

                                <c:if test="${not isMainColumn}">
                                    <div class="form-group">
                                        <div class="col-sm-4">
                                            <label class="control-label multiline-auto">${definition.shortname}:</label>
                                        </div>
                                        <%-- display field with fixed values list--%>
                                        <c:if test="${definition.writable and hasFixedValues}">
                                            <div class="col-sm-8">
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
                                            </div>
                                        </c:if>

                                        <c:if test="${not hasFixedValues}">
                                            <div class="col-sm-8">
                                                <c:choose>
                                                    <c:when test="${definition.dataType eq GENERIC_TYPE_DATETIME and definition.writable}">
                                                        <mvc:hidden path="${propName}"/>
                                                        <div class="js-datetime-field" data-field="datetime"
                                                             data-property="${propName}"
                                                             data-field-options="value: '${form.additionalColumns[definition.columnName]}',
                                                                                 dateFormat: '${fn:toLowerCase(adminDateFormat)}',
                                                                                 defaultValue: '${definition.defaultValue}'">
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${definition.dataType eq GENERIC_TYPE_DATE and definition.writable}">
                                                        <div class="input-group">
                                                            <div class="input-group-controls">
                                                                <mvc:text path="${propName}"
                                                                          cssClass="form-control datepicker-input js-datepicker"
                                                                          data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}', formatSubmit: '${fn:toLowerCase(adminDateFormat)}'"
                                                                          readonly="${not definition.writable}"/>
                                                                    </div>
                                                                    <div class="input-group-btn">
                                                                <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1">
                                                                    <i class="icon icon-calendar-o"></i>
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </c:when>
                                                    <c:when test="${definition.dataType eq GENERIC_TYPE_VARCHAR}">
                                                        <mvc:text path="${propName}" cssClass="form-control"
                                                              maxlength="${definition.maxSize}" readonly="${not definition.writable}"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <mvc:text path="${propName}" cssClass="form-control" readonly="${not definition.writable}"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>                                             
                                        </c:if>
                                    </div>
                                </c:if>

                                <c:if test="${definition.lineAfter}">
                                    <div class="tile-separator"></div>
                                </c:if>

                            </c:if>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <c:set var="editable" value="false"/>
            <emm:ShowByPermission token="recipient.change">
                    <c:set var="editable" value="true"/>
                </emm:ShowByPermission>
            <div class="tile">
                <div class="tile-header">
                    <h2 class="headline"><mvc:message code="recipient.Mailinglists"/></h2>
                </div>
                <div class="tile-content tile-content-forms">
                    <%--@elvariable id="bindingsListForm" type="com.agnitas.emm.core.recipient.forms.RecipientBindingListForm"--%>
                    <c:set var="bindingsListForm" value="${form.bindingsListForm}"/>

                    <c:forEach items="${mailinglists}" var="mlist">
                        <%--@elvariable id="mailinglistBindings" type="com.agnitas.emm.core.recipient.forms.RecipientBindingForm"--%>
                        <c:set var="mailinglistBindings" value="${bindingsListForm.getListBinding(mlist.id)}"/>
                        <c:set var="mailinglistTileState" value="close"/>

                        <c:forEach items="${MediaTypes.values()}" var="mediaType">
                            <emm:ShowByPermission token="${mediaType.requiredPermission.tokenString}">
                                <%--@elvariable id="binding" type="com.agnitas.emm.core.recipient.dto.RecipientBindingDto"--%>
                                <c:set var="binding" value="${mailinglistBindings.getBinding(mediaType)}"/>

                                <c:if test="${not empty binding.status and binding.status eq USER_STATUS_ACTIVE}">
                                    <c:set var="mailinglistTileState" value="open"/>
                                </c:if>
                            </emm:ShowByPermission>
                        </c:forEach>

                        <div class="tile">
                        	<div class="tile-header" style="padding-bottom: 15px; height: auto;">
                                <a href="#" class="headline js-show-tile-if-checked"
                                   data-toggle-tile="#tile-recipient-mailinglist-${mlist.id}"
                                   data-toggle-tile-default-state="${mailinglistTileState}">
                                    <i class="tile-toggle icon icon-angle-down" style="padding-right: 5px;"></i>
                                    ${mlist.shortname}
                                </a>
                            </div>
                            <div class="tile-content tile-content-forms hidden" id="tile-recipient-mailinglist-${mlist.id}">
                                <%--@elvariable id="mediaType" type="com.agnitas.emm.core.mediatypes.common.MediaTypes"--%>
                                <c:forEach items="${MediaTypes.values()}" var="mediaType">
                                    <emm:ShowByPermission token="${mediaType.requiredPermission.tokenString}">
                                        <%--@elvariable id="binding" type="com.agnitas.emm.core.recipient.dto.RecipientBindingDto"--%>
                                        <c:set var="binding" value="${mailinglistBindings.getBinding(mediaType)}"/>

                                        <c:set var="propertyName" value="bindingsListForm.mailinglistBindings[${mlist.id}].mediatypeBindings[${mediaType}]"/>
                                        <mvc:hidden path="${propertyName}.mediaType" value="${mediaType}"/>
                                        <mvc:hidden path="${propertyName}.mailinglistId" value="${mlist.id}"/>
                                        <mvc:hidden path="${propertyName}.referrer"/>
                                        <mvc:hidden path="${propertyName}.exitMailingId"/>

                                        <div class="form-group">
                                            <div class="col-sm-2"></div>
                                            <div class="col-sm-3 control-label-left">
                                                <label class="checkbox-inline control-label">
                                                    <mvc:checkbox path="${propertyName}.status" value="${USER_STATUS_ACTIVE}" disabled="${not editable}"/>
                                                    <mvc:message code="mailing.MediaType.${mediaType.mediaCode}"/>
                                                </label>
                                            </div>
                                            <div class="col-sm-7">
                                                <mvc:select path="${propertyName}.userType" cssClass="form-control" disabled="${not editable}" multiple="false">
                                                    <mvc:option value="${USER_TYPE_ADMIN}"><mvc:message code="recipient.Administrator"/></mvc:option>
                                                    <mvc:option value="${USER_TYPE_TEST}"><mvc:message code="TestSubscriber"/></mvc:option>
                                                    <%@include file="recipient-test-vip-user-type.jspf" %>
                                                    <mvc:option value="${USER_TYPE_NORMAL}"><mvc:message code="NormalSubscriber"/></mvc:option>
                                                    <%@include file="recipient-normal-vip-user-type.jspf" %>
                                                </mvc:select>
                                            </div>
                                            <c:if test="${not empty binding.status}">
                                                <div class="form-group">
                                                    <div class="col-sm-2"></div>
                                                    <div class="col-sm-3 control-label-left">
                                                        <label class="control-label">
                                                            <mvc:message code="recipient.Status"/>
                                                        </label>
                                                    </div>
                                                    <div class="col-sm-7">
                                                        <c:set var="openingCandidateClass" value="" />
                                                        <c:if test="${binding.status eq USER_STATUS_ACTIVE}">
                                                            <c:set var="openingCandidateClass" value="opening-candidate"/>
                                                        </c:if>

                                                        <p class="form-control-static ${openingCandidateClass}"><mvc:message code="recipient.MailingState${binding.status.statusCode}"/></p>
                                                    </div>
                                                </div>
                                            </c:if>


                                            <c:set var="statusRemark" value="${binding.userRemark}"/>
                                            <c:if test="${not empty binding.referrer}">
                                                <c:set var="statusRemark" value="${binding.userRemark} Ref: ${emm:abbreviate(binding.referrer, 15)}"/>
                                            </c:if>
                                            <c:if test="${binding.status eq USER_STATUS_USER_OUT}">
                                                <c:set var="statusRemark" value="${statusRemark}<br>Opt-Out-Mailing: ${binding.exitMailingId}"/>
                                            </c:if>

                                            <c:if test="${not empty statusRemark}">
                                                <div class="form-group">
                                                    <div class="col-sm-2"></div>
                                                    <div class="col-sm-3 control-label-left">
                                                        <label class="control-label">
                                                            <mvc:message code="recipient.Remark"/>
                                                        </label>
                                                    </div>
                                                    <div class="col-sm-7">
                                                        <p class="form-control-static">${statusRemark}</p>

                                                        <c:if test="${not empty binding.changeDate}">
                                                            <p class="form-control-static">
                                                                <fmt:formatDate value="${binding.changeDate}" pattern="${adminDateTimeFormatWithSeconds}"/>
                                                            </p>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </c:if>

                                        </div>
                                        <div class="tile-separator"></div>
                                    </emm:ShowByPermission>

                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </div>
    </div>

    <script id="email-confirmation-modal" type="text/x-mustache-template">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i></button>
                        <h4 class="modal-title">
                            <mvc:message code="warning"/>
                        </h4>
                    </div>

                    <div class="modal-body">
                        <p>{{- question }}</p>
                    </div>

                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text">
                                    <mvc:message code="button.Cancel"/>
                                </span>
                            </button>

                            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <span class="text">
                                    <mvc:message code="button.Proceed"/>
                                </span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>

    <script id="hide-recipient-confirmation-modal" type="text/x-mustache-template">
        <div class="modal">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                            <i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <h4 class="modal-title"><mvc:message code="Recipient"/></h4>
                    </div>
                    <div class="modal-body">
                        <mvc:message code="recipient.hide.question"/>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><mvc:message code="button.Cancel"/></span>
                            </button>
                            <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                                <i class="icon icon-check"></i>
                                <span class="text"><mvc:message code="button.Save"/></span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </script>
</mvc:form>
