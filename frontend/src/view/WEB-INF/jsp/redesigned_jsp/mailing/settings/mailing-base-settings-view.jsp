<%@ page contentType="text/html; charset=utf-8" buffer="64kb" errorPage="/errorRedesigned.action" %>

<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="noMailingTemplatesFound" type="java.lang.Boolean"--%>
<%--@elvariable id="workflowId" type="java.lang.Integer"--%>
<%--@elvariable id="mediaType" type="java.lang.Integer"--%>
<%--@elvariable id="templateShortname" type="java.lang.String"--%>
<%--@elvariable id="mailinglistEditable" type="java.lang.Boolean"--%>
<%--@elvariable id="isGridMailing" type="java.lang.Boolean"--%>
<%--@elvariable id="emailPlaceholder" type="java.lang.String"--%>
<%--@elvariable id="mailingSettingsForm" type="com.agnitas.emm.core.mailing.forms.MailingSettingsForm"--%>
<%--@elvariable id="selectedRemovedMailinglist" type="com.agnitas.beans.Mailinglist"--%>
<%--@elvariable id="mailinglists" type="java.util.List<com.agnitas.beans.Mailinglist>"--%>
<%--@elvariable id="parentMailings" type="java.util.List<org.agnitas.emm.core.mailing.beans.LightweightMailingWithMailingList>"--%>

<c:set var="workflowParams" value="${emm:getWorkflowParamsWithDefault(pageContext.request, workflowId)}" />
<c:set var="workflowDriven" value="${workflowParams.workflowId gt 0 or not empty workflowForwardParams}" />

<c:set var="emailSettingsDisabled" value="${not isGridMailing && emm:permissionAllowed('mailing.settings.hide', pageContext.request)}" />

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg" data-controller="mailing-settings-base-view">
        <mvc:form servletRelativeAction="/mailing/generate.action?mediaType=${mediaType}&keepForward=${workflowDriven}&isGrid=${isGridMailing eq true}"
                  method="POST" modelAttribute="mailingSettingsForm" cssClass="modal-content" data-form="resource"
                  data-form-focus="shortname" data-disable-controls="generate">
            <div class="modal-header">
                <h1 class="modal-title">
                    <span class="text-capitalize"><mvc:message code="mailing.base.information" /></span>
                    <c:choose>
                        <c:when test="${isGridMailing}">
                            <span class="status-badge mailing.status.emc badge--dark-blue" data-tooltip="<mvc:message code="mailing.grid.GridMailing" />"></span>
                        </c:when>
                        <c:when test="${mediaType eq 0}">
                            <span class="status-badge mailing.mediatype.email badge--blue" data-tooltip="<mvc:message code="mailing.standard" />"></span>
                        </c:when>
                        <c:when test="${mediaType eq 2}">
                            <span class="status-badge mailing.mediatype.post badge--dark-yellow" data-tooltip="<mvc:message code="mailing.MediaType.2" />"></span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge mailing.mediatype.sms badge--cyan" data-tooltip="<mvc:message code="mailing.MediaType.4" />"></span>
                        </c:otherwise>
                    </c:choose>
                </h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body vstack gap-3 js-scrollable" data-action="change-mailing-base-info">
                <mvc:hidden path="parentId" />
                <emm:workflowParameters />

                <c:if test="${not empty templateShortname}">
                    <div>
                        <label for="selected-template" class="form-label"><mvc:message code="Template" /></label>
                        <input id="selected-template" type="text" class="form-control" value="${fn:escapeXml(templateShortname)}" readonly>
                    </div>
                </c:if>

                <div data-field="validator">
                    <mvc:message var="nameMsg" code="default.Name" />
                    <label class="form-label" for="mailingShortname">${nameMsg}</label>
                    <mvc:text id="mailingShortname" cssClass="form-control" path="shortname" maxlength="99"
                              data-field-validator="length"
                              data-validator-options="required: true, min: 3, max: 99" placeholder="${nameMsg}" />
                </div>

                <c:if test="${mediaType eq 0}">
                    <div data-field="validator">
                        <label class="form-label" for="emailSubject"><mvc:message code="mailing.Subject"/></label>
                        <mvc:text path="emailMediatype.subject" id="emailSubject"
                                  cssClass="form-control" readonly="${emailSettingsDisabled}"
                                  data-field-validator="length" data-validator-options="required: true, min: 2" />
                    </div>
                </c:if>

                <div data-field="validator">
                    <label class="form-label" for="settingsGeneralMailingList">
                        <mvc:message code="Mailinglist" />
                        <a href="#" class="icon icon-question-circle" data-help="mailing/view_base/MailingListMsg.xml"></a>
                        <c:if test="${not mailinglistEditable}">
                            <i class="icon icon-exclamation-triangle" data-tooltip="<mvc:message code="warning.mailinglist.disabled" />"></i>
                        </c:if>
                    </label>

                    <mvc:select path="mailinglistId" id="settingsGeneralMailingList" size="1"
                                cssClass="form-control js-select"
                                data-action="save-mailing-list-id"
                                disabled="${not mailinglistEditable or emailSettingsDisabled or workflowDriven}"
                                data-field-validator="mailinglist-exist"
                                readonly="${parentMailings ne null}"
                                data-workflow-driven="${workflowDriven}"
                                data-validator-options='removedMailinglistId: ${not empty selectedRemovedMailinglist ? selectedRemovedMailinglist.id : ""}'>
                        <c:if test="${not empty selectedRemovedMailinglist}">
                            <mvc:option value="${selectedRemovedMailinglist.id}" id="${selectedRemovedMailinglist.id}-mailinglist">
                                ${selectedRemovedMailinglist.shortname}
                            </mvc:option>
                        </c:if>
                        <mvc:option value="0" styleId="0-mailinglist">--</mvc:option>
                        <c:forEach var="mailinglist" items="${mailinglists}">
                            <mvc:option value="${mailinglist.id}" id="${mailinglist.id}-mailinglist">
                                ${mailinglist.shortname}
                            </mvc:option>
                        </c:forEach>
                    </mvc:select>
                </div>

                <c:if test="${mediaType eq 0}">
                    <div>
                        <label class="form-label" for="emailSenderMail"><mvc:message code="mailing.SenderEmail" /></label>
                        <%@include file="tiles/fragments/domain-addresses-dropdown.jspf" %>
                        <c:choose>
                            <c:when test="${domainAddressesDropdown eq null}">
                                <mvc:text path="emailMediatype.fromEmail" id="emailSenderMail"
                                          cssClass="form-control" readonly="${emailSettingsDisabled}"
                                          data-field="required" placeholder="${emailPlaceholder}" />
                            </c:when>
                            <c:otherwise>
                                ${domainAddressesDropdown}
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div>
                        <label class="form-label" for="emailReplyEmail"><mvc:message code="mailing.ReplyEmail" /></label>
                        <mvc:text path="emailMediatype.replyEmail" id="emailReplyEmail"
                                  cssClass="form-control" readonly="${emailSettingsDisabled}"
                                  data-field="required"
                                  placeholder="${emailPlaceholder}" />
                    </div>
                </c:if>

                <c:if test="${mediaType eq 4}">
                    <div data-field="validator">
                        <label class="form-label" for="mediaSmsSender"><mvc:message code="mailing.Sender_Adress"/></label>
                        <mvc:text path="smsMediatype.fromAdr" id="mediaSmsSender" cssClass="form-control" maxlength="50"
                                  data-field-validator="length" data-validator-options="required: true, min: 3, max: 50" />
                    </div>
                </c:if>

                <%@include file="fragments/mailing-base-extended-inputs.jspf" %>
            </div>

            <div class="modal-footer modal-footer--nav">
                <mvc:message var="backMsg" code="mailing.template.back" />

                <c:choose>
                    <c:when test="${isGridMailing}">
                        <c:url var="backUrl" value="/layoutbuilder/released.action">
                            <c:param name="keepForward" value="${workflowDriven}" />
                        </c:url>
                    </c:when>
                    <c:when test="${noMailingTemplatesFound or mediaType eq 2}">
                        <mvc:message var="backMsg" code="mailing.create.back" />

                        <c:if test="${not workflowDriven}">
                            <c:url var="backUrl" value="/mailing/create.action">
                                <c:param name="keepForward" value="${workflowDriven}" />
                            </c:url>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <c:url var="backUrl" value="/mailing/templates.action">
                            <c:param name="keepForward" value="${workflowDriven}" />
                            <c:param name="mediaType" value="${mediaType}" />
                        </c:url>
                    </c:otherwise>
                </c:choose>

                <c:if test="${not empty backUrl}">
                    <a href="${backUrl}" class="btn btn-secondary" data-bs-dismiss="modal" data-confirm>
                        <i class="icon icon-angle-left"></i>
                        <span class="mobile-hidden">${backMsg}</span>
                        <span class="mobile-visible"><mvc:message code="button.Back" /></span>
                    </a>
                </c:if>

                <button id="generateBtn" class="btn btn-success btn-grey-out flex-grow-1" type="button"
                        data-tooltip="<mvc:message code="error.mailing.create.empty" />"
                        ${workflowDriven ? 'data-action="generate-mailing-for-workflow"' : 'data-form-submit'}
                        data-controls-group="generate">
                    <i class="icon icon-play-circle"></i>
                    <span><mvc:message code="grid.template.create.mailing" /></span>
                </button>
            </div>

            <script data-initializer="mailing-settings-base-view" type="application/json">
                {
                    "selectedRemovedMailinglistId": ${emm:toJson(selectedRemovedMailinglist.id)},
                    "mailinglists": ${emm:toJson(mailinglists)},
                    "isFollowUpMailing": ${parentMailings ne null}
                }
            </script>
        </mvc:form>
    </div>
</div>

<script id="edit-with-campaign-btn" type="text/x-mustache-template">
    <%@include file="fragments/edit-with-campaign-btn.jspf" %>
</script>
