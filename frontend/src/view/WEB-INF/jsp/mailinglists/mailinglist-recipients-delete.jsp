<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="mvc"     uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm"     uri="https://emm.agnitas.de/jsp/jsp/common" %>

<%--@elvariable id="deleteForm" type="com.agnitas.emm.core.mailinglist.form.MailinglistRecipientDeleteForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailinglist/recipientsDelete.action" modelAttribute="deleteForm">
                <mvc:hidden path="id"/>
                <mvc:hidden path="shortname"/>
                <mvc:hidden path="onlyActiveUsers"/>
                <mvc:hidden path="noAdminAndTestUsers"/>

                    <div class="modal-header">
                        <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                            <i aria-hidden="true" class="icon icon-times-circle"></i>
                            <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                            </button>
                        <h4 class="modal-title">
                            <mvc:message code="mailinglist.delete.recipientsOf"/> ${deleteForm.shortname}
                        </h4>
                    </div>
                    <div class="modal-body">
                        <mvc:message code="mailinglist.delete.recipients.question"/>
                    </div>
                    <div class="modal-footer">
                        <div class="btn-group">
                            <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                                <i class="icon icon-times"></i>
                                <span class="text"><mvc:message code="button.Cancel"/></span>
                            </button>
                        <emm:ShowByPermission token="mailinglist.recipients.delete">
                            <button type="button" class="btn btn-primary btn-large" data-dismiss="modal" data-form-submit>
                                <i class="icon icon-check"></i>
                                <span><mvc:message code="default.Yes"/></span>
                            </button>
                        </emm:ShowByPermission>
                        </div>
                    </div>
            </mvc:form>
        </div>
    </div>
</div>
