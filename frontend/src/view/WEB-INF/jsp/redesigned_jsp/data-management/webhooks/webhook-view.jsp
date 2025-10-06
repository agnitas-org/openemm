<%@ page contentType="text/html; charset=utf-8" errorPage="/errorRedesigned.action" %>
<%@ page import="com.agnitas.emm.core.service.RecipientStandardField" %>
<%@ page import="com.agnitas.emm.core.webhooks.common.WebhookEventType" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"	uri="http://java.sun.com/jsp/jstl/core" %>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/webhooks/${WEBHOOK_EVENT_TYPE}/save.action" id="webhookConfigForm"
                  data-form="resource"
                  modelAttribute="webhookConfigForm" method="POST">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="webhooks.event.${WEBHOOK_EVENT_TYPE}.label"/></h1>
                <button type="button" class="btn-close" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body vstack gap-3">
                <div>
                    <label class="form-label" for="webhook_url"><mvc:message code="webhooks.url"/></label>
                    <mvc:text path="url" maxlength="1000" cssClass="form-control" id="webhook_url"/>
                </div>

                <c:if test="${WEBHOOK_EVENT_TYPE.includesRecipientData}">
                    <div>
                        <label class="form-label" for="profileFields"><mvc:message code="recipient.fields"/></label>
                        <mvc:select path="includedProfileFields" multiple="true" id="profileFields" cssClass="form-control js-select"
                                    data-result-template="target-group-select-item"
                                    data-selection-template="target-group-select-item">
                            <c:forEach var="field" items="${PROFILE_FIELDS}">
                                <mvc:option value="${field.columnName}" data-historized="${RecipientStandardField.isHistorized(field)}">
                                    ${field.shortName}
                                </mvc:option>
                            </c:forEach>
                        </mvc:select>
                    </div>
                </c:if>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-form-submit>
                    <i class="icon icon-save"></i>
                    <mvc:message code="button.Save"/>
                </button>
            </div>
        </mvc:form>
    </div>
</div>

<script id="target-group-select-item" type="text/x-mustache-template">
    <div class="d-flex gap-1">
        {{ if (${WEBHOOK_EVENT_TYPE eq WebhookEventType.PROFILE_FIELD_CHANGED} && element.dataset.historized === 'false') { }}
        <span class="status-badge status.bell-slash" data-tooltip="<mvc:message code='GWUA.untracked'/>"></span>
        {{ } }}
        <span>{{= text }}</span>
    </div>
</script>
