<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="worldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowDriven" type="java.lang.Boolean"--%>
<%--@elvariable id="isCopying" type="java.lang.Boolean"--%>
<%--@elvariable id="isSettingsReadonly" type="java.lang.Boolean"--%>
<%--@elvariable id="companyDomainAddresses" type="java.util.List<com.agnitas.emm.core.companydomain.beans.impl.DomainAddressEntryDto>"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="emailSettingsEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="isMailingGrid" value="${gridTemplateId > 0}"/>
<c:set var="isNewGridMailing" value="${mailingId le 0 and isMailingGrid}"/>
<c:set var="emailSettingsDisabled" value="${not MAILING_EDITABLE or not emailSettingsEditable}"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>

<c:set var="planDateInput">
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
</c:set>

<c:if test="${not param.isTabsView}">
    ${planDateInput}
</c:if>

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

<div ${param.isTabsView ? '' : 'class="border rounded p-2"'} id="sender-fields">
    <div>
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
    <div>
        <label class="form-label mt-3" for="emailSenderName"><mvc:message code="mailing.SenderFullname"/></label>
        <mvc:text path="emailMediatype.fromFullname" id="emailSenderName" cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"/>
    </div>
</div>

<div ${param.isTabsView ? '' : 'class="border rounded p-2"'} id="reply-to-fields">
    <div data-field="validator">
        <label class="form-label" for="emailReplyEmail"><mvc:message code="mailing.ReplyEmail"/> *</label>
        <mvc:text path="emailMediatype.replyEmail" id="emailReplyEmail"
                  cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"
                  data-field-validator="length" data-validator-options="required: true"/>
    </div>
    <div>
        <label class="form-label mt-3" for="emailReplyName"><mvc:message code="mailing.ReplyFullName"/></label>
        <mvc:text path="emailMediatype.replyFullname" id="emailReplyName" cssClass="form-control" readonly="${emailSettingsDisabled}" disabled="${isSettingsReadonly}"/>
    </div>
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
        <div id="mailing-bcc-recipients" class="${mailingSettingsForm.mailingType eq DATEBASED_MAILING_TYPE ? '' : 'hidden'}">
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

<c:if test="${param.isTabsView}">
    ${planDateInput}
</c:if>
