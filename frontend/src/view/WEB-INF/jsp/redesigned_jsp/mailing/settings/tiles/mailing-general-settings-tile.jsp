<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ page import="com.agnitas.beans.MediatypeEmail" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="worldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="archives" type="java.util.List<com.agnitas.beans.Campaign>"--%>
<%--@elvariable id="emailSettingsEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isCopying" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="isPostMediatypeActive" type="java.lang.Boolean"--%>
<%--@elvariable id="gridTemplateName" type="java.lang.String"--%>
<%--@elvariable id="templateShortname" type="java.lang.String"--%>
<%--@elvariable id="prioritizedMediatypes" type="java.util.List<com.agnitas.emm.core.mediatypes.common.MediaTypes>"--%>

<c:set var="NORMAL_MAILING_TYPE" value="<%= MailingType.NORMAL %>"/>
<c:set var="ACTIONBASED_MAILING_TYPE" value="<%= MailingType.ACTION_BASED %>"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" scope="request"/>
<c:set var="workflowDriven" value="${workflowParams.workflowId gt 0}"/>
<c:set var="isMailingGrid" value="${gridTemplateId > 0}"/>
<c:set var="isNewGridMailing" value="${mailingId le 0 and isMailingGrid}"/>

<div class="tile" data-editable-tile>
    <div class="tile-header">
        <h1 class="tile-title text-truncate"><mvc:message code="mailing.generalSettings"/></h1>
    </div>
    <div class="tile-body grid gap-3 js-scrollable" style="--bs-columns: 1">
        <div data-field="validator">
            <c:set var="nameMsg"><mvc:message code="default.Name"/></c:set>
            <label class="form-label" for="mailingShortname">${nameMsg} *</label>
            <mvc:text id="mailingShortname" cssClass="form-control" path="shortname" maxlength="99"
                      data-field-validator="length"
                      data-validator-options="required: true, min: 3, max: 99" placeholder="${nameMsg}" disabled="${isSettingsReadonly}"/>
        </div>
        <div>
            <mvc:message var="descriptionMsg" code="Description"/>
            <label class="form-label" for="mailingDescription">${descriptionMsg}</label>
            <mvc:textarea id="mailingDescription" cssClass="form-control" path="description" rows="1" placeholder="${descriptionMsg}" disabled="${isSettingsReadonly}"/>
        </div>
        <emm:ShowByPermission token="settings.extended">
            <div class="form-check form-switch mt-auto">
                <mvc:checkbox cssClass="form-check-input" path="mailingContentTypeAdvertising" id="mailingContentTypeAdvertising" role="switch" disabled="${isSettingsReadonly}"/>
                <label class="form-label form-check-label">
                    <mvc:message code="mailing.contentType.advertising"/>
                    <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/AdvertisingMsg.xml"></a>
                </label>
            </div>
        </emm:ShowByPermission>
        <emm:HideByPermission token="settings.extended">
            <mvc:hidden path="mailingContentTypeAdvertising"/>
        </emm:HideByPermission>

        <emm:ShowByPermission token="mailing.show.types">
            <c:set var="followupNotice" value=""/>
            <c:set var="mailingTypeContainerClass" value=""/>
            <%@include file="fragments/mailing-settings-followup-notice.jspf" %>

            <div class="${mailingTypeContainerClass}">
                <label class="form-label">
                    <label for="settingsGeneralMailType"><mvc:message code="mailing.Mailing_Type"/></label>
                    <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/MailingTypeMsg.xml"></a>
                </label>
                <mvc:select path="mailingType" id="settingsGeneralMailType" cssClass="form-control"
                            data-action="change-general-mailing-type"
                            data-workflow-driven="${workflowDriven}"
                            disabled="${not MAILING_EDITABLE or workflowDriven or not emailSettingsEditable or isSettingsReadonly}">
                    <mvc:option value="${NORMAL_MAILING_TYPE}">
                        <mvc:message code="Normal_Mailing"/>
                    </mvc:option>
                    <mvc:option value="${ACTIONBASED_MAILING_TYPE}">
                        <mvc:message code="mailing.action.based.mailing"/>
                    </mvc:option>
                    <mvc:option value="${DATEBASED_MAILING_TYPE}">
                        <mvc:message code="mailing.Rulebased_Mailing"/>
                    </mvc:option>
                    <%@include file="fragments/mailingtype-extended-options.jspf" %>
                </mvc:select>
                ${followupNotice}
            </div>
            <%@include file="fragments/mailing-settings-followup-additional-inputs.jspf" %>
        </emm:ShowByPermission>
        <emm:HideByPermission token="mailing.show.types">
            <mvc:hidden path="mailingType"/>
        </emm:HideByPermission>

        <c:if test="${gridTemplateId <= 0}">
            <div class="${isPostMediatypeActive ? 'hidden' : ''}">
                <label class="form-label">
                    <mvc:message code="mediatype.mediatypes"/>
                    <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/MediaTypesMsg.xml"></a>
                </label>
                <ul id="mediatypes-list" class="list-group">
                    <c:forEach var="mediaType" items="${prioritizedMediatypes}">
                        <c:set var="mediaTypeName" value="${fn:toLowerCase(mediaType.name())}"/>
                        <c:set var="mediaTypeCode" value="${mediaType.mediaCode}"/>
                        <c:set var="mtFormName" value="${mediaTypeName}Mediatype"/>
                        <c:set var="mt" value="${mailingSettingsForm[mtFormName]}" />
                        <c:set var="mediatypeItem">
                            <li class="list-group-item" data-priority="${mt.priority}" data-mediatype="${mediaTypeName}" data-action="change-mediatype">
                                <div class="form-check form-switch" data-field="toggle-vis" data-field-vis-scope="#mailingSettingsForm">
                                    <c:choose>
                                        <c:when test="${mediaTypeName eq 'email'}">
                                            <c:set var="filedsToToggle" value="#subject-field, #pre-header-field, #format-fields, #sender-fields, #reply-to-fields, #envelope-email-field, #mailing-bcc-recipients, #onepixel-field"/>
                                        </c:when>
                                        <c:when test="${mediaTypeName eq 'sms'}">
                                            <c:set var="filedsToToggle" value="#sms-address-field, #smsTemplate"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="filedsToToggle" value=""/>
                                        </c:otherwise>
                                    </c:choose>
                                    <mvc:checkbox id="${mediaTypeName}-mediatype-switch" cssClass="form-check-input" path="${mediaTypeName}Mediatype.active" role="switch" value="true" disabled="${not MAILING_EDITABLE or mt.readonly or isSettingsReadonly}"
                                                  data-field-vis="" data-field-vis-nondisabled="" data-field-vis-show="${filedsToToggle}" />
                                    <div class="hidden" data-field-vis-default="" data-field-vis-hide="${filedsToToggle}" ></div>
                                    <label class="form-label form-check-label fw-normal"><mvc:message code="mailing.MediaType.${mediaTypeName}"/></label>
                                </div>
                                <c:if test="${mt.readonly}">
                                    <mvc:hidden path="${mediaTypeName}Mediatype.active" disabled="${not MAILING_EDITABLE}" />
                                </c:if>
                                <c:if test="${not empty mt and mt.active and not isSettingsReadonly}">
                                    <div class="list-group-item-controls">
                                        <a href="#" class="icon icon-angle-down fs-2" data-config="mediatypeCode: ${mediaTypeCode}" data-action="prioritise-mediatype-down"></a>
                                        <a href="#" class="icon icon-angle-up fs-2" data-config="mediatypeCode: ${mediaTypeCode}" data-action="prioritise-mediatype-up"></a>
                                    </div>
                                </c:if>
                            </li>
                        </c:set>

                        <emm:ShowByPermission token="${mediaType.requiredPermission}">
                            ${mediatypeItem}
                        </emm:ShowByPermission>

                        <c:if test="${mediaTypeName ne 'email'}">
                            <%@include file="fragments/mailing-not-email-mediatypes.jspf"%>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </c:if>
        
        <c:if test="${not isNewGridMailing || isCopying}">
            <emm:ShowByPermission token="template.show">
                <c:set var="isTemplateNameShown" value="${not isMailingGrid and (mailingId ne 0 or (mailingId eq 0 and isCopying))}"/>
                <c:if test="${isMailingGrid || isTemplateNameShown}">
                    <div class="row">
                        <c:if test="${isMailingGrid and (not empty mailingId and mailingId ne 0 or isCopying)}">
                            <%@include file="fragments/mailing-settings-grid-owner.jspf" %>
                        </c:if>
                        
                        <c:if test="${empty templateShortname}">
                            <c:set var="templateShortname"><mvc:message code="mailing.No_Template"/></c:set>
                        </c:if>
                        <c:if test="${isMailingGrid}">
                            <div class="col">
                                <label for="creator-name" class="form-label"><mvc:message code="Template"/></label>
                                <input id="creator-name" type="text" class="form-control" value="${gridTemplateName}" readonly>
                            </div>
                        </c:if>
                        <c:if test="${isTemplateNameShown}">
                            <div class="col">
                                <label for="template-name" class="form-label"><mvc:message code="Template"/></label>
                                <input id="template-name" type="text" class="form-control" value="${templateShortname}" readonly>
                            </div>
                        </c:if>
                    </div>
                </c:if>
            </emm:ShowByPermission>
    
            <div class="row g-3" id="format-fields">
                <div class="col-6">
                    <label class="form-label" for="emailMailFormat"><mvc:message code="action.Format"/></label>
                    <mvc:select path="emailMediatype.mailFormat" id="emailMailFormat" cssClass="form-control" disabled="${emailSettingsDisabled or isSettingsReadonly}">
                        <mvc:option value="0"><mvc:message code="only_Text"/></mvc:option>
                        <mvc:option value="1"><mvc:message code="Text_HTML"/></mvc:option>
                        <mvc:option value="2"><mvc:message code="Text_HTML_OfflineHTML"/></mvc:option>
                    </mvc:select>
                </div>
                <emm:ShowByPermission token="settings.extended">
                    <div class="col-6">
                        <label class="form-label" for="emailCharset"><mvc:message code="mailing.Charset"/></label>
                        <mvc:select path="emailMediatype.charset" id="emailCharset" cssClass="form-control" disabled="${emailSettingsDisabled or isSettingsReadonly}">
                            <c:set var="emailCharsetOptions">
                                <emm:ShowByPermission token="charset.use.iso_8859_15">
                                    <mvc:option value="ISO-8859-15"><mvc:message code="mailing.iso-8859-15" /></mvc:option>
                                </emm:ShowByPermission>
                                <emm:ShowByPermission token="charset.use.utf_8">
                                    <mvc:option value="UTF-8"><mvc:message code="mailing.utf-8" /></mvc:option>
                                </emm:ShowByPermission>
                            </c:set>
                            <%@include file="fragments/mailing-settings-email-charsets-extended.jspf" %>
                            ${emailCharsetOptions}
                        </mvc:select>
                    </div>
                </emm:ShowByPermission>
                <emm:HideByPermission token="settings.extended">
                    <mvc:hidden path="emailMediatype.charset"/>
                    <mvc:hidden path="emailMediatype.linefeed"/>
                </emm:HideByPermission>
            </div>
    
            <emm:ShowByPermission token="settings.extended">
                <div id="onepixel-field">
                    <label class="form-label" for="emailOnepixel">
                        <mvc:message code="openrate.measure"/>
                    </label>
                    <mvc:select path="emailMediatype.onepixel" id="emailOnepixel" size="1"
                                cssClass="form-control js-select" disabled="${emailSettingsDisabled or isSettingsReadonly}">
                        <mvc:option value="<%= MediatypeEmail.ONEPIXEL_TOP %>">
                            <mvc:message code="mailing.openrate.top"/>
                        </mvc:option>
                        <mvc:option value="<%= MediatypeEmail.ONEPIXEL_BOTTOM %>">
                            <mvc:message code="mailing.openrate.bottom"/>
                        </mvc:option>
                        <mvc:option value="<%= MediatypeEmail.ONEPIXEL_NONE %>">
                            <mvc:message code="openrate.none"/>
                        </mvc:option>
                    </mvc:select>
                </div>
            </emm:ShowByPermission>
            <emm:HideByPermission token="settings.extended">
                <mvc:hidden path="emailMediatype.onepixel"/>
            </emm:HideByPermission>
    
            <emm:ShowByPermission token="campaign.show">
                <div>
                    <label class="form-label">
                        <label for="settings_general_campaign"><mvc:message code="mailing.archive"/></label>
                        <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/CampaignMsg.xml"></a>
                    </label>
                    <mvc:select path="archiveId" id="settings_general_campaign" cssClass="form-control js-select" disabled="${workflowDriven or isSettingsReadonly}" data-workflow-driven="${workflowDriven}">
                        <mvc:option value="0"><mvc:message code="mailing.NoCampaign"/></mvc:option>
                        <c:forEach var="archive" items="${archives}" end="500">
                            <mvc:option value="${archive.id}">${archive.shortname}</mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>
                <div>
                    <div class="form-check form-switch mt-auto">
                        <mvc:checkbox cssClass="form-check-input" path="archived" disabled="${not emailSettingsEditable or workflowDriven or isSettingsReadonly}" role="switch" data-workflow-driven="${workflowDriven}"/>
                        <label class="form-label form-check-label">
                            <mvc:message code="mailing.archived"/>
                            <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/ShowInOnlineArchive.xml"></a>
                        </label>
                    </div>
                </div>
            </emm:ShowByPermission>
            <emm:HideByPermission token="campaign.show">
                <mvc:hidden path="archiveId"/>
            </emm:HideByPermission>
    
            <%@include file="fragments/mailing-settings-frequency-toggle.jspf" %>
        </c:if>
    </div>
</div>
