<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="mailingShortname" type="java.lang.String"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailing/send/${mailingId}/resume.action">
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                    <h4 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${mailingShortname}</h4>
                </div>


                <div class="modal-body">
                    <mvc:message code="mailing.generation.resume.question"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="default.No"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="default.Yes"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
