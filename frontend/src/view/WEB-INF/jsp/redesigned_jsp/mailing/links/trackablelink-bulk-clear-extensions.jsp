<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>

<div class="modal" tabindex="-1">
    <div class="modal-dialog modal-fullscreen-lg-down modal-lg modal-dialog-centered">
        <mvc:form cssClass="modal-content" servletRelativeAction="/mailing/${mailingId}/trackablelink/bulkClearExtensions.action" modelAttribute="trackableLinksForm">
            <div class="modal-header">
                <h1 class="modal-title"><mvc:message code="ClearAllProperties"/></h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>
            <div class="modal-body pb-0">
                <mvc:hidden path="bulkIds"/>
                <mvc:message code="bulkAction.delete.trackablelink.extension.question"/>
                <ul class="p-3">
                    <c:forEach var="extension" items="${extensionsToDelete}">
                        <li><strong>${extension.propertyName}=${extension.propertyValue}</strong></li>
                    </c:forEach>
                </ul>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-danger flex-grow-1 js-confirm-positive" data-dismiss="modal">
                    <i class="icon icon-trash-alt"></i>
                    <span class="text"><mvc:message code="button.Delete"/></span>
                </button>
            </div>
        </mvc:form>
    </div>
</div>
