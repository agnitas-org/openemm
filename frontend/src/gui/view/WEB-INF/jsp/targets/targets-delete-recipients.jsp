<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="targetIdToDeleteRecipients" type="java.lang.Integer"--%>
<%--@elvariable id="numberOfRecipients" type="java.lang.Integer"--%>

<mvc:form cssClass="modal modal-adaptive" tabindex="-1" servletRelativeAction="/target/${targetIdToDeleteRecipients}/delete/recipients.action" method="POST">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="target.delete.recipients"/></h1>

                <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <p>
                    <c:choose>
                        <c:when test="${numberOfRecipients gt 0}">
                            <mvc:message code="target.delete.recipients.question" arguments="${numberOfRecipients}" />
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="target.delete.empty" />
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>

            <div class="modal-footer">
                <c:choose>
                    <c:when test="${numberOfRecipients gt 0}">
                        <button type="button" class="btn btn-danger js-confirm-positive" data-bs-dismiss="modal">
                            <i class="icon icon-trash-alt"></i>
                            <span class="text"><mvc:message code="button.Delete" /></span>
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="btn btn-primary js-confirm-negative" data-bs-dismiss="modal">
                            <i class="icon icon-check"></i>
                            <span class="text"><mvc:message code="button.OK" /></span>
                        </button>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </div>
</mvc:form>
