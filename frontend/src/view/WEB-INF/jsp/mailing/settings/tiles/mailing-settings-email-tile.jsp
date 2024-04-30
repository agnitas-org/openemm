<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isCopying" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="companyDomainAddresses" type="java.util.List<com.agnitas.emm.core.companydomain.beans.impl.DomainAddressEntryDto>"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="emailSettingsEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="isMailingGrid" value="${gridTemplateId > 0}"/>
<c:set var="emailSettingsDisabled" value="${not MAILING_EDITABLE or not emailSettingsEditable}"/>
<c:set var="MAILING_TYPE_DATE_BASED" value="<%= MailingType.DATE_BASED %>"/>

<div id="email-tile" class="tile" data-action="scroll-to">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mediaEmail">
            <i class="tile-toggle icon icon-angle-up"></i>
            <mvc:message code="mailing.MediaType.0"/>
        </a>
        <c:if test="${mailingId != 0 || !isMailingGrid || isCopying}">
            <ul class="tile-header-nav">
                <li class="active"><a href="#" data-toggle-tab="#tab-mailingMediaEmailBase"><mvc:message code="default.basic"/></a></li>
                <li><a href="#" data-toggle-tab="#tab-mailingMediaEmailAdvanced" data-extends-tab="#tab-mailingMediaEmailBase"><mvc:message code="default.advanced"/></a></li>
            </ul>
        </c:if>
    </div>
    <div id="tile-mediaEmail" class="tile-content tile-content-forms">
        <div id="tab-mailingMediaEmailBase">
            <div class="form-group" data-field="validator">
                <div class="col-sm-4">
                    <label class="control-label" for="emailSubject">
                        <mvc:message code="mailing.Subject"/>*
                    </label>
                </div>
                <div class="col-sm-8 text-char-counter">
                    <mvc:text path="emailMediatype.subject" id="emailSubject"
                              cssClass="form-control" readonly="${emailSettingsDisabled}"
                              data-field-validator="length"
                              data-action="count-text-chars"
                              data-validator-options="required: true, min: 2"/>
                    <div data-char-counter-for="emailSubject">
                        <span class="small status">&nbsp;</span>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="emailMailFormat">
                        <mvc:message code="action.Format"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="emailMediatype.mailFormat"
                                id="emailMailFormat" cssClass="form-control" disabled="${emailSettingsDisabled}">
                        <mvc:option value="0"><mvc:message code="only_Text"/></mvc:option>
                        <mvc:option value="1"><mvc:message code="Text_HTML"/></mvc:option>
                        <mvc:option value="2"><mvc:message code="Text_HTML_OfflineHTML"/></mvc:option>
                    </mvc:select>
                </div>
            </div>

            <div class="form-group" data-field="required">
                <div class="col-sm-4">
                    <label class="control-label" for="emailSenderMail">
                        <mvc:message code="mailing.SenderEmail"/>*
                    </label>
                </div>
                <%@include file="../fragments/domain-addresses-dropdown.jspf" %>

                <c:choose>
                    <c:when test="${domainAddressesDropdown eq null}">
                        <div class="col-sm-8">
                            <mvc:text path="emailMediatype.fromEmail" id="emailSenderMail"
                                      cssClass="form-control" readonly="${emailSettingsDisabled}"
                                      data-field-required=""/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        ${domainAddressesDropdown}
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="emailSenderName">
                        <mvc:message code="mailing.SenderFullname"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="emailMediatype.fromFullname" id="emailSenderName"
                              cssClass="form-control" readonly="${emailSettingsDisabled}" />
                </div>
            </div>

            <div class="form-group" data-field="validator">
                <div class="col-sm-4">
                    <label class="control-label" for="emailReplyEmail">
                        <mvc:message code="mailing.ReplyEmail"/>*
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="emailMediatype.replyEmail" id="emailReplyEmail"
                              cssClass="form-control" readonly="${emailSettingsDisabled}"
                              data-field-validator="length" data-validator-options="required: true"/>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="emailReplyName">
                        <mvc:message code="mailing.ReplyFullName"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:text path="emailMediatype.replyFullname" id="emailReplyName"
                              cssClass="form-control" readonly="${emailSettingsDisabled}" />
                </div>
            </div>
        </div>

        <div id="tab-mailingMediaEmailAdvanced" class="hidden">

            <emm:ShowByPermission token="mailing.envelope_address">
                <div class="form-group">
                    <div class="col-sm-4">
                        <label class="control-label" for="emailEnvelopeEmail">
                            <mvc:message code="EnvelopeEmail"/>
	                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/EnvelopeAddress.xml" tabindex="-1" type="button"></button>
	                    </label>
                    </div>
                    <div class="col-sm-8">
                        <mvc:text path="emailMediatype.envelopeEmail" id="emailEnvelopeEmail"
                                   cssClass="form-control" readonly="${emailSettingsDisabled}" />
                    </div>
                </div>
            </emm:ShowByPermission>
            <emm:HideByPermission token="mailing.envelope_address">
                <mvc:hidden path="emailMediatype.envelopeEmail"/>
            </emm:HideByPermission>
            

            <div id="mailing-bcc-recipients"
                 class="form-group ${mailingSettingsForm.mailingType eq MAILING_TYPE_DATE_BASED ? '' : 'hidden'}">
               <div class="col-sm-4">
                   <label class="control-label" for="bccRecipientEmails">
                       <mvc:message code="action.address.bcc"/>
                   </label>
               </div>
               <div class="col-sm-8">
                   <mvc:text path="emailMediatype.bccRecipients" id="bccRecipientEmails" cssClass="form-control" />
               </div>
            </div>

            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="emailCharset">
                        <mvc:message code="mailing.Charset"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <mvc:select path="emailMediatype.charset" id="emailCharset" cssClass="form-control" disabled="${emailSettingsDisabled}">
                        <%@include file="../fragments/mailing-settings-email-charsets.jspf" %>
                    </mvc:select>                    
                </div>
            </div>
            <mvc:hidden path="emailMediatype.linefeed"/>
            <%@include file="../fragments/mailing-settings-email-onepixel.jspf" %>
            
        </div>
    </div>
</div>
