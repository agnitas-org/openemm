<%@ page import="com.agnitas.beans.Mailing" %>
<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="isCopying" type="java.lang.Boolean"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="worldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="emailSettingsEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="SHOW_TARGET_MODE_TOGGLE" type="java.lang.Boolean"--%>
<%--@elvariable id="TARGET_MODE_TOGGLE_DISABLED" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="companyDomainAddresses" type="java.util.List<com.agnitas.emm.core.companydomain.beans.impl.DomainAddressEntryDto>"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="selectedRemovedMailinglist" type="com.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>

<c:set var="INTERVAL_MAILING_TYPE" value="<%= MailingType.INTERVAL %>"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>

<c:set var="isMailingGrid" value="${gridTemplateId > 0}"/>
<c:set var="isNewGridMailing" value="${mailingId le 0 and isMailingGrid}"/>
<c:set var="emailSettingsDisabled" value="${not MAILING_EDITABLE or not emailSettingsEditable}"/>
<c:set var="MAILING_TYPE_DATE_BASED" value="<%= MailingType.DATE_BASED %>"/>
<c:set var="TARGET_MODE_OR" value="<%= Mailing.TARGET_MODE_OR %>"/>
<c:set var="TARGET_MODE_AND" value="<%= Mailing.TARGET_MODE_AND %>"/>
<c:set var="mailinglistEditable" value="${mailinglistEditable or isNewGridMailing}"/>
<c:set var="isNewGridMailing" value="${mailingId le 0 and isMailingGrid}"/>

<div data-field="validator">
    <label class="form-label">
        <label for="settingsGeneralMailingList"><mvc:message code="Mailinglist"/> *</label>
        <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/MailingListMsg.xml"></a>
        <c:if test="${not mailinglistEditable}">
            <div class="icon icon-exclamation-triangle" data-tooltip="<mvc:message code="warning.mailinglist.disabled"/>"></div>
        </c:if>
    </label>

    <c:set var="isMailinglistDenied" value="${not mailinglistEditable or not MAILING_EDITABLE or not emailSettingsEditable or workflowDriven or isSettingsReadonly}"/>

    <mvc:select path="mailinglistId" id="settingsGeneralMailingList" size="1"
                cssClass="form-control js-select"
                data-action="save-mailing-list-id"
                disabled="${isMailinglistDenied}"
                data-field-validator="mailinglist-exist"
                data-workflow-driven="${workflowDriven}"
                data-validator-options='removedMailinglistId: ${not empty selectedRemovedMailinglist ? selectedRemovedMailinglist.id : ""}'>
        <c:if test="${not empty selectedRemovedMailinglist and not isCopying}">
            <mvc:option value="${selectedRemovedMailinglist.id}" id="${selectedRemovedMailinglist.id}-mailinglist">${selectedRemovedMailinglist.shortname}</mvc:option>
        </c:if>
        <mvc:option value="0" styleId="0-mailinglist">--</mvc:option>
        <c:forEach var="mailinglist" items="${mailinglists}">
            <mvc:option value="${mailinglist.id}" id="${mailinglist.id}-mailinglist">${mailinglist.shortname}</mvc:option>
        </c:forEach>
    </mvc:select>
</div>

<div id="regular-targets-box">
    <%@include file="fragments/mailing-settings-targets-select.jspf" %>
    <c:if test="${SHOW_TARGET_MODE_TOGGLE}">
        <div id="target-mode-box">
            <c:set var="targetModeDisabled" value="${TARGET_MODE_TOGGLE_DISABLED or isSettingsReadonly}"/>
            <div class="switch" data-action="change-target-mode">
                <mvc:radiobutton path="targetMode" value="${TARGET_MODE_AND}" id="target-mode-and-btn" disabled="${targetModeDisabled}"/>
                <label for="target-mode-and-btn">AND</label>
                <mvc:radiobutton path="targetMode" value="${TARGET_MODE_OR}" id="target-mode-or-btn" disabled="${targetModeDisabled}"/>
                <label for="target-mode-or-btn">OR</label>
            </div>
            <label class="form-label mb-0" id="target-mode-description" for="target-mode-box">
                    <%-- loads by js--%>
            </label>
        </div>
    </c:if>
</div>

<%@ include file="fragments/mailing-settings-altg-select.jspf" %>

<c:choose>
    <c:when test="${isTemplate}">
        <div>
            <c:set var="isNeedsTargetCheckboxDisabled" value="${not MAILING_EDITABLE or isSettingsReadonly or mailingSettingsForm.mailingType eq DATEBASED_MAILING_TYPE or mailingSettingsForm.mailingType eq INTERVAL_MAILING_TYPE}"/>
            <div class="form-check form-switch mt-auto">
                <mvc:checkbox path="needsTarget" id="needsTargetToggle" cssClass="form-check-input" role="switch" disabled="${isNeedsTargetCheckboxDisabled}"/>
                <label class="form-label form-check-label" for="needsTargetToggle"><mvc:message code="mailing.needsTarget"/></label>
            </div>
            <c:if test="${isNeedsTargetCheckboxDisabled}">
                <mvc:hidden path="needsTarget"/>
            </c:if>
        </div>
    </c:when>
    <c:otherwise>
        <mvc:hidden path="needsTarget"/>
    </c:otherwise>
</c:choose>

<div id="subject-field" data-field="validator">
    <label class="form-label" for="emailSubject"><mvc:message code="mailing.Subject"/> *</label>
    <mvc:text path="emailMediatype.subject" id="emailSubject"
              cssClass="form-control" readonly="${emailSettingsDisabled}"
              disabled="${isSettingsReadonly}"
              data-field-validator="length" data-show-char-counter=""
              data-validator-options="required: true, min: 2"/>
</div>

<div id="pre-header-field">
    <label class="form-label" for="pre-header"><mvc:message code="mailing.preheader"/></label>
    <mvc:text path="emailMediatype.preHeader" id="pre-header" data-show-char-counter="" cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"/>
</div>

<div id="sender-email-field">
    <label class="form-label" for="emailSenderMail"><mvc:message code="mailing.SenderEmail"/> *</label>
    <%@include file="fragments/domain-addresses-dropdown.jspf" %>
    <c:choose>
        <c:when test="${domainAddressesDropdown eq null}">
            <mvc:text path="emailMediatype.fromEmail" id="emailSenderMail"
                      cssClass="form-control" readonly="${emailSettingsDisabled}"
                      disabled="${isSettingsReadonly}"
                      data-field="required" data-field-options="ignoreHidden: true"/>
        </c:when>
        <c:otherwise>
            ${domainAddressesDropdown}
        </c:otherwise>
    </c:choose>
</div>

<div id="sender-name-field">
    <label class="form-label" for="emailSenderName"><mvc:message code="mailing.SenderFullname"/></label>
    <mvc:text path="emailMediatype.fromFullname" id="emailSenderName" cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"/>
</div>

<div data-field="validator" id="reply-to-email-field">
    <label class="form-label" for="emailReplyEmail"><mvc:message code="mailing.ReplyEmail"/> *</label>
    <mvc:text path="emailMediatype.replyEmail" id="emailReplyEmail"
              cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"
              data-field-validator="length" data-validator-options="required: true"/>
</div>

<div id="reply-to-name-field">
    <label class="form-label" for="emailReplyName"><mvc:message code="mailing.ReplyFullName"/></label>
    <mvc:text path="emailMediatype.replyFullname" id="emailReplyName" cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"/>
</div>

<c:if test="${not isNewGridMailing || isCopying}">
    <emm:ShowByPermission token="settings.extended">
        <emm:ShowByPermission token="mailing.envelope_address">
            <div id="envelope-email-field">
                <label class="form-label" for="emailEnvelopeEmail">
                    <mvc:message code="EnvelopeEmail"/>
                    <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/EnvelopeAddress.xml"></a>
                </label>
                <mvc:text path="emailMediatype.envelopeEmail" id="emailEnvelopeEmail" cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"/>
            </div>
        </emm:ShowByPermission>
        <emm:HideByPermission token="mailing.envelope_address">
            <mvc:hidden path="emailMediatype.envelopeEmail"/>
        </emm:HideByPermission>
    </emm:ShowByPermission>

    <emm:ShowByPermission token="settings.extended">
        <div id="mailing-bcc-recipients" class="${mailingSettingsForm.mailingType eq MAILING_TYPE_DATE_BASED ? '' : 'hidden'}">
           <label class="form-label" for="bccRecipientEmails"><mvc:message code="action.address.bcc"/></label>
           <mvc:text path="emailMediatype.bccRecipients" id="bccRecipientEmails" cssClass="form-control" disabled="${isSettingsReadonly}"/>
        </div>
    </emm:ShowByPermission>
    <emm:HideByPermission token="settings.extended">
        <mvc:hidden path="emailMediatype.envelopeEmail"/>
        <mvc:hidden path="emailMediatype.bccRecipients"/>
    </emm:HideByPermission>

    <%@include file="fragments/mailing-settings-sms-sender-address.jspf" %>
</c:if>

<c:if test="${not isTemplate}">
    <div>
        <label class="form-label" for="mailingPlanDate"><mvc:message code="mailing.plan.date"/></label>
        <c:set var="isReadonlyDate" value="${worldMailingSend || workflowDriven || isSettingsReadonly}"/>
        <div id="mailingPlanDate" data-field="datetime" data-property="planDate"
             data-field-options="value: '${mailingSettingsForm.planDate}', defaultSubmitTime:''"
             data-field-extra-attributes="${workflowDriven or isReadonlyDate ? 'disabled' : ''}">
        </div>
    </div>
</c:if>

<c:if test="${mailingId > 0}">
    <div id="calculate-recipients-box">
        <label class="form-label" for="number-of-recipients"><mvc:message code="report.numberRecipients"/></label>
        <div class="d-flex gap-1">
            <input type="text" class="form-control" id="number-of-recipients" readonly value='<mvc:message code="GWUA.clickOnCalculate"/>'>
            <button type="button" class="btn btn-primary px-2" data-action="calculateRecipients">
                <i class="icon icon-play-circle"></i><mvc:message code="button.Calculate"/>
            </button>
        </div>
    </div>
</c:if>
