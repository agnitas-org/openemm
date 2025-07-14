<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="bulkDeleteForm" type="com.agnitas.web.forms.BulkActionForm"--%>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <mvc:message code="bulkAction.delete.action"/>
                </h4>
            </div>

            <mvc:form servletRelativeAction="/action/bulkDelete.action" modelAttribute="bulkDeleteForm" method="DELETE">
                <div class="modal-body">
                    <mvc:message code="bulkAction.delete.action.question"/>
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

                <c:forEach var="actionId" items="${bulkDeleteForm.bulkIds}">
                    <input type="hidden" name="bulkIds" value="${actionId}"/>
                </c:forEach>
            </mvc:form>
        </div>
    </div>
</div>
