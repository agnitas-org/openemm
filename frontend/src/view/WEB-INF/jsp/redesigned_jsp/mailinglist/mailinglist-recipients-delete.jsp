<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="deleteForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistRecipientDeleteForm"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailinglist/recipientsDelete.action" modelAttribute="deleteForm">
            <mvc:hidden path="id"/>
            <mvc:hidden path="shortname"/>
            <mvc:hidden path="onlyActiveUsers"/>
            <mvc:hidden path="noAdminAndTestUsers"/>

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="mailinglist.delete.recipientsOf"/>&nbsp;${deleteForm.shortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="mailinglist.delete.recipients.question"/>
            </div>
            <div class="modal-footer">
                <emm:ShowByPermission token="mailinglist.recipients.delete">
                    <button type="button" class="btn btn-danger js-confirm-positive" data-dismiss="modal" data-form-submit>
                        <i class="icon icon-trash-alt"></i>
                        <span><mvc:message code="default.Yes"/></span>
                    </button>
                </emm:ShowByPermission>
            </div>
        </mvc:form>
    </div>
</div>
