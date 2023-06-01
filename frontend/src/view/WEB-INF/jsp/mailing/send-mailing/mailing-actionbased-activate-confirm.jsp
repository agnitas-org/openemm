<%@ page contentType="text/html; charset=utf-8" errorPage="/error.do" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="agn" uri="https://emm.agnitas.de/jsp/jstl/tags" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.components.form.MailingSendForm"--%>
<%--@elvariable id="mailingSubject" type="java.lang.String"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <mvc:form servletRelativeAction="/mailing/send/activate-action-based.action" modelAttribute="form">
                <mvc:hidden path="mailingID" />

                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                    <h4 class="modal-title"><mvc:message code="Mailing"/>:&nbsp;${form.shortname}</h4>
                </div>

                <div class="modal-body">
                    <%@ include file="fragments/mailing-size-message-initialize.jspf" %>

                    <%-- Show the size value at least in KB since the byte number is inaccurate anyway --%>
                    <mvc:message code="mailing.action.confirm.activation" arguments="${form.shortname}& ${mailingSubject}& ${sizeMessage}" argumentSeparator="&"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Activate"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
