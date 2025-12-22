<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/send/${mailingId}/resume-by-copy.action" data-form="static">

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${mailingShortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="mailing.generation.resumeByCopy.question"/>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary js-confirm-positive">
                    <i class="icon icon-check"></i>
                    <span class="text"><mvc:message code="default.Yes"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
