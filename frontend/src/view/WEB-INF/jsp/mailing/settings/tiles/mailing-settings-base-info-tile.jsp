<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="helplanguage" type="java.lang.String"--%>
<%--@elvariable id="gridTemplateId" type="java.lang.Integer"--%>
<%--@elvariable id="adminDateFormat" type="java.lang.String"--%>
<%--@elvariable id="worldMailingSend" type="java.lang.Boolean"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" scope="request"/>
<c:set var="workflowDriven" value="${workflowParams.workflowId gt 0}"/>
<c:set var="isMailingGrid" value="${gridTemplateId > 0}"/>

<div id='base-info-tile' class="tile" data-action="scroll-to">
    <div class="tile-header">
        <a href="#" class="headline" data-toggle-tile="#tile-mailingBase">
            <i class="tile-toggle icon icon-angle-up"></i>
            <mvc:message code="mailing.base.information"/>
        </a>
    </div>
    <div id="tile-mailingBase" class="tile-content tile-content-forms">
        <div class="form-group" data-field="validator">
            <div class="col-sm-4">
                <label class="control-label" for="mailingShortname">
                    <c:set var="nameMsg"><mvc:message code="default.Name"/></c:set>
                    ${nameMsg}*
                </label>
            </div>
            <div class="col-sm-8">
                <mvc:text id="mailingShortname" cssClass="form-control" path="shortname" maxlength="99"
                          data-field-validator="length"
                          data-validator-options="required: true, min: 3, max: 99" placeholder="${nameMsg}"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label" for="mailingDescription">
                    <c:set var="descriptionMsg"><mvc:message code="default.description"/></c:set>
                    ${descriptionMsg}
                </label>
            </div>
            <div class="col-sm-8">
                <mvc:textarea id="mailingDescription" cssClass="form-control v-resizable" path="description" rows="5" cols="32" placeholder="${descriptionMsg}"/>
            </div>
        </div>

        <c:if test="${not isTemplate}">
            <div class="form-group">
                <div class="col-sm-4">
                    <label class="control-label" for="mailingPlanDate">
                        <mvc:message code="mailing.plan.date"/>
                    </label>
                </div>
                <div class="col-sm-8">
                    <div class="input-group">
                        <c:set var="isReadonlyDate" value="${worldMailingSend || workflowDriven}"/>
                        <div class="input-group-controls">
                            <input type="text" name="planDate" value="${mailingSettingsForm.planDate}"
                                   id="mailingPlanDate" class="form-control datepicker-input js-datepicker"
                                   data-datepicker-options="format: '${fn:toLowerCase(adminDateFormat)}'" ${isReadonlyDate ? "disabled='disabled'" : ""}/>
                        </div>
                        <div class="input-group-btn">
                            <button type="button" class="btn btn-regular btn-toggle js-open-datepicker" tabindex="-1" ${isReadonlyDate ? "disabled='disabled'" : ""}>
                                <i class="icon icon-calendar-o"></i>
                            </button>
                            <c:if test="${workflowDriven}">
                                <c:url var="editWithCampaignLink" value="/workflow/${workflowId}/view.action">
                                    <c:param name="forwardParams"
                                             value="${workflowParams.workflowForwardParams};elementValue=${mailingId}"/>
                                </c:url>
                                <a href="${editWithCampaignLink}" class="btn btn-info btn-regular"
                                   data-tooltip="<mvc:message code='mailing.EditWithCampaignManager'/>">
                                    <i class="icon icon-linkage-campaignmanager"></i>
                                    <strong><mvc:message code="campaign.manager.icon"/></strong>
                                </a>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
        <div class="form-group">
            <div class="col-sm-4">
                <label class="control-label checkbox-control-label" for="mailingContentTypeAdvertising">
                    <mvc:message code="mailing.contentType.advertising"/>
                    <button class="icon icon-help" data-help="help_${helplanguage}/mailing/view_base/AdvertisingMsg.xml" tabindex="-1" type="button"></button>
                </label>
            </div>
            <div class="col-sm-8">
                <label class="toggle">
                    <mvc:checkbox path="mailingContentTypeAdvertising" id="mailingContentTypeAdvertising"/>
                    <div class="toggle-control"></div>
                </label>
            </div>
        </div>
        <%@include file="../fragments/mailing-frequency-toggle.jspf" %>

        <c:if test="${isMailingGrid and (not empty mailingId and mailingId ne 0 or isCopying)}">
            <%@include file="../fragments/mailing-settings-grid-owner.jspf" %>
        </c:if>
    </div>
</div>
