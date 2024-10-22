<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/errorRedesigned.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/send/deactivate.action">

            <input type="hidden" name="mailingID" value="${mailingId}">

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${shortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="mailing.deactivate.question"/>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary js-confirm-positive">
                    <i class="icon icon-state-alert"></i>
                    <span class="text"><mvc:message code="btndeactivate"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
