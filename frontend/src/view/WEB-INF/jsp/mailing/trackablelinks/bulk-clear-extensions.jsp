<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="emm" uri="https://emm.agnitas.de/jsp/jsp/common" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>

<emm:CheckLogon/>

<div class="modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close-icon close js-confirm-negative" data-dismiss="modal">
                    <i aria-hidden="true" class="icon icon-times-circle"></i>
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
                <h4 class="modal-title">
                    <mvc:message code="ClearAllProperties"/>
                </h4>
            </div>

            <mvc:form servletRelativeAction="/mailing/${mailingId}/trackablelink/bulkClearExtensions.action" modelAttribute="trackableLinksForm">
                <%@include file="fragments-new/bulk_action/link-clear-extensions.jspf" %>
            </mvc:form>
        </div>
    </div>
</div>
