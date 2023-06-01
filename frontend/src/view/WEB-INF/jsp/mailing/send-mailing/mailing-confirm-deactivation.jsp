<%@ page contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.do" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailing/send/deactivate.action" modelAttribute="form">
                <mvc:hidden path="mailingID"/>

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                    <h4 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${form.shortname}</h4>
                </div>

                <div class="modal-body">
                    <mvc:message code="mailing.deactivate.question"/>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="btndeactivate"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
