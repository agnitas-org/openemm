<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="deleteForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistRecipientDeleteForm"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailinglist/confirmRecipientsDelete.action" modelAttribute="deleteForm">
            <mvc:hidden path="id"/>
            <mvc:hidden path="shortname"/>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="mailinglist.delete.recipientsOf"/> ${deleteForm.shortname}</h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <div class="form-check form-switch mb-3">
                    <mvc:checkbox cssClass="form-check-input" path="onlyActiveUsers" role="switch" id="only-active-users"/>
                    <label class="form-label form-check-label" for="only-active-users"><mvc:message code="mailinglist.delete.recipients.active"/></label>
                </div>
                <div class="form-check form-switch">
                    <mvc:checkbox cssClass="form-check-input" path="noAdminAndTestUsers" role="switch" id="no-admin-and-test"/>
                    <label class="form-label form-check-label" for="no-admin-and-test"><mvc:message code="mailinglist.delete.recipients.noadmin"/></label>
                </div>
            </div>

            <div class="modal-footer">
                <emm:ShowByPermission token="mailinglist.recipients.delete">
                    <button type="button" class="btn btn-danger js-confirm-positive flex-grow-1">
                        <i class="icon icon-trash-alt"></i>
                        <span class="text"><mvc:message code="button.Delete"/></span>
                    </button>
                </emm:ShowByPermission>
            </div>
        </mvc:form>
    </div>
</div>
