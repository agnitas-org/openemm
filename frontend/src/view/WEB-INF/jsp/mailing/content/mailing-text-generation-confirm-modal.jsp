<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/content/${mailingId}/text-from-html/generate.action">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${mailingShortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <mvc:message code="mailing.generateText.question"/>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger" data-confirm-negative>
                    <i class="icon icon-times"></i>
                    <mvc:message code="default.No"/>
                </button>
                <button type="button" class="btn btn-primary" data-confirm-positive>
                    <i class="icon icon-check"></i>
                    <mvc:message code="default.Yes"/>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
