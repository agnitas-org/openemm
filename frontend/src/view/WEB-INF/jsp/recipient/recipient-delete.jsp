<%@ page language="java" contentType="text/html; charset=utf-8" buffer="32kb" errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--@elvariable id="form" type="com.agnitas.emm.core.recipient.forms.RecipientSimpleActionForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">

            <mvc:form servletRelativeAction="/recipient/delete.action" modelAttribute="form" method="DELETE">
                <mvc:hidden path="id"/>
                <mvc:hidden path="email"/>
                <div class="modal-header">
                    <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                    <h4 class="modal-title"><mvc:message code="Recipient"/>:&nbsp;${fn:escapeXml(form.shortname)}</h4>
                </div>

                <div class="modal-body">
                    <mvc:message code="recipient.confirm_delete"/>
                </div>
                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.Delete"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>
        </div>
    </div>
</div>
