<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="affectedRecipientsCount" type="java.lang.Integer"--%>

<c:url var="saveUrl" value="/recipient/bulkSave.action" />

<div class="modal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="recipient.change.bulk.apply"/></h1>
                <button type="button" class="btn-close" data-confirm-negative>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <p><mvc:message code="recipient.change.bulk.question" arguments="${[affectedRecipientsCount]}"/></p>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-danger" data-confirm-negative>
                    <i class="icon icon-times"></i>
                    <span class="text"><mvc:message code="button.Cancel"/></span>
                </button>

                <button type="button" class="btn btn-primary" data-bs-dismiss="modal"
                        data-form-target='#recipientBulkForm' data-form-url="${saveUrl}" data-form-submit>
                    <i class="icon icon-check"></i>
                    <span class="text"><mvc:message code="button.Apply"/></span>
                </button>
            </div>
        </div>
    </div>
</div>
