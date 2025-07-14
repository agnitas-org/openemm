<%@ page import="com.agnitas.emm.common.MailingType" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="selectedRemovedMailinglist" type="com.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="archives" type="java.util.List<com.agnitas.beans.Campaign>"--%>
<%--@elvariable id="emailSettingsEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="mailinglistEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="MAILING_EDITABLE" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>

<c:set var="ACTIONBASED_MAILING_TYPE" value="<%= MailingType.ACTION_BASED %>"/>
<c:set var="DATEBASED_MAILING_TYPE" value="<%= MailingType.DATE_BASED %>"/>
<c:set var="INTERVAL_MAILING_TYPE" value="<%= MailingType.INTERVAL %>"/>
<c:set var="FOLLOWUP_MAILING_TYPE" value="<%= MailingType.FOLLOW_UP %>"/>
<c:set var="NORMAL_MAILING_TYPE" value="<%= MailingType.NORMAL %>"/>
<c:set var="workflowDriven" value="${workflowId gt 0}"/>

<div class="tile" data-action="change-mailing-settings">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingGeneral">
            <i class="tile-toggle icon icon-angle-up"></i>
            <mvc:message code="mailing.generalSettings"/>
        </a>
    </div>
    <div id="tile-mailingGeneral" class="tile-content tile-content-forms" data-action="scroll-to">
        <div class="form-group" data-field="validator">
            <div class="col-sm-4">
                <label class="control-label">
                    <label for="settingsGeneralMailingList"><mvc:message code="Mailinglist"/>*</label>
                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MailingListMsg.xml" tabindex="-1" type="button"></button>
                    <c:if test="${not mailinglistEditable}">
                        <div class="icon icon-warning" data-tooltip="<mvc:message code="warning.mailinglist.disabled"/>"></div>
                    </c:if>
                </label>
            </div>
            <div class="col-sm-8">
                <div class="input-group">
                    <div class="input-group-controls">
                        <c:set var="isMailinglistDenied" value="${not mailinglistEditable or not MAILING_EDITABLE or not emailSettingsEditable or workflowDriven or isSettingsReadonly}"/>
                        <mvc:select path="mailinglistId" id="settingsGeneralMailingList" cssClass="form-control js-select" size="1"
                                       data-action="save-mailing-list-id"
                                       disabled="${isMailinglistDenied}"
                                       data-field-validator="mailinglist-exist"
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
                    <%@include file="../fragments/edit-with-campaign-btn.jspf" %>
                </div>
            </div>
        </div>

        <emm:ShowByPermission token="campaign.show">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <label for="settings_general_campaign"><mvc:message code="mailing.archive"/></label>
                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/CampaignMsg.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-4">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:select path="archiveId" id="settings_general_campaign" cssClass="form-control js-select" disabled="${workflowDriven or isSettingsReadonly}">
                                <mvc:option value="0"><mvc:message code="mailing.NoCampaign"/></mvc:option>
                                <c:forEach var="archive" items="${archives}" end="500">
                                    <mvc:option value="${archive.id}">${archive.shortname}</mvc:option>
                                </c:forEach>
                            </mvc:select>
                        </div>
                        <%@include file="../fragments/edit-with-campaign-btn.jspf" %>
                    </div>
                </div>
                <div class="col-sm-4">
                    <label class="toggle">
                        <mvc:checkbox path="archived" disabled="${not emailSettingsEditable or workflowDriven or isSettingsReadonly}"/>
                        <div class="toggle-control"></div>
                        <span class="text">
                            <mvc:message code="mailing.archived"/>
                        	<button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/ShowInOnlineArchive.xml" tabindex="-1" type="button" data-original-title="" title=""></button>
                    	</span>
                	</label>
            	</div>
            </div>
        </emm:ShowByPermission>
        <emm:HideByPermission token="campaign.show">
            <mvc:hidden path="archiveId"/>
        </emm:HideByPermission>

        <emm:ShowByPermission token="mailing.show.types">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label">
                        <label for="settingsGeneralMailType"><mvc:message code="mailing.Mailing_Type"/></label>
                        <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/MailingTypeMsg.xml" tabindex="-1" type="button"></button>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <div class="input-group-controls">
                            <mvc:select path="mailingType" id="settingsGeneralMailType" cssClass="form-control" 
                                        data-action="change-general-mailing-type"
                                        data-result-template="mailing-type-option"
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
								<%@include file="../fragments/mailingtype-follow-option.jspf" %>
								<%@include file="../fragments/mailingtype-interval-option.jspf" %>
                            </mvc:select>
                            
                            <script id="mailing-type-option" type="text/x-mustache-template">
                                {{ if(element.dataset.tooltip && element.dataset.tooltip.length) { }}
                                    <span data-tooltip="{{- element.dataset.tooltip }}" style="display: block">
                                        {{- text }}
                                    </span>
                                {{ } else { }}
                                    {{- text }}
                                {{ } }}
                            </script>
                        </div>
                        <%@include file="../fragments/edit-with-campaign-btn.jspf" %>
                    </div>
                    <%@include file="../fragments/mailing-settings-followup-help-block.jspf" %>
                </div>
            </div>
            <%@include file="../fragments/mailing-settings-followup-additional-inputs.jspf" %>
        </emm:ShowByPermission>
        <emm:HideByPermission token="mailing.show.types">
            <mvc:hidden path="mailingType"/>
        </emm:HideByPermission>
    </div>
</div>
