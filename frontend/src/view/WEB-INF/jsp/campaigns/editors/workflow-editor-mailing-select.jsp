<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<div class="form-column">
    <div>
        <c:if test="${not param.hideLabels}">
            <label class="form-label" for="${param.containerId}-mailing-status-select"><mvc:message code="Status"/></label>
        </c:if>
        <select name="${param.statusName}" id="${param.containerId}-mailing-status-select" class="form-control" data-action="${param.baseMailingEditor}-status-change">
            <c:if test="${param.containerId != 'mailing-editor'}">
                <option value="all" id="${param.containerId}-mailings_status_all"><mvc:message code="default.All"/></option>
            </c:if>
            <option value="${param.status1}" id="${param.containerId}-mailings_status_${param.status1}"><mvc:message code="${param.message1}"/></option>
            <option value="${param.status2}" id="${param.containerId}-mailings_status_${param.status2}"><mvc:message code="${param.message2}"/></option>
        </select>
    </div>

    <div>
        <c:if test="${not param.hideLabels}">
            <label class="form-label" for="${param.containerId}-mailing-select"><mvc:message code="Mailing"/></label>
        </c:if>
        <div class="d-flex gap-1">
            <select name="${param.selectName}" id="${param.containerId}-mailing-select" class="form-control"
                    data-action="${param.baseMailingEditor}-select-change"
                    ${param.disabledSelection == 'true' ? 'disabled' : ''}>
            </select>
            <c:if test="${param.showMailingLinks and not emm:permissionAllowed('mailing.content.readonly', pageContext.request)}">
                <div id="mailing_create_edit_link">
                    <a href="#" class="btn btn-icon btn-primary" data-tooltip="<mvc:message code="mailing.MailingEdit" />"
                        ${param.disabledSelection == 'true' ? 'data-action=\"mailing-editor-new\"' : ''}>
                        <i class="icon icon-plus"></i>
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</div>
