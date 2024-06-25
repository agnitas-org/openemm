<%@ taglib prefix="mvc"   uri="https://emm.agnitas.de/jsp/jsp/spring" %>
<%@ taglib prefix="c"     uri="http://java.sun.com/jsp/jstl/core" %>

<%--@elvariable id="mailingId" type="java.lang.Integer"--%>
<%--@elvariable id="isTemplate" type="java.lang.Boolean"--%>
<%--@elvariable id="shortname" type="java.lang.String"--%>

<mvc:form cssClass="modal modal-adaptive" servletRelativeAction="/mailing/${mailingId}/undo.action" method="GET" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h1 class="modal-title">
                    <c:choose>
                        <c:when test="${isTemplate}">
                            <mvc:message code="Template"/>:&nbsp;${shortname}
                        </c:when>
                        <c:otherwise>
                            <mvc:message code="Mailing"/>:&nbsp;${shortname}
                        </c:otherwise>
                    </c:choose>
                </h1>
                <button type="button" class="btn-close shadow-none js-confirm-negative" data-bs-dismiss="modal">
                    <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                </button>
            </div>

            <div class="modal-body">
                <c:choose>
                    <c:when test="${isTemplate}">
                        <mvc:message code="mailing.Undo_Template_Question"/>
                    </c:when>
                    <c:otherwise>
                        <mvc:message code="mailing.MailingUndoQuestion"/>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn btn-primary js-confirm-positive flex-grow-1" data-bs-dismiss="modal">
                    <i class="icon icon-undo"></i>
                    <span class="text"><mvc:message code="button.Undo"/></span>
                </button>
            </div>
        </div>
    </div>
</mvc:form>
