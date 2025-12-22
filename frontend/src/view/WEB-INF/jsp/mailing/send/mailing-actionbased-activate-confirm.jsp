<%@ page contentType="text/html; charset=utf-8" errorPage="/error.action" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="mailingSubject" type="java.lang.String"--%>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/send/activate-action-based.action">

            <input type="hidden" name="mailingID" value="${mailingId}">

            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${shortname}</h1>
                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body">
                <%@ include file="fragments/mailing-size-message-initialize.jspf" %>
                <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                <mvc:message code="mailing.action.confirm.activation" arguments="${[shortname, mailingSubject, sizeMessage]}"/>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary js-confirm-positive">
                    <i class="icon icon-check"></i>
                    <span class="text"><mvc:message code="button.Activate"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
