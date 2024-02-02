<%@ page language="java" contentType="text/html; charset=utf-8"  errorPage="/error.action" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal"><i aria-hidden="true" class="icon icon-times-circle"></i><span class="sr-only"><mvc:message code="button.Cancel"/></span></button>
                <h4 class="modal-title">
                    <mvc:message code="${param.headerMessageKey}"/>
                </h4>
            </div>
            <c:set var="action" value="/workflow/${param.bulkOperation}"/>
            <mvc:form servletRelativeAction="${action}" method="POST">
                <c:forEach var="workflowlistId" items="${bulkForm.bulkIds}">
                    <input type="hidden" name="bulkIds" value="${workflowlistId}"/>
                </c:forEach>

                <div class="modal-body">
                    <mvc:message code="${param.bulkActionQuestion}"/>
                </div>

                <div class="modal-footer">
                    <div class="btn-group">
                        <button type="button" class="btn btn-default btn-large js-confirm-negative" data-dismiss="modal">
                            <i class="icon icon-times"></i>
                            <span class="text"><mvc:message code="button.Cancel"/></span>
                        </button>
                        <button type="button" class="btn btn-primary btn-large js-confirm-positive" data-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="${param.bulkActionButton}"/></span>
                        </button>
                    </div>
                </div>
            </mvc:form>

        </div>
    </div>
</div>
